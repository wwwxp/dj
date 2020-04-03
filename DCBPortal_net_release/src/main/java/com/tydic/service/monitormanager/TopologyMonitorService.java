package com.tydic.service.monitormanager;

import java.util.Map;

public interface TopologyMonitorService {

	public Map queryTopologySummary(Map<String, String> params) throws Exception;

	public Map queryWorkerMetrics(Map<String, String> params) throws Exception;

	public Map queryComponentMetric(Map<String, String> params) throws Exception;

	public Map queryComponentMetricList(Map<String, String> params) throws Exception;

	public Map<String, Object> querySupervisorWorkers(Map<String, String> params) throws Exception;

	public Map queryNettyMetrics(Map<String, String> params) throws Exception;

	public Map<String, Object> querySupervisorWorkerMetrics(Map<String, String> params) throws Exception;
	
	 
}
