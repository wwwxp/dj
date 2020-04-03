package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.common.BusParamsHelper;
import com.tydic.service.configure.RunTopologyService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [RunTopologyServiceImpl]     
  * @Description:  [Topology类型启停]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-28 下午2:45:29]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-28 下午2:45:29]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class RunTopologyServiceImpl implements RunTopologyService {
	@Resource
	private CoreService coreService;

	private static Logger log = Logger.getLogger(RunTopologyServiceImpl.class);

	/**
	 * Topology业务启停
	 * 
	 * @param param 业务参数
	 * @param dbKey 数据库连接串Key
	 * @return Map 返回值对象
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateRunAndStopHost(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("启动业务程序(Topology), 参数:" + param.toString());
		//返回对象
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//前台业务参数
		List<Map<String, Object>> topologyList = (List<Map<String, Object>>) param.get("TOPOLOGY_LIST");
		//业务主集群ID
		final String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		//获取当前业务集群ID
		final String clusterId = StringTool.object2String(param.get("CLUSTER_ID"));
		//获取当前业务集群类型
		final String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		//获取当前版本号目录
		final String versionDir = FileTool.exactPath("V" + StringTool.object2String(param.get("versionDir")));
		//操作用户ID
		final String empeeId = StringTool.object2String(param.get("EMPEE_ID"));
		//获取当前业务集群部署路径
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("获取业务集群信息失败, 请检查！");
		}
		//业务组件部署根目录
		final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));

		//查询当前业务集群可部署主机是否达到最大值，如果达到最大值则不能在部署
		final String currentFlag = StringTool.object2String(param.get("flag"));
		//批量启停结果
		List<Map<String, Object>> startStopRetList = new ArrayList<>();
		//命令执行开始时间
		Long startTimes = System.currentTimeMillis();
		//业务启停是否全部成功标识
		int startStopSuccCnt = 0;
		//单例获取执行线程
		ExecutorService pool = SingletonThreadPool.getExecutorService();
		//线程执行计数器
		CountDownLatch latch = new CountDownLatch(topologyList.size());
		//业务程序处理返回结果
		List<Future<Map<String, Object>>> futureList = new ArrayList<>();

		for (int k=0; k<topologyList.size(); k++) {
			Map<String, Object> topologyProMap = topologyList.get(k);

			Future<Map<String, Object>> retFuture = pool.submit(new Callable<Map<String,Object>>() {
				//topology业务参数
				private Map<String, Object> topologyMap = null;
				private CountDownLatch latch = null;
				private Logger log = null;
				public Callable initParams(Map<String, Object> topologyMap, CountDownLatch latch, Logger log) {
					this.topologyMap = topologyMap;
					this.latch = latch;
					this.log = log;
					return this;
				}

				@Override
				public Map<String, Object> call() throws Exception {
					long startProTimes = System.currentTimeMillis();

					String resultStr = "";
					Map<String, Object> retMap = new HashMap<>();
					Map<String, Object> startStopMap = new HashMap<>();
					//程序任务ID
					String programTaskId = StringTool.object2String(topologyMap.get("ID"));
					String programCode = StringTool.object2String(topologyMap.get("PROGRAM_CODE"));
					if (StringUtils.isBlank(programTaskId)) {
						Map<String, Object> newIdMap = coreService.queryForObject2New("config.queryNewID", null, FrameConfigKey.DEFAULT_DATASOURCE);
						programTaskId = ObjectUtils.toString(newIdMap.get("NEW_ID"));
					}

					//程序操作类型
					String operationAction = BusinessConstant.PARAMS_START_FLAG.equals(currentFlag) ? "start" : "stop";
					String operationActionZh = BusinessConstant.PARAMS_START_FLAG.equals(currentFlag) ? "启动" : "停止";
					//生成日志文件名称
					String logPath = SystemProperty.getContextProperty(Constant.BUSS_TASK_LOG_PATH);
					String logName = programTaskId + "_" + programCode + ".log";
					Logger threadLogger = LoggerUtils.getThreadLogger(logPath, logName, "RunTopologyServiceImpl-" + Thread.currentThread().getName());
					String programName = "";
					try {
						programName = StringTool.object2String(topologyMap.get("PROGRAM_NAME"));
						threadLogger.debug("ProgramName: " + programName + ", programCode: " + programCode + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);

						//查询业务集群关联的Jstorm组件集群，获取到组件Nimbus
						Map<String, Object> nimbusParams = new HashMap<String, Object>();
						nimbusParams.put("CLUSTER_ID", clusterId);
						threadLogger.info("Query jstorm cluster nimbus list, params: " + nimbusParams);
						List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", nimbusParams, dbKey);
						threadLogger.info("Query jstorm cluster nimbus list succeeded, count: " + (nimbusList == null ? 0 : nimbusList.size()) + ", list: " + (nimbusList == null ? "" : nimbusList.toString()));
						if (BlankUtil.isBlank(nimbusList)) {
							threadLogger.error("<label style='color:red;'>Associated jstorm cluster did not start nimbus, business cluster ID: " + clusterId + "</label>");
							throw new RuntimeException("该业务集群绑定的组件集群无运行的Nimbus主机!");
						}

						// 查询出数据库得到脚本名称
						String shName = StringTool.object2String(topologyMap.get("SCRIPT_SH_NAME"));
						// 更新程序Task表数据
						Map<String, String> updateMap = new HashMap<String, String>();
						updateMap.put("TASK_ID", StringTool.object2String(topologyMap.get("TASK_ID")));

						//遍历集群所有部署的Nimbus列表，提交Topology图
						boolean hasExecSuccess = false;
						String sshIp = "";
						for (int i = 0; i < nimbusList.size(); i++) {
							try {
								HashMap<String, Object> nimbusMap = nimbusList.get(i);
								threadLogger.debug("start topology, current node: " + nimbusMap.toString());

								//获取Jstorm组件部署根目录
								String jstormDeployPath = StringTool.object2String(nimbusMap.get("CLUSTER_DEPLOY_PATH"));
								//获取Nimbus部署版本
								String nimbusVersion = StringTool.object2String(nimbusMap.get("VERSION"));
								//获取Topology启动参数
								String envHome = BusParamsHelper.getEnvParam(coreService, busClusterId, jstormDeployPath, nimbusVersion, appRootPath, versionDir, dbKey);
								threadLogger.debug("start topology, environment variable: " + envHome);
								// 组装命令
								String execCmd = Constant.SERVICE_SH;

								// 远程主机登录ssh
								sshIp = StringTool.object2String(nimbusMap.get("HOST_IP"));
								String sshUser = StringTool.object2String(nimbusMap.get("SSH_USER"));
								String sshPwd = DesTool.dec(StringTool.object2String(nimbusMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPwd);
								threadLogger.debug("start topology, hostIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPwd);

								//获取Topology名称
								String currentAction = "";
								String topologyName = programCode + "-" + StringTool.object2String(topologyMap.get("versionDir"));
								topologyMap.put("PROGRAM_NAME", topologyName);
								if (BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
									currentAction = topologyName + " start";
									updateMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
								} else if (BusinessConstant.PARAMS_STOP_FLAG.equals(currentFlag)) {
									currentAction = topologyName + " stop";
									updateMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
								}

								//billing启停参数，分别为：业务版本路径、业务执行脚本、业务执行操作、业务执行参数
								String busPath = appRootPath + Constant.BUSS + versionDir + Constant.BIN;
								threadLogger.debug("start topology, config file: " + (busPath + topologyName + ".conf"));
								String busShName = shName;
								String busAction = currentAction;
								String busParams = "\"" + envHome + "\"";
								execCmd = MessageFormat.format(execCmd, busPath, busShName, busAction, busParams);
								log.debug("Topology执行命令: " + execCmd);
								execCmd = execCmd.replace("$EMP", StringTool.object2String(param.get("EMPEE_ID")));
								threadLogger.debug("Program [ " + programName + " ] " + operationAction + " command: <label style='color:green;'>" + execCmd + "</label>");
								startStopMap.put("execCmd", execCmd);
								// 执行命令返回结果
								resultStr = cmdUtil.execMsg(execCmd);
								log.debug("Topology执行命令结果: " + resultStr);
								threadLogger.debug("Program [ " + programName + " ] " + operationAction + " result: " + resultStr);

								//Topology启停执行关键字
								String keyWords = Constant.FILTER_KEYWORD;
								String[] strArray = keyWords.split(",");
								String temp_result = resultStr.toLowerCase();
								boolean wordsFlag = true;
								if (strArray.length > 0) {
									for (int k = 0; k < strArray.length; k++) {
										if (resultStr.toLowerCase().contains(strArray[k])) {
											//temp_result=temp_result.replace(strArray[k], " ");
											wordsFlag = false;
											break;
										}
									}
								}
								// 若执行失败,继续换主机执行,直到执行成功后更新数据库
								if (temp_result.toLowerCase().indexOf(ResponseObj.SUCCESS) >= 0 && wordsFlag) {
									log.info("主机" + nimbusMap.get("HOST_IP") + "执行成功");
									startStopMap.put("reason", resultStr);

									updateMap.put("ID", StringTool.object2String(topologyMap.get("ID")));
									if (BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
										//更新程序启停标志
										String programId = StringTool.object2String(topologyMap.get("ID"));
										if (BlankUtil.isBlank(programId) || BusinessConstant.PARAMS_UNDEFINED.equals(programId)) {
											topologyMap.put("CONFIG_FILE", programCode + ".conf");
											topologyMap.put("ID", programTaskId);
											BusParamsHelper.insertTaskProgramWithNewId(coreService, topologyMap, dbKey);
											log.debug("新增启动实例数据成功(Topology)，参数: " + topologyMap);
											threadLogger.debug("Program status added successfully(Start), params:" + topologyMap);

											//BusParamsHelper.insertProgram(coreService, topologyMap, dbKey);
										} else {
											BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
											log.debug("修改启动实例数据成功(Topology)，参数: " + updateMap);
											threadLogger.debug("Program status updated successfully(Start), params:" + updateMap);
										}
										startStopMap.put("info", "在主机【" + nimbusMap.get("HOST_IP") + "】上启动Topology成功!");
										startStopMap.put("flag", BusinessConstant.PARAMS_RST_SUCCESS);
									} else {
										BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
										log.debug("修改停止实例数据成功(Topology)，参数: " + updateMap);
										startStopMap.put("info", "在主机【" + nimbusMap.get("HOST_IP") + "】上停止Topology成功!");
										startStopMap.put("flag", BusinessConstant.PARAMS_RST_SUCCESS);

										threadLogger.debug("Program status updated successfully(Stop), params:" + updateMap);
									}
									retMap.put("RET_FLAG", BusinessConstant.PARAMS_BUS_1);
									hasExecSuccess = true;
									break;
								}
							} catch (Exception e) {
								log.error("当前节点启停Topology异常, 节点:" + (sshIp) + "，异常信息:", e);
								threadLogger.error("current node " + operationAction + "error, cause by:", e);
							}
						}
						//所有的节点主机执行最终结果
						if (!hasExecSuccess) {
							log.error("Topology在所有Nimbus节点执行结果失败!!!");
							if (BusinessConstant.PARAMS_START_FLAG.equals(currentFlag)) {
								String programId = StringTool.object2String(topologyMap.get("ID"));
								if (BlankUtil.isBlank(programId) || BusinessConstant.PARAMS_UNDEFINED.equals(programId)) {
									topologyMap.put("CONFIG_FILE", programCode + ".conf");
									topologyMap.put("ID", programTaskId);
									BusParamsHelper.insertTaskProgramWithNewId(coreService, topologyMap, dbKey);
									log.debug("新增启动实例数据成功(Topology)，参数: " + topologyMap);
									threadLogger.debug("Program status added successfully(Start Failed), params:" + topologyMap);
								}
							}
							throw new RuntimeException(resultStr);
						}
					} catch (Exception e) {
						log.error("启停Topology程序失败， 失败原因: ", e);
						startStopMap.put("info", "Topology: [ " + programName + " ] 在所有节点" + operationActionZh + "失败，详细信息请查看对应程序日志!");
						startStopMap.put("reason", resultStr);
						startStopMap.put("flag", BusinessConstant.PARAMS_RST_ERROR);
						threadLogger.error("Topology [ " + programName + " ] " + operationAction + " failed!!!");
					} finally {
						threadLogger.info("Topology [ " + programName + " ] " + operationAction + " finish!!!");
						latch.countDown();
					}
					retMap.put("RET_DATA", startStopMap);
					long endProTimes = System.currentTimeMillis();
					long totalProTimes = (endProTimes - startProTimes) / 1000;
					threadLogger.debug("Topology: [ " + programName + " ] " + operationAction + " end, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
					return retMap;
				}
			}.initParams(topologyProMap, latch, log));
			futureList.add(retFuture);
		}

		try {
			latch.await();
			log.info("批量程序处理完成(Topology)， 总程序数:" + futureList.size());
			for (int i=0; i<futureList.size(); i++) {
				Future<Map<String, Object>> retFuture = futureList.get(i);
				if (retFuture.isDone()) {
					Map<String, Object> retFutureMap = retFuture.get();
					startStopRetList.add((Map<String, Object>) retFutureMap.get("RET_DATA"));
					String retFlag = ObjectUtils.toString(retFutureMap.get("RET_FLAG"), "");
					if (StringUtils.equals(retFlag, BusinessConstant.PARAMS_BUS_1)) {
						startStopSuccCnt++;
					}
				}
			}
		} catch (InterruptedException e) {
			log.error("程序处理失败(Topology), 原因: ", e);
		} catch (ExecutionException e) {
			log.error("获取程序返回异常(Topology), 原因: ", e);
		}
		Long endTimes = System.currentTimeMillis();
		returnMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_1);
		returnMap.put("SUCCESS_CNT", startStopSuccCnt);
		returnMap.put("FAIL_CNT", (topologyList.size() - startStopSuccCnt));
		returnMap.put("TOTAL_CNT", topologyList.size());
		returnMap.put("TOTAL_TIMES", (endTimes - startTimes)/1000);
		returnMap.put("RET_DATA", startStopRetList);
		return returnMap;
	}

	/**
	 * Topology状态检查
	 * 
	 * @param param 业务参数 
	 * @param dbKey 数据库Key
	 * @return Map
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateCheckProgramState(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("Topology状态检查, 参数: " + param.toString() + ", dbKey: " + dbKey);
		
		//状态检查Map返回对象
		Map<String, Object> returnMap = new HashMap<String, Object>();
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
				ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPwd);
				
				//获取Topology名称
				String currentAction = "";
				String topologyName = programCode + "-" + StringTool.object2String(queryParam.get("versionDir"));
				if(BusinessConstant.PARAMS_CHECK_FLAG.equals(currentFlag)) {
					currentAction = topologyName + " check";
				}

				//billing状态检查参数，分别为：业务版本路径、业务执行脚本、业务执行操作、业务执行参数
				String busPath = appRootPath + Constant.BUSS + versionDir + Constant.BIN;
				String busShName = shName;
				String busAction = currentAction;
				String busParams = "\""+envHome+"\"";
				execCmd = MessageFormat.format(execCmd, busPath, busShName, busAction, busParams);
				log.debug("Topology状态检查执行命令: " + execCmd);
				
				// 执行命令返回结果
				String resultStr = cmdUtil.execMsg(execCmd);
				log.debug("Topology状态检查命令结果: " + resultStr);

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
						returnMap.put("info", "【"+returnMap.get("PROGRAM_NAME")+"】当前程序正在运行,已同步数据库。");
					} else {
						returnMap.put("info", "【"+returnMap.get("PROGRAM_NAME")+"】当前程序正在运行。");
					}
					returnMap.put("state", BusinessConstant.PARAMS_BUS_1);
					returnMap.put("reason",resultStr);
				} else {// 检查出未运行
					returnMap.put("state", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
					if (BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(runState)){// 若当前状态为1,则更新状态
						returnMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
						coreService.updateObject2New("taskProgram.updateProgramRunState", returnMap, dbKey);
						returnMap.put("info", "【"+returnMap.get("PROGRAM_NAME")+"】当前程序未运行,已同步数据库。");
					} else {
						returnMap.put("info", "【"+returnMap.get("PROGRAM_NAME")+"】当前程序未运行。");
					}
					returnMap.put("state", BusinessConstant.PARAMS_BUS_0);
					returnMap.put("reason",resultStr);
				}
				returnMap.put("rstCode", BusinessConstant.PARAMS_RST_SUCCESS);
			//	break;
			//}
		} catch (Exception e) {
			log.debug("执行状态检查失败，失败原因: ", e);
			//throw e;
			returnMap.put("state", BusinessConstant.PARAMS_BUS_4);
			returnMap.put("rstCode", BusinessConstant.PARAMS_DO_RST_FAILED);
			returnMap.put("info", "【"+returnMap.get("PROGRAM_NAME")+"】检查执行失败。");
			returnMap.put("reason", e.getMessage());
		}
		return returnMap;
	}

	/**
	 * 查询定义Topology配置文件
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return map 返回值独享
	 */
	@Override
	public Map<String, Object> queryViewConf(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("Topology配置文件信息， 参数: " + param + ", dbKey: " + dbKey);
		
		//版本包名称，如:DIC-BIL-DUCC-SH_V18.0.0.1
		//String fileName = StringTool.object2String(param.get("NAME"));
		//版本, 例如:18.0.0.1
		String version = StringTool.object2String(param.get("VERSION"));
		//配置文件名称
		String config_file = StringTool.object2String(param.get("CONFIG_FILE"));
		//部署环境路径
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		//集群类型
		String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		//集群ID
		String clusterId = StringTool.object2String(param.get("CLUSTER_ID"));
		
		Trans trans = null;
		Map<String, Object> cont = new HashMap<String, Object>();
		String fileContent = "";
		try {
			//本地临时保存目录
			String localPath = webRootPath + Constant.TMP+System.currentTimeMillis()+"/"+config_file;
			
			//获取当前集群主机列表
			Map<String, Object> qryParams = new HashMap<String, Object>();
			qryParams.put("CLUSTER_ID", clusterId);
			qryParams.put("CLUSTER_TYPE", clusterType);

			//获取业务程序部署根目录
			Map<String, Object> busClusterMap = coreService.queryForObject2New("serviceType.queryServiceTypeList", qryParams, dbKey);
			log.debug("当前业务集群信息: " + busClusterMap.toString());
			
			//获取业务程序关联Jstorm组件部署主机列表
			List<HashMap<String, Object>> hostList = coreService.queryForList2New("instConfig.queryBusNimbusListByBusClusterId", qryParams, dbKey);
			if (!BlankUtil.isBlank(hostList)) {
				for (int i=0; i<hostList.size(); i++) {
						Map<String, Object> hostMap = hostList.get(i);
						log.debug("获取Jstorm配置文件，获取文件主机： " + hostMap.toString());
						//String hostState = StringTool.object2String(hostMap.get("STATE"));
					//if (!BusinessConstant.PARAMS_BUS_0.equals(hostState)) {
						
						//获取业务程序部署路径
						String deployPath = StringTool.object2String(busClusterMap.get("CLUSTER_DEPLOY_PATH"));
						
						//主机信息
						String sshHostIp = StringTool.object2String(hostMap.get("HOST_IP"));
						String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
						String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
						trans = FTPUtils.getFtpInstance(sshHostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
						trans.login();
						
						//远程主机配置文件路径
						String remotePath = FileTool.exactPath(deployPath) + FileTool.exactPath(Constant.BUSS) 
								+ FileTool.exactPath("V" + version) + FileTool.exactPath(Constant.CFG) + config_file;
						log.debug("查询配置文件, 配置文件路径: " + remotePath);
						
						trans.get(remotePath, localPath);
					//}
				}
			}
			
//			String ftpUserName = SessionUtil.getConfigValue("FTP_USERNAME");
//			String ftpPasswd = SessionUtil.getConfigValue("FTP_PASSWD");
//			String ftpIp = SessionUtil.getConfigValue("FTP_IP");
//			String ftpType = SessionUtil.getConfigValue("FTP_TYPE");
//			String ftpPath = SessionUtil.getConfigValue("FTP_ROOT_PATH");
//			trans = FTPUtils.getFtpInstance(ftpIp, ftpUserName, ftpPasswd, ftpType);
//			trans.login();
//			
//			//文件服务器上的文件路径
//			String remotePath = FileTool.exactPath(ftpPath) + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR
//					+ FileTool.exactPath(fileName) + FileTool.exactPath(clusterType) + config_file;
//			log.debug("Topology查看配置文件定义， 远程目录: " + remotePath);
//			
//			String localPath = webRootPath + Constant.TMP+System.currentTimeMillis()+"/"+config_file;
//			trans.get(remotePath, localPath);

			//将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath);
		} catch (Exception e) {
			log.error("查询Topology配置文件失败， 失败原因: ", e);
			throw new RuntimeException("查看定义失败, 失败原因: " + e.getMessage());
		}finally {
			if(trans!=null){
				trans.close();
			}
		}
		cont.put("fileContent", fileContent);
		return cont;
	}
	
	public static void main(String[] args) {
		String resultStr="\""+"$Errorinfo$E   yyy"+"\"";

		System.out.println(resultStr.replace("$E", "SS"));
	}

}
