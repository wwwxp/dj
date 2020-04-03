package com.tydic.service.versiondeployment.service;

import com.tydic.service.versiondeployment.bean.PubCfg2Enty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NodeCfgPublishService {
    /**
     * 查
     * @param dbKey
     * @return
     */
    List<HashMap<String, String>> queryVersionType(String dbKey);

    List<HashMap<String, String>> queryDeployVersion(String nodeTypeId,String dbKey);

    List<HashMap<String, Object>> queryNodeDeployInfo(String nodeTypeId,String version, String dbKey);

    List<PubCfg2Enty> queryNodeDeployCfgFileDir(String nodeTypeId, String nodeId, String version, String dbKey);

    Map<String, String> getFileContent(Map<String, String> params, String dbKey);

    String updateCfgAndPublish(PubCfg2Enty pubCfg2Enty,boolean isPublishAll, String fileContent, String dbkey);

    Map<String, String> synConfig(Map<String, Object> params, String dbKey) throws Exception;

}