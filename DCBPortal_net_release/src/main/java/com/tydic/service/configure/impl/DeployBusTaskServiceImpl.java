package com.tydic.service.configure.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.framework.osgi.commons.lang.StringUtils;
import com.tydic.service.configure.DeployBusTaskService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [DeployBusTaskServiceImpl]     
  * @Description:  [业务部署实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:09:25]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:09:25]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class DeployBusTaskServiceImpl implements DeployBusTaskService {
	
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DeployBusTaskServiceImpl.class);
	
	//JDK目录
	private final String JDK = "jdk";
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 业务部署
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return String
	 */
	@Override
	public String updateDistribute(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("开始业务部署, 参数:" + params.toString());
		
		StringBuffer returnStr = new StringBuffer();
		//集群ID
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		//集群类型
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		//主机ID
		String hostId = StringTool.object2String(params.get("HOST_ID"));
		//本地临时目录
		String localPath = params.get("webRootPath") + Constant.TMP + System.currentTimeMillis() + "/";
		//当前部署程序版本
		String versionDir = FileTool.exactPath("V" + StringTool.object2String(params.get("VERSION"))); // 获取当前版本号(前台传递)
		//当前部署程序名称
		String nameDir = StringTool.object2String(params.get("NAME")) + "/";
		//包类型
		String packageType = StringTool.object2String(params.get("PACKAGE_TYPE"));
		
		Map<String,Object> queryMap = new HashMap<String,Object>();
		//判断PERSONAL_CONF是不是为1：是,则创建目录
		queryMap.put("CLUSTER_TYPE", clusterType);
		queryMap.put("VERSION", params.get("VERSION"));
		queryMap.put("PACKAGE_TYPE", packageType);
		//获取部署版本任务信息
		Map<String,Object> deployTaskMap = coreService.queryForObject2New("deployHome.queryOcsDeploy", queryMap, dbKey);
		if(BlankUtil.isBlank(deployTaskMap)){
			 throw new RuntimeException("未查询到对应的版本文件，跳过不部署\n");
		}
		
		//部署根目录
		String appRootPath = null;
		
		//List<String> codes = (List<String>)params.get("codes");
		//版本名称
		String parentFileName = StringTool.object2String(deployTaskMap.get("NAME"));
		//版本子程序名称
		String defineFileName = StringTool.object2String(deployTaskMap.get("TASK_NAME"));
		//部署主机目录
		String businessCfgPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
		Trans sftClient = null;
		Trans ftpClient = null;
		
		//获取部署主机信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		ftpClient = FTPUtils.getFtpInstance(ftpDto);


		ftpClient.login();
	    try{
			String ftpFullPath = ftpDto.getFtpRootPath() + Constant.BUSS + FileTool.exactPath(packageType) + FileTool.exactPath(Constant.RELEASE) + nameDir + defineFileName;
			log.debug("部署主机版本路径:" + ftpFullPath);
			
			//获取部署主机信息
			Map<String, Object> queryHostMap = new HashMap<String, Object>();
			queryHostMap.put("HOST_ID", hostId);
			Map<String, Object> hostMap = coreService.queryForObject2New("host.queryHostById", queryHostMap, dbKey);
			if (BlankUtil.isBlank(hostMap) || hostMap.isEmpty()) {
				throw new RuntimeException("主机【" + hostMap.get("HOST_IP") + "(" + hostMap.get("SSH_USER") + ")】不存在,请检查\n");
			}
			
			//根据集群ID获取集群部署路径
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", clusterId);
			queryClusterMap.put("CLUSTER_TYPE", clusterType);
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
			if (BlankUtil.isBlank(clusterMap) || clusterMap.isEmpty() || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				throw new RuntimeException("集群信息查询失败, 请检查！");
			}
			//集群部署路径
			appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//集群编码
			String clusterCode = StringTool.object2String(clusterMap.get("CLUSTER_CODE"));
			//判断当前集群是否需要根据IP区分
			String personConf = StringTool.object2String(clusterMap.get("PERSONAL_CONF"));
			
			//部署主机FTP信息
		    String ip = StringTool.object2String(hostMap.get("HOST_IP"));
			String sshUserName = StringTool.object2String(hostMap.get("SSH_USER"));
			String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
			ShellUtils cmdUtil = new ShellUtils(ip, sshUserName, sshPwd);
			
			//先上传到远程主机
			sftClient = FTPUtils.getFtpInstance(ip, sshUserName, sshPwd, ftpDto.getFtpType());
			sftClient.login();
			
			//判断该业务程序是否需要JDK支持，如果需要JDK支持而该主机未安装JDK则自动上传JDK包并且安装

			String installJDK = StringTool.object2String(clusterMap.get("INSTALL_JDK"));
			log.debug("业务程序：" + clusterType + "， 是否需要安装JDK： " + installJDK);
			if (!BlankUtil.isBlank(installJDK) && BusinessConstant.PARAMS_BUS_1.equals(installJDK)) {
				//判断JDK是否已经安装，如果安装就不需要在推送JDK
				String checkJdkCmd = "cat ~/.bash_profile | grep JAVA_HOME=";
				String jdkRst = cmdUtil.execMsg(checkJdkCmd);
				log.debug("业务程序部署， 检查远程主机JDK是否安装，命令直接结果： " + jdkRst);
				if (BlankUtil.isBlank(jdkRst)) {
					//判断JDK目录是否存在，如果不存在在创建目录
					String jdkTargetPath = appRootPath + Constant.Tools + StringTool.object2String(clusterMap.get("TARGET_SH_PATH"));
					boolean isExist = sftClient.isExistPath(jdkTargetPath);
					if (!isExist) {
						sftClient.mkdir(jdkTargetPath);
						returnStr.append("mkdir ["+jdkTargetPath+"] -------------------- Success\n");
					}
					log.debug("部署"+clusterType+", 生成JDK目标目录成功， 目标目录 ---> " + jdkTargetPath);
					
					//判断JDK是否存在，如果存在则不需要再上传
					Boolean isExists = false;
					Vector<FileRecord> fileList = sftClient.getFileList(jdkTargetPath);
					for (int j=0; j<fileList.size(); j++) {
						if (JDK.equalsIgnoreCase(fileList.get(j).getFileName()) 
								&& String.valueOf(FileRecord.DIR).equalsIgnoreCase(String.valueOf(fileList.get(j).getFileType()))) {
							isExists = true;
							break;
						}
					}
					//JDK安装文件不存在，则下载
					if(!isExists) {
						returnStr.append("get [JDK] -------------------- Start\n");
						//上传文件
						String jdkSourcePath = String.valueOf(clusterMap.get("SOURCE_SH_FILE"));
						if (!BlankUtil.isBlank(jdkSourcePath) && jdkSourcePath.indexOf("/") != -1) {
							jdkSourcePath = jdkSourcePath.substring(0, jdkSourcePath.lastIndexOf("/"));
						}
						//版本发布服务器获取JDK
						String sourceFilePath = ftpDto.getFtpRootPath() + "env/jdk.zip";
						//远程主机JDK位置
						String targetFilePath = FileTool.exactPath(jdkTargetPath) + "env/jdk.zip";
						//将版本发布服务器JDK拉起到本地
						InputStream inputStream = ftpClient.get(sourceFilePath);
						sftClient.put(inputStream, targetFilePath);
						inputStream.close();
						ftpClient.completePendingCommand();
						returnStr.append("get [JDK] -------------------- Success\n");
						log.debug("业务程序部署，安装JDK，上传JDK文件包成功...");
					}
					
					//设置JAVA环境变量
					String jdkRootPath = FileTool.exactPath(jdkTargetPath) + FileTool.exactPath("env");
					String envCmd = "unzip -o -q " + jdkRootPath + "jdk.zip -d "+ jdkRootPath + ";";
						envCmd += "chmod -R a+x " + jdkRootPath + "jdk;" ;
						envCmd += "echo 'export JAVA_HOME="+jdkRootPath+"jdk' >> ~/.bash_profile; ";
						envCmd += "echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bash_profile; ";
						envCmd += "echo 'export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar' >> ~/.bash_profile; ";
						envCmd += "source ~/.bash_profile";
					String envStr = cmdUtil.execMsg(envCmd);
					log.debug("设置JDK环境变量，执行命令：" + envCmd + "，返回结果：" + envStr);
					returnStr.append("install [JDK] -------------------- Success\n");
				}
			}
			
			String localFilePath = localPath + defineFileName;
			log.debug("本地临时路径:" + localFilePath);
			ftpClient.get(ftpFullPath, localFilePath);
			log.debug("部署主机文件下载到本地成功, 本地保存路径:" + localFilePath);
			
			returnStr.append("正在上传文件【"+defineFileName+"】 \n");
			
			String remotePath = appRootPath + Constant.BUSS + versionDir + defineFileName;
			log.debug("应用主机路径:" + remotePath);
			sftClient.put(localFilePath, remotePath);
			returnStr.append("上传文件【"+defineFileName+"】  ......  成功 \n");
			
			//解压缩应用主机业务程序包
			String cmd = SystemProperty.getContextProperty("file.tar.sh");
			if(defineFileName.lastIndexOf(".zip") > -1){
				 cmd = SystemProperty.getContextProperty("file.zip.sh");
			}
			cmd = MessageFormat.format(cmd, appRootPath + Constant.BUSS + versionDir, defineFileName);
			
			//解压完成后删除gz包
			//cd /public/bp/dccp/business/V1.0.2/;tar xf route.tar.gz; rm -rf route.tar.gz
			cmd += "; rm -rf " + defineFileName;
			
			String keywords = SystemProperty.getContextProperty("file.replace.keywords");
			String findReplaceCmd = SystemProperty.getContextProperty("find.replace.sh");
			String findReplaceCmdStr ="";
			if(StringUtils.isNotBlank(keywords)){
				String [] keys = keywords.split(",");
				for(String key : keys){
					String value = "";
					if(key.equals("$HOST_IP")){
						value = ip;
					}else if(key.equals("$HOST_NAME")){
						value = sshUserName;
					}
					findReplaceCmdStr += findReplaceCmd.replace("$1$", key).replace("$2$", value).replace("$5$", Constant.CFG_DIR)+";";
				}
				//替换文件中的关
				cmd =cmd +";" + findReplaceCmdStr;
			}
			
			returnStr.append("正在解压文件【"+defineFileName+"】  \n");
			//再到远程主机上解压
			log.debug("部署解压缩命令: " + cmd);
			String resultStr = cmdUtil.execMsg(cmd);
			log.debug("部署解压缩命令执行结果: " + resultStr);
			if(resultStr.toLowerCase().indexOf(ResponseObj.ERROR) >=0 
					 || resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >=0){
				log.error("执行脚本失败--->" + resultStr);
				returnStr.append("解压文件【"+defineFileName+"】 ...... 失败 \n"+resultStr);
				throw new RuntimeException(resultStr);
			}else{
				returnStr.append("解压文件【"+defineFileName+"】 ...... 成功 \n");
			}
			
			//业务配置文件目录
			//  /public/bp/AH_DCBPortal/conf/business_config/release/prePayment/DIC-BIL-DUCC-SH_V12.9.1.5
			String path = ftpDto.getFtpRootPath() + businessCfgPath + FileTool.exactPath(packageType) + parentFileName;
			
			//部署主机SSH对象
			ShellUtils deployUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(),ftpDto.getPassword());
			
			//判断业务集群配置目录是否存在，如果不存在则创建集群配置目录
			//业务主集群编码
			String busClusterCode = StringTool.object2String(params.get("BUS_CLUSTER_CODE"));
			///public/bp/DCBPortal_Release2/conf/business_config/release/DIC-BIL-NET_V12.0.0.0/OCSCODE/route
			String clusterCfgPath = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + clusterType;
			boolean isExistCfgPath = ftpClient.isExistPath(clusterCfgPath);
			log.debug("目录: " + clusterCfgPath + ", 是否存在: " + isExistCfgPath  + ", 集群类型: " + clusterType);
			if (!isExistCfgPath) {
				if (BusinessConstant.PARAMS_BUS_1.equals(personConf)) {
					//判断当前IP对应配置文件是否存在，如果不存在则复制
					String clusterHostCfgPath = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + ip;
					boolean isExistHostCfgPath = ftpClient.isExistPath(clusterHostCfgPath);
					if (!isExistHostCfgPath) {
						//创建部署主机配置文件目录
						ftpClient.mkdir(clusterHostCfgPath);
						//上面这个命令会将192.168.161.26目录里面的内容复制到192.168.161.28目录
						String rmFile = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + clusterType;
						String cmdMove = "cd "+ path +"; cp -R "+ clusterType+ " ./" + FileTool.exactPath(busClusterCode) + ip + "; rm -rf " + rmFile;
						String execRst = deployUtil.execMsg(cmdMove);
						log.debug("区分IP业务集群配置文件初始化命令:" + cmdMove + "， 执行结果: " + execRst);
						log.debug("创建业务集群配置文件成功....");
					}
					
				} else {
					//创建业务集群配置文件
					ftpClient.mkdir(clusterCfgPath);
					//将当前版本对应集群类型文件复制到该目录
					String cpCmd = "cd " + path + "; cp -R " + clusterType + " ./" + busClusterCode;
					String execRst = deployUtil.execMsg(cpCmd);
					log.debug("不区分IP业务集群配置文件初始化命令: " + cpCmd + "， 执行结果: " + execRst);
					log.debug("创建业务集群配置文件成功...");
				}
			} else {
				//判断当前集群是否需要根据IP区分
				if (BusinessConstant.PARAMS_BUS_1.equals(personConf)) {
					//判断当前IP对应配置文件是否存在，如果不存在则复制
					String clusterHostCfgPath = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + ip;
					boolean isExistHostCfgPath = ftpClient.isExistPath(clusterHostCfgPath);
					if (!isExistHostCfgPath) {
						//创建部署主机配置文件目录
						ftpClient.mkdir(clusterHostCfgPath);
					}
					//上面这个命令会将192.168.161.26目录里面的内容复制到192.168.161.28目录
					String rmFile = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + clusterType;
					String cmdMove = "cd "+ path +"; cp -R "+ clusterType+ " ./" + FileTool.exactPath(busClusterCode) + ip + "; rm -rf " + rmFile;
					//String execRst = deployUtil.execMsg(cmdMove);
					//log.debug("业务集群配置文件创建结果: " + execRst);
					log.debug("创建业务集群配置文件成功......");
				} else {
					//将当前版本对应集群类型文件复制到该目录
					//String cpCmd = "cd " + path + "; cp -R " + clusterType + " ./" + busClusterCode;
					//String execRst = deployUtil.execMsg(cpCmd);
					//log.debug("业务集群配置文件创建结果: " + execRst);
					//log.debug("创建业务集群配置文件成功...");
				}
			}

			///public/bp/AH_DCBPortal/conf/business_config/release/prePayment/DIC-BIL-DUCC-SH_V12.9.1.5/PrePaymeny/other/
			String tmpPath = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + FileTool.exactPath(clusterType);
			if (BusinessConstant.PARAMS_BUS_1.equals(personConf)) {
				tmpPath = FileTool.exactPath(path) + FileTool.exactPath(busClusterCode) + FileTool.exactPath(ip) + clusterType;
			}
			log.debug("远程主机配置文件根目录: " + tmpPath);
			
			Vector<FileRecord> files =new Vector<FileRecord>();
			//获取该目录下的所有文件
			getPath(ftpClient, tmpPath, "false", files);
			log.debug("该业务集群下配置文件数量：" + files.size() + ", 版本发布服务器路径: " + tmpPath);

			for(FileRecord file : files){
				if(file.isDirectory() || new File(file.getTargetPath()).isDirectory()){
					continue;
				}
				//获取配置文件
				String filePath = (FileTool.exactPath(file.getFilePath()) + file.getFileName()).replace(tmpPath, "");
				//获取文件名，当存在多级目录，远程主机文件只要文件名称
				if (filePath.indexOf("/") != -1) {
					filePath = filePath.substring(filePath.lastIndexOf("/"));
				}
				//远程主机配置文件目录
				String appPath = appRootPath + Constant.BUSS + versionDir + Constant.CFG_DIR + filePath;
				//本地临时目录
				String localTmpPath = FileTool.exactPath(localPath) + FileTool.exactPath(clusterCode) + FileTool.exactPath(clusterType) + file.getFileName();
				//将部署主机配置文件临时保存到本地
				ftpClient.get(FileTool.exactPath(file.getFilePath()) +file.getFileName(), localTmpPath);
				try {
					// 存在sp_switch.xml文件则上移
					if(Constant.SP_SWITCH.equals(file.getFileName())){
						//删除sp_switch.xml文件
						ftpClient.delete(FileTool.exactPath(file.getFilePath()) +file.getFileName());
						//将部署主机sp_switch.xml文件移位
						String spFinalPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessCfgPath) + FileTool.exactPath(packageType) + file.getFileName();
						ftpClient.put(localTmpPath, spFinalPath);
					}else{
						sftClient.put(localTmpPath, appPath);
						log.debug("业务程序部署，配置文件同步，本地临时目录: " + localTmpPath + ", 远程主机目录: " + appPath + ", 主机: " + ip);
					}
				} catch (Exception e) {
					log.error("动态同步文件失败, 失败原因:", e);
				} 
			}
			//保存业务集群部署信息
		    //判断当前版本是否部署
		    Map<String, String> addParams = new HashMap<String, String>();
		    addParams.put("CLUSTER_ID", clusterId);
		    addParams.put("CLUSTER_TYPE", clusterType);
		    addParams.put("HOST_ID", hostId);
		    addParams.put("PACKAGE_TYPE", packageType);
		    addParams.put("VERSION", StringTool.object2String(params.get("VERSION")));
		    List<HashMap<String, String>> deployList = coreService.queryForList("deployHome.queryBusinessDeployList", addParams, dbKey);
		    if (!BlankUtil.isBlank(deployList)) {
		    	Map<String, String> updMap = new HashMap<String, String>();
		    	updMap.put("ID", deployList.get(0).get("ID"));
		    	coreService.updateObject("deployHome.updateBusinessDeployList", updMap, dbKey);
		    } else {
		    	coreService.insertObject("deployHome.addBusinessDeployList", addParams, dbKey);
		    }
		    
		    //修改部署状态以及最新部署版本
		    Map<String, String> tempMap = new HashMap<String, String>();
		    tempMap.put("ID", StringTool.object2String(params.get("ID")));
		    tempMap.put("VERSION", StringTool.object2String(params.get("VERSION")));
		    coreService.updateObject("deployHome.updateHostStateEffectiveById", tempMap,dbKey);
		}catch(Exception e){
			returnStr.append("上传文件【"+defineFileName+"】失败， 失败原因:\n" + e.getMessage() + "\n");
			log.error("上传文件【"+defineFileName+"】失败", e);
			throw new RuntimeException(returnStr.toString());
		}finally {
			if(sftClient!=null){
				try{
					//创建 log目录
					sftClient.mkdir(appRootPath + Constant.BUSS + Constant.LOG_DIR);
				}catch(Exception e){
					log.error("创建log目录失败");
				}
				sftClient.close();
			}
			if(ftpClient!=null){
				ftpClient.close();
			}
			//删除临时文件
			if (!BlankUtil.isBlank(localPath)) {
				//删除临时文件
				FileUtil.deleteFile(localPath);
			}
		}
	    return returnStr.toString();			
	}

	/**
	 * 迭代查询给于路径下的所有文件详情
	 *
	 * @param trans
	 * @param subPath
	 * @param isRoot
	 * @param files
	 * @throws Exception
	 */
	public static void getPath(Trans trans,String subPath,String isRoot,Vector<FileRecord> files) throws Exception{
		try {
			Vector<FileRecord> subFile = null;
			//获取子目录下的所有文件列表以及目录
			subFile =trans.getFileList(subPath);
			if(subFile!=null && subFile.size()>0){
				for(int i=0;i<subFile.size();i++){
					//获取到每一个文件对象
					FileRecord file= subFile.get(i);
					//给于文件对象一个随机id
					String currentId=UUID.randomUUID().toString();
					file.setCurrId(currentId);
					//将对象添加到List当中
					files.add(subFile.get(i));
					if(!file.isFile()){
						//新目录（查询出的当前文件夹的路径）
						String newPath;
						if(isRoot=="true"){
							newPath=subPath+file.getFileName();
						}else{
							newPath=subPath+"/"+file.getFileName();
						}
						getPath(trans,newPath,"false",files);
					}
				}
			}
		} catch (Exception e) {
			log.error("获取"+subPath+"下的子目录失败-->", e);
			throw new Exception(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
	 
	}
	
	

}
