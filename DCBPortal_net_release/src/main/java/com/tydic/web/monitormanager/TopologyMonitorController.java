package com.tydic.web.monitormanager;

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
import com.tydic.service.monitormanager.TopologyMonitorService;
@Controller
@RequestMapping(value = "/monitorManager/topology")
public class TopologyMonitorController extends BaseController{
	private static Logger log = LoggerFactory.getLogger(TopologyMonitorController.class);
	@Autowired
	private TopologyMonitorService topologyMonitorService;
		/**
		 * 
		 * 查询componentMetrics信息
		 * @param request
		 * @return
		 */
		@RequestMapping(value = "/componentMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String componentMetrics(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=topologyMonitorService.queryComponentMetric(params);
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
		@RequestMapping(value = "/workerMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
		@ResponseBody
	    public String workerMetrics(HttpServletRequest request) {
			Map resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=topologyMonitorService.queryWorkerMetrics(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
	    }
		
		 /**
		 * 查询拓扑摘要信息
		 * @param request
		 * @return
		 */
	 @RequestMapping(value = "/summary", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	 @ResponseBody
	 public String getTopologySummary(HttpServletRequest request) {
		    Map<String,Object> resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap=topologyMonitorService.queryTopologySummary(params);
			} catch (Exception e) {
				 log.error("",e);
				 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			}
			return JSON.Encode(resultMap);
		
	}
	 
	 /**
	  * 查询拓扑摘要信息
	  * @param request
	  * @return
	  */
	 @RequestMapping(value = "/componentMetricList", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	 @ResponseBody
	 public String queryComponentMetricList(HttpServletRequest request) {
		 Map<String,Object> resultMap = null ;
		 Map<String, String> params=getParamsMap(request);
		 try {
			 resultMap=topologyMonitorService.queryComponentMetricList(params);
		 } catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		 }
		 return JSON.Encode(resultMap);
		 
	 }
	 
	 /**
	 * 查询拓扑摘要信息
	 * @param request
	 * @return
	 */
	 @RequestMapping(value = "/supervisor", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	 @ResponseBody
	 public String getSupervisorWorkers(HttpServletRequest request) {
		    Map<String,Object> resultMap = null ;
			Map<String, String> params=getParamsMap(request);
			try {
				resultMap = topologyMonitorService.querySupervisorWorkers(params);
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
	@RequestMapping(value = "/nettyMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String nettyMetrics(HttpServletRequest request) {
		Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=topologyMonitorService.queryNettyMetrics(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultMap);
    }
	
	/**
	 * 
	 * 查询单个workerMetrics信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/supervisor/workerMetrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String supervisorWorkerMetrics(HttpServletRequest request) {
		Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=topologyMonitorService.querySupervisorWorkerMetrics(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultMap);
    }
		
}
