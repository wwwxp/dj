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
import com.tydic.service.configure.SwitchMasterStandbyService;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/switchMasterStandby")
public class SwitchMasterStandbyController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SwitchMasterStandbyController.class);
	
	@Autowired
	private SwitchMasterStandbyService switchMasterStandbyService;
	
	 /**
     * 值获取：网元
     * @param request
     * @return
     */
    @RequestMapping(value="/value/netElement",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadNetElement(HttpServletRequest request) {
    	log.debug("灰度升级， 获取网元信息开始...");
    	List<Map<String, Object>> netList = new ArrayList<Map<String, Object>>();
        try {
        	netList = switchMasterStandbyService.getNetElement(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级， 获取网元信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级， 获取网元信息结束...");
        return JSON.Encode(netList);
    }
    

	/**
    * 获取sp_switch.xml已有号段信息
    * @param request
    * @return
    */
   @RequestMapping(value="/info/existNum",produces = {"application/json;charset=UTF-8"})
   @ResponseBody
   public String loadExistNumInfo(HttpServletRequest request) {
   	log.debug("灰度升级， 获取已有号段信息开始...");
   	Map<String, Object> netMap = new HashMap<String, Object>();
       try {
    	   netMap = switchMasterStandbyService.getExistNumInfo(this.getParamsMapByObject(request), getDbKey(request));
       } catch (Exception e) {
    	   log.debug("灰度升级， 获取已有号段异常， 异常信息: ", e);
    	   return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
       }
       log.debug("灰度升级， 获取已有号段信息结束...");
       return JSON.Encode(netMap);
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
        	resultMap = switchMasterStandbyService.updateGreyUpgrade(params, getDbKey(request));
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
        	resultMap = switchMasterStandbyService.updateOfficialLaunch(params, getDbKey(request));
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
        	resultMap = switchMasterStandbyService.updateRollback(params, getDbKey(request));
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
        	resultMap=switchMasterStandbyService.updateGreyUpgradeConfig(params, getDbKey(request));
        } catch (Exception e) {
        	log.debug("灰度升级配置文件修改异常, 异常信息：", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("灰度升级配置文件修 改结束...");
        return JSON.Encode(resultMap);
    }
}
