package com.tydic.web.plugin;

 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.plugin.TopologyPluginService;
import com.tydic.util.SessionUtil;
/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.plugin]    
  * @ClassName:    [ToploPluginManageController]     
  * @Description:  [插件管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-22 下午7:56:08]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-22 下午7:56:08]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping(value = "/topologyPlugin")
public class TopologyPluginManageController  extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = LoggerFactory.getLogger(TopologyPluginManageController.class);
	
	/**
	 * 插件Service对象
	 */
	@Autowired
	private TopologyPluginService topologyPluginService;
	
	/**
	 * 列表
	 * @param request
	 * @return
	 */
    @RequestMapping(value="queryPlugin",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
	public String queryPlugin(HttpServletRequest request) {
		log.debug("查询插件列表开始...");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {

			Map<String, Object> params = this.getParamsMapByObject(request);
			// 部署主机根目录
			params.put("ftpRootPath", SessionUtil.getConfigValue("FTP_ROOT_PATH"));
			// 本地临时根目录
			params.put("tempPath", SessionUtil.getWebRootPath(request));
			list = topologyPluginService.queryPlugin(params, this.getDbKey(request));
			log.debug("查询插件列表结束...");
			return JSON.Encode(list);
		} catch (Exception e) {
			log.error("查询插件列表异常， 异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
    
    /**
     * 获取XML文件信息
     * @param request
     * @return
     */
    @RequestMapping(value="getXmlDesc",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getXmlDesc(HttpServletRequest request) {
    	log.debug("获取插件文件描述信息开始...");
        try {
        	Map<String, Object> params = this.getParamsMapByObject(request);
        	//部署主机根目录
        	params.put("ftpRootPath", SessionUtil.getConfigValue("FTP_ROOT_PATH"));
        	//本地临时根目录
        	params.put("tempPath", SessionUtil.getWebRootPath(request));
        	
        	Map<String, Object> rstMap = topologyPluginService.getXmlDesc(params, this.getDbKey(request));
        	log.debug("获取插件文件描述信息结束...");
        	return JSON.Encode(rstMap);
        } catch (Exception e) {
            log.error("获取插件文件描述信息失败， 失败原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
