package com.tydic.web.monitor;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import PluSoft.Utils.JSON;

import com.alibaba.jstorm.ui.model.ClusterEntity;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.monitor.cluster.ClusterMonitorService;

@Controller
@RequestMapping(value = "/host/minitor/clusters")
public class ClusterMinitorController extends BaseController{
	private static Logger log = LoggerFactory.getLogger(ClusterMinitorController.class);

	@Autowired
	private ClusterMonitorService clusterMonitorService;
	
	 /**
	 *展示所有集群信息
	 * @param request
	 * @return
	 */
 @RequestMapping(value = "",method = RequestMethod.GET)
 public String showClusters(HttpServletRequest request,ModelMap model) {
	    Map resultMap = null ;
		Map<String, String> params=getParamsMap(request);
		try {
			resultMap=clusterMonitorService.showClusters(params);
			Collection<ClusterEntity> clusters=(Collection<ClusterEntity>)resultMap.get("data");
			if(clusters.size()==1){
//				System.out.println("*********clusters.size()**************"+clusters.size());
				ClusterEntity cluster=clusters.iterator().next();
				String clusterName=cluster.getClusterName();
				return "redirect:/jsp/minitor/mainHostMonitor?clusterName="+clusterName;
			}else{
				model.addAttribute("clusters", JSON.Encode(clusters));
				return "/minitor/clusters";
			}
		} catch (Exception e) {
			 log.error("",e);
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	
}
}
