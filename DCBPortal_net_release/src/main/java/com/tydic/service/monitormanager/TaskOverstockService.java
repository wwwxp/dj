package com.tydic.service.monitormanager;

import java.util.List;
import java.util.Map;

public interface TaskOverstockService {



	/**
	 * 查询ZK集群列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryZookeeperList(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 查询ZK集群服务列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryZkServiceList(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 查询ZK集群服务组列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public List<String> queryZkServiceGroupList(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 查询ZK服务节点信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryZkServiceDataList(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 查询ZK服务组节点数据
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryZkServiceDataListWithGroup(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 查询服务积压情况（图表展示，根据服务名称分组）
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryChartsList(Map<String, Object> params, String dbKey) throws Exception;
}
