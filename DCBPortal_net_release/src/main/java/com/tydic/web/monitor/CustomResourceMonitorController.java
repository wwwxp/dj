package com.tydic.web.monitor;

import java.util.List;
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
import com.tydic.service.monitor.resource.CustomResourceMonitorService;
@Controller
@RequestMapping(value = "/host/minitor/resource")
public class CustomResourceMonitorController  extends BaseController {
	private static Logger log = LoggerFactory.getLogger(CustomResourceMonitorController.class);
	@Autowired
	private CustomResourceMonitorService customResourceMonitorService;
	/**
	 * 获取Supervisor监控信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/supervisor/summary", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
	public String getCustomTopologyInfo(HttpServletRequest request) {
		Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=customResourceMonitorService.getCustomSupervisorInfo(params);
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
	@RequestMapping(value = "/nimbus/configuration", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String nimbusConf(HttpServletRequest request) {
		Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=customResourceMonitorService.nimbusConf(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultMap);
    }
	

	/**
	 * worker资源占用情况
	 * @param request
	 * @return
	 */
   @RequestMapping(value = "/worker/metrics", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
   @ResponseBody
   public String workerMetrics(HttpServletRequest request) {
	   Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=customResourceMonitorService.workerMetrics(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultMap);
	
}
	
   /**
	 * worker资源占用情况图表
	 * @param request
	 * @return
	 */
  @RequestMapping(value = "/worker/charts", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
  @ResponseBody
  public String getChartsData(HttpServletRequest request) {
	   List<Map<String,Object>> resultList = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultList=customResourceMonitorService.getChartsData(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultList);
	
}
	
 
 	
	}