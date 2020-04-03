package com.tydic.service.monitor.cluster.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.jstorm.ui.model.ClusterEntity;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.tydic.service.monitor.cluster.ClusterMonitorService;

@Service("clusterMonitorService")
public class ClusterMonitorServiceImpl  implements ClusterMonitorService{
	private static Logger log = LoggerFactory.getLogger(ClusterMonitorServiceImpl.class);

	@Override
	public Map showClusters(Map<String, String> params) throws Exception {
		
		   Map resultMap = new HashMap();
		   UIUtils.readUiConfig();
	        long start = System.currentTimeMillis();
	        log.info("nimbus config: " + UIUtils.clusterConfig);
	        Collection<ClusterEntity> clusterEntities = UIUtils.clustersCache.values();
	        resultMap.put("data", clusterEntities);
	        log.info("clusters page show cost:{}ms", System.currentTimeMillis() - start);
		return resultMap;
	}

}
