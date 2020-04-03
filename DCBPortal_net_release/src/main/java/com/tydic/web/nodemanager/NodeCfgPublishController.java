package com.tydic.web.nodemanager;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.versiondeployment.bean.PubCfg2Enty;
import com.tydic.service.versiondeployment.dao.VersionOptDao;
import com.tydic.service.versiondeployment.service.NodeCfgPublishService;
import com.tydic.service.versiondeployment.service.impl.NodeCfgPublishServiceImpl;
import com.tydic.service.versiondeployment.util.NodeVerUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/nodeCfgPub")
public class NodeCfgPublishController extends BaseController {

    private static Logger log = Logger.getLogger(NodeCfgPublishServiceImpl.class);

    @Autowired
    VersionOptDao versionOptDao;

    @Autowired
    NodeCfgPublishService nodeCfgPublishService;

    @RequestMapping(value = "/synConfig", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String synConfig(HttpServletRequest request, HttpServletResponse response) {
        log.debug("配置发布，同步配置文件开始...");
        try {
            Map<String, String> result = nodeCfgPublishService.synConfig(getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);

            return JSON.toJSONString(result);
        } catch (Exception e) {
            log.error("配置发布，同步配置文件，失败原因:", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, "查询异常"));
        }
    }

    @RequestMapping(value = "/queryDeployVersion", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryDeployVersion(HttpServletRequest request, HttpServletResponse response) {
        log.debug("配置发布，查询配置项，单个增加节点信息开始...");
        try {
            Map<String, String> paramMap = getParamsMap(request);
            String nodeTypeId = paramMap.get("NODE_TYPE_ID");

            List<HashMap<String, String>> list = nodeCfgPublishService.queryDeployVersion(nodeTypeId,null);



            return JSON.toJSONString(list);
        } catch (Exception e) {
            log.error("配置发布，查询配置项，失败原因:", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, "查询异常"));
        }
    }

    @RequestMapping(value = "/queryNodeDeployInfo", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodeDeployInfo(HttpServletRequest request, HttpServletResponse response) {
        log.debug("配置发布，查询配置项，单个增加节点信息开始...");
        try {
            Map<String, String> paramMap = getParamsMap(request);
            String nodeTypeId = paramMap.get("NODE_TYPE_ID");
            String version = paramMap.get("VERSION");
            List<HashMap<String, Object>> list = nodeCfgPublishService.queryNodeDeployInfo(nodeTypeId,version, null);
            return JSON.toJSONString(list);
        } catch (Exception e) {
            log.error("配置发布，查询配置项，失败原因:", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, "查询异常"));
        }
    }

    @RequestMapping(value = "/queryNodeDeployCfgFileDir", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodeDeployCfgFileDir(HttpServletRequest request, HttpServletResponse response) {
        log.debug("配置发布，查询配置文件路径，单个增加节点信息开始...");
        try {
            Map<String, String> paramMap = getParamsMap(request);
            String nodeTypeId = paramMap.get("NODE_TYPE_ID");
            String nodeId = paramMap.get("NODE_ID");
            String version = paramMap.get("VERSION");
            List<PubCfg2Enty> pubCfg2EntyList = nodeCfgPublishService.queryNodeDeployCfgFileDir(nodeTypeId, nodeId, version, null);
            return JSON.toJSONString(pubCfg2EntyList);
        } catch (Exception e) {
            log.error("配置发布，查询配置文件路径，失败原因:", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, "查询异常"));
        }
    }

    @RequestMapping(value = "/updateCfgAndPublish", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateCfgAndPublish(HttpServletRequest request, HttpServletResponse response) {
        log.debug("节点配置Controller，单个增加节点信息开始...");
        try {
            Map<String, Object> param = getParamsMapByObject(request);
            String entyJsonStr = StringTool.object2String(param.get("nodeparam"));
            String fileContent = StringTool.object2String(param.get("fileContent"));
            Boolean isPublishAll;
            PubCfg2Enty pubCfg2Enty = null;
            try {
                pubCfg2Enty = JSON.parseObject(entyJsonStr, PubCfg2Enty.class);
                if (pubCfg2Enty == null) {
                    throw new RuntimeException("节点参数序列化异常");
                }
                try{
                    isPublishAll = (Boolean) param.get("isPublishAll");
                }catch (Exception e){
                    throw new RuntimeException("参数异常");
                }
            } catch (Exception e) {
                throw e;
            }
            String retStr = nodeCfgPublishService.updateCfgAndPublish(pubCfg2Enty,isPublishAll, fileContent, null);
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
            fileContent = nodeCfgPublishService.getFileContent(param, this.getDbKey(request));
        } catch (Exception e) {
            log.error("获取部署主机配置文件内容异常， 异常信息: ", e);
            return PluSoft.Utils.JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("获取部署主机配置文件内容结束...");
        return PluSoft.Utils.JSON.Encode(fileContent);
    }

    /**
     * 业务类型（程序）
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryNodeTypeVersionList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String uploadVersionPkg(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询程序类型版本（程序）");
        try {
            List<HashMap<String, String>> list = versionOptDao.queryNodeTypeVersionListTbl(getParamsMap(request),null);
            NodeVerUtil.sortDeployVersion2HashMap(list);
            return PluSoft.Utils.JSON.Encode(list);
        } catch (Exception e) {
            log.error("查询程序类型版本（程序） ， 失败原因: ", e);
            return PluSoft.Utils.JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

}
