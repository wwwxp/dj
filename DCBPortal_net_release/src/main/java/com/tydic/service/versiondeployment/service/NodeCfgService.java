package com.tydic.service.versiondeployment.service;

import com.tydic.service.versiondeployment.bean.PubCfg2Enty;

import java.util.List;
import java.util.Map;

public interface NodeCfgService {
    public List<PubCfg2Enty> queryCfgInfo(String nodeTypeId, String version, String dbKey);

    public Map<String, String> getFileContent(Map<String, String> params, String dbKey) throws Exception;

    public String updateCfgAndPublish(PubCfg2Enty pubCfg2Enty, String fileContent, String dbKey);
}
