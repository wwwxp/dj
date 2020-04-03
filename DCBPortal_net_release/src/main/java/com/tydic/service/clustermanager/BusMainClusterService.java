package com.tydic.service.clustermanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BusMainClusterService {

	/**
	 * 删除集群
	 * @param list
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> deleteBusMainCluster(List<Map<String, String>> list, String dbKey) throws Exception;

	/**
	 * 新增集群
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insertBusMainCluster(Map<String, Object> params, String dbKey) throws Exception;

	
	/**
	 * 修改集群配置信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateBusMainCluster(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 获取用户集群配置信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>>getUserBusMainCluster(Map<String, Object> params, String dbKey) throws Exception;
}
