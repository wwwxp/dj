package com.tydic.service.nodemanager;

import java.util.List;
import java.util.Map;

public interface ClusterManagerService {
    public Map<String,Object> insertCluster(Map<String,Object> params,String userName,String dbKey) throws Exception;
    public Map<String,Object> deleteCluster(Map<String,Object> params, String dbKey) throws Exception;
    public Map<String,Object> loadClusterInfo(Map<String,Object> params,int pageSize,int pageIndex,String dbKey) throws Exception;
    public Map<String,Object> findClusterById(Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> updateCluster(Map<String,Object> params,String dbKey) throws Exception;

}
