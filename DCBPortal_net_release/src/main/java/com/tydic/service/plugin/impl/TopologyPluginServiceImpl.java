package com.tydic.service.plugin.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.tydic.bean.FtpDto;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esotericsoftware.minlog.Log;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.plugin.TopologyPluginService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.XmlTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.plugin.impl]    
  * @ClassName:    [TopologyPluginServiceImpl]     
  * @Description:  [插件管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-23 上午9:23:51]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-23 上午9:23:51]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class TopologyPluginServiceImpl implements TopologyPluginService {
	/**
	 * 日志对象
	 */
	private static Logger log = LoggerFactory.getLogger(TopologyPluginServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Autowired
	private  CoreService coreService; 
	
	/**
	 * 查询插件列表
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List 返回对象
	 */
	@Override
	public List<Map<String, Object>> queryPlugin(Map<String, Object> params, String dbKey) throws Exception {
		Log.debug("查询插件列表, 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//部署主机根目录
		String ftpRootPath = StringTool.object2String(params.get("ftpRootPath"));
		//程序临时目录
		String tempPath = StringTool.object2String(params.get("tempPath"));
		//程序版本
		String version = StringTool.object2String(params.get("VERSION"));
		//Topology类型
    	String topologyType = StringTool.object2String(params.get("CLUSTER_TYPE"));
    	//业务部署路径
    	String configPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
    	String fullPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(configPath) + FileTool.exactPath(version) + FileTool.exactPath(topologyType) ;
    	log.info("插件列表, 目录: " + fullPath);
    	
    	String webRootPath = tempPath + Constant.TMP +System.currentTimeMillis() +"/";
    	
    	//业务主机群编码
    	String busMainCode = StringTool.object2String(params.get("BUS_CLUSTER_CODE"));
    	
    	//返回插件列表文件
    	List<Map<String, Object>> pluginList = new ArrayList<Map<String, Object>>();
    	
    	//查询插件配置文件
    	Map<String, Object> queryMap = new HashMap<String, Object>();
    	queryMap.put("BUS_CLUSTER_CODE", busMainCode);
    	queryMap.put("CLUSTER_TYPE", topologyType);
    	List<HashMap<String, Object>> fileList = coreService.queryForList2New("pluginConfig.queryPluginConfigList", queryMap, dbKey);
    	if (!BlankUtil.isBlank(fileList)) {
    		for (int i=0; i<fileList.size(); i++) {
    			String id = UUID.randomUUID().toString().replace("-", "");
    			Map<String, Object> pluginNode = fileList.get(i);
	    		List<Map<String, Object>> xmlList = readXml(fullPath, webRootPath, pluginNode, id);
	    		pluginList.addAll(xmlList);
    		}
    	}
    	log.debug("插件列表数量: " + (pluginList.size()));
		return pluginList;
	}
	
	/**
	 * 解析XML文件
	 * @param filePath 远程文件目录
	 * @param localPath 本地临时目录
	 * @param pluginMap XML文件名称
	 * @param rootId
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> readXml(String filePath,String localPath, Map<String, Object> pluginMap, String rootId) throws Exception{
		log.debug("解析XML文件, 远程目录: " + filePath + ", 远程XML文件: " + pluginMap.get("SOURCEC_FILE_NAME") + ", 根目录标识: " + rootId);
		// 获取所有参数信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
        
			String sourceFile = filePath + StringTool.object2String(pluginMap.get("SOURCE_FILE_NAME"));
			String localTempFile = localPath + StringTool.object2String(pluginMap.get("SOURCE_FILE_NAME"));
			
			//判断部署主机文件是否存在,如果不存在直接返回
			boolean isExists = trans.isExistPath(sourceFile);
			log.debug("配置文件目录: " + sourceFile + ", 部署主机是否存在: " + isExists);
			if (!isExists) {
				return list;
			}
			trans.get(sourceFile, localTempFile);
			log.debug("XML文件下载到本地临时目录成功, 下载源文件: " + sourceFile + ", 本地临时存储文件: " + localTempFile);
			
			Document doc = XmlTool.read(new File(localTempFile), "UTF-8");
			Element rootElement = doc.getRootElement();
			
			String pluginFileName = StringTool.object2String(pluginMap.get("PLUGIN_FILE_NAME"));
			
			//当前插件文件添加到Map对象
			Map<String, Object> fileMap = new HashMap<String, Object>();
			fileMap.put("id", rootId);
			fileMap.put("parentId", "-1");
			fileMap.put("desc", StringTool.object2String(pluginMap.get("NODE_DESC")));
			fileMap.put("name", StringTool.object2String(pluginMap.get("NODE_NAME")));
			fileMap.put("pluginFileName", pluginFileName);
			list.add(fileMap);
			
			String pluginPath = filePath + pluginFileName;
			String localTempPath = localPath + pluginFileName;
			log.debug("插件配置文件路径: " + pluginPath + ", 本地临时保存文件目录: " + localTempPath);
			trans.get(pluginPath, localTempPath);
			log.debug("获取插件文件成功, 插件名称: " + pluginFileName);
			
			Document pluginDoc = XmlTool.read(new File(localTempPath), "UTF-8");
			Element pluginRootElement = pluginDoc.getRootElement();
			log.debug("解析插件XML文件， 获取Root节点， Root节点名称: " + pluginRootElement.getName());
			
			getXmlElement(rootElement, pluginRootElement, rootId, true, list, pluginFileName);
		} catch (Exception e) {
			log.debug("解析XML文件失败, 失败原因: ", e);
			throw e;
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return list;
	 }

	/**
	 * 解析XML子节点信息
	 * @param el
	 * @param pluginEl
	 * @param rootId
	 * @param isRoot
	 * @param list
	 * @param pluginFileName
	 */
	@SuppressWarnings("unchecked")
	private static void getXmlElement(Element el, Element pluginEl, String rootId ,boolean isRoot,List<Map<String, Object>> list,String pluginFileName){
		log.debug("解析XML子节点开始..."); 
		
		//源配置文件
		List<Element> els = el.elements();
		
		//插件元素
		List<Element> pluginEls = pluginEl.elements();
		
		for (Element temp : els) {
			Map<String, Object> map = new HashMap<String, Object>();
			String name = temp.attributeValue("start_plugin");
			if (StringUtils.isBlank(name)) {
				name = temp.attributeValue("plugin");
			}
			String start_plugin = temp.attributeValue("start_plugin");
			for (Element pl : pluginEls) {
				String plname = pl.attributeValue("name");
				if (name.equals(plname)) {
					String desc = pl.attributeValue("desc");
					map.put("desc", desc);
					break;
				}
			}
			if (map.get("desc") == null) {
				map.put("desc", name);
			}
			String id = UUID.randomUUID().toString().replace("-", "");
			map.put("id", id);
			map.put("name", name);
			map.put("img", "plugin.png");
			map.put("pluginFileName", pluginFileName);
			map.put("start_plugin", start_plugin);
			map.put("parentId", rootId);
			list.add(map);
			if (!BlankUtil.isBlank(temp.elements())) {
				getXmlElement(temp, pluginEl, id, false, list, pluginFileName);
			}
		}
	 }

	/**
	 * 获取XML文件信息
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getXmlDesc(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("获取XML文件信息 ,业务参数: " + params.toString() + ", dbKey: " + dbKey);
    	//部署主机根目录
		String ftpRootPath = StringTool.object2String(params.get("ftpRootPath"));
		//程序临时目录
		String tempPath = StringTool.object2String(params.get("tempPath"));
		//当前版本(例如:10.0.0.0)
		String version = StringTool.object2String(params.get("version"));
		//程序版本(例如:DIC-BIL-NET_V10.0.0.0)
		String versionName = StringTool.object2String(params.get("versionName"));
		//Topology类型
    	String topologyType = StringTool.object2String(params.get("topologyType"));
    	//业务部署路径
    	String configPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
    	String fullPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(configPath) + FileTool.exactPath(versionName) + FileTool.exactPath(topologyType);
    	log.info("插件列表, 目录: " + fullPath);

    	//插件名称
    	String pluginName = StringTool.object2String(params.get("pluginFileName"));
    	//节点属性名称
    	String attrName = StringTool.object2String(params.get("name"));
    	//本地保存临时目录
    	String webRootPath = FileTool.exactPath(tempPath) + Constant.TMP +System.currentTimeMillis();
    	
    	//集群编码
    	String busClusterCode = StringTool.object2String(params.get("busClusterCode"));
    	
    	//查询集群部署根目录
    	Map<String, Object> clusterQueryMap = new HashMap<String, Object>();
    	clusterQueryMap.put("BUS_CLUSTER_CODE", busClusterCode);
    	clusterQueryMap.put("CLUSTER_TYPE", topologyType);
    	List<HashMap<String, Object>> clusterList = coreService.queryForList2New("serviceType.queryClusterDeployList", clusterQueryMap, dbKey);
    	if (BlankUtil.isBlank(clusterList)) {
    		log.debug("集群部署信息为空， 请检查！");
    		return new HashMap<String, Object>();
    	}
    	String appRootPath = StringTool.object2String(clusterList.get(0).get("CLUSTER_DEPLOY_PATH"));
    	String clusterId = StringTool.object2String(clusterList.get(0).get("CLUSTER_ID"));
    	 
    	// 获取所有参数信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		//返回对象
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			String sourcePath = FileTool.exactPath(fullPath) + pluginName;
			String localPath = FileTool.exactPath(webRootPath)  + pluginName;
			log.debug("源文件名称: " + sourcePath  + ", 本地临时文件: " + localPath);
			
			trans.get(sourcePath, localPath);
			log.debug("获取部署主机源文件到本地临时目录成功...");
			
			
			Document doc = XmlTool.read(new File(localPath), "UTF-8");
			Element rootElement = doc.getRootElement();
			List<Element> els = rootElement.elements();
			 for(Element temp : els){
				 String name = temp.attributeValue("name");
				 if(name.equals(attrName)){
					 resultMap.put("name", name);
					 resultMap.put("version", temp.attributeValue("version"));
					 resultMap.put("category", temp.attributeValue("category"));
					 resultMap.put("desc", temp.attributeValue("desc"));
					 String soFile = temp.getStringValue();
					 resultMap.put("so", soFile); 
					 
					 //查询集群部署主机列表
					 Map<String,Object> queryMap = new HashMap<String,Object>();
					 queryMap.put("CLUSTER_ID",  clusterId);
					 queryMap.put("CLUSTER_TYPE",  topologyType);
					 List<HashMap<String,Object>> list = coreService.queryForList2New("deployHome.queryHostByDeploy", queryMap, dbKey);
					 if(!BlankUtil.isBlank(list)){
						 String soFilepath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath("V" + version);
						 String command = "cd " + soFilepath +Constant.BIN +";"
								 + "export LD_LIBRARY_PATH=" + soFilepath +"lib:$LD_LIBRARY_PATH;"
								 + "./plugininfo " + soFilepath + "plugin/" + soFile.substring(soFile.lastIndexOf("/")+1,soFile.length());
						 
					     Map<String, Object> tempMap = list.get(0);
					     String hostIp = StringTool.object2String(tempMap.get("HOST_IP"));
					     String hostUser = StringTool.object2String(tempMap.get("SSH_USER"));
					     String hostPwd = DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD")));
    					 ShellUtils cmdUtil = new ShellUtils(hostIp, hostUser, hostPwd);
					     log.debug("获取插件XML文件， 执行命令: " + command);
					     
    					 String resultStr =  cmdUtil.execMsg(command); 
    					 log.debug("获取插件XML文件， 命令执行结果: " + resultStr);
    					 
    					 if(resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >=0 
    							 || resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >=0){
    						 resultStr = resultStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
    					}
    					 
    					 resultMap.put("soDesc", "插件信息获取失败, 失败原因: \n" + resultStr);
					 } else {    
						 String result = "@组件名称：libReqBaseFlow.so.x.x.x                                                                     \n"+
    							 "@创建日期：2016/6/15                                                                                   \n"+
    							 "@修改日期：2016/12/10                                                                                  \n"+
    							 "@当前版本：1.0.0                                                                                        \n"+
    							 "@功能描述：流程组件，反序列化ccr消息，调用请求流程功能组件                                             \n"+
    							 "@版本历史：                                                                                            \n"+
    							 "@1.0.0：                                                                                                \n"+
    							 "---2016/6/24： reqbolt编译通过                                                                         \n"+
    							 "---2016/7/11： 参数读取配置文件修改                                                                    \n"+
    							 "---2016/7/28:  位置变更截单                                                                            \n"+
    							 "---2016/8/15:  M2DB，增加ACE接口                                                                       \n"+
    							 "---2016/8/18:  日志优化成功，查找用户资料修改                                                          \n"+
    							 "---2016/8/22:  请求实名制发送CCA topic支持可配置，用户管理流程返回结果码判断，用户管理流程状态修改     \n"+
    							 "---2016/9/22:  号码跟踪问题修改                                                                        \n"+
    							 "---2016/10/19: term上报不存在的rg走出单流程支撑                                                        \n"+
    							 "---2016/10/21: 统一添加UHead消息头                                                                     \n"+
    							 "---2016/11/30: 多个SP对应一个top图，将CCA的topic放入消息体，传到后端                                   \n"+
    							 "---2016/12/06: decode异常捕获                                                                          \n"+
    							 "---2016/12/08: 免费号码流程未查主产品实例表问题                                                        \n"+
    							 "---2016/12/10: 解决异常时内存泄漏问题                                                                  \n";
						 resultMap.put("soDesc", result);
					 }
					 break;
				 }
			 }
		} catch (Exception e) {
			log.error("获取插件XML文件内容失败， 失败原因:", e);
			throw e;
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return resultMap;
	}
}
