package com.tydic.service.configure.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.TopicConfigService;
import com.tydic.util.Constant;
import com.tydic.util.SSHRemoteCmdUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.XmlTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [TopicConfigServiceImpl]     
  * @Description:  [Topic配置管理实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:15:23]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:15:23]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class TopicConfigServiceImpl implements TopicConfigService {

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(TopicConfigServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;
	
	/**
	 * zk连接超时时间
	 */
	private static final int TIME_OUT = 10000;
	
	/**
	 * 连接远程主机Encode编码
	 */
	private static final String DEFAULT_ENCODE = "UTF-8";
	
	/**
	 * 文件分割符
	 */
	private static final String FILE_SEPARATOR = "/"; //File.separator;
	
	/**
	 * 方法执行结果编码
	 */
	private static final String EXEC_RESULT_0 = "0";
	
	
	/**
	 * 添加Topic配置信息
	 * 
	 * @param params 参数
	 */
	@Override
	public Map<String, Object> addTopicConfig(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("添加Topic信息，参数为:" + params + ", dbKey: " + dbKey);
		
		//判断TopicName是否存在
		List<HashMap<String, Object>> rstList = coreService.queryForList2New("topicConfig.queryTopicConfigListByName", params, dbKey);
		if (!BlankUtil.isBlank(rstList)) {
			log.debug("Topic名称已经存在,topName:" + params.get("topicName"));
			throw new RuntimeException("Topic名称已经存在，请重新输入");
		}
		
		//业务主集群ID
		String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
		//rocketMq集群ID
		String rqClusterId = StringTool.object2String(params.get("rq_cluster"));
		//rocketMq集群版本
		String rqClusterVersion = StringTool.object2String(params.get("rq_version"));
		
		//获取主机登录SSH账号
		Map<String, Object> rqMap = new HashMap<String, Object>();
		rqMap.put("CLUSTER_ID", rqClusterId);
		rqMap.put("CLUSTER_TYPE", Constant.ROCKETMQ);
		rqMap.put("VERSION", rqClusterVersion);
		List<HashMap<String, Object>> hostList = coreService.queryForList2New("topicConfig.queryRocketMqHostList", rqMap, dbKey);
		if (BlankUtil.isBlank(hostList)) {
			throw new RuntimeException("没有部署状态的RocketMQ主机信息，请先配置部署RocketMQ主机");
		}
		HashMap<String, Object> hostSingleMap = hostList.get(0);
		String sshUserName = StringTool.object2String(hostSingleMap.get("SSH_USER"));
		String sshUserPwd = DesTool.dec(StringTool.object2String(hostSingleMap.get("SSH_PASSWD")));
		String sshHostIp = StringTool.object2String(hostSingleMap.get("HOST_IP"));
//		String sshUserName = "ah_test";
//		String sshUserPwd = "ah_test6";
//		String sshHostIp = "192.168.161.221";
		
		//获取RocketMq主机及配置信息
		String topicName = StringTool.object2String(params.get("topicName"));
		String rocketMqIp = StringTool.object2String(params.get("rq_ip"));
		String rocketMqPort = StringTool.object2String(params.get("rq_port"));
		String rocketMqHost = rocketMqIp + ":" + rocketMqPort;
//		String rocketMqHost = "192.168.161.221:9876";
		String rocketMqW = StringTool.object2String(params.get("w_num"));
		String rocketMqR = StringTool.object2String(params.get("r_num"));
		
		//获取mqadmin程序所在路径
		//获取当前集群部署根目录
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", rqClusterId);
		queryClusterMap.put("CLUSTER_TYPE", Constant.ROCKETMQ);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("集群信息查询失败, 请检查！");
		}
		//组件部署根目录
		final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		//rocketMq路径
		String appPath = FileTool.exactPath(appRootPath) + Constant.Tools + Constant.ENV + FileTool.exactPath(rqClusterVersion) + Constant.ROCKETMQ_DIR + Constant.BIN;
		
		//判断ZK是否需要集群
//		Map<String, Object> queryParams = new HashMap<String, Object>();
//		queryParams.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
//		queryParams.put("BUS_CLUSTER_ID", busClusterId);
//		List<HashMap<String, Object>> zkDeployList = coreService.queryForList2New("topicConfig.queryDeployIsClusterByCode", queryParams, dbKey);
//		if (BlankUtil.isBlank(zkDeployList)) {
//			throw new RuntimeException("请先配置Zookeeper主机");
//		}
//		params.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
//		params.put("CLUSTER_ID", zkDeployList.get(0).get("CLUSTER_ID"));
//		//添加ZK数据
//		List<HashMap<String, Object>> zkList = coreService.queryForList2New("topicConfig.queryZkList", params, dbKey);
//		if (BlankUtil.isBlank(zkList)) {
//			throw new RuntimeException("请先配置Zookeeper主机");
//		}
//		
//		// 获取 文件ftp服务 器
//		String clusterCode = StringTool.object2String(zkDeployList.get(0).get("CLUSTER_CODE"));
//		Map<String, Object> deployMap = new HashMap<String, Object>();
//		deployMap.put("GROUP_CODE", "WEB_FTP_CONFIG");
//		List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", deployMap, dbKey);
//		String ftpType = SessionUtil.getConfigValue("FTP_TYPE", ftpList);
//		String ftpUserName = SessionUtil.getConfigValue("FTP_USERNAME",ftpList);
//		String ftpPasswd = SessionUtil.getConfigValue("FTP_PASSWD", ftpList);
//		String ftpIp = SessionUtil.getConfigValue("FTP_IP", ftpList);
//		String ftpPath = SessionUtil.getConfigValue("FTP_ROOT_PATH", ftpList);
//		String zkConfigPath = FileTool.exactPath(ftpPath) + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR + 
//				Constant.ZOOKEEPER_DIR + FileTool.exactPath(clusterCode) + "zoo.cfg";
//		Trans src = FTPUtils.getFtpInstance(ftpIp, ftpUserName, ftpPasswd, ftpType);
//		src.login();
//		InputStream zooStream = src.get(zkConfigPath);
//		BufferedReader reader = new BufferedReader(new InputStreamReader(zooStream));
//		String lineContent = "";
//		//clientPort=22889
//		String clientPort = "";
//		while ((lineContent = reader.readLine()) != null) {
//			if (lineContent.startsWith("clientPort".trim()) && lineContent.indexOf("=") != -1) {
//				clientPort = lineContent.split("=")[1];
//				break;
//			}
//		}
//		log.debug("当前Zookeeper端口: " + clientPort + ", 集群编码: " + clusterCode);
//		if (!BlankUtil.isBlank(clientPort)) {
//			Map updateMap = new HashMap();
//			updateMap.put("PORT", clientPort);
//			updateMap.put("IDS", zkList);
//			coreService.updateObject("deployHome.updateHostDeployPort", updateMap, dbKey);
//			log.debug("修改集群端口成功...");
//			zkList = coreService.queryForList2New("topicConfig.queryZkList", params, dbKey);
//		}
		
		//查询router是否需要集群
//		queryParams = new HashMap<String, Object>();
//		queryParams.put("CLUSTER_TYPE", Constant.ROUTE);
//		queryParams.put("BUS_CLUSTER_ID", busClusterId);
//		List<HashMap<String, Object>> routerDeployList = coreService.queryForList2New("topicConfig.queryDeployIsClusterByCode", queryParams, dbKey);
//		if (BlankUtil.isBlank(routerDeployList)) {
//			throw new RuntimeException("请先配置路由主机");
//		}
//		//获取router组备份主机
//		params.put("CLUSTER_TYPE", Constant.ROUTE);
//		params.put("CLUSTER_ID", routerDeployList.get(0).get("CLUSTER_ID"));
//		List<HashMap<String,Object>> routeHostList=coreService.queryForList2New("topicConfig.queryRouteHostByCluster", params, dbKey);
//		if (BlankUtil.isBlank(routeHostList)) {
//			throw new RuntimeException("请先配置路由主机");
//		}
		
//		HashMap<String, Object> zkSingleMap = null;
		try {
			//执行启动Topic命令
			SSHRemoteCmdUtil cmd = new SSHRemoteCmdUtil(sshHostIp, sshUserName, sshUserPwd, DEFAULT_ENCODE);
			String execCmd = "source ~/.bash_profile;cd " + appPath + ";sh mqadmin updateTopic -c DefaultCluster -n " + rocketMqHost + " -t " + topicName + " -w " + rocketMqW + " -r " + rocketMqR +"";
			log.debug("执行创建Topic命令: " + execCmd);
			String execRst = cmd.execMsgGBK(execCmd);
			log.debug("执行创建Topic命令返回结果: " + execRst);
			
			String queryCmd = "source ~/.bash_profile;cd " + appPath + ";sh mqadmin topicList -n " + rocketMqHost;
			log.debug("查询Topic列表命令: " + queryCmd);
			String queryRst = cmd.execMsgGBK(queryCmd);
			log.debug("执行查询Topic命令返回结果: " + queryRst);
			if (queryRst.indexOf(topicName+"\n") == -1) {
				throw new RuntimeException("Topic创建失败, 当前Topic名称为:" + topicName);
			}
			log.debug("Topic创建成功, 当前topicName: " + topicName);
			
			
			//添加zookeep信息
//			Boolean isAddZkOk = Boolean.FALSE;
//			for (int i=0; i<zkList.size(); i++) {
//				zkSingleMap = zkList.get(i);
//				if (addZk(zkSingleMap, topicName)) {
//					isAddZkOk = Boolean.TRUE;
//					break;
//				}
//			}
//			if (!isAddZkOk) {
//				throw new RuntimeException("Zookeeper添加Topic节点失败");
//			}
//			log.debug("Topic配置添加zookeeper成功, topicName:" + params.get("topicName"));
			
			
			//修改topology配置文件
//			String topologyId = StringTool.object2String(params.get("topology_id"));
//			String topologyGroup = StringTool.object2String(params.get("PROGRAM_GROUP"));
//			if (!BlankUtil.isBlank(topologyId) && !BlankUtil.isBlank(topologyGroup)) {
//				
//				//topology配置文件
//				String fileName= Constant.SP_SWITCH;
//				
//				//临时目录
//				String webRootPath = StringTool.object2String(params.get("webRootPath"));
//				String tmpPath = FileTool.exactPath(webRootPath) + Constant.TMP + System.currentTimeMillis();
//				String localPath = FileTool.exactPath(tmpPath) + fileName;
//				Trans trans = null;
//				
//				//将sp_switch.xml文件同步到ftp主机
//				String ip=SessionUtil.getConfigValue("FTP_IP");
//				String userName = SessionUtil.getConfigValue("FTP_USERNAME");
//				String password = SessionUtil.getConfigValue("FTP_PASSWD");
//				String ftp_type = SessionUtil.getConfigValue("FTP_TYPE");
//				String ftpRootPath = SessionUtil.getConfigValue("FTP_ROOT_PATH");
//				String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
//				String docStr = "";
//				String TopoRootPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(businessPath) + fileName;
//				InputStream inputStream = null;
//				try{
//					trans  = FTPUtils.getFtpInstance(ip, userName, password, ftp_type);
//					trans.login();
//					boolean isExist= true;
//					try {
//						trans.get(TopoRootPath, localPath);
//					} catch (Exception e) {
//						isExist = false;
//					}
//					Document doc = null ;
//					Element topicUpNodeEle = null;
//					Element rootElement = null;
//					if(isExist){
//				    	//将远程主机文件解析成XML文件
//						doc = XmlTool.read(new File(localPath), DEFAULT_ENCODE);
//					    rootElement = XmlTool.getRootElement(doc);
//						Iterator<Element> iter = rootElement.elementIterator();
//						
//						while(iter.hasNext()) {
//							Element childEle = iter.next();
//							if("Up".equalsIgnoreCase(childEle.getName())){
//								if (topologyGroup.equalsIgnoreCase(childEle.attribute("topologyGroup").getValue())) {
//									topicUpNodeEle = childEle;
//									break;
//								}
//							}
//						}
//					}else{
//						//不存在则创建xml文件
//						rootElement = DocumentHelper.createElement("configuration");
//						doc = DocumentHelper.createDocument(rootElement);
//					}
//					
//					//没有找到topology，新创建一个Up节点
//					if (BlankUtil.isBlank(topicUpNodeEle)) {
//						log.debug("topology节点不存在，创建节点，节点topology名称为:" + topologyId);
//						topicUpNodeEle = rootElement.addElement("Up").addAttribute("topologyGroup", topologyGroup).addAttribute("upgradestate", "1");
//						topicUpNodeEle.addAttribute("current", topologyId);
//					}else{
//						String current = topicUpNodeEle.attributeValue("current");
//						if(!current.equals(topologyId)){
//							topicUpNodeEle.addAttribute("next", topologyId);
//						}
//					}
//					
//					//修改XML文件节点属性值
//				/*	if ("M".equalsIgnoreCase(topologyAttr)) {
//						topicUpNodeEle.addAttribute("current", topicName);
//					} else if ("B".equalsIgnoreCase(topologyAttr)) {
//						topicUpNodeEle.addAttribute("next", topicName);
//					}*/
//					//将DOM文件写入到远程主机
//				    docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
//					
//					inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//					trans.put(inputStream, TopoRootPath);
//					log.debug("新增Topic配置，ftp主机【sp_switch.xml】文件修改成功，ftp主机：" + ip);
//				}catch(Exception e){
//					log.error("Topic配置修改【sp_switch.xml】文件失败， 失败信息: ", e);
//					throw new RuntimeException("ftp服务器上的Topology配置文件修改失败或者配置文件【sp_switch.xml】");
//				}finally {
//					if (inputStream != null) {
//						inputStream.close();
//					}
//					if(trans !=null){
//						trans.close();
//					}
//				}
//				
//				//将sp_switch.xml文件同步到路由所在主机
//				for (int i=0; i<routeHostList.size(); i++) {
//					Trans sftClient = null;
//					String hostIp = null;
//					try {
//						Map<String, Object> singleHost = routeHostList.get(i);
//						hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
//						String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
//						String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
//						String deployPath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
//						
//						//分发远程主机路径
//						String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + fileName;
//						sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
//						sftClient.login();
//						inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//						sftClient.put(inputStream, remotePath);
//						log.debug("新增Topic配置，修改路由【sp_switch.xml】文件成功， 当前主机IP：" + hostIp);
//					} catch (Exception e) {
//						log.error("修改配置文件失败, topicName:" + topicName + ", 主机IP: " + hostIp);
//						throw new RuntimeException("Topology配置文件修改失败或者配置文件【sp_switch.xml】不存在, 当前主机: " + hostIp);
//					} finally {
//						if (inputStream != null) {
//							inputStream.close();
//						}
//						if (sftClient != null) {
//							sftClient.close();
//						}
//					}
//				}
//				log.debug("修改topology配置文件成功, topicName:" + topicName);
//			}
			
			
			//添加数据库
			Map<String, String> addParams = new HashMap<>();
			addParams.put("topicName", StringTool.object2String(params.get("topicName")));
			//业务主集群ID
			addParams.put("bus_cluster_id", busClusterId);
			//rocketmq集群ID
			addParams.put("rq_cluster_id", StringTool.object2String(params.get("rq_cluster")));
			//rocketmq集群版本
			addParams.put("rq_version", StringTool.object2String(params.get("rq_version")));
			addParams.put("rq_ip", StringTool.object2String(params.get("rq_ip")));
			addParams.put("rq_port", StringTool.object2String(params.get("rq_port")));
			addParams.put("topicDesc", StringTool.object2String(params.get("topicDesc")));
			addParams.put("w_num", StringTool.object2String(params.get("w_num")));
			addParams.put("r_num", StringTool.object2String(params.get("r_num")));
			addParams.put("program_code", StringTool.object2String(params.get("topology_id")));
			addParams.put("program_attr", StringTool.object2String(params.get("topology_attr")));
			coreService.insertObject("topicConfig.addTopicConfig", addParams, dbKey);
			log.debug("Topic配置添加数据库成功,topicName:" + params.get("topicName"));
		} catch (Exception e) {
			log.error("添加Topic配置信息失败", e);
			//反向操作
			//判断rocketMq启动是否成功，如果启动成功则删除
			//执行启动Topic命令
			SSHRemoteCmdUtil cmd = new SSHRemoteCmdUtil(sshHostIp, sshUserName, sshUserPwd, DEFAULT_ENCODE);
			String execCmd = "source ~/.bash_profile;cd " + appPath + ";sh mqadmin deleteTopic -c DefaultCluster -n " + rocketMqHost + " -t " + topicName;
			String execRst = cmd.execMsgGBK(execCmd);
			log.debug("删除Topic执行结果:" + execRst);
			
			//删除ZK节点
//			if (zkSingleMap != null  && zkSingleMap.isEmpty()) {
//				Boolean rst = delZK(zkSingleMap, topicName);
//				log.debug("反向操作，删除Zookeeper节点信息" + (rst ? "成功" : "失败"));
//			}
			throw e;
		}  
		//用来返回前台标志成功
		Map<String, Object> retMap = new HashMap<>();
		retMap.put("rstCode", EXEC_RESULT_0);
		return retMap;
	}
	
	
	/**
	 * 创建ZK目录
	 * @param fullName
	 * @param zk
	 * @throws Exception
	 */
	private void createNode(String fullName, ZooKeeper zk) throws Exception {
		String [] nodeNames = fullName.split(FILE_SEPARATOR);
		String node = "";
		for (int i=0; i < nodeNames.length; i++) {
			String nodeName = nodeNames[i];
			if (BlankUtil.isBlank(nodeName)) {
				continue;
			}
			node += FILE_SEPARATOR + nodeName;
			Stat stat = zk.exists(node, null);
			if (stat != null) {
				continue;
			}
			zk.create(node, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		log.debug("ZK目录创建成功...");
	}
	
	/**
	 * 删除ZK节点
	 * @param zkSingleMap
	 * @param topicName
	 * @return
	 */
	private Boolean delZK(Map<String, Object> zkSingleMap, String topicName) {
		String zkPath = "/brokers/mq/address/";
		String topicPath = "/brokers/mq/topic/";
		ZooKeeper zkeeper = null;
		Boolean rst = Boolean.TRUE;
		try {
			//连接ZK主机信息
			String hostIP = StringTool.object2String(zkSingleMap.get("HOST_IP"));
			String hostPort = StringTool.object2String(zkSingleMap.get("HOST_PORT"));
			String zkHost = hostIP + ":" + hostPort;
			
			zkeeper = new ZooKeeper(zkHost, TIME_OUT, null);
			
			//创建address信息
			String zkAddPath = zkPath + zkHost;
			Stat stat = zkeeper.exists(zkAddPath, null);
			if (stat != null) {
				zkeeper.delete(zkAddPath, stat.getVersion());
			}
			
			//创建topic信息
			String topicAddPath = topicPath + topicName;
			stat = zkeeper.exists(topicAddPath, null);
			if (stat != null) {
				zkeeper.delete(topicAddPath, stat.getVersion());
			}
		} catch (Exception e) {
			rst = Boolean.FALSE;
			log.error("删除Zookeeper节点信息失败", e);
		} finally {
			if (zkeeper != null) {
				try {
					zkeeper.close();
				} catch (InterruptedException e) {
					log.error("zookeeper关闭失败", e);
				}
			}
		}
		return rst;
	}
	
	/**
	 * 添加Zookeeper信息
	 * @param zkSingleMap
	 * @param topicName
	 * @return
	 */
	private Boolean addZk(Map<String, Object> zkSingleMap, String topicName) {
		String zkPath = "/brokers/mq/address/";
		String topicPath = "/brokers/mq/topic/";
		ZooKeeper zkeeper = null;
		Boolean rst = Boolean.TRUE;
		try {
			//连接ZK主机信息
			String hostIP = StringTool.object2String(zkSingleMap.get("HOST_IP"));
			String hostPort = StringTool.object2String(zkSingleMap.get("HOST_PORT"));
			String zkHost = hostIP + ":" + hostPort;
			
			zkeeper = new ZooKeeper(zkHost, TIME_OUT, null);
			
			//创建address信息
			String zkAddPath = zkPath + zkHost;
			createNode(zkAddPath, zkeeper);
			
			//创建topic信息
			String topicAddPath = topicPath + topicName;
			createNode(topicAddPath, zkeeper);
		} catch (Exception e) {
			rst = Boolean.FALSE;
			log.error("添加Zookeeper信息失败", e);
		} finally {
			if (zkeeper != null) {
				try {
					zkeeper.close();
				} catch (InterruptedException e) {
					log.error("zookeeper关闭失败", e);
				}
			}
		}
		return rst;
	}

	/**
	 * 删除Topic配置信息
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map
	 */
	@Override
	public Map<String, Object> delTopicConfig(Map<String, Object> params, String dbKey) {
		log.debug("删除Topic配置，参数:" + params + ", dbKey: " + dbKey);
		
		//获取主机登录SSH账号
		try {
			FtpDto ftpDto = SessionUtil.getFtpParams();
			Map<String, Object> rocketmqMap = new HashMap<String, Object>();
			rocketmqMap.put("RQ_CLUSTER_ID", StringTool.object2String(params.get("RQ_CLUSTER_ID")));
			rocketmqMap.put("BUS_CLUSTER_ID", StringTool.object2String(params.get("BUS_CLUSTER_ID")));
			List<HashMap<String, Object>> hostList = coreService.queryForList2New("topicConfig.queryTopicHostListByName", rocketmqMap, dbKey);
			if (BlankUtil.isBlank(hostList)) {
				throw new RuntimeException("没有部署状态的RocketMQ主机信息，请先配置部署RocketMQ主机");
			}
			HashMap<String, Object> hostSingleMap = hostList.get(0);
			String sshUserName = StringTool.object2String(hostSingleMap.get("SSH_USER"));
			String sshUserPwd = DesTool.dec(StringTool.object2String(hostSingleMap.get("SSH_PASSWD")));
			String sshHostIp = StringTool.object2String(hostSingleMap.get("HOST_IP"));
			//String sshUserName = "ah_test";
			//String sshUserPwd = "ah_test6";
			//String sshHostIp = "192.168.161.221";
			//临时目录
			String tempPath = StringTool.object2String(params.get("webRootPath"));
			String tmpPath = FileTool.exactPath(tempPath) + Constant.TMP + System.currentTimeMillis();
			
			//获取RocketMq主机及配置信息
			String topicName = StringTool.object2String(params.get("topicName"));
			String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
			String rqClusterId = StringTool.object2String(params.get("RQ_CLUSTER_ID"));
			String rqVersion = StringTool.object2String(params.get("RQ_VERSION"));
			String rocketMqIp = StringTool.object2String(params.get("rq_ip"));
			String rocketMqPort = StringTool.object2String(params.get("rq_port"));
			String rocketMqHost = rocketMqIp + ":" + rocketMqPort;
			//String rocketMqHost = "192.168.161.221:9876";
			
			//获取当前rocketMq集群部署路径以及部署版本
			//获取当前集群部署根目录
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", rqClusterId);
			queryClusterMap.put("CLUSTER_TYPE", Constant.ROCKETMQ);
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
			if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				throw new RuntimeException("集群信息查询失败, 请检查！");
			}
			//组件部署根目录
			final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			
			//远程主机部署路径
			String appPath = FileTool.exactPath(appRootPath) + Constant.Tools + Constant.ENV +FileTool.exactPath(rqVersion) + Constant.ROCKETMQ_DIR + Constant.BIN;
			
			//查询Topic进程是否存在，如果存在则删除
			SSHRemoteCmdUtil cmd = new SSHRemoteCmdUtil(sshHostIp, sshUserName, sshUserPwd, DEFAULT_ENCODE);
			String queryCmd = "source ~/.bash_profile;cd " + appPath + ";sh mqadmin topicList -n " + rocketMqHost;
			log.debug("删除Topic, 查询Topic命令: " + queryCmd);
			
			String queryRst = cmd.execMsgGBK(queryCmd);
			log.debug("删除Topic, 查询Topic命令执行结果: " + queryRst);
			
			if (queryRst.indexOf(topicName+"\n") != -1) {
				String execCmd = "source ~/.bash_profile;cd " + appPath + ";sh mqadmin deleteTopic -c DefaultCluster -n " + rocketMqHost + " -t " + topicName;
				log.debug("删除Topic, 执行命令: " + execCmd);
				
				String execRst = cmd.execMsgGBK(execCmd);
				log.debug("删除Topic, 执行命令结果: " + execRst);
			}
			
			//判断ZK是否需要集群
			Map<String, Object> queryParams = new HashMap<String, Object>();
//			queryParams.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
//			queryParams.put("BUS_CLUSTER_ID", busClusterId);
//			List<HashMap<String, Object>> zkDeployList = coreService.queryForList2New("topicConfig.queryDeployIsClusterByCode", queryParams, dbKey);
//			if (BlankUtil.isBlank(zkDeployList)) {
//				throw new RuntimeException("请先配置Zookeeper主机");
//			}
//			params.put("CLUSTER_ID", zkDeployList.get(0).get("CLUSTER_ID"));
//			params.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
//			
//			//添加ZK数据
//			List<HashMap<String, Object>> zkList = coreService.queryForList2New("topicConfig.queryZkList", params, dbKey);
//			if (BlankUtil.isBlank(zkList)) {
//				throw new RuntimeException("请先配置Zookeeper主机");
//			}
//			
//			//添加zookeep信息
//			HashMap<String, Object> zkSingleMap = null;
//			Boolean isDelZkOk = Boolean.FALSE;
//			for (int i=0; i<zkList.size(); i++) {
//				zkSingleMap = zkList.get(i);
//				if (delZK(zkSingleMap, topicName)) {
//					isDelZkOk = Boolean.TRUE;
//					break;
//				}
//			}
//			if (!isDelZkOk) {
//				throw new RuntimeException("zookeeper节点数据删除失败, topicName:" + "topicName");
//			}
//			log.debug("删除Zookeeper节点数据成功, topicName:" + topicName);
			
			
			//删除Topic数据需要将sp_switch.xml文件中对应节点给置为空
			String topologyId = StringTool.object2String(params.get("topology_id"));
			String topologyAttr = StringTool.object2String(params.get("topology_attr"));
			if (!BlankUtil.isBlank(topologyId)) {
				//查询router是否需要集群
				queryParams = new HashMap<String, Object>();
				queryParams.put("CLUSTER_TYPE", Constant.ROUTE);
				queryParams.put("BUS_CLUSTER_ID", busClusterId);
				List<HashMap<String, Object>> routerDeployList = coreService.queryForList2New("topicConfig.queryDeployIsClusterByCode", queryParams, dbKey);
				if (BlankUtil.isBlank(routerDeployList)) {
					throw new RuntimeException("请先配置路由主机");
				}
				//获取router组备份主机
				params.put("CLUSTER_TYPE", Constant.ROUTE);
				params.put("CLUSTER_ID", routerDeployList.get(0).get("CLUSTER_ID"));
				List<HashMap<String,Object>> routeHostList=coreService.queryForList2New("topicConfig.queryRouteHostByCluster", params, dbKey);
				if (BlankUtil.isBlank(routeHostList)) {
					throw new RuntimeException("请先配置路由主机");
				}
				
				//topology配置文件
				String fileName= Constant.SP_SWITCH;
				
				Trans trans = null;
				Boolean isUpdated = Boolean.FALSE;
				String localPath = FileTool.exactPath(tmpPath) + fileName;
				for (int i=0; i<routeHostList.size(); i++) {
					Trans sftClient = null;
					InputStream inputStream = null;
					try {
						Map<String, Object> singleHost = routeHostList.get(i);
						String hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
						String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
						String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
						String clusterDeployPath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
						
						//远程主机sp_switch.xml文件路径
						String remotePath = FileTool.exactPath(clusterDeployPath) + Constant.BUSS + fileName;
						sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
						sftClient.login();
						sftClient.get(remotePath, localPath);
						
						//将远程主机文件解析成XML文件
						Document doc = XmlTool.read(new File(localPath), DEFAULT_ENCODE);
						Element rootElement = XmlTool.getRootElement(doc);
						Iterator<Element> iter = rootElement.elementIterator();
						Element topicUpNodeEle = null;
						while(iter.hasNext()) {
							Element childEle = iter.next();
							if("Up".equalsIgnoreCase(childEle.getName())){
								if (topologyId.equalsIgnoreCase(childEle.attribute("topology").getValue())) {
									topicUpNodeEle = childEle;
									break;
								}
							}
						}
						
						//没有找到topology，新创建一个Up节点
						if (BlankUtil.isBlank(topicUpNodeEle)) {
							log.debug("topology节点不存在，直接跳过，节点topology名称为:" + topologyId);
							continue;
						}
						
						//修改XML文件节点属性值
						if ("M".equalsIgnoreCase(topologyAttr)) {
							topicUpNodeEle.addAttribute("current", "");
						} else if ("B".equalsIgnoreCase(topologyAttr)) {
							topicUpNodeEle.addAttribute("next", "");
						}
						//将DOM文件写入到远程主机
						String docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
						inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
						sftClient.put(inputStream, remotePath);
						log.debug("删除Topic配置，修改路由【sp_switch.xml】文件成功， 当前主机IP：" + hostIp);
						
						if (!isUpdated) {
							isUpdated = Boolean.TRUE;
							//将sp_switch.xml文件同步到ftp主机

							String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
							
							//部署主机sp_switch.xml文件位置
							String deployPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessPath) + fileName;
							
							trans  = FTPUtils.getFtpInstance(ftpDto);
							trans.login();
							inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
							trans.put(inputStream, deployPath);
							log.debug("删除Topic配置，ftp主机【sp_switch.xml】文件修改成功，ftp主机：" + ftpDto.getHostIp());
						}
					} catch (Exception e) {
						log.error("Topology配置文件修改失败或者配置文件【sp_switch.xml】不存在, topicName:" + topicName);
						throw new RuntimeException("Topology配置文件修改失败或者配置文件【sp_switch.xml】不存在");
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						if (sftClient != null) {
							sftClient.close();
						}
					}
				}
				log.debug("修改topology配置文件成功, topicName:" + topicName);
			}
			
			//删除数据库表数据
			Map<String, String> delParams = new HashMap<>();
			delParams.put("topicName", topicName);
			coreService.deleteObject("topicConfig.delTopicConfig", delParams, dbKey);
			log.debug("删除Topic数据成功, topicName:" + topicName);
		} catch (RuntimeException e) {
			log.error("删除Topic配置信息失败", e);
			throw e;
		} catch (Exception e) {
			log.error("删除Topic配置信息失败", e);
			throw new RuntimeException("删除Topic配置信息失败!");
		}
		//用来返回前台标志成功
		Map<String, Object> retMap = new HashMap<>();
		retMap.put("rstCode", EXEC_RESULT_0);
		return retMap;
	}

}
