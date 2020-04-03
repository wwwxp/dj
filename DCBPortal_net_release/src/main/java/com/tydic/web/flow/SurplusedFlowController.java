package com.tydic.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.flow.SurplusedFlowService;
import com.tydic.web.monitor.CustomResourceMonitorController;

@Controller
@RequestMapping("/surplusedFlow")
public class SurplusedFlowController extends BaseController {
	private static Logger log = LoggerFactory.getLogger(CustomResourceMonitorController.class);
	
	@Autowired
	private SurplusedFlowService surplusedFlowService;

	@RequestMapping(value = "/flowTransferQuery", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String flowTransferQuery(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);
		try {
			resultMap = surplusedFlowService.flowTransferQuery(params,
					FrameConfigKey.DEFAULT_DATASOURCE);
		} catch (Exception e) {
			log.error(e.getMessage());
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

}
