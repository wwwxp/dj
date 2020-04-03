package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface DcaStartService {
	
	/**
	 * 启动Dca
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startDca(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止Dca
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopDca(List<Map<String, String>> map, String dbKey) throws Exception;

	/**
	 * 启动Dca
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startSxDca(List<Map<String, String>> map, String dbKey) throws Exception;


	/**
	 * 停止Dca
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopSxDca(List<Map<String, String>> map, String dbKey) throws Exception;


}
