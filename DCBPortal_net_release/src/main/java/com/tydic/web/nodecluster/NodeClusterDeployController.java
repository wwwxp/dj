package com.tydic.web.nodecluster;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodecluster.NodeClusterDeployService;
import com.tydic.util.NodeConstant;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
  * Simple to Introduction
  * @ProjectName:  [DCBPortal_v1.0]
  * @Package:      [com.tydic.web.configure]
  * @ClassName:    [SwitchMasterStandbyNetController]
  * @Description:  [节点集群部署Controller]
  * @Author:       [Yuanh]
  * @CreateDate:   [2018-3-21 下午2:12:13]
  * @UpdateUser:   [Yuanh]
  * @UpdateDate:   [2018-3-21 下午2:12:13]
  * @UpdateRemark: [说明本次修改内容]
  * @Version:      [v1.0]
  *
 */
@Controller
@RequestMapping("/nodeClusterDeploy")
public class NodeClusterDeployController extends BaseController {

	private static Logger log = Logger.getLogger(NodeClusterDeployController.class);
	
	@Autowired
	private NodeClusterDeployService nodeClusterDeployService;

    @Autowired
    CoreService coreService;

    /**
     * 根据程序类型，查询已部署的节点、版本
     * @param request
     * @return
     */
    @RequestMapping(value="/queryDeployedNodeByNodeType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryDeployedNodeByNodeType(HttpServletRequest request) {
        log.debug("节点集群部署，查询程序版本列表&主机节点信息开始...");
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            result = coreService.queryForList3New("nodeClusterDeployMapper.queryDeployedNodeAndVersion", getParamsMapByObject(request), FrameConfigKey.DEFAULT_DATASOURCE);
        } catch (Exception e) {
            log.debug("节点集群部署，查询程序版本列表&主机节点信息异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("节点集群部署，查询程序版本列表&主机节点信息结束...");
        return JSON.Encode(result);
    }

    /**
     * 节点部署，查询程序列表
     * @param request
     * @return
     */
    @RequestMapping(value="/queryNodeClusterProgramList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodeClusterProgramList(HttpServletRequest request) {
    	log.debug("节点集群部署，查询程序列表开始...");
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = nodeClusterDeployService.queryNodeClusterProgramList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("节点集群部署，查询程序列表异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("节点集群部署，查询程序列表结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 节点部署，查询程序对应版本信息&部署节点信息
     * @param request
     * @return
     */
    @RequestMapping(value="/queryNodeClusterVersionHostList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodeClusterVersionHostList(HttpServletRequest request) {
        log.debug("节点集群部署，查询程序版本列表&主机节点信息开始...");
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap = nodeClusterDeployService.queryNodeClusterVersionHostList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.debug("节点集群部署，查询程序版本列表&主机节点信息异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("节点集群部署，查询程序版本列表&主机节点信息结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 节点部署开始
     * @param request
     * @return
     */
    @RequestMapping(value="/startNodeDeploy",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String startNodeDeploy(HttpServletRequest request) {
        log.debug("节点集群部署，节点部署开始...");
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map params = this.getParamsMapByObject(request);
            Map<String,Object> runWeb=coreService.queryForObject2New("nodeClusterDeployMapper.queryRunWeb",params,FrameConfigKey.DEFAULT_DATASOURCE);
            boolean isRunWeb=NodeConstant.RUN_WEB.equals(StringTool.object2String(runWeb.get("RUN_WEB")));

            //判断是否为web类型程序
            if(isRunWeb){
                resultMap = nodeClusterDeployService.startWebNodeDeploy(params, FrameConfigKey.DEFAULT_DATASOURCE);
            }else {
                resultMap = nodeClusterDeployService.startNodeDeploy(params,FrameConfigKey.DEFAULT_DATASOURCE);
            }
        } catch (Exception e) {
            log.debug("节点集群部署，节点部署异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("节点集群部署，节点部署结束...");
        return JSON.Encode(resultMap);
    }



}
