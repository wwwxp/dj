package com.tydic.web.configure;

import java.util.HashMap;
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
import com.tydic.service.configure.RentService;
import com.tydic.util.BusinessConstant;


@Controller
@RequestMapping("/rent")
public class RentController extends BaseController {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(RentController.class);
	
	@Autowired
	private RentService rentService;
	
    /**
     * 运行task
     * @param request
     * @return
     */
    @RequestMapping(value="/runTask",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateRunTask(HttpServletRequest request) {
    	log.debug("租费程序启动开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = new HashMap<String,Object>();
        	param.put("flag", BusinessConstant.PARAMS_START_FLAG);
        	param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
        	param.put("queryParam", this.getParamsMapByObject(request));
        	resultMap = rentService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("租费程序启动失败， 失败原因: ", e);
            resultMap.put("info","启动失败");
            resultMap.put("reason",e.getMessage());
        }
        log.debug("租费程序启动结束...");
        return JSON.Encode(resultMap);
    }
    
    /**
     * 停止运行Task
     * @param request
     * @return
     */
    @RequestMapping(value="/stopTask",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateStopTask(HttpServletRequest request) {
        log.debug("租费程序停止开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = new HashMap<String,Object>();
        	param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
        	param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
        	param.put("queryParam", this.getParamsMapByObject(request));
        	resultMap = rentService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("租费程序停止失败， 失败原因:", e);
            resultMap.put("info","停止失败");
            resultMap.put("reason",e.getMessage());
        }
        log.debug("租费程序停止结束...");
        return JSON.Encode(resultMap);
    }
    
    
    /**
     * 检查program
     * @param request
     * @return
     */
    @RequestMapping(value="/checkProgram",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateCheckProgram(HttpServletRequest request) {
    	log.debug("租费程序状态检查开始...");
    	Map<String, String> resultMap = new HashMap<String, String>();
        try {
        	Map<String,Object> param = new HashMap<String,Object>();
        	param.put("flag", BusinessConstant.PARAMS_CHECK_FLAG);
        	param.put("queryParam", this.getParamsMapByObject(request));
        	resultMap = rentService.updateCheckProgramState(param, getDbKey(request));
        } catch (Exception e) {
            log.error("租费程序状态检查失败， 失败原因:", e);
            resultMap.put("reason",e.getMessage());
        }
        log.debug("租费程序状态检查结束...");
        return JSON.Encode(resultMap);
    }

    /**
     * 查看定义
     * @param request
     * @return
     */
    @RequestMapping(value="/scanConfigFile",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateScanConfigFile(HttpServletRequest request) {
    	log.debug("租费程序配置文件查看开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	try {
    		resultMap = rentService.scanConfigFile(this.getParamsMapByObject(request), this.getDbKey(request));
    	} catch (Exception e) {
    		log.error("租费程序配置文件查看失败， 失败原因: ", e);
    		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
    	}
    	log.debug("租费程序配置文件查看结束...");
		return JSON.Encode(resultMap);
    }
}


