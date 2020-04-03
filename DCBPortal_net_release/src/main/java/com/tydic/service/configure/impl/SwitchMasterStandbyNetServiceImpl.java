package com.tydic.service.configure.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.RunTopologyService;
import com.tydic.service.configure.SwitchMasterStandbyNetService;
import com.tydic.service.configure.TopManagerService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [SwitchMasterStandbyNetServiceImpl]     
  * @Description:  [版本切换Net版本]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-21 下午1:54:58]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-21 下午1:54:58]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class SwitchMasterStandbyNetServiceImpl implements SwitchMasterStandbyNetService {
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger logger = Logger.getLogger(SwitchMasterStandbyNetServiceImpl.class);

	/**
	 * 设置默认编码
	 */
	private static final String DEFAULT_ENCODE = "UTF-8";
	
	/**
	 * 启停Topology服务对象
	 */
	@Autowired
	private RunTopologyService runTopologyService;
	
	/**
	 * 重新负载Topology服务对象
	 */
	@Autowired
	private TopManagerService topManagerService;
	
	/**
	 * 获取当前运行的Topology列表
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<HashMap<String, Object>> getRunningTopologyList(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询当前运行的Topology列表， 业务参数： " + params + ", dbKey: " + dbKey);
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));
		queryMap.put("RUN_STATE", BusinessConstant.PARAMS_BUS_1);
		List<HashMap<String, Object>> topologyList = coreService.queryForList2New("taskProgram.queryRunningTopologyList", queryMap, dbKey);
		topologyList = topologyList == null ? new ArrayList<HashMap<String, Object>>() : topologyList;
		logger.debug("查询当前运行的Topology结束， 返回结果大小: " + topologyList.size());
		return topologyList;
	}
	
	/**
	 * 获取待升级的Topology列表
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<HashMap<String, Object>> getUpgradeTopologyList(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询当前待升级的Topology列表， 业务参数： " + params + ", dbKey: " + dbKey);
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));
		queryMap.put("PROGRAM_GROUP", params.get("PROGRAM_GROUP"));
		queryMap.put("TASK_PROGRAM_ID", params.get("TASK_PROGRAM_ID"));
		//queryMap.put("RUN_STATE", BusinessConstant.PARAMS_BUS_0);
		List<HashMap<String, Object>> topologyList = coreService.queryForList2New("taskProgram.queryRunningTopologyList", queryMap, dbKey);
		topologyList = topologyList == null ? new ArrayList<HashMap<String, Object>>() : topologyList;
		logger.debug("查询当前待升级的Topology结束， 返回结果大小: " + topologyList.size());
		return topologyList;
	}
	
	/**
	 * 获取当前运行的Topology列表节点信息
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<HashMap<String, Object>> getRunningTopologyNodeList(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询当前运行的Topology节点运行信息， 业务参数： " + params + ", dbKey: " + dbKey);
		
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", params.get("TASK_PROGRAM_ID"));
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		logger.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());
				
		//查询版本发布服务器信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//业务配置文件
		String runState = StringTool.object2String(taskProgramMap.get("RUN_STATE"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		
		//查询Topology运行节点信息
		List<HashMap<String, Object>> hostList = new ArrayList<HashMap<String, Object>>();
		HashSet<Object> hostSet = new HashSet<Object>();
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		InputStream fileStream = null;
		BufferedReader reader = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			fileStream = trans.get(realFilePath);
			
			StringBuffer buffer = new StringBuffer();
			reader = new BufferedReader(new InputStreamReader(fileStream));
			String lineStr = "";
			while( (lineStr = reader.readLine()) != null) {
				buffer.append(lineStr);
			}
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.toString().trim());
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			for (Object hostCfg : jsonArray) {
				JSONArray hostArray =  (JSONArray) hostCfg; 
				if (BusinessConstant.PARAMS_BUS_1.equals(runState)) {
					HashMap<String, Object> hostMap = new HashMap<String, Object>();
					hostMap.put("HOST_IP", hostArray.get(0));
					hostMap.put("STATUS", BusinessConstant.PARAMS_BUS_1);
					hostSet.add(hostArray.get(0));
					hostList.add(hostMap);
				}
			}
		} catch (Exception e) {
			logger.error("版本切换， 获取Topology运行节点信息失败， 失败原因: ", e);
			throw new RuntimeException("查询Topology图运行节点信息失败， 失败原因: 获取节点列表失败");
		} finally {
			try {
				if (trans != null) {
					trans.close();
				}
				if (fileStream != null) {
					fileStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.debug("版本切换， 获取Topology运行节点，流关闭失败， 失败原因: ", e);
			}
		}
		
		//查询Topology对应的集群运行主机列表
//		List<HashMap<String, Object>> deployNodeList = coreService.queryForList2New("taskProgram.queryRunningTopologyNodeList", queryParams, dbKey);
//		for (HashMap<String, Object> deployNode : deployNodeList) {
//			if (!hostSet.contains(deployNode.get("HOST_IP"))) {
//				HashMap<String, Object> nodeMap = new HashMap<String, Object>();
//				nodeMap.put("HOST_IP", deployNode.get("HOST_IP"));
//				nodeMap.put("STATUS", BusinessConstant.PARAMS_BUS_0);
//				hostList.add(nodeMap);
//			}
//		}
		hostList = hostList == null ? new ArrayList<HashMap<String, Object>>() : hostList;
		logger.debug("查询当前运行的Topology节点信息， 返回结果大小: " + hostList.size());
		return hostList;
	}
	
	/**
	 * 灰度发布
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public HashMap<String, Object> startNodeVersionUpgrade(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("版本切换，灰度发布， 业务参数： " + params + ", dbKey: " + dbKey);
		//返回对象
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		//处理信息
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap.put("DO_MSG", new StringBuffer());
		try {
			//查询版本发布服务器信息
			Map<String, Object> configParams = new HashMap<String, Object>();
			configParams.put("GROUP_CODE", "WEB_FTP_CONFIG");
			List<HashMap<String, Object>> configList = coreService.queryForList2New("config.queryConfigList", configParams, dbKey);
			
			//修改已运行Topology信息
			//根据任务ID查询配置文件(正在运行Topology信息)
			Map<String, Object> queryRunningParams = new HashMap<String, Object>();
			queryRunningParams.put("TASK_PROGRAM_ID", params.get("RUNNING_TASK_PROGRAM_ID"));
			HashMap<String, Object> taskRunningProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryRunningParams, dbKey);
			logger.info("业务程序重新负载，业务程序信息: " + taskRunningProgramMap.toString());
			try {
				this.updateRunningTopology(tempMap, params, taskRunningProgramMap, configList, dbKey);
			} catch (Exception e) {
				logger.error("运行节点切换处理失败， 失败原因: ", e);
				
				//处理失败， 回退版本
				String doLevel = StringTool.object2String(tempMap.get("DO_LEVEL"));
				String oriConfigFileStr = StringTool.object2String(tempMap.get("ORI_CONFIG_FILE"));
				if(BusinessConstant.PARAMS_BUS_1.equals(doLevel)) {
					//回退版本发布服务器
					this.rollbackDeployHostConfig(oriConfigFileStr, taskRunningProgramMap, configList, dbKey);
					logger.debug("回退版本发布服务器配置文件成功...");
				} else {
					//回退版本发布服务器以及集群所有主机配置文件
					this.rollbackDeployHostConfig(oriConfigFileStr, taskRunningProgramMap, configList, dbKey);
					logger.debug("回退版本发布服务器配置文件成功...");
					
					//回退集群远程主机配置文件
					this.rollbackRemoteHostConfig(oriConfigFileStr, taskRunningProgramMap, configList, dbKey);
					logger.debug("回退远程服务器配置文件成功...");
				}
				throw new RuntimeException("处理失败， 失败原因: " + e.getMessage());
				//throw new RuntimeException("节点切换处理失败！");
			}
			logger.debug("运行版本切换成功...");
			
			//修改待升级Topology信息
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("TASK_PROGRAM_ID", params.get("UPGREAD_TASK_PROGRAM_ID"));
			HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
			logger.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());
			try {
				this.updateUpgradTopology(tempMap, params, taskProgramMap, configList, dbKey);
				
				//记录升级成功记录数据
				this.addUpgradHistoryLog(taskRunningProgramMap, taskProgramMap, tempMap, dbKey);
				
			} catch (Exception e) {
				logger.error("节点切换处理失败， 失败原因: ", e);
				
				String oriConfigFileStr = StringTool.object2String(tempMap.get("ORI_CONFIG_FILE"));
				//回退版本发布服务器以及集群所有主机配置文件
				this.rollbackDeployHostConfig(oriConfigFileStr, taskRunningProgramMap, configList, dbKey);
				logger.debug("待升级Topology信息，回退版本发布服务器配置文件成功...");
				
				//回退集群远程主机配置文件信息
				this.rollbackRemoteHostConfig(oriConfigFileStr, taskRunningProgramMap, configList, dbKey);
				logger.debug("待升级Topology信息，回退远程服务器配置文件成功...");
				
				//回退Topology信息
				taskRunningProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryRunningParams, dbKey);
				this.rollbackTopologyStatus(tempMap, taskRunningProgramMap, configList, dbKey);
				
				//处理失败， 回退版本
				String doLevel = StringTool.object2String(tempMap.get("DO_LEVEL"));
				String destConfigFileStr = StringTool.object2String(tempMap.get("DEST_CONFIG_FILE"));
				if (BusinessConstant.PARAMS_BUS_2.equals(doLevel)) {
					
				} else if(BusinessConstant.PARAMS_BUS_4.equals(doLevel)) {
					//回退版本发布服务器
					this.rollbackDeployHostConfig(destConfigFileStr, taskProgramMap, configList, dbKey);
					logger.debug("待升级Topology信息，回退待升级版本发布服务器配置文件成功...");
				} else {
					//回退版本发布服务器以及集群所有主机配置文件
					this.rollbackDeployHostConfig(destConfigFileStr, taskProgramMap, configList, dbKey);
					logger.debug("待升级Topology信息，回退待升级版本发布服务器配置文件成功...");
					
					this.rollbackRemoteHostConfig(destConfigFileStr, taskProgramMap, configList, dbKey);
					logger.debug("待升级Topology信息，回退待升级远程服务器配置文件成功...");
				} 
				throw new RuntimeException("节点切换处理失败！");
				//throw new RuntimeException("处理失败， 失败原因: " + e.getMessage());
			}
			logger.debug("待升级版本切换成功...");
			
			StringBuffer msgBuffer = (StringBuffer) tempMap.get("DO_MSG");
			msgBuffer.append("版本切换成功...");
			retMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_1);
			retMap.put("RET_MSG", msgBuffer.toString());
		} catch (RuntimeException e) {
			StringBuffer msgBuffer = (StringBuffer) tempMap.get("DO_MSG");
			msgBuffer.append("版本切换失败...");
			retMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
			retMap.put("RET_MSG", msgBuffer.toString());
			throw e;
		} catch (Exception e) {
			logger.error("版本切换失败， 失败原因: ", e);
			
			StringBuffer msgBuffer = (StringBuffer) tempMap.get("DO_MSG");
			msgBuffer.append("版本切换失败...");
			retMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_0);
			retMap.put("RET_MSG", msgBuffer.toString());
			
			throw new RuntimeException("版本切换失败， 失败原因: " + e.getMessage());
		} 
		return retMap;
	}
	
	/**
	 * 添加版本升级成功记录
	 * @param taskRunningProgramMap
	 * @param taskProgramMap
	 * @param tempMap
	 * @param dbKey
	 */
	private void addUpgradHistoryLog(Map<String, Object> taskRunningProgramMap, Map<String, Object> taskProgramMap, Map<String, Object> tempMap, String dbKey) {
		logger.debug("添加版本升级成功记录信息， 升级前业务程序信息: " + taskRunningProgramMap + ", 待升级业务程序: " + taskProgramMap + ", 升级主机信息: " + tempMap.get("HOST_LIST"));
		Map<String, Object> addParams = new HashMap<String, Object>();
		addParams.put("SOURCE_TASK_PROGRAM_ID", taskRunningProgramMap.get("ID"));
		addParams.put("DEST_TASK_PROGRAM_ID", taskProgramMap.get("ID"));
		addParams.put("HOST_LIST", tempMap.get("HOST_LIST"));
		coreService.insertObject2New("upgradeHistory.addUpgradHistory", addParams, dbKey);
		logger.debug("版本升级日志添加成功...");
	}
	
	/**
	 * 修改待升级版本Topology
	 * @param params
	 * @param taskProgramMap
	 * @param configList
	 * @param dbKey
	 */
	private void updateUpgradTopology(Map<String, Object> tempMap, Map<String, Object> params, HashMap<String, Object> taskProgramMap, 
			List<HashMap<String, Object>> configList, String dbKey) {
		//版本切换主机列表
		JSONArray hostList = (JSONArray) params.get("HOST_LIST");
		//处理信息输出
		StringBuffer msgBuffer = (StringBuffer) tempMap.get("DO_MSG");
		//版本发布服务信息
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//操作类型
		String operatorType = StringTool.object2String(params.get("OPERATOR_TYPE"));
		if (BlankUtil.isBlank(operatorType)) {
			operatorType = BusinessConstant.PARAMS_BUS_1;
		}
		String operatorTypeStr = BusinessConstant.PARAMS_BUS_1.equals(operatorType) ? "(2-升级)" : "(3-回退)";
		
		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		String runState = StringTool.object2String(taskProgramMap.get("RUN_STATE"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String scriptshName = StringTool.object2String(taskProgramMap.get("SCRIPT_SH_NAME"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterId = StringTool.object2String(taskProgramMap.get("CLUSTER_ID"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String clusterName = StringTool.object2String(taskProgramMap.get("CLUSTER_NAME"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		String busClusterId = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_ID"));
		String programCode = StringTool.object2String(taskProgramMap.get("PROGRAM_CODE"));
		String programName = StringTool.object2String(taskProgramMap.get("PROGRAM_NAME"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String taskId = StringTool.object2String(taskProgramMap.get("TASK_ID"));
		String id = StringTool.object2String(taskProgramMap.get("ID"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		Trans outTrans = null;
		InputStream fileStream = null;
		InputStream instram = null;
		BufferedReader reader = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			fileStream = trans.get(realFilePath);
			StringBuffer buffer = new StringBuffer();
			reader = new BufferedReader(new InputStreamReader(fileStream));
			String lineStr = "";
			while( (lineStr = reader.readLine()) != null) {
				buffer.append(lineStr);
			}
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.toString().trim());
			
			//待升级配置原始文件
			tempMap.put("DEST_CONFIG_FILE", buffer.toString().trim());
			
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			//当前主机worker数量，至少保证每台主机一个worker
			int workNum = configJsonObj.getInteger("topology.billing.workgroup.num");
			int oldHostSize = jsonArray.size() == 0 ? 1 : jsonArray.size();
			
			String addIpStr = "";
			int hcount = 0;
			for (int i=0; i<hostList.size(); i++) {
				JSONObject hostMap = hostList.getJSONObject(i);
				List<String> newHostList = new ArrayList<String>();
				//判断是否存在该节点配置，如果不存在则添加
				boolean isExist = false;
				for (Object hostObject : jsonArray) {
					if (hostObject instanceof JSONArray) {
						JSONArray hostArray =  (JSONArray) hostObject;
						if (hostArray.get(0).equals(hostMap.get("HOST_IP"))) {
							isExist = true;
							break;
						}
					} else if (hostObject instanceof ArrayList) {
						ArrayList<String> hostArray = (ArrayList<String>) hostObject;
						if (hostArray.get(0).equals(hostMap.get("HOST_IP"))) {
							isExist = true;
							break;
						}
					}
				}
				if (!isExist) {
					newHostList.add(StringTool.object2String(hostMap.get("HOST_IP")));
					newHostList.add("1");
					addIpStr += hostMap.get("HOST_IP") + ",";
					jsonArray.add(newHostList);
					hcount++;
				}
			}
			
			//扩展节点新增worker
			int newWorkNum = (int)Math.ceil(workNum / oldHostSize);
			configJsonObj.put("topology.billing.workgroup.num", workNum + (hcount * newWorkNum));
			
			//将Json转化为文件
			outTrans = FTPUtils.getFtpInstance(ftpDto);
			outTrans.login();
			String jsonStr = JSON.toJSONString(fileJsonObj);
			jsonStr = JsonFormatTool.formatJson(jsonStr);
			instram = new ByteArrayInputStream(jsonStr.getBytes(DEFAULT_ENCODE));
			outTrans.put(instram, realFilePath);
			tempMap.put("DO_LEVEL", BusinessConstant.PARAMS_BUS_4);
			logger.debug("版本切换，修改已运行版本配置文件成功...");
			
			//将版本发布服务器配置文件分发到远程主机
			String versionDir = FileTool.exactPath("V" + version);
			String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + versionDir + Constant.CFG_DIR + configFileName;		
			
			//查询当前业务集群所有部署的主机
			Map<String, Object> hostQueryParams = new HashMap<String, Object>();
			hostQueryParams.put("CLUSTER_ID", clusterId);
			List<HashMap<String, Object>> deployHostList = coreService.queryForList2New("deployHome.queryVersionByClusterId", hostQueryParams, dbKey);
			for (HashMap<String, Object> hostMap : deployHostList) {
				//远程主机信息
				instram = new ByteArrayInputStream(jsonStr.getBytes(DEFAULT_ENCODE));
				String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
				Trans tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpDto.getFtpType());
				tran.login();
				tran.put(instram, remotePath);
				tran.close();
				logger.debug("版本切换，主机: " + sshIp + "， 修改重新负载配置文件成功...");
			}
			tempMap.put("DO_LEVEL", BusinessConstant.PARAMS_BUS_5);
			
			if (!BlankUtil.isBlank(addIpStr)) {
				addIpStr = addIpStr.substring(0, addIpStr.length() - 1);
				//升级主机列表
				tempMap.put("HOST_LIST", addIpStr);
			}
			msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    节点 【" + addIpStr + "】添加到【" + clusterName + "】集群，【"+programName+"】程序成功...<br/>");
			
			//当前Topology主机列表信息为空，需要启动
			if (BusinessConstant.PARAMS_BUS_0.equals(runState)) {
				Map<String, String> queryParam = new HashMap<String, String>();
				queryParam.put("BUS_CLUSTER_ID", busClusterId);
				queryParam.put("CLUSTER_ID", clusterId);
				queryParam.put("CLUSTER_TYPE", clusterType);
				queryParam.put("versionDir", version);
				queryParam.put("SCRIPT_SH_NAME", scriptshName);
				queryParam.put("PROGRAM_CODE", programCode);
				queryParam.put("TASK_ID", taskId);
				queryParam.put("ID", id);
				
				Map<String, Object> stopParams = new HashMap<String, Object>();
				stopParams.put("flag", BusinessConstant.PARAMS_START_FLAG);
				stopParams.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
				stopParams.put("queryParam", queryParam);
				runTopologyService.updateRunAndStopHost(stopParams, dbKey);
				msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】启动成功...<br/>");
				logger.debug("发布路径：" + remotePath + ", 操作类型: " + operatorTypeStr + ", 新版本号: " + version);
				logger.debug("节点标识： " + addIpStr + ", 服务启动命令名称： start， 服务启动结果: 成功");
				logger.debug("版本切换，Topology启动成功");
			} else {
				//当前Topology主机信息不为空，重新负载
				Map<String, Object> reblanceMap = new HashMap<String, Object>();
				reblanceMap.put("BUS_CLUSTER_ID", taskProgramMap.get("BUS_CLUSTER_ID"));
				reblanceMap.put("CLUSTER_ID", taskProgramMap.get("CLUSTER_ID"));
				reblanceMap.put("CLUSTER_TYPE", taskProgramMap.get("CLUSTER_TYPE"));
				reblanceMap.put("PROGRAM_CODE", taskProgramMap.get("PROGRAM_CODE"));
				reblanceMap.put("VERSION", taskProgramMap.get("VERSION"));
				reblanceMap.put("CONFIG_FILE", configFileName);
				topManagerService.topRebalanceReload(reblanceMap, dbKey);
				msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】重新负载成功...<br/>");
				logger.debug("发布路径：" + remotePath + ", 操作类型: " + operatorTypeStr + ", 新版本号: " + version);
				logger.debug("节点标识： " + addIpStr + ", 服务启动命令名称： rebalance， 服务启动结果: 成功");
				logger.debug("版本切换，重新负载成功...");
			}
		} catch (Exception e) {
			msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】节点变更失败，失败原因: " + e.getMessage() + "<br/>");
			logger.error("版本切换，灰度升级失败， 失败原因: ", e);
			throw new RuntimeException("灰度升级失败， 失败原因: " + e.getMessage());
		} finally {
			try {
				if (trans != null) {
					trans.close();
				}
				if (outTrans != null) {
					outTrans.close();
				}
				if (fileStream != null) {
					fileStream.close();
				}
				if (instram != null) {
					instram.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.debug("流关闭失败， 失败原因: ", e);
			}
		}
	}
	
	/**
	 * 回退版本发布服务器配置文件
	 * @param oriConfigStr 最初的原始文件
	 * @param taskProgramMap
	 * @param configList
	 * @param dbKey
	 */
	private void rollbackDeployHostConfig(String oriConfigStr, HashMap<String, Object> taskProgramMap, List<HashMap<String, Object>> configList, String dbKey) {
		logger.debug("回退版本发布服务器配置文件， 业务参数： " + taskProgramMap);
		Trans outTrans = null;
		ByteArrayInputStream inputStream = null;
		try {
			//版本发布服务信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			//业务配置文件
			String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
			String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
			String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
			String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
			String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
			
			//版本发布服务器配置文件真实路径
			String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
					+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
					+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
			String realFilePath = realPath + configFileName;
			
			//将Json转化为文件
			outTrans = FTPUtils.getFtpInstance(ftpDto);
			outTrans.login();
			String jsonStr = JsonFormatTool.formatJson(oriConfigStr);
			inputStream = new ByteArrayInputStream(jsonStr.getBytes(DEFAULT_ENCODE));
			outTrans.put(inputStream, realFilePath);
		} catch (UnsupportedEncodingException e) {
			logger.error("回退版本发布服务器配置文件失败 ，失败原因: ", e);
			throw new RuntimeException("回退版本发布服务器配置文件失败， 失败原因: " + e.getMessage());
		} catch (Exception e) {
			logger.error("回退版本发布服务器配置文件失败 ，失败原因: ", e);
			throw new RuntimeException("回退版本发布服务器配置文件失败， 失败原因: " + e.getMessage());
		} finally {
			if (outTrans != null) {
				outTrans.close();
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("流关闭失败， 失败原因: ", e);
				}
			}
		}
	}
	
	/**
	 * 回退集群远程主机配置文件
	 * @param oriConfigFileStr 原始配置文件
	 * @param taskProgramMap
	 * @param configList
	 * @param dbKey
	 */
	private void rollbackRemoteHostConfig(String oriConfigFileStr, HashMap<String, Object> taskProgramMap, List<HashMap<String, Object>> configList, String dbKey) {
		logger.debug("回退集群主机配置文件， 业务参数： " + taskProgramMap);
		ByteArrayInputStream inputStream = null;
		try {
			//版本发布服务信息
			 FtpDto ftpDto = SessionUtil.getFtpParams();
			
			//业务配置文件
			String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
			String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
			String clusterId = StringTool.object2String(taskProgramMap.get("CLUSTER_ID"));
			String version = StringTool.object2String(taskProgramMap.get("VERSION"));
			
			//将版本发布服务器配置文件分发到远程主机
			String versionDir = FileTool.exactPath("V" + version);
			String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + versionDir + Constant.CFG_DIR + configFileName;		
			
			//查询当前业务集群所有部署的主机
			Map<String, Object> hostQueryParams = new HashMap<String, Object>();
			hostQueryParams.put("CLUSTER_ID", clusterId);
			List<HashMap<String, Object>> deployHostList = coreService.queryForList2New("deployHome.queryVersionByClusterId", hostQueryParams, dbKey);
			for (HashMap<String, Object> hostMap : deployHostList) {
				//远程主机信息
				inputStream = new ByteArrayInputStream(oriConfigFileStr.getBytes(DEFAULT_ENCODE));
				String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
				Trans tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpDto.getFtpType());
				tran.login();
				tran.put(inputStream, remotePath);
				tran.close();
				logger.debug("回退远程主机: " + sshIp + "配置文件成功...");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("回退远程主机配置文件失败 ，失败原因: ", e);
			throw new RuntimeException("回退远程主机配置文件失败， 失败原因: " + e.getMessage());
		} catch (Exception e) {
			logger.error("回退远程主机配置文件失败 ，失败原因: ", e);
			throw new RuntimeException("回退远程主机配置文件失败， 失败原因: " + e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("流关闭失败， 失败原因: ", e);
				}
			}
		}
	}
	
	/**
	 * 回退Topology初始化状态
	 * @param tempMap
	 * @param taskProgramMap
	 * @param configList
	 * @param dbKey
	 */
	private void rollbackTopologyStatus(Map<String, Object> tempMap, HashMap<String, Object> taskProgramMap, List<HashMap<String, Object>> configList, String dbKey) {
		logger.debug("回退集群主机配置文件， 业务参数： " + taskProgramMap);
		try {
			//业务配置文件
			String runState = StringTool.object2String(taskProgramMap.get("RUN_STATE"));
			String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
			String scriptshName = StringTool.object2String(taskProgramMap.get("SCRIPT_SH_NAME"));
			String clusterId = StringTool.object2String(taskProgramMap.get("CLUSTER_ID"));
			String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
			String busClusterId = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_ID"));
			String programCode = StringTool.object2String(taskProgramMap.get("PROGRAM_CODE"));
			String version = StringTool.object2String(taskProgramMap.get("VERSION"));
			String taskId = StringTool.object2String(taskProgramMap.get("TASK_ID"));
			String id = StringTool.object2String(taskProgramMap.get("ID"));
			
			//当前Topology主机列表信息为空，需要启动
			if (BusinessConstant.PARAMS_BUS_0.equals(runState)) {
				Map<String, String> queryParam = new HashMap<String, String>();
				queryParam.put("BUS_CLUSTER_ID", busClusterId);
				queryParam.put("CLUSTER_ID", clusterId);
				queryParam.put("CLUSTER_TYPE", clusterType);
				queryParam.put("versionDir", version);
				queryParam.put("SCRIPT_SH_NAME", scriptshName);
				queryParam.put("PROGRAM_CODE", programCode);
				queryParam.put("TASK_ID", taskId);
				queryParam.put("ID", id);
				
				Map<String, Object> stopParams = new HashMap<String, Object>();
				stopParams.put("flag", BusinessConstant.PARAMS_START_FLAG);
				stopParams.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
				stopParams.put("queryParam", queryParam);
				runTopologyService.updateRunAndStopHost(stopParams, dbKey);
				logger.debug("回退Topology原始状态，Topology启动成功...");
			} else {
				//当前Topology主机信息不为空，重新负载
				Map<String, Object> reblanceMap = new HashMap<String, Object>();
				reblanceMap.put("BUS_CLUSTER_ID", taskProgramMap.get("BUS_CLUSTER_ID"));
				reblanceMap.put("CLUSTER_ID", taskProgramMap.get("CLUSTER_ID"));
				reblanceMap.put("CLUSTER_TYPE", taskProgramMap.get("CLUSTER_TYPE"));
				reblanceMap.put("PROGRAM_CODE", taskProgramMap.get("PROGRAM_CODE"));
				reblanceMap.put("VERSION", taskProgramMap.get("VERSION"));
				reblanceMap.put("CONFIG_FILE", configFileName);
				topManagerService.topRebalanceReload(reblanceMap, dbKey);
				logger.debug("回退Topology原始状态，重新负载成功...");
			}
		} catch (Exception e) {
			logger.debug("回退Topology原始状态失败， 失败原因: ", e);
		}
	}
	
	/**
	 * 运行中的Topology状态变更
	 * @param params
	 * @param taskProgramMap
	 * @param configList
	 * @param dbKey
	 */
	private void updateRunningTopology(Map<String, Object> tempMap, Map<String, Object> params, HashMap<String, Object> taskProgramMap, List<HashMap<String, Object>> configList, String dbKey) {
		//版本切换主机列表
		JSONArray hostList = (JSONArray) params.get("HOST_LIST");
		
		//处理信息输出
		StringBuffer msgBuffer = (StringBuffer) tempMap.get("DO_MSG");
		
		//操作类型
		String operatorType = StringTool.object2String(params.get("OPERATOR_TYPE"));
		if (BlankUtil.isBlank(operatorType)) {
			operatorType = BusinessConstant.PARAMS_BUS_1;
		}
		String operatorTypeStr = BusinessConstant.PARAMS_BUS_1.equals(operatorType) ? "(2-升级)" : "(3-回退)";
		
		//版本发布服务信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		//String runState = StringTool.object2String(taskProgramMap.get("RUN_STATE"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String scriptshName = StringTool.object2String(taskProgramMap.get("SCRIPT_SH_NAME"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterId = StringTool.object2String(taskProgramMap.get("CLUSTER_ID"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String clusterName = StringTool.object2String(taskProgramMap.get("CLUSTER_NAME"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		String busClusterId = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_ID"));
		String programCode = StringTool.object2String(taskProgramMap.get("PROGRAM_CODE"));
		String programName = StringTool.object2String(taskProgramMap.get("PROGRAM_NAME"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String taskId = StringTool.object2String(taskProgramMap.get("TASK_ID"));
		String id = StringTool.object2String(taskProgramMap.get("ID"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		Trans outTrans = null;
		InputStream fileStream = null;
		InputStream instram = null;
		BufferedReader reader = null;
		StringBuffer buffer = new StringBuffer();
		String remotePath = "";
		List<HashMap<String, Object>> deployHostList = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			fileStream = trans.get(realFilePath);
			
			reader = new BufferedReader(new InputStreamReader(fileStream));
			String lineStr = "";
			while( (lineStr = reader.readLine()) != null) {
				buffer.append(lineStr);
			}
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.toString().trim());
			
			//将原始配置文件保存
			tempMap.put("ORI_CONFIG_FILE", buffer.toString().trim());
			
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			//获取当前移除的节点IP
			String removeIpStr = "";
			
			//当前Topology设置的work数量
			int workNum = configJsonObj.getInteger("topology.billing.workgroup.num");
			int oldHostSize = jsonArray.size() == 0 ? 1 : jsonArray.size();
			int index = 0;
			for (int i=0; i<hostList.size(); i++) {
				JSONObject hostMap = hostList.getJSONObject(i);
				for (Object hostCfg : jsonArray) {
					JSONArray hostArray =  (JSONArray) hostCfg; 
					if (hostMap.get("HOST_IP").equals(hostArray.get(0))) {
						removeIpStr += hostMap.get("HOST_IP") + ",";
						jsonArray.remove(hostCfg);
						index++;
						break;
					}
				}
			}
			
			//设置修改后的work数量
			int newWorkNum = workNum - (index*(int)Math.floor(workNum / oldHostSize));
			if(newWorkNum<=0){
				newWorkNum = 1;
			}
			configJsonObj.put("topology.billing.workgroup.num", StringUtils.defaultString(String.valueOf(newWorkNum), String.valueOf(1)));
			
			//将Json转化为文件
			outTrans = FTPUtils.getFtpInstance(ftpDto);
			outTrans.login();
			String jsonStr = JSON.toJSONString(fileJsonObj);
			jsonStr = JsonFormatTool.formatJson(jsonStr);
			instram = new ByteArrayInputStream(jsonStr.getBytes(DEFAULT_ENCODE));
			outTrans.put(instram, realFilePath);
			//当前程序处理步骤
			tempMap.put("DO_LEVEL", BusinessConstant.PARAMS_BUS_1);
			logger.debug("版本切换，修改已运行版本配置文件成功...");
			
			//将版本发布服务器配置文件分发到远程主机
			String versionDir = FileTool.exactPath("V" + version);
			remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + versionDir + Constant.CFG_DIR + configFileName;		
			
			//查询当前业务集群所有部署的主机
			Map<String, Object> hostQueryParams = new HashMap<String, Object>();
			hostQueryParams.put("CLUSTER_ID", clusterId);
			deployHostList = coreService.queryForList2New("deployHome.queryVersionByClusterId", hostQueryParams, dbKey);
			for (HashMap<String, Object> hostMap : deployHostList) {
				//远程主机信息
				instram = new ByteArrayInputStream(jsonStr.getBytes(DEFAULT_ENCODE));
				String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
				Trans tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpDto.getFtpType());
				tran.login();
				tran.put(instram, remotePath);
				tran.close();
				logger.debug("版本切换，主机: " + sshIp + "， 修改重新负载配置文件成功...");
			}
			tempMap.put("DO_LEVEL", BusinessConstant.PARAMS_BUS_2);
			
			//获取本次移除的节点信息
			if(!BlankUtil.isBlank(removeIpStr)) {
				removeIpStr = removeIpStr.substring(0, removeIpStr.length() - 1);
			}
			msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    节点: 【" + removeIpStr + "】从【" + clusterName + "】集群，【"+programName+"】程序移除成功...<br/>");
			
			//当前Topology主机列表信息为空，需要停止
			if (jsonArray.isEmpty() || jsonArray.size() == 0) {
				Map<String, String> queryParam = new HashMap<String, String>();
				queryParam.put("BUS_CLUSTER_ID", busClusterId);
				queryParam.put("CLUSTER_ID", clusterId);
				queryParam.put("CLUSTER_TYPE", clusterType);
				queryParam.put("versionDir", version);
				queryParam.put("SCRIPT_SH_NAME", scriptshName);
				queryParam.put("PROGRAM_CODE", programCode);
				queryParam.put("TASK_ID", taskId);
				queryParam.put("ID", id);
				
				Map<String, Object> stopParams = new HashMap<String, Object>();
				stopParams.put("flag", BusinessConstant.PARAMS_STOP_FLAG);
				stopParams.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
				stopParams.put("queryParam", queryParam);
				runTopologyService.updateRunAndStopHost(stopParams, dbKey);
				msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】无节点运行，停止成功...<br/>");
				logger.debug("发布路径：" + remotePath + ", 操作类型: " + operatorTypeStr + ", 历史版本号: " + version);
				logger.debug("移除节点标识： " + removeIpStr + ", 服务启动命令名称： stop， 服务启动结果: 服务停止成功");
				logger.debug("版本切换，Topology停止成功");
			} else {
				//当前Topology主机信息不为空，重新负载
				Map<String, Object> reblanceMap = new HashMap<String, Object>();
				reblanceMap.put("BUS_CLUSTER_ID", taskProgramMap.get("BUS_CLUSTER_ID"));
				reblanceMap.put("CLUSTER_ID", taskProgramMap.get("CLUSTER_ID"));
				reblanceMap.put("CLUSTER_TYPE", taskProgramMap.get("CLUSTER_TYPE"));
				reblanceMap.put("PROGRAM_CODE", taskProgramMap.get("PROGRAM_CODE"));
				reblanceMap.put("VERSION", taskProgramMap.get("VERSION"));
				reblanceMap.put("CONFIG_FILE", configFileName);
				topManagerService.topRebalanceReload(reblanceMap, dbKey);
				msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】尚有节点运行，重新负载成功...<br/>");
				logger.debug("发布路径：" + remotePath + ", 操作类型: " + operatorTypeStr + ", 历史版本号: " + version);
				logger.debug("移除节点标识： " + removeIpStr + ", 服务启动命令名称： rebalance， 服务启动结果: 重新负载成功");
				logger.debug("版本切换，重新负载成功...");
			}
		} catch (Exception e) {
			msgBuffer.append("【" + DateUtil.getCurrent(DateUtil.allPattern) + "】    集群【" + clusterName + "】，程序【"+programName+"】节点变更失败，失败原因: " + e.getMessage() + "<br/>");
			logger.error("版本切换，灰度升级失败， 失败原因: ", e);
			throw new RuntimeException("灰度升级失败， 失败原因: " + e.getMessage());
		} finally {
			try {
				if (trans != null) {
					trans.close();
				}
				if (outTrans != null) {
					outTrans.close();
				}
				if (fileStream != null) {
					fileStream.close();
				}
				if (instram != null) {
					instram.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.debug("流关闭失败， 失败原因: ", e);
			}
		}
	}
}
