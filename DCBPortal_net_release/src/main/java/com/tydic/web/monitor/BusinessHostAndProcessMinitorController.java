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
import com.tydic.service.monitor.business.BusinessMonitorService;
@Controller
@RequestMapping(value = "/host/minitor/business")
public class BusinessHostAndProcessMinitorController extends BaseController{
	private static Logger log = LoggerFactory.getLogger(ClusterMinitorController.class);
	@Autowired
	BusinessMonitorService  businessMonitorService;
	/**
	 * 主机资源占用情况图表
	 * @param request
	 * @return
	 */
  @RequestMapping(value = "/resource/charts", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
  @ResponseBody
  public String getResourceChartsData(HttpServletRequest request) {
	   List<Map<String,Object>> resultList = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultList=businessMonitorService.getResourceChartsData(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultList);
	
}
}
