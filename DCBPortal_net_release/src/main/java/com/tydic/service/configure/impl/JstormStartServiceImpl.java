package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.service.configure.JstormStartService;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.SingletonThreadPool;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.util.TempFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [JstormStartServiceImpl]     
  * @Description:  [Jstorm组件启停实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:13:15]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:13:15]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class JstormStartServiceImpl implements JstormStartService {
	/**
	 * Jstorm启停日志
	 */
	private static Logger logger = Logger.getLogger(JstormStartServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 获取启停日志文件service对象
	 */
	@Autowired
	private InstConfigService instConfigService;
	
	/**
	 * 启动Jstorm
	 * 
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startJstorm(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动Jstorm,参数 ---> " + param + ", dbKey ---> " + dbKey);
		//主机处理结果保存对象
		List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
		//返回对象
		Map<String, Object> rstMap = new HashMap<String, Object>();
		
		if(!BlankUtil.isBlank(param)){
			//获取当前集群部署根目录
			Map<String, String> querySingleMap = param.get(0);
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", querySingleMap.get("CLUSTER_ID"));
			queryClusterMap.put("CLUSTER_TYPE", querySingleMap.get("CLUSTER_TYPE"));
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
			if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				throw new RuntimeException("集群信息查询失败, 请检查！");
			}
			//组件部署根目录
			final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//集群ID
			final String clusterId = querySingleMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = querySingleMap.get("CLUSTER_TYPE");
			//操作用户ID
			final String empeeId = StringTool.object2String(querySingleMap.get("EMPEE_ID"));
			//集群编码
			//final String clusterCode = querySingleMap.get("CLUSTER_CODE");
			//获取启动组件执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				final List<Map<String,String>> resultList = new ArrayList<>();
				Long startTimes = System.currentTimeMillis();
				for(int i=0;i<param.size();i++){
					final Map<String ,String> hostMap = (Map<String, String>) param.get(i);

					//异步调用执行远程命令
					pool.execute(new Runnable() {
						public void run() {
							Map<String, String> resultMap = new HashMap<>();
							Map<String, Object> tempMap = null;
							Logger threadLogger = null;
							Long startProTimes = System.currentTimeMillis();
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

								//判断是否需要清除DATA
								final String dataClear = StringTool.object2String(hostMap.get("DATA_CLEAR"));
								//程序启停版本
								final String version = StringTool.object2String(hostMap.get("VERSION"));
								//启停组件小类型
								final String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));
								//启停配置文件
								final String filePath = Constant.STORM_YAML;
								//获取启停命令
								// chmod a+x auto.sh;./auto.sh -s ${clusterType} -2 ${tracker} -3 ${配置文件} -4 ${version}
								final String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));
								//主机ID
								String hostId = hostMap.get("HOST_ID");
								//启动文件
								String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.JSTORM_DIR + filePath;

								//线程日志文件目录
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("start jstorm logPath--->" + logPath);

								//线程日志文件
								Map<String, Object> logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, deployType, startFilePath);
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("start jstorm logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "JstormStartServiceImpl-Start-" + Thread.currentThread().getName());
								threadLogger.info("start jstorm, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + clusterType + ", deployFileType: " + deployType + ", startAutoFilePath: " + startFilePath);
								threadLogger.info("logName source --->" + logStrName);

								//Jstorm启动命令
								final String deployFile = MessageFormat.format(autoFilePath, Constant.JSTORM, deployType, appRootPath, version, dataClear,  StringTool.object2String(System.nanoTime()));
								logger.debug("Jstorm 启停脚本命令 ---> " + deployFile);

								//获取启停主机信息
								Map<String, Object> queryMap = new HashMap<String, Object>();
								queryMap.put("HOST_ID", hostId);
								threadLogger.debug("query host, params: " + queryMap);
								tempMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);


								//最终SSH2执行的命令
								String command = "";
								//获取Jstorm配置文件目录
								command += "cd " + appRootPath + Constant.Tools + ";" + deployFile;
								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));

								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);
								threadLogger.info("sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								logger.debug("执行启动Jstorm命令，命令: " + command);
								String result = cmdUtil.execMsg(command);
								threadLogger.info("result: " + result);
								logger.debug("执行启动Jstorm命令，结果: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));
							 
								if (result.indexOf(Constant.SUCCESS) < 0) {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("start jstorm " + deployType + " failed, cause by: " + result);
								} else {
									threadLogger.info("start jstorm " + deployType + " success!!");
									//该参数是用来添加到启动记录表
									resultMap.put("DEPLOY_FILE_TYPE", deployType);
									resultMap.put("FILE_PATH", startFilePath);
									resultMap.put("DEPLOY_TYPE", clusterType);
									resultMap.put("VERSION", version);
									String instPath = filePath;
									instPath = instPath.replace(deployType + "/", "");
									resultMap.put("INST_PATH", instPath);
									String softLinkPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.JSTORM_DIR + Constant.CONF + instPath;
									resultMap.put("SOFT_LINK_PATH", softLinkPath);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result--->" + resultMap.toString());
								Long endProTimes = System.currentTimeMillis();
								Long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.info("start jstorm final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("Jstorm程序启动失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "jstorm failed to start, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" :String.valueOf(tempMap.get("HOST_ID"))));
								resultList.add(resultMap);

								if (threadLogger != null) {
									threadLogger.error("start jstorm failed, cause by: ", e);
									threadLogger.info("ret result --->" + resultMap.toString());
									Long endProTimes = System.currentTimeMillis();
									Long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.info("start jstorm final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮询等待所有线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总启动Jstorm进程数:" + param.size() + ", 已经启动完成Jstorm进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}

				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("Jstorm进程启动完成，本次启动Jstorm进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");
				
				for(int i = 0 ; i < resultList.size();i++){
					Map<String, Object> singleMap = new HashMap<String, Object>();
					  Map<String,String> resultMap = resultList.get(i);
					  if(Constant.RST_CODE_SUCCESS.equalsIgnoreCase(resultMap.get(Constant.RST_CODE))){
						 singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 启动成功\n" + resultMap.get(Constant.RST_STR));
						  
						  //将配置信息添加到数据库保存
						  Map<String, String> fileMap = new HashMap<>();
						  fileMap.put("HOST_ID", resultMap.get("HOST_ID"));
						  fileMap.put("DEPLOY_FILE_TYPE", resultMap.get("DEPLOY_FILE_TYPE"));
						  fileMap.put("FILE_PATH", resultMap.get("FILE_PATH"));
						  fileMap.put("DEPLOY_TYPE", resultMap.get("DEPLOY_TYPE"));
						  fileMap.put("INST_PATH", resultMap.get("INST_PATH"));
						  fileMap.put("VERSION", resultMap.get("VERSION"));
						  fileMap.put("SOFT_LINK_PATH", resultMap.get("SOFT_LINK_PATH"));
						  fileMap.put("STATUS", Constant.STATE_ACTIVE);
						  fileMap.put("CLUSTER_ID", clusterId);
						  //存在匹配记录，更新
						  List<HashMap<String, String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey); 
						  if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){
							  coreService.updateObject("instConfig.updateConfigInfoByConditions", fileMap, dbKey);
						  }else{//不存在匹配记录，添加
							  coreService.insertObject("instConfig.addDcfDeployInstConfig", fileMap, dbKey);
						  }
					  }else{
						  singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 启动失败\n" + resultMap.get(Constant.RST_STR));
					  }
					  singleMap.put(Constant.RST_CODE, resultMap.get(Constant.RST_CODE));
					  resultMsgList.add(singleMap);
				 }
				
				//返回结果信息
				StringBuffer rstBuffer = new StringBuffer();
				String rstCode = Constant.RST_CODE_SUCCESS;
				for (int i=0; i<resultList.size(); i++) {
					Map<String, Object> hostRstMap = resultMsgList.get(i);
					rstBuffer.append(hostRstMap.get(Constant.RST_STR)).append(Constant.LINE_FLAG);
					if (Constant.RST_CODE_FAILED.equalsIgnoreCase(StringTool.object2String(hostRstMap.get(Constant.RST_CODE)))) {
						rstCode = Constant.RST_CODE_FAILED;
					}
				}
				rstMap.put(Constant.RST_STR, rstBuffer.toString());
				rstMap.put(Constant.RST_CODE, rstCode);
			} catch(Exception e){
				logger.error("启动Jstorm失败, 失败原因: ", e);
			} 
		}
		return rstMap;
 	}
	
	/**
	 * 停止Jstorm
	 * 
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopJstorm(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止Jstorm， 参数 ---> " + param + ", dbKey ---> " + dbKey);
		//主机处理结果保存对象
		List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
		//返回对象
		Map<String, Object> rstMap = new HashMap<String, Object>();
		
		if(!BlankUtil.isBlank(param)){
			//获取当前集群部署根目录
			Map<String, String> querySingleMap = param.get(0);
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", querySingleMap.get("CLUSTER_ID"));
			queryClusterMap.put("CLUSTER_TYPE", querySingleMap.get("CLUSTER_TYPE"));
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
			if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				throw new RuntimeException("集群信息查询失败, 请检查！");
			}
			//组件部署根目录
			final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//用户ID
			final String empeeId = querySingleMap.get("EMPEE_ID");
			//集群类型
			final String clusterType = StringTool.object2String(querySingleMap.get("CLUSTER_TYPE"));
			//获取启动组件执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				final List<Map<String,String>> resultList = new ArrayList<>();
				Long startTimes = System.currentTimeMillis();
				for(int i=0;i<param.size();i++){
					final Map<String ,String> hostMap =(Map<String, String>) param.get(i);

					//通过线程池异步调用停止脚本
					pool.execute(new Runnable() {
						public void run() {
							Map<String, String> resultMap = new HashMap<>();
							Long startProTimes = System.currentTimeMillis();
							Logger threadLogger = null;
							Map tempMap = null;
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

								//停止程序执行Shell命令
								String executeCmd = hostMap.get("autoFile");
								//停止程序小类型
								String deployFileType = hostMap.get("DEPLOY_FILE_TYPE");
								//配置文件
								String instName = hostMap.get("INST_PATH");
								//停止程序版本
								String version = hostMap.get("VERSION");
								//组件实例ID
								String instId = hostMap.get("INST_ID");
								//格式化后的执行命令
								final String autoFile = MessageFormat.format(executeCmd, Constant.JSTORM, deployFileType, instName, version, StringTool.object2String(System.nanoTime()));

								//日志文件路径
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("stop jstorm logPath--->" + logPath);

								Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
								//加密后的日志文件名称
								String logShaName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								//日志文件名称构成字串
								String logStrName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("stop jstorm logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "JstormStartServiceImpl-Stop-" + Thread.currentThread().getName());
								threadLogger.info("stop jstorm, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("logName source: " + logStrName);

								//根据主机ID查询主机信息
								threadLogger.debug("query host, params: " + hostMap);
								tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);

								String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);

								logger.debug("执行停止Jstorm命令，命令: " + command);
								threadLogger.info("sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: " + command);
								String result = cmdUtil.execMsg(command);
								logger.debug("执行停止Jstorm命令，结果: " + result);
								threadLogger.info("result: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								//用来修改记录状态
								resultMap.put("INST_ID", String.valueOf(hostMap.get("INST_ID")));

								if (result.indexOf(Constant.SUCCESS) < 0) {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("stop jstorm " + deployFileType + " failed, cause by: " + result);
								} else {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
									threadLogger.info("stop jstorm " + deployFileType + " success!!");
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result: " + resultMap.toString());
								long endProTimes = System.currentTimeMillis();
								long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.debug("stop fastdfs final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("Jstorm程序停止失败， 失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "jstorm failed to stop, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);

								if (threadLogger != null) {
									threadLogger.error("stop jstorm failed, cause by:", e);
									threadLogger.info("ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("stop jstorm final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");

								}
							}
						}
					});
				}
				while(resultList.size() < param.size()){
					logger.debug("本次总停止Jstorm进程数:" + param.size() + ", 已经停止完成Jstorm进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}

				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("Jstorm进程停止完成，本次停止Jstorm进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

				for(int i = 0 ; i < resultList.size();i++){
					Map<String, Object> singleMap = new HashMap<String, Object>();
					  Map<String,String> resultMap = resultList.get(i);
					  if(Constant.RST_CODE_SUCCESS.equalsIgnoreCase(resultMap.get(Constant.RST_CODE))){
						  //成功信息添加到List返回到前台
						  singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 停止成功\n" + resultMap.get(Constant.RST_STR));
						  
						  //停止成功，修改实例状态
						  Map<String, String> updateParams = new HashMap<String, String>();
						  updateParams.put("INST_ID", resultMap.get("INST_ID"));
						  updateParams.put("STATUS", Constant.STATE_NOT_ACTIVE);
						  coreService.updateObject("instConfig.updateDcfDeployInstConfig", updateParams, dbKey);
					  }else{
						  singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 停止失败\n" + resultMap.get(Constant.RST_STR));
					  }
					  singleMap.put(Constant.RST_CODE, resultMap.get(Constant.RST_CODE));
					  resultMsgList.add(singleMap);
				 }
				
				//返回结果信息
				StringBuffer rstBuffer = new StringBuffer();
				String rstCode = Constant.RST_CODE_SUCCESS;
				for (int i=0; i<resultList.size(); i++) {
					Map<String, Object> hostRstMap = resultMsgList.get(i);
					rstBuffer.append(hostRstMap.get(Constant.RST_STR)).append(Constant.LINE_FLAG);
					if (Constant.RST_CODE_FAILED.equalsIgnoreCase(StringTool.object2String(hostRstMap.get(Constant.RST_CODE)))) {
						rstCode = Constant.RST_CODE_FAILED;
					}
				}
				rstMap.put(Constant.RST_STR, rstBuffer.toString());
				rstMap.put(Constant.RST_CODE, rstCode);
			} catch(Exception e){
				logger.error("停止Jstorm失败， 失败原因: ", e);
			} 
		}
		return rstMap;
	}
}
