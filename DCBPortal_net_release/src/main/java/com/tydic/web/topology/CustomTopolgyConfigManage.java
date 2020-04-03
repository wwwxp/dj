package com.tydic.web.topology;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bean.file.FileTree;
import com.tydic.bean.file.FileTreeFilter;
import com.tydic.bean.file.FileTreeNode;
import com.tydic.util.EncodingDetect;
import com.tydic.util.FileUtil;
import com.tydic.util.JsonFormatTool;
import com.tydic.util.SessionUtil;
/**
 * 保存拓扑图定制信息
 * @author niejianwen
 *
 */
public class CustomTopolgyConfigManage {
	private static final Logger log = Logger.getLogger(CustomTopolgyConfigManage.class);
//	private static String path="E:\\data.txt";
//	private static String path=CustomTopolgyConfigManage.class.getResource("/").getPath()+"topology";
	private static String path =SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"/view";
//	private static String path ="E:/test/view";
	private static JSONArray config= new JSONArray();
	
/**
 *转成JSONObject对象
 * @param path 路径
 * @return
 */
private static JSONObject loadConfig(String topology){
	log.info("path:"+path);
    File file = new File(path+File.separator+topology+File.separator+topology+".topo");
//    System.out.println("getAbsoluteFile:"+file.getAbsoluteFile());
    if(!file.isFile()){
    	return null;
    }
    String content=FileUtil.readFileUnicode(file.getAbsolutePath());
    return JSONObject.parseObject(content);
}

/**
 * 初始化加载所有topology配置信息
 */
public static void loadAllConfig(){
	config= new JSONArray();
	log.info("初始化加载所有topology配置信息，加载根目录："+path);
	FileTreeFilter filter=new FileTreeFilter();
	filter.setFileType(new String[]{"topo"});
	File file =new File(path);
	if(!file.exists()){
		log.error("根目录："+path+"不存在，将自动创建！");
		file.mkdirs();
	}
	FileTree tree=new FileTree(path,filter);
	List<FileTreeNode> treeNodes=tree.getFilesTress();
	for(int i=0;i<treeNodes.size();i++){
		FileTreeNode node = treeNodes.get(i);
		if(!node.isDirectory()){
			String filePath = node.getPath();
			try {
				filePath = URLDecoder.decode(filePath, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String content=FileUtil.readFileUnicode(filePath);
			JSONObject topo = JSONObject.parseObject(content);
			File topoFile = new File(filePath);
		    String topoFileName = topoFile.getName();
			topo.put("topologyName", topoFileName.substring(0, topoFileName.length()-5));
		    config.add(topo);
		}
		
	}
    
}

		
/**
 *保存JSONObject对象
 * @param path 路径
 * @return
 */
public synchronized static  int  saveConfig(JSONObject topo){
	 int state=1;
	 if(topo != null){
		String clusterName=topo.getString("clusterName");
		String topologyName=topo.getString("topologyName");
		JSONObject config_topo=CustomTopolgyConfigManage.getTopoConfig(clusterName, topologyName);
		if(config_topo != null){
			int index=CustomTopolgyConfigManage.getTopoConfigIndex(clusterName, topologyName);
			if(index == -1){
				config.add(topo);
			}else if(index < config.size()){
				config.set(index, topo);
			}else{
				config.add(topo);
			}
			
			
		}else{
			config.add(topo);
		}
		boolean isuccess=FileUtil.writeFileUnicode(path+File.separator+topologyName+File.separator+topologyName+".topo",JsonFormatTool.formatJson(topo.toJSONString()),false);
	    if(!isuccess) state=0;
	}
	 log.debug("保存topology_config:"+config.toJSONString());
	return state;
}

/**
 * 获取topology配置信息
 * @param clusterName 集群名称
 * @param topologyName   topologyName 
 * @return
 */
public static JSONObject getTopoConfig(String clusterName,String topologyName){
	JSONObject ret_topo=null;
	for(int i=0;i<config.size();i++){
		JSONObject topo=(JSONObject)config.get(i);
		String topo_clusterName=topo.getString("clusterName")== null ? "":topo.getString("clusterName");
		String topo_topologyName=topo.getString("topologyName") == null ? "":topo.getString("topologyName");
//		if(topo_clusterName.equals(clusterName) && topo_topologyName.equals(topologyName) ){
		if(topo_clusterName.equals(clusterName) && topo_topologyName.equals(topologyName) ){
			ret_topo= topo;
			return ret_topo;
		}
	}
	//不 存在缓存中试读取文件
	 ret_topo= loadConfig(topologyName);
	 if(ret_topo != null){
		 config.add(ret_topo);
	 }
	
	return ret_topo;
}

/**
 * 获取topology配置对应的索引
 * @param clusterName 集群名称
 * @param topologyid topology ID
 * @return
 */
public static int getTopoConfigIndex(String clusterName,String topologyName){
	for(int i=0;i<config.size();i++){
		JSONObject topo=(JSONObject)config.get(i);
		String topo_clusterName=topo.getString("clusterName");
		String topo_topologyName=topo.getString("topologyName");
		if(topo_clusterName.equals(clusterName) && topo_topologyName.equals(topologyName) ){
			return i;
		}
		
		
	}
	
	return -1;
}


public static void main(String [] args){
	new CustomTopolgyConfigManage();
	try {
		String code=EncodingDetect.getJavaEncode(("E:\\jstorm.txt"));
		System.out.println(System.getProperty("user.home"));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println(JsonFormatTool.formatJson(config.toJSONString()));
}
}
