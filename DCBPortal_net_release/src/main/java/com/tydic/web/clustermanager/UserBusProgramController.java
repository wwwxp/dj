package com.tydic.web.clustermanager;

import PluSoft.Utils.JSON;
import com.tydic.bean.UserPrivilegeNode;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.common.BusException;
import com.tydic.service.clustermanager.UserBusProgramService;
import com.tydic.util.BusinessConstant;
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
 * Auther: Yuanh
 * Date: 2018-07-09 15:55
 * Description:
 */
@Controller
@RequestMapping(value = "/userBus")
public class UserBusProgramController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(UserBusProgramController.class);

    /**
     * 业务程序权限分配Service
     */
    @Autowired
    private UserBusProgramService userBusProgramService;

    /**
     * 查询业务程序列表
     * @param request
     * @return
     */
    @RequestMapping(value="/queryUserProgramList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryUserProgramList(HttpServletRequest request) {
        log.debug("查询业务程序列表开始...");
        List<UserPrivilegeNode> programList = new ArrayList<UserPrivilegeNode>();
        try {
            Map<String, Object> params = this.getParamsMapByObject(request);
            programList = userBusProgramService.queryUserProgramPrivilegeList(params, getDbKey(request));
        } catch (Exception e) {
            log.error("查询业务程序列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("查询业务程序列表结束...");
        return JSON.Encode(programList);
    }

    /**
     * 添加用户业务程序权限
     * @param request
     * @return
     */
    @RequestMapping(value = "/addUserBusProgramList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addUserBusProgramList(HttpServletRequest request) {
        log.debug("添加业务程序列表开始...");
        try {
            userBusProgramService.addUserBusProgramList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("添加业务程序列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("添加业务程序列表结束...");
        return null;
    }

    /**
     * 查询业务程序启停列表
     * @param request
     * @return
     */
    @RequestMapping(value="/queryUserProgramStartStopList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryUserProgramStartStopList(HttpServletRequest request) {
        log.debug("查询业务程序启停列表开始...");
        Map<String, Object> retMap = null;
        try {
            retMap = userBusProgramService.queryUserProgramStartStopList(this.getParamsMapByObject(request), getDbKey(request), request);
        } catch (Exception e) {
            log.error("查询业务程序启停列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("查询业务程序启停列表结束...");
        return JSON.Encode(retMap);
    }

    /**
     * 业务程序启动
     * @param request
     * @return
     */
    @RequestMapping(value="/addRunProgram", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addRunProgram(HttpServletRequest request) {
        log.debug("业务程序启动开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Map<String, Object> param = this.getParamsMapByObject(request);
            param.put("flag", BusinessConstant.PARAMS_START_FLAG);
            param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
            Map empeeMap = (Map) request.getSession().getAttribute("userMap");
            param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
            resultMap = userBusProgramService.addRunStopProgram(param, getDbKey(request));
        } catch (BusException e) {
            log.error("程序停止异常， 异常原因: ", e);
            resultMap.put("info", e.getErrorMsg());
            resultMap.put("flag", e.getErrorCode());
            resultMap.put("reason", e.getErrorReason());
        } catch (Exception e) {
            log.error("程序启动异常， 异常原因: ", e);
            resultMap.put("info", "启动失败");
            resultMap.put("flag", "error");
            resultMap.put("reason", e.getMessage());
        }
        log.debug("业务程序启动结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 业务程序停止
     * @param request
     * @return
     */
    @RequestMapping(value="/addStopProgram", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addStopProgram(HttpServletRequest request) {
        log.debug("业务程序停止开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Map<String, Object> param = this.getParamsMapByObject(request);
            param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
            param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
            Map empeeMap = (Map) request.getSession().getAttribute("userMap");
            param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
            resultMap = userBusProgramService.addRunStopProgram(param, getDbKey(request));
        } catch (BusException e) {
            log.error("程序停止异常， 异常原因: ", e);
            resultMap.put("info", e.getErrorMsg());
            resultMap.put("flag", e.getErrorCode());
            resultMap.put("reason", e.getErrorReason());
        } catch (Exception e) {
            log.error("程序停止异常， 异常原因: ", e);
            resultMap.put("info", "停止失败");
            resultMap.put("flag", "error");
            resultMap.put("reason", e.getMessage());
        }
        log.debug("业务程序停止结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 业务程序批量状态检查
     * @param request
     * @return
     */
    @RequestMapping(value="/checkRunStopProgram",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String checkRunStopProgram(HttpServletRequest request) {
        log.debug("业务程序状态检查开始...");
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            List<Map<String, Object>> paramList = this.getParamsObjList(request);
            result = userBusProgramService.checkRunStopProgram(paramList, getDbKey(request));
        } catch (Exception e) {
            log.error("业务程序状态检查异常, 异常原因:",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("业务程序状态检查结束...");
        return JSON.Encode(result);
    }

    /**
     * 获取参数Object列表对象
     * @param request
     * @return
     */
    public List<Map<String, Object>> getParamsObjList(HttpServletRequest request) {
        List<Map<String, Object>> paramsList = new ArrayList<Map<String, Object>>();
        String paramsStr = request.getParameter(FrameParamsDefKey.PARAMS);
        log.debug("参数，转换前数据 ---> " + paramsStr);
        if(paramsStr != null && !paramsStr.equals("")) {
            if(paramsStr.startsWith("[") && paramsStr.endsWith("]")) {
                paramsList = (List) com.alibaba.fastjson.JSON.parse(paramsStr);
                log.debug("参数，参数为 ---> " + paramsList);
                return paramsList;
            } else {
                log.debug("参数：参数不是有效的Json格式，不转换");
                return paramsList;
            }
        } else {
            log.debug("参数，参数为空，不转换");
            return paramsList;
        }
    }
}
