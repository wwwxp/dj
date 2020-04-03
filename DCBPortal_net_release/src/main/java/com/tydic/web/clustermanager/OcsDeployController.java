package com.tydic.web.clustermanager;

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
import com.tydic.service.configure.DeployBusTaskService;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/busDeploy")
public class OcsDeployController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(OcsDeployController.class);

	@Autowired
	public DeployBusTaskService deployBusTaskService;
	
	/**
	 * 业务程序 的版本部署
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/distribute",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateDistribute(HttpServletRequest request) throws Exception {
		log.debug("业务部署开始...");
		try {
			// 获取页面传递的其他信息
			Map<String, Object> params = this.getParamsMapByObject(request);
			String webPath = SessionUtil.getWebRootPath(request);
			params.put("webRootPath", webPath);
			String resultStr = deployBusTaskService.updateDistribute(params, this.getDbKey(request));
			log.debug("业务部署结束...");
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, resultStr));
		} catch (Exception e) {
			log.error("业务部署异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, e.getMessage()));
		}
	}
}
