package com.tydic.service.clustermanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface ServiceTypeService {

	/**
	 * 删除集群
	 * @param paramsList
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> deleteServiceType(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 新增集群
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insertServiceType(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	
	/**
	 * 修改集群配置信息
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateServiceType(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;
	
	/**
	 * 获取组件集群配置参数
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> queryComponentsParams(Map<String, Object> params, String dbKey) throws Exception;
}
