package com.tydic.dcm.device;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tydic.bp.QuartzConstant;
import com.tydic.bp.QuartzManager;
import com.tydic.bp.QuartzManagerImpl;
import com.tydic.bp.ScheduleJob;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.dto.WarnLinkDto;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.PropertiesUtil;
import com.tydic.dcm.util.tools.StringTool;
import com.tydic.dcm.util.tools.TimeTool;

/**
 * 采集链路
 * @author Yuanh
 *
 */
public class CollRefreshLinkThrd extends Thread {

	//日志对象
	private static Logger logger = Logger.getLogger(CollRefreshLinkThrd.class);
	
	//定时器刷新间隔
	private Long refreshInterval = 0L;
	
	//当前Dcm程序GroupId
	private String groupId;
	
	/**
	 * 采集链路定时刷新任务
	 * 
	 * @param refreshInterval  定时刷新时间间隔
	 * @param groupId     当前采集程序GroupId组
	 */
	public CollRefreshLinkThrd(Long refreshInterval, String groupId) {
		//采集链路刷新时间间隔,默认5秒刷新一次
		this.refreshInterval = refreshInterval;
		
		//当前采集程序Group组
		this.groupId = groupId;
		
	}
	
	@Override
	public void run() {
		//程序已启动初始化执行一次自动采集
		while(true) {
			try {
				refreshColl();
				
				sleep(refreshInterval);
			} catch (Exception e) {
				logger.error("collect refresh fail.", e);
			}
		}
	}
	
	/**
	 * 初始化获取所有需要自动采集的采集链路信息
	 */
	private void refreshColl() {
		logger.debug("begin refresh collect link.");
		
		//查询需要采集的链路信息
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		List<Map<String, Object>> rstList = JdbcUtil.queryForList("collectMapper.queryRefreshCollLinkList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		logger.info("get collect link list, list size: " + ArrayUtil.getSize(rstList));
		
		//添加刷新告警日志
		addCollWarnLog(rstList);
		logger.info("update collect link warn time ok!");
		
		//任务调度失败默认初始化时间（单位：秒）
		String quartFailDefaulTime = PropertiesUtil.getValueByKey(ParamsConstant.QUARTZ_FAIL_DEFAULT_TIME, ParamsConstant.PARAMS_600);
		if (!BlankUtil.isBlank(rstList)) {
			for (int i=0; i<rstList.size(); i++) {
				Map<String, Object> collLinkMap = rstList.get(i);
				
				//链路ID
				String devId = StringTool.object2String(collLinkMap.get("DEV_ID"));
				
				//定时调度触发器模式
				String triggerType = StringTool.object2String(collLinkMap.get("DATE_MODE"));
				//定时调度任务表达式
				String exp = StringTool.object2String(collLinkMap.get("TIME_MODE"));
				
				//触发器容错校验,默认触发器类型为简单触发器，触发方式为600秒执行一次
				//设置默认值的情况有下面几种
				//1、触发器类型为空
				//2、触发器类型不为Cron或者简单触发器类型
				//3、触发器类型为简单触发器，但是对应的值为空或者为非数字
				//4、触发器类型为Cron触发器，但是对应值不能通过Cron校验
                if (BlankUtil.isBlank(triggerType) 
                        || (!QuartzConstant.CRON_TRIGGER_TYPE.equals(triggerType) && !QuartzConstant.SIMPLE_TRIGGER_TYPE.equals(triggerType))
                        || (QuartzConstant.SIMPLE_TRIGGER_TYPE.equals(triggerType) && (BlankUtil.isBlank(exp) || !exp.matches("\\d+"))
                        || (QuartzConstant.CRON_TRIGGER_TYPE.equals(triggerType) && (BlankUtil.isBlank(exp) || !SpringUtil.getQuartzManager().validateCron(exp))))) {
                	logger.debug("collect link trigger config error later set default config, default config:{triggerType:2, exp:60}" +
                			", triggerType: " + triggerType + ", devId: " + devId);
                    triggerType = QuartzConstant.SIMPLE_TRIGGER_TYPE;
                    exp = quartFailDefaulTime;
                }
				
				//JobName
				collLinkMap.put(QuartzConstant.JOB_NAME, StringTool.object2String(collLinkMap.get("DEV_NAME")));
				//链路执行Class
				collLinkMap.put(QuartzConstant.JOB_CLASS, ParamsConstant.PARAMS_JOB_COLL_CLZ);
				//链路执行Cron表达式
				collLinkMap.put(QuartzConstant.TIME_KEY, exp);
				
				//链路执行开始时间
				collLinkMap.put(QuartzConstant.START_TIME, StringTool.object2String(collLinkMap.get("STATE_BEGIN_TIME")));
				//链路执行结束时间
				collLinkMap.put(QuartzConstant.END_TIME, StringTool.object2String(collLinkMap.get("STATE_END_TIME")));
				//链路执行表达式类型
				collLinkMap.put(QuartzConstant.TRIGGER_TYPE, triggerType);
				//触发器优先级
				collLinkMap.put(QuartzConstant.TRIGGER_PRIORITY, StringTool.object2String(collLinkMap.get("PRIORITY")));
				
				//链路参数
				Map<String, Object> busParams = new HashMap<String, Object>();
				busParams.put("devId", collLinkMap.get("DEV_ID"));
				busParams.put("task_type", ParamsConstant.TASK_TYPE_AUTO_COLL);
				//Job业务参数
				collLinkMap.put(QuartzConstant.BUS_PARAMS, busParams);
			}
		}
		//添加到定时Job中,如果符合要求的链路为null也应该传入到Quartz中，在Quartz中将所有的Job的移除 
		if (BlankUtil.isBlank(rstList)) {
			rstList = new ArrayList<Map<String, Object>>();
		}
		SpringUtil.getQuartzManager().addJobList(rstList);
		logger.debug("end refresh collect link.");
	}
	
	/**
	 * 更新采集链路最新分发时间
	 * 
	 * @param rstList
	 */
	private void addCollWarnLog(List<Map<String, Object>> rstList) {
		logger.debug("begin refresh collect warn log list, collWarnHashTab size: " + ArrayUtil.getSize(DcmSystem.warnRefreshLinkThrd.collWarnHashTab));
		
		if (BlankUtil.isBlank(rstList) || rstList.isEmpty()) {
			DcmSystem.warnRefreshLinkThrd.collWarnHashTab = new Hashtable<String, WarnLinkDto>();
			return;
		} 
		
		Boolean isExist = false;
		int len = rstList.size();
		for (int i=0; i<len; i++) {
			//链路ID
			String linkDevId = StringTool.object2String(rstList.get(i).get("DEV_ID"));
			//链路对应的地址
			String addrId = StringTool.object2String(rstList.get(i).get("ADDR_ID"));
			
			//获取告警间隔、告警开始时间、告警结束时间
			String warnInterval = ParamsConstant.PARAMS_60;
			
			String paramsValue = StringTool.object2String(rstList.get(i).get("PARAMS_VALUE"));
			logger.debug("warn param : " + paramsValue);
			if (!BlankUtil.isBlank(paramsValue)) {
				String [] paramValue = paramsValue.split(",");
				if (!BlankUtil.isBlank(paramValue)) {
					for (int j=0; j<paramValue.length; j++) {
						String [] paramObj = paramValue[j].split("#");
						if (paramObj != null && paramObj.length > 1) {
							//告警间隔时间
							if (ParamsConstant.PARAMS_WARN_INTERVAL.equals(paramObj[0])) {
								warnInterval = paramObj[1];
							}
						} 
					}
				}
			}
			
			synchronized (DcmSystem.warnRefreshLinkThrd.collWarnHashTab) {
				//判断当前链路ID在table中是否存在，如果不存在保存到table中，并且将当前时间作为value
				if(!DcmSystem.warnRefreshLinkThrd.collWarnHashTab.keySet().contains(linkDevId)) {
					WarnLinkDto warnLinkDto = new WarnLinkDto(TimeTool.getTime(), addrId, Integer.parseInt(warnInterval));
					DcmSystem.warnRefreshLinkThrd.collWarnHashTab.put(linkDevId, warnLinkDto);
				}
				
				//判断当前链路是否在Table中存在，如果不存在则将Table中的项删掉
				Iterator<String> iter = DcmSystem.warnRefreshLinkThrd.collWarnHashTab.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					if (key.equals(linkDevId)) {
						isExist = true;
						
						//跟新链路对应的链路信息(防止用户没有重启采集程序，修改了告警刷新间隔时间)
						WarnLinkDto oldWarnLinkDto = DcmSystem.warnRefreshLinkThrd.collWarnHashTab.get(key);
						WarnLinkDto newLinkDto = new WarnLinkDto(oldWarnLinkDto.getLastTime(), addrId, Integer.parseInt(warnInterval));
						DcmSystem.warnRefreshLinkThrd.collWarnHashTab.put(linkDevId, newLinkDto);
						break;
					}
				}
				
				if (!isExist) {
					iter.remove();
				}
			}
		}
		logger.debug("end refresh collect warn log list, collWarnHashTab size: " + ArrayUtil.getSize(DcmSystem.warnRefreshLinkThrd.collWarnHashTab));
	}

	/**
	 * 定时刷新采集退出
	 */
	public void exit() {
		logger.debug("begin collect refresh task exit.");
		QuartzManager quartzMgr = new QuartzManagerImpl();
		List<ScheduleJob> allJobs =  quartzMgr.queryAllJobInfo();
		Iterator<ScheduleJob> iter = allJobs.iterator();
		while(iter.hasNext()) {
			String jobName = iter.next().getJobName();
			String jobGroupName = iter.next().getJobGroup();
			quartzMgr.removeJob(jobName, jobGroupName);
		}
		logger.debug("end collect refresh task exit.");
	}
}
