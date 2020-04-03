package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface RocketmqStartService {
	/**
	 * 开启任务
	 * @param map
	 */
	public Map<String, Object> startRocketmq(List<Map<String,String>> map, String dbKey) throws Exception; 
	/**
	 * 停止任务
	 * @param map
	 */
	public Map<String, Object> stopRocketmq(List<Map<String,String>> map, String dbKey) throws Exception; 
	
	
}
