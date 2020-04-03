package com.tydic.service.configure;

import java.util.Map;

import com.tydic.common.BusException;

public interface RunDiffIPService {

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
	 * Route程序状态检查
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateCheckHostState(Map<String, Object> params, String dbkey) throws BusException;

	/**
	 * Route程序主备切换
	 * 
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateSwitch(String dbKey) throws Exception;

}
