package com.tydic.web.configure;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.SwitchMasterStandbyNetService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.web.configure]    
  * @ClassName:    [SwitchMasterStandbyNetController]     
  * @Description:  [版本切换Controller]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-21 下午2:12:13]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-21 下午2:12:13]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping("/switchMasterStandbyNet")
public class SwitchMasterStandbyNetController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SwitchMasterStandbyNetController.class);
	
	@Autowired
	private SwitchMasterStandbyNetService switchMasterStandbyNetService;
	
	 /**
     * 版本切换，查询Topology列表
     * @param request
     * @return
     */
    @RequestMapping(value="/loadRunningTopologyList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadRunningTopologyList(HttpServletRequest request) {
    	log.debug("版本切换， 获取正在运行的Topology列表开始...");
    	List<HashMap<String, Object>> topologyList = new ArrayList<HashMap<String, Object>>();
        try {
        	topologyList = switchMasterStandbyNetService.getRunningTopologyList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("版本切换， 获取正在运行的Topology列表异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("版本切换， 获取正在运行的Topology列表结束...");
        return JSON.Encode(topologyList);
    }
    
    /**
     * 版本切换，查询待升级的Topology列表
     * @param request
     * @return
     */
    @RequestMapping(value="/loadUpgradTopologyList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadUpgradTopologyList(HttpServletRequest request) {
    	log.debug("版本切换， 获取待升级的Topology列表开始...");
    	List<HashMap<String, Object>> topologyList = new ArrayList<HashMap<String, Object>>();
        try {
        	topologyList = switchMasterStandbyNetService.getUpgradeTopologyList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("版本切换， 获取待升级的Topology列表异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("版本切换， 获取待升级的Topology列表结束...");
        return JSON.Encode(topologyList);
    }
    
	 /**
     * 加载Topology运行主机
     * @param request
     * @return
     */
    @RequestMapping(value="/loadRunningTopologyNodeList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String loadRunningTopologyNodeList(HttpServletRequest request) {
    	log.debug("版本切换， 查询Topology节点信息开始...");
    	List<HashMap<String, Object>> topologyList = new ArrayList<HashMap<String, Object>>();
        try {
        	topologyList = switchMasterStandbyNetService.getRunningTopologyNodeList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("版本切换， 查询Topology节点信息异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("版本切换， 查询Topology节点信息结束...");
        return JSON.Encode(topologyList);
    }
    
	 /**
     * 灰度发布（部分节点升级）
     * @param request
     * @return
     */
    @RequestMapping(value="/startNodeVersionUpgrade",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String startNodeVersionUpgrade(HttpServletRequest request) {
    	log.debug("版本切换， 灰度发布开始...");
    	HashMap<String, Object> retMap = new HashMap<String, Object>();
        try {
        	retMap = switchMasterStandbyNetService.startNodeVersionUpgrade(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("版本切换， 灰度发布信息异常， 异常信息： ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("版本切换， 灰度发布结束...");
        return JSON.Encode(retMap);
    }
}
