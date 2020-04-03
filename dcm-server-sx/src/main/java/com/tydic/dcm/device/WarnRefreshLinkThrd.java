package com.tydic.dcm.device;


import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.dto.WarnLinkDto;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.TimeTool;
import com.tydic.dcm.warn.WarnManager;

/**
 * 链路刷新告警
 * @author Yuanh
 *
 */
public class WarnRefreshLinkThrd extends Thread {

	//日志对象
	private static Logger logger = Logger.getLogger(WarnRefreshLinkThrd.class);
	
	//定时器刷新间隔
	private Long refreshInterval = 0L;
	
	//退出标志
	public Boolean exitFlag = false;
	
	//是否自动采集
	private Boolean isCollActive = true;
	
	//是否自动分发
	//private Boolean isDistActive = true;
	
	//采集链路告警实时列表数据
	protected Hashtable<String, WarnLinkDto> collWarnHashTab = new Hashtable<String, WarnLinkDto>();
	
	//分发链路告警实时列表数据
	//protected Hashtable<String, WarnLinkDto> distWarnHashTab = new Hashtable<String, WarnLinkDto>();

	/**
	 * 采集链路定时刷新任务
	 * 
	 * @param freshTimes  定时刷新时间间隔
	 * @param groupId     当前采集程序GroupId组
	 */
	public WarnRefreshLinkThrd(Long refreshInterval, Boolean isCollActive, Boolean isDistActive) {
		setName("warnTaks");
		
		//采集链路刷新时间间隔,默认5秒刷新一次
		this.refreshInterval = refreshInterval;
		
		//是否已经启动自动采集
		this.isCollActive = isCollActive;
		
		//是否已经启动自动分发
		//this.isDistActive = isDistActive;
	}
	
	@Override
	public void run() {
		//程序已启动初始化执行一次自动采集
		while(!exitFlag) {
			try {
				if (isCollActive) {
					refreshCollWarn();
				}
				
				//贵州采集不需要分发告警
				//if (isDistActive) {
				//	refreshDistWarn();
				//}
				
				sleep(refreshInterval);
			} catch (Exception e) {
				logger.error("warn refresh fail.", e);
			}
		}
	}
	
	/**
	 * 刷新采集告警
	 */
	private void refreshCollWarn() {
		Hashtable<String, WarnLinkDto> collWarnHashTab = DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab();
		logger.debug("begin collect warn refresh, refresh link list size: " + ArrayUtil.getSize(collWarnHashTab));
		
		int isChangeCount = 0;
		if (collWarnHashTab != null && collWarnHashTab.size() > 0) {
			Iterator<String> iter = collWarnHashTab.keySet().iterator();
			while (iter.hasNext()) {
				//链路ID
				String devId = iter.next();
				//链路对应的告警信息
				WarnLinkDto warnLinkDto = collWarnHashTab.get(devId);
				
				//判断当前时间是否超过制定刷新时间
				Calendar lastCalendar = Calendar.getInstance();
				lastCalendar.setTime(new Date(warnLinkDto.getLastTime()));
				lastCalendar.add(Calendar.MINUTE, warnLinkDto.getWarnInterval());
				Date linkLastDate = lastCalendar.getTime();
				logger.debug("collect warn refresh, linkLastDate: " + DateUtil.parseDate2Str(linkLastDate, DateUtil.allPattern) + ", devId: " + devId);
				
				//超过规定时间没有告警
				if(linkLastDate.before(new Date())) {
					//链路最新时间+告警间隔时间 > 当前时间 
					WarnManager.tranWarn(warnLinkDto.getAddrId(), WarnManager.SWITCH_NO_FILE, devId, "", "no files for a long time");
					
					//更新当前链路的最新刷新时间
					warnLinkDto.setLastTime(TimeTool.getTime());
					collWarnHashTab.put(devId, warnLinkDto);
					isChangeCount++;
				}
			}
		}
		logger.debug("end collect warn refresh, add warn count: " + isChangeCount);
	}
	
	/**
	 * 刷新分发告警
	 * 
	 */
	/*private void refreshDistWarn() {
		Hashtable<String, WarnLinkDto> distWarnHashTab = DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab();
		logger.debug("begin distribute warn refresh, refresh link list size: " + ArrayUtil.getSize(distWarnHashTab));
		
		int isChangeCount = 0;
		if (distWarnHashTab != null && distWarnHashTab.size() > 0) {
			Iterator<String> iter = distWarnHashTab.keySet().iterator();
			while (iter.hasNext()) {
				//链路ID
				String devId = iter.next();
				//链路对应的告警信息
				WarnLinkDto warnLinkDto = distWarnHashTab.get(devId);
				
				//判断当前时间是否超过制定刷新时间
				Calendar lastCalendar = Calendar.getInstance();
				lastCalendar.setTime(new Date(warnLinkDto.getLastTime()));
				lastCalendar.add(Calendar.MINUTE, warnLinkDto.getWarnInterval());
				Date linkLastDate = lastCalendar.getTime();
				
				//告警开始时间 2015-06-25 12:00:00
				//String warnStartDate = warnLinkDto.getWarnStartDate();
				//告警结束时间
				//String warnEndDate = warnLinkDto.getWarnEndDate();
				
				//告警时间段不为空，则在告警时间段内不需要添加告警日志
				if (!BlankUtil.isBlank(warnStartDate) && !BlankUtil.isBlank(warnEndDate)) {
					Date startDate =  DateUtil.parse(warnStartDate, DateUtil.allPattern);
					Date endDate =  DateUtil.parse(warnEndDate, DateUtil.allPattern);
					if (startDate.before(new Date()) && endDate.after(new Date())) {
						logger.debug("not add warn log, startDate: " + warnStartDate + ", endDate: " + warnEndDate 
								+ ", currentDate: " + DateUtil.getCurrent(DateUtil.allPattern) + ", devId: " + devId);
						continue;
					}
				} else if (!BlankUtil.isBlank(warnStartDate) && BlankUtil.isBlank(warnEndDate)) {
					Date startDate =  DateUtil.parse(warnStartDate, DateUtil.allPattern);
					if (startDate.before(new Date())) {
						logger.debug("not add warn log, startDate: " + warnStartDate 
								+ ", currentDate: " + DateUtil.getCurrent(DateUtil.allPattern) + ", devId: " + devId);
						continue;
					}
				} else if (BlankUtil.isBlank(warnStartDate) && !BlankUtil.isBlank(warnEndDate)) {
					Date endDate =  DateUtil.parse(warnEndDate, DateUtil.allPattern);
					if (endDate.after(new Date())) {
						logger.debug("not add warn log, endDate: " + warnEndDate 
								+ ", currentDate: " + DateUtil.getCurrent(DateUtil.allPattern) + ", devId: " + devId);
						continue;
					}
				}
		
				
				//链路最新时间+告警间隔时间 >当前时间
				if(linkLastDate.before(DateUtil.getCurrentDate())) {
					WarnManager.tranWarn(warnLinkDto.getAddrId(), WarnManager.SWITCH_NO_FILE, devId, "", "no files for a long time");
					
					//更新当前链路的最新刷新时间
					warnLinkDto.setLastTime(TimeTool.getTime());
					distWarnHashTab.put(devId, warnLinkDto);
					isChangeCount++;
				}
			}
		}
		logger.debug("end distribute warn refresh, add warn count: " + isChangeCount);
	}*/
	
	/**
	 * 退出
	 */
	public void exit() {
		logger.debug("begin warn refresh task exit.");
		Hashtable<String, WarnLinkDto> collWarnHashTab = DcmSystem.warnRefreshLinkThrd.getCollWarnHashTab();
		if (collWarnHashTab != null && collWarnHashTab.size() > 0) {
			collWarnHashTab = new Hashtable<String, WarnLinkDto>();
		}
		/*Hashtable<String, WarnLinkDto> distWarnHashTab = DcmSystem.warnRefreshLinkThrd.getDistWarnHashTab();
		if (distWarnHashTab != null && distWarnHashTab.size() > 0) {
			distWarnHashTab = new Hashtable<String, WarnLinkDto>();
		}*/
		exitFlag = true;
		logger.debug("end warn refresh task exit.");
	}
	
	public Hashtable<String, WarnLinkDto> getCollWarnHashTab() {
		return DcmSystem.warnRefreshLinkThrd.collWarnHashTab == null ? new Hashtable<String, WarnLinkDto>() : DcmSystem.warnRefreshLinkThrd.collWarnHashTab;
	}

	public void setCollWarnHashTab(Hashtable<String, WarnLinkDto> collWarnHashTab) {
		DcmSystem.warnRefreshLinkThrd.collWarnHashTab = collWarnHashTab;
	}
	
	/*public Hashtable<String, WarnLinkDto> getDistWarnHashTab() {
		return DcmSystem.warnRefreshLinkThrd.distWarnHashTab == null ? new Hashtable<String, WarnLinkDto>() : DcmSystem.warnRefreshLinkThrd.distWarnHashTab;
	}

	public void setDistWarnHashTab(Hashtable<String, WarnLinkDto> distWarnHashTab) {
		DcmSystem.warnRefreshLinkThrd.distWarnHashTab = distWarnHashTab;
	}*/
}
