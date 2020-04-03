package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface DclogStartService {
	
	/**
	 * 启动Dclog
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startDclog(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止Dclog
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopDclog(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
}
