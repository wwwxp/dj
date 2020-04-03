package com.tydic.web.configure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.SwitchMasterStandbyAbmService;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/switchMasterStandbyAbm")
public class SwitchMasterStandbyAbmController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SwitchMasterStandbyAbmController.class);
	
	@Autowired
	private SwitchMasterStandbyAbmService switchMasterStandbyAbmService;
	
	 /**
     * 值获取：本地网
     * @param request
     * @return
     */
    @RequestMapping(value="/value/latnElement",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadLatnElement(HttpServletRequest request) {
    	log.debug("灰度升级， 获取网元信息开始...");
    	List<HashMap<String, Object>> latnList = new ArrayList<HashMap<String, Object>>();
        try {
        	latnList = switchMasterStandbyAbmService.getLatnElement(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级， 获取网元信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级， 获取网元信息结束...");
        return JSON.Encode(latnList);
    }
    
    /**
     * 程序操作：灰度升级
     * @param request
     * @return
     */
    @RequestMapping(value="/opt/upgrade",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateGreyUpgrade(HttpServletRequest request) {
    	log.debug("灰度升级开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String, Object> params = this.getParamsMapByObject(request);
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	resultMap = switchMasterStandbyAbmService.updateGreyUpgrade(params, getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级结束...");
        return JSON.Encode(resultMap);
    }
    
    /**
     * 程序操作：正式发布
     * @param request
     * @return
     */
    @RequestMapping(value="/opt/launch",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateOfficialLaunch(HttpServletRequest request) {
    	log.debug("灰度升级， 正式发布开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String, Object> params = this.getParamsMapByObject(request);
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	resultMap = switchMasterStandbyAbmService.updateOfficialLaunch(params, getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级， 正式发布异常，异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级，正式发布结束...");
        return JSON.Encode(resultMap);
    }
    
    /**
     * 程序操作：回退
     * @param request
     * @return
     */
    @RequestMapping(value="/opt/rollback",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateRollback(HttpServletRequest request) {
    	log.debug("灰度升级， 回退开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String, Object> params = this.getParamsMapByObject(request);
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	resultMap = switchMasterStandbyAbmService.updateRollback(params, getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级， 回退异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级， 回退结束...");
        return JSON.Encode(resultMap);
    }
    
    /**
     * 程序操作：灰度升级后配置文件修改
     * @param request
     * @return
     */
    @RequestMapping(value="/opt/upgradeConfig",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateGreyUpgradeConfig(HttpServletRequest request) {
    	log.debug("灰度升级配置文件修改开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> params = this.getParamsMapByObject(request);
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	resultMap=switchMasterStandbyAbmService.updateGreyUpgradeConfig(params, getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级配置文件修改异常, 异常信息：", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级配置文件修 改结束...");
        return JSON.Encode(resultMap);
    }
}
