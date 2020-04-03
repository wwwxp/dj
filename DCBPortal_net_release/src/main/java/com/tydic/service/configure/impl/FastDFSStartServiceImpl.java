package com.tydic.service.configure.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.FastDFSStartService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ThreadInfo;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [FastDFSStartServiceImpl]     
  * @Description:  [FastDFS组件启停实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:10:08]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:10:08]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class FastDFSStartServiceImpl implements FastDFSStartService {
	/**
	 * fastdfs启停日志
	 */
	private static Logger logger = Logger.getLogger(FastDFSStartServiceImpl.class);
	
	/**
	 * 部署模式-Storage
	 */
	private static final String FASTDFS_STORAGE = "storage";
	
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
	 * 启动fastdfs
	 * 
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startFastDFS(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动FastDfFS,参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			//本地临时文件根目录
			final String localRootPath = StringTool.object2String(querySingleMap.get("localRootPath")); 
			//组件部署根目录
			final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//集群ID
			final String clusterId = querySingleMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = querySingleMap.get("CLUSTER_TYPE");
			//操作用户ID
			final String empeeId = StringTool.object2String(querySingleMap.get("EMPEE_ID"));
			//集群编码
			final String clusterCode = querySingleMap.get("CLUSTER_CODE");
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
							Map<String, Object> tempMap = null;
							Logger threadLogger = null;
							Long startProTimes = System.currentTimeMillis();
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

								//启动模式
								String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));
								//程序启停版本
								String version = StringTool.object2String(hostMap.get("VERSION"));
								//启动配置文件实例
								String file = StringTool.object2String(hostMap.get("FILE"));
								//启动命令
								String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));
								String autoFile = MessageFormat.format(autoFilePath, Constant.FASTDFS, deployType, file, version, StringTool.object2String(System.nanoTime()));
								//主机ID
								String hostId = hostMap.get("HOST_ID");

								//启动文件
								String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.FASTDFS_DIR + file;

								//线程日志文件目录
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("start fastdfs logPath--->" + logPath);

								//线程日志文件
								Map<String, Object> logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, deployType, startFilePath);
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("start fastdfs logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "FastDFSStartServiceImpl-Start-" + Thread.currentThread().getName());
								threadLogger.info("start fastdfs, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + clusterType + ", deployFileType: " + deployType + ", startAutoFilePath: " + startFilePath);
								threadLogger.info("logName source --->" + logStrName);

								//获取启停主机信息
								Map<String, Object> queryMap = new HashMap<String, Object>();
								queryMap.put("HOST_ID", hostId);
								tempMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
								threadLogger.debug("query host, params: " + queryMap.toString());

								//创建fastDFS base_path目录
								//该参数是用来添加到启动记录表
								String filePath = SessionUtil.getConfigValue("FTP_ROOT_PATH") + Constant.CONF + Constant.PLAT_CONF
										+ Constant.RELEASE_DIR + Constant.FASTDFS_DIR + FileTool.exactPath(clusterCode) + file;
								String fileName = file.substring(file.lastIndexOf("/") + 1);
								InputStreamReader reader = null;
								BufferedReader bufferedReader = null;
								String configContent = "";
								String basePath = "";
								List<Map<String, String>> storeList = new ArrayList<Map<String, String>>();
								try {
									threadLogger.info("getFileContent, params, localTempFile: " + localRootPath +", filePath: " + filePath + ", fileName: " + fileName);
									configContent = getFileContent(localRootPath, filePath, fileName, dbKey);
									threadLogger.info("getFileContent, result: " + configContent);

									reader = new InputStreamReader(
											new ByteArrayInputStream(configContent.getBytes()));
									bufferedReader = new BufferedReader(reader);
									String lineContent = "";
									while ((lineContent = bufferedReader.readLine()) != null) {
										if (lineContent.startsWith("store_path".trim()) && !lineContent.startsWith("store_path_count".trim())) {
											if (lineContent.indexOf("=") != -1) {
												String storePath = (lineContent.split("=")[1]).trim();
												String linkName = ((lineContent.split("=")[0]).replace("store_path", "")).trim();

												Map<String, String> storeMap = new HashMap<String, String>();
												storeMap.put("STORE_NAME", linkName);
												storeMap.put("STORE_PATH", storePath);
												storeList.add(storeMap);
											}
										}

										if (lineContent.startsWith("base_path".trim())) {
											if (lineContent.indexOf("=") != -1) {
												basePath = (lineContent.split("=")[1]).trim();
											}
										}
									}
									threadLogger.info("storeList: " + storeList.toString());
									threadLogger.info("basePath: " + basePath);
								} catch (IOException e) {
									threadLogger.error("getFileContent exception, cause by:", e);
									logger.error("创建FastDFS Base_Path失败", e);
								} finally {
									try {
										if (reader != null) {
											reader.close();
										}
										if (bufferedReader != null) {
											bufferedReader.close();
										}
										threadLogger.info("getFileContent stream close success!");
									} catch (IOException e) {
										logger.error("获取配置文件内容，关闭流失败 --->", e);
									}
								}


								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);

								String result = "";
								String command = "";

								if (!BlankUtil.isBlank(storeList) && FASTDFS_STORAGE.equalsIgnoreCase(hostMap.get("deployType"))) {
									String shellCmd = "";
									for (int i = 0; i < storeList.size(); i++) {
										String storeName = storeList.get(i).get("STORE_NAME");
										String storePath = storeList.get(i).get("STORE_PATH");
										storePath = FileTool.exactPath(storePath) + "data";
										shellCmd += "mkdir -p " + storePath + ";";
										if (BlankUtil.isBlank(storeName)) {
											shellCmd += "ln -s -f " + storePath + " " + FileTool.exactPath(storePath) + "M00;";
										} else if (storeName.length() == 1) {
											shellCmd += "ln -s -f " + storePath + " " + FileTool.exactPath(storePath) + "M0" + storeName + ";";
										} else {
											shellCmd += "ln -s -f " + storePath + " " + FileTool.exactPath(storePath) + "M" + storeName + ";";
										}
									}
									threadLogger.debug("fastdfs storage, create soft link, exeCmd: " + shellCmd);
									result += cmdUtil.execMsg(shellCmd);
									threadLogger.debug("fastdfs storage, creaye soft link, result: " + result);
									logger.debug("创建目录成功...");
								}

								//创建fastDFS basePath目录
								if (!BlankUtil.isBlank(basePath)) {
									command = "mkdir -p " + basePath + "; ";
								}
								command += "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								logger.debug("执行启动FastDFS命令，命令: " + command);
								threadLogger.info("sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("execCmd: <label style='color:green;'>" + command + "</label>");
								result = cmdUtil.execMsg(command);
								logger.debug("执行启动FastDFS命令，结果: " + result);
								threadLogger.info("result: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								if ((result.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0 && result.toLowerCase().indexOf("ngx_http_fastdfs_set pid=") < 0)
										|| result.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
									threadLogger.error("start fastdfs " + deployType + " failed, cause by: " + result);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								} else {
									threadLogger.info("start fastdfs " + deployType + " success!!");
									//该参数是用来添加到启动记录表
									resultMap.put("DEPLOY_FILE_TYPE", deployType);
									resultMap.put("FILE_PATH", startFilePath);
									resultMap.put("DEPLOY_TYPE", clusterType);
									resultMap.put("INST_PATH", file);
									resultMap.put("CONFIG_PATH", file);
									resultMap.put("VERSION", version);
									if (Constant.NGINX.equals(deployType)) {
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.FASTDFS_DIR + "nginx/conf/" + file);
									} else {
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.FASTDFS_DIR + Constant.FASTDFS_ETC_FDFS + file);
									}
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("ret result--->" + resultMap.toString());
								Long endProTimes = System.currentTimeMillis();
								Long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.info("start fastdfs final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("FastDFS程序启动失败，失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "fastdfs failed to start, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);

								if (threadLogger != null) {
									threadLogger.error("start fastdfs failed, cause by: ", e);
									threadLogger.info("ret result --->" + resultMap.toString());
									Long endProTimes = System.currentTimeMillis();
									Long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.info("start fastdfs final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮序等待所有线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总启动FastDFS进程数:" + param.size() + ", 已经启动完成FastDFS进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("FastDFS进程启动完成，本次启动FastDFS进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

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
						  //判断是否唯一
						  List<HashMap<String,String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey); 
						  if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){//存在匹配记录，更新
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
				logger.error("启动FastDFS失败, 失败原因 --->", e);
			}
		}
		return rstMap;
 	}
	
	
	/**
	 * 获取文件内容
	 * 
	 * @param localRootPath 部署主机根目录
	 * @param filePath 文件下载路径
	 * @param fileName 文件下载名称
	 * @param dbKey 数据库连接ID
	 * @return String 文件内容
	 */
	private String getFileContent(String localRootPath, String filePath, String fileName, String dbKey){
		logger.debug("获取远程配置文件内容，参数 ---> " + filePath + ", fileName ---> "+ fileName + ", dbKey ---> " + dbKey);
		// 获取所有参数信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		// 文件内容
		String fileContent = "";
		String localPath = FileTool.exactPath(localRootPath + Constant.TMP + System.currentTimeMillis()) + fileName;
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			trans.get(filePath, localPath);
			// 将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath);
			// 去掉尾部所有空格
			int len = fileContent.length();
			int st = 0;
			char[] val = fileContent.toCharArray();
			while ((st < len) && (val[len - 1] <= ' ')) {
				len--;
			}
			fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
		} catch (Exception e) {
			logger.error("获取远程配置文件内容失败 ---> ", e);
		} finally {
			if (trans != null) {
				trans.close();
			}
			if (FileTool.exists(localPath)) {
				try {
					FileUtil.deleteFile(localPath);
				} catch (IOException e) {
					logger.error("删除本地临时文件失败 ---> ", e);
				}
			}
		}
		return fileContent;
	}
	
	/**
	 * 停止FastDFS
	 * 
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopFastDFS(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止FastDFS， 参数 ---> " + param + ", dbKey ---> " + dbKey);
		//返回的消息
		List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
		
		//处理结果
		Map<String, Object> rstMap = new HashMap<String, Object>();
		
		if(!BlankUtil.isBlank(param)){
			//获取当前集群部署根目录
			Map<String, String> queryMap = param.get(0);
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", queryMap.get("CLUSTER_ID"));
			queryClusterMap.put("CLUSTER_TYPE", queryMap.get("CLUSTER_TYPE"));
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
			if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				throw new RuntimeException("集群信息查询失败, 请检查！");
			}
			//组件部署根目录
			final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//集群ID
			final String clusterId = StringTool.object2String(queryMap.get("CLUSTER_ID"));
			//用户ID
			final String empeeId = queryMap.get("EMPEE_ID");
			//集群类型
			final String clusterType = StringTool.object2String(queryMap.get("CLUSTER_TYPE"));
			//单例获取执行线程
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
								//程序小类
								final String deployFileType = hostMap.get("DEPLOY_FILE_TYPE");
								//程序停止版本
								final String version = hostMap.get("VERSION");
								//程序停止配置文件
								final String instName = hostMap.get("INST_PATH");
								//格式化后的执行命令
								//chmod a+x auto.sh;./auto.sh -e fastdfs -2 tracker -3  配置文件实例+配置文件名称 -4 ${version}
								final String autoFile = MessageFormat.format(executeCmd, Constant.FASTDFS, deployFileType, instName, version, StringTool.object2String(System.nanoTime()));
								//根据主机ID查询主机信息
								tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);

								//组件程序启停实例ID
								String instId = hostMap.get("INST_ID");
								//日志文件路径
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("stop fastdfs logPath--->" + logPath);

								Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
								//加密后的日志文件名称
								String logShaName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								//日志文件名称构成字串
								String logStrName = org.apache.commons.lang.ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("stop fastdfs logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "FastdfsStartServiceImpl-Stop-" + Thread.currentThread().getName());
								threadLogger.info("stop fastdfs, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("logName source: " + logStrName);

								String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								String sshIp = String.valueOf(tempMap.get("HOST_IP"));
								String sshUser = String.valueOf(tempMap.get("SSH_USER"));
								String sshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(sshIp, sshUser, sshPasswd);

								threadLogger.info("sshIp: " + sshIp + ", sshUser: " + sshUser + ", sshPasswd: " + sshPasswd);
								threadLogger.info("exeCmd: <label style='color:green;'>" + command + "</label>");
								logger.debug("执行停止FastDFS命令，命令: " + command );
								String result = cmdUtil.execMsg(command);
								logger.debug("执行停止FastDFS命令，结果: " + result);
								threadLogger.info("result: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", sshIp);

								//用来修改记录状态
								resultMap.put("INST_ID", instId);
								if ((result.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0 && result.toLowerCase().indexOf("pid [") == -1)
										|| result.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("stop fastdfs " + deployFileType + " failed, cause by: " + result);
								} else {
									threadLogger.info("stop fastdfs " + deployFileType + " success!!");
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
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
								logger.error("FastDFS程序停止失败， 失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "FastDFS failed to stop, cause: " + e.getMessage());
								resultMap.put("HOST_IP", (tempMap == null ? "" : String.valueOf(tempMap.get("HOST_IP"))));
								resultList.add(resultMap);

								if (threadLogger != null) {
									threadLogger.error("stop fastdfs failed, cause by:", e);
									threadLogger.info("ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("stop fastdfs final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}
				
				//轮询等待所有的线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总停止FastDFS进程数:" + param.size() + ", 已经停止完成FastDFS进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("FastDFS进程停止完成，本次停止FastDFS进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

				for(int i = 0 ; i < resultList.size();i++){
					Map<String, Object> singleMap = new HashMap<String, Object>();
					Map<String,String> resultMap = resultList.get(i);
					if(Constant.RST_CODE_SUCCESS.equalsIgnoreCase(StringTool.object2String(resultMap.get(Constant.RST_CODE)))){
						//停止成功，修改实例状态
						Map<String, String> updateParams = new HashMap<String, String>();
						updateParams.put("INST_ID", resultMap.get("INST_ID"));
						updateParams.put("STATUS", Constant.STATE_NOT_ACTIVE);
						coreService.updateObject("instConfig.updateDcfDeployInstConfig", updateParams, dbKey);
						
						//返回前台信息
						singleMap.put(Constant.RST_STR, resultMap.get("HOST_IP")+" 停止成功\n" + resultMap.get(Constant.RST_STR));
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
					rstBuffer.append(hostRstMap.get(Constant.RST_STR)).append("\n");
					if (Constant.RST_CODE_FAILED.equalsIgnoreCase(StringTool.object2String(hostRstMap.get(Constant.RST_CODE)))) {
						rstCode = Constant.RST_CODE_FAILED;
					}
				}
				rstMap.put(Constant.RST_STR, rstBuffer.toString());
				rstMap.put(Constant.RST_CODE, rstCode);
			} catch(Exception e){
				logger.error("停止FastDFS失败， 失败原因: ", e);
			}
		}
		return rstMap;
	}
	
	
	
	/**
	 * 获取指定目录下所有的文件列表信息
	 * @param params
	 * @param dbKey
	 * @return List
	 */
	public List<Map<String, Object>> getCurrentPathFileList(Map<String, String> params, String dbKey) {
		logger.debug("获取当前目录下所有的文件列表信息, 参数 ---> " + params + ", dbKey---> " + dbKey);
		
		List<Map<String, Object>> retList = new ArrayList<>();
		try {
			//集群类型
			String clusterType = params.get("CLUSTER_TYPE");
			//当前集群编码
			String clusterCode = params.get("CLUSTER_CODE");
			
			//获取部署主机配置文件信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			Trans sftClient = FTPUtils.getFtpInstance(ftpDto);
			sftClient.login();
			
			//部署主机配置文件目录,获取集群编码下对应的所有配置文件
			String path = ftpDto.getFtpRootPath() + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR + FileTool.exactPath(clusterType) + FileTool.exactPath(clusterCode);
			
			//遍历获取所有当前类型所有配置文件
			List<FileRecord> files = new ArrayList<FileRecord>();
			String rootId="";
			
			if(Constant.CLOUDB.equals(clusterType)){
				// 创建一个根目录
				FileRecord file = new FileRecord();
				rootId= UUID.randomUUID().toString();
				file.setCurrId(rootId);
				file.setFileName(Constant.CLOUDB);
				file.setFilePath("");
				file.setParentId(null);
				file.setFileType('D');
				files.add(file);
			}
			getAllFileList(sftClient, files, path, rootId);
			
			for (FileRecord file : files) {
				Map<String, Object> fileMap = new HashMap<>();
				fileMap.put("id", file.getCurrId());
				fileMap.put("parentId", file.getParentId());
				fileMap.put("fileName", file.getFileName());
				fileMap.put("filePath", file.getFilePath());
				fileMap.put("fileType", String.valueOf(file.getFileType()));
				fileMap.put("fileName", file.getFileName());
				fileMap.put("relativePath", StringTool.object2String(file.getFilePath().replace(path, "") + "/" + file.getFileName()));
				retList.add(fileMap);
				
				//设置根节点名称
				if (BlankUtil.isBlank(file.getParentId())) {
					fileMap.put("rootName", file.getFileName());
					fileMap.put("parentRelativePath", "");
				}
			}
			
			//通过目录节点构建树关系
			for (int i=0; i<retList.size(); i++) {
				String currentId = StringTool.object2String(retList.get(i).get("id"));
				String relativePath = StringTool.object2String(retList.get(i).get("relativePath"));
				if (relativePath.startsWith("/")) {
					retList.get(i).put("relativePath", relativePath.substring(1));
				}
				for (int j=0; j<retList.size(); j++) {
					String parentId = StringTool.object2String(retList.get(j).get("parentId"));
					if (parentId.equals(currentId)) {
						retList.get(j).put("rootName", retList.get(i).get("rootName"));
						retList.get(j).put("parentRelativePath", retList.get(i).get("relativePath"));
					}
				}
			}
		} catch (Exception e) {
			logger.error("获取启停配置文件失败 ---> ", e);
		}
		logger.debug("获取配置文件列表成功,配置文件大小:" + (retList.size()));
		
		//对结果文件进行排序
		Collections.sort(retList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> fileObj1, Map<String, Object> fileObj2) {
				String prevFileName = StringTool.object2String(fileObj1.get("fileName"));
				String nextFileName = StringTool.object2String(fileObj2.get("fileName"));
				Collator collator = Collator.getInstance(Locale.CHINA);
				return collator.compare(prevFileName, nextFileName);
			}
		});
		return retList;
	}
	
	/**
	 * 递归获取所有的文件
	 * @param trans ftp/sftp连接对象
	 * @param fileList  远程主机文件列表
	 * @param path  远程主机目录名称
	 * @param parentId 父目录ID
	 * @return 
	 */
	private void getAllFileList(Trans trans, List<FileRecord> fileList, String path, String parentId) {
		try {
			List<FileRecord> firstList = trans.getFileList(path);
			for (int i=0; i<firstList.size(); i++) {
				//设置当前文件ID
				firstList.get(i).setCurrId(UUID.randomUUID().toString());
				//设置当前节点父节点
				firstList.get(i).setParentId(parentId);
				//将当前节点加入到列表
				fileList.add(firstList.get(i));
				//判断当前文件是否为目录，如果为目录则判断是否包含子文件，递归遍历
				if (String.valueOf(FileRecord.DIR).equals(String.valueOf(firstList.get(i).getFileType()))) {
					String fileName = firstList.get(i).getFileName();
					String filePath = firstList.get(i).getFilePath() + "/" + fileName;
					getAllFileList(trans, fileList, filePath, firstList.get(i).getCurrId());
				}
			}
		} catch (Exception e) {
			logger.error("获取配置文件失败 --->", e);
		}
	}


	/**
	 * 保存用户初始化数据
	 * 
	 * @param params
	 * @param dbKey
	 * @return Map
	 */
	@Override
	public Map<String, Object> addOperator(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("保存用户配置信息, 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		Map<String, Object> rstMap = new HashMap<String, Object>();
		try {
			//获取集群编码类型
			String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
			//集群名称
			String clusterName = StringTool.object2String(params.get("CLUSTER_NAME"));
			//集群类型
			String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
			//批次名称
			String batchName = StringTool.object2String(params.get("BATCH_NAME"));
			
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("BATCH_NAME", batchName.trim());
			queryMap.put("CLUSTER_TYPE", clusterType);
			List<HashMap<String, Object>> existsList = coreService.queryForList2New("startConfig.queryBatchNameExistsList", queryMap, dbKey);
			if (!BlankUtil.isBlank(existsList)) {
				throw new RuntimeException("集群名称:【" + clusterName + "】，已经存在该批次名称， 请重新输入！");
			}
			
			//获取配合信息
			List<Map<String, String>> hostList = (List<Map<String, String>>) params.get("HOST_LIST");
			for (int i=0; i<hostList.size(); i++) {
				Map<String, String> hostMap = hostList.get(i);
				hostMap.put("CLUSTER_ID", clusterId);
				hostMap.put("CLUSTER_TYPE", clusterType);
				hostMap.put("BATCH_NAME", batchName);
				if (BlankUtil.isBlank(hostMap.get("DEPLOY_FILE_TYPE"))) {
					hostMap.put("DEPLOY_FILE_TYPE", "");
				}
				if (BlankUtil.isBlank(hostMap.get("CONFIG_PATH"))) {
					hostMap.put("CONFIG_PATH", "");
				}
			}
			
			//批量添加配置信息
			coreService.insertBatchObject("startConfig.addBatchStartConfig", hostList, dbKey);
			logger.debug("保存用户配置信息成功， 本次保存记录数:" + (hostList == null ? 0 : hostList.size()));
			rstMap.put("RST_CODE", "1");
			rstMap.put("RST_MSG", "配置数据保存成功！");
		} catch (Exception e) {
			logger.error("保存用户配置信息失败， 失败原因:", e);
			rstMap.put("RST_CODE", "0");
			rstMap.put("RST_MSG", "配置数据保存失败， 失败原因: " + e.getMessage());
		}
		return rstMap;
	}
	
	
	/**
	 * 保存用户初始化数据
	 * 
	 * @param params
	 * @param dbKey
	 * @return Map
	 */
	@Override
	public List<HashMap<String, Object>> queryOperator(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询用户配置信息, 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		List<HashMap<String, Object>> hostList = new ArrayList<HashMap<String,Object>>();
		try {
			//获取集群编码类型
			String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
			//批次名称
			String batchName = StringTool.object2String(params.get("BATCH_NAME"));
			
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("CLUSTER_ID", clusterId);
			queryParams.put("BATCH_NAME", batchName);
			hostList = coreService.queryForList2New("startConfig.queryStartConfig", queryParams, dbKey);
			logger.debug("查询用户配置信息成功， 返回用户信息数据: " + (hostList == null ? 0 : hostList.size()));
		} catch (Exception e) {
			logger.error("保存用户配置信息失败， 失败原因:", e);
		}
		return hostList;
	}


}
