package com.tydic.web.monitor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.monitor.pressure.CustomPressureMonitorService;
@Controller
@RequestMapping(value = "/host/minitor/pressure")
public class CustomPressureMonitorController extends BaseController{
	private static Logger log = LoggerFactory.getLogger(CustomResourceMonitorController.class);
	@Autowired
	private CustomPressureMonitorService customPressureMonitorService;
	
	 /**
		 * 查询拓扑摘要信息
		 * @param request
		 * @return
		 */
	 @RequestMapping(value = "/topology/summary", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	 @ResponseBody
	 public String getTopologySummary(HttpServletRequest request) {
		    Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.getTopologySummary(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
		
	}
	 
	 /**
		 * nimbus配置信息
		 * 
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/topology/configuration", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String nimbusConf(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.topologyConf(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		
		/**
		 * 查询topologyState信息
		 * 
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/topology/state", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String topologyState(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.topologyState(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		
		/**
		 * 
		 * 查询componentMetrics信息
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/topology/componentMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String componentMetrics(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.componentMetrics(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		/**
		 * 
		 * 查询workerMetrics信息
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/topology/workerMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String workerMetrics(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.workerMetrics(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		/**
		 * 
		 * 查询 taskStats信息
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/topology/taskStats", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String taskStats(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=customPressureMonitorService.taskStats(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		
		
}
