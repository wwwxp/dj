package com.tydic.web.loglevelcfg;

 

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.loglevelcfg.LogLevelCfgService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.loglevelcfg]    
  * @ClassName:    [LogLevelCfgController]     
  * @Description:  [日志级别调整]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-11-25 上午10:30:39]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-11-25 上午10:30:39]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping(value = "/logLevelCfg")
public class LogLevelCfgController  extends BaseController {
	/**
	 * 日志级别调整日志对象
	 */
	private static Logger log = LoggerFactory.getLogger(LogLevelCfgController.class);
	
	/**
	 * 日志级别Service对象
	 */
	@Autowired
	private LogLevelCfgService logLevelCfgService;
	
    @RequestMapping(value="update",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String update(HttpServletRequest request) {
    	log.debug("LogLevelCfgController,运行开始");
        Map resultMap = new HashMap();
        try {
        	
        	Map<String, Object> param = this.getParamsMapByObject(request);
        	  logLevelCfgService.update(param, getDbKey(request));
        } catch (Exception e) {
            log.error("LogLevelCfgController, 失败 ---> ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }
    
    @RequestMapping(value="sendMsg",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String sendMsg(HttpServletRequest request) {
    	log.debug("LogLevelCfgController,运行开始");
        Map resultMap = new HashMap();
        try {
        	
        	Map<String, Object> param = this.getParamsMapByObject(request);
        	 logLevelCfgService.sendMsg(param);
        } catch (Exception e) {
            log.error("LogLevelCfgController, 失败 ---> ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }
	 
	 
	/**
	 * 添加日志级别信息
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/addLogLevel",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addLogLevel(HttpServletRequest request) {
        log.debug("添加日志级别配置开始...");
        try {
        	logLevelCfgService.addLogLevel(this.getParamsMapByObject(request), getDbKey(request), request);
        } catch (Exception e) {
            log.error("添加日志级别配置异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("添加日志级别配置结束...");
        return null;
    }

    /**
     * 修改日志级别信息
     * @param request
     * @return
     */
    @RequestMapping(value="/updateLogLevel",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateLogLevel(HttpServletRequest request) {
        log.debug("修改日志级别信息开始...");
        try {
        	logLevelCfgService.updateLogLevel(this.getParamsMapByObject(request), getDbKey(request), request);
        } catch (Exception e) {
            log.error("修改日志级别信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改日志级别信息结束...");
        return null;
    }

	/**
	 * 删除日志级别信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delLogLevel",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delLogLevel(HttpServletRequest request) {
		log.debug("删除日志级别信息开始...");
		String result = null;
		try {
			logLevelCfgService.delLogLevel(this.getParamsMapByObject(request), getDbKey(request));
		} catch (Exception e) {
			log.error("删除日志级别信息异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("删除日志级别信息结束...");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}
	 
}
