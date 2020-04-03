package com.tydic.service.versiondeployment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.StartNodeService;
import com.tydic.service.versiondeployment.bean.json.ClusterViewEnty;
import com.tydic.service.versiondeployment.service.ClusterViewService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClusterViewServiceImpl implements ClusterViewService {

    private static Logger log = Logger.getLogger(NodeCfgPublishServiceImpl.class);

    @Autowired
    CoreService coreService;

    @Autowired
    StartNodeService startNodeService;

    @Override
    public ClusterViewEnty getClusterView(String cluster_id, String dbKey) {
        ClusterViewEnty clusterView = queryClusterForViewEnty(cluster_id, dbKey);
        if (clusterView == null) {
            return null;
        }
        List<ClusterViewEnty> clusterViewEntyList = queryClusterNodeTypeForViewEnty(cluster_id, dbKey);

        clusterView.setChildren(clusterViewEntyList);

        return clusterView;
    }

    @Override
    public List<Map<String, Object>> refreshClusterState(String cluster_id, String dbKey) {
        //查询集群所有NodeId
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("NODE_CLUSTER_ID", cluster_id);
        List<Map<String, Object>> mapList = coreService.queryForList3New("nodeClusterManager.queryClusterDeployListForRefeash", paramMap, dbKey);
        //刷新NodeId状态
        formatRefreshListForVersion(mapList,3);
        try {
            List<Map<String, Object>> list = startNodeService.checkNode(mapList);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤掉不需要的版本
     * @param mapList
     */
    private void formatRefreshListForVersion(List<Map<String, Object>> mapList,int versionDepth) {
    }

    /**
     * 查询单个集群消息
     *
     * @param cluster_id
     * @param dbKey
     * @return
     */
    public ClusterViewEnty queryClusterForViewEnty(String cluster_id, String dbKey) {
        if (BlankUtil.isBlank(cluster_id)) {
            throw new RuntimeException("集群id错误");
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("ID", cluster_id);
        List<HashMap<String, String>> clusList = coreService.queryForList("nodeClusterManager.queryNodeClusterConfig", paramMap, dbKey);
        if (!BlankUtil.isBlank(clusList)) {
            Map<String, String> clusMap = clusList.get(0);
            String node_cluster_name = StringTool.object2String(clusMap.get("NODE_CLUSTER_NAME"));
            String node_cluster_code = StringTool.object2String(clusMap.get("NODE_CLUSTER_CODE"));
            String node_cluster_state = StringTool.object2String(clusMap.get("NODE_CLUSTER_STATE"));
            String node_cluster_desc = StringTool.object2String(clusMap.get("NODE_CLUSTER_DESC"));
            ClusterViewEnty clusterViewEnty = new ClusterViewEnty();
            clusterViewEnty.setName(node_cluster_name + "[" + node_cluster_code + "]");
            clusterViewEnty.setValue(node_cluster_desc);
            clusterViewEnty.setC_type(ClusterViewEnty.C_TYPE_CLUSTER);
            return clusterViewEnty;
        }
        return null;
    }

    /**
     * 查询单个集群下的多个程序类型
     *
     * @param cluster_id
     * @param dbKey
     * @return
     */
    public List<ClusterViewEnty> queryClusterNodeTypeForViewEnty(String cluster_id, String dbKey) {
        if (BlankUtil.isBlank(cluster_id)) {
            throw new RuntimeException("集群id错误");
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("NODE_CLUSTER_ID", cluster_id);
        List<HashMap<String, String>> ndInfoList = coreService.queryForList("nodeClusterManager.queryClusterTypeCfg", paramMap, dbKey);
        List<ClusterViewEnty> nodeTypeViewEntyList = null;
        if (!BlankUtil.isBlank(ndInfoList)) {
            nodeTypeViewEntyList = new ArrayList<>();
            for (Map<String, String> ndtInfoMap : ndInfoList) {
                String clsTypeId = StringTool.object2String(ndtInfoMap.get("ID"));
                String node_type_alias = StringTool.object2String(ndtInfoMap.get("NODE_TYPE_ALIAS"));
                String name = StringTool.object2String(ndtInfoMap.get("NAME"));
                String code = StringTool.object2String(ndtInfoMap.get("CODE"));
                String version = StringTool.object2String(ndtInfoMap.get("CURR_VERSION"));
                ClusterViewEnty nodeTypeViewEty = new ClusterViewEnty();
                String ntyViewName = BlankUtil.isBlank(node_type_alias) ? name : node_type_alias;
                ntyViewName = ntyViewName + "[" + code + "]";
                nodeTypeViewEty.setName(ntyViewName);
                nodeTypeViewEty.setValue(version);
                nodeTypeViewEty.setC_type(ClusterViewEnty.C_TYPE_NODE_TYPE);
                //第三层，子节点
                List<ClusterViewEnty> NodeInfoForViewEntyList = queryClusterNodeInfoForViewEnty(clsTypeId, dbKey);
                nodeTypeViewEty.setChildren(NodeInfoForViewEntyList);

                nodeTypeViewEntyList.add(nodeTypeViewEty);
            }
        }
        return nodeTypeViewEntyList;
    }

    /**
     * 查询单个程序类型下的所属集群节点
     *
     * @param cluTypeId
     * @param dbKey
     * @return
     */
    public List<ClusterViewEnty> queryClusterNodeInfoForViewEnty(String cluTypeId, String dbKey) {
        if (BlankUtil.isBlank(cluTypeId)) {
            return null;
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("CLUSTER_TYPE_ID", cluTypeId);
        List<HashMap<String, String>> ndInfoList = coreService.queryForList("nodeClusterManager.queryClusterTypeNodeDetailCfg", paramMap, dbKey);
        List<ClusterViewEnty> nodeTypeViewEntyList = null;
        if (!BlankUtil.isBlank(ndInfoList)) {
            nodeTypeViewEntyList = new ArrayList<>();
            for (Map<String, String> ndInfoMap : ndInfoList) {
                String nodeId = StringTool.object2String(ndInfoMap.get("NODE_ID"));
                String nodeName = StringTool.object2String(ndInfoMap.get("NODE_NAME"));
                String hostIp = StringTool.object2String(ndInfoMap.get("HOST_IP"));
                String version = StringTool.object2String(ndInfoMap.get("VERSION"));
                String deploy_id = StringTool.object2String(ndInfoMap.get("DEPLOY_ID"));
                String state = StringTool.object2String(ndInfoMap.get("STATE"));
                ClusterViewEnty nodeTypeViewEty = new ClusterViewEnty();
                nodeTypeViewEty.setName(getNodeViewEtyName(nodeName, hostIp, version, state));
                nodeTypeViewEty.setValue(nodeId);//节点id
                nodeTypeViewEty.setC_id(deploy_id);//部署id
                nodeTypeViewEty.setC_type(ClusterViewEnty.C_TYPE_NODE);
                nodeTypeViewEntyList.add(nodeTypeViewEty);
            }
        }
        return nodeTypeViewEntyList;
    }

    /**
     * 节点名称(节点ip)[运行状态]版本
     *
     * @param nodeName
     * @param hostIp
     * @param version
     * @param state
     * @return
     */
    public static String getNodeViewEtyName(String nodeName, String hostIp, String version, String state) {
        String curState = null;
        if (BlankUtil.isBlank(version) && BlankUtil.isBlank(state)) {
            curState = "待部署状态";
        } else if (!BlankUtil.isBlank(version) && BlankUtil.isBlank(state)) {
            curState = "已部署状态";
        } else {
            curState = "1".equals(state) ? "正在运行状态" : "未运行状态";
        }
        String name = String.format("%s(%s)[%s]%s", nodeName, hostIp, curState, version);
        return name;
    }
}
