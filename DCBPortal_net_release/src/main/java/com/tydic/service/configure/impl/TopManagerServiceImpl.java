package com.tydic.service.configure.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import backtype.storm.generated.TaskSummary;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.utils.NimbusClient;

import com.alibaba.jstorm.ui.utils.UIUtils;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.common.BusParamsHelper;
import com.tydic.service.configure.TopManagerService;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.SftpTran;
import com.tydic.util.ftp.Trans;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [TopManagerServiceImpl]     
  * @Description:  [Topology重新负载实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:15:44]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:15:44]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class TopManagerServiceImpl implements TopManagerService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(TopManagerServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Autowired
	CoreService coreService;
	
	/**
	 * Topology重新负载
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回值
	 */
	@Override
	public Map<String, Object> topRebalanceReload(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("重新负载， 参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String, Object> returnMap = new HashMap<String, Object>();

		//业务主集群ID
		String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
		//当前业务集群ID
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		//获取业务集群类型
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		//当前程序编码
		String programCode = StringTool.object2String(params.get("PROGRAM_CODE"));
		//当前程序版本
		String version = StringTool.object2String(params.get("VERSION"));
		//当前程序版本目录
		String versionDir = FileTool.exactPath("V" + StringTool.object2String(params.get("VERSION")));
		//配置文件
		String configFile = StringTool.object2String(params.get("CONFIG_FILE"));
		
		//获取当前业务集群部署路径
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("获取业务集群信息失败, 请检查！");
		}
		//业务组件部署根目录
		String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		
		//查询业务集群关联的Jstorm组件集群，获取到组件Nimbus
		Map<String, Object> nimbusParams = new HashMap<String, Object>();
		nimbusParams.put("CLUSTER_ID", clusterId);
		List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", nimbusParams, dbKey);
		if (BlankUtil.isBlank(nimbusList)) {
			throw new RuntimeException("该业务集群绑定的组件集群无运行的Nimbus主机");
		}
		
		String resultStr = "";
		try{
			//for (int i=0; i<nimbusList.size(); i++) {
				//获取Nimbus运行版本
				HashMap<String,Object> nimbusMap = nimbusList.get(0);
				//获取Nimbus部署版本
				String nimbusVersion = StringTool.object2String(nimbusMap.get("VERSION"));
				//获取Jstorm组件部署根目录
				String jstormDeployPath = StringTool.object2String(nimbusMap.get("CLUSTER_DEPLOY_PATH"));
				//获取Nimbus部署版本
				String envHome = BusParamsHelper.getEnvParam(coreService, busClusterId, jstormDeployPath, nimbusVersion, appRootPath, versionDir, dbKey);
				
				returnMap.put("info", "在主机【" + nimbusMap.get("HOST_IP") + "】上进行REBALANCE!\n");
				
				String topologyName = programCode + "-" + version;
				// 组装命令
				String cmd = Constant.TOP_REBALANCE_SH;
				String paramValue = topologyName+" -r ";
				
				//获取配置文件目录
				//topologyDCM-V7.0.0.0 -r /public/bp/DCBPortal_test/tools/env/1.8.0/jstorm/bin/V7.0.0.0/topologyDCM.conf
//				if(!BlankUtil.isBlank(fileName)){
//					paramValue =  paramValue + jstormDeployPath + Constant.Tools + Constant.ENV + FileTool.exactPath(nimbusVersion) + 
//					          Constant.JSTORM_DIR + Constant.BIN + FileTool.exactPath(versionDir) + fileName;
//				}
				
				paramValue = paramValue + (appRootPath + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG_DIR + configFile) + " " + envHome;
				
				//获取当前运行Jstorm bin目录
				///public/bp/DCBPortal_test/tools/env/1.8.0/jstorm/bin/
				String jstormBinPath = FileTool.exactPath(jstormDeployPath) + Constant.Tools + Constant.ENV + FileTool.exactPath(nimbusVersion) 
						+ Constant.JSTORM_DIR + Constant.BIN;
				//source ~/.bash_profile;cd /public/bp/DCBPortal_test/tools/env/1.8.0/jstorm/bin/;chmod +x reblance.sh;./reblance.sh "topologyDCM-V7.0.0.0 -r /public/bp/DCBPortal_test/tools/env/1.8.0/jstorm/bin/V7.0.0.0/topologyDCM.conf"
				String execCmd = MessageFormat.format(cmd, jstormBinPath, "\""+paramValue+"\"");
				
				// 远程主机登录ssh
				String sshIp = StringTool.object2String(nimbusMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(nimbusMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(nimbusMap.get("SSH_PASSWD")));
				ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPwd);
				log.debug("重新负载执行命令: " + execCmd);
				
				//rebalance之前主机列表
				List<TaskSummary> beforeSData = null;
				//集群名称
				String clusterName = StringTool.object2String(nimbusMap.get("CLUSTER_CODE"));
				//String clusterName = getClusterName(clusterId, clusterType, dbKey);
				if (!BlankUtil.isBlank(clusterName)) {
					//转译特殊字符
					clusterName = StringEscapeUtils.escapeHtml(clusterName);
					try{
						NimbusClient client = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
						TopologyInfo topologyInfo = client.getClient().getTopologyInfoByName(topologyName);
						beforeSData = topologyInfo.get_tasks();
					}catch(Exception e){
						log.warn("Rebalance执行命令前检查task报错 ：获取【"+topologyName+"】task失败" );
						beforeSData = new ArrayList<TaskSummary>();
					}
					log.debug("负载之前，TASKS列表：" + beforeSData.toString());
				}else{
					log.error("Rebalance执行命令报错 ：获取集群名称失败..." );
				}
				
				// 执行命令返回结果
				resultStr = cmdUtil.execMsg(execCmd);
				log.debug("Rebalance脚本执行结果： " + resultStr);
			    
			    if (resultStr.indexOf("Successfully submit command rebalance") != -1 && !BlankUtil.isBlank(clusterName)) {
			    	log.debug("重新负载成功...");
			    	
					Thread.sleep(20*1000);
					List<TaskSummary> afterSData = null;
					try{
						NimbusClient client = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
						TopologyInfo topologyInfo = client.getClient().getTopologyInfoByName(topologyName);
						afterSData = topologyInfo.get_tasks();
						log.debug("负载之后， TASKS列表: " + afterSData.toString());
					}catch(Exception e){
						log.warn("Rebalance执行命令后检查task报错 ：获取【"+topologyName+"】task失败" );
						afterSData = new ArrayList<TaskSummary>();
					}
					
					//rebalance前后都存在的主机
					List<String> beforeList = new ArrayList<String>();
					for (int i=0; i<beforeSData.size(); i++) {
						String beforeHost = beforeSData.get(i).get_host();
						beforeList.add(beforeHost);
					}
					
					List<String> afterList = new ArrayList<String>();
					for (int i=0; i<afterSData.size(); i++) {
						String afterHost = afterSData.get(i).get_host();
						afterList.add(afterHost);
					}
					
					List<String> delNodeList = new ArrayList<String>();
					for (int i=0; i<beforeList.size(); i++) {
						Boolean isExist = false;
						String beforeHost = beforeList.get(i);
						for (int j=0; j<afterList.size(); j++) {
							if (beforeHost.equals(afterList.get(j))) {
								isExist = true;
								break;
							}
						}
						if (!isExist && !delNodeList.contains(beforeHost)) {
							delNodeList.add(beforeHost);
						}
					}
					log.info("删除节点:" + delNodeList.toString());
					
					List<String> addNodeList = new ArrayList<String>();
					for (int i=0; i<afterList.size(); i++) {
						Boolean isExist = false;
						String afterHost = afterList.get(i);
						for (int j=0; j<beforeList.size(); j++) {
							if (afterHost.equals(beforeList.get(j))) {
								isExist = true;
								break;
							}
						}
						if (!isExist && !addNodeList.contains(afterHost)) {
							addNodeList.add(afterHost);
						}
					}
					log.info("新增节点: " + addNodeList.toString());
					
					if (!BlankUtil.isBlank(delNodeList) || !BlankUtil.isBlank(addNodeList)) {
						resultStr += "重新负载后节点变化：";
						for (int i=0; i<delNodeList.size(); i++) {
							resultStr += delNodeList.get(i) + "[移除]，";
							if (i !=0 && i%6 == 0) {
								resultStr+= "\n";
							}
						}
						for (int i=0; i<addNodeList.size(); i++) {
							resultStr += addNodeList.get(i) + "[新增]，";
							if (i !=0 && i%6 == 0) {
								resultStr+= "\n";
							}
						}
						resultStr = resultStr.substring(0, resultStr.length() - 1);
					}
			    }
			    returnMap.put("reason",resultStr);
		}catch(Exception e){
			log.error("Rebalance执行失败", e);
			throw e;
			//returnMap.put("reason", "重新负载失败, 失败原因: " + e.getMessage());
		}
		return returnMap;
	}
	
	/**
	 * 获取集群名称
	 * @param clusterId 集群ID
	 * @param clusterType 集群类型
	 * @param dbKey 数据库Key
	 * @return String 集群名称
	 */
	private String getClusterName(String clusterId, String clusterType, String dbKey) {
		log.debug("重Nimbus主机获取ZK配置根目录， 集群ID: " + clusterId + ", 集群类型: " + clusterType);
		//集群名称
		String clusterName = "";
		try {
			// 获取当前业务关联的Nimbus信息，重Nimbus主机上获取ZK集群信息
			Map<String, Object> nimbusParams = new HashMap<String, Object>();
			nimbusParams.put("CLUSTER_ID", clusterId);
			List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", nimbusParams, dbKey);
			if (BlankUtil.isBlank(nimbusList)) {
				throw new RuntimeException("该业务集群绑定的组件集群无运行的Nimbus主机");
			}
			for (int i = 0; i < nimbusList.size(); i++) {
				try {
					String hostIp = StringTool.object2String(nimbusList.get(i).get("HOST_IP"));
					String sshPort = StringTool.object2String(nimbusList.get(i).get("SSH_PORT"));
					String sshUser = StringTool.object2String(nimbusList.get(i).get("SSH_USER"));
					String sshPwd = StringTool.object2String(nimbusList.get(i).get("SSH_PASSWD"));
					String yamlPath = StringTool.object2String(nimbusList.get(i).get("FILE_PATH"));
					if (!BlankUtil.isBlank(sshPwd)) {
						sshPwd = DesTool.dec(sshPwd);
					} else {
						log.debug("请配置主机密码, 主机: " + hostIp);
						continue;
					}
					
					//登录Nimbus所在主机解析Yaml配置文件
					Trans trans = new SftpTran(hostIp, Integer.parseInt(sshPort), sshUser, sshPwd, 8000);
					trans.login();
					InputStream in = trans.get(yamlPath);
					Yaml yaml = new Yaml();
					Map<String, Object> remoteYamlMap = (Map<String, Object>) yaml.load(in);
					in.close();
					trans.completePendingCommand();
					Object zkRoot = remoteYamlMap.get("storm.zookeeper.root");
					clusterName = StringTool.object2String(zkRoot).replaceAll("/", "");
					break;
				} catch (Exception e) {
					log.error("解析Jstorm配置文件失败， 失败原因:", e);
				}
			}
		} catch (Exception e) {
			log.error("解析Yaml配置文件失败", e);
		}
		return clusterName;
	}
}
