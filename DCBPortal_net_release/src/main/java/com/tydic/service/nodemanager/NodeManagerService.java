package com.tydic.service.nodemanager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface NodeManagerService {

    public Map<String,Object> insertNode(Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> insertBatchNode(Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> updateNode(Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> deployRunState(List<Map<String,Object>> params, String dbKey) throws Exception;
    public Map<String,Object> deleteNode(String userName,List<Map<String,Object>> params, String dbKey) throws Exception;
}
