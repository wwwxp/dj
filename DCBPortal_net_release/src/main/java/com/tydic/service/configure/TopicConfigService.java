package com.tydic.service.configure;

import java.util.Map;

public interface TopicConfigService {

	/**
	 * 添加Topic配置信息
	 * @param params
	 * @return Map
	 */
	public Map<String, Object> addTopicConfig(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 删除Topic配置信息
	 * @param params
	 * @return Map
	 */
	public Map<String, Object> delTopicConfig(Map<String, Object> params, String dbKey);
}
