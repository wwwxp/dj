package com.tydic.web.monitormanager;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.monitormanager.TaskOverstockService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.monitormanager]    
  * @ClassName:    [TaskOverstockController]     
  * @Description:  [任务运行积压情况查询]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-12-25 下午4:42:35]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-12-25 下午4:42:35]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping(value="/overstock")
public class TaskOverstockController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(TaskOverstockController.class);

	/**
	 * 任务运行积压Service对象
	 */
	@Autowired
	private TaskOverstockService taskOverstockService;


    /**
     * 查询ZK集群列表
     * @param request
     * @return
     */
    @RequestMapping(value="/queryZookeeperList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryZookeeperList(HttpServletRequest request) {
        log.debug("查询ZK集群列表开始...");
        try {
            List<Map<String, Object>> serviceList = taskOverstockService.queryZookeeperList(this.getParamsMapByObject(request), getDbKey(request));
            log.debug("查询ZK集群列表结束...");
            return JSON.Encode(serviceList);
        } catch (Exception e) {
            log.error("查询ZK集群列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }


	/**
	 * 查询ZK节点服务列表
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/queryServiceList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryServiceList(HttpServletRequest request) {
        log.debug("查询ZK集群服务列表开始...");
        try {
        	List<Map<String, Object>> serviceList = taskOverstockService.queryZkServiceList(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("查询ZK集群服务列表结束...");
        	return JSON.Encode(serviceList);
        } catch (Exception e) {
            log.error("查询ZK集群服务列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
	/**
	 * 查询ZK节点服务列表
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/queryZkServiceGroupList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryZkServiceGroupList(HttpServletRequest request) {
        log.debug("查询ZK集群服务组对应服务列表开始...");
        try {
        	List<String> serviceList = taskOverstockService.queryZkServiceGroupList(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("查询ZK集群服务组对应服务列表结束...");
        	return JSON.Encode(serviceList);
        } catch (Exception e) {
            log.error("查询ZK集群服务组对应服务列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
	/**
	 * 查询ZK服务节点信息
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/queryZkServiceDataList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryZkServiceDetailList(HttpServletRequest request) {
        log.debug("查询ZK集群服务节点数据开始...");
        try {
        	List<Map<String, Object>> serviceDataList = taskOverstockService.queryZkServiceDataList(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("查询ZK集群服务节点数据结束...");
        	return JSON.Encode(serviceDataList);
        } catch (Exception e) {
            log.error("查询ZK集群服务节点数据异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
    /**
	 * 查询ZK服务节点信息
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/queryZkServiceDataListWithGroup",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryZkServiceDataListWithGroup(HttpServletRequest request) {
        log.debug("查询ZK集群服务组节点数据开始...");
        try {
        	List<Map<String, Object>> serviceDataList = taskOverstockService.queryZkServiceDataListWithGroup(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("查询ZK集群服务组节点数据结束...");
        	return JSON.Encode(serviceDataList);
        } catch (Exception e) {
            log.error("查询ZK集群服务组节点数据异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
    /**
	 * 查询图表积压情况（根据服务名称分组）
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/queryChartsList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryChartsList(HttpServletRequest request) {
        log.debug("查询集群服务列表数据图表展示开始...");
        try {
        	List<Map<String, Object>> serviceDataList = taskOverstockService.queryChartsList(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("查询集群服务列表数据图表展示结束...");
        	return JSON.Encode(serviceDataList);
        } catch (Exception e) {
            log.error("查询集群服务列表数据图表展示异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
