package com.tydic.web.configure;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.TopManagerService;


/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.configure]    
  * @ClassName:    [TopManagerController]     
  * @Description:  [Topology重新负载]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-29 下午5:20:40]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-29 下午5:20:40]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping("/topManager")
public class TopManagerController extends BaseController{
	@Autowired
	private TopManagerService topManagerService;
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(TopManagerController.class);
	
	/**
	 * 获取文件内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/topRebalanceReload", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String topRebalanceReload (HttpServletRequest request) {
		log.debug("重新负载开始...");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String,Object> params = this.getParamsMapByObject(request);
			resultMap = topManagerService.topRebalanceReload(params,getDbKey(request));
		} catch (Exception e) {
			log.error("TopManagerController重载出错：－－－>", e);
			resultMap.put("info", "重新负载失败");
            resultMap.put("reason", e.getMessage());
		}
		log.debug("重新负载结束, 返回结果: " + resultMap.toString());
		return JSON.Encode(resultMap);
	}
	
}
