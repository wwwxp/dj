package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface InstConfigService {
	
	/**
	 * 删除实例
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> deleteInstConfig(Map<String, Object> params, String dbKey) throws Exception; 

	/**
	 * 查询实例状态
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> queryInstConfigTreeData(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 查询实例状态
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> querybusInstConfigTreeData(Map<String, Object> params, String dbKey) throws Exception;


	/**
	 * 查询实例启停日志详细信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> queryInstConfigLogDetail(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 获取启停日志文件名称
	 * @param instId
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLogName(String instId, String dbKey) throws Exception;


	/**
	 * 获取启停日志文件名称
	 * @param clusterId
	 * @param clusterType
	 * @param hostId
	 * @param version
	 * @param deployFileType
	 * @param startAutoFilePath
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLogName(String clusterId, String clusterType, String hostId, String version, String deployFileType, String startAutoFilePath) throws Exception;

	/**
	 * 获取日志文件目录
	 * @param clusterType
	 * @return
	 * @throws Exception
	 */
	public String getLogPath(String clusterType) throws Exception;

}
