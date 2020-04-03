package com.tydic.web.configure;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.common.BusException;
import com.tydic.service.configure.RunSameIPService;
import com.tydic.util.BusinessConstant;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/runSame")
public class RunSameIPController extends BaseController {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(RunSameIPController.class);
	
	@Autowired
	private RunSameIPService runSameIPService;
 
    /**
     * 运行program
     * @param request
     * @return
     */
    @RequestMapping(value="/program/run",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateRunProgram(HttpServletRequest request) {
    	log.debug("不区分IP程序启动开始 ...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	Map empeeMap = (Map) request.getSession().getAttribute("userMap");
        	if (empeeMap != null && !empeeMap.isEmpty()) {
                param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
            }
        	param.put("flag", BusinessConstant.PARAMS_START_FLAG);
			param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
        	resultMap = runSameIPService.updateRunAndStopHost(param, getDbKey(request));
        } catch (Exception e) {
            log.error("不区分IP程序启动异常， 异常原因: ",e);
            resultMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
        }
        log.debug("不区分IP程序启动结束...");
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
        log.debug("不区分IP程序停止开始...");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	Map empeeMap = (Map) request.getSession().getAttribute("userMap");
            if (empeeMap != null && !empeeMap.isEmpty()) {
                param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
            }
        	param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
        	param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
        	resultMap = runSameIPService.updateRunAndStopHost(param, getDbKey(request));
        }catch (Exception e) {
            log.error("不区分IP程序停止异常， 异常原因: ", e);
            resultMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
        }
        log.debug("不区分IP程序停止结束...");
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
		log.debug("不区分IP程序检查开始...");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			List param = this.getParamsList(request);
			for(int i = 0;i< param.size() ;i++){
				
				result = runSameIPService.updateCheckHostState((Map<String, Object>)param.get(i), getDbKey(request));
			}
			
		} catch (BusException e) {
			log.error("不区分IP程序检查异常, 异常原因:",e);
            result.put("info", e.getErrorMsg());
            result.put("state", e.getErrorCode());
            result.put("reason", e.getErrorReason());
        } catch (Exception e) {
			log.error("不区分IP程序检查异常, 异常原因:",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("不区分IP程序检查结束...");
		return JSON.Encode(result);
	}
}


