package com.tydic.web.configure;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.configure.DcaStartService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dcaStart")
public class DcaStartController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DcaStartController.class);

	@Autowired
	private DcaStartService dcaStartService;

	/**
	 * 部署任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/startDca", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String startDca(HttpServletRequest request) {
		log.debug("DcaStartController, 启动Dca...");
		try {
			//本地临时根目录
			String localRootPath = SessionUtil.getWebRootPath(request);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
			// 获取前台参数
		 	List<Map<String, String>> hostList = this.getParamsList(request);
	    	if (!BlankUtil.isBlank(hostList)) {
	    		for (int i=0; i<hostList.size(); i++) {
	    			Map<String, String> singleHostMap = hostList.get(i);
	    			singleHostMap.put("autoFile", Constant.RUN_AUTH_FILE_EXT);
	    			singleHostMap.put("localRootPath", localRootPath);
					if (empeeMap != null && !empeeMap.isEmpty()) {
						singleHostMap.put("EMPEE_ID", ObjectUtils.toString(empeeMap.get("EMPEE_ID")));
					}
	    		}
	    	} else {
	    		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "请选择需要启动的主机！"));
	    	}

			Map<String, Object> rstMap = null;
	    	if (StringUtils.equals(SystemProperty.getContextProperty("latnId"), "sx")) {  //陕西DCA启动
				rstMap = dcaStartService.startSxDca(hostList, this.getDbKey(request));
			} else {
				rstMap = dcaStartService.startDca(hostList, this.getDbKey(request));   //其他地区DCA启动
			}
			return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("DcaStartController启动报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 停止任务
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/stopDca", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String stopDcas(HttpServletRequest request) {
		log.debug("DcaStartController, 停止Dca...");
		try {
			List<Map<String, String>> serviceList = this.getParamsList(request);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
			if (!BlankUtil.isBlank(serviceList)) {
				for(int i=0;i<serviceList.size();i++){
					serviceList.get(i).put("autoFile",  Constant.STOP_AUTH_FILE_EXT);
					serviceList.get(i).put("state", Constant.STATE_NOT_ACTIVE);
					if (empeeMap != null && !empeeMap.isEmpty()) {
						serviceList.get(i).put("EMPEE_ID", ObjectUtils.toString(empeeMap.get("EMPEE_ID")));
					}
				 }
	    	} else {
	    		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "请选择需要停止的进程！"));
	    	}
			Map<String, Object> rstMap = null;
			if (StringUtils.equals(SystemProperty.getContextProperty("latnId"), "sx")) {  //陕西DCA启动
				rstMap = dcaStartService.stopSxDca(serviceList, this.getDbKey(request));
			} else {
				rstMap = dcaStartService.stopDca(serviceList, this.getDbKey(request));
			}
			return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("DcaStartController停止报错--->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
}
