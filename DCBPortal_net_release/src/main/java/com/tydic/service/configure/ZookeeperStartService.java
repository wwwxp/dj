package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface ZookeeperStartService {
	
	/**
	 * 启动zookeeper
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startZookeeper(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止zookeeper
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopZookeeper(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
}
