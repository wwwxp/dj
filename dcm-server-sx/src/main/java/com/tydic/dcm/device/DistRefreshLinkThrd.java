package com.tydic.dcm.device;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.dto.DistLinkDto;
import com.tydic.dcm.ftran.DistLink;
import com.tydic.dcm.task.DistTask;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 分发链路定时刷新类
 * @author Yuanh
 *
 */
public class DistRefreshLinkThrd extends Thread {

	// 日志对象
	private static Logger logger = Logger.getLogger(DistRefreshLinkThrd.class);

	// 定时器刷新间隔
	private Long refreshInterval = 0L;

	// 当前Dcm程序GroupId
	private String groupId;
	
	//本地网对应的Ftp信息
	public List<Map<String, Object>> latnList = null;
	
	//当前批次所有的分发链路
	public static List<Map<String, Object>> rstList = null;

	/**
	 * 本地网
	 */
	private static final String FMT_DIST = "999";
	
	//线程池对象
	public ThreadPoolExecutor executorService;
	
	/**
	 * 分发定时任务
	 * 
	 * @param refreshInterval  刷新间隔时间
	 * @param groupId          当前分发程序GroupId
	 */
	public DistRefreshLinkThrd(Long refreshInterval, String groupId, int threadSize) {
		//定时器刷新时间,默认5秒刷新一次
		this.refreshInterval = refreshInterval;
		
		//当前采集程序Group组
		this.groupId = groupId;
		
		//线程池对象
		executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadSize);
	}
	
	/**
	 * 获取本地网信息
	 */
	public void loadLatnList() {
		logger.debug("begin load latn list.");
		latnList = JdbcUtil.queryForList("distributeMapper.queryLatnOfFtpList", null, FrameConfigKey.DEFAULT_DATASOURCE);
		logger.debug("end init load latn list, list size:" + ArrayUtil.getSize(latnList));
	}

	/**
	 * 定时刷新分发任务
	 * 
	 */
	@Override
	public void run() {
		
		//程序已启动初始化执行一次自动采集
		while(true) {
			long start = System.currentTimeMillis();//启动时间
			
			try {
				refreshDist();
				
				long end = System.currentTimeMillis();//完成时间
				long interval = end - start;
				if(interval >= refreshInterval){//在间隔时间内没有完成要马上进行扫描
					continue;
				} else {//在间隔时间内完成开始休眠
					sleep(refreshInterval - interval);
				}
			} catch (Exception e) {
				logger.error("distribute refresh fail", e);
			};
		}
	}
	
	/**
	 * 初始化获取所有需要自动采集的采集链路信息
	 */
	private void refreshDist() {
		logger.debug("begin refresh distribute link.");
		
		//查询需要分发的链路信息
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		rstList = JdbcUtil.queryForList("distributeMapper.queryRefreshDistLinkList",  queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		logger.info("get distribute link list, list size: " + ArrayUtil.getSize(rstList));

		//添加刷新告警日志，贵州采集不需要分发告警
		//addDistWarnLog(rstList);
		//logger.info("update distribute link warn time ok");
		
		//本地网为999需要做拆分处理
		List<DistLink> links = splitLinks(rstList);
		
		//将分发链路信息添加到分发Job中
		if (!BlankUtil.isBlank(links)) {
			//线程执行计数器
			CountDownLatch latch=new CountDownLatch(links.size());
			
			//遍历所有的分发链路任务
			for (DistLink link : links) {
				//创建任务线程
				DistTask distTask = new DistTask(link,ParamsConstant.TYPE_DIST_LINK,ParamsConstant.TASK_TYPE_AUTO_DIST, latch);
				distTask.setPriority(Thread.MAX_PRIORITY);
				//将任务线程添加到线程池
				executorService.execute(distTask);
				System.out.println("distribution link ID: " + link.id + ", current datetime:" + DateUtil.getCurrent(DateUtil.allPattern));
			}
			try {
				//等待线程计数器为0，才能再次接受新的任务
				//latch.await(1, TimeUnit.HOURS);
				latch.await();
			} catch (InterruptedException e) {
				logger.error("count down latch fail.", e);
			}
		}
		logger.debug("end refresh distribute link.");
	}
	
	/**
	 * 本地网链路拆分
	 * @Title: splitLinks
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: void
	 * @author: tianjc
	 * @date: 2017年5月8日 上午10:38:55
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	private List<DistLink> splitLinks(List<Map<String, Object>> rstList){
		List<DistLink> results = new ArrayList<DistLink>();
		
		if(CollectionUtils.isNotEmpty(rstList)){
			Iterator<Map<String,Object>> iterator = rstList.iterator();
			while(iterator.hasNext()){
				Map<String,Object> distLinkMap = iterator.next();
				String devId = StringTool.object2String(distLinkMap.get("DEV_ID"));
				DistLink reallink = new DistLink(devId);
				
				String localLatn = reallink.getLatnId();
				if (FMT_DIST.equals(localLatn)) {
					logger.debug("begin splite dist link with latn_id");
					
					//当前所有本地网对应的Ftp/Sftp主机信息
					List<Map<String, Object>> latnList = DcmSystem.distRefreshLinkThrd.latnList;
					logger.debug("auto distribute, latnList size: " + ArrayUtil.getSize(latnList) + ", devId: " + devId);
					if (!BlankUtil.isBlank(latnList)) {
						for (int i=0; i<latnList.size(); i++) {
							Map<String, Object> latnMap = latnList.get(i);
							String locId = StringTool.object2String(latnMap.get("LOC_ID"));
							if (FMT_DIST.equals(locId)) {
								continue;
							}
							
							//复制link
							DistLink virtualLink = ObjectUtils.clone(reallink);
							
							//设置本地网
							virtualLink.setLatn_Id_Sub(locId);
							//切换到本地网对应Ftp/sftp主机信息
							String newIp = StringTool.object2String(latnMap.get("IP"));
							String newPort = StringTool.object2String(latnMap.get("PORT"));
							String newUserName = StringTool.object2String(latnMap.get("USERNAME"));
							String newPassword = StringTool.object2String(latnMap.get("PASSWORD"));
							String type = StringTool.object2String(latnMap.get("TYPE"));
							
							DistLinkDto distLinkDto = virtualLink.getDistLinkDto();
							distLinkDto.getLinkParams().put("ip", newIp);
							distLinkDto.getLinkParams().put("port", newPort);
							distLinkDto.getLinkParams().put("username", newUserName);
							distLinkDto.getLinkParams().put("password", newPassword);
							distLinkDto.getLinkParams().put("trans_protocol", type);
							logger.debug("split latn=999 link,dev_id:"+devId+",latn_Id_Sub: " + locId+",ip: " + newIp + ",port: " + newPort + ",username: " + newUserName + ",trans_protocol: " + type);
							
							results.add(virtualLink);
						}
					}
				} else{
					results.add(reallink);
				}
			}
		}
		
		return results;
	}
	
	/**
	 * 更新分发链路最新分发时间
	 * 
	 * @param rstList
	 */
	/*private void addDistWarnLog(List<Map<String, Object>> rstList) {
		logger.debug("begin refresh distribute warn log list, distWarnHashTab size: " + ArrayUtil.getSize(DcmSystem.warnRefreshLinkThrd.distWarnHashTab));
		
		if (BlankUtil.isBlank(rstList) || rstList.isEmpty()) {
			DcmSystem.warnRefreshLinkThrd.distWarnHashTab = new Hashtable<String, WarnLinkDto>();
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
			String warnStartTime = "";
			String warnEndTime = ""; 
			String paramsValue = StringTool.object2String(rstList.get(i).get("PARAMS_VALUE"));
			if (!BlankUtil.isBlank(paramsValue)) {
				String [] paramValue = paramsValue.split(",");
				if (!BlankUtil.isBlank(paramValue)) {
					for (int j=0; j<paramValue.length; j++) {
						String [] paramObj = paramValue[j].split("#");
						if (paramObj != null && paramObj.length > 1) {
							if (ParamsConstant.PARAMS_WARN_START_TIME.equals(paramObj[0])) {
								warnStartTime = paramObj[1];
							} else if (ParamsConstant.PARAMS_WARN_END_TIME.equals(paramObj[0])) {
								warnEndTime = paramObj[1];
							} else if (ParamsConstant.PARAMS_WARN_INTERVAL.equals(paramObj[0])) {
								warnInterval = paramObj[1];
							}
						}
					}
				}
			}
			
			//判断当前链路ID在table中是否存在，如果不存在保存到table中，并且将当前时间作为value
			if(!DcmSystem.warnRefreshLinkThrd.distWarnHashTab.keySet().contains(linkDevId)) {
				WarnLinkDto warnLinkDto = new WarnLinkDto(TimeTool.getTime(), addrId, Integer.parseInt(warnInterval), warnStartTime, warnEndTime);
				DcmSystem.warnRefreshLinkThrd.distWarnHashTab.put(linkDevId, warnLinkDto);
			}
			
			//判断当前链路是否在Table中存在，如果不存在则将Table中的项删掉
			Iterator<String> iter = DcmSystem.warnRefreshLinkThrd.distWarnHashTab.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				if (key.equals(linkDevId)) {
					isExist = true;
					
					//跟新链路对应的链路信息(防止用户没有重启采集程序，修改了告警刷新间隔时间)
					WarnLinkDto oldWarnLinkDto = DcmSystem.warnRefreshLinkThrd.distWarnHashTab.get(key);
					WarnLinkDto newLinkDto = new WarnLinkDto(oldWarnLinkDto.getLastTime(), addrId, Integer.parseInt(warnInterval), warnStartTime, warnEndTime);
					DcmSystem.warnRefreshLinkThrd.distWarnHashTab.put(linkDevId, newLinkDto);
					break;
				}
			}
			
			if (!isExist) {
				iter.remove();
			}
		}
		logger.debug("end refresh distribute warn log list, distWarnHashTab size: " + ArrayUtil.getSize(DcmSystem.warnRefreshLinkThrd.distWarnHashTab));
	}*/
}
