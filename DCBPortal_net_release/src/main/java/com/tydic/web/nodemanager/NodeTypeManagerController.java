package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.nodemanager.NodeTypeManagerService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/nodeTypeManager")
public class NodeTypeManagerController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(NodeManagerController.class);

    @Autowired
    private NodeTypeManagerService nodeTypeManagerService;

    @RequestMapping(value = "/addNodeType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("程序类型配置Controller，增加程序类型开始...");
        try{
            String userName=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
            }

            Map<String,Object> result=nodeTypeManagerService.insertNodeType(userName,this.getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("程序类型配置Controller,增加程序类型失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/updateNodeType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateNodeType(HttpServletRequest request, HttpServletResponse response){
        log.debug("程序类型配置Controller，修改程序类型开始...");
        try{
            String userName=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
            }

            Map<String,Object> result=nodeTypeManagerService.updateNodeType(userName,this.getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("程序类型配置Controller,修改程序类型失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/delNodeType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delNodeType(HttpServletRequest request, HttpServletResponse response){
        log.debug("程序类型配置Controller，删除程序类型开始...");
        try{
            String userName=null;
            String empeeId=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
                empeeId = StringTool.object2String(((Map<String,String>)userMap).get("EMPEE_ID"));
            }

            Map<String,Object> result=nodeTypeManagerService.deleteNodeType(userName+"("+empeeId+")",this.getParamsList2New(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("程序类型配置Controller,删除程序类型失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/beingUsed",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String beingUsed(HttpServletRequest request, HttpServletResponse response){
        log.debug("程序类型配置Controller，删除程序类型开始...");
        try{

            Map<String,Object> result=nodeTypeManagerService.beingUsed(this.getParamsList2New(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("程序类型配置Controller,删除程序类型失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
