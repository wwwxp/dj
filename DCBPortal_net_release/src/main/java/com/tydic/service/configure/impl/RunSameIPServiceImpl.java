package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.common.BusException;
import com.tydic.common.BusParamsHelper;
import com.tydic.service.configure.RunSameIPService;
import com.tydic.util.*;
import com.tydic.util.ftp.FileTool;
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
  * @ClassName:    [RunSameIPServiceImpl]     
  * @Description:  [不区分IP程序启停]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-28 下午2:49:50]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-28 下午2:49:50]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class RunSameIPServiceImpl implements RunSameIPService {
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(RunSameIPServiceImpl.class);

	//常量
	private static final String RST_STATE = "state";
	

	/**
	 * 不区分IP程序启停
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回值对象
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateRunAndStopHost(Map<String, Object> param, String dbKey) throws BusException {
		log.debug("不区分IP程序启停， 参数: " + param.toString() + ", dbKey: " + dbKey);
		
		//返回对象
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//业务集群ID
		String clusterId = StringTool.object2String(param.get("CLUSTER_ID"));
		//业务集群类型
		String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		//获取当前业务集群部署路径
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			log.error("查询业务集群信息失败，无法启停程序(不区分IP业务)，请确保数据正确!");
			throw new RuntimeException("获取业务集群信息失败, 请检查！");
		}
		//业务组件部署根目录
		String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		
		//需要启停的主机列表
		List<Map<String, Object>> paramList = (List<Map<String, Object>>) param.get("HOST_LIST");
		//当前业务程序启停版本目录
		String versionDir = StringTool.object2String("V" + paramList.get(0).get("VERSION")) + "/";

		//操作用户ID
		final String empeeId = StringTool.object2String(param.get("EMPEE_ID"));
		//批量启停结果
		List<Map<String, Object>> startStopRetList = new ArrayList<>();
		//业务启停是否全部成功标识
		int startStopSuccCnt = 0;
		//命令执行开始时间
		Long startTimes = System.currentTimeMillis();
		//单例获取执行线程
		ExecutorService pool = SingletonThreadPool.getExecutorService();
		//线程执行计数器
		CountDownLatch latch=new CountDownLatch(paramList.size());
		List<Future<Map<String, Object>>> futureList = new ArrayList<>();
		for (int i=0; i<paramList.size(); i++) {
			Map<String, Object> paramMap = paramList.get(i);

			Future<Map<String, Object>> retFuture = pool.submit(new Callable<Map<String,Object>>() {
				private CountDownLatch latch = null;
				private Logger log = null;
				public Callable initParams(CountDownLatch latch, Logger log) {
					this.latch = latch;
					this.log = log;
					return this;
				}

				@Override
				public Map<String, Object> call() throws Exception {
					long startProTimes = System.currentTimeMillis();
					//程序任务ID
					String programTaskId = StringTool.object2String(paramMap.get("ID"));
					String programCode = StringTool.object2String(paramMap.get("PROGRAM_CODE"));
					if (StringUtils.isBlank(programTaskId)) {
						Map<String, Object> newIdMap = coreService.queryForObject2New("config.queryNewID", null, FrameConfigKey.DEFAULT_DATASOURCE);
						programTaskId =	ObjectUtils.toString(newIdMap.get("NEW_ID"));
					}

					//程序操作类型
					String operationAction = BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag")) ? "start" : "stop";

					//生成日志文件名称
					String logPath = SystemProperty.getContextProperty(Constant.BUSS_TASK_LOG_PATH);
					String logName = programTaskId + "_" + programCode + ".log";
					Logger threadLogger = LoggerUtils.getThreadLogger(logPath, logName, "RunSameIpServiceImpl-" + Thread.currentThread().getName());

					String programName = StringTool.object2String(paramMap.get("PROGRAM_NAME"));
					threadLogger.debug("ProgramName: " + programName + ", programCode: " + programCode + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);

					Map<String, Object> retMap = new HashMap<>();
					Map<String, Object> startStopMap = new HashMap<>();
					String runAndStop = "启动";

					Map<String, Object> hostMap = null;
					String resultStr = "";
					try {
						// 远程主机登录ssh
						threadLogger.debug("Query host information, params:" + paramMap.toString());
						hostMap = coreService.queryForObject2New("host.queryHostList", paramMap, dbKey);
						String hostIp = StringTool.object2String(hostMap.get("HOST_IP"));
						String hostUser = StringTool.object2String(hostMap.get("SSH_USER"));
						String hostPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
						threadLogger.debug("Query host information succeeded, hostId:" + hostMap.get("HOST_ID") + ", hostIp:" + hostIp + ", UserName:" + hostUser);

						ShellUtils cmdUtil = new ShellUtils(hostIp, hostUser, hostPwd);

						// 查询出数据库得到脚本名称
						String shName = StringTool.object2String(paramMap.get("SCRIPT_SH_NAME"));

						// 不区分IP程序启动命令
						String cmd = Constant.SERVICE_SH;
						String currentAction = "";
						if (BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag"))) {
							currentAction = "start";
						} else if (BusinessConstant.PARAMS_STOP_FLAG.equals(param.get("flag"))) {
							currentAction = "stop";
						}
						//业务程序配置文件路径
						String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG;
						//业务程序路径
						String busPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.BIN;
						//业务程序脚本
						String exShName = shName;
						//业务程序启动类型
						String busAction = currentAction;
						String execCmd = MessageFormat.format(cmd, busPath, exShName, busAction, "");
						execCmd = execCmd.replace("$P", busCfgPath);
						execCmd = execCmd.replace("$EMP", StringTool.object2String(param.get("EMPEE_ID")));

						log.debug("不区分IP程序启停命令: " + execCmd);
						threadLogger.debug("Program " + operationAction + ", hostIP: " + hostIp + ", hostUser: " + hostUser + ", hostPwd: " + hostPwd);
						threadLogger.debug("Program "+ operationAction + " command: <label style='color:green;'>" + execCmd + "</label>");
						startStopMap.put("execCmd", execCmd);
						// 执行命令返回结果
						resultStr = cmdUtil.execMsg(execCmd);
						log.debug("不区分IP程序启停结果: " + resultStr);
						threadLogger.debug("Program " + operationAction + " result: " + resultStr);

						if (resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0
								|| resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
							resultStr = resultStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
							throw new RuntimeException(resultStr);
						}

						Map<String, String> updateMap = new HashMap<String, String>();
						updateMap.put("RUN_STATE", StringTool.object2String(param.get("RUN_STATE")));
						updateMap.put("HOST_ID", StringTool.object2String(paramMap.get("HOST_ID")));
						updateMap.put("TASK_ID", StringTool.object2String(paramMap.get("TASK_ID")));
						updateMap.put("PROGRAM_NAME", StringTool.object2String(paramMap.get("PROGRAM_NAME")));

						if (BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag"))) {
							String id = StringTool.object2String(paramMap.get("ID"));
							if (BlankUtil.isBlank(id) || BusinessConstant.PARAMS_UNDEFINED.equals(id)) {// 需插入一条记录
								paramMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
								//BusParamsHelper.insertProgram(coreService, paramMap, dbKey);
								paramMap.put("ID", programTaskId);
								BusParamsHelper.insertTaskProgramWithNewId(coreService, paramMap, dbKey);
								log.debug("新增启动实例数据成功(不区分IP业务)，参数: " + paramMap);
								threadLogger.debug("Program status added successfully(Start), params:" + paramMap);
							} else {
								// 更新状态
								updateMap.put("ID", StringTool.object2String(paramMap.get("ID")));
								BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
								log.debug("修改启动实例数据成功(不区分IP业务)，参数: " + updateMap);
								threadLogger.debug("Program status updated successfully(Start), params:" + updateMap);
							}
						} else {
							runAndStop = "停止";
							updateMap.put("ID", StringTool.object2String(paramMap.get("ID")));
							BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
							log.debug("修改停止实例数据成功(不区分IP业务)，参数: " + updateMap);
							threadLogger.debug("Program status updated successfully(Stop), params:" + updateMap);
						}
						startStopMap.put("info", "主机 [ " + StringTool.object2String(hostMap.get("HOST_IP") + " ] " + runAndStop + "[ " + programName + " ]成功\n"));
						startStopMap.put("reason", resultStr);
						startStopMap.put("flag", BusinessConstant.PARAMS_RST_SUCCESS);
						retMap.put("RET_FLAG", BusinessConstant.PARAMS_BUS_1);
						threadLogger.info("HostIp[ " + StringTool.object2String(hostMap.get("HOST_IP") + " ] " + operationAction + " [ " + programName + " ] successed\n"));
					} catch (Exception e) {
						log.error("不区分IP程序启停失败， 主机IP: " + StringTool.object2String(hostMap.get("HOST_IP")), e);
						startStopMap.put("info", "主机 [" + StringTool.object2String(hostMap.get("HOST_IP") + "] " + runAndStop + " [ " + programName + " ]失败\n"));
						startStopMap.put("reason", resultStr);
						startStopMap.put("flag", BusinessConstant.PARAMS_RST_ERROR);

						threadLogger.error("<label style='color:red;'>HostIp[" + StringTool.object2String(hostMap.get("HOST_IP") + "] " + operationAction + " failed. Cause by:</label>"), e);
						threadLogger.error("<label style='color:red;'>HostIp[ " + StringTool.object2String(hostMap.get("HOST_IP") + " ] " + operationAction + " [ " + programName + " ] failed.</label>\n"));
						//判断程序是否为新启动，如果是新启动，没有ID，添加一条失败记录
						if (BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag"))) {
							String id = StringTool.object2String(paramMap.get("ID"));
							if (BlankUtil.isBlank(id) || BusinessConstant.PARAMS_UNDEFINED.equals(id)) {
								paramMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
								paramMap.put("ID", programTaskId);
								BusParamsHelper.insertTaskProgramWithNewId(coreService, paramMap, dbKey);
								log.debug("启动异常，新增启动实例数据成功(不区分IP业务)，参数: " + paramMap);
								threadLogger.debug("Program status added successfully(Start Failed), params:" + paramMap);
							}
						}
					} finally {
						threadLogger.debug("Program [ " + programCode + " ] " + operationAction + " finish!!!");
						latch.countDown();
					}
					retMap.put("RET_DATA", startStopMap);
					long endProTimes = System.currentTimeMillis();
					long totalProTimes = (endProTimes - startProTimes)/1000;
					threadLogger.debug("Program " + operationAction + " end, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
					return retMap;
				}
			}.initParams(latch, log));
			futureList.add(retFuture);
		}

		try {
			latch.await();
			log.info("批量程序处理完成(SAME)， 总程序数:" + futureList.size());
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
			log.error("程序处理失败, 原因: ", e);
		} catch (ExecutionException e) {
			log.error("获取程序返回异常, 原因: ", e);
		}
		Long endTimes = System.currentTimeMillis();
		returnMap.put("RET_CODE", BusinessConstant.PARAMS_BUS_1);
		returnMap.put("SUCCESS_CNT", startStopSuccCnt);
		returnMap.put("FAIL_CNT", (paramList.size() - startStopSuccCnt));
		returnMap.put("TOTAL_CNT", paramList.size());
		returnMap.put("TOTAL_TIMES", (endTimes - startTimes)/1000);
		returnMap.put("RET_DATA", startStopRetList);
		return returnMap;
	}

	/**
	 * 检查不区分IP程序运行状态
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回值对象
	 */
	@Override
	public Map<String, Object> updateCheckHostState(Map<String, Object> params, String dbKey) throws BusException {
		log.debug("检查不区分IP程序运行状态, 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		//业务集群ID
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		//业务集群类型
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
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

		//当前程序运行状态
		String runState = StringTool.object2String(params.get("RUN_STATE"));
		//程序运行版本目录
		String versionDir = StringTool.object2String("V" + params.get("VERSION")) + "/";
		
		//状态检查执行返回结果对象
		Map<String, Object> result = new HashMap<String, Object>();

		// 如果页面传递记录中没有HOST_ID,则说明该程序未在该主机上
		if (BlankUtil.isBlank(StringTool.object2String(params.get("HOST_ID")))) {
			result.put(RST_STATE, BusinessConstant.PARAMS_BUS_3);
			return result;
		}
		try {
			// 远程主机登录ssh
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("HOST_ID", params.get("HOST_ID"));
			Map<String, Object> hostMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
			String hostIp = StringTool.object2String(hostMap.get("HOST_IP"));
			String hostUser = StringTool.object2String(hostMap.get("SSH_USER"));
			String hostPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
			ShellUtils cmdUtil = new ShellUtils(hostIp, hostUser, hostPwd);
	
			// 查询出数据库得到脚本名称
			String shName = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
	
			// 组装命令
			String cmd = Constant.SERVICE_SH;
			//业务程序配置文件路径
			String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG;
			//业务程序路径
			String busPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.BIN;
			//业务程序脚本
			String exShName = shName;
			//业务程序启动类型
			String busAction = "check";
			//业务程序版本目录
			//String busVerPath = FileTool.exactPath(appRootPath) + Constant.BUSS + versionDir;
			//检查执行命令
			String execCmd = MessageFormat.format(cmd, busPath, exShName, busAction, "");
			//替换参数
			execCmd = execCmd.replace("$P", busCfgPath);
			
			
			log.debug("不区分IP程序状态检查, 执行命令: " + execCmd);
			// 执行命令返回结果
			String resultStr = cmdUtil.execMsg(execCmd);
			log.debug("不区分IP程序状态检查执行命令结果; " + resultStr);
			
			if (resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0
					|| resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
				resultStr = resultStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
				result.put(RST_STATE, BusinessConstant.PARAMS_BUS_4);
				throw new RuntimeException(resultStr);
			}
			
			if (resultStr.toLowerCase().contains("run ")) {// 检查出已运行
				result.put(RST_STATE, 1);
				if (!runState.equals("1")) {// 若当前状态不为1（0或null）,则更新状态
					params.put("RUN_STATE", "1");
					BusParamsHelper.updateOrInsertProgram(coreService, params, dbKey);
					result.put("info", "当前程序正在运行,已同步数据库");
				}
				result.put("process", resultStr.substring(resultStr.toLowerCase().indexOf("run") + 3));
			} else {// 检查出未运行
				result.put(RST_STATE, 0);
				if (runState.equals("1") ) {// 若当前状态为1,则更新状态
					params.put("RUN_STATE", "0");
					BusParamsHelper.updateProgram(coreService, params, dbKey);
					result.put("info", "当前程序未运行,已同步数据库");
				}
			} 
		} catch (Exception e) {
			log.debug("不区分IP程序执行状态检查失败，失败原因: ", e);
			throw new BusException(BusinessConstant.PARAMS_BUS_4, "检查执行失败！", e.getMessage());
			//result.put("info", "检查执行失败！");
			//result.put("reason", e.getMessage());
		}
		return result;
	}
}
