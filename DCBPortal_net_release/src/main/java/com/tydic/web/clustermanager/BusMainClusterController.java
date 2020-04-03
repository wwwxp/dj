package com.tydic.web.clustermanager;

import javax.servlet.http.HttpServletRequest;

import com.tydic.bp.core.utils.properties.SystemProperty;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.clustermanager.BusMainClusterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busMainCluster")
public class BusMainClusterController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(BusMainClusterController.class);

	/**
	 * 主机操作Service
	 */
	@Autowired
	private BusMainClusterService busMainClusterService;

	/**
	 * 删除业务主集群信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/deleteBusMainCluster",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String deleteBusMainCluster(HttpServletRequest request) {
		log.debug("删除业务主集群开始...");
		String result = null;
		try {
			busMainClusterService.deleteBusMainCluster(this.getParamsList(request), getDbKey(request));
		} catch (Exception e) {
			log.error("删除业务主集群异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("删除业务主集群结束...");
		return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, result));
	}
	
	/**
	 * 添加业务主集群信息
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/insertBusMainCluster",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String insertBusMainCluster(HttpServletRequest request) {
        log.debug("添加业务主集群信息开始...");
        try {
        	busMainClusterService.insertBusMainCluster(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("添加业务主集群信息异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("添加业务主集群信息结束...");
        return null;
    }

    /**
     * 修改业务主集群配置信息
     * @param request
     * @return
     */
    @RequestMapping(value="/updateBusMainCluster",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateBusMainCluster(HttpServletRequest request) {
        log.debug("修改业务主集群信息开始...");
        try {
        	busMainClusterService.updateBusMainCluster(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("修改业务主集群信息异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改业务主集群信息结束...");
        return null;
    }


	/**
	 * 获取用户集群配置信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/getUserBusMainCluster",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String getUserBusMainCluster(HttpServletRequest request){
		log.debug("获取用户集群配置信息开始...");
		try{
			Map<String,Object> param = this.getParamsMapByObject(request);
			Map empeeMap = (Map) request.getSession().getAttribute("userMap");
			param.put("EMPEE_ID", empeeMap.get("EMPEE_ID"));
			List<HashMap<String, Object>> clusterList = busMainClusterService.getUserBusMainCluster(param,getDbKey(request));
			log.debug("获取用户集群配置信息结束...");
			return JSON.Encode(clusterList);
		}catch (Exception e){
			log.error("获取用户集群配置信息异常,异常信息:",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,e.getMessage()));
		}
	}
}
