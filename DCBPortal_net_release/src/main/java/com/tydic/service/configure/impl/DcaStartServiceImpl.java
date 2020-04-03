package com.tydic.service.configure.impl;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.configure.DcaStartService;
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import com.tydic.util.log.LoggerUtils;
import com.tydic.util.zk.ZkClientUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [DcaStartServiceImpl]     
  * @Description:  [DCA组件启停实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:08:54]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:08:54]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class DcaStartServiceImpl implements DcaStartService {
	/**
	 * DCA启停日志
	 */
	private static Logger logger = Logger.getLogger(DcaStartServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	* 组件实例对象，用来获取日志文件信息
	*/
	@Autowired
	private InstConfigService instConfigService;
	
	/**
	 * 检查进程是否拉起来次数
	 */
	private static final int CHECK_TIMES = 5;

	/**
	 * 获取redis配置信息
	 */
	private static ConcurrentHashMap zkRedisMap = new ConcurrentHashMap();

	/**
	 * 根据集群编码获取redis节点信息
	 * @param clusterCode
	 */
	private synchronized String getRedisPasswd(String clusterCode, String hostIp, String hostPort) {
		logger.info("获取Redis配置信息，业务参数：" + clusterCode + ", 主机IP:" + hostIp + ", 主机端口: " + hostPort);

		String hostPasswd = getHostPasswd(clusterCode, hostIp, hostPort);
		if (StringUtils.isNotBlank(hostPasswd)) {
			logger.info("获取redis配置信息，返回信息: " + hostPasswd);
			return hostPasswd;
		}
		List<Map<String, Object>> redisList = new ArrayList<Map<String, Object>>();
		CuratorFramework client = ZkClientUtil.createZkClient(clusterCode);
		client.start();
		try {
			byte [] redisBytes = client.getData().forPath("/"+clusterCode+"/RedisCfg");
			String msg = new String(redisBytes);
			logger.debug("获取Redis配置信息，结果：" + msg);
			if (StringUtils.isNotBlank(msg)) {
				JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(msg);
				Iterator<Map.Entry<String, Object>> entries = jsonObject.entrySet().iterator();
				while (entries.hasNext()) {
					Map.Entry<String, Object> entry = entries.next();
					String key = entry.getKey();
					if ("Action".equals(key)) {
						continue;
					}
					List<Map<String,Object>> ObjList = (List<Map<String,Object>>)entry.getValue();
					if (CollectionUtils.isNotEmpty(ObjList)) {
						for(Map<String,Object> map : ObjList){
							String ip = ObjectUtils.toString(map.get("ip"));
							String port = ObjectUtils.toString(map.get("port"));
							String passwd = DesTool.dec(ObjectUtils.toString(map.get("password")));
							Map<String, Object> redisMap = new HashMap<String, Object>(){{
								put("IP", ip);
								put("PORT", port);
								put("TYPE", "master");
								put("PASSWD", passwd);
							}};
							redisList.add(redisMap);
							List<Map<String,Object>> slaveList = (List<Map<String,Object>>)map.get("slaves");
							if (CollectionUtils.isNotEmpty(slaveList)) {
								for(Map<String,Object> slaveMap : slaveList){
									String slaveIp = ObjectUtils.toString(slaveMap.get("ip"));
									String slavePort = ObjectUtils.toString(slaveMap.get("port"));
									String slavePasswd = DesTool.dec(ObjectUtils.toString(map.get("password")));
									Map<String, Object> sRedisMap = new HashMap<String, Object>(){{
										put("IP", slaveIp);
										put("PORT", slavePort);
										put("TYPE", "slaves");
										put("PASSWD", slavePasswd);
									}};
									redisList.add(sRedisMap);
								}
							}
						}
					}
				}
				zkRedisMap.put(clusterCode, redisList);
				return this.getHostPasswd(clusterCode, hostIp, hostPort);
			}
		} catch (Exception e) {
			logger.error("获取Redis配置信息异常，异常信息：", e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		logger.info("获取Redis配置信息， 返回结果: " + hostPasswd);
		return hostPasswd;
	}

	/**
	 * 获取配置文件密码信息
	 * @param hostIp
	 * @param hostPort
	 * @return
	 */
	private String getHostPasswd(String clusterCode, String hostIp, String hostPort) {
		List<Map<String, Object>> redisList = (List<Map<String,Object>>) zkRedisMap.get(clusterCode);
		String hostPasswd = "";
		if (CollectionUtils.isNotEmpty(redisList)) {
			for (int i=0; i<redisList.size(); i++) {
				String currIp = StringTool.object2String(redisList.get(i).get("IP"));
				String currPort = StringTool.object2String(redisList.get(i).get("PORT"));
				if (StringUtils.equals(currIp, hostIp) && StringUtils.equals(currPort, hostPort)) {
					hostPasswd = StringTool.object2String(redisList.get(i).get("PASSWD"));
					break;
				}
			}
		}
		logger.info("集群编码:" + clusterCode + ", 主机IP: " + hostIp + ", 主机端口: " + hostPort + ",主机Redis配置文件密码: " + hostPasswd);
		return hostPasswd;
	}


	/**
	 * 启动Dca
	 * 
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startDca(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动DCA,参数 ---> " + param + ", dbKey ---> " + dbKey);
		
		//主机处理结果保存对象
		List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
		//返回对象
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
			final String clusterId = queryMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = queryMap.get("CLUSTER_TYPE");
			//集群编码
			final String clusterCode = queryMap.get("CLUSTER_CODE");
			//单例获取执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				final List<Map<String,String>> resultList = new ArrayList<>();
				for(int i=0;i<param.size();i++){
					//处理一批次后休眠
					if (i != 0 && (i % Constant.THREAD_HANDLE_NUM == 0)) {
						Thread.sleep(Constant.THREAD_HANDLE_TIMES);
					}
					
					final Map<String ,String> hostMap =(Map<String, String>) param.get(i);
					hostMap.put("CLUSTER_CODE", clusterCode);
					//程序启停版本
					final String version = StringTool.object2String(hostMap.get("VERSION"));
					
					//启停组件小类型
					final String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));
					
					//获取启停命令
					// chmod a+x auto.sh;./auto.sh -s ${clusterType} -2 ${dcas} -3 ${配置文件} -4 ${version} -5 ${extParams}
					final String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));
					
					//获取启停主机信息
					Map<String, Object> queryHostMap = new HashMap<String, Object>();
					queryHostMap.put("HOST_ID", hostMap.get("HOST_ID"));
					final Map<String, Object> tempMap = coreService.queryForObject2New("host.queryHostList", queryHostMap, dbKey);
					
					//异步调用执行远程命令
					pool.execute(new Runnable() {
						public void run() {
							//最终SSH2执行的命令
							String command = "";
							//获取组件启停小类型
							String deployType_DIR = null ;
							//返回Map对象
							Map<String,String> resultMap = new HashMap<>();
							
							String filePath = StringTool.object2String(hostMap.get("FILE"));
							//如果目录分割中包含//则将该字符替换成单/
							if (filePath.indexOf("//") != -1) {
								filePath = filePath.replaceAll("//", "/");
								hostMap.put("FILE", filePath);
							}
							
							resultMap.put("CONFIG_PATH", filePath);
							String deployFile = "";
							if (Constant.DCAM.equals(deployType) || Constant.DCAS.equals(deployType)) {
								//获取PM2名称
								String pm2Name = getCustPm2Name(hostMap, dbKey);
								if (!BlankUtil.isBlank(pm2Name)) {
									pm2Name = pm2Name.trim();
								}
								resultMap.put("PM2_NAME", pm2Name);
								
								//获取前台选中的实例，把组件小类别过滤 dcas/192.168.161.26_01, 实例为192.168.161.26_01
								if (filePath.indexOf("/") != -1) {
									filePath = filePath.split("/")[1];
								}
								deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, pm2Name, StringTool.object2String(System.nanoTime()));
							} else if (Constant.REDIS.equals(deployType)) {
								//Redis前台传递的参数为配置文件名称,例如 : redis/192.168.161.26_01/redis.conf
								filePath = filePath.replace(deployType + "/", "");
								deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
							} else if (Constant.SENTINEL.equals(deployType)) {
								//sentinel前台传递的参数为配置文件名称,例如 : sentinel/192.168.161.26_01/sentinel.conf
								filePath = filePath.replace(deployType + "/", "");
								deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
							}else if (Constant.DAEMON.equals(deployType)) {
								if (BlankUtil.isBlank(filePath)) {
									filePath = "config.js";
								} else {
									if (filePath.indexOf("/") != -1) {
										filePath = filePath.substring(filePath.indexOf("/") + 1);
									}
								}
								deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
							} else {
								deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
							}
							logger.debug("DCA 启停脚本命令 ---> " + deployFile);
							
							switch (deployType) {
								case "redis":
									deployType_DIR = "redis/";
									break;
								case "sentinel":
									deployType_DIR = "sentinel/";
									break;
								case "dcam":
									deployType_DIR = "DCAM/";
									break;
								case "dcas":
									deployType_DIR = "DCAS/";
									break;
								case "daemon":
									deployType_DIR = "daemon/";
									break;
								case "redisIncRefresh":
								case "redisWholeRefresh":
								case "redisWholeCheck":
								case "redisIncCheck":
								case "redisRevise":
									deployType_DIR = "etc/";
									break;
							}
							
							//DCAS或者DCAM需要创建日志目录
							if (Constant.DCAM.equals(deployType) || Constant.DCAS.equals(deployType)) {
								//获取日志存放目录
								String custBaseDir = getCustBaseDir(hostMap, dbKey);
								//项目部署目录
								String programPath = appRootPath + Constant.Tools + Constant.ENV  + FileTool.exactPath(version) + Constant.DCA_DIR + deployType.toUpperCase();
								if (!BlankUtil.isBlank(custBaseDir)) {
									custBaseDir = FileTool.exactPath(custBaseDir) + "monitor";
									command = "cd " + programPath + "; mkdir -p " + custBaseDir + ";";
								}
							} else if (Constant.REDIS.equals(deployType)) {
								//修改部署主机配置文件
								//updateRedisHostFile(hostMap, tempMap, dbKey);
							} 
							command += "cd " + appRootPath + Constant.Tools + ";" + deployFile;
						  	ShellUtils cmdUtil = new ShellUtils(String.valueOf(tempMap.get("HOST_IP")),
						  			String.valueOf(tempMap.get("SSH_USER")), DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD"))));
							 
						  	 String result =  cmdUtil.execMsg(command);
						  	 logger.debug("执行启动DCA命令，命令: " + command + ", 执行结果: " + result);
						  	
							 resultMap.put("HOST_ID",String.valueOf(tempMap.get("HOST_ID")));
							 resultMap.put("HOST_IP",String.valueOf(tempMap.get("HOST_IP")));
							 
							 /*if(result.toLowerCase().indexOf(Constant.FLAG_ERROR) >=0 
									 || (result.toLowerCase().indexOf(ResponseObj.ERROR) >=0 && result.toLowerCase().indexOf("│ errored │") == -1)
									 || (result.toLowerCase().indexOf(ResponseObj.FAILED) >=0 && result.toLowerCase().indexOf("failed:") == -1)){*/
							 if(result.indexOf(Constant.SUCCESS) < 0 
									 || (result.toLowerCase().indexOf(ResponseObj.ERROR) >=0 && result.toLowerCase().indexOf("│ errored │") == -1)
									 || (result.toLowerCase().indexOf(ResponseObj.FAILED) >=0 && result.toLowerCase().indexOf("failed:") == -1)){	 
								logger.error("启动Redis失败， 请检查脚本...");
							 	resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
							 }else{
								 //该参数是用来添加到启动记录表
								 resultMap.put("DEPLOY_FILE_TYPE", deployType);
								 
								 //启动实例配置文件真实路径
								 String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + hostMap.get("FILE");
								 resultMap.put("FILE_PATH", startFilePath);
								 resultMap.put("DEPLOY_TYPE", clusterType);
								 resultMap.put("VERSION", version);
								 resultMap.put("CLUSTER_ID", clusterId);
								 resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
								 resultMap.put("STATUS", Constant.STATE_ACTIVE);
								 
								 
								 if (Constant.DCAM.equals(deployType) || Constant.DCAS.equals(deployType)) {
									 String instPath = hostMap.get("FILE");
									 if (instPath.indexOf("/") != -1) {
										 resultMap.put("INST_PATH", instPath.split("/")[1]);
									 } else {
										 resultMap.put("INST_PATH", instPath);
									 }
									 resultMap.put("SOFT_LINK_PATH", appRootPath +Constant.Tools+Constant.ENV+ FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR+Constant.CONFIG + resultMap.get("INST_PATH"));
								 } else if (Constant.DAEMON.equals(deployType)) {
									 resultMap.put("FILE_PATH", appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + Constant.DAEMON);
									 resultMap.put("INST_PATH", filePath);
									 resultMap.put("SOFT_LINK_PATH", appRootPath +Constant.Tools+Constant.ENV+ FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
								 } else if (Constant.REDIS_INC_REFRESH.equals(deployType)){
									 resultMap.put("INST_PATH", filePath);
									 resultMap.put("SOFT_LINK_PATH", appRootPath +Constant.Tools+Constant.ENV+ FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
								 } else if (Constant.REDIS_WHOLE_REFRESH.equals(deployType) 
										 || Constant.REDIS_WHOLE_CHECK.equals(deployType)
										 || Constant.REDIS_REVISE.equals(deployType)
										 || Constant.REDIS_INC_CHECK.equals(deployType)) {
									 resultMap.put("INST_PATH", filePath);
									 resultMap.put("SOFT_LINK_PATH", appRootPath +Constant.Tools+Constant.ENV+ FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
									 resultMap.put("STATUS", Constant.STATE_NOT_ACTIVE);
									 resultMap.put("IS_MONITOR", BusinessConstant.PARAMS_BUS_0);
								 }else if(Constant.SENTINEL.equals(deployType)){
									 String instPath = hostMap.get("FILE");
									 instPath =  instPath.replace(deployType + "/", "");
									 resultMap.put("INST_PATH", instPath);
									 resultMap.put("SOFT_LINK_PATH",appRootPath +Constant.Tools+Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + resultMap.get("INST_PATH"));

								 }else {
									 //本地网，如果是陕西省份， 则redis 不需要由 daemon程序拉起， 自己直接启动
									 String latnId = SystemProperty.getContextProperty("latnId");
									 if("sx".equals(latnId)){
										 String instPath = hostMap.get("FILE");
										 instPath =  instPath.replace(deployType + "/", "");
										 resultMap.put("INST_PATH", instPath);
										 resultMap.put("SOFT_LINK_PATH",appRootPath +Constant.Tools+Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + resultMap.get("INST_PATH"));
										 resultMap.put("PORT", hostMap.get("FILE_PORT"));
									 }else{
										 String instPath = hostMap.get("FILE");
										 instPath =  instPath.replace(deployType + "/", "");
										 resultMap.put("INST_PATH", instPath);
										 resultMap.put("SOFT_LINK_PATH",appRootPath +Constant.Tools+Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + resultMap.get("INST_PATH"));

										 resultMap.put("STATUS", Constant.STATE_NOT_ACTIVE);
										 //Redis端口号
										 resultMap.put("PORT", hostMap.get("FILE_PORT"));
										 //Redis进程号
										 resultMap.put("DAEMON_STATE", Constant.STATE_ACTIVE);


										 //Redis添加到数据库,等待DCA守护进程拉起Redis进程
										 //存在匹配记录，更新
										 List<HashMap<String, String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", resultMap, dbKey);
										 if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){
											 coreService.updateObject("instConfig.updateDcfDeployInstConfigByRedis", resultMap, dbKey);
										 }else{//不存在匹配记录，添加
											 coreService.insertObject("instConfig.addDcfDeployInstConfigByRedis", resultMap, dbKey);
										 }

										 //休眠查询Redis启动状态
										 int scanCount = 0;
										 boolean isStartOk = false;
										 while(scanCount <= CHECK_TIMES) {
											 List<HashMap<String, String>> portList = coreService.queryForList("instConfig.queryConfigInfoByConditions", resultMap, dbKey);
											 if (!BlankUtil.isBlank(portList) && Constant.STATE_ACTIVE.equals(portList.get(0).get("STATUS"))) {
												 result += "\nRedis Start Success ...";
												 isStartOk = true;
												 break;
											 }
											 scanCount++;
											 try {
												 Thread.sleep(3000);
											 } catch (InterruptedException e) {
												 logger.error("查询Redis启动状态休眠失败, 失败原因:", e);
											 }
										 }
										 if (!isStartOk) {
											 //将damon_state状态修改为0
											 resultMap.put("DAEMON_STATE", Constant.STATE_NOT_ACTIVE);
											 coreService.updateObject("instConfig.updateDcfDeployInstConfigByRedis", resultMap, dbKey);
											 logger.debug("启动redis失败，修改daemon_state=0");

											 resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
											 result += "\nRedis Start Failed ...";
										 }
									 }

								 }
							 }
							 //将脚本输出信息中的空行和错误标志给替换
							 result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
							 resultMap.put(Constant.RST_STR, result);
							 resultList.add(resultMap);
						}
					});
				}
				
				//轮询等待所有线程执行完成
				while(resultList.size() < param.size()){
					 Thread.sleep(100);
				}
				
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
						fileMap.put("STATUS", resultMap.get("STATUS"));
						fileMap.put("PM2_NAME", resultMap.get("PM2_NAME"));
						fileMap.put("IS_MONITOR", resultMap.get("IS_MONITOR"));
						fileMap.put("CLUSTER_ID", clusterId);
						fileMap.put("PORT", resultMap.get("PORT"));
						//存在匹配记录，更新
						List<HashMap<String, String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey); 
						if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){
							coreService.updateObject("instConfig.updateConfigInfoByConditions", fileMap, dbKey);
							logger.debug("启动实例修改成功, 参数: " + fileMap.toString());
						} else {//不存在匹配记录，添加
							if (Constant.DCAM.equals(resultMap.get("DEPLOY_FILE_TYPE")) 
									|| Constant.DCAS.equals(resultMap.get("DEPLOY_FILE_TYPE"))) {
								coreService.insertObject("instConfig.addDcfDeployInstConfigByDcasAndDcam", fileMap, dbKey);
							} else {
								coreService.insertObject("instConfig.addDcfDeployInstConfig", fileMap, dbKey);
							}
							logger.debug("启动实例添加成功, 参数: " + fileMap.toString());
						}
						  
					} else {
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
				logger.error("启动DCA失败, 失败原因: ", e);
			}
		}
		return rstMap;
 	}

	/**
	 * 获取PM2启停名称
	 * @param hostMap
	 * @param dbKey
	 * @return
	 */
	private String getCustPm2Name(Map<String, String> hostMap, String dbKey) {
		logger.debug("获取启停DCAS&DCAM Pm2名称, 参数: " + hostMap + ", dbKey: " + dbKey);
		//获取部署类型
		String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));
		//获取部署根目录
		String localRootPath = StringTool.object2String(hostMap.get("localRootPath"));
		
		//获取组件log.js文件，该文件中需要创建日志目录
		String configFile = hostMap.get("FILE");
		if (configFile.indexOf("//") != -1) {
			configFile = configFile.replaceAll("//", "/");
		}
		String instName = configFile.split("/")[1];
		
		String appPath = SessionUtil.getConfigValue("FTP_ROOT_PATH") + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR 
				+ Constant.DCA_DIR + FileTool.exactPath(hostMap.get("CLUSTER_CODE")) + FileTool.exactPath(deployType)  + FileTool.exactPath(instName) + "app.js";
		logger.debug("获取启动PM2名称配置文件路径: " + appPath);
		
		String configContent = getFileContent(localRootPath, appPath, "app.js", dbKey);
		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;
		String pm2Name = "";
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(configContent.getBytes()));
			bufferedReader = new BufferedReader(reader);
			String lineContent = "";
			while ((lineContent = bufferedReader.readLine()) != null) {
				if (Constant.DCAS.equals(deployType) && lineContent.trim().startsWith("id".trim())) {
					if (lineContent.trim().indexOf(":") != -1) {
						String appExtName = lineContent.trim().split(":")[1];
						pm2Name = appExtName.substring(0, appExtName.indexOf(",")).replaceAll("'", "");
						break;
					}
				} else if (Constant.DCAM.equals(deployType) && lineContent.trim().startsWith("appName".trim())) {
					if (lineContent.trim().indexOf(":") != -1) {
						String appExtName = lineContent.trim().split(":")[1];
						pm2Name = appExtName.substring(0, appExtName.indexOf(",")).replaceAll("'", "");
						break;
					}
				}
			}
		} catch (IOException e) {
			logger.error("获取DCA日志路径失败 ---> ", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				logger.error("获取配置文件内容，关闭流失败 --->", e);
			}
		}
		logger.debug("当前启动PM2名称: " + pm2Name);
		return pm2Name;
	}
	
	/**
	 * 获取用户日志目录
	 * @return
	 */
	private String getCustBaseDir(Map<String, String> hostMap, String dbKey) {
		logger.debug("获取启停DCAS&DCAM日志目录, 参数: " + hostMap + ", dbKey: " + dbKey);
		//获取部署类型
		String deployType = hostMap.get("DEPLOY_TYPE");
		//获取部署根目录
		String localRootPath = StringTool.object2String(hostMap.get("localRootPath"));
		
		//获取组件log.js文件，该文件中需要创建日志目录
		String configFile = hostMap.get("FILE");
		if (configFile.indexOf("//") != -1) {
			configFile = configFile.replaceAll("//", "/");
		}
		String instName = configFile.split("/")[1];
		
		String logPath = SessionUtil.getConfigValue("FTP_ROOT_PATH") + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR 
				+ Constant.DCA_DIR + FileTool.exactPath(hostMap.get("CLUSTER_CODE")) + FileTool.exactPath(deployType) + FileTool.exactPath(instName) + "log.js";
		logger.debug("获取启动日志配置文件路径: " + logPath);
		
		String configContent = getFileContent(localRootPath, logPath, "log.js", dbKey);
		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;
		String custBaseDir = "";
		try {
			reader = new InputStreamReader(new ByteArrayInputStream(configContent.getBytes()));
			bufferedReader = new BufferedReader(reader);
			String lineContent = "";
			while ((lineContent = bufferedReader.readLine()) != null) {
				if (lineContent.trim().startsWith("customBaseDir".trim())) {
					custBaseDir = lineContent.trim().replaceAll("customBaseDir".trim() + ":", "").replaceAll("'", "").replaceAll(",", "").trim();
					break;
				}
			}
		} catch (IOException e) {
			logger.error("获取DCA日志路径失败 ---> ", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				logger.error("获取配置文件内容，关闭流失败 --->", e);
			}
		}
		logger.debug("当前启动日志路径: " + custBaseDir);
		return custBaseDir;
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
		logger.debug("获取远程配置文件内容，参数 ---> " + filePath + ", dbKey ---> " + dbKey);
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
	 * 停止DCA
	 * 
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopDca(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止Dca， 参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			//final String clusterId = querySingleMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = querySingleMap.get("CLUSTER_TYPE");
			//获取启动组件执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				final List<Map<String,String>> resultList = new ArrayList<>();
				for(int i=0;i<param.size();i++){
					//处理一批次后休眠
					if (i != 0 && (i % Constant.THREAD_HANDLE_NUM == 0)) {
						Thread.sleep(Constant.THREAD_HANDLE_TIMES);
					}
					
					final Map<String ,String> hostMap =(Map<String, String>) param.get(i);
					
					//停止程序执行Shell命令
					String executeCmd = hostMap.get("autoFile");
					//停止程序小类型
					String deployFileType = hostMap.get("DEPLOY_FILE_TYPE");
					//配置文件
					String instName = hostMap.get("INST_PATH");
					//停止程序版本
					String version = StringTool.object2String(hostMap.get("VERSION"));
					//PM2名称
					final String pm2Name = StringTool.object2String(hostMap.get("PM2_NAME"));
					String newPm2Name = pm2Name;
					if (BlankUtil.isBlank(newPm2Name)) {
						newPm2Name = "default";
					}
					
					//Daemon不需要配置文件启动，所以给默认配置文件
					if (Constant.DAEMON.equals(deployFileType) && BlankUtil.isBlank(instName)) {
						instName = "default.conf";
					}
					
					//获取Redis、 sentinel启动端口，直接根据端口号查找进程
					if (Constant.REDIS.equals(deployFileType) || Constant.SENTINEL.equals(deployFileType)) {
						instName = StringTool.object2String(hostMap.get("PORT"));
					}
					
					//格式化后的执行命令
					final String autoFile = MessageFormat.format(executeCmd, Constant.DCA, deployFileType, instName, version, newPm2Name, StringTool.object2String(System.nanoTime()));
					
					//根据主机ID查询主机信息
					final Map tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
					
					//通过线程池异步调用停止脚本
					pool.execute(new Runnable() {
						public void run() {
							 String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
						  	 ShellUtils cmdUtil = new ShellUtils(String.valueOf(tempMap.get("HOST_IP")),
						  			String.valueOf(tempMap.get("SSH_USER")), DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD"))));
							 String result =  cmdUtil.execMsg(command);
							 logger.debug("执行停止DCA实例命令，命令: " + command + ", 执行结果: " + result);
							 
							 Map<String,String> resultMap = new HashMap<>();
							 resultMap.put("HOST_ID",String.valueOf(tempMap.get("HOST_ID")));
							 resultMap.put("HOST_IP",String.valueOf(tempMap.get("HOST_IP")));
							 
							 //用来修改记录状态
							 resultMap.put("INST_ID",String.valueOf(hostMap.get("INST_ID")));
							/* if((result.toLowerCase().indexOf(Constant.FLAG_ERROR) >=0
									 || result.toLowerCase().indexOf(ResponseObj.FAILED) >=0)
									 && (result.indexOf(pm2Name + " doesn't exist") == -1)){*/
							 if((result.indexOf(Constant.SUCCESS) < 0
									 || result.toLowerCase().indexOf(ResponseObj.FAILED) >=0)
									 && (result.indexOf(pm2Name + " doesn't exist") == -1)){
								 resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
							 } else {
								 resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
							 }
							//将脚本输出信息中的空行和错误标志给替换
							 result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
							 resultMap.put(Constant.RST_STR, result);
							 resultList.add(resultMap);
						}
					});
				}
				while(resultList.size() < param.size()){
					 Thread.sleep(100);
				}
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
						  updateParams.put("DAEMON_STATE", Constant.STATE_NOT_ACTIVE);
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
				logger.error("停止DCA失败， 失败原因: ", e);
			}
		}
		return rstMap;
	}

	/**
	 * 陕西版本Dca启动
	 *
	 * @param param
	 * @paramd bKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> startSxDca(List<Map<String, String>> param, final String dbKey) throws Exception {
		logger.debug("启动DCA,参数 ---> " + param + ", dbKey ---> " + dbKey);

		//主机处理结果保存对象
		List<Map<String, Object>> resultMsgList = new ArrayList<Map<String, Object>>();
		//返回对象
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
			final String clusterId = queryMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = queryMap.get("CLUSTER_TYPE");
			//集群编码
			final String clusterCode = queryMap.get("CLUSTER_CODE");
			//操作用户ID
			final String empeeId = StringTool.object2String(param.get(0).get("EMPEE_ID"));

			Map<String, Object> redisMap = new ConcurrentHashMap<>();
			//单例获取执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				zkRedisMap.remove(clusterCode);
				final List<Map<String,String>> resultList = new ArrayList<>();
				Long startTimes = System.currentTimeMillis();
				for(int i=0;i<param.size();i++){
					final Map<String ,String> hostMap =(Map<String, String>) param.get(i);

					//异步调用执行远程命令
					pool.execute(new Runnable() {
						public void run() {
							//返回Map对象
							Map<String, String> resultMap = new HashMap<>();
							Long startProTimes = System.currentTimeMillis();
							Logger threadLogger = null;
							Map<String, Object> tempMap = null;
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

								hostMap.put("CLUSTER_CODE", clusterCode);
								//程序启停版本
								String version = StringTool.object2String(hostMap.get("VERSION"));

								//启停组件小类型
								String deployType = StringTool.object2String(hostMap.get("DEPLOY_TYPE"));

								//获取启停命令
								// chmod a+x auto.sh;./auto.sh -s ${clusterType} -2 ${dcas} -3 ${配置文件} -4 ${version} -5 ${extParams}
								String autoFilePath = StringTool.object2String(hostMap.get("autoFile"));

								//主机ID
								String hostId = hostMap.get("HOST_ID");

								//最终SSH2执行的命令
								String command = "";
								//获取组件启停小类型
								String deployType_DIR = null;
								String filePath = StringTool.object2String(hostMap.get("FILE"));
								//如果目录分割中包含//则将该字符替换成单/
								if (filePath.indexOf("//") != -1) {
									filePath = filePath.replaceAll("//", "/");
									hostMap.put("FILE", filePath);
								}

								//日志文件输出目录
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("start DCA, logPath--->" + logPath);

								//日志文件输出名称
								String startAutoFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + hostMap.get("FILE");
								Map logNameMap = instConfigService.getLogName(clusterId, clusterType, hostId, version, deployType, startAutoFilePath);
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"),"");
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"),"");
								logger.info("start DCA, logShaName--->" + logShaName +", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								logger.info("start DCA, logFile--->" + logFinalPath);

								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "DcaStartServiceImpl-Start-" + Thread.currentThread().getName());
								threadLogger.info("start DCA, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("start DCA, clusterId: " + clusterId + ", hostId: " + hostId + ", version: " + version + ", deployType: " + deployType + ", startAutoFilePath: " + startAutoFilePath);
								threadLogger.info("start DCA, logName source --->" + logStrName);

								//获取启停主机信息
								Map<String, Object> queryHostMap = new HashMap<String, Object>();
								queryHostMap.put("HOST_ID", hostId);
								threadLogger.debug("query host, params: " + queryHostMap);
								tempMap = coreService.queryForObject2New("host.queryHostList", queryHostMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);

								resultMap.put("CONFIG_PATH", filePath);
								String deployFile = "";
								String redisPasswd = "";
								if (Constant.DCAM.equals(deployType)
										|| Constant.DCAS.equals(deployType)
										|| Constant.DCA_MONITOR.equalsIgnoreCase(deployType)
										|| Constant.DCA_SWITCH.equalsIgnoreCase(deployType)) {
									//获取PM2名称
									String pm2Name = null;//getCustPm2Name(hostMap, dbKey);
									if (!BlankUtil.isBlank(pm2Name)) {
										pm2Name = pm2Name.trim();
									} else {
										pm2Name = "DCA";
									}
									resultMap.put("PM2_NAME", pm2Name);

									//获取前台选中的实例，把组件小类别过滤 dcas/192.168.161.26_01, 实例为192.168.161.26_01
									if (filePath.indexOf("/") != -1) {
										filePath = filePath.split("/")[1];
									}
									deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, pm2Name, StringTool.object2String(System.nanoTime()));
								} else if (Constant.REDIS.equals(deployType)) {
									//Redis前台传递的参数为配置文件名称,例如 : redis/192.168.161.26_01/redis.conf
									String filePort = hostMap.get("FILE_PORT");
									String hostIp = StringTool.object2String(tempMap.get("HOST_IP"));
									filePath = filePath.replace(deployType + "/", "");

									threadLogger.info("start DCA, getRedisPasswd, params--> clusterCode: " + clusterCode + ", hostIp: " + hostIp + ", filePort:" + filePort);
									redisPasswd = getRedisPasswd(clusterCode, hostIp, filePort);
									threadLogger.info("start DCA, getRedisPasswd, result---> redisPasswd: " + redisPasswd);

									if (StringUtils.isBlank(StringTool.object2String(redisMap.get("INIT")))) {
										//获取zk集群信息
										Properties properties = ZkClientUtil.initZkConfig(clusterCode);
										String zkClusterStr = StringTool.object2String(properties.get("dca.zk.connectString"));
										String hostNetCard = Objects.toString(tempMap.get("HOST_NET_CARD"), "");
										String initClusterCode = clusterCode + hostNetCard;
										String redisDeployFile = MessageFormat.format(autoFilePath, Constant.DCA, "initRedis", initClusterCode, version, zkClusterStr, StringTool.object2String(System.nanoTime()));
										redisMap.put("INIT", "1");
										redisMap.put("CLUSTER_CODE", clusterCode);
										redisMap.put("EXEC_CMD", redisDeployFile);
										redisMap.put("APP_PATH", appRootPath);
										threadLogger.info("init Redis, clusterCode: " + clusterCode + ", execCmd: " + redisDeployFile + ", rootPath: " + appRootPath);
									}

									//IPV4启动Redis
									if (StringTool.isIPV4Legal(hostIp)) {
										deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, hostIp + "_" + filePort + "#" + redisPasswd, StringTool.object2String(System.nanoTime()));
									} else {
										String hostNetCard = Objects.toString(tempMap.get("HOST_NET_CARD"), "");
										String finalHostIp = hostIp + hostNetCard;
										deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, finalHostIp + "_" + filePort + "#" + redisPasswd, StringTool.object2String(System.nanoTime()));
									}
								} else if (Constant.SENTINEL.equals(deployType)) {
									//sentinel前台传递的参数为配置文件名称,例如 : sentinel/192.168.161.26_01/sentinel.conf
									String filePort = hostMap.get("FILE_PORT");

									//sentinel启停需要传入主机网卡信息
									String hostIp = StringTool.object2String(tempMap.get("HOST_IP"));
									String hostNetCard = Objects.toString(tempMap.get("HOST_NET_CARD"), "");
									String finalHostIp = hostIp + hostNetCard;
									filePort = finalHostIp + "_" + filePort;


									filePath = filePath.replace(deployType + "/", "");
									deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, filePort, StringTool.object2String(System.nanoTime()));
								} else if (Constant.DAEMON.equals(deployType) || Constant.MONITOR_SERVICE.equals(deployType)) {
									if (BlankUtil.isBlank(filePath)) {
										filePath = "config.js";
									} else {
										if (filePath.indexOf("/") != -1) {
											filePath = filePath.substring(filePath.indexOf("/") + 1);
										}
									}
									deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
								} else {
									deployFile = MessageFormat.format(autoFilePath, Constant.DCA, deployType, filePath, version, "default", StringTool.object2String(System.nanoTime()));
								}
								logger.debug("DCA 启停脚本命令 ---> " + deployFile);
								threadLogger.info("start DCA, execCmd: " + deployFile);

								switch (deployType) {
									case "redis":
										deployType_DIR = "redis/";
										break;
									case "sentinel":
										deployType_DIR = "sentinel/";
										break;
									case "dcam":
										deployType_DIR = "DCAM/";
										break;
									case "dcas":
										deployType_DIR = "DCAS/";
										break;
									case "daemon":
										deployType_DIR = "daemon/";
										break;
									case "monitorDCA":
										deployType_DIR = "monitorDCA/";
										break;
									case "switchDCA":
										deployType_DIR = "switchDCA/";
										break;
									case "redisIncRefresh":
									case "redisWholeRefresh":
									case "redisWholeCheck":
									case "redisIncCheck":
									case "redisRevise":
										deployType_DIR = "etc/";
										break;
								}

								//DCAS或者DCAM需要创建日志目录
								if (Constant.DCAM.equals(deployType) || Constant.DCAS.equals(deployType)) {
									//获取日志存放目录
									String custBaseDir = null;//getCustBaseDir(hostMap, dbKey);
									//项目部署目录
									String programPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType.toUpperCase();
									if (!BlankUtil.isBlank(custBaseDir)) {
										custBaseDir = FileTool.exactPath(custBaseDir) + "monitor";
										command = "cd " + programPath + "; mkdir -p " + custBaseDir + ";";
									}
								} else if (Constant.REDIS.equals(deployType)) {
									//修改部署主机配置文件
									//updateRedisHostFile(hostMap, tempMap, dbKey);
								}
								command += "cd " + appRootPath + Constant.Tools + ";" + deployFile;

								String exeHostIp = String.valueOf(tempMap.get("HOST_IP"));
								String exeSshUser = String.valueOf(tempMap.get("SSH_USER"));
								String exeSshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
								ShellUtils cmdUtil = new ShellUtils(exeHostIp, exeSshUser, exeSshPasswd);

								threadLogger.info("start DCA, deployType: " + deployType + ", hostIp: " + exeHostIp + ", sshUser: " + exeSshUser + ", sshPasswd: " + exeSshPasswd);
								threadLogger.info("start DCA, final exeCmd: <label style='color:green;'>" + command + "</label>");

								logger.debug("执行启动" + deployType + "命令，命令: " + command);
								String result = "";
								if (Constant.REDIS.equals(deployType) && StringUtils.isBlank(redisPasswd)) {
									threadLogger.error("<label style='color:red;'>start redis failed, redis password is empty, please check redis configuration!!<label>");
									result = "\n";
									result += "redis password is empty, please check redis configuration!";
									result += " Failed.";
									result += "\n";
								} else {
									try {
										result = cmdUtil.execMsg(command);
									} catch (Exception e) {
										threadLogger.error("start DCA failed, Cause by: ", e);

										logger.error("执行启动命令失败，请检查主机配置和执行命令， 失败原因: ", e);
										result += "\n";
										result += " Failed.";
										result += e.getMessage();
									}
								}
								logger.debug("执行启动" + deployType + "命令，执行结果: " + result);
								threadLogger.info("start DCA, exeCmd result: " + result);

								resultMap.put("HOST_ID", String.valueOf(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP", String.valueOf(tempMap.get("HOST_IP")));

								if (result.indexOf(Constant.OPERATOR_FAILED) > 0) {
									logger.error("启动" + deployType + "失败， 请检查脚本...");
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("start DCA " + deployType + " failed, cause by: " + result);
								} else {
									threadLogger.info("start DCA " + deployFile + " success!");
									//设置Redis初始化执行主机
									if (redisMap.isEmpty() || StringUtils.isBlank(StringTool.object2String(redisMap.get("HOST_IP")))) {
										redisMap.put("HOST_IP", tempMap.get("HOST_IP"));
										redisMap.put("SSH_USER", tempMap.get("SSH_USER"));
										redisMap.put("SSH_PASSWD", tempMap.get("SSH_PASSWD"));
									}

									//该参数是用来添加到启动记录表
									resultMap.put("DEPLOY_FILE_TYPE", deployType);

									//启动实例配置文件真实路径
									String startFilePath = appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + hostMap.get("FILE");
									resultMap.put("FILE_PATH", startFilePath);
									resultMap.put("DEPLOY_TYPE", clusterType);
									resultMap.put("VERSION", version);
									resultMap.put("CLUSTER_ID", clusterId);
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
									resultMap.put("STATUS", Constant.STATE_ACTIVE);

									if (Constant.DCAM.equals(deployType) || Constant.DCAS.equals(deployType)) {
										String instPath = hostMap.get("FILE");
										if (instPath.indexOf("/") != -1) {
											resultMap.put("INST_PATH", instPath.split("/")[1]);
										} else {
											resultMap.put("INST_PATH", instPath);
										}
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + Constant.CONFIG + resultMap.get("INST_PATH"));
									} else if (Constant.DAEMON.equals(deployType)) {
										resultMap.put("FILE_PATH", appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + Constant.DAEMON);
										resultMap.put("INST_PATH", filePath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
									} else if (Constant.DCA_SWITCH.equals(deployType) || Constant.DCA_MONITOR.equals(deployType)) {
										resultMap.put("FILE_PATH", appRootPath + Constant.Tools + Constant.CONF + Constant.DCA_DIR + deployType_DIR + filePath);
										resultMap.put("INST_PATH", filePath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
									} else if (Constant.REDIS_INC_REFRESH.equals(deployType)) {
										resultMap.put("INST_PATH", filePath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
									} else if (Constant.REDIS_WHOLE_REFRESH.equals(deployType)
											|| Constant.REDIS_WHOLE_CHECK.equals(deployType)
											|| Constant.REDIS_REVISE.equals(deployType)
											|| Constant.REDIS_INC_CHECK.equals(deployType)) {
										resultMap.put("INST_PATH", filePath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + filePath);
										resultMap.put("STATUS", Constant.STATE_NOT_ACTIVE);
										resultMap.put("IS_MONITOR", BusinessConstant.PARAMS_BUS_0);
									} else if (Constant.SENTINEL.equals(deployType)) {
										String instPath = hostMap.get("FILE");
										instPath = instPath.replace(deployType + "/", "");
										resultMap.put("PORT", hostMap.get("FILE_PORT"));
										resultMap.put("INST_PATH", instPath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + resultMap.get("INST_PATH"));
									} else {
										String instPath = hostMap.get("FILE");
										instPath = instPath.replace(deployType + "/", "");
										resultMap.put("INST_PATH", instPath);
										resultMap.put("SOFT_LINK_PATH", appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + deployType_DIR + resultMap.get("INST_PATH"));
										resultMap.put("PORT", hostMap.get("FILE_PORT"));
									}
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.debug("start DCA, initRedis result --->" + redisMap.toString());
								threadLogger.info("start DCA, result--->" + resultMap.toString());
								Long endProTimes = System.currentTimeMillis();
								Long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.info("start DCA final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("DCA启动失败， 失败原因: ", e);
								resultMap.put("HOST_IP", (tempMap == null ? "" : StringTool.object2String(tempMap.get("HOST_IP"))));
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "dca startup fail, cause: " + e.getMessage());
								resultList.add(resultMap);

								if (threadLogger != null) {
									threadLogger.error("start DCA failed, cause by:", e);
									threadLogger.info("ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("start DCA final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}

				//轮询等待所有线程执行完成
				while(resultList.size() < param.size()){
					logger.debug("本次总启动DCA进程数:" + param.size() + ", 已经启动完成DCA进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("DCA进程启动完成，本次启动DCA进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

				//初始化Redis
				boolean execResult = false;
				if (StringUtils.equals(StringTool.object2String(redisMap.get("INIT")), "1")) {
					execResult = initRedis(redisMap);
				}

				logger.info("所有启动命令结束，转化结果开始...");
				for(int i = 0 ; i < resultList.size();i++){
					Map<String, Object> singleMap = new HashMap<String, Object>();
					Map<String,String> resultMap = resultList.get(i);
					if(resultMap.get(Constant.RST_CODE) != null && Constant.RST_CODE_SUCCESS.equalsIgnoreCase(resultMap.get(Constant.RST_CODE))){
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
						fileMap.put("STATUS", resultMap.get("STATUS"));
						fileMap.put("PM2_NAME", resultMap.get("PM2_NAME"));
						fileMap.put("IS_MONITOR", resultMap.get("IS_MONITOR"));
						fileMap.put("CLUSTER_ID", clusterId);
						fileMap.put("PORT", resultMap.get("PORT"));
						//存在匹配记录，更新
						List<HashMap<String, String>> instList = coreService.queryForList("instConfig.queryConfigInfoByConditions", fileMap, dbKey);
						if(!BlankUtil.isBlank(instList) && !instList.isEmpty()){
							coreService.updateObject("instConfig.updateConfigInfoByConditions", fileMap, dbKey);
							logger.debug("启动实例修改成功, 参数: " + fileMap.toString());
						} else {//不存在匹配记录，添加
							if (Constant.DCAM.equals(resultMap.get("DEPLOY_FILE_TYPE"))
									|| Constant.DCAS.equals(resultMap.get("DEPLOY_FILE_TYPE"))) {
								coreService.insertObject("instConfig.addDcfDeployInstConfigByDcasAndDcam", fileMap, dbKey);
							} else {
								coreService.insertObject("instConfig.addDcfDeployInstConfig", fileMap, dbKey);
							}
							logger.debug("启动实例添加成功, 参数: " + fileMap.toString());
						}

					} else {
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

				if (StringUtils.equals(StringTool.object2String(redisMap.get("INIT")), "1")) {
					if (execResult) {
						rstBuffer.append("在主机:").append(redisMap.get("HOST_IP")).append("执行Redis初始化").append(Constant.LINE_FLAG);
						rstBuffer.append("输出日志:").append(redisMap.get("EXEC_MSG")).append(Constant.LINE_FLAG);
						rstBuffer.append("初始化结论:").append("成功! Success.\n");
					} else {
						if (StringUtils.isBlank(StringTool.object2String(redisMap.get("HOST_IP")))) {
							rstBuffer.append("执行Redis初始化失败，请在前台手动触发初始化! Failed.\n");
						} else {
							rstBuffer.append("在主机:").append(redisMap.get("HOST_IP")).append("执行Redis初始化").append(Constant.LINE_FLAG);
							rstBuffer.append("输出日志:").append(redisMap.get("EXEC_MSG")).append(Constant.LINE_FLAG);
							rstBuffer.append("初始化结论:").append("失败! Failed.\n");
						}
					}
				}

				rstMap.put(Constant.RST_STR, rstBuffer.toString());
				rstMap.put(Constant.RST_CODE, rstCode);
			} catch(Exception e){
				logger.error("启动DCA失败, 失败原因: ", e);
				throw e;
			}
		}
		return rstMap;
	}

	/**
	 * 初始化Redis信息
	 * @param redisMap
	 */
	private boolean initRedis(Map<String, Object> redisMap) {
		boolean execResult = false;
		logger.info("redis初始化，业务参数:" + redisMap.toString());
		if (redisMap != null && !redisMap.isEmpty() && StringUtils.isNotBlank(StringTool.object2String(redisMap.get("HOST_IP")))) {
			String deployFile = StringTool.object2String(redisMap.get("EXEC_CMD"));
			String appRootPath = StringTool.object2String(redisMap.get("APP_PATH"));
			String hostIp = StringTool.object2String(redisMap.get("HOST_IP"));
			String sshUser = StringTool.object2String(redisMap.get("SSH_USER"));
			String sshPasswd = DesTool.dec(StringTool.object2String(redisMap.get("SSH_PASSWD")));
			String cmd = "cd " + appRootPath + Constant.Tools + ";" + deployFile;

			ShellUtils shellUtils = new ShellUtils(hostIp, sshUser, sshPasswd);
			String retStr = shellUtils.execMsg(cmd);
			logger.debug("执行Redis初始化命令，命令: " + cmd + ", 执行结果: " + retStr);
			if (StringUtils.isNotBlank(retStr)) {
				retStr = retStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
			}
			redisMap.put("EXEC_MSG", retStr);
			if(retStr.indexOf(Constant.OPERATOR_FAILED) > 0 || retStr.toUpperCase().indexOf("UPDATE REDISCFG TO ZOOKEEPER SUCCESS") < 0){
				logger.error("执行Redis初始化失败， 请检查配置，并且手动执行初始化!");
			} else {
				execResult = true;
				logger.info("Redis初始化执行成功!");
			}
		}
		return execResult;
	}

	/**
	 * 陕西停止DCA
	 *
	 * @param param
	 * @param dbKey
	 * @return List<String>
	 */
	@Override
	public Map<String, Object> stopSxDca(List<Map<String, String>> param, String dbKey) throws Exception {
		logger.debug("停止Dca， 参数 ---> " + param + ", dbKey ---> " + dbKey);
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
			final String clusterCode = StringTool.object2String(clusterMap.get("CLUSTER_CODE"));
			//集群ID
			//final String clusterId = querySingleMap.get("CLUSTER_ID");
			//集群类型
			final String clusterType = querySingleMap.get("CLUSTER_TYPE");
			//用户ID
			final String empeeId = querySingleMap.get("EMPEE_ID");
			//获取启动组件执行线程
			ExecutorService pool = SingletonThreadPool.getExecutorService();
			try {
				zkRedisMap.remove(clusterCode);
				final List<Map<String,String>> resultList = new ArrayList<>();
				Long startTimes = System.currentTimeMillis();
				for(int i=0;i<param.size();i++){
					final Map<String ,String> hostMap = (Map<String, String>) param.get(i);
					//通过线程池异步调用停止脚本
					pool.execute(new Runnable() {
						public void run() {
							Map<String, String> resultMap = new HashMap<>();
							Long startProTimes = System.currentTimeMillis();
							Logger threadLogger = null;
							String hostIp = "";
							try {
								Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
								//停止程序执行Shell命令
								String executeCmd = hostMap.get("autoFile");
								//停止程序小类型
								String deployFileType = hostMap.get("DEPLOY_FILE_TYPE");
								//配置文件
								String instName = hostMap.get("INST_PATH");
								//停止程序版本
								String version = StringTool.object2String(hostMap.get("VERSION"));
								//程序实例ID
								String instId = ObjectUtils.toString(hostMap.get("INST_ID"));
								//日志文件路径
								String logPath = instConfigService.getLogPath(clusterType);
								logger.info("stop dca logPath--->" + logPath);

								Map<String, Object> logNameMap = instConfigService.getLogName(instId, dbKey);
								//加密后的日志文件名称
								String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"));
								//日志文件名称构成字串
								String logStrName = ObjectUtils.toString(logNameMap.get("LOG_NAME_STR"));
								logger.info("logShaName--->" + logShaName + ", logStrName--->" + logStrName);

								String logFinalPath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
								threadLogger = LoggerUtils.getThreadLogger(logPath, logShaName, "DcaStartServiceImpl-Stop-" + Thread.currentThread().getName());
								threadLogger.info("stop DCA, logName: " + logFinalPath + ", startTime:" + DateUtil.getCurrent(DateUtil.allPattern) + ", operation userID:" + empeeId);
								threadLogger.info("stop DCA, logName source: " + logStrName);

								//根据主机ID查询主机信息
								threadLogger.debug("query host, params: " + hostMap);
								Map tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
								threadLogger.debug("query host, result: " + tempMap);

								//PM2名称
								String pm2Name = StringTool.object2String(hostMap.get("PM2_NAME"));
								String newPm2Name = pm2Name;
								if (BlankUtil.isBlank(newPm2Name)) {
									newPm2Name = "default";
								}
								threadLogger.debug("stop DCA, pm2Name: " + newPm2Name);

								//Daemon不需要配置文件启动，所以给默认配置文件
								if (Constant.DAEMON.equals(deployFileType) && BlankUtil.isBlank(instName)) {
									instName = "default.conf";
								}

								//获取Redis、 sentinel启动端口，直接根据端口号查找进程
								hostIp = ObjectUtils.toString(tempMap.get("HOST_IP"), "");
								String hostId = ObjectUtils.toString(tempMap.get("HOST_ID"), "");
								if (Constant.SENTINEL.equals(deployFileType)) {
									String netCard = ObjectUtils.toString(tempMap.get("HOST_NET_CARD"), "");
									instName = StringTool.object2String(hostMap.get("PORT"));
									instName = (hostIp + netCard) + "_" + instName;
									threadLogger.debug("stop DCA sentinel, instName: " + instName);
								}

								if (Constant.REDIS.equals(deployFileType)) {
									instName = StringTool.object2String(hostMap.get("PORT"));
									threadLogger.debug("stop DCA redis, getRedisPasswd, params---> clusterCode: " + clusterCode + ", hostIp: " + hostIp + ", instName: " + instName);
									String passWd = getRedisPasswd(clusterCode, hostIp, instName);
									threadLogger.info("stop DCA redis. getRedisPasswd, result---> redisPasswd: " + passWd);
									if (StringUtils.isBlank(passWd)) {
										threadLogger.error("stop DCA redis, redisPasswd is empty!!");
										tempMap.put("FAIL_FLAG", "1");
									}
									//IPV4主机
									if (StringTool.isIPV4Legal(hostIp)) {
										instName = hostIp + "_" + instName + "#" + passWd;
									} else {
										String netCard = Objects.toString(tempMap.get("HOST_NET_CARD"));
										instName = (hostIp + netCard) + "_" + instName + "#" + passWd;
									}
									threadLogger.info("stop DCA redis, instName: " + instName);
									logger.info("停止Redis实例: " + instName);
								}

								//格式化后的执行命令
								final String autoFile = MessageFormat.format(executeCmd, Constant.DCA, deployFileType, instName, version, newPm2Name, StringTool.object2String(System.nanoTime()));

								String result = "";
								if (StringUtils.equals(StringTool.object2String(tempMap.get("FAIL_FLAG")), "1")) {
									threadLogger.error("stop DCA redis failed, redis password is empty, please check redis configuration!!");
									result = "\n";
									result += "redis password is empty, please check redis configuration!";
									result += " Failed.";
									result += "\n";
								} else {
									String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;

									String exeHostIp = String.valueOf(tempMap.get("HOST_IP"));
									String exeSshUser = String.valueOf(tempMap.get("SSH_USER"));
									String exeSshPasswd = DesTool.dec(String.valueOf(tempMap.get("SSH_PASSWD")));
									ShellUtils cmdUtil = new ShellUtils(exeHostIp, exeSshUser, exeSshPasswd);
									try {
										logger.debug("执行停止DCA实例命令，命令: " + command);
										threadLogger.info("stop DCA, sshIp: " + exeHostIp + ", sshUser: " + exeSshUser + ", sshPasswd: " + exeSshPasswd);
										threadLogger.info("stop DCA, exeCmd: <label style='color:green;'>" + command + "</label>");
										result = cmdUtil.execMsg(command);
										logger.debug("执行停止DCA实例命令，结果: " + result);
										threadLogger.info("stop DCA " + deployFileType + ", result: " + result);
									} catch (Exception e) {
										logger.error("DCA进程停止失败，失败原因: ", e);
										threadLogger.error("stop DCA " + deployFileType +" failed, Cause by: ", e);
										result += "\n";
										result += " Failed.";
										result += "\n";
										result += e.getMessage();
									}
								}
								if (result.indexOf(Constant.OPERATOR_FAILED) > 0) {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
									threadLogger.error("stop DCA " + deployFileType + " failed, cause by: " + result);
								} else {
									resultMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
									threadLogger.info("stop DCA " + deployFileType + " success!!");
								}
								//将脚本输出信息中的空行和错误标志给替换
								result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
								resultMap.put("HOST_ID", hostId);
								resultMap.put("HOST_IP", hostIp);
								resultMap.put("INST_ID", instId);
								resultMap.put(Constant.RST_STR, result);
								resultList.add(resultMap);

								threadLogger.info("stop DCA, ret result: " + resultMap.toString());
								long endProTimes = System.currentTimeMillis();
								long totalProTimes = (endProTimes - startProTimes)/1000;
								threadLogger.debug("stop DCA final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
							} catch (Exception e) {
								logger.error("DCA程序停止失败， 失败原因: ", e);
								resultMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
								resultMap.put(Constant.RST_STR, "dca failed to stop, cause: " + e.getMessage());
								resultMap.put("HOST_IP", hostIp);
								resultList.add(resultMap);
								if (threadLogger != null) {
									threadLogger.error("stop DCA failed, cause by:", e);
									threadLogger.info("stop DCA, ret result: " + resultMap.toString());
									long endProTimes = System.currentTimeMillis();
									long totalProTimes = (endProTimes - startProTimes)/1000;
									threadLogger.debug("stop DCA final, endTime: " + (DateUtil.getCurrent(DateUtil.allPattern)) + ", totalTime: <label style='color:red'>" + totalProTimes + "</label>S");
								}
							}
						}
					});
				}

				while(resultList.size() < param.size()){
					logger.debug("本次总停止DCA进程数:" + param.size() + ", 已经停止完成DCA进程数:" + resultList.size());
					SingletonThreadPool.getExecutorService();
					Thread.sleep(2000);
				}
				Long endTimes = System.currentTimeMillis();
				Long totalTimes = (endTimes - startTimes)/1000;
				logger.info("DCA进程停止完成，本次停止DCA进程数: " + param.size() + ", 总耗时: [ " + totalTimes + " ]S");

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
						updateParams.put("DAEMON_STATE", Constant.STATE_NOT_ACTIVE);
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
				logger.error("停止DCA失败， 失败原因: ", e);
			}
		}
		return rstMap;
	}
}