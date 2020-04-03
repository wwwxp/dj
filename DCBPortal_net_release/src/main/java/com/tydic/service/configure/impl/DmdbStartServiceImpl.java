package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.DmdbStartService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.SingletonThreadPool;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
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
  * @ClassName:    [DmdbStartServiceImpl]     
  * @Description:  [DMDB组件启停实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:09:57]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:09:57]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class DmdbStartServiceImpl implements DmdbStartService {
	/**
	 * DMDB启停日志
	 */
	private static Logger logger = Logger.getLogger(DmdbStartServiceImpl.class);
	
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
	 * 启动Dmdb
	 * 
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startDmdb(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动Dmdb,参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			//集群编码
			final String clusterCode = querySingleMap.get("CLUSTER_CODE");
			//操作用户ID
			final String empeeId = StringTool.object2String(querySingleMap.get("EMPEE_ID"));
			//获取启动组件执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				final List<Map<String,String>> resultList = new ArrayList<>();
				Long startTimes = System.currentTimeMillis();
				for(int i=0;i<param.size();i++){
					final Map<String ,String> hostMap =(Map<String, String>) param.get(i);
					//异步调用执行远程命令
					pool.execute(new Runnable() {
						public void run() {
							Map<String, String> resultMap = new HashMap<>();
							Long startProTimes = System.currentTimeMillis();
							Logger threadLogger = null;
							Map tempMap = null;
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
								//程序启停版本
								String version = StringTool.object2String(hostMap.get("VERSION"));
								//启停组件小类型
								String deployType = StringUtils.defaultIfBlank(StringTool.object2String(hostMap.get("DEPLOY_TYPE")), Constant.DMDB);
								//启停配置文件
								String filePath = StringUtils.defaultIfBlank(StringTool.object2String(hostMap.get("FILE")), "mdb.conf");
								//获取启停命令
								String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));
								//DMDB前台传递的参数为配置文件名称,例如 : dmdb/192.168.161.26_01/mdb.conf
								String deployFile = MessageFormat.format(autoFilePath, Constant.DMDB, deployType, filePath, version, StringTool.object2String(System.nanoTime()));
								logger.debug("DMDB启动脚本命令 ---> " + deployFile);
								//配置文件路径
								String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.DMDB_DIR + filePath;
								//主机ID
								String hostId = hostMap.get("HOST_ID");

								//线程日志文件目录
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("start dmdb logPath--->" + logPath);

								//线程日志文件
								Map<String, Object> logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, deployType, startFilePath);
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("start dmdb logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "DmdbStartServiceImpl-Start-" + Thread.currentThread().getName());
								threadLogger.info("start dmdb, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + clusterType + ", deployFileType: " + deployType + ", startAutoFilePath: " + startFilePath);
								threadLogger.info("logName source --->" + logStrName);


								//获取启停主机信息
								Map<String, Object> queryMap = new HashMap<String, Object>();
								queryMap.put("HOST_ID", hostId);
								threadLogger.debug("query host, params: " + queryMap);
								tempMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);

								//最终SSH2执行的命令
								String command = "";
								//获取配置文件名称
								command += "cd " + appRootPath + Constant.Tools + ";" + deployFile;
								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);
								logger.debug("执行启动DMDB命令，命令: " + command);
								threadLogger.debug("sshIp: " + sshIp +", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								String result = cmdUtil.execMsg(command);
								threadLogger.info("result: " + result);
								logger.debug("执行启动DMDB命令，结果: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								if (result.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0
										|| result.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
									threadLogger.error("start dmdb " + deployType +" failed, cause by: " + result);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								} else {
									threadLogger.info("start dmdb success!!");
									//该参数是用来添加到启动记录表
									resultMap.put("DEPLOY_FILE_TYPE", deployType);
									resultMap.put("FILE_PATH", startFilePath);
									resultMap.put("DEPLOY_TYPE", clusterType);
									resultMap.put("VERSION", version);
									resultMap.put("CONFIG_PATH", filePath);
									String instPath = filePath;
									if (instPath.indexOf("/") != -1) {
										resultMap.put("INST_PATH", instPath.split("/")[1]);
									} else {
										resultMap.put("INST_PATH", instPath);
									}
									//resultMap.put("INST_PATH", instPath);

									//完整的配置文件路径
									String linkPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DMDB_DIR + Constant.CFG_DIR + instPath;
									resultMap.put("SOFT_LINK_PATH", linkPath);

									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result--->" + resultMap.toString());
								Long endProTimes = System.currentTimeMillis();
								Long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.info("start dmdb " + deployType + " final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("Dmdb程序启动失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "dmdb failed to start, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);
								if (threadLogger != null) {
									threadLogger.error("start dmdb failed, cause by: ", e);
									threadLogger.info("ret result --->" + resultMap.toString());
									Long endProTimes = System.currentTimeMillis();
									Long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.info("start dmdb final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮询等待所有线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总启动DMDB进程数:" + param.size() + ", 已经启动完成DMDB进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}

				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("Dmdb进程启动完成，本次启动Dmdb进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

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
						  fileMap.put("CONFIG_PATH", resultMap.get("CONFIG_PATH"));
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
				logger.error("启动Dmdb失败, 失败原因: ", e);
			}
		}
		return rstMap;
 	}
	
	
	/**
	 * 获取文件内容
	 * 
	 * @param appRootPath 部署主机根目录
	 * @param filePath 文件下载路径
	 * @param fileName 文件下载名称
	 * @param dbKey 数据库连接ID
	 * @return String 文件内容
	 */
//	private String getFileContent(String localRootPath, String filePath, String fileName, String dbKey){
//		logger.debug("获取远程配置文件内容，参数 ---> " + filePath + ", dbKey ---> " + dbKey);
//		// 获取所有参数信息
//		String userName = SessionUtil.getConfigValue("FTP_USERNAME");
//		String password = SessionUtil.getConfigValue("FTP_PASSWD");
//		String ftp_type = SessionUtil.getConfigValue("FTP_TYPE");
//		String ip = SessionUtil.getConfigValue("FTP_IP");
//		
//		// 文件内容
//		String fileContent = "";
//		String localPath = FileTool.exactPath(localRootPath + Constant.TMP + System.currentTimeMillis()) + fileName;
//		Trans trans = null;
//		try {
//			trans = FTPUtils.getFtpInstance(ip, userName, password, ftp_type);
//			trans.login();
//			trans.get(filePath, localPath);
//			// 将文件转成字符串
//			fileContent = FileUtil.readFileUnicode(localPath);
//			// 去掉尾部所有空格
//			int len = fileContent.length();
//			int st = 0;
//			char[] val = fileContent.toCharArray();
//			while ((st < len) && (val[len - 1] <= ' ')) {
//				len--;
//			}
//			fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
//		} catch (Exception e) {
//			logger.error("获取远程配置文件内容失败 ---> ", e);
//		} finally {
//			if (trans != null) {
//				trans.close();
//			}
//			if (FileTool.exists(localPath)) {
//				try {
//					FileUtil.deleteFile(localPath);
//				} catch (IOException e) {
//					logger.error("删除本地临时文件失败 ---> ", e);
//				}
//			}
//		}
//		return fileContent;
//	}
	
	/**
	 * 停止DMDB
	 * 
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopDmdb(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止Dmdb， 参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			//集群编码
			final String clusterCode = querySingleMap.get("CLUSTER_CODE");
			//用户ID
			final String empeeId = querySingleMap.get("EMPEE_ID");
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
								//格式化后的执行命令
								String configPath = "";
								if (StringUtils.equals(deployFileType, "sync_pattern")
										|| StringUtils.equals(deployFileType, "mgr_pattern")
										|| StringUtils.equals(deployFileType, "watcher_pattern")
										|| StringUtils.equals(deployFileType, "movesync_pattern")) {
									configPath = FileTool.exactPath("instance_pattern")+instName;
								} else {
									configPath = FileTool.exactPath(deployFileType)+instName;
								}
								final String autoFile = MessageFormat.format(executeCmd, Constant.DMDB, deployFileType, configPath, version, StringTool.object2String(System.nanoTime()));

								//组件程序启停实例ID
								String instId = hostMap.get("INST_ID");
								//日志文件路径
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("stop dmdb logPath--->" + logPath);

								Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
								//加密后的日志文件名称
								String logShaName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								//日志文件名称构成字串
								String logStrName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("stop dmdb logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "DmdbStartServiceImpl-Stop-" + Thread.currentThread().getName());
								threadLogger.info("stop dmdb, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
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

								logger.debug("执行停止Dmdb命令，命令: " + command);
								threadLogger.info("sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								String result = cmdUtil.execMsg(command);
								threadLogger.info("result: " + result);
								logger.debug("执行停止Dmdb命令，结果: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								//用来修改记录状态
								resultMap.put("INST_ID", String.valueOf(hostMap.get("INST_ID")));
								if (result.indexOf(Constant.OPERATOR_FAILED) >= 0) {
									threadLogger.error("stop dmdb failed, cause by: " + result);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								} else {
									threadLogger.info("stop dmdb success!!");
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result: " + resultMap.toString());
								long endProTimes = System.currentTimeMillis();
								long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.debug("stop dmdb " + deployFileType + " final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("Dmdb程序停止失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "dmdb failed to stop, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);
								if (threadLogger != null) {
									threadLogger.error("stop dmdb failed, cause by:", e);
									threadLogger.info("ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("stop dmdb final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				while(resultList.size() < param.size()){
					logger.debug("本次总停止Dmdb进程数:" + param.size() + ", 已经停止完成Dmdb进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("Dmdb进程停止完成，本次停止Dmdb进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

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
				logger.error("停止Dmdb失败， 失败原因: ", e);
			}
		}
		return rstMap;
	}
}
