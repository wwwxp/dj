package com.tydic.service.configure;

import java.util.Map;

public interface TopManagerService {
	/**
	 * 重载
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> topRebalanceReload(Map<String, Object> params, String dbKey) throws Exception;
	
}
