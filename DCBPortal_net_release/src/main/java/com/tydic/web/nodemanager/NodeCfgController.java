package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.versiondeployment.bean.PubCfg2Enty;
import com.tydic.service.versiondeployment.service.NodeCfgService;
import com.tydic.util.SessionUtil;
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
@RequestMapping("/nodecfg")
public class NodeCfgController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(NodeCfgController.class);

    @Autowired
    private NodeCfgService nodeCfgService;

    @RequestMapping(value = "/query", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addNode(HttpServletRequest request, HttpServletResponse response) {
        log.debug("节点配置Controller，单个增加节点信息开始...");
        try {
            Map<String, String> param = getParamsMap(request);
            String nodeTypeId = param.get("nodeTypeId");
            String version = param.get("version");
            List<PubCfg2Enty> pubCfgEntyList = nodeCfgService.queryCfgInfo(nodeTypeId, version, null);
            return JSON.toJSONString(pubCfgEntyList);
        } catch (Exception e) {
            log.error("节点配置Controller,单个增加节点信息失败，失败原因:" + e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    @RequestMapping(value = "/updateCfgAndPublish", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateCfgAndPublish(HttpServletRequest request, HttpServletResponse response) {
        log.debug("节点配置Controller，单个增加节点信息开始...");
        try {
            Map<String, String> param = getParamsMap(request);
            String entyJsonStr = param.get("nodeparam");
            String fileContent = param.get("fileContent");
            PubCfg2Enty pubCfg2Enty = null;
            try {
                pubCfg2Enty = JSON.parseObject(entyJsonStr, PubCfg2Enty.class);
                if (pubCfg2Enty == null) {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw new RuntimeException("参数错误");
            }
            String retStr = nodeCfgService.updateCfgAndPublish(pubCfg2Enty, fileContent, null);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.SUCCESS, retStr));
        } catch (Exception e) {
            log.error("节点配置Controller,单个增加节点信息失败，失败原因:", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 获取部署主机配置文件内容
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getFileContent", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getFileContent(HttpServletRequest request) {
        log.debug("获取部署主机配置文件内容开始...");
        Map<String, String> fileContent;
        try {
            Map<String, String> param = getParamsMap(request);
            param.put("webRootPath", SessionUtil.getWebRootPath(request));
            fileContent = nodeCfgService.getFileContent(param, this.getDbKey(request));
        } catch (Exception e) {
            log.error("获取部署主机配置文件内容异常， 异常信息: ", e);
            return PluSoft.Utils.JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("获取部署主机配置文件内容结束...");
        return PluSoft.Utils.JSON.Encode(fileContent);
    }

}
