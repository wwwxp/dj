package com.tydic.dcm.device;


import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.dto.CollLinkDto;
import com.tydic.dcm.dto.DistLinkDto;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.ConvertMap2Dto;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 采集链路
 * @author Yuanh
 *
 */
public class LinkDataRefreshThrd extends Thread {

	//日志对象
	private static Logger logger = Logger.getLogger(LinkDataRefreshThrd.class);
	
	//采集链路缓存
	private static Hashtable<String, CollLinkDto> collTable = new Hashtable<String, CollLinkDto>();
	
	//采集链路参数缓存
	private static List<Map<String, Object>> collParameterList = new ArrayList<Map<String, Object>>();
	
	//分发链路缓存
	private static Hashtable<String, DistLinkDto> distTable = new Hashtable<String, DistLinkDto>();
	
	//分发链路参数缓存
	private static List<Map<String, Object>> distParameterList = new ArrayList<Map<String, Object>>();
	
	//定时器刷新间隔
	private Long refreshInterval = 0L;
	
	//当前Dcm程序GroupId
	private String groupId;
	
	//自动采集是否有效
	private Boolean isCollActive;
	
	//自动分发是否有效
	private Boolean isDistActive;

	/**
	 * 采集链路定时刷新任务
	 *
	 * @param refreshInterval
	 * @param groupId
	 * @param isCollActive
	 * @param isDistActive
	 */
	public LinkDataRefreshThrd(Long refreshInterval, String groupId, Boolean isCollActive, Boolean isDistActive) {
		//采集链路刷新时间间隔,默认5秒刷新一次
		this.refreshInterval = refreshInterval;
		
		//当前采集程序Group组
		this.groupId = groupId;
		
		//自动采集是否开启
		this.isCollActive = isCollActive;
		
		//自动分发是否开启
		this.isDistActive = isDistActive;
		
	}
	
	/**
	 * 初始化启动一次,如果现场资源抢占过来可能运行的时候数据还未来得及加载
	 */
	public void init() {
		if (this.isCollActive) {
			refreshColl();
		}
		
		if (this.isDistActive) {
			refreshDist();
		}
	}
	
	@Override
	public void run() {
		//程序已启动初始化执行一次自动采集
		while(true) {
			try {
				if (this.isCollActive) {
					refreshColl();
				}
				
				if (this.isDistActive) {
					refreshDist();
				}
				
				sleep(refreshInterval);
			} catch (Exception e) {
				logger.error("auto refresh link attribute and parameters fail.", e);
			}
		}
	}
	
	/**
	 * 间隔刷新所有的采集属性和参数
	 */
	private void refreshColl() {
		logger.debug("begin refresh collect link attribute and parameters");
		long startTime = System.currentTimeMillis();
		//查询采集链路属性
		queryCollAttributes();
		//查询采集链路参数
		queryCollParameters();
		long endTimes = System.currentTimeMillis();
		logger.debug("end refresh collect link attribute and parameters, used time:[ " + (endTimes - startTime) + " ]ms");
	}
	
	/**
	 * 查询链路属性
	 */
	private void queryCollAttributes() {
		logger.debug("begin query collect link attributes");
		//查询需要采集的链路信息
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		List<Map<String, Object>> rstList = JdbcUtil.queryForList("collectMapper.queryAllCollAttrsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		StringBuffer linkBuffer = new StringBuffer();
		if (!BlankUtil.isBlank(rstList)) {
			for (int i=0; i<rstList.size(); i++) {
				Map<String, Object> linkMap = rstList.get(i);
				CollLinkDto collLinkDto = ConvertMap2Dto.convert2CollDto(linkMap);
				collTable.put(StringTool.object2String(collLinkDto.getDevId()), collLinkDto);
				linkBuffer.append(collLinkDto.getDevId()).append(",");
			}
		}
		logger.debug("end query collect link attributes, total size: " + ArrayUtil.getSize(rstList)  + ", devIds: " + linkBuffer.toString());
	}
	
	/**
	 * 查询链路参数
	 */
	private void queryCollParameters() {
		logger.debug("begin query collect link parameters");
		//获取所有的采集链路参数
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		collParameterList = JdbcUtil.queryForList("collectMapper.queryAllCollParamsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		if (BlankUtil.isBlank(collParameterList)) {
			collParameterList = new ArrayList<Map<String, Object>>();
		}
		logger.debug("end query collect link parameters, total size: " + ArrayUtil.getSize(collParameterList));
	}
	
	/**
	 * 间隔刷新所有分发链路参数和属性
	 * 
	 */
	private void refreshDist() {
		logger.debug("begin refresh distribute link attribute and parameters");
		long startTime = System.currentTimeMillis();
		//查询分发链路属性
		queryDistAttributes();
		//查询分发链路参数
		queryDistParameters();
		long endTimes = System.currentTimeMillis();
		logger.debug("end refresh distribute link attribute and parameters, used time:[ " + (endTimes - startTime) + " ]ms");
	}
	
	/**
	 * 查询分发链路属性信息
	 */
	private void  queryDistAttributes() {
		logger.debug("begin query distribute link attributes");
		//查询分发链路属性
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		List<Map<String, Object>> rstList = JdbcUtil.queryForList("distributeMapper.queryDistAllAttrsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		StringBuffer linkBuffer = new StringBuffer();
		if (!BlankUtil.isBlank(rstList)) {
			for (int i=0; i<rstList.size(); i++) {
				Map<String, Object> linkMap = rstList.get(i);
				DistLinkDto distLinkDto = ConvertMap2Dto.convert2DistDto(linkMap);
				distTable.put(StringTool.object2String(distLinkDto.getDevId()), distLinkDto);
				linkBuffer.append(distLinkDto.getDevId()).append(",");
			}
		}
		logger.debug("end query distribute link attributes, total size: " + ArrayUtil.getSize(rstList) + ", devIds: " + linkBuffer.toString());
	}
	
	/**
	 * 查询分发链路参数
	 */
	private void queryDistParameters() {
		logger.debug("begin query distribute link parameters");
		//查询分发链路参数
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("groupId", groupId);
		distParameterList = JdbcUtil.queryForList("distributeMapper.queryDistAllParamsList", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
		if (BlankUtil.isBlank(distParameterList)) {
			distParameterList = new ArrayList<Map<String, Object>>();
		}
		logger.debug("end query distribute link parameters, total size : " + ArrayUtil.getSize(distParameterList));
	}

	/**
	 * 获取采集链路参数，属性
	 * @param devId
	 * @return
	 */
	public static CollLinkDto getCollLinkAllInfo(String devId) {
		logger.debug("begin get collect link info, devId: " + devId);
		CollLinkDto collLinkDto = collTable.get(devId);
		if (!BlankUtil.isBlank(collLinkDto)) {
			collLinkDto.setLinkParams(getCollParametersById(collLinkDto, devId));
		}
		logger.debug("end get collect link info, devId: " + devId);
		return collLinkDto;
	}
	
	/**
	 * 获取纷繁链路参数、属性
	 * @param devId
	 * @return
	 */
	public static DistLinkDto getDistLinkAllInfo(String devId) {
		logger.debug("begin get distribute link info, devId: " + devId);
		DistLinkDto distLinkDto = distTable.get(devId);
		if (!BlankUtil.isBlank(distLinkDto)) {
			distLinkDto.setLinkParams(getDistParametersById(distLinkDto, devId));
			//设置latn字段
//			distLinkDto.setLatnId(ObjectUtils.toString(distLinkDto.getLinkParams().get("JK_latn_id"), "-99"));
		}
		logger.debug("end get distribute link info, devId: " + devId);
		return distLinkDto;
	}
	
	/**
	 * 根据链路ID获取链路参数
	 * @param devId
	 * @return
	 */
	private static Hashtable<String, Object> getCollParametersById(CollLinkDto collLinkDto, String devId) {
		logger.debug("begin get collect link parameters, devId: " + devId);
		
		//用来记录出现问题的参数名称
		StringBuffer buffer = new StringBuffer();
		
		//链路参数
		Hashtable<String, Object> linkParametersTable = new Hashtable<String, Object>();
		for (int i=0; i<collParameterList.size(); i++) {
			Map<String, Object> linkParametersMap = collParameterList.get(i);
			String currentDevId = StringTool.object2String(linkParametersMap.get("DEV_ID"));
			if (currentDevId.equals(devId)) {
				String paramName = StringTool.object2String(linkParametersMap.get("PARAM_NAME"));
				String paramValue = StringTool.object2String(linkParametersMap.get("PARAM_VALUE"));
				//链路参数是否必填
				String paramIsRequired = StringTool.object2String(linkParametersMap.get("IS_REQUIRED"));
				if (ParamsConstant.PARAMS_1.equals(paramIsRequired) && BlankUtil.isBlank(paramValue)) {
					buffer.append(paramName);
					buffer.append(",");
				}
				linkParametersTable.put(paramName, paramValue);
			}
		}
		//有必填参数为空或者改链路没有配置链路参数
		if (BlankUtil.isBlank(linkParametersTable) || !BlankUtil.isBlank(buffer.toString())) {
			//添加告警信息
			String tipsMsg = "link parameter is null or missing required, " 
					+ (!BlankUtil.isBlank(buffer.toString()) ? "parameter name: " + buffer.toString().substring(0, buffer.toString().length()-1) : "");
			collLinkDto.setIsMissParams(Boolean.TRUE);
			collLinkDto.setTipsMsg(tipsMsg);
		}
		
		logger.debug("end get collect link parameters, parameters: " + linkParametersTable + ", devId: " + devId);
		return linkParametersTable;
	}
	
	/**
	 * 根据链路ID获取分发参数
	 * @param devId
	 * @return
	 */
	private static Hashtable<String, Object> getDistParametersById(DistLinkDto distLinkDto, String devId) {
		logger.debug("begin get distribute link parameters, devId: " + devId);
		
		//用来记录出现问题的参数名称
		StringBuffer buffer = new StringBuffer();
		
		Hashtable<String, Object> linkParametersTable = new Hashtable<String, Object>();
		for (int i=0; i<distParameterList.size(); i++) {
			Map<String, Object> linkParametersMap = distParameterList.get(i);
			String currentDevId = StringTool.object2String(linkParametersMap.get("DEV_ID"));
			if (currentDevId.equals(devId)) {
				String paramName = StringTool.object2String(linkParametersMap.get("PARAM_NAME"));
				String paramValue = StringTool.object2String(linkParametersMap.get("PARAM_VALUE"));
				
				//链路参数是否必填
				String paramIsRequired = StringTool.object2String(linkParametersMap.get("IS_REQUIRED"));
				if (ParamsConstant.PARAMS_1.equals(paramIsRequired) && BlankUtil.isBlank(paramValue)) {
					buffer.append(paramName);
					buffer.append(",");
				}
				linkParametersTable.put(paramName, paramValue);
			}
		}
		
		//有必填参数为空或者改链路没有配置链路参数,TSK_TYPE=1 MQ分发不需要配置参数，FTP的需要检查参数
		if (distLinkDto.getTskType() == 2 && (BlankUtil.isBlank(linkParametersTable) || !BlankUtil.isBlank(buffer.toString()))) {
			//添加告警信息
			String tipsMsg = "link parameter is null or missing required, " 
					+ (!BlankUtil.isBlank(buffer.toString()) ? "parameter name: " + buffer.toString().substring(0, buffer.toString().length()-1) : "");
			distLinkDto.setIsMissParams(Boolean.TRUE);
			distLinkDto.setTipsMsg(tipsMsg);
		}
				
		logger.debug("end get distribute link parameters, parameters: " + linkParametersTable + ", devId: " + devId);
		return linkParametersTable;
	}
}
