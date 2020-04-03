package com.tydic.web.nodemanager;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.versiondeployment.service.VersionOptService;
import com.tydic.util.ftp.FileRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/nodeopt")
public class NodeOptController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(NodeOptController.class);

    @Autowired
    VersionOptService versionOptService;

    @Autowired
    CoreService coreService;

    /**
     * 查询远程目录的目录树
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getRemoteFileTree", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getRemoteFileTree(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询业务类型（程序） 配置");
        try {
            Map<String,Object> result = versionOptService.getRemoteFileTree(getParamsMapByObject(request),FrameConfigKey.DEFAULT_DATASOURCE);
            return JSON.Encode(result);
        } catch (Exception e) {
            log.error("查询业务类型（程序） 配置异常， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 查询业务类型（程序） 配置
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryNoteTypeConfig", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryClusterTreeList(HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询业务类型（程序） 配置");
        try {
            List<HashMap<String, String>> mapList = versionOptService.queryNoteTypeConfig(getParameterMap(request), null);
            return JSON.Encode(mapList);
        } catch (Exception e) {
            log.error("查询业务类型（程序） 配置异常， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 查询版本信息列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/queryNodeTypeVersionDetail"}, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryNodeTypeVersionDetail(@PathVariable(value = "type", required = false) String type, HttpServletRequest request, HttpServletResponse response) {
        log.debug("查询版本信息列表");
        try {
            String retStr = null;
            String dbKey = null;
            String execKey = "versionOptService.queryNodeTypeVersionDetail";
            Map<String, Object> retMap = coreService.queryPageList2New(execKey, getPageSize(request), getPageIndex(request), getParamsMapByObject(request), dbKey);
            retStr = JSON.Encode(retMap);
            return retStr;
        } catch (Exception e) {
            log.error("查询版本信息列表异常， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


    /**
     * 业务类型（程序） 上传版本包
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/uploadVersionPkg", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String uploadVersionPkg(@RequestParam MultipartFile uFile, HttpServletRequest request, HttpServletResponse response) {
        log.debug("上传版本包（程序）");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String retString = versionOptService.updateVersionPkg(uFile, request, response, getParameterMap(request));
            out.println(JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, retString)));
            return null;
        } catch (Exception e) {
            log.error("上传版本包（程序） ， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }







    /**
     * 业务类型（程序） 上传版本包
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateVersionPatchPkg", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateVersionPatchPkg(@RequestParam MultipartFile uFile, HttpServletRequest request, HttpServletResponse response) {
        log.debug("上传版本补丁包（程序）");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String retString = versionOptService.updateVersionPatchPkg(uFile, request, response, getParameterMap(request));
            out.println(JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, retString)));
            return null;
        } catch (Exception e) {
            log.error("上传版本补丁包（程序） ， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


    /**
     * 删除版本
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteVersion", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String deleteVersion(HttpServletRequest request, HttpServletResponse response) {
        log.debug("删除版本");
        try {
            Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
            String empeeId = StringTool.object2String(userMap.get("EMPEE_ID"));
            String userName = StringTool.object2String(userMap.get("EMPEE_NAME"));
            String createdUser = userName + "(" + empeeId + ")";

            Map<String, String> paramMap = getParamsMap(request);
            paramMap.put("OPT_USER", createdUser);
            StringBuilder retBlder = new StringBuilder();
            String ret = "";
            try {
                ret = versionOptService.deleteVersion(paramMap, null);
                retBlder.append(ret).append("\n").append("操作成功");
            } catch (Exception e) {
                log.error("删除版本包,操作异常", e);
                retBlder.append(ret).append("\n").append("操作异常：" + e.getMessage());
            }

            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, ret));
        } catch (RuntimeException e) {
            log.error("删除版本错误，失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        } catch (Exception e) {
            log.error("删除版本异常，失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


}
