package com.tydic.web.configure;

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
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.Constant;

@Controller
@RequestMapping("/instConfig")
public class InstConfigController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(InstConfigController.class);

	@Autowired
	private InstConfigService instConfigService;

	/**
	 * 部署任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteInstConfig", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteInstConfig(HttpServletRequest request) {
		log.debug("InstConfigController, 删除实例...");
		try {
			String autoFile = Constant.DELETE_AUTH_FILE_COMMON;
			Map<String, Object> params = this.getParamsMapByObject(request);
			params.put("autoFile",autoFile);
			//本地临时根目录
			Map<String, Object> resultMap = instConfigService.deleteInstConfig(params ,this.getDbKey(request));
			return JSON.Encode(resultMap);
		} catch (Exception e) {
			log.error("InstConfigController删除报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 查询实例状态记录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryInstConfigTreeData", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String queryInstConfigTreeData(HttpServletRequest request) {
		log.debug("InstConfigController, 删除实例...");
		try {
			List<Map<String, Object>> resultList = instConfigService.queryInstConfigTreeData(this.getParamsMapByObject(request) ,this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("InstConfigController实例状态查询失败", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 查询实例状态记录
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/querybusInstConfigTreeData", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String querybusInstConfigTreeData(HttpServletRequest request) {
		log.debug("InstConfigController, 删除实例...");
		try {
			List<Map<String, Object>> resultList = instConfigService.querybusInstConfigTreeData(this.getParamsMapByObject(request) ,this.getDbKey(request));
			return JSON.Encode(resultList);
		} catch (Exception e) {
			log.error("InstConfigController实例状态查询失败", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	* @Description: 查询组件实例启停日志
	* @return String
	* @author yuanhao
	* @date 2019-12-13 11:35
	*/
	@RequestMapping(value="/queryInstConfigLogDetail",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String queryInstConfigLogDetail(HttpServletRequest request) {
		log.debug("查询实例程序启停日志信息...");
		Map<String, Object> retMap = null;
		try {
			retMap = instConfigService.queryInstConfigLogDetail(this.getParamsMapByObject(request), getDbKey(request));
		} catch (Exception e) {
			log.error("查询实例程序启停日志信息异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查询实例程序启停日志信息结束...");
		return JSON.Encode(retMap);
	}
}
