package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface DmdbStartService {
	
	/**
	 * 启动Dmdb
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startDmdb(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止Dmdb
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopDmdb(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
}
