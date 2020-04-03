package com.tydic.web.clustermanager;

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

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.clustermanager.ServiceTypeService;
import com.tydic.util.StringTool;

@Controller
@RequestMapping("/serviceType")
public class ServiceTypeController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ServiceTypeController.class);

	/**
	 * 主机操作Service
	 */
	@Autowired
	private ServiceTypeService serviceTypeService;

	/**
	 * 添加集群信息
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/insertServiceType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String insertServiceType(HttpServletRequest request) {
        log.debug("添加集群信息开始...");
        try {
        	serviceTypeService.insertServiceType(this.getParamsMapByObject(request), getDbKey(request), request);
        } catch (Exception e) {
            log.error("添加集群信息异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("添加集群信息结束...");
        return null;
    }

    /**
     * 修改集群配置信息
     * @param request
     * @return
     */
    @RequestMapping(value="/updateServiceType",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateServiceType(HttpServletRequest request) {
        log.debug("修改集群信息开始...");
        try {
        	serviceTypeService.updateServiceType(this.getParamsMapByObject(request), getDbKey(request), request);
        } catch (Exception e) {
            log.error("修改集群信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改集群信息结束...");
        return null;
    }

	/**
	 * 删除集群信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/deleteServiceType",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String deleteServiceType(HttpServletRequest request) {
		log.debug("删除集群开始...");
		String result = null;
		try {
			serviceTypeService.deleteServiceType(this.getParamsMapByObject(request), getDbKey(request));
		} catch (Exception e) {
			log.error("删除集群异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("删除集群结束...");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}
	
	/**
	 * 删除集群信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/queryComponentsParams",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String queryComponentsParams(HttpServletRequest request) {
		log.debug("获取集群配置参数开始...");
		try {
			List<HashMap<String, Object>> retList = serviceTypeService.queryComponentsParams(this.getParamsMapByObject(request), getDbKey(request));
			log.debug("获取集群配置参数结束...");
			return JSON.Encode(retList);
		} catch (Exception e) {
			log.error("获取集群配置参数信息异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 获取目录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getPropList",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getPropKeyValue(HttpServletRequest request) {
		log.debug("获取配置参数开始...");
		HashMap<String, Object> prop = new HashMap<String, Object>();
		Map<String, Object> params = this.getParamsMapByObject(request);
		String propKey = StringTool.object2String(params.get("PROPERTIES_KEY"));
		if (!BlankUtil.isBlank(propKey)) {
			if (propKey.indexOf(",") != -1) {
				String [] keys = propKey.split(",");
				for (int i=0; i<keys.length; i++) {
					String propValue = SystemProperty.getContextProperty(keys[i]);
					prop.put(keys[i], propValue);
				}
			} else {
				String propValue = SystemProperty.getContextProperty(propKey);
				prop.put(propKey, propValue);
			}
			log.debug("获取配置参数成功 , 配置信息: " + (prop == null ? 0 : prop));
		}
		return JSON.Encode(prop);
	}
	
}
