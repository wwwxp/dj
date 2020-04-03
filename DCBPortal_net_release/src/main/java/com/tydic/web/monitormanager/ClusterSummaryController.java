package com.tydic.web.monitormanager;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.monitormanager.clustersummary.ClusterSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/monitorManager/clusterSummary")
public class ClusterSummaryController extends BaseController{
	private static Logger log = LoggerFactory.getLogger(ClusterSummaryController.class);

	@Autowired
	private ClusterSummaryService clusterSummaryService;

	/**
	 * 获取集群摘要列表信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/geBusClusterList", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String geBusClusterList(HttpServletRequest request) {
		try {
			Map<String, Object> params = this.getParamsMapByObject(request);
			Map<String, Object> userMap = (Map)request.getSession().getAttribute("userMap");
			params.put("EMPEE_ID", userMap.get("EMPEE_ID"));
			params.put("PAGE_SIZE", request.getParameter("pageSize"));
			params.put("PAGE_INDEX", request.getParameter("pageIndex"));
			Map<String, Object> clusterMap = clusterSummaryService.getBusClusterList(params);
			return JSON.Encode(clusterMap);
		} catch (Exception e) {
			log.error("获取集群摘要列表失败",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 获取集群配置
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getClusterList", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getClusterList(HttpServletRequest request) {
		Map<String, String> params=getParamsMap(request);
		try {
			List<Map<String, Object>> clusterList = clusterSummaryService.getClusterList(params);
			return JSON.Encode(clusterList);
		} catch (Exception e) {
			 log.error("获取集群配置失败",e);
			 return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
 	}
	
	/**
	 * 获取表格信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dataGridInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String showClusters(HttpServletRequest request) {
		log.debug("获取Topology信息开始...");
		Map<String, Object> resultMap = new HashMap<String, Object>() ;
		try {
			resultMap = clusterSummaryService.showCluster(this.getParamsMapByObject(request));
		} catch (Exception e) {
			 log.error("获取Topology信息异常， 异常信息:", e);
		}
		log.debug("获取Topology信息结束...");
		return  JSON.Encode(resultMap);
 	}
	/**
	 * 拓扑信息--获取配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/topConfigureInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String showTopConfInfo(HttpServletRequest request) {
		List resultList = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultList=clusterSummaryService.showTopConfInfo(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return  JSON.Encode(resultList);
 	}
	
	/**
	 * nimbus信息--获取配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/nimConfigureInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String showNimConfInfo(HttpServletRequest request) {
		List resultList = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultList=clusterSummaryService.showNimConfInfo(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return  JSON.Encode(resultList);
 	}
	
	/**
	 * supervisor信息--获取配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/supConfigureInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String showSupConfInfo(HttpServletRequest request) {
		List resultList = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultList=clusterSummaryService.showSupConfInfo(params);
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultList);
 	}
}
