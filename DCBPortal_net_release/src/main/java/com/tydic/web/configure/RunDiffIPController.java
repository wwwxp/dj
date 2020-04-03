package com.tydic.web.configure;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.RunDiffIPService;
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


@Controller
@RequestMapping("/runDiff")
public class RunDiffIPController extends BaseController {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(RunDiffIPController.class);
	
	@Autowired
	private RunDiffIPService runDiffIPService;
	
 
    /**
     * 运行program
     * @param request
     * @return
     */
    @RequestMapping(value="/program/run",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateRunProgram(HttpServletRequest request) {
    	log.debug("区分IP程序启动开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("flag", BusinessConstant.PARAMS_START_FLAG);
			param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
            param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
        	resultMap = runDiffIPService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("区分IP程序启动异常， 异常原因: ", e);
			resultMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
		}
        log.debug("区分IP程序启动结束...");
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
    	log.debug("区分IP程序停止开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
        	param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
        	Map empeeMap = (Map) request.getSession().getAttribute("userMap");
            param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
        	resultMap = runDiffIPService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("区分IP程序停止异常， 异常原因: ", e);
			resultMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
		}
        log.debug("区分IP程序停止结束...");
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
		log.debug("区分IP程序状态检查开始...");
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			List list  = this.getParamsList(request);
			for(int i = 0 ; i < list.size() ;i++){
				Map<String, Object>  param = (Map<String, Object> )list.get(i);
				Map<String, Object> result = runDiffIPService.updateCheckHostState(param, getDbKey(request));
				resultList.add(result);
			}
			
		} catch (Exception e) {
            log.error("区分IP程序状态检查失败， 失败原因: ", e);
        }  
		log.debug("区分IP程序状态检查结束...");
		return JSON.Encode(resultList);
	}
	
	/**
	 * 主备切换
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/program/switch",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateSwitch(HttpServletRequest request) {
		log.debug("区分IP程序主备切换开始...");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			result = runDiffIPService.updateSwitch(getDbKey(request));
		} catch (Exception e) {
			log.error("区分IP程序主备切换异常， 异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("区分IP程序主备切换结束...");
		return JSON.Encode(result);
	}
}


