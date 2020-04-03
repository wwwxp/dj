package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface M2dbStartService {
	
	/**
	 * 启动M2db
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startM2db(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止M2db
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopM2db(List<Map<String, String>> map, String dbKey) throws Exception; 

}
