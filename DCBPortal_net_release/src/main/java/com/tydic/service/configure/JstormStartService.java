package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface JstormStartService {
	
	/**
	 * 启动Jstorm
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startJstorm(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止Jstorm
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopJstorm(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
}
