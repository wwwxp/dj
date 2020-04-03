package com.tydic.service.configure;

import java.util.Map;

public interface OtherService {

	/**
	 * 起停版本
	 * 
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateRunAndStopHost(Map<String, Object> param,
			String dbKey) throws Exception;

	/**
	 * 检查
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateCheckHostState(Map<String, Object> params,
			String dbkey) throws Exception;

}
