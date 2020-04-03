package com.tydic.service.configure;

import java.util.Map;

public interface RunTopologyService {

	/**
	 * 起停版本
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateRunAndStopHost(Map<String,Object> param,String dbKey) throws Exception;
	
	/**
	 * 检查程序启停
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateCheckProgramState(Map<String,Object> param,String dbKey) throws Exception;
	
	/**
	 * 查看定义
	 * @param param
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> queryViewConf(Map<String, Object> param, String dbKey) throws Exception;

}
