package com.tydic.web.configure;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.TopicConfigService;
import com.tydic.util.SessionUtil;

/**
 * Created with IntelliJ IDEA.
 * User: yuanhao
 * Date: 16-10-11
 * Time: 上午11:01
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/topicConfig")
public class TopicConfigController extends BaseController {
	/**
	 * 日志对象
	 */
	 private static Logger log = Logger.getLogger(TopicConfigController.class);
	 
	 @Resource
	 private TopicConfigService topicConfigService;
	 
	/**
	 * 添加Topic配置管理
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/addTopicConfig",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addTopicConfig(HttpServletRequest request) {
    	log.debug("TopicConfigController，添加Topic配置信息开始");
    	Map<String, Object> retMap = new HashMap<String, Object>();
        try {
        	Map<String, Object> param = getParamsMapByObject(request);
        	
        	String webRootPath = SessionUtil.getWebRootPath(request);
        	param.put("webRootPath", webRootPath);
        	retMap = topicConfigService.addTopicConfig(param, this.getDbKey(request));
        } catch (Exception e) {
            log.error("TopicConfigController，添加Topic配置信息失败 ---> ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("TopicConfigController，添加Topic配置信息结束");
        return JSON.Encode(retMap);
    }
    
    /**
	 * 删除Topic配置管理
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/delTopicConfig",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delTopicConfig(HttpServletRequest request) {
    	log.debug("TopicConfigController，删除Topic配置信息开始...");
    	Map<String, Object> retMap = new HashMap<String, Object>();
        try {
        	Map<String, Object> param = getParamsMapByObject(request);
        	
        	String webRootPath = SessionUtil.getWebRootPath(request);
        	param.put("webRootPath", webRootPath);
        	retMap = topicConfigService.delTopicConfig(param, this.getDbKey(request));
        } catch (Exception e) {
            log.error("TopicConfigController，删除Topic配置信息对象失败 ---> ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("TopicConfigController，删除Topic配置信息对象结束...");
        return JSON.Encode(retMap);
    }
}
