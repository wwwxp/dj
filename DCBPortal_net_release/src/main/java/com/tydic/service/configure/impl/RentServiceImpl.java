package com.tydic.service.configure.impl;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.common.BusParamsHelper;
import com.tydic.service.configure.RentService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.FileUtil;
import com.tydic.util.SSHRemoteCmdUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [RentServiceImpl]     
  * @Description:  [租费业务程序启停、状态检查实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:00:36]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:00:36]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class RentServiceImpl implements RentService {
	@Resource
	private CoreService coreService;

	private static Logger log = Logger.getLogger(RentServiceImpl.class);

	/**
	 * 租费业务启停
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回值对象
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateRunAndStopHost(Map<String,Object> param, String dbKey) throws Exception {
		log.debug("租费程序启停， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//最终执行返回结果Map对象
		Map<String, Object> returnMap = new HashMap<String,Object>();
		//前台业务参数
		Map<String, Object> queryParam = (Map<String, Object>) param.get("queryParam");
		//业务主集群ID
		String busClusterId = StringTool.object2String(queryParam.get("BUS_CLUSTER_ID"));
		//获取当前业务集群ID
		String clusterId = StringTool.object2String(queryParam.get("CLUSTER_ID"));
		//获取当前业务集群类型
		String clusterType = StringTool.object2String(queryParam.get("CLUSTER_TYPE"));
		//获取当前版本号目录
		String versionDir = FileTool.exactPath("V" + StringTool.object2String(queryParam.get("versionDir")));
		
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

		//查询当前业务集群可部署主机是否达到最大值，如果达到最大值则不能在部署
		String currentFlag = StringTool.object2String(param.get("flag"));
		if(BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
			//查询最大数和正在运行数
			Map<String,Object> runCountMap=coreService.queryForObject2New("taskProgram.queryRunningProgramCount", queryParam, dbKey);
			if(runCountMap != null && !runCountMap.isEmpty()){
				//最大个数
				long runingCout = Long.parseLong(StringTool.object2String(runCountMap.get("SUM")));
				//正在运行数
				long maxCount = Long.parseLong(StringTool.object2String(runCountMap.get("MAX_COUNT")));
				if(runingCout >= maxCount){
					throw new RuntimeException("同一个topology最多只能启【"+maxCount+"】个，请检查！");
				}
			}
		}
						
		// 脚本执行成功标识符
		boolean flag = true;
		//脚本执行返回结果
		String resultStr = "";
		try {
			
			//查询业务集群关联的Jstorm组件集群，获取到组件Nimbus
			Map<String, Object> nimbusParams = new HashMap<String, Object>();
			nimbusParams.put("CLUSTER_ID", clusterId);
			List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", nimbusParams, dbKey);
			if (BlankUtil.isBlank(nimbusList)) {
				throw new RuntimeException("该业务集群绑定的组件集群无运行的Nimbus主机!");
			}
			
			// 查询出数据库得到脚本名称
			String shName = StringTool.object2String(queryParam.get("SCRIPT_SH_NAME"));
			String programCode = StringTool.object2String(queryParam.get("PROGRAM_CODE"));

			// 更新程序Task表数据
			Map<String, String> updateMap = new HashMap<String, String>();
			updateMap.put("TASK_ID", StringTool.object2String(queryParam.get("TASK_ID")));
			
			//遍历集群所有部署的Nimbus列表，提交Topology图
																

			for(int i = 0; i < nimbusList.size(); i ++){
				HashMap<String,Object> nimbusMap = nimbusList.get(i);
				
				//获取Jstorm组件部署根目录
				String jstormDeployPath = StringTool.object2String(nimbusMap.get("CLUSTER_DEPLOY_PATH"));
				//获取Nimbus部署版本
				String nimbusVersion = StringTool.object2String(nimbusMap.get("VERSION"));
				//获取rent启动参数
				String envHome = BusParamsHelper.getEnvParam(coreService, busClusterId, jstormDeployPath, nimbusVersion, appRootPath, versionDir, dbKey);
				// 组装命令
				String execCmd = Constant.SERVICE_SH;
				
				// 远程主机登录ssh
				String sshIp = StringTool.object2String(nimbusMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(nimbusMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(nimbusMap.get("SSH_PASSWD")));
				SSHRemoteCmdUtil cmdUtil = new SSHRemoteCmdUtil(sshIp, sshUser, sshPwd, null);
				
				//获取Topology名称
				String currentAction = "";
				String topologyName = programCode + "-" + StringTool.object2String(queryParam.get("versionDir"));
				queryParam.put("PROGRAM_NAME", topologyName);
	
							  
				if(BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
					currentAction = topologyName + " start";
					updateMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
				} else if(BusinessConstant.PARAMS_STOP_FLAG.equals(currentFlag)) {
					currentAction = topologyName + " stop";
					updateMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
				}
				
				//rent启停参数，分别为：业务版本路径、业务执行脚本、业务执行操作、业务执行参数
				String busPath = appRootPath + Constant.BUSS + versionDir + Constant.BIN;
				String busShName = shName;
				String busAction = currentAction;
				String busParams = "\""+envHome+"\"";
				execCmd = MessageFormat.format(execCmd, busPath, busShName, busAction, busParams);
				log.debug("rent执行命令: " + execCmd);
				
				// 执行命令返回结果
				resultStr = cmdUtil.execMsg(execCmd);
				log.debug("rent执行命令结果: " + resultStr);

				//rent启停执行关键字
				String keyWords=Constant.FILTER_KEYWORD;
				String[] strArray=keyWords.split(",");
				String temp_result=resultStr.toLowerCase();
				if(strArray.length>0){
					for(int k=0;k<strArray.length;k++){
						if(resultStr.toLowerCase().contains(strArray[k])){
							temp_result=temp_result.replace(strArray[k], " ");
						}
					}
				}
				// 若执行失败,继续换主机执行,直到执行成功后更新数据库
				if(temp_result.toLowerCase().indexOf(ResponseObj.SUCCESS) >= 0){
					log.info("主机" + nimbusMap.get("HOST_IP") + "执行成功");
					updateMap.put("ID", StringTool.object2String(queryParam.get("ID")));
					if(BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
						Trans trans = null;
						//采集程序不需要进行Topic
						if (!"topologyDCM".equalsIgnoreCase(programCode)) {
							try {
								//获取部署主机信息
								FtpDto ftpDto = SessionUtil.getFtpParams();
								trans = FTPUtils.getFtpInstance(ftpDto);
								trans.login();
								String fileName = StringTool.object2String(queryParam.get("NAME"));
								String config_file = StringTool.object2String(queryParam.get("CONFIG_FILE"));
								String remotePath = ftpDto.getFtpRootPath() + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR
										+ FileTool.exactPath(fileName) + FileTool.exactPath(clusterType) + config_file;
								log.debug("获取部署主机配置文件信息， 配置文件路径: " + remotePath);
								
								InputStream in = trans.get(remotePath); 
							    String confStr = cmdUtil.processStderr(in, "UTF-8");
							    in.close();
							    trans.completePendingCommand();
							 	JSONObject json = JSONObject.parseObject(confStr);
							 	Map<String,Object> map = (Map<String,Object>)json.get("development");
							 	Map<String,Object> topMap = (Map<String,Object>)map.get("topology");
							 	Map<String,Object> SMSpoutMap = (Map<String,Object>)topMap.get("SMSpout");
							 	String mqs = StringTool.object2String(SMSpoutMap.get("mq")); 
							 	String topic = mqs.split(",")[1];
							 	queryParam.put("TOPIC_NAME", topic);
							 	updateMap.put("TOPIC_NAME", topic);
							} catch (Exception e) {
								queryParam.put("TOPIC_NAME","未知");
								updateMap.put("TOPIC_NAME","未知");
								log.error("获取topology的topic失败：", e);
							}finally {
								if(trans!=null){
									trans.close();
								}
							}
						}
						
						//更新程序启停标志
						String programId = StringTool.object2String(queryParam.get("ID"));
						if(BlankUtil.isBlank(programId) || BusinessConstant.PARAMS_UNDEFINED.equals(programId)){
							BusParamsHelper.insertProgram(coreService, queryParam,dbKey);
						} else {
							BusParamsHelper.updateProgram(coreService, updateMap,dbKey);
						}
						returnMap.put("info", "在nimbus主机【" + nimbusMap.get("HOST_IP") + "】上启动成功!\n");
					} else {
						BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
						returnMap.put("info", "在nimbus主机【" + nimbusMap.get("HOST_IP") + "】上停止成功!\n");
					}
					flag = false;
				}
				returnMap.put("reason", resultStr);
				if(flag){
					continue;
				}else{
					break;
				}
			}

			if(flag){// 若执行完所有主机都未成功,
				throw new RuntimeException(resultStr);
			}

		} catch (Exception e) {
			log.debug("启停rent程序失败， 失败原因: ", e);
			throw new RuntimeException("执行失败!" + e.getMessage());
		}
		return returnMap;
	}

	/**
	 * 租费业务状态检查
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> updateCheckProgramState(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("租费程序状态检查， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//状态检查Map返回对象
		Map<String, String> returnMap = new HashMap<String, String>();
		//业务参数
		Map<String, Object> queryParam = (Map<String, Object>) param.get("queryParam");
		//业务主集群ID
		String busClusterId = StringTool.object2String(queryParam.get("BUS_CLUSTER_ID"));
		//Topology运行状态
		String runState = StringTool.object2String(queryParam.get("RUN_STATE"));
		//业务集群ID
		String clusterId = StringTool.object2String(queryParam.get("CLUSTER_ID"));
		//业务集群类型
		String clusterType = StringTool.object2String(queryParam.get("CLUSTER_TYPE"));
		//获取当前版本号目录
		String versionDir = FileTool.exactPath("V" + StringTool.object2String(queryParam.get("versionDir")));
		
		String shName = StringTool.object2String(queryParam.get("SCRIPT_SH_NAME"));
		String programCode = StringTool.object2String(queryParam.get("PROGRAM_CODE"));
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
		returnMap.put("ID", StringTool.object2String(queryParam.get("ID")));
		returnMap.put("CLUSTER_ID", clusterId);
		returnMap.put("CLUSTER_TYPE", clusterType);
		returnMap.put("PROGRAM_GROUP", StringTool.object2String(queryParam.get("PROGRAM_GROUP")));
		returnMap.put("TASK_ID", StringTool.object2String(queryParam.get("TASK_ID")));
		returnMap.put("SCRIPT_SH_NAME", StringTool.object2String(queryParam.get("SCRIPT_SH_NAME")));
		returnMap.put("CONFIG_FILE", StringTool.object2String(queryParam.get("CONFIG_FILE")));
		returnMap.put("PROGRAM_CODE", StringTool.object2String(queryParam.get("PROGRAM_CODE")) );
		returnMap.put("PROGRAM_NAME", StringTool.object2String(queryParam.get("PROGRAM_CODE"))+ "-" +StringTool.object2String(queryParam.get("versionDir")));
						
		//操作类型
		String currentFlag = StringTool.object2String(param.get("flag"));
		try {
			//查询业务集群关联的Jstorm组件集群，获取到组件Nimbus
			Map<String, Object> nimbusParams = new HashMap<String, Object>();
			nimbusParams.put("CLUSTER_ID", clusterId);
			List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", nimbusParams, dbKey);
			if (BlankUtil.isBlank(nimbusList)) {
				throw new RuntimeException("该业务集群绑定的组件集群无运行的Nimbus主机");
			}

			//for(int i = 0;i < nimbusList.size(); i++){
				HashMap<String,Object> nimbusMap = nimbusList.get(0);
				//获取Jstorm组件部署根目录
				String jstormDeployPath = StringTool.object2String(nimbusMap.get("CLUSTER_DEPLOY_PATH"));
				//获取Nimbus部署版本
				String nimbusVersion = StringTool.object2String(nimbusMap.get("VERSION"));
				//状态检查参数
				String envHome = BusParamsHelper.getEnvParam(coreService, busClusterId, jstormDeployPath, nimbusVersion, appRootPath, versionDir, dbKey);
				// 组装命令
				String execCmd = Constant.SERVICE_SH;

				// 远程主机登录ssh
				String sshIp = StringTool.object2String(nimbusMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(nimbusMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(nimbusMap.get("SSH_PASSWD")));
				SSHRemoteCmdUtil cmdUtil = new SSHRemoteCmdUtil(sshIp, sshUser, sshPwd, null);
				
				//获取Topology名称
				String currentAction = "";
				String topologyName = programCode + "-" + StringTool.object2String(queryParam.get("versionDir"));
							  
				if(BusinessConstant.PARAMS_CHECK_FLAG.equals(currentFlag)) {
					currentAction = topologyName + " check";
				}

				//rent状态检查参数，分别为：业务版本路径、业务执行脚本、业务执行操作、业务执行参数
				String busPath = appRootPath + Constant.BUSS + versionDir + Constant.BIN;
				String busShName = shName;
				String busAction = currentAction;
				String busParams = "\""+envHome+"\"";
				execCmd = MessageFormat.format(execCmd, busPath, busShName, busAction, busParams);
				log.debug("rent状态检查执行命令: " + execCmd);
				
				// 执行命令返回结果
				String resultStr = cmdUtil.execMsg(execCmd);
				log.debug("rent状态检查命令结果: " + resultStr);

				String keyWords=Constant.FILTER_KEYWORD;
				String[] strArray=keyWords.split(",");
				String temp_result=resultStr.toLowerCase();
				if(strArray.length>0){
					for(int k=0;k<strArray.length;k++){
						if(resultStr.toLowerCase().contains(strArray[k])){
							temp_result=temp_result.replace(strArray[k], " ");
						}
					}
				}

				if(resultStr.toLowerCase().contains(BusinessConstant.PARAMS_START_FLAG)){// 检查出已运行
					returnMap.put("state", BusinessConstant.PARAMS_START_STATE_ACTIVE);
					if (!BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(runState)){// 若当前状态为0或null,则更新状态
						returnMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
						BusParamsHelper.updateOrInsertProgram(coreService, returnMap, dbKey);
						returnMap.put("info", "当前程序正在运行,已同步数据库。");
					} else {
						returnMap.put("info", "当前程序正在运行。");
					}
					returnMap.put("reason",resultStr);
				} else {// 检查出未运行
					returnMap.put("state", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
					if (BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(runState)){// 若当前状态为1,则更新状态
						returnMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
						coreService.updateObject("taskProgram.updateProgramRunState", returnMap, dbKey);
						returnMap.put("info", "当前程序未运行,已同步数据库。");
					} else {
						returnMap.put("info", "当前程序未运行。");
					}
					returnMap.put("reason",resultStr);
				}
			//	break;
			//}
		} catch (Exception e) {
			log.debug("执行状态检查失败，失败原因: ", e);
			returnMap.put("info", "检查执行失败。");
			returnMap.put("reason", e.getMessage());
		}
		return returnMap;
	}


	/**
	 * 查看配置文件定义
	 * @param param 业务参数
	 * @param dbkey 数据库Key
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> scanConfigFile(Map<String, Object> param, String dbkey) throws Exception {
		log.debug("租费程序查看配置文件定义， 参数: " + param.toString() + ", dbKey: " + dbkey);
		
		String fileName = StringTool.object2String(param.get("NAME"));
		//集群类型
		String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		//配置文件路径
		String config_file = StringTool.object2String(param.get("CONFIG_FILE"));
		Trans trans = null;
		String fileContent = "";
		
		//配置文件内容
		Map<String, Object> cont = new HashMap<String, Object>();

		try {
			//获取部署主机信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			String remotePath = ftpDto.getFtpRootPath() + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR
					+ FileTool.exactPath(fileName) + FileTool.exactPath(clusterType) + config_file;
			String localPath = webRootPath+Constant.TMP+System.currentTimeMillis()+"/"+config_file;
			log.debug("租费程序查看配置文件定义， 远程目录: " + remotePath);
			
			trans.get(remotePath, localPath);
			//将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath);
		} catch (Exception e) {
			log.error("租费程序查看配置文件定义失败， 失败原因:", e);
			throw new RuntimeException("查看定义失败！ 失败原因:" + e.getMessage());
		}finally {
			if(trans!=null){
				trans.close();
			}
		}
		cont.put("fileContent", fileContent);
		return cont;
	}

	public static void main(String[] args) {
		Trans trans = null;
		try {
			String ftpUserName = "bp";
			String ftpPasswd = "bp";
			String ftpIp = "192.168.161.89";
			String ftpType = "ftp";
			String ftpPath = "/public/bp/ftpfiles_ah/";
			trans = FTPUtils.getFtpInstance(ftpIp, ftpUserName, ftpPasswd, ftpType);
			trans.login();
			String fileName = "DIC-BIL-OCS-MOD-AH_V2.0.4.1";
			String config_file = "topologyV.conf";
			String remotePath = ftpPath + "conf/business_config/release/" 
					+ fileName + "/" + "billing" + "/" + config_file;
			//String localPath = webRootPath+Constant.TMP+System.currentTimeMillis()+"/"+config_file;
			InputStream in = trans.get(remotePath);
		    SSHRemoteCmdUtil cmd = new SSHRemoteCmdUtil();
		    String confStr = cmd.processStderr(in, "UTF-8");
		    in.close();
		    trans.completePendingCommand();
		    
		 	JSONObject json = JSONObject.parseObject(confStr);
		 	Map<String,Object> map = (Map<String,Object>)json.get("development");
		 	Map<String,Object> topMap = (Map<String,Object>)map.get("topology");
		 	Map<String,Object> SMSpoutMap = (Map<String,Object>)topMap.get("SMSpout");
		 	String mqs = StringTool.object2String(SMSpoutMap.get("mq")); 
		 	String topic = mqs.split(",")[1];
		 	System.out.println(topic);
		} catch (Exception e) {
			log.error(e);
		 
		}finally {
			if(trans!=null){
				trans.close();
			}

		}
	}
}
