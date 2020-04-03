package com.tydic.service.monitor.resource;

import java.util.List;
import java.util.Map;

public interface CustomResourceMonitorService {
	 public Map workerMetrics(Map<String, String>  params) throws Exception;
	 public Map nimbusConf(Map<String, String>  params)throws Exception;
	 public Map getCustomSupervisorInfo(Map<String, String>  params)throws Exception;
	 public List<Map<String,Object>> getChartsData(Map<String, String> params);
	 
	 
}
