package com.tydic.web.configure;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.ZookeeperStartService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/zookeeperStart")
public class ZookeeperStartController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ZookeeperStartController.class);

	@Autowired
	private ZookeeperStartService zookeeperStartService;

	/**
	 * 部署任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/startZookeeper", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String startZookeeper(HttpServletRequest request) {
		log.debug("ZookeeperStartController, 启动Zookeeper...");
		try {
			//本地临时根目录
			String localRootPath = SessionUtil.getWebRootPath(request);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
			// 获取前台参数
		 	List<Map<String, String>> hostList = this.getParamsList(request);
	    	if (!BlankUtil.isBlank(hostList)) {
	    		for (int i=0; i<hostList.size(); i++) {
	    			Map<String, String> singleHostMap = hostList.get(i);
	    			singleHostMap.put("autoFile", Constant.RUN_AUTH_FILE_COMMON);
	    			singleHostMap.put("localRootPath", localRootPath);
					if (empeeMap != null && !empeeMap.isEmpty()) {
						singleHostMap.put("EMPEE_ID", ObjectUtils.toString(empeeMap.get("EMPEE_ID")));
					}
	    		}
	    	} else {
	    		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "请选择需要启动的主机！"));
	    	}
	    	
	    	Map<String, Object> rstMap = zookeeperStartService.startZookeeper(hostList, this.getDbKey(request));
	    	return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("ZookeeperStartController启动报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 停止任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/stopZookeeper", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String stopZookeeper(HttpServletRequest request) {
		log.debug("ZookeeperStartController, 停止Zookeeper...");
		try {
			List<Map<String, String>> serviceList = this.getParamsList(request);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
			if (!BlankUtil.isBlank(serviceList)) {
				for(int i=0;i<serviceList.size();i++){
					serviceList.get(i).put("autoFile",  Constant.STOP_AUTH_FILE_COMMON);
					serviceList.get(i).put("state", Constant.STATE_NOT_ACTIVE);
					if (empeeMap != null && !empeeMap.isEmpty()) {
						serviceList.get(i).put("EMPEE_ID", ObjectUtils.toString(empeeMap.get("EMPEE_ID")));
					}
				 }
	    	} else {
	    		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "请选择需要停止的进程！"));
	    	}
			Map<String, Object> rstMap = zookeeperStartService.stopZookeeper(serviceList, this.getDbKey(request));
	    	return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("ZookeeperStartController停止报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
}
