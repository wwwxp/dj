package com.tydic.service.nodemanager;

import java.util.List;
import java.util.Map;

public interface StartNodeService {

    public Map<String,Object> startNode(String userName,List<Map<String,Object>> params) throws Exception;
    public Map<String,Object> stopNode(String userName,List<Map<String,Object>> params) throws Exception;
    public List<Map<String,Object>> checkNode(List<Map<String,Object>> params) throws Exception;
    public List<Map<String,Object>> loadFileTree(Map<String,Object> params) throws Exception;
    public Map<String,Object> getFileContent(Map<String,Object> params) throws Exception;
    public Map<String,Object> deleteNodeVersion(String userName,List<Map<String,Object>> params) throws Exception;

}
