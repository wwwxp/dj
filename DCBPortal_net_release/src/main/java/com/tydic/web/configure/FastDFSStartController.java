package com.tydic.web.configure;

import java.util.HashMap;
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
import com.tydic.service.configure.FastDFSStartService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/fastdfsStart")
public class FastDFSStartController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(FastDFSStartController.class);

	@Autowired
	private FastDFSStartService fastDFSStartService;

	/**
	 * 部署任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/startFastDFS", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String startFastDFS(HttpServletRequest request) {
		log.debug("FastDFSStartController, 启动FastDFS...");
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
	    	Map<String, Object> rstMap = fastDFSStartService.startFastDFS(hostList, this.getDbKey(request));
	    	return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("FastDFSStartController启动报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 停止任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/stopFastDFS", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String stopFastDFS(HttpServletRequest request) {
		log.debug("FastDFSStartController,停止FastDFS...");
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
			
			Map<String, Object> rstList = fastDFSStartService.stopFastDFS(serviceList, this.getDbKey(request));
			return JSON.Encode(rstList);
		} catch (Exception e) {
			log.error("FastDFSStartController停止报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 查询目录下所有的文件列表
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getFileList", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String getFileList(HttpServletRequest request) {
		log.debug("获取配置文件列表...");
		try {
			Map<String, String> paramsMap = this.getParamsMap(request);
			List<Map<String, Object>> resultList = fastDFSStartService.getCurrentPathFileList(paramsMap, this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("获取配置文件列表失败--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	
	/**
	 * 保存用户初始化数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addOperator", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addOperator(HttpServletRequest request) {
		log.debug("保存用户初始化数据...");
		try {
			Map<String, Object> paramsMap = this.getParamsMapByObject(request);
			Map<String, Object> resultList = fastDFSStartService.addOperator(paramsMap, this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("保存用户初始化数据失败--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 保存用户初始化数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryOperator", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryOperator(HttpServletRequest request) {
		log.debug("查询用户初始化数据...");
		try {
			Map<String, Object> paramsMap = this.getParamsMapByObject(request);
			List<HashMap<String, Object>> resultList = fastDFSStartService.queryOperator(paramsMap, this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("查询用户初始化数据失败--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 一键启动组件
	 * 
	 * @param request
	 * @return
	 */
//	@RequestMapping(value = "/addOnceStartConfig", produces = { "application/json;charset=UTF-8" })
//	@ResponseBody
//	public String addOnceStartConfig(HttpServletRequest request) {
//		log.debug("加载一键启动配置...");
//		try {
//			Map<String, Object> paramsMap = this.getParamsMapByObject(request);
//			List<HashMap<String, Object>> resultList = fastDFSStartService.addOnceStartConfig(paramsMap, this.getDbKey(request));
//			return JSON.Encode(resultList);
//		} catch (Exception e) {
//			log.error("加载一键启动配置失败--->", e);
//			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
//		}
//	}
}
