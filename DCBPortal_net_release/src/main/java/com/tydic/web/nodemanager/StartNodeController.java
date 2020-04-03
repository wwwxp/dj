package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodemanager.StartNodeService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/startNode")
public class StartNodeController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(StartNodeController.class);

    @Autowired
    private StartNodeService startNodeService;

    @Autowired
    private CoreService coreService;

    @RequestMapping(value = "/startNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String startNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，启动程序开始...");
        try{
            String userName=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
            }

            Map<String,Object> result=startNodeService.startNode(userName,this.getParamsList2New(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,启动程序失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/stopNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String stopNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，停止程序开始...");
        try{
            String userName=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
            }

            Map<String,Object> result=startNodeService.stopNode(userName,this.getParamsList2New(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,停止程序失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/checkNode",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String checkNode(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，停止程序开始...");
        try{

            List<Map<String,Object>> result=startNodeService.checkNode(this.getParamsList2New(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,停止程序失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    
    @RequestMapping(value = "/loadFileTree",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadFileTree(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，停止程序开始...");
        try{
            List<Map<String,Object>> result=startNodeService.loadFileTree(this.getParamsMapByObject(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,停止程序失败，失败原因:",e);
            e.printStackTrace();
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/getFileContent",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getFileContent(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，获得文件内容开始...");
        try{
            Map<String,Object> result=startNodeService.getFileContent(this.getParamsMapByObject(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,获得文件内容失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/delNodeVersion",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delNodeVersion(HttpServletRequest request, HttpServletResponse response){
        log.debug("启停程序Controller，节点删除开始...");
        try{
            String userName=null;
            String empeeId=null;
            Object userMap=request.getSession().getAttribute("userMap");
            if(userMap!=null){
                userName=((Map<String,String>)userMap).get("EMPEE_NAME");
                empeeId = StringTool.object2String(((Map<String,String>)userMap).get("EMPEE_ID"));
            }


            Map<String,Object> result=startNodeService.deleteNodeVersion(userName+"("+empeeId+")",this.getParamsList2New(request));
            return JSON.toJSONString(result);
        }catch(Exception e){
            log.error("启停程序Controller,节点删除失败，失败原因:",e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
