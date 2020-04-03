package com.tydic.service.plugin;

import java.util.List;
import java.util.Map;

public interface TopologyPluginService {
	/**
	 * 获取插件列表
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryPlugin(Map<String, Object> params, String dbKey)throws Exception;

	
	/**
	 * 获取XML文件信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getXmlDesc(Map<String, Object> params, String dbKey)throws Exception;

}
