package com.tydic.dcm.util.tools;

import com.tydic.dcm.dto.CollLinkDto;
import com.tydic.dcm.dto.DistLinkDto;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Map;

/**
 * 将链路Map对象转化为对应Dto
 * 
 * @author Yuanh
 *
 */
public class ConvertMap2Dto {
	/**
	 * 日志对象
	 */
	//private static Logger logger = Logger.getLogger(ConvertMap2Dto.class);

	/**
	 * 将采集链路属性Map对象转化为Dto
	 * 
	 * @param linkMap
	 * @return
	 */
	public static CollLinkDto convert2CollDto(Map<String, Object> linkMap) {
		//logger.debug("begin conver collect link map to dto, map info : " + linkMap);
		CollLinkDto collLinkDto = new CollLinkDto();
		String devId = StringTool.object2String(linkMap.get("DEV_ID"));
		collLinkDto.setDevId(Long.parseLong(devId));
		collLinkDto.setDevName(StringTool.object2String(linkMap.get("DEV_NAME")));
		collLinkDto.setAddrId(linkMap.get("ADDR_ID") == null ? null : Long.parseLong(((Object) linkMap.get("ADDR_ID")).toString()));
		collLinkDto.setSubType(StringTool.object2String(linkMap.get("SUB_TYPE")));
		collLinkDto.setRemark(StringTool.object2String(linkMap.get("REMARK")));
		collLinkDto.setValidFlag(StringTool.object2String(linkMap.get("STATUS")));
		collLinkDto.setFmtFlag(StringTool.object2String(linkMap.get("FMT_FLAG")));
		collLinkDto.setDateMode(StringTool.object2String(linkMap.get("DATE_MODE")));
		collLinkDto.setTimeMode(StringTool.object2String(linkMap.get("TIME_MODE")));
		collLinkDto.setRltDistId(StringTool.object2String(linkMap.get("RLT_DIST_ID")));
		collLinkDto.setBeginTime(StringTool.object2String(linkMap.get("BEGIN_TIME")));
		collLinkDto.setEndTime(StringTool.object2String(linkMap.get("END_TIME")));
		collLinkDto.setLatnId(StringTool.object2String(linkMap.get("LATN_ID")));
		collLinkDto.setLinkErr(StringTool.object2String(linkMap.get("LINK_ERR")));
		collLinkDto.setStateBeginTime(StringTool.object2String(linkMap.get("STATE_BEGIN_TIME")));
		collLinkDto.setStateEndTime(StringTool.object2String(linkMap.get("STATE_END_TIME")));
		collLinkDto.setRunState(StringTool.object2String(linkMap.get("RUN_STATE")));
		collLinkDto.setFileStoreType(StringTool.object2String(linkMap.get("FILE_STORE_TYPE")));
		//logger.debug("end conver collect link map to dto, dto info : " + collLinkDto);
		return collLinkDto;
	}
	
	
	/**
	 * 分发链路属性Map对象转化为Dto
	 * 
	 * @param linkMap
	 * @return
	 */
	public static DistLinkDto convert2DistDto(Map<String, Object> linkMap) {
		//logger.debug("begin conver distribute link map to dto, map info : " + linkMap);
		DistLinkDto distLinkDto = new DistLinkDto();
		String devId = StringTool.object2String(linkMap.get("DEV_ID"));
		distLinkDto.setDevId(Long.parseLong(devId));
		distLinkDto.setDevName(StringTool.object2String(linkMap.get("DEV_NAME")));
		distLinkDto.setAddrId(linkMap.get("ADDR_ID") == null ? null : Long.parseLong(StringTool.object2String(linkMap.get("ADDR_ID"))));
		distLinkDto.setSubType(StringTool.object2String(linkMap.get("SUB_TYPE")));
		distLinkDto.setRemark(StringTool.object2String(linkMap.get("REMARK")));
		distLinkDto.setValidFlag(StringTool.object2String(linkMap.get("STATUS")));
		distLinkDto.setLatnId(StringTool.object2String(linkMap.get("LATN_ID")));
		distLinkDto.setCollDevId(StringTool.object2String(linkMap.get("COLL_DEV_ID")));
		distLinkDto.setBeginTime(StringTool.object2String(linkMap.get("BEGIN_TIME")));
		distLinkDto.setEndTime(StringTool.object2String(linkMap.get("END_TIME")));
		distLinkDto.setParentFlag(StringTool.object2String(linkMap.get("PARENT_FLAG")));
		distLinkDto.setRunState(StringTool.object2String(linkMap.get("RUN_STATE")));
		distLinkDto.setLinkErr(StringTool.object2String(linkMap.get("LINK_ERR")));
		distLinkDto.setStateBeginTime(StringTool.object2String(linkMap.get("STATE_BEGIN_TIME")));
		distLinkDto.setTskType(NumberUtils.toInt(ObjectUtils.toString(linkMap.get("TSK_TYPE")), 2));//1MQ 2ftp
		distLinkDto.setFileStoreType(StringTool.object2String(linkMap.get("FILE_STORE_TYPE")));
		//logger.debug("end conver distribute link map to dto, dto info : " + distLinkDto);
		return distLinkDto;
	}
}
