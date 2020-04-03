package com.tydic.dcm.device;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.PropertiesUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分发链路异常数据定时回收
 * @author Yuanh
 *
 */
public class DistTaskAbnCallbackThrd extends Thread {

	// 日志对象
	private static Logger logger = Logger.getLogger(DistTaskAbnCallbackThrd.class);

	// 定时器刷新间隔
	private static Long refreshInterval = NumberUtils.toLong(PropertiesUtil.getValueByKey(ParamsConstant.PARAMS_DIST_TASK_ABN_INTERVAL), ParamsConstant.DEFAULT_DIST_TASK_ABN_INTERVAL) * 1000;

	// 当前Dcm程序GroupId
	private String groupId;

	/**
	 * 一次性回收文件个数
	 */
	private static final int CALLBACK_ROWS = 500;


	/**
	 * 分发定时任务
	 *
	 * @param groupId          当前分发程序GroupId
	 */
	public DistTaskAbnCallbackThrd(String groupId) {
		//当前采集程序Group组
		this.groupId = groupId;
	}

	/**
	 * 分发定时回收
	 */
	@Override
	public void run() {
		//程序已启动初始化执行一次自动采集
		while(true) {
			long start = System.currentTimeMillis();//启动时间
			try {
				distTaskAbnCallBack();
				long end = System.currentTimeMillis();//完成时间
				long interval = end - start;
				if(interval >= refreshInterval){//在间隔时间内没有完成要马上进行扫描
					continue;
				} else {//在间隔时间内完成开始休眠
					sleep(refreshInterval - interval);
				}
			} catch (Exception e) {
				logger.error("distribute callback fail", e);
			};
		}
	}

	/**
	 * 回退回收数据
	 */
	private void distTaskAbnCallBack() {
		logger.debug("begin callback distribute task abn list, groupId: " + this.groupId + ", interval: " + refreshInterval);
		//查询当前分发采集程序对应的分发异常表是否存在异常数据

		// 获取数据库连接对象
		SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
		// 设置上下文数据源
		DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("GROUP_ID", groupId);
		queryParams.put("CALLBACK_ROWS", CALLBACK_ROWS);
		boolean callBackContinue = true;
		int callBackCnt = 0;
		long startTimes = System.currentTimeMillis();
		while (callBackContinue) {
			List<Map<String, Object>> list = JdbcUtil.queryForList("distributeMapper.queryDistTaskAbnListCallbackList",  queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
			logger.info("callback task data list, size: " + (list == null ? 0 : CollectionUtils.size(list)));
			if (CollectionUtils.isNotEmpty(list)) {
				int step = 0;
				try {
					//批量分发任务表
					int addCnt = sqlSession.insert("distributeMapper.addBatchDistTaskCallbackList", list);
					logger.info("add dist task abn callback, success size: " + addCnt);
					if (list.size() != addCnt) {
						logger.error("The number of recovered records is not equal to the number of abnormal queries, abn list: "
								+ list.size() + ", task list: " + addCnt);
					}
					step = 1;
					int delCnt = sqlSession.delete("distributeMapper.delBatchDistTaskAbnCallbackList", list);
					logger.info("del dist task abn callback, success size: " + delCnt);
					callBackCnt += list.size();
				} catch (Exception e) {
					callBackContinue = false;
					logger.error("callback dist task exception!", e);
					//删除分发任务表数据
					if (step == 1) {
						logger.info("delete dist task data for callback exception, list size: " + (list.size()));
						int delCnt = sqlSession.delete("distributeMapper.delBatchDistTaskkList", list);
						logger.info("del dist task callback, success size: " + delCnt);
					}
				}
			} else {
				callBackContinue = false;
			}
		}
		Long endTimes = System.currentTimeMillis();
		logger.info("callback dist task list success, total callback list: " + callBackCnt + ", total times:" + (endTimes - startTimes)/100 + "s");
	}
}
