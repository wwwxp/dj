package com.tydic.service.monitor.business.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tydic.service.monitor.business.BusinessMonitorService;
@Service("businessMonitorServiceImpl")
public class BusinessMonitorServiceImpl implements BusinessMonitorService {

	@Override
	public List<Map<String, Object>> getResourceChartsData(
			Map<String, String> params) {
		/*Map<String, List> resourceMap = HostRealTimeMonitorImpl.resourceMap;
		String key = params.get("RESOURCE_ID").toString();
		return resourceMap.get(key);*/
		return null;
	}

}
