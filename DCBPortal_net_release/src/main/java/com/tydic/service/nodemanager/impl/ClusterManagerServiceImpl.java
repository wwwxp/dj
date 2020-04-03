package com.tydic.service.nodemanager.impl;

import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.ClusterManagerService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.ParamsConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author 王贤朋
 *
 */
@Service
public class ClusterManagerServiceImpl implements ClusterManagerService {

    /**
     * 核心Service对象
     */
    @Resource
    private CoreService coreService;

    private static Logger log = LoggerFactory.getLogger(ClusterManagerServiceImpl.class);

    /**
     * 集群表、及其关联表的新增
     * @param params
     * @param userName
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> insertCluster(Map<String, Object> params, String userName,String dbKey) throws Exception {
        log.debug("集群表、及其相关联表的的插入开始...");
        Map<String,Object> result = new HashMap<>();
        result.put("rstCode",BusinessConstant.PARAMS_BUS_0);

        //判断集群名称和集群编码是否为空
        String clusterCode = StringTool.object2String(params.get("NODE_CLUSTER_CODE"));
        String clusterName = StringTool.object2String(params.get("NODE_CLUSTER_NAME"));
        if(BlankUtil.isBlank(clusterCode) || BlankUtil.isBlank(clusterName)){
            result.put("rstMsg","集群名称和集群编码不能为空，请重新输入！");
            return result;
        }

        if(!clusterCode.matches("^[\\w-$]+$")){
            result.put("rstMsg","集群编码只能由字母、数字、-、$、下划线组成！");
            return result;
        }

        //判断集群编码是否重复
        Map<String,Object> clusterCountMap = coreService.queryForObject2New("nodeClusterManager.queryClusterExistsByCode",params,dbKey);
        if (!StringTool.object2String(clusterCountMap.get("CLUSTER_COUNT")).equals(BusinessConstant.PARAMS_BUS_0)) {
            result.put("rstMsg", "集群的编码已存在，请重新输入！");
            return result;
        }

        params.put("CREATED_USER",userName);
        params.put("NODE_CLUSTER_STATE",1);

        //插入数据到集群表
        coreService.insertObject2New("nodeClusterManager.insertCluster",params,dbKey);

        log.debug("集群表的插入完成...");

        String nodeClusterId = StringTool.object2String(params.get("NEWEST_CLUSTER_ID"));

        List<Map<String,Object>> nodeTypes= (List)params.get("NODE_TYPES");

        //插入到集群程序类型表
        Map<String,Object> curNodeType = null;
        String clusterTypeId = null;
        List<Map<String,Object>> nodes = null;
        for(int i=0;i<nodeTypes.size();++i){

                curNodeType = nodeTypes.get(i);
                curNodeType.put("NODE_CLUSTER_ID",nodeClusterId);
                coreService.insertObject2New("nodeClusterManager.insertNodeClusterType",curNodeType,dbKey);
                log.debug("ID为{}的集群的集群程序类型表的插入，当前插入的程序类型的ID：{}",nodeClusterId,curNodeType.get("NODE_TYPE_ID"));

                //插入到dcf_node_cluster_ele_config表
                clusterTypeId = StringTool.object2String(curNodeType.get("CLUSTER_TYPE_ID"));
                nodes = (List)curNodeType.get("NODE_IDS");
                Map<String,Object> curNode = null;
                for(int j=0;j<nodes.size();++j){
                    curNode = nodes.get(j);
                    curNode.put("CLUSTER_TYPE_ID",clusterTypeId);
                }
                coreService.insertObject2New("nodeClusterManager.insertNodeClusterEle",nodes,dbKey);
                log.debug("ID为{}的集群的关联表dcf_node_cluster_ele_config的插入，当前插入的CLUSTER_TYPE_ID的值：{}",nodeClusterId,clusterTypeId);
        }

        result.put("rstCode",BusinessConstant.PARAMS_BUS_1);

        log.debug("集群表、及其相关联表的的插入结束...");

        return result;
    }

    /**
     * 集群删除
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> deleteCluster(Map<String,Object> params,String dbKey) throws Exception{
        if(MapUtils.isEmpty(params)){
            throw new IllegalArgumentException("集群删除，传入参数为空");
        }
        Map<String,Object> result = new HashMap<>();
        log.debug("集群删除，集群删除开始");

        //删除dcf_node_cluster_ele_config表记录
        int rows=coreService.deleteObject2New("nodeClusterManager.deleteClusterNode",params,dbKey);

        //删除dcf_node_cluster_type_config表记录
        rows += coreService.deleteObject2New("nodeClusterManager.deleteClusterNodeType",params,dbKey);

        //删除dcf_node_cluster_config表记录
        rows += coreService.deleteObject2New("nodeClusterManager.deleteCluster",params,dbKey);
        log.debug("集群删除，集群删除开始结束");

        result.put("effectRows",rows);
        return result;
    }

    /**
     * 获取集群信息
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> loadClusterInfo(Map<String,Object> params,int pageSize,int pageIndex,String dbKey) throws Exception{

        log.debug("获得集群表数据开始");
        Map<String,Object> clusterPageTable = coreService.queryPageList2New("nodeClusterManager.queryClusterInfo",pageSize,pageIndex,params,dbKey);
        List<Map<String,Object>> clusterInfo = (List)clusterPageTable.get(FrameParamsDefKey.DATA);
        List<Map<String,Object>> nodeTypeInfo = coreService.queryForList3New("nodeClusterManager.queryClusterNodeTypeInfo",params,dbKey);

        String clusterId = null;
        String clusterId2 = null;
        StringBuffer nodeTypeInfoBuffer = null;
        String nodeTypeInfos = null;
        int index = -1;
        for(int i=0;i<clusterInfo.size();++i){
            clusterId = StringTool.object2String(clusterInfo.get(i).get("ID"));
            nodeTypeInfoBuffer = new StringBuffer();

            //获得该集群拥有的所有nodeType信息
            for(int j=0;j<nodeTypeInfo.size();++j){
                clusterId2 = StringTool.object2String(nodeTypeInfo.get(j).get("NODE_CLUSTER_ID"));

                if(clusterId2.equals(clusterId)){
                    nodeTypeInfoBuffer.append(StringTool.object2String(nodeTypeInfo.get(j).get("NODE_TYPE_INFO")));
                    nodeTypeInfoBuffer.append(",");
                }
            }
            nodeTypeInfos = nodeTypeInfoBuffer.toString();
            index = nodeTypeInfos.lastIndexOf(",");
            if(index!=-1){
                nodeTypeInfos = nodeTypeInfos.substring(0,index);
            }
            clusterInfo.get(i).put("CLUSTER_MEMBER",nodeTypeInfos);
        }
        log.debug("获得集群表数据结束");
        return clusterPageTable;
    }

    /**
     * 查询集群信息通过集群Id
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> findClusterById(Map<String,Object> params,String dbKey) throws Exception{
        Map<String,Object> result = new HashMap<>();

        //查询集群信息
        Map<String,Object> clusterInfo = coreService.queryForObject2New("nodeClusterManager.queryClusterById",params,dbKey);
        List<Map<String,Object>> nodeTypeInfo = coreService.queryForList3New("nodeClusterManager.queryNodeTypeByClusterId",params,dbKey);
        List<Map<String,Object>> nodeInfo = coreService.queryForList3New("nodeClusterManager.queryNodeByClusterId",params,dbKey);

        String clusterTypeId = null;
        String clusterTypeId2 = null;
        List<Map<String,Object>> nodes = null;
        for(int i=0;i<nodeTypeInfo.size();++i){
            clusterTypeId = StringTool.object2String(nodeTypeInfo.get(i).get("ID"));

            //为程序类型，添加其程序节点
            nodes = new ArrayList<>();
            for(int j=0;j<nodeInfo.size();++j){
                clusterTypeId2 = StringTool.object2String(nodeInfo.get(j).get("CLUSTER_TYPE_ID"));
                if(clusterTypeId.equals(clusterTypeId2)){

                    nodes.add(nodeInfo.get(j));
                }
            }
            nodeTypeInfo.get(i).put("nodes",nodes);
        }

        result.put("NODE_CLUSTER_NAME",clusterInfo.get("NODE_CLUSTER_NAME"));
        result.put("NODE_CLUSTER_CODE",clusterInfo.get("NODE_CLUSTER_CODE"));
        result.put("NODE_CLUSTER_DESC",clusterInfo.get("NODE_CLUSTER_DESC"));
        result.put("ID",clusterInfo.get("ID"));
        result.put("nodeTypes",nodeTypeInfo);
        return result;
    }

    /**
     * 集群修改
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    public Map<String,Object> updateCluster(Map<String,Object> params,String dbKey) throws Exception{
        log.debug("集群表、及其相关联表的的更新开始...");
        Map<String,Object> result = new HashMap<>();
        result.put("rstCode",BusinessConstant.PARAMS_BUS_0);

        //判断集群名称和集群编码是否为空
        String clusterCode = StringTool.object2String(params.get("NODE_CLUSTER_CODE"));
        String clusterName = StringTool.object2String(params.get("NODE_CLUSTER_NAME"));
        if(BlankUtil.isBlank(clusterCode) || BlankUtil.isBlank(clusterName)){
            result.put("rstMsg","集群名称和集群编码不能为空，请重新输入！");
            return result;
        }

        if(!clusterCode.matches("^[\\w-$]+$")){
            result.put("rstMsg","集群编码只能由字母、数字、-、$、下划线组成！");
            return result;
        }

        //判断集群编码是否重复
        Map<String,Object> clusterCountMap = coreService.queryForObject2New("nodeClusterManager.queryClusterExistsByCode",params,dbKey);
        if (!StringTool.object2String(clusterCountMap.get("CLUSTER_COUNT")).equals(BusinessConstant.PARAMS_BUS_0)) {
            result.put("rstMsg", "集群的编码已存在，请重新输入！");
            return result;
        }

        //更新集群表的数据
        coreService.updateObject2New("nodeClusterManager.updateCluster",params,dbKey);

        log.debug("集群表的更新完成...");

        //删除dcf_node_cluster_ele_config表记录
        coreService.deleteObject2New("nodeClusterManager.deleteNodeByClusterId",params,dbKey);

        //删除dcf_node_cluster_type_config表记录
        coreService.deleteObject2New("nodeClusterManager.deleteTypeByClusterId",params,dbKey);

        String clusterId = StringTool.object2String(params.get("ID"));

        List<Map<String,Object>> nodeTypes= (List)params.get("NODE_TYPES");

        //插入到集群程序类型表
        Map<String,Object> curNodeType = null;
        String clusterTypeId = null;
        List<Map<String,Object>> nodes = null;
        for(int i=0;i<nodeTypes.size();++i){

            curNodeType = nodeTypes.get(i);
            curNodeType.put("NODE_CLUSTER_ID",clusterId);
            coreService.insertObject2New("nodeClusterManager.insertNodeClusterType",curNodeType,dbKey);
            log.debug("ID为{}的集群的集群程序类型表的插入，当前插入的程序类型的ID：{}",clusterId,curNodeType.get("NODE_TYPE_ID"));

            //插入到dcf_node_cluster_ele_config表
            clusterTypeId = StringTool.object2String(curNodeType.get("CLUSTER_TYPE_ID"));
            nodes = (List)curNodeType.get("NODE_IDS");
            Map<String,Object> curNode = null;
            for(int j=0;j<nodes.size();++j){
                curNode = nodes.get(j);
                curNode.put("CLUSTER_TYPE_ID",clusterTypeId);
            }
            coreService.insertObject2New("nodeClusterManager.insertNodeClusterEle",nodes,dbKey);
            log.debug("ID为{}的集群的关联表dcf_node_cluster_ele_config的插入，当前插入的CLUSTER_TYPE_ID的值：{}",clusterId,clusterTypeId);
        }


        result.put("rstCode",BusinessConstant.PARAMS_BUS_1);

        log.debug("集群表、及其相关联表的的更新结束...");

        return result;
    }

}
