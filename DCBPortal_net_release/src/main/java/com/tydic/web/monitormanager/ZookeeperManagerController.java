package com.tydic.web.monitormanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.alibaba.jstorm.ui.model.ZookeeperNode;
import com.alibaba.jstorm.ui.utils.ZookeeperManager;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.monitormanager.zookeepermanager.ZookeeperManagerService;
import com.tydic.util.ftp.FileTool;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.monitormanager]    
  * @ClassName:    [ZookeeperManagerController]     
  * @Description:  [zookeeper管理类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:03:42]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:03:42]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping(value = "/monitorManager/zookeeperManager")
public class ZookeeperManagerController extends BaseController{
	/**
	 * 日志对象
	 */
	private static Logger log = LoggerFactory.getLogger(ZookeeperManagerController.class);

	/**
	 * Service对象
	 */
	@Autowired
	private ZookeeperManagerService zookeeperManagerService;
	
	/**
	 * 获取表格信息
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/dataGridInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String showClusters(HttpServletRequest request) {
		log.debug("zookeeper管理， 查询zookeeper开始...");
		List<Map<String, Object>> resultList = null ;
		try {
			resultList = zookeeperManagerService.showCluster(this.getParamsMapByObject(request), getDbKey(request));
		} catch (Exception e) {
			log.error("zookeeper管理， 查询zookeeper异常， 异常信息: ", e);
			return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("zookeeper管理， 查询zookeeper结束...");
		return  JSON.Encode(resultList);
 	}
	
	/**
	 * 获取树节点
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/zookeeper/node",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getChildren(HttpServletRequest request) {
		log.debug("zookeeper管理， 获取节点子信息开始...");
		
		//集群名称
		String clusterName = request.getParameter("clusterName");
		//当前选中zookeeper节点路径
		String path = request.getParameter("path");
		
    	List<ZookeeperNode> result;
		try {
			clusterName = StringEscapeUtils.escapeHtml(clusterName);
			result = ZookeeperManager.listZKNodes(clusterName, path);
			
			//获取服务组Data信息，用来在topology启停界面服务查看使用
			if (result != null && result.size() > 0) {
				for (ZookeeperNode zkNode : result) {
					String zkName = zkNode.getName();
					if (path.indexOf("localservice/serviceInfo") != -1) {
						String zkNodeData = ZookeeperManager.getZKNodeData(clusterName, FileTool.exactPath(path) + zkName);
						zkNode.setData(zkNodeData);
						zkNode.setIdLeaf(false);
					}
				}
			}
		} catch (Exception e) {
			log.debug("zookeeper管理， 获取节点子信息异常， 异常信息: ", e);
			return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("zookeeper管理， 获取节点子信息结束...");
        return JSON.Encode(result);
    }
	
	/**
	 * 获取每个节点的文本数据
	 * @param request
	 * @return
	 */
    @RequestMapping(value = "/zookeeper/nodeData",method = RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Map<String, Object> getData(HttpServletRequest request) {
    	log.debug("zookeeper管理， 获取节点数据开始...");
    	String clusterName=request.getParameter("clusterName");
		String path=request.getParameter("path");
		
    	String data = null;
		Map<String, Object> map = null;
		try {
			clusterName = StringEscapeUtils.escapeHtml(clusterName);
			data = ZookeeperManager.getZKNodeData(clusterName, path);
			map = new HashMap<>();
		} catch (Exception e) {
			log.error("zookeeper管理， 获取节点数据异常， 异常信息: ", e);
		}
    	map.put("data", data);
    	log.debug("zookeeper管理， 获取节点数据结束...");
    	return map;
    }
}
