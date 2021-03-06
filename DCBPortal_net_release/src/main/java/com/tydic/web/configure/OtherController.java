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
import com.tydic.service.configure.OtherService;
import com.tydic.util.BusinessConstant;


@Controller
@RequestMapping("/other")
public class OtherController extends BaseController {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(OtherController.class);
	
	@Autowired
	private OtherService otherService;
 
    /**
     * 运行program
     * @param request
     * @return
     */
    @RequestMapping(value="/program/run",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateRunProgram(HttpServletRequest request) {
    	log.debug("周边程序启动开始 ...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("flag", BusinessConstant.PARAMS_START_FLAG);
			param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
        	resultMap = otherService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("周边程序启动异常， 异常原因: ",e);
            resultMap.put("info", "启动失败");
            resultMap.put("flag","error");
            resultMap.put("reason",e.getMessage());
        }
        log.debug("周边程序启动结束...");
        return JSON.Encode(resultMap);
    }
    
    /**
     * 停止运行program
     * @param request
     * @return
     */
    @RequestMapping(value="/program/stop",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateStopProgram(HttpServletRequest request) {
        log.debug("周边程序停止开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
        	param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
        	resultMap = otherService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("周边程序停止异常， 异常原因: ", e);
            resultMap.put("info", "停止失败");
            resultMap.put("flag","error");
            resultMap.put("reason",e.getMessage());
        }
        log.debug("周边程序停止结束...");
        return JSON.Encode(resultMap);
    }

	/**
	 * 停止运行program
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/program/check",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updatecheck(HttpServletRequest request) {
		log.debug("周边程序检查开始...");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Map<String, Object> param = this.getParamsMapByObject(request);
			result = otherService.updateCheckHostState(param, getDbKey(request));
		} catch (Exception e) {
			log.error("周边程序检查异常, 异常原因:",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("周边程序检查结束...");
		return JSON.Encode(result);
	}
}


