package com.tydic.dcm.device;

import java.util.List;

import org.apache.log4j.Logger;

import com.tydic.bp.ScheduleJob;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.ArrayUtil;

/**
 * 任务调度状态监控
 * 
 * @author Yuanh
 *
 */
public class QuartzStatusMonitorThrd extends Thread {
	/**
	 * 任务调度状态监控
	 */
	private static Logger logger = Logger.getLogger(QuartzStatusMonitorThrd.class);

	/**
	 * 任务调度休眠时间
	 */
	private int slpTime;
	
	
	/**
	 * 任务监控构造方法
	 * 
	 * @param slpTime
	 */
	public QuartzStatusMonitorThrd(int slpTime) {
		this.slpTime = slpTime;
	}
	
	@Override
	public void run() {
		logger.debug("begin monitor quartz status");
		while(true) {
			try {
				List<ScheduleJob> list = SpringUtil.getQuartzManager().queryAllJobInfo();
				List<ScheduleJob> executeList = SpringUtil.getQuartzManager().queryExcutingJobs();
				logger.debug("All Job size: " + ArrayUtil.getSize(list));
				logger.debug("Executing Job size: " + ArrayUtil.getSize(executeList));
				int normalCount = 0;
				int blockedCount = 0;
				if (list != null && list.size() > 0) {
					for (int i=0; i<list.size(); i++) {
						if("NORMAL".equalsIgnoreCase(list.get(i).getJobStatus())) {
							normalCount++;
						} else if ("BLOCKED".equalsIgnoreCase(list.get(i).getJobStatus())) {
							blockedCount++;
						}
						
						logger.debug(i + "  Job Name ---> " + list.get(i).getJobName() 
								+ ", Job Status ---> " + list.get(i).getJobStatus() 
								+ "\t Job Trigger ---> " + list.get(i).getTriggerKey());
					}
					logger.debug("blockedCount: " + blockedCount + "\t\tnormalCount: " + normalCount);
					logger.debug("\n\n\n");
				}
				sleep(slpTime * 1000);
			} catch (Exception e) {
				logger.error("get quartz status fail", e);
			}
		}
	}
}
