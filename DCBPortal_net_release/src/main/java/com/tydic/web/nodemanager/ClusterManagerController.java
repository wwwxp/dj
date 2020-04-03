package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.ClusterManagerService;
import com.tydic.service.versiondeployment.bean.PubCfg2Enty;
import com.tydic.service.versiondeployment.bean.json.ClusterViewEnty;
import com.tydic.service.versiondeployment.service.ClusterViewService;
import com.tydic.util.StringTool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/nodeClusterManager")
public class ClusterManagerController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(ClusterManagerController.class);

    @Resource
    CoreService coreService;

    @Autowired
    ClusterViewService clusterViewService;

    @Autowired
    ClusterManagerService clusterManagerService;

    /**
     * 集群表格的加载
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/loadClusterInfo", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadClusterInfo(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群查询Controller，集群查询开始...");
        try {

            Map<String,Object> result=clusterManagerService.loadClusterInfo(getParamsMapByObject(request),getPageSize(request), getPageIndex(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群查询Controller，集群查询失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 根据集群Id获得集群信息
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/findClusterById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String findClusterById(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群查询Controller，集群查询开始...");
        try {

            Map<String,Object> result=clusterManagerService.findClusterById(getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群查询Controller，集群查询失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 集群的新增
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/addCluster", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addCluster(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群新增Controller，集群新增开始...");
        try {
            String userName=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
            }
            Map<String,Object> result=clusterManagerService.insertCluster(getParamsMapByObject(request),userName,FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群新增Controller，集群新增失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 集群的修改
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/updateCluster", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateCluster(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群修改Controller，集群修改开始...");
        try {

            Map<String,Object> result=clusterManagerService.updateCluster(getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群修改Controller，集群修改失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 集群的删除
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/deleteCluster", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String deleteCluster(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群删除Controller，集群删除开始...");
        try {

            Map<String,Object> result=clusterManagerService.deleteCluster(getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群删除Controller，集群删除失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    @RequestMapping(value = "/queryNodeInfo", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addNode(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询节点信息开始...");
        try {
            Map<String, String> param = getParamsMap(request);
            String nodeTypeId = param.get("NODE_TYPE_ID");
            if (BlankUtil.isBlank(nodeTypeId)) {
                throw new RuntimeException("参数不能为空");
            }
            List retList = coreService.queryForList("versionOptService.queryClusterNodeCfg", param, null);
            return JSON.toJSONString(retList);
        } catch (Exception e) {
            log.error("查询节点信息失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    /**
     * 集群配置表的新增
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getNodeType", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getNodeType(HttpServletRequest request, HttpServletResponse response) {
        log.debug("集群新增Controller，查询节点信息开始...");
        try {
            List<Map<String,Object>> result=coreService.queryForList3New("clusterAddMapper.queryDeployedNodeType",Collections.emptyMap(),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("集群新增Controller，查询节点信息失败，失败原因:" ,e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryNodeClusterConfig", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryClusterList(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询集群视图...");
        try {
            Map<String, Object> param = getParamsMapByObject(request);
            List<Map<String, Object>> list = coreService.queryForList3New("nodeClusterManager.queryNodeClusterConfig", param, null);
            return JSON.toJSONString(list);
        } catch (Exception e) {
            log.error("查询集群视图异常，失败原因:" ,e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    @RequestMapping(value = "/queryClusterView", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryClusterView(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询集群视图...");
        try {
            Map<String, String> param = getParamsMap(request);
            String cluster_id = param.get("CLUSTER_ID");
            ClusterViewEnty clusterViewEnty = clusterViewService.getClusterView(cluster_id, null);
            return JSON.toJSONString(clusterViewEnty);
        } catch (Exception e) {
            log.error("查询集群视图异常，失败原因:" ,e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    @RequestMapping(value = "/queryNodesByNodeType", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodesByNodeType(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询节点信息开始...");
        try {
            Map<String, Object> param = getParamsMapByObject(request);
            String nodeTypeId = StringTool.object2String(param.get("NODE_TYPE_ID"));
            if (BlankUtil.isBlank(nodeTypeId)) {
                throw new RuntimeException("参数不能为空");
            }
            List<Map<String,Object>> result=coreService.queryForList3New("nodeClusterManager.queryNodesByNodeType",getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);

            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("查询节点信息失败，失败原因:" ,e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/refreshClusterState", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String refreshClusterState(HttpServletRequest request, HttpServletResponse response) {
        log.debug("刷新集群状态...");
        try {
            Map<String, String> param = getParamsMap(request);
            String cluster_id = StringTool.object2String(param.get("CLUSTER_ID"));
            List<Map<String, Object>> resultList = clusterViewService.refreshClusterState(cluster_id, null);
            return JSON.toJSONString(resultList);
        } catch (Exception e) {
            log.error("刷新集群状态，失败原因:" ,e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
