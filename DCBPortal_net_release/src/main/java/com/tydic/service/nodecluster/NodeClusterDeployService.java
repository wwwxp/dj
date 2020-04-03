package com.tydic.service.nodecluster;

import java.util.Map;

/**
 * Auther: Yuanh
 * Date: 2019-09-19 09:52
 * Description:
 */
public interface NodeClusterDeployService {

    /**
     * 查询节点集群部署程序列表
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryNodeClusterProgramList(Map<String, Object> params, String dbKey) throws Exception;

    /**
     * 查询节点集群部署版本列表&主机列表
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryNodeClusterVersionHostList(Map<String, Object> params, String dbKey) throws Exception;

    /**
     * 节点部署开始
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String, Object> startNodeDeploy(Map<String, Object> params, String dbKey) throws Exception;

    /**
     * web类型的节点部署开始
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String, Object> startWebNodeDeploy(Map<String, Object> params, String dbKey) throws Exception;


}
