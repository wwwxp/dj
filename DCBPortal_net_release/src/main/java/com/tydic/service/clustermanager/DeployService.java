package com.tydic.service.clustermanager;

import java.util.List;
import java.util.Map;

public interface DeployService {

	/**
	 * 部署环境
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public String updateDeployHost(Map<String, Object> param, String dbKey) throws Exception;
	
	
	/**
	 * 删除主机以及远程目录
	 * @param param
	 */
	public String deteleHostAndPath(Map<String,String> param, String dbKey) throws Exception;

	
	/**
	 * 查询配置列表
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List queryFileTree(Map<String, String> params) throws Exception;

	/**
	 * 集群划分--添加supervisor主机
	 * @param paramsList
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public String insertSupervisor(Map<String, Object> paramsList, String dbKey) throws Exception;

	/**
	 * 集群划分--添加业务主机
	 * @param paramsList
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public String insertBusiness(Map<String, Object> paramsList, String dbKey) throws Exception;
	
	/**
	 * 集群划分--批量删除主机
	 * @param paramsList
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public String delHostBatchPartition(Map<String, Object> paramsList, String dbKey) throws Exception;
	
	/**
	 * 集群划分--删除业务类主机
	 * @param paramsMap
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public String deleteServiceHost(Map paramsMap, String dbKey) throws Exception;
 
}
