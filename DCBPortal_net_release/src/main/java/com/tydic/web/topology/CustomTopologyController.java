package com.tydic.web.topology;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bean.file.FileTree;
import com.tydic.bean.file.FileTreeFilter;
import com.tydic.bean.file.FileTreeNode;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
@Controller
@RequestMapping(value = "/customTopology")
public class CustomTopologyController  extends BaseController{
	private static Logger log = LoggerFactory.getLogger(CustomTopologyController.class);
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String customTopology(
			@RequestParam(value = "clusterName", required = true) String clusterName,
			@RequestParam(value = "topologyName", required = true) String topologyName) {
		log.info("request topology info for clusterName: " + clusterName + " topologyName:" + topologyName);
		return "jstorm/topologyDisplay";

	}
	/**
	 * 获取拓扑图定制信息
	 * @param cluster
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getCustomTopologyInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
	public String getCustomTopologyInfo(HttpServletRequest request) {
		 Map<String, String> params=getParamsMap(request);
		 String clusterName=params.get("clusterName");
		 String topologyName=params.get("topologyName");
		 int index = topologyName.lastIndexOf("-");
		 if(index>0){
			 topologyName = topologyName.substring(0, index); 
		 }
		 
		log.info("request topology info for clusterName: " + clusterName + " topologyName:" + topologyName);
		JSONObject info;
		try {
			info = CustomTopolgyConfigManage.getTopoConfig(clusterName, topologyName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return info== null ?"":info.toJSONString();

	}
	
	/**
	 * 保存拓扑图定制信息
	 * 并返回保存后的定制信息
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "/saveCustomTopologyInfo", method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
	public String saveCustomTopologyInfo(HttpServletRequest request ) {
		 Map<String, String> params=getParamsMap(request);
		 String param=params.get("param");
		log.info("request topology info for param : " + param );
		JSONObject topo=JSONObject.parseObject(param);
		int state=CustomTopolgyConfigManage.saveConfig(topo);
		JSONObject info=CustomTopolgyConfigManage.getTopoConfig(topo.getString("clusterName"), topo.getString("topologyName"));
		return info.toJSONString();
	}
	
	/**
	 * 获取拓扑节点图片信息
	 * @param cluster
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getImagesInfo", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
	public String getImagesInfo(HttpServletRequest request) {
		List<FileTreeNode> treeNodes = null;
		try {
			
			FileTreeFilter filter=new FileTreeFilter();
			filter.setFileType(new String[]{"gif","png","jpg","jpeg"});
			String rootPath = request.getServletContext().getRealPath("/");
			String imageRootPath=rootPath+File.separator+"images"+File.separator+"topology"+File.separator+"custom";
			FileTree tree=new FileTree(imageRootPath,filter);
			 treeNodes=tree.getFilesTress();
			for(int i=0;i<treeNodes.size();i++){
				FileTreeNode node = treeNodes.get(i);
				String tmp = URLEncoder.encode(rootPath, "UTF-8");
				
				String parentPath = node.getParentPath();
				if(parentPath != null){
					parentPath =URLDecoder.decode(parentPath.replaceFirst(tmp, ""),"UTF-8");
					parentPath = parentPath.replaceAll("\\\\", "/").replaceFirst("images/topology/custom", "/").replaceAll("/+", "/");
					node.setParentPath(parentPath);
				}
				
				
				String path = node.getPath();
				path = URLDecoder.decode(path.replaceFirst(tmp, ""),"UTF-8");
				path = path.replaceAll("\\\\", "/").replaceFirst("images/topology/custom", "/").replaceAll("/+", "/");
				node.setPath(path);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(treeNodes);

	}
	
}
