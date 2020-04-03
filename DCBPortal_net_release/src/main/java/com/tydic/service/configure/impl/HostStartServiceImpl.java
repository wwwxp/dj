package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.common.BusException;
import com.tydic.service.configure.HostStartService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [HostStartServiceImpl]     
  * @Description:  [主机管理操作类， 包含实例状态检查]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-14 上午8:55:00]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-14 上午8:55:00]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class HostStartServiceImpl implements HostStartService {
	
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(HostStartServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;
	
	/**
	 * M2DB刷数据
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> updateM2dbRefreshTables(Map<String,Object> param, String dbKey) throws Exception{
		log.debug("M2DB刷数据， 业务参数: " + param.toString() + ", dbKey: " + dbKey);
		
		//获取当前集群部署根目录
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", param.get("CLUSTER_ID"));
		queryClusterMap.put("CLUSTER_TYPE", param.get("CLUSTER_TYPE"));
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("集群信息查询失败, 请检查！");
		}
		//组件部署根目录
		final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		log.debug("当前集群部署路径: " + appRootPath);
		
		//M2DB实例
		final String instanceName = StringTool.object2String(param.get("INSTANCE_NAME"));
		log.debug("当前集群M2DB对应的实例: " + instanceName);
				
		//返回结果信息
		List<String> resultMsgList = new ArrayList<>();
		
		List<Map<String, String>> hostList = (List)param.get("hostList");
		String cmdParam = StringTool.object2String(param.get("cmdParam"));
		//前台中将+转义成  %2b,现场偶尔没有将 %2b转化为+，手动转化
		if (StringUtils.isNotBlank(cmdParam) && cmdParam.toUpperCase().indexOf("%2B") >0) {
			cmdParam = cmdParam.replace("%2b", "+").replace("%2B", "+");
		}

		if (BlankUtil.isBlank(cmdParam)) {
			cmdParam = "all";
		}
		final String cmd = cmdParam;
		
		String radioValue = StringTool.object2String(param.get("radio"));
		if ("2".equals(radioValue)) {    //操作表
			radioValue = "cmd_m2db";
		} else {                         //表名
			radioValue = "refresh";
		}
		final String action = radioValue;

		boolean isRestartProcess = Boolean.valueOf(StringTool.object2String(param.get("isRestartProcess")));
		//是否重启acWholeRefresh进程标识
		final String restartFlag = isRestartProcess?"1":"0";


		if(!BlankUtil.isBlank(hostList)){
			ExecutorService pool=Executors.newCachedThreadPool();
			try{
				final List<Map<String,String>> resultList = new ArrayList<>();
				//获取启停命令
				//chmod a+x auto.sh;mkdir -p exec_temp/{5};cp auto.sh exec_temp/{5}/auto.sh;
				//cd exec_temp/{6};./auto.sh {0} {1} -2 {2} -3 {3} -4 {4} -5 {5};rm -rf ../{6};
				
				final String autoFilePath = StringTool.object2String(param.get("autoFile"));
				for(int i = 0 ; i < hostList.size() ; i ++){
					Map<String ,String> hostMap = hostList.get(i);
					//查询当前主机运行版本
					hostMap.put("CLUSTER_ID", StringTool.object2String(param.get("CLUSTER_ID")));
					hostMap.put("INSTANCE_NAME", instanceName);
					final List<HashMap<String, String>> instanceVersionList = coreService.queryForList("instConfig.queryRunVersionByHostId", hostMap, dbKey);
					//查询当前主机信息
				  	final Map<String, String> tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
				   	
				  	pool.execute(new Runnable() {
				   		public void run() {
							Map<String,String> resultMap = new HashMap<>();
							if(BlankUtil.isBlank(instanceVersionList)){
								 resultMap.put(ResponseObj.ERROR,"没有获取到对应的版本号");
							} else {
								HashMap<String, String> instMap = instanceVersionList.get(0);
								String VERSION = StringTool.object2String(instMap.get("VERSION"));
								
								String autoFile = MessageFormat.format(autoFilePath, "-t", Constant.M2DB, "\""+action + "|"+ instanceName+"\"","\""+cmd+"\"", VERSION,restartFlag, StringTool.object2String((System.nanoTime())));
								String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								log.debug("M2DB刷新表， 执行命令: " + command);
								 
								ShellUtils cmdUtil = new ShellUtils(StringTool.object2String(tempMap.get("HOST_IP")),
							  		StringTool.object2String(tempMap.get("SSH_USER")), DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD"))));
								String result =  cmdUtil.execMsg(command);
								log.debug("M2DB刷新表执行命令返回结果: " + result);
								 
								resultMap.put("HOST_ID",StringTool.object2String(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP",StringTool.object2String(tempMap.get("HOST_IP")));
								if(result.toLowerCase().indexOf(ResponseObj.SUCCESS) >=0 ){
									resultMap.put(ResponseObj.SUCCESS ,result);
								}else{
									resultMap.put(ResponseObj.ERROR, result);
								}
							}
							 resultList.add(resultMap);
							// 此处疑问,为何hostMap添加信息,list也会随即添加相同信息
						}
					});
				  	
				}
				 while(resultList.size() < hostList.size()){
					 Thread.sleep(100);
				 }
				 for(int i = 0 ; i < resultList.size();i++){
					  Map<String,String> resultMap = resultList.get(i);
					  if(resultMap.containsKey(ResponseObj.SUCCESS)){
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】 刷新成功\n" + resultMap.get(ResponseObj.SUCCESS));
					  }else{
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】 刷新失败\n" + resultMap.get(ResponseObj.ERROR));
					  }
				 }
			}catch(Exception e){
				log.debug("M2DB刷新表数据失败， 异常信息: ", e);
				resultMsgList.add("M2DB刷新表数据失败， 失败信息：" + e.getMessage());
			} finally {
				pool.shutdown();
			}	
		}
		return resultMsgList;
	}
	
	/**
	 * M2DB刷新数据(刷内存数据)
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<String> updateM2dbRefreshMem(Map<String,Object> param, String dbKey) throws Exception{
		log.debug("M2DB刷数据， 业务参数: " + param + ", dbKey: " + dbKey);
		
		//获取当前集群部署根目录
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", param.get("CLUSTER_ID"));
		queryClusterMap.put("CLUSTER_TYPE", param.get("CLUSTER_TYPE"));
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("集群信息查询失败, 请检查！");
		}
		//组件部署根目录
		final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		log.debug("当前集群部署路径: " + appRootPath);
		//M2DB实例
		final String instanceName = StringTool.object2String(param.get("INSTANCE_NAME"));
		log.debug("当前集群M2DB对应的实例: " + instanceName);
		
		//刷新数据主机列表
		List<Map<String,String>> hostList = (List)param.get("hostList");
		
		//返回对象
		List<String> resultMsgList = new ArrayList<>();
		
		final String  bolt = StringTool.object2String(param.get("bolt"));
		  
		if(!BlankUtil.isBlank(hostList)){
			ExecutorService pool=Executors.newCachedThreadPool();
			try{
				final List<Map<String,String>> resultList = new ArrayList<>();
				final String autoFilePath = StringTool.object2String(param.get("autoFile"));
				for(int i = 0 ; i < hostList.size() ; i ++){
					Map<String ,String> hostMap = hostList.get(i);
					hostMap.put("CLUSTER_ID", StringTool.object2String(param.get("CLUSTER_ID")));
					hostMap.put("INSTANCE_NAME", instanceName);
					//查询当前主机运行版本
					final  List<HashMap<String, String>> instVersionList = coreService.queryForList("instConfig.queryRunVersionByHostId", hostMap, dbKey);
					//查询当前主机信息
				  	final Map<String, String> tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
				  
				  	pool.execute(new Runnable() {
				  		public void run() {
							Map<String,String> resultMap = new HashMap<>();
							if(BlankUtil.isBlank(instVersionList)){
								 resultMap.put(ResponseObj.ERROR,"没有获取到对应的版本号");
							} else {
								Map<String, String> instMap = instVersionList.get(0);
								String VERSION = StringTool.object2String(instMap.get("VERSION"));
								String autoFile = MessageFormat.format(autoFilePath,"-r", Constant.M2DB, instanceName, bolt, VERSION, StringTool.object2String((System.nanoTime())));
								String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
								log.debug("M2DB刷数据，执行命令: " + command);
								 
								ShellUtils cmdUtil = new ShellUtils(StringTool.object2String(tempMap.get("HOST_IP")),
										StringTool.object2String(tempMap.get("SSH_USER")), DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD"))));
								String result =  cmdUtil.execMsg(command);
								log.debug("M2DB刷数据， 执行命令返回结果: " + result);
								 
								resultMap.put("HOST_ID",StringTool.object2String(tempMap.get("HOST_ID")));
								resultMap.put("HOST_IP",StringTool.object2String(tempMap.get("HOST_IP")));
								if(result.toLowerCase().indexOf(ResponseObj.SUCCESS) >=0 ){
									resultMap.put(ResponseObj.SUCCESS, result);
								} else {
									resultMap.put(ResponseObj.ERROR,result);
								}
							}
							 resultList.add(resultMap);
							// 此处疑问,为何hostMap添加信息,list也会随即添加相同信息
						}
					});
				  	
				}
				 while(resultList.size() < hostList.size()){
					 Thread.sleep(100);
				 }
				 for(int i = 0 ; i < resultList.size();i++){
					  Map<String,String> resultMap = resultList.get(i);
					  if(resultMap.containsKey(ResponseObj.SUCCESS)){
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】 刷新成功\n"+resultMap.get(ResponseObj.SUCCESS));
					  }else{
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】 刷新失败\n"+resultMap.get(ResponseObj.ERROR));
					  }
				 }
			}catch(Exception e){
				log.error("M2DB刷数据失败， 失败信息: ", e);
				resultMsgList.add("M2DB刷数据失败， 失败原因：" + e.getMessage());
			} finally {
				pool.shutdown();
			}	
		}
		return resultMsgList;
    	
	}
	
	/**
	 * M2DB导入数据到表
	 * 
	 * @param param 业务参数
	 * @param dbKey 数据Key
	 * @return List 返回对象
	 */
	@Override
	public List<String> updateM2dbInputTable(Map<String,Object> param, String dbKey) throws Exception{
		log.debug("M2DB导入数据， 业务参数: " + param + ", dbKey: " + dbKey);
		
		//获取当前集群部署根目录
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", param.get("CLUSTER_ID"));
		queryClusterMap.put("CLUSTER_TYPE", param.get("CLUSTER_TYPE"));
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("集群信息查询失败, 请检查！");
		}
		//组件部署根目录
		final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		log.debug("当前集群部署路径: " + appRootPath);
		
		//M2DB实例
		final String instanceName = StringTool.object2String(param.get("INSTANCE_NAME"));
		log.debug("当前集群M2DB对应的实例: " + instanceName);
				
		//临时本地目录
		final String localFilePath = StringTool.object2String(param.get("webRootPathFile"));//根目录
		
		//导入主机列表
		List<Map<String,String>> hostList = (List)param.get("hostList");
		
		//返回对象
		List<String> resultMsgList = new ArrayList<>();
		
		//导入文件
		final String  fileName = StringTool.object2String(param.get("fileName"));
		  
		if(!BlankUtil.isBlank(hostList)){
			ExecutorService pool=Executors.newCachedThreadPool();
			try{
				final List<Map<String,String>> resultList = new ArrayList<>();
				final String autoFilePath = StringTool.object2String(param.get("autoFile"));
				for(int i = 0 ; i < hostList.size() ; i ++){
					Map<String ,String> hostMap = hostList.get(i);
					hostMap.put("CLUSTER_ID", StringTool.object2String(param.get("CLUSTER_ID")));
					hostMap.put("INSTANCE_NAME", instanceName);
					//查询当前主机运行版本
					final List<HashMap<String, String>> instVersionList = coreService.queryForList("instConfig.queryRunVersionByHostId", hostMap, dbKey);
					//查询当前主机信息
				  	final Map<String, String> tempMap = coreService.queryForObject("host.queryHostList", hostMap, dbKey);
				  	pool.execute(new Runnable() {
				  		public void run() {
							Map<String,String> resultMap = new HashMap<>();
							boolean isRun = true;
							if(BlankUtil.isBlank(instVersionList)){
								 isRun = false;
								 resultMap.put(ResponseObj.ERROR,"没有获取到对应的版本号");
							} else {
								Map<String, String> instMap = instVersionList.get(0);
								String version = StringTool.object2String(instMap.get("VERSION"));
								Trans ftpClient = null;
								try{
									ftpClient = FTPUtils.getFtpInstance(StringTool.object2String(tempMap.get("HOST_IP")), StringTool.object2String(tempMap.get("SSH_USER")),
										           DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD"))), SessionUtil.getConfigValue("FTP_TYPE"));
								    ftpClient.login();
								      
								      String remotePath = FileTool.exactPath(appRootPath) + Constant.Tools + Constant.ENV
								    		  + FileTool.exactPath(version) + Constant.M2DB_DIV + Constant.BIN + "data/" + fileName;
								      log.debug("M2DB导入数据， 上传文件到远程目录: " + remotePath);
								      
								      ftpClient.put(localFilePath, remotePath);
								 }catch(Exception e){
									 log.error("M2DB导入数据, 上传文件失败， 失败信息: ", e);
									 isRun = false;
									 resultMap.put(ResponseObj.ERROR, e.getMessage());
								 }finally{
									 if(ftpClient !=null){
										 ftpClient.close();
									 }
								 }
								 
								 if(isRun){
									 //String command = "cd " + appRootPath +Constant.Tools + Constant.ENV +VERSION+"/"+ Constant.M2DB_DIV + Constant.BIN +
									//		          ";source ~/.bash_profile;./hbimport -m " +SessionUtil.getConfigValue("WEB_M2DB_NODE_CFG") +" -s all -f data/" + fileName;
									 String autoFile = MessageFormat.format(autoFilePath, "-i", Constant.M2DB , instanceName, fileName, 
												version, StringTool.object2String((System.nanoTime())));
									 String command = "cd " + appRootPath + Constant.Tools + ";" + autoFile;
									 log.debug("M2DB导入数据， 执行命令: " + command);
									 
									 ShellUtils cmdUtil = new ShellUtils(StringTool.object2String(tempMap.get("HOST_IP")),
								  			StringTool.object2String(tempMap.get("SSH_USER")), DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD"))));
									 String result =  cmdUtil.execMsg(command);
									 log.debug("M2DB导入数据， 执行命令结果: " + result);
									 
									 resultMap.put("HOST_ID",StringTool.object2String(tempMap.get("HOST_ID")));
									 resultMap.put("HOST_IP",StringTool.object2String(tempMap.get("HOST_IP")));
									 if(result.toLowerCase().indexOf(ResponseObj.SUCCESS) >=0 ){
										 resultMap.put(ResponseObj.SUCCESS, result);
									 }else{
										 result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
										 resultMap.put(ResponseObj.ERROR,result);
									 }
								 } 
							}
							 resultList.add(resultMap);
						}
					});
				}
				 while(resultList.size() < hostList.size()){
					 Thread.sleep(100);
				 }
				 for(int i = 0 ; i < resultList.size();i++){
					  Map<String,String> resultMap = resultList.get(i);
					  if(resultMap.containsKey(ResponseObj.SUCCESS)){
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】导入成功\n" + resultMap.get(ResponseObj.SUCCESS));
					  }else{
						  resultMsgList.add("【"+resultMap.get("HOST_IP")+"】导入失败\n" + resultMap.get(ResponseObj.ERROR));
					  }
				 }
			}catch(Exception e){
				log.error("M2DB导入数据失败， 失败原因: ", e);
				resultMsgList.add("M2DB导入数据失败， 失败原因: " + e.getMessage());
			} finally {
				pool.shutdown();
			}	
		}
		return resultMsgList;
	}

	/**
	 * 组件状态检查
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String,Object> checkProcessState(Map<String, Object> params) throws Exception {
		log.debug("检查组件进程状态， 参数: " + params.toString());
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		String clusterType = String.valueOf(params.get("CLUSTER_TYPE"));
		String deployFileType = String.valueOf(params.get("DEPLOY_FILE_TYPE"));
		//String softLinkPath = params.get("SOFT_LINK_PATH");
		
		//获取集群部署根目录
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, FrameConfigKey.DEFAULT_DATASOURCE);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("组件部署目录查询失败, 请检查！");
		}
		String clusterDeployPath = StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH"));
		
		//当前集群实例名称
		String m2dbInstanceName = null;
		if (Constant.M2DB.equals(clusterType)) {
			m2dbInstanceName = StringTool.object2String(params.get("INST_PATH"));
		}
		log.debug("组件实例状态检查， 集群部署根目录: " + clusterDeployPath + ", M2DB实例: " + m2dbInstanceName);
		
		//将集群部署根目录添加到Params参数
		params.put("appRootPath", clusterDeployPath);
		params.put("m2dbInstanceName", m2dbInstanceName);
		
		//1.查询主机信息，登陆主机
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("HOST_ID", params.get("HOST_ID"));
		HashMap<String, Object> hostInfo = coreService.queryForObject2New("host.queryHostList", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
		if(hostInfo == null || hostInfo.isEmpty()){
			throw new RuntimeException("未查询到有效的主机信息!");
		}
		
		String hostIp = StringTool.object2String(hostInfo.get("HOST_IP"));
		String sshUser = StringTool.object2String(hostInfo.get("SSH_USER"));
		String sshPwd = DesTool.dec(StringTool.object2String(hostInfo.get("SSH_PASSWD")));
		ShellUtils cmdUtil = new ShellUtils(hostIp, sshUser, sshPwd);
		
		//redis端口号获取
		String port="";
//		if(Constant.REDIS.equals(deployFileType)){
//			String portCommand="echo `cat " + softLinkPath + " |grep port|grep -v '#'`";
//			String portStr=cmdUtil.execMsg(portCommand).trim();
//			portStr=portStr.replaceAll("[\\s]+", ":");
//			String[] portArr=portStr.split(":");
//			if(portArr.length>1){
//				port=portArr[1];
//			}
//		}
		
		//2.拼接命令，执行
		String command = this.getShellCommand(params, port);
		String resultStr = cmdUtil.execMsg(command);
		log.debug("组件状态检查执行结果:" + resultStr);
		
		String[] resultArray =null;
		if(!resultStr.equals("")){
			resultArray = resultStr.split("\n");
		}
		String processInfo="";
		//3.返回端口和程序名
		if(Constant.FASTDFS.equals(clusterType)){
			if(resultArray!=null && resultArray.length>=2){
				//取第一次命令查询结果
				List<String> portList=new ArrayList<String>();
				for(String str:resultArray){
					processInfo=(str.trim().replaceAll("[\\s]+", ":").split(":"))[1];
					portList.add(processInfo);
				}
				
				//命令查询会出现两个进程，其中一个进程是用命令查询的时候出现的，端口号不定，再查一次，端口号对比
				String resultStr_2= cmdUtil.execMsg(command);
				String processInfo_2;
				String[] resultArray_2 =null;
				if(!resultStr_2.equals("")){
					resultArray_2 =resultStr_2.split("\n");
				}
				List<String> portList_2=new ArrayList<String>();
				if(resultArray_2!=null && resultArray_2.length>=1){
					for(String str_2:resultArray_2){
						processInfo_2=(str_2.trim().replaceAll("[\\s]+", ":").split(":"))[1];
						portList_2.add(processInfo_2);
					}
					
				}
				//端口号对比
				String real_port="";
				for(String first_port:portList){
					for(String second_port:portList_2){
						if(first_port.equals(second_port)){
							real_port+=first_port+", ";
							break;
						}
					}
					
				}
				
				//返回
				if (!BlankUtil.isBlank(real_port)) {
					resultMap.put("processState", 1);
					resultMap.put("processPort", real_port.substring(0, real_port.length() - 2));
				} else {
					resultMap.put("processState", 0);
					resultMap.put("processPort", real_port);
				}
			}
		} else if(Constant.ROCKETMQ.equals(clusterType)){
			if(resultArray!=null && resultArray.length>=1){
				String str=resultArray[1];
				processInfo=(str.trim().replaceAll("[\\s]+", ":").split(":"))[1];
				//返回
				resultMap.put("processState", 1);
				resultMap.put("processPort", processInfo);
			}
		} else if(Constant.M2DB.equals(clusterType)){
			if(resultStr !=null){
				 if(resultStr.toLowerCase().indexOf("success") >0){
					 resultMap.put("processState", 1);
					 resultMap.put("processPort", m2dbInstanceName);
				 } 
			}
		} else if(Constant.DCA.equals(clusterType)){
			if((resultArray!=null && resultArray.length>=1) && !Constant.REDIS.equals(deployFileType)){
				//取第一次命令查询结果
				List<String> portList=new ArrayList<String>();
				for(String str:resultArray){
					//processInfo=(str.trim().replaceAll("[\\s]+", ":").split(":"))[1];
					//portList.add(processInfo);
					portList.add(str);
				}
				
				//命令查询会出现两个进程，其中一个进程是用命令查询的时候出现的，端口号不定，再查一次，端口号对比
				String resultStr_2= cmdUtil.execMsg(command);
				//String processInfo_2;
				String[] resultArray_2 =null;
				if(!resultStr_2.equals("")){
					resultArray_2 =resultStr_2.split("\n");
				}
				List<String> portList_2=new ArrayList<String>();
				if(resultArray_2!=null && resultArray_2.length>=1){
					for(String str_2:resultArray_2){
						//processInfo_2=(str_2.trim().replaceAll("[\\s]+", ":").split(":"))[1];
						//portList_2.add(processInfo_2);
						portList_2.add(str_2);
					}
					
				}
				//端口号对比
				String real_port="";
				for(String first_port:portList){
					for(String second_port:portList_2){
						if(first_port.equals(second_port) && StringUtils.isNumeric(first_port.trim()) && !StringTool.object2String(first_port).equals("0")){
							real_port+=first_port + ", ";
							break;
						}
					}
					
				}
				
				//返回
				if (!BlankUtil.isBlank(real_port)) {
					resultMap.put("processState", 1);
					resultMap.put("processPort", real_port.substring(0, real_port.length() - 2));
				} else {
					resultMap.put("processState", 0);
					resultMap.put("processPort", real_port);
				}
			} else if((Constant.REDIS.equals(deployFileType)) && resultArray!=null  && resultArray.length>0){
				for(String str:resultArray){
					if (!BlankUtil.isBlank(str) && StringUtils.isNumeric(str.trim())) {
						processInfo = str;
					}
				}
				if (!BlankUtil.isBlank(processInfo) && StringUtils.isNumeric(processInfo.trim())) {
					resultMap.put("processState", 1);
					resultMap.put("processPort", processInfo);
				} else {
					resultMap.put("processState", 0);
					resultMap.put("processPort", processInfo);
				}
			}
		} else if (Constant.MONITOR_SERVICE.equals(clusterType) && Constant.MONITOR_SERVICE.equals(deployFileType)) {
			String message = "";
			if (!BlankUtil.isBlank(resultArray)) {

				String [] processList = Constant.MONITOR_PROCESS_ARRAY;
				String webLatn = SystemProperty.getContextProperty(Constant.WEB_LATN);
				if (StringUtils.equals(webLatn, Constant.WEB_LATN_SX)) {
					processList = Constant.MONITOR_PROCESS_ARRAY_SX;
				}

				int checkExistNum = 0;
				Map<String, String> componetMap = new HashMap<String, String>();
				for(int k=0;k<processList.length;k++){
					//获取监控进程名称
					String processFullName = processList[k];
					//获取进程名称
					String processName = "";
					if (processFullName.indexOf("/") != -1) {
						processName = processFullName.substring(processFullName.lastIndexOf("/") + 1);
					}
					boolean isExist=false;
					//查询到的结果集
					for(int i=0;i<resultArray.length;i++){
						String processInfos = resultArray[i].trim();
						if(!BlankUtil.isBlank(processInfos) && processInfos.contains(processFullName) && !componetMap.containsKey(processName)){
							processInfo = processInfos.trim().replaceAll("[\\s]+", ":");
							if (processInfo.indexOf(":") != -1) {
								String processNo = processInfo.split(":")[1];
								message += " " + processName +":" + processNo;
								isExist = true;
								componetMap.put(processName, processName);
								checkExistNum++;
							}
						}
					}
					if(!isExist){
						message+=" "+processName+":不存在  ";
					}
				}
				
				if(checkExistNum == processList.length){
					resultMap.put("processState", 1);
					resultMap.put("processPort", message);
				}else if(checkExistNum>0 && checkExistNum<processList.length){
					resultMap.put("processState", 0);
					resultMap.put("processPort", "但存在进程号【"+message+"】。");
					resultMap.put("killProcessFlag", 0); //0 还剩余进程  
				}else{
					resultMap.put("processState", 0);
					resultMap.put("processPort", "");
				}
			}
		} else if(Constant.DMDB.equals(clusterType) 
				|| Constant.ZOOKEEPER.equals(clusterType)
				|| Constant.JSTORM.equals(clusterType)
				|| Constant.DSF.equals(clusterType)){
			if(resultArray!=null && resultArray.length>=1){
				for(int h=0;h<resultArray.length;h++){

					String str=resultArray[h];
					processInfo +=(str.trim().replaceAll("[\\s]+", ":").split(":"))[1]+"  ";
				}

				//返回
				resultMap.put("processState", 1);
				resultMap.put("processPort", processInfo);
			}
		} else if(Constant.DCLOG.equals(clusterType)){
			String message="";
			if(!BlankUtil.isBlank(resultArray)){
				//组件是否存在
				Map<String, Boolean> componentArray = new HashMap<String, Boolean>();
				int checkExistNum=0;
				//循环监控的所有进程名
				for(int k=0;k<Constant.DCLOG_PROCESS_ARRAY.length;k++){
					String processName = Constant.DCLOG_PROCESS_ARRAY[k];
					boolean isExist=false;
					//查询到的结果集
					for(int i=0;i<resultArray.length;i++){
						String processInfos = resultArray[i].trim();
						//Status: pid=59178 /public/bp/DCBPortal_test/tools/env/0.0.9/dclog/bin/agent [[1;32mExist[0;39m]
						if(processInfos.contains(processName)){
							String processLine=((processInfos.replaceAll("[\\s]+", ":::")).split(":::"))[1];
							if(processLine.split("pid=").length>1){
								String processNum=(processLine.split("pid="))[1];
								message+=" " + processName + ":"+processNum+" ";
								isExist = true;
								checkExistNum++;
								break;
							}
						}
					}
					componentArray.put(processName, isExist);
					if(!isExist){
						message+=" " + processName + ":不存在  ";
					}
				}
				
				if(checkExistNum == Constant.DCLOG_PROCESS_ARRAY.length){
					resultMap.put("processState", 1);
					resultMap.put("processPort", message);
				}
//				else if(checkExistNum>0 && checkExistNum<Constant.DCLOG_PROCESS_ARRAY.length){
//					//agent和agent.bak至少一个存在，并且另外两个程序进程都存在则认为是运行状态
//					if (!componentArray.isEmpty() && checkExistNum == Constant.DCLOG_PROCESS_ARRAY.length - 1) {
//						if (componentArray.get(Constant.DCLOG_PROCESS_ARRAY[0]) 
//								&& componentArray.get(Constant.DCLOG_PROCESS_ARRAY[3])) {
//							resultMap.put("processState", 1);
//							resultMap.put("processPort", message);
//							return resultMap;
//						}
//					}
//					resultMap.put("processState", 0);
//					resultMap.put("processPort", "但存在进程号【"+message+"】。");
//				}
				else{
					resultMap.put("processState", 0);
					resultMap.put("processPort", "");
				}
			}
		}
		log.debug("组件状态检查，返回结果: " + resultMap.toString());
		return resultMap;
	}
	
	/**
	 * 更新进程状态
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateProcessState(Map<String, Object> params,String dbKey) throws Exception {
		log.debug("更新组件进程状态， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		
		//实例版本
//		String VERSION=StringTool.object2String(params.get("VERSION"));
		//实例状态
		String updateState=StringTool.object2String(params.get("STATUS"));
		//实例ID
		String instId = StringTool.object2String(params.get("INST_ID"));
		
//		String killProcessFlag=StringTool.object2String(params.get("killProcessFlag"));
		Map<String, Object> resultMap =new HashMap<String, Object>();
		try {
			
			//1.更新当前程序状态
			Map<String, String> updateParams = new HashMap<String, String>();
			updateParams.put("STATUS", updateState);
			updateParams.put("INST_ID", instId);
			coreService.updateObject("instConfig.updateDcfDeployInstConfig", updateParams, dbKey);
			
			//检查是否为Monitor，干掉存在的进程
//			if(CODE.equalsIgnoreCase(Constant.MONITOR_SERVICE) && updateState.equals("0") && killProcessFlag.equals("0")){
//				StringBuffer cmdBuf = new StringBuffer("source ~/.bash_profile;cd ");
//				cmdBuf.append(appRootPath).append(Constant.Tools).append(Constant.ENV).append(VERSION).append("/").append(Constant.MONITOR).append(Constant.BIN)
//				.append(";./").append(Constant.STOP_SH);
//				cmdBuf.append(";cd ").append(appRootPath).append(Constant.Tools).append(Constant.ENV).append(VERSION).append("/").append(Constant.MONITOR_COMPONENT)
//				.append(";./").append(Constant.STOP_SH);
//				String command=cmdBuf.toString();
//				
//				//1.查询主机信息，登陆主机
//				HashMap<String, String> hostInfo=coreService.queryForObject("host.queryHostList", params, FrameConfigKey.DEFAULT_DATASOURCE);
//				if(hostInfo == null || hostInfo.isEmpty()){
//					throw new RuntimeException("未查询到有效的主机信息!");
//				}
//				
//				ShellUtils cmdUtil = new ShellUtils(hostInfo.get("HOST_IP"),
//				hostInfo.get("SSH_USER"), DesTool.dec(hostInfo.get("SSH_PASSWD")));
//				String result = cmdUtil.execMsg(command);
//				if( (result.toLowerCase().indexOf(ResponseObj.ERROR) >=0 
//						 || result.toLowerCase().indexOf(ResponseObj.FAILED) >=0 )){
//					log.error("更新状态失败-->删除【"+hostInfo.get("HOST_IP")+"】主机进程失败，原因-->"+result);
//					resultMap.put(Constant.ERROR,"数据库更新失败，杀死【"+hostInfo.get("HOST_IP")+"】主机进程失败！");
//				}
//			}
		} catch (Exception e) {
			log.error("组件状态检查同步失败 ，失败原因: ", e);
			throw new Exception("组件状态检查同步更新失败，请检查!");
		}
		return resultMap;
	}

	/**
	 * 将实例参数赋值非响应结果
	 * @param instMap
	 * @param retMap
	 */
	public Map<String, Object> mapObjCopy(Map<String, Object> instMap, Map<String, Object> retMap) {
		if (instMap != null) {
			Iterator<String> keyIterator = instMap.keySet().iterator();
			if (retMap == null) {
				retMap = new HashMap<String, Object>();
			}
			while (keyIterator.hasNext()) {
				String key = keyIterator.next();
				Object keyValue = instMap.get(key);
				retMap.put(key, keyValue);
			}
		}
		return retMap;
	}

	/**
	 * 组件批量状态检查
	 * @param paramsList
	 * @param dbKey
	 * @return
	 * @throws BusException
	 */
	@Override
	public Map<String, Object> batchCheckStatus(List<Map<String, Object>> paramsList, String dbKey) throws BusException {
		log.debug("组件批量状态检查， 业务参数: " + paramsList.toString() + ", dbKey: " + dbKey);

		//程序检查返回对象
		Map<String, Object> rstMap = new HashMap<String, Object>();

		String retMessage = "";
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		if (CollectionUtils.isNotEmpty(paramsList)) {
			for (int i=0; i<paramsList.size(); i++) {
				Map<String, Object> instMap = paramsList.get(i);
				//当前实例状态
				String status = String.valueOf(instMap.get("STATUS"));
				try {
					Map<String, Object> retMap = this.checkProcessState(instMap);
					retMap = this.mapObjCopy(instMap, retMap);
					retList.add(retMap);

					//当数据库查询结果和实例状态真实查询结果不一致，修改数据库运行状态
					String processState = retMap.get("processState") == null ? "0" : String.valueOf(retMap.get("processState"));
					if (!StringUtils.equals(status, processState)) {
						Map<String, Object> updateMap = new HashMap<String, Object>();
						updateMap.put("INST_ID", instMap.get("INST_ID"));
						updateMap.put("STATUS", processState);
						this.updateProcessState(updateMap, dbKey);
					}
				} catch (Exception e) {
					Map<String, Object> retMap = new HashMap<String, Object>();
					retMap = this.mapObjCopy(instMap, retMap);
					retMap.put("processState", "99");
					retMap.put("errorStr", e.getMessage());
					retList.add(retMap);
				}
			}
		}

		int successCount = 0;
		int failCount = 0;
		int totalCount = retList.size();
		for (int i=0; i<totalCount; i++) {
			Map<String, Object> retMap = retList.get(i);
			//检查结果
			String processState = String.valueOf(retMap.get("processState"));
			//进程
			String processPort = retMap.get("processPort") == null ? "" : String.valueOf(retMap.get("processPort"));
			//异常信息
			String errorStr = String.valueOf(retMap.get("errorStr"));
			//组件名称
			String clusterType = String.valueOf(retMap.get("CLUSTER_TYPE"));
			//集群名称
			String clusterName = String.valueOf(retMap.get("CLUSTER_NAME"));
			//主机
			String hostIp = String.valueOf(retMap.get("HOST_IP"));
			String sshUser = String.valueOf(retMap.get("SSH_USER"));
			//部署类型
			String deployFileType = String.valueOf(retMap.get("DEPLOY_FILE_TYPE"));
			//实例名称
			String instPath = String.valueOf(retMap.get("INST_PATH"));

			retMessage += "<p>" + (i+1) + "、组件：<font style='color:green;font-weight:bold;'>" + clusterType + "</font>"
					+ "，集群名称：" + clusterName + "，主机IP: " + hostIp + "（" + sshUser + "）"
					+ "，部署类型：" + deployFileType + "， 实例名称: " + instPath;
			if (StringUtils.equals(processState, "99")) {
				retMessage += "，检查结果：<font style='color:red;font-weight:bold;'>失败</font>， 失败原因: <font style='color:red;'>" +  errorStr + "</font>";
				failCount++;
			} else {
				retMessage += "，检查结果：<font style='color:green;font-weight:bold;'>成功</font>";
				if (StringUtils.isNotBlank(processPort)) {
					retMessage += "，实例进程号：" + processPort;
				} else {
					retMessage += "，当前实例未运行！";
				}
				successCount++;
			}
			retMessage += "</p>";
		}
		String totalDesc = "本次共检查组件进程：<font style='font-weight:bold;'>"
				+ totalCount + "</font>个，其中成功：<font style='color:green;font-weight:bold;'>"
				+ successCount + "</font>个，失败：<font style='color:red;font-weight:bold;'>" + failCount + "</font>个，进程状态都已同步数据库，具体检查信息如下：";
		rstMap.put("TOTAL_DESC", totalDesc);
		rstMap.put("TOTAL_MSG", retMessage);

		log.debug("业务程序启停完成， 返回结果：" + rstMap.toString());
		return rstMap;
	}


	/**
	 * 获取组件进程状态查询命令
	 * @param params 业务参数
	 * @param port 端口
	 * @return String
	 */
	private String getShellCommand(Map<String,Object> params, String port){
		log.debug("获取组件状态检查命令， 参数: " + params.toString());
		//组件类型
		String clusterType = String.valueOf(params.get("CLUSTER_TYPE"));
		//组件部署类型
		String deployFileType = String.valueOf(params.get("DEPLOY_FILE_TYPE"));
		//组件实例名称
		String instPath = String.valueOf(params.get("INST_PATH"));
		//启停根目录
		String appRootPath = FileTool.exactPath(String.valueOf(params.get("appRootPath")));
		//启停版本
		String version = String.valueOf(params.get("VERSION"));
		//软连接完整路径
		String softLinkPath = String.valueOf(params.get("SOFT_LINK_PATH"));
		//Redis启动端口号
		String newPort = StringTool.object2String(params.get("PORT"));
		//Pm2名称
		String pm2Name = StringTool.object2String(params.get("PM2_NAME"));
		
		String processName = null ;
		switch(deployFileType){
			case "namesrv":processName="NamesrvStartup";break;
			case "broker":processName="BrokerStartup";break;
			case "tracker":processName="fdfs_trackerd";break;
			case "storage":processName="fdfs_storaged";break;
			case "dcam":processName="app.js";break;
			case "dcas":processName="index.js";break;
			case "redis":processName="redis-server";break;
			case "sentinel":processName="redis-server";break;
			case "redisIncRefresh":processName="redisIncRefresh";break;
			case "redisWholeRefresh":processName="redisWholeRefresh";break;
			case "redisWholeCheck":processName="redisWholeCheck";break;
			case "redisRevise":processName="redisRevise";break;
			case "elasticsearch":processName="elasticsearch";break;
			case "monitor":processName="monitor";break;
			case "monitorDCA":processName="monitorDCA";break;
			case "switchDCA":processName="switchDCA";break;
			case "zookeeper":processName="QuorumPeerMain";break;
			case "nimbus":processName="nimbus";break;
//			case "supervisor":processName="supervisor";break;
			case "supervisor":processName="supervisor.log";break;
			case "dclog":processName="dclog";break;
			case "nginx":processName=instPath;break;
			case "route":processName="route";break;
			case "dsf":processName="AgentMain";break;
			case "instance_pattern":processName="dmdb.conf";break;
			case "route_pattern":processName="mdb_tdal.conf";break;
			case "sync_pattern":processName="dmdb_sync";break;
			case "mgr_pattern":processName="dmdb_mgr";break;
			case "watcher_pattern":processName="dmdb_watcher";break;
			case "movesync_pattern":processName="AgentMain";break;
			case "other":processName="other";break;
		}
		
		String command="";
		if(Constant.FASTDFS.equals(clusterType)){
			command="ps ux|grep "+processName+"|grep " + softLinkPath;
		}else if(Constant.ROCKETMQ.equals(clusterType)){
			command="ps ux|grep "+processName+" |grep  " + softLinkPath + " |grep -v grep";
		}else if(Constant.DCA.equals(clusterType)){
			if(Constant.REDIS.equals(deployFileType)){
				command = "ps ux | grep "+processName+" | grep -v grep|grep :" + newPort + " |awk '{print $2}'";
			}

			//哨兵模式
			if (Constant.SENTINEL.equals(deployFileType)) {
				command = "ps ux | grep " + processName + " | grep -v grep | grep " + newPort + " |awk '{print $2}'";
			}

			String webLatn = SystemProperty.getContextProperty(Constant.WEB_LATN);
			if (StringUtils.equals(webLatn, Constant.WEB_LATN_SX)) {
				if (Constant.DCAM.equals(deployFileType)) {
					String dcamPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "DCAM/config/" + instPath + "/dcam.json";
					command = "ps ux | grep mserver | grep -v grep | grep " + dcamPath + " |awk '{print $2}'";
				} else if (Constant.DCAS.equals(deployFileType)) {
					String dcasPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "DCAS/config/" + instPath + "/dcas.json";
					command = "ps ux | grep sserver | grep -v grep | grep " + dcasPath + " |awk '{print $2}'";
				} else if (Constant.DCA_MONITOR.equals(deployFileType)) {
					String monitorDCAPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "monitorDCA/config/" + instPath + "/monitor.json";
					command = "ps ux | grep monitorDCA | grep -v grep | grep " + monitorDCAPath + " |awk '{print $2}'";
				} else if (Constant.DCA_SWITCH.equals(deployFileType)) {
					String switchDCAPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "switchDCA/config/" + instPath + "/switch.json";
					command = "ps ux | grep switchDCA | grep -v grep | grep " + switchDCAPath + " |awk '{print $2}'";
				}
			} else {
				if (Constant.DCAM.equals(deployFileType)) {
					String envPath = appRootPath + Constant.Tools;
					//PM2进程检查
					String autoFile = Constant.CHECK_AUTH_FILE_EXT;
					autoFile = MessageFormat.format(autoFile, Constant.DCA, deployFileType, instPath, version, pm2Name, StringTool.object2String(System.nanoTime()));
					command = "cd " + envPath + "; " + autoFile;
				} else if (Constant.DCAS.equals(deployFileType)) {
					String envPath = appRootPath + Constant.Tools;
					//PM2进程检查
					String autoFile = Constant.CHECK_AUTH_FILE_EXT;
					autoFile = MessageFormat.format(autoFile, Constant.DCA, deployFileType, instPath, version, pm2Name, StringTool.object2String(System.nanoTime()));
					command = "cd " + envPath + "; " + autoFile;
				}
			}
			if (Constant.DAEMON.equals(deployFileType)) {
				String daemonPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "daemon/daemonDCA.js";
				command = "ps ux | grep node | grep " + daemonPath + " |awk '{print $2}'";
			} else if (Constant.MONITOR_SERVICE.equals(deployFileType)) {
				String daemonPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "Monitor/app.js";
				command = "ps ux | grep node | grep " + daemonPath + " |awk '{print $2}'";
			}
			/**
			else if (Constant.DCA_MONITOR.equals(deployFileType)) {
				//String daemonPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "monitorDCA/app.js";
				//command = "ps ux | grep node | grep " + daemonPath + " |awk '{print $2}'";

				//SwitchDCA PM2进程检查
				String envPath = appRootPath + Constant.Tools;
				String autoFile = Constant.CHECK_AUTH_FILE_EXT;
				autoFile = MessageFormat.format(autoFile, Constant.DCA, deployFileType, instPath, version, "DCA", StringTool.object2String(System.nanoTime()));
				command = "cd " + envPath + "; " + autoFile;
			} else if (Constant.DCA_SWITCH.equals(deployFileType)) {
				//String daemonPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DCA_DIR + "switchDCA/app.js";
				//command = "ps ux | grep node | grep " + daemonPath + " |awk '{print $2}'";

				//SwitchDCA PM2进程检查
				String envPath = appRootPath + Constant.Tools;
				String autoFile = Constant.CHECK_AUTH_FILE_EXT;
				autoFile = MessageFormat.format(autoFile, Constant.DCA, deployFileType, instPath, version, "DCA", StringTool.object2String(System.nanoTime()));
				command = "cd " + envPath + "; " + autoFile;
			} **/
			else if (Constant.REDIS_INC_REFRESH.equals(deployFileType)
					|| Constant.REDIS_WHOLE_REFRESH.equals(deployFileType)
					|| Constant.REDIS_WHOLE_CHECK.equals(deployFileType)
					|| Constant.REDIS_REVISE.equals(deployFileType)) {
				command = "ps ux | grep " + processName + " | grep " + softLinkPath + " |awk '{print $2}'";
			}
		}else if(Constant.MONITOR_SERVICE.equals(clusterType)){
			if(Constant.MONITOR_SERVICE.equals(deployFileType)){
				//ps -aux | grep monitor| grep /project/bill03/test/tools/env/1.6.0/monitor
				String monitorPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.MONITOR;
				command = "ps ux | grep monitor | grep " + monitorPath;
			}else{
				command="ps ux|grep "+processName+"|grep -v grep";
			}
		}else if(Constant.DMDB.equals(clusterType)){
			///public/bp/DCBPortal_test/tools/env/0.0.2/dmdb/cfg/192.168.161.26_01
			//command="ps ux|grep "+processName+"|grep "+SOFT_LINK_PATH+"|grep -v grep";
			//主模式
			/*if (Constant.MAIN_PATTERN.equals(deployFileType)) {
				String dmdbPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DMDB_DIR + Constant.BIN + "dmdb_conn";
				command = "ps ux |grep " + dmdbPath +" |grep -v grep";
			} else if (Constant.INSTANCE_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath) + "mdb_store.conf";
				command = "ps ux |grep " + configPath + " |grep -v grep";
			} else if (Constant.ROUTE_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath)  +"mdb_tdal.conf";
				command = "ps ux |grep " + configPath + " |grep -v grep";
			}*/


//			if (Constant.MAIN_PATTERN.equals(deployFileType)) {
//				String dmdbPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DMDB_DIR + Constant.BIN + "dmdb_conn";
//				command = "ps ux |grep " + dmdbPath +" |grep -v grep";
//			} else
			if (Constant.INSTANCE_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath) + "dmdb.conf";
				command = "ps ux | grep dmdb_server |grep " + configPath + " |grep -v grep";
			} else if (Constant.ROUTE_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath)  +"mdb_tdal.conf";
				command = "ps ux | grep dmdb_server |grep " + configPath + " |grep -v grep";
			} else if (Constant.SYNC_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath)  +"dmdb.conf";
				configPath = configPath.replace(Constant.INSTANCE_PATTERN, Constant.SYNC_PATTERN);
				command = "ps ux | grep dmdb_sync |grep " + configPath + " |grep -v grep";
			} else if (Constant.MGR_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath)  +"dmdb.conf";
				configPath = configPath.replace(Constant.INSTANCE_PATTERN, Constant.MGR_PATTERN);
				command = "ps ux | grep dmdb_mgr |grep " + configPath + " |grep -v grep";
			} else if (Constant.WATCHER_PATTERN.equals(deployFileType)) {
				String configPath = FileTool.exactPath(softLinkPath)  +"dmdb.conf";
				configPath = configPath.replace(Constant.INSTANCE_PATTERN, Constant.WATCHER_PATTERN);
				command = "ps ux | grep dmdb_watcher |grep " + configPath + " |grep -v grep";
			} else if (Constant.MOVESYNC_PATTERN.equals(deployFileType)) {
				String moveSyncPattern = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DMDB_DIR + Constant.BIN + "sync_delete_log_file";
				command = "ps ux |grep " + moveSyncPattern + " |grep -v grep";
			} else {
				String dmdbPath = appRootPath + Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DMDB_DIR + "conf/mdb.conf";
				command = "ps ux |grep " + dmdbPath +" |grep -v grep";
			}
		} else if(Constant.M2DB.equals(clusterType)){
			String psPath=appRootPath+Constant.Tools;
			String instName = String.valueOf(params.get("m2dbInstanceName"));
			String execCmd = Constant.CHECK_AUTH_FILE_COMMON;
			command = MessageFormat.format(execCmd, Constant.M2DB, Constant.M2DB, instName, version, StringTool.object2String(System.nanoTime()));
			command = "cd " + psPath + "; " + command;
		} else if(Constant.ZOOKEEPER.equals(clusterType)){
			String psPath=appRootPath+Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.ZOOKEEPER;
			command="ps ux|grep "+processName+"|grep "+psPath+"|grep -v grep";
		} else if(Constant.JSTORM.equals(clusterType)){
			String psPath=appRootPath+Constant.Tools+ Constant.ENV + FileTool.exactPath(version) + Constant.JSTORM;
			command="ps ux|grep "+processName+"|grep "+psPath+"|grep -v grep";
		}else if(Constant.DCLOG.equals(clusterType)){
			StringBuffer cmdBuf = new StringBuffer("source ~/.bash_profile;cd ");
			cmdBuf.append(appRootPath).append(Constant.Tools).append(Constant.ENV).append(version).append("/").append(Constant.DCLOG_DIR).append(Constant.BIN)
			.append(";./").append(Constant.MONITOR_SH);
			command=cmdBuf.toString();
		}else if(Constant.DSF.equals(clusterType)){
			String psPath = appRootPath+Constant.Tools + Constant.ENV + FileTool.exactPath(version) + Constant.DSF;
			command="ps ux|grep "+processName+"|grep "+psPath+"|grep -v grep";
		}
		log.debug("组件状态检查命令: " + command);
		return command;
	}

	public static void main(String arg[]){
		ShellUtils cmdUtil = new ShellUtils("192.168.161.222","ah_test","ah_test6");
		String cmd =  " source ~/.bash_profile;cd /public/ah_test/ah_dccp/myapp/tools/env/dclog/bin/;./monitor.sh";
		String result = cmdUtil.execMsg(cmd);
		System.out.println(result);
	}
}
