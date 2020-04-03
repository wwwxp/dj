package com.tydic.service.monitormanager.clustersummary;

import java.util.List;
import java.util.Map;

public interface ClusterSummaryService {


	/**
	 * 获取集群摘要列表
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getBusClusterList(Map<String, Object> params)throws Exception;

	/**
	 * 获取集群配置
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getClusterList(Map<String, String> params)throws Exception;
	
	/**
	 * 获取集群摘要表格信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> showCluster(Map<String, Object> params)throws Exception;
	
	/**
	 * 拓扑信息--获取配置信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List showTopConfInfo(Map<String, String> params)throws Exception;
	
	/**
	 * nimbus信息--获取配置信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List showNimConfInfo(Map<String, String> params)throws Exception;
	
	/**
	 * supervisor信息--获取配置信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List showSupConfInfo(Map<String, String> params)throws Exception;
}
