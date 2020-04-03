package com.tydic.dcm.ftran;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.tools.StringTool;

public class Sequence {

	private static final Long maxSequence = 99999999L;
	
	private static final Long minSequence = -1L;
	
	private static Logger logger = Logger.getLogger(Sequence.class);
	
	private static Hashtable<String, Long> sequenceTab = new Hashtable<String, Long>();
	
	/**
	 * 初始化获取链路序列
	 * @param devId
	 */
	public static void initSequence(String devId) {
		Long lastSequence = -1L;
		//获取当前链路最新序列
		try {
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("DEV_ID", devId);
			Map<String, Object> rstSequence = JdbcUtil.queryForObject("distributeMapper.queryLastSequence", queryParams, FrameConfigKey.DEFAULT_DATASOURCE);
			if (!BlankUtil.isBlank(rstSequence) && !rstSequence.isEmpty()) {
				//解析序列
				String lastSequenceStr = StringTool.object2String(rstSequence.get("LAST_SEQUENCE"));
				lastSequenceStr = lastSequenceStr.substring(lastSequenceStr.lastIndexOf("_") + 1);
				lastSequence = Long.valueOf(lastSequenceStr);
				if (lastSequence > maxSequence) {
					lastSequence = minSequence;
				}
			}
		} catch (NumberFormatException e) {
			logger.error("get link sequence fail, devId: " + devId, e);
			e.printStackTrace();
		}
		logger.debug("init sequence value: " + lastSequence + ", devId: " + devId);
		sequenceTab.put(devId, lastSequence);
	}
	
	/**
	 * 获取当前链路最新序列
	 * @param devId
	 * @return
	 * @throws InterruptedException 
	 */
	public synchronized static Long getLastSequence(String devId) {
		Long newSequence = 0L;
		if (isExistLink(devId)) {
			Long lastSequence = sequenceTab.get(devId);
			newSequence = ++lastSequence;
			sequenceTab.put(devId, newSequence);
		} else {
			initSequence(devId);
			newSequence = getLastSequence(devId);
		}
		logger.debug("get next sequence value: " + newSequence + ", devId: " + devId);
		return newSequence;
	}
	
	/**
	 * 获取当前链路最新序列
	 * @param devId
	 * @return
	 */
	public static Long getCurrentSequence(String devId) {
		Long newSequence = 0L;
		if (isExistLink(devId)) {
			Long currentSequence = sequenceTab.get(devId);
			newSequence = currentSequence;
		} else {
			initSequence(devId);
			newSequence = getCurrentSequence(devId);
		}
		logger.debug("get current sequence value: " + newSequence + ", devId: " + devId);
		return newSequence;
	}
	
	/**
	 * 序列回退
	 * @param devId
	 * @return
	 */
	public synchronized static Boolean rollBackSequence(String devId) {
		Long lastSequence = sequenceTab.get(devId);
		Long newSequence = --lastSequence;
		sequenceTab.put(devId, newSequence);
		logger.debug("get last sequence value: " + newSequence + ", devId: " + devId);
		return true;
	}
	
	/**
	 * 判断当前链路序列是否存在
	 * @param devId
	 * @return
	 */
	public static Boolean isExistLink(String devId) {
		if (BlankUtil.isBlank(sequenceTab.get(devId))) {
			return false;
		}
		return true;
	}
}
