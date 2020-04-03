package com.tydic.service.configure;

import com.tydic.common.BusException;

import java.util.Map;

public interface RunSameIPService {

	/**
	 * 起停版本
	 * 
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateRunAndStopHost(Map<String, Object> param, String dbKey) throws BusException;

	/**
	 * 检查
	 * 
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateCheckHostState(Map<String, Object> params, String dbKey) throws BusException;

}
