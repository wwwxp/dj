package com.tydic.service.clustermanager;

import java.util.Map;

public interface GroupConfigService {

	/**
	 * 添加配置信息
	 *
	 * @param param
	 */
	public Map<String, Object> addGroupConfig(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * 删除配置信息
	 *
	 * @param param
	 */
	public Map<String, Object> delGroupConfig(Map<String, Object> param, String dbKey) throws Exception;
}
