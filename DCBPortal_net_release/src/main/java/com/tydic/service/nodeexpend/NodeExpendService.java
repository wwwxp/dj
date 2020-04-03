package com.tydic.service.nodeexpend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface NodeExpendService {
	/**
	 * 查询集群Topology列表
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> queryClusterTreeList(Map<String, Object> params, String dbKey) throws Exception; 
	
	
	/**
	 * 新增阈值配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addThresholdConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	
	/**
	 * 修改阈值配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateThresholdConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	/**
	 * 修改阈值配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> delThresholdConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;
	
	
	/**
	 * 新增定时配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addTimingConfig(List<Map<String, String>> params, String dbKey, HttpServletRequest request) throws Exception;

	
	/**
	 * 修改定时配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateTimingConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	/**
	 * 删除定时配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> delTimingConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;
	
	
	/**
	 * 新增手动配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addManualConfig(List<Map<String, String>> params, String dbKey, HttpServletRequest request) throws Exception;
	
	/**
	 * 新增手动配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addExecConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	
	/**
	 * 新增手动配置
	 * @param param
	 * @param dbKeyupdateExecImmJob
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateNodeexpendJob(Map<String, Object> params, String dbKey) throws Exception;
	
	
	/**
	 * 新增手动配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateExecImmJob(Map<String, Object> params, String dbKey) throws Exception;
	
	/**
	 * 修改手动配置
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateManualConfig(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;


}
