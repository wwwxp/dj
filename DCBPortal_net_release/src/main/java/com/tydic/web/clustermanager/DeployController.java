package com.tydic.web.clustermanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.tydic.service.clustermanager.DeployService;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/deploy")
public class DeployController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DeployController.class);

	@Autowired
	private DeployService deployService;

	/**
	 * 部署环境
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/deployHost",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateDeployHost(HttpServletRequest request) {
		log.debug("组件部署开始...");
		String result = null;
		try {
			Map<String,Object> param =  new HashMap<String, Object>();
			param.put("paramList", getParamsList(request));
			param.put("rootPath", SessionUtil.getWebRootPath(request));
			result = deployService.updateDeployHost(param, getDbKey(request));
		} catch (Exception e) {
			log.error("组件部署异常， 异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, e.getMessage()));
		}
		log.debug("组件部署结束...");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}

	/**
	 * 删除主机以及远程目录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/deleteHostAndPath",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String deleteHostAndPath(HttpServletRequest request) {
		log.debug("DeployController，删除主机以及远程目录开始");
		String result = "";
		try {
			result=deployService.deteleHostAndPath(getParamsMap(request),getDbKey(request));
		} catch (Exception e) {
			log.error("DeployController，删除主机以及远程目录失败 ---> ",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, e.getMessage()));
		}
		log.debug("DeployController，删除主机以及远程目录结束");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}
	
	/**
	 * 集群划分-删除业务类主机
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/deleteServiceHost",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String deleteServiceHost(HttpServletRequest request) {
		log.debug("删除业务类主机开始");
		String result = "";
		try {
			result=deployService.deleteServiceHost(getParamsMap(request),getDbKey(request));
		} catch (Exception e) {
			log.error("DeployController，删除业务类主机失败 ---> ",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("DeployController，删除业务类主机结束");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}
	
	/**
	 * 添加supervisor、nimbus主机时,billing和rent同步添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/insertSupervisor",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String insertSupervisor(HttpServletRequest request) {
		log.debug("添加supervosir主机开始");
		String result = "";
		try {
			result = deployService.insertSupervisor(getParamsMapByObject(request),getDbKey(request));
		} catch (Exception e) {
			log.error("添加supervosir主机失败 ---> ",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("添加supervosir主机结束");
		return JSON.Encode(result);
	}
	
	/**
	 * 业务主机划分
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/insertBusiness",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String insertBusiness(HttpServletRequest request) {
		log.debug("添加业务主机划分开始...");
		String result = "";
		try {
			result = deployService.insertBusiness(getParamsMapByObject(request),getDbKey(request));
		} catch (Exception e) {
			log.error("添加业务主机划分失败 ---> ",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("添加业务主机划分结束");
		return JSON.Encode(result);
	}
	
	/**
	 * 批量删除划分主机
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delHostBatchPartition",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delHostBatchPartition(HttpServletRequest request) {
		log.debug("批量删除集群划分主机开始...");
		String result = "";
		try {
			result = deployService.delHostBatchPartition(getParamsMapByObject(request),getDbKey(request));
		} catch (Exception e) {
			log.error("批量删除集群划分主机失败 ---> ",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("批量删除集群划分主机结束");
		return JSON.Encode(result);
	}
	
    /**
     * 查看文件树
     * @param request
     * @return
     */
    @RequestMapping(value="/ocsFileTree",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String ocsFileTree(HttpServletRequest request) {
    	List resultList = null;
		try {
			resultList=deployService.queryFileTree(getParamsMap(request));
		} catch (Exception e) {
			log.error("读取文件列表出错：－－－>", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultList);
    }

	public static void main(String[] args) {
		List list  = new ArrayList();
		list.add("ddd");
		list.add("ccc");
		String aa = JSON.Encode(list);
		System.out.println(aa);
	}


}
