package com.tydic.service.clustermanager;

import java.util.Map;

public interface BusProgramTaskService {

	/**
	 * 获取业务程序状态检查中用到的配置文件
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> getBusTargetConfigList(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 获取业务程序配置文件列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> getBusConfigList(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 获取业务程序列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> getBusProgramListWithHost(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 删除删除业务程序
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> deleteBusProgramTask(Map<String, Object> params, String dbKey) throws Exception;


	/**
	 * 业务程序删除，只删除当前业务程序版本，其他版本相同业务程序不会删除
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> deleteCurrVersionBusProgramTask(Map<String, Object> params, String dbKey) throws Exception;

	
	/**
	 * 删除Topology业务程序
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> deleteBusTopologyProgramTask(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 新增业务程序
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insetBusProgramTask(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 修改业务程序
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateBusProgramTask(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 修改业务程序
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateTaskCell(Map<String, Object> params, String dbKey) throws Exception;


	/**
	 * 查询业务启停日志信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> queryLogDetail(Map<String, Object> params, String dbKey) throws Exception;
}
