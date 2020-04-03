package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface MonitorStartService {
	
	/**
	 * 启动Monitor
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startMonitor(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止Monitor
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopMonitor(List<Map<String, String>> map, String dbKey) throws Exception; 
	
}
