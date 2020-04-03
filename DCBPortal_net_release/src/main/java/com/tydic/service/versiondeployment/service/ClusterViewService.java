package com.tydic.service.versiondeployment.service;

import com.tydic.service.versiondeployment.bean.json.ClusterViewEnty;

import java.util.List;
import java.util.Map;

public interface ClusterViewService {
    public ClusterViewEnty getClusterView(String cluster_id,String dbKey);
    public List<Map<String, Object>> refreshClusterState(String cluster_id, String dbKey);
}
