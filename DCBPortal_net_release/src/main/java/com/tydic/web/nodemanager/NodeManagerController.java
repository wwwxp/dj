package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.nodemanager.NodeManagerService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/nodeManager")
public class NodeManagerController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(NodeManagerController.class);

    @Autowired
    private NodeManagerService nodeManagerService;

    @RequestMapping(value = "/addNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("节点配置Controller，单个增加节点信息开始...");
        try{
            Map<String,Object> result=nodeManagerService.insertNode(this.getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("节点配置Controller,单个增加节点信息失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/batchAddNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String batchAddNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("节点配置Controller，批量增加节点信息开始...");
        try{
            Map<String,Object> result=nodeManagerService.insertBatchNode(this.getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("节点配置Controller,批量增加节点信息失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/updateNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("节点配置Controller，修改节点开始");

        try{
            Map<String,Object> result=nodeManagerService.updateNode(this.getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);

        }catch(Exception e){
            log.error("节点配置Controller，修改节点失败，失败原因：",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

    }

    @RequestMapping(value = "/deployRunState",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String haveRun(HttpServletRequest request, HttpServletResponse response){
        log.debug("节点配置Controller，判断节点部署运行状态开始");

        try{

            Map<String,Object> result=nodeManagerService.deployRunState(this.getParamsList2New(request),FrameConfigKey.DEFAULT_DATASOURCE);

            return JSON.toJSONString(result);

        }catch(Exception e){
            log.error("节点配置Controller，判断节点部署运行状态失败，失败原因：",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

    }

    @RequestMapping(value = "/delNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("节点配置Controller，删除节点开始");

        try{
            String userName=null;
            String empeeId=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
                empeeId = StringTool.object2String(((Map<String,String>)userMap).get("EMPEE_ID"));
            }

            Map<String,Object> result=nodeManagerService.deleteNode(userName+"("+empeeId+")",this.getParamsList2New(request),FrameConfigKey.DEFAULT_DATASOURCE);

            return JSON.toJSONString(result);

        }catch(Exception e){
            log.error("节点配置Controller，删除节点失败，失败原因：",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

    }

}
