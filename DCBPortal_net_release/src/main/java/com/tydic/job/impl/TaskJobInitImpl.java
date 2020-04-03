package com.tydic.job.impl;

import com.esotericsoftware.minlog.Log;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.QuartzManager;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.Constant;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("taskJobInitService")
public class TaskJobInitImpl {
	@Autowired
	private CoreService coreService;

	@Autowired
	QuartzManager quartzManager;

	private static Logger logger = Logger.getLogger(TaskJobInitImpl.class);

	private static final String JOB_STATE_EFF_ONE = "1";

	private static final String JOB_STATE_NO_EFF_ZERO = "0";

	public void scanTaskJob() {
		logger.debug("定时任务扫描开始");
		// 查询符合条件的数据
		List<HashMap<String, Object>> dsArchiveJobList = coreService.queryForList2New("datasourceArchive.queryTaskJob",
				null, FrameConfigKey.DEFAULT_DATASOURCE);
		quartzManager.addJobList(getEffJobList(dsArchiveJobList));
		logger.debug("定时任务扫描结束");
	}

	/**
	 * 参数转换，并移除定时任务 中，数据归档和伸缩策略的状态为2的任务
	 * 
	 * @param jobList
	 *            List<HashMap<String, Object>> 转换为 List<Map<String, Object>>
	 * @return List<Map<String, Object>>
	 */
	private List<Map<String, Object>> getEffJobList(List<HashMap<String, Object>> jobList) {
		Log.debug("TaskJobInitImpl,定时任务扫描-参数转换--->"+jobList.toString());
		if (BlankUtil.isBlank(jobList)) {
			return new ArrayList<Map<String, Object>>();
		}
		List<Map<String, Object>> rusltList = new ArrayList<Map<String, Object>>();
		try{
			for (HashMap<String, Object> hashMap : jobList) {
				String jobName = StringTool.object2String(hashMap.get("JOB_NAME"));
				String state = StringTool.object2String(hashMap.get("STATE"));
				if (!JOB_STATE_EFF_ONE.equals(state)) {
					quartzManager.removeJob(jobName, null);
					logger.debug("移除状态为无效的定时任务--->jobName:" + jobName);
					continue;
				}
				Map<String, Object> jobMap = new HashMap<String, Object>();
				jobMap.put(QuartzConstant.JOB_NAME, jobName);
	
				// job类实例
				String busType = StringTool.object2String(hashMap.get("BUS_TYPE"));
				if (Constant.BUS_TYPE_ONE_EXPAND_STRATEGY.equals(busType) 
						|| Constant.BUS_TYPE_FIVE_DYNIMIC_CONF_EXPEND.equals(busType)) { //1 伸缩策略  5:动态伸缩策略
					jobMap.put(QuartzConstant.JOB_CLASS, Constant.JOB_CLASS_EXPAND_STRATEGY);
				} else if (Constant.BUS_TYPE_TWO_DB_ARCH.equals(busType)) { //2 数据归档
					jobMap.put(QuartzConstant.JOB_CLASS, Constant.JOB_CLASS_DB_ARCH);
				}else if (Constant.BUS_TYPE_THREE_FILE_ARCH.equals(busType)) { //3 文件归档
					jobMap.put(QuartzConstant.JOB_CLASS, Constant.JOB_CLASS_FILE_ARCH);
				} else if (Constant.BUS_TYPE_FOUR_DYNIMIC_CONF_REPORT.equals(busType)) {  //4动态伸缩管理
					jobMap.put(QuartzConstant.JOB_CLASS, Constant.JOB_CLASS_DYNAMIC_EXPEND_STRATEGY);
				} else {
					jobMap.put(QuartzConstant.JOB_CLASS, "");
					continue;
				}
				// cron表达式或者 数值（单位秒，循环）
				hashMapToMap(jobMap, QuartzConstant.TIME_KEY, hashMap, "TIME_KEY");
	
				// 参数
				Map<String, Object> busParamsMap = new HashMap<String, Object>();
				busParamsMap.put("BUS_TYPE", busType);
				busParamsMap.put("BUS_ID", hashMap.get("BUS_ID"));
				//扩展字段，动态伸缩扩展使用，对应的是预测报告ID
				busParamsMap.put("EXTENDED_FIELD", hashMap.get("EXTENDED_FIELD"));
				jobMap.put(QuartzConstant.BUS_PARAMS, busParamsMap);
	
				// 起止时间
				hashMapToMap(jobMap, QuartzConstant.START_TIME, hashMap, "START_TIME");
				hashMapToMap(jobMap, QuartzConstant.END_TIME, hashMap, "END_TIME");
				// TRIGGER_TYPE 1=cron表达式，2=常规 几秒循环（未验证）
				hashMapToMap(jobMap, QuartzConstant.TRIGGER_TYPE, hashMap, "TRIGGER_TYPE");
				// 优先级，置空，默认为5
				hashMapToMap(jobMap, QuartzConstant.TRIGGER_PRIORITY, hashMap, "");
				rusltList.add(jobMap);
			}
		}catch(Exception e){
			Log.error("定时任务列表获取失败， 失败原因:", e);
		}
		logger.debug("TaskJobInitImpl,有效的定时任务--->" + rusltList.toString());
		return rusltList;
	}

	private static void hashMapToMap(Map<String, Object> map, String mapkey, HashMap<String, Object> hashMap,
			String hashMapKey) {
		if (BlankUtil.isBlank(hashMapKey)) {
			map.put(mapkey, null);
		} else {
			Object obj = hashMap.get(hashMapKey);
			map.put(mapkey, obj);
		}
	}
}
