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
import com.tydic.service.configure.BillingService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/billing")
public class BillingController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(BillingController.class);

	/**
	 * Billing Service对象
	 */
	@Autowired
	private BillingService billingService;

	/**
	 * Billing运行
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/run", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateRunProgram(HttpServletRequest request) {
		log.debug("billing启动...");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			Map<String, String> queryParam = this.getParamsMap(request);
			param.put("flag", BusinessConstant.PARAMS_START_FLAG);
			param.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
			param.put("queryParam", queryParam);
			resultMap = billingService.updateRunAndStopHost(param, getDbKey(request));
		} catch (Exception e) {
			log.error("Biling启动失败， 失败原因: ", e);
			resultMap.put("info", "启动失败");
			resultMap.put("reason", e.getMessage());
		}
		log.debug("billing启动结束...");
		return JSON.Encode(resultMap);
	}

	/**
	 * 停止运行task
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/stop", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateStopProgram(HttpServletRequest request) {
		log.debug("billing停止...");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			Map<String, String> queryParam = this.getParamsMap(request);
			param.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
			param.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
			param.put("queryParam", queryParam);
			resultMap = billingService.updateRunAndStopHost(param, getDbKey(request));
		} catch (Exception e) {
			log.error("Billing停止失败， 失败原因: ", e);
			resultMap.put("info", "停止失败");
			resultMap.put("reason", e.getMessage());
		}
		log.debug("billing停止结束");
		return JSON.Encode(resultMap);
	}

	/**
	 * 检查program
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/task/check", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateCheckProgram(HttpServletRequest request) {
		log.debug("billing状态检查开始...");
		Map<String, String> resultMap = new HashMap<String, String>();
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			Map<String, String> queryParam = this.getParamsMap(request);
			param.put("flag", BusinessConstant.PARAMS_CHECK_FLAG);
			param.put("queryParam", queryParam);
			resultMap = billingService.updateCheckProgramState(param, getDbKey(request));
		} catch (Exception e) {
			log.error("检查运行program状态失败 ---> ", e);
			resultMap.put("info", "状态检查失败");
			resultMap.put("reason", e.getMessage());
		}
		log.debug("billing状态检查结束...");
		return JSON.Encode(resultMap);
	}

	/**
	 * 查看定义
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/viewConf", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryViewConf(HttpServletRequest request) {
		log.debug("查看定义开始...");
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Map<String, Object> param = this.getParamsMapByObject(request);
			param.put("webRootPath", SessionUtil.getWebRootPath(request));
			result = billingService.queryViewConf(param, getDbKey(request));
		} catch (Exception e) {
			log.error("查看定义失败 ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查看定义结束...");
		return JSON.Encode(result);
	}
}
