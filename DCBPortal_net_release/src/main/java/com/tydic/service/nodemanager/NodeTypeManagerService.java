package com.tydic.service.nodemanager;

import java.util.List;
import java.util.Map;

public interface NodeTypeManagerService {

    public Map<String,Object> insertNodeType(String userName,Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> updateNodeType(String userName,Map<String,Object> params,String dbKey) throws Exception;
    public Map<String,Object> beingUsed(List<Map<String,Object>> params, String dbKey) throws Exception;
    public Map<String,Object> deleteNodeType(String userName,List<Map<String,Object>> params,String dbKey) throws Exception;
}
