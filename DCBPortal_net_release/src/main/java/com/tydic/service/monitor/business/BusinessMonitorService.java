package com.tydic.service.monitor.business;

import java.util.List;
import java.util.Map;

public interface BusinessMonitorService {
	 public List<Map<String,Object>> getResourceChartsData(Map<String, String> params);

}
