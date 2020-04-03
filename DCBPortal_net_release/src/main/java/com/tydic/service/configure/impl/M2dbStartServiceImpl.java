package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.service.configure.M2dbStartService;
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
  * @ClassName:    [M2dbStartServiceImpl]     
  * @Description:  [M2DB组件启停实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:13:29]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:13:29]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class M2dbStartServiceImpl implements M2dbStartService {
	/**
	 * M2db启停日志
	 */
	private static Logger logger = Logger.getLogger(M2dbStartServiceImpl.class);
	
	/**
	 * 核心Service
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 获取启停日志文件service对象
	 */
	@Autowired
	private InstConfigService instConfigService;
	
	/**
	 * 启动M2db
	 * 
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startM2db(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动M2db,参数 ---> " + param + ", dbKey ---> " + dbKey);
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
				
				//校验每台主机实例是否已经启动，每台主机相同实例只能启动一次
				StringBuffer rstBuffer = new StringBuffer();
				for (int i=0; i<param.size(); i++) {
					Map<String ,String> hostMap =(Map<String, String>) param.get(i);
					String hostIp = hostMap.get("HOST_IP");
					String instanceName = hostMap.get("INSTANCE_NAME");
					
					Map<String, Object> queryM2DBMap = new HashMap<String, Object>();
					queryM2DBMap.put("CLUSTER_ID", clusterId);
					queryM2DBMap.put("INST_PATH", instanceName);
					queryM2DBMap.put("HOST_ID", hostMap.get("HOST_ID"));
					queryM2DBMap.put("STATUS", Constant.STATE_ACTIVE);
					List<HashMap<String, Object>> instList = coreService.queryForList2New("instConfig.queryM2dbInstList", queryM2DBMap, dbKey);
					if (!BlankUtil.isBlank(instList)) {
						rstBuffer.append("error ---> 主机：" + hostIp + "，启动实例：" + instanceName + "，已经存在运行进程，请先停掉！").append(Constant.LINE_FLAG);
					}
				}
				if (!BlankUtil.isBlank(rstBuffer.toString())) {
					rstMap.put(Constant.RST_STR, rstBuffer.toString());
					rstMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
					return rstMap;
				}
				
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

								//M2DB实例名称
								String instanceName = StringTool.object2String(hostMap.get("INSTANCE_NAME"));
								//程序启停版本
								String version = StringTool.object2String(hostMap.get("VERSION"));

								// chmod a+x auto.sh;./auto.sh -s m2db -2 m2db -3 ${file} -4 ${version}
								String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));
								String autoFile = MessageFormat.format(autoFilePath, Constant.M2DB, Constant.M2DB, instanceName, version, StringTool.object2String(System.nanoTime()));
								//实例启动配置
								String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.M2DB;
								//主机ID
								String hostId = hostMap.get("HOST_ID");

								//线程日志文件目录
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("start m2db logPath--->" + logPath);

								//线程日志文件
								Map<String, Object> logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, clusterType, instanceName);
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("start m2db logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "M2dbStartServiceImpl-Start-" + Thread.currentThread().getName());
								threadLogger.info("start m2db, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + clusterType + ", deployFileType: " + clusterType + ", instName: " + instanceName);
								threadLogger.info("logName source --->" + logStrName);

								//获取启停主机信息
								Map<String, Object> queryMap = new HashMap<String, Object>();
								queryMap.put("HOST_ID", hostId);
								threadLogger.debug("query host, params: " + queryMap);
								tempMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);

								String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);

								logger.debug("执行启动M2db命令，命令: " + command);
								threadLogger.debug("sshIp: " + sshIp +", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								String result = cmdUtil.execMsg(command);
								threadLogger.info("result: " + result);
								logger.debug("执行启动M2db命令，结果: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								if (result.toLowerCase().indexOf("deal success") >= 0) {
									//该参数是用来添加到启动记录表
									resultMap.put("DEPLOY_FILE_TYPE", clusterType);
									resultMap.put("FILE_PATH", startFilePath);
									resultMap.put("DEPLOY_TYPE", clusterType);
									resultMap.put("INST_PATH", instanceName);
									resultMap.put("VERSION", version);
									resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.M2DB_DIV + Constant.CFG_DIR);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
									threadLogger.info("start m2db success!!");
								} else {
									threadLogger.error("start m2db failed, cause by: " + result);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result--->" + resultMap.toString());
								Long endProTimes = System.currentTimeMillis();
								Long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.info("start m2db final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("M2DB程序启动失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "m2db failed to start, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);
								if (threadLogger != null) {
									threadLogger.error("start m2db failed, cause by: ", e);
									threadLogger.info("ret result --->" + resultMap.toString());
									Long endProTimes = System.currentTimeMillis();
									Long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.info("start m2db final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮序等待所有线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总启动M2DB进程数:" + param.size() + ", 已经启动完成M2DB进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("M2DB进程启动完成，本次启动M2DB进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");
				
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
						  //判断是否唯一
						  List<HashMap<String,String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey); 
						  if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){//存在匹配记录，更新
							  coreService.updateObject("instConfig.updateConfigInfoByConditions", fileMap, dbKey);
							  logger.debug("M2DB更新实例表数据成功...");
						  }else{//不存在匹配记录，添加
							  coreService.insertObject("instConfig.addDcfDeployInstConfig", fileMap, dbKey);
							  logger.debug("M2DB添加实例表数据成功...");
						  }
						  
						  //删除当前主机非当前版本统一实例进程
						  Map<String, Object> m2dbMap = new HashMap<String, Object>();
						  m2dbMap.put("CLUSTER_ID", clusterId);
						  m2dbMap.put("DEPLOY_TYPE", resultMap.get("DEPLOY_TYPE"));
						  m2dbMap.put("INST_PATH", resultMap.get("INST_PATH"));
						  m2dbMap.put("HOST_ID", resultMap.get("HOST_ID"));
						  m2dbMap.put("STATUS", Constant.STATE_NOT_ACTIVE);
						  int delCount = coreService.deleteObject2New("instConfig.delDcfDeployInstConfigByM2db", m2dbMap, dbKey);
						  logger.debug("删除M2DB实例， 删除实例个数: " + delCount);
					  }else{
						  singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP") + " 启动失败\n" + resultMap.get(Constant.RST_STR));
					  }
					  singleMap.put(Constant.RST_CODE, resultMap.get(Constant.RST_CODE));
					  resultMsgList.add(singleMap);
				 }
				
				//返回结果信息
				rstBuffer = new StringBuffer();
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
				logger.error("启动M2db失败, 失败原因 ---> ", e);
			}
		}
		return rstMap;
 	}
	
	/**
	 * 停止M2db
	 * 
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopM2db(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止M2db， 参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			//集群类型
			final String clusterType = querySingleMap.get("CLUSTER_TYPE");
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

								//程序停止命令
								String executeCmd = hostMap.get("autoFile");
								//程序停止版本
								String version = StringTool.object2String(hostMap.get("VERSION"));
								//程序停止配置文件
								String instName = StringTool.object2String(hostMap.get("INST_PATH"));
								//格式化后的执行命令
								//chmod a+x auto.sh;./auto.sh -e M2db -2 tracker -3  配置文件实例+配置文件名称 -4 ${version}
								String autoFile = MessageFormat.format(executeCmd, Constant.M2DB, Constant.M2DB, instName, version, StringTool.object2String(System.nanoTime()));
								//chmod a+x auto.sh;mkdir -p exec_temp/{4};cp auto.sh exec_temp/{4}/auto.sh;cd exec_temp/{4};./auto.sh -e {0} -2 {1} -3 {2} -4 {3};rm -rf ../{4};
								//m2db  m2db  实例号 版本号

								//组件程序启停实例ID
								String instId = hostMap.get("INST_ID");
								//日志文件路径
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("stop m2db logPath--->" + logPath);

								Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
								//加密后的日志文件名称
								String logShaName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								//日志文件名称构成字串
								String logStrName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("stop m2db logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "M2dbStartServiceImpl-Stop-" + Thread.currentThread().getName());
								threadLogger.info("stop m2db, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
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
								logger.debug("执行停止M2db命令，命令: " + command);
								threadLogger.info("stop m2db, sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								String result = cmdUtil.execMsg(command);
								threadLogger.info("result: " + result);
								logger.debug("执行停止M2db命令，结果: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								//用来修改记录状态
								resultMap.put("INST_ID", String.valueOf(hostMap.get("INST_ID")));
								if (result.toLowerCase().indexOf("deal success") >= 0) {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
									threadLogger.info("stop m2db success!!");
								} else {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("stop m2db failed, cause by: " + result);
								}

								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result: " + resultMap.toString());
								long endProTimes = System.currentTimeMillis();
								long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.debug("stop m2db final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("M2DB程序停止失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "m2db failed to stop, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" :String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);
								if (threadLogger != null) {
									threadLogger.error("stop m2db failed, cause by:", e);
									threadLogger.info("ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("stop m2db final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮询等待所有的线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总停止M2DB进程数:" + param.size() + ", 已经停止完成M2DB进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("M2DB进程停止完成，本次停止M2DB进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");
				
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
				logger.error("停止M2DB失败， 失败原因: ", e);
			}
		}
		return rstMap;
	}
}
