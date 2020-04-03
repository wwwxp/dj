package com.tydic.service.monitor.pressure;

import java.util.Map;

public interface CustomPressureMonitorService {
	/**
	 * 获取拓扑摘要信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map getTopologySummary(Map<String, String> params)throws Exception;

	/**
	 * 查询拓扑配置信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map topologyConf(Map<String, String> params)throws Exception;
	
	/**
	 * 查询topologyState
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map topologyState(Map<String, String> params)throws Exception;
	
	/**
	 * 查询componentMetrics信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map componentMetrics(Map<String, String> params)throws Exception;
	
	/**
	 * 查询workerMetrics信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map workerMetrics(Map<String, String> params)throws Exception;
	
	/**
	 * 查询taskStats信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map taskStats(Map<String, String> params)throws Exception;
	
	 
}
