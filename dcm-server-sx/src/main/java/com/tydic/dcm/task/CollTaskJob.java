package com.tydic.dcm.task;

import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.tydic.bp.QuartzConstant;
import com.tydic.dcm.ftran.CollLink;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;

/**
 * 采集链路Job
 * @author Yuanh
 *
 */
@DisallowConcurrentExecution
public class CollTaskJob implements Job {

	/**
	 * Log4J日志对象
	 */
	private static Logger logger = Logger.getLogger(CollTaskJob.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException{
		logger.debug("begin collect link job...");
		
		//获取Job业务参数
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Map<String, Object> jobParams = (Map<String, Object>) dataMap.get(QuartzConstant.BUS_PARAMS);
		
		//任务类型
		String taskType = StringTool.object2String(jobParams.get("task_type"));
		
		//链路ID
		String devId = StringTool.object2String(jobParams.get("devId"));
		logger.info("collect job params, taskType: " + taskType+"\tdevId: "+devId);
		
		try {
			//自动采集
			if (ParamsConstant.TASK_TYPE_AUTO_COLL.equalsIgnoreCase(taskType)) {
				CollLink link = new CollLink(devId);
				link.taskType = ParamsConstant.TASK_TYPE_AUTO_COLL;
				link.autoTransfer();
				logger.debug("auto collect Ok");
			} 
			//手动采集
			else if (ParamsConstant.TASK_TYPE_HAND_COLL.equalsIgnoreCase(taskType)) {
				//获取手动采集采集列表
				String file_list = StringTool.object2String(jobParams.get("file_list"));
				//是否启用链路过滤条件
				boolean enableFilter = Boolean.parseBoolean((String)jobParams.get("enable_filter"));
				
				//获取采集链路信息
				CollLink link = new CollLink(devId);
				link.taskType = ParamsConstant.TASK_TYPE_HAND_COLL;
				link.handTransfer(file_list, enableFilter);
				logger.debug("hand collect Ok, devId: " + devId);
			}
			//立即采集
			else if (ParamsConstant.TASK_TYPE_REAL_COLL.equalsIgnoreCase(taskType)) {
				CollLink link = new CollLink(devId);
				link.taskType = ParamsConstant.TASK_TYPE_REAL_COLL;
				link.autoTransfer();
				logger.debug("real collect Ok, devId: " + devId);
			} 
			//异常采集类型
			else {
				logger.error("error collect task type!");
			}
		} catch (Exception e) {
			logger.error("transfer file fail, devId: " + devId +", taskType: " + taskType , e );
		}
	}
}
