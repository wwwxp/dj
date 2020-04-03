package com.tydic.service.configure;

import java.util.Map;

public interface ProcessStateOfHostService {
	/**
	 * 检查运行状态
	 * 
	 * @param param
	 * @return
	 */
	public Map<String, Object> checkHostState(Map<String, Object> params, String dbKey) throws Exception;
}
