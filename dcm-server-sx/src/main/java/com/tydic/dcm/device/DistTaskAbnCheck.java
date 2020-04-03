package com.tydic.dcm.device;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.dcm.util.spring.SpringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分发链路异常数据回收
 * 1、当采集程序直接kill -9停止或者异常停止时，由于分发异常回收是先从分发异常表(dc_dist_task_abn)查询数据批量添加到分发任务表(dc_dist_task)，然后删除分发异常表数据，
 * 这时可能出现分发任务表数据添加成功，分发异常表数据未删除情况，导致两个表中同时存在数据，出现话单分发重复
 * 2、分发过程手动控制事务，出现写入分发日志表成功，写入SOURCE_FILES表等数据异常，回滚失败，dc_dist_log表数据出现重复问题（该问题不严重，只是出现分发日志表数据不准确问题）
 * @author Yuanh
 *
 */
public class DistTaskAbnCheck {

	// 日志对象
	private static Logger logger = Logger.getLogger(DistTaskAbnCheck.class);

	/**
	* @Description: 校验分发异常重复数据
	* @return boolean
	* @author yuanhao
	* @date 2019-11-19 9:10
	*/
	public static void checkDuplicateData(String groupId) {
		logger.debug("start check duplicate data, groupId: " + groupId);
		try {
			// 获取数据库连接对象
			SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
			// 设置上下文数据源
			DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);
			//查询在分发任务表和异常表都存在的数据
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("GROUP_ID", groupId);
			List<Map<String, Object>> list = sqlSession.selectList("distributeMapper.queryDistTaskAbnDuplicateList", queryParams);
			logger.info("query distribution task duplicate data, data: " + (list == null ? 0 : list.size()) + ", groupId: " + groupId);
			if (CollectionUtils.isNotEmpty(list)) {
				for (Map<String, Object> dupMap : list) {
					logger.warn("duplicate data: " + dupMap.toString());
				}
				int delCnt = sqlSession.delete("distributeMapper.delBatchDistTaskAbnCallbackList", list);
				logger.warn("delete distribution task duplicate data from dc_dist_task_abn, success size: " + delCnt + ", groupId: " + groupId);
			}
			logger.info("check duplicate data ok, groupId: " + groupId);
		} catch (Exception e) {
			logger.error("check duplicate data exception, Please check manually!");
			logger.error("", e);
		}
	}
}
