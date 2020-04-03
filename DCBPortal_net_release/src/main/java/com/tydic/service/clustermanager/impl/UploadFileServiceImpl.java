package com.tydic.service.clustermanager.impl;

import PluSoft.Utils.JSON;
import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.clustermanager.UploadFileService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.clustermanager.impl]    
  * @ClassName:    [UploadFileServiceImpl]     
  * @Description:  [版本上传管理实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:16:40]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:16:40]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class UploadFileServiceImpl implements UploadFileService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(UploadFileServiceImpl.class);
	Vector<FileRecord> files = null;
	@Autowired
	public CoreService coreService;
	
	/**
	 * 版本包上传
	 * @param uFile 上传包对象
	 * @param formMap 业务参数
	 * @param dbKey
	 * @return String
	 */
	public String insertFileUpload(MultipartFile uFile, Map<String, String> formMap, String dbKey) throws Exception{
		log.debug("开始版本上传， 参数:" + formMap.toString());

		//获取部署主机信息
		Map<String, Object> queryMap = new HashMap<String, Object>();

		//上传包业务类型（1：组件 2：业务）
		String fileType = StringTool.object2String(formMap.get("FILE_TYPE"));
		//根据前缀判断文件包类型
		queryMap.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
		queryMap.put("EXTENDS_FIELD", formMap.get("PACKAGE_TYPE"));
		List<HashMap<String, Object>> webPackageList = coreService.queryForList2New("config.queryConfigList", queryMap, dbKey);
		if(!BlankUtil.isBlank(webPackageList)){
			formMap.put("PACKAGE_TYPE", StringTool.object2String(webPackageList.get(0).get("CONFIG_VALUE")));
		}else{
			throw new RuntimeException("该上传的文件不合法，找不到对应的包类型，请检查！");
		}
		//获取历史版本记录
		queryMap.clear();
		queryMap.put("VERSION", formMap.get("VERSION"));
		queryMap.put("FILE_TYPE", formMap.get("FILE_TYPE"));
		if(!BusinessConstant.FILE_TYPE_COMPONENT.equals(fileType)) {
			queryMap.put("PACKAGE_TYPE", formMap.get("PACKAGE_TYPE"));
		}
		List<HashMap<String,Object>> versionList = coreService.queryForList2New("ftpFileUpload.queryVersion", queryMap, dbKey);
		if(!BlankUtil.isBlank(versionList)){
			String lastVersion =  StringTool.object2String(versionList.get(0).get("VERSION"));
			String currentVersion = StringTool.object2String(formMap.get("VERSION"));
			if(StringTool.compareVersion(currentVersion, lastVersion) == -1){
				throw new RuntimeException("当前上传的版本号要大于之前的版本号");
			}

			//版本上传版本必须比上个版本要高
			if (lastVersion.equals(currentVersion) && BusinessConstant.FILE_TYPE_COMPONENT.equals(fileType)) {
				throw new RuntimeException("当前上传的版本号要大于之前的版本号");
			}

			formMap.put("lastVersion", lastVersion);
		}else{
			formMap.put("lastVersion","none");
		}
		
		//参数准备
		FtpDto ftpDto = SessionUtil.getFtpParams();

		Trans trans = null;
		Trans remoteTrans = null;
		String fileName="";
		InputStream input =null;
		try{
			if(!BlankUtil.isBlank(ftpDto)){
				//获取部署主机信息

				trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans.login();
				//文件上传类型， 1：远程主机上传，主要是本地操作堡垒机上传版本  非1：本地版本包上传
				String uploadType = StringTool.object2String(formMap.get("uploadType"));
				if(BusinessConstant.UPLOAD_TYPE_REMOTE.equals(uploadType)){   //远程主机上传程序包
					fileName = StringTool.object2String(formMap.get("remoteFile"));
					Map<String,String> remoteConfigMap = SessionUtil.getConfigByGroupCode("WEB_REMOTE_FILE_CFG");
					String ip = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_IP");
					String user = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_USER");
					String pwd = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PASSWD");
					String path = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PATH");
					remoteTrans = FTPUtils.getFtpInstance(ip, user, pwd, ftpDto.getFtpType());
					remoteTrans.login();
					String localPath = formMap.get("webRootPath") + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
					remoteTrans.get(path + fileName,localPath);
					File file =new File(localPath);
					input = new FileInputStream(file);  
				}else{
					//本地主机上传程序包
					fileName = uFile.getOriginalFilename();  // ocs_v0.0.1.tar.gz
					input = uFile.getInputStream();
				}
				
				String filePath="";
				//上传后进行解压
				//组件版本
				if(BusinessConstant.FILE_TYPE_COMPONENT.equals(fileType)){
					filePath = ftpDto.getFtpRootPath()  + fileName;
					formMap.put("FILE_PATH", ftpDto.getFtpRootPath());
					log.debug("组件程序部署主机保存路径:" + filePath);
					
					//通过文件流与远程文件全路径上传
					trans.put(input, filePath);		
					log.debug("组件版本包上传成功, 远程文件路径: " + filePath);
					
					// 插入数据库
					addFtpServer(formMap, dbKey);
					log.debug("组件版本数据更新成功...");
					
					fileUnZip(ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getHostIp(), ftpDto.getFtpRootPath(), formMap, fileName);
					log.debug("组件版本包解压缩成功...");
					
					// 解压完成后的小版本入库操作
					ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(),ftpDto.getUserName(),ftpDto.getPassword());
					insertReleaseVersion(trans,cmdUtil,ftpDto.getFtpRootPath(),fileName,formMap);
					log.debug("组件版本解压缩后组件程序数据入表成功...");
				} else {// 业务类
					String packageType = StringTool.object2String(formMap.get("PACKAGE_TYPE")); 
					filePath = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath(packageType) + fileName;
					formMap.put("FILE_PATH", ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath(packageType));
					log.debug("业务程序部署主机保存路径:" + filePath);
					//通过文件流与远程文件全路径上传
					trans.put(input, filePath);				 
					// 插入数据库
					addFtpServer(formMap, dbKey);
					log.debug("业务版本数据更新成功...");
					String busClusterId = StringTool.object2String(formMap.get("BUS_CLUSTER_ID"));
					
					serviceFileUnZip(ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getHostIp(),ftpDto.getFtpRootPath(),formMap,fileName,packageType);
					log.debug("业务版本包解压缩成功...");
					
					// 解压完成后的入库操作
					formMap.remove("BUS_CLUSTER_ID");
					insertServicePackage(trans,ftpDto.getFtpRootPath(),fileName,formMap);
					log.debug("业务版本解压缩后组件程序数据入表成功...");
					
					//删除版本和集群关联关系
					int delCount = coreService.deleteObject("ftpFileUpload.deleteFileByServiceId", formMap, dbKey);
					log.debug("删除版本集群关联关系成功， 删除记录数: " + delCount);
					
					//插入包与集群的关系表中
					String [] busClusterIds = busClusterId.split(",");
					for(String id : busClusterIds){
						Map<String,String> tempMap = new HashMap<String,String>();
						tempMap.put("BUS_CLUSTER_ID", id);
						tempMap.put("SERVICE_ID", formMap.get("SERVER_ID")+"");
						coreService.insertObject("ftpFileUpload.insertFileClusterConfig", tempMap, dbKey);
					}
					
				}
			}
		}catch(Exception e){
			log.error("文件上传失败， 失败原因:", e);
			//业务类上传失败后进行删除操作（路径：根目录/conf/business_config/release/版本包）
			try {
				if(BusinessConstant.FILE_TYPE_BUSINESS.equals(fileType)){
					String busConfPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
					String fileNameWithoutPostfix = fileName.substring(0, fileName.lastIndexOf(".tar.gz"));
					
					String command = "cd " + (ftpDto.getFtpRootPath() + busConfPath) + ";rm -rf "+fileNameWithoutPostfix;
					// 根据远程IP地址,用户名,密码创建命令执行对象
					ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(),ftpDto.getUserName(),ftpDto.getPassword());
					String result=cmdUtil.execMsg(command);
					log.info("业务版本上传失败后，删除文件服务器上对应文件成功:" + result);
				}
			} catch (Exception e1) {
				log.error("业务版本上传失败后，删除文件服务器上对应文件出错，原因：--->", e1);
			}
			throw new Exception(e.getMessage());
		}finally{
			// 关闭资源
			if(trans != null){
				trans.close();
			}
			if(input != null){
				input.close();
			}
			if(remoteTrans != null){
				remoteTrans.close();
			}
		}
		return JSON.Encode(ftpDto.getFtpRootPath());
	}
	
	/**
	 * 非业务类文件解压
	 */
	private String fileUnZip(String ftpUserName,String ftpPasswd,String ftpIp,String filePath,Map formMap,String fileName){
		String lastVersion  = StringTool.object2String(formMap.get("lastVersion"));
		String currentVersion  = StringTool.object2String( formMap.get("VERSION"));
		String result = null;
		try{
			// 根据远程IP地址,用户名,密码创建命令执行对象
			ShellUtils cmdUtil = new ShellUtils(ftpIp,ftpUserName,ftpPasswd);
			String command ="";
			//在back下创建目录
			//String command = "cd " + filePath + "; mkdir -p " + Constant.BACK + lastVersion ;
			//打包旧版本
			//command+="; zip -qr ./" + Constant.BACK + lastVersion + "/env_V" + lastVersion + ".zip" + " ./" + Constant.ENV + "* ./" + Constant.CONF + Constant.PLAT_CONF + "*";
//			if(!lastVersion.equals("none")){
//				result = cmdUtil.execMsg(command);
//			}
			// 解压现在上传的文件
			command = "cd " + filePath + ";unzip -qo " + fileName + ";chmod -R a+x *.sh";
			command+=";cd " + (FileTool.exactPath(filePath) + Constant.ENV) + ";mkdir -p "+ currentVersion+";mv *.zip ./"+currentVersion;
			result = cmdUtil.execMsg(command);
			if(result.toLowerCase().indexOf(Constant.ERROR) > -1 || result.toLowerCase().indexOf(Constant.FAILED) > -1){
				log.error("解压失败--->"+result);
				throw new Exception(result);
			}
			log.info("解压完成:" + result);
		}catch(Exception e){
			log.error("框架版本包上传，解压失败, 失败原因：", e);
			throw new RuntimeException("解压失败!");
		}
		return null;
	}
	
	
	/**
	 * 框架程序包入库
	 * @param trans
	 * @param ftpPath
	 * @param fileName
	 * @param formMap
	 * @throws Exception
	 */
	private void insertReleaseVersion(Trans trans,ShellUtils cmdUtil ,String ftpPath,String fileName,Map formMap) throws Exception{
		try {
			List<Map<String,String>> insertParams = new ArrayList<>();
			// env_v0.0.1
			String fileNameWithoutPostfix = fileName.substring(0, fileName.lastIndexOf(".zip"));
			// _v0.0.1
			String versionPostfix = fileNameWithoutPostfix.substring(fileNameWithoutPostfix.toLowerCase().lastIndexOf("_v"));
			// 0.0.1
			String version = versionPostfix.substring(versionPostfix.toLowerCase().indexOf("_v") + 2);
			//	根目录/env/0.0.1（版本）/
			Vector<FileRecord> fileRecordList = trans.getFileList(ftpPath + Constant.ENV + version + "/*");
			if(fileRecordList==null  || fileRecordList.size()<1){
				throw new Exception("未找到程序包，请检查！");
			}
			boolean jdk_flag=false;
			for(FileRecord fileRecord : fileRecordList){
				Map<String,String> insertMap = new HashMap<>();
				String tempName = fileRecord.getFileName();
				String clusterType = tempName.substring(0,tempName.lastIndexOf(".zip"));
				insertMap.put("CLUSTER_TYPE", clusterType);
				insertMap.put("VERSION", version);
				insertMap.put("STATE", "1");
				insertParams.add(insertMap);
				if(tempName.equalsIgnoreCase(Constant.JDK_ZIP)){
					jdk_flag=true;
				}
			}
			if(jdk_flag){
				String cmds="cd "+ftpPath + Constant.ENV + version+";mv "+Constant.JDK_ZIP+" ..";
				String result=cmdUtil.execMsg(cmds);
				if(result.toLowerCase().indexOf(Constant.ERROR) > -1 || result.toLowerCase().indexOf(Constant.FAILED) > -1){
					log.error("jdk.zip移动失败--->"+result);
					throw new Exception(result);
				}
			}
			
			// 数据库插入
			coreService.insertBatchObject("releaseVersion.insertBatchReleaseVersion", insertParams, FrameConfigKey.DEFAULT_DATASOURCE);
			log.debug("批量添加版本包包含组件信息列表...");
			
			//相同版本组件包上传逻辑
//			if (!BlankUtil.isBlank(insertParams)) {
//				for (int i=0; i<insertParams.size(); i++) {
//					Map<String, String> versionObj = insertParams.get(i);
//					List<HashMap<String, String>> versionList = coreService.queryForList("releaseVersion.queryReleaseVersion", versionObj, FrameConfigKey.DEFAULT_DATASOURCE);
//					if (!BlankUtil.isBlank(versionList)) {
//						//修改当前上传版本数据
//						coreService.updateObject("releaseVersion.updateReleaseVersion", versionObj, FrameConfigKey.DEFAULT_DATASOURCE);
//					} else {
//						//添加组件记录
//						coreService.insertObject("releaseVersion.insertyReleaseVersion", versionObj, FrameConfigKey.DEFAULT_DATASOURCE);
//					}
//				}
//			}
			
		} catch (Exception e) {
			log.error("框架版本包上传，程序包入库失败, 失败原因: ", e);
			throw new RuntimeException("程序包入库失败!");
		}
	}
	
	/**
	 * 业务类文件解压
	 */
	private String serviceFileUnZip(String ftpUserName,String ftpPasswd,String ftpIp,String ftpPath,Map formMap,String fileName,String packageType){
		// 根据远程IP地址,用户名,密码创建命令执行对象
		ShellUtils cmdUtil = null;
		String fileNameWithoutPostfix="1";
		String fileRootPath =  ftpPath + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath(packageType);
		try{
			cmdUtil = new ShellUtils(ftpIp,ftpUserName,ftpPasswd);
			String fileSuffixType = StringTool.object2String((formMap.get("fileSuffixType")));
			//上传文件格式类型
			fileNameWithoutPostfix = fileName.substring(0, fileName.lastIndexOf(fileSuffixType));
			String command = "mkdir -p "+ fileRootPath + fileNameWithoutPostfix + ";cd "+fileRootPath+";tar -zxf " + fileName +" -C " +fileNameWithoutPostfix;
			if(".zip".equals(fileSuffixType)){
				  command = "mkdir -p "+ fileRootPath + fileNameWithoutPostfix + ";cd "+fileRootPath+";unzip -o " + fileName +" -d "+fileNameWithoutPostfix;
			}
			// 解压现在上传的文件
			
			String result = cmdUtil.execMsg(command);
			
			if(result.indexOf(Constant.FLAG_ERROR) >= 0){
				log.error("解压失败--->"+result);
				throw new Exception(result);
			}
			log.info("解压完成:" + result);
			
			log.info("执行脚本");
			//OCS_V1.0.0.tar.gz
			String currentName = fileName;
			String lastName = "\'\'";
			// 先根据插入时间得到最新版本跟上个版本文件名称
			List<HashMap<String,Object>> nameList = coreService.queryForList2New("ftpFileUpload.queryServiceNameInfo", formMap, FrameConfigKey.DEFAULT_DATASOURCE);
			if(nameList.size() == 2){
				//OCS_V0.1.0.tar.gz
				lastName = (String) nameList.get(1).get("FILE_NAME");
				if(formMap.get("sameVersion") != null){
					lastName = currentName;
				}
			}
			
//			command = "cd " + filePath + "; chmod a+x sync_cfg.sh; ./" + "sync_cfg.sh " + currentName + " " + lastName; 
			String execCmd = SystemProperty.getContextProperty("sync.sh");
			execCmd = MessageFormat.format(execCmd, ftpPath,Constant.BUSS,Constant.CFG,packageType, currentName, lastName);
			log.debug("业务类文件解压缩执行命令: " + execCmd);
			result = cmdUtil.execMsg(execCmd);
			log.debug("业务类文件解压缩执行命令， 返回结果: " + result);
			if(result.toLowerCase().indexOf(Constant.FLAG_ERROR) > -1 
					|| result.toLowerCase().indexOf(Constant.FAILED) > -1){
				result = result.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
				log.error("执行脚本失败--->"+result);
				
//				稽核文件失败返回的信息如下
//				$$start files check ...
//				./dcbilling/cfg/DCASQL.xml: FAILED
//				./dcbilling/cfg/CloudRECtl.callrel.xml: FAILED
//				./dcbilling/cfg/aaa.txt: FAILED open or read
//				end files check ...
//				Sync Config Files And Merge Pakage Success...
//				Success
//
//				md5sum: ./dcbilling/cfg/aaa.txt: 没有那个文件或目录
//				md5sum: WARNING: 1 of 348 listed files could not be read
//				md5sum: WARNING: 2 of 347 computed checksums did NOT match
//				$$
				if (result.toLowerCase().indexOf(": " + Constant.FAILED) > -1) {
					
					//文件根目录
					String checkPath = FileTool.exactPath(ftpPath) + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath(packageType) + "cfg_tmp";
					//解析获取稽核失败的文件列表
					StringBuffer buffer = new StringBuffer(200);
					byte [] bytes = result.getBytes();
					InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(bytes));
					BufferedReader reader = new BufferedReader(isr);
					String lineMsg = "";
					while((lineMsg = reader.readLine()) != null) {
						if (lineMsg.toLowerCase().indexOf(": " + Constant.FAILED) > -1) {
							buffer.append(lineMsg);
							buffer.append("\n");
						}
					}
					reader.close();
					isr.close();
					throw new RuntimeException("<textarea style='height:250px;width:650px;text-align:left;'>文件稽核失败，稽核文件根目录：" + checkPath + "，失败文件列表：\n" + buffer.toString() + "</textarea>");
				}
				throw new RuntimeException(result);
			}
			
		}catch(Exception e){
			try{
				cmdUtil.execMsg("rm -rf " + fileRootPath + fileNameWithoutPostfix+";rm -rf " + fileRootPath +Constant.RELEASE_DIR + fileNameWithoutPostfix);
			}catch(Exception e1){
				log.error("文件上传失败后，删除残留文件失败", e1);
			}
			throw new RuntimeException("文件上传失败,"+ e.getMessage());
		}
		return null;
	}
	
	/**
	 * 版本包回退
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return void
	 */
	public void deleteOrBack(Map<String, String> params, String dbKey) throws Exception{
		String flag = StringTool.object2String(params.get("flag"));
		// 如果为删除请求,判断当前是否只有一个版本
		if(flag != null && flag.equals("delete")){
			// 查询数据库获取上个版本号
			//获取历史版本记录
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("VERSION", params.get("VERSION"));
			queryMap.put("FILE_TYPE", params.get("FILE_TYPE"));
			List<HashMap<String, Object>> versionList = coreService.queryForList2New("ftpFileUpload.queryVersion", queryMap, dbKey);
			if(versionList.size() == 1){// 当前只有一个版本,直接删除
				log.debug("版本删除:当前只有一个版本,删除操作");
				String currentVersion = (String) versionList.get(0).get("VERSION");
				params.put("VERSION", currentVersion);
				coreService.deleteObject("ftpFileUpload.deleteFileInfoByVersion", params, dbKey);
			}else{// 当前不止一个版本,版本回退
				log.debug("版本删除:当前有多个版本,作版本回退操作");
				backVersion(params);
			}
		}else{// 不为删除请求,回退
			log.debug("版本删除:当前有多个版本,作版本回退操作");
			backVersion(params);
		}
	}

	/**
	 * 版本回退
	 */
	private void backVersion(Map params) throws Exception {
		//查询map
		// 查询数据库获取上个版本号
		List<HashMap<String, Object>> versionList = coreService.queryForList2New("ftpFileUpload.queryVersion", params, FrameConfigKey.DEFAULT_DATASOURCE);
		if(versionList.size() == 1){
			log.debug("版本回退:当前只有一个版本");
			throw new RuntimeException("当前只有一个版本,无法回退");
		}else if(versionList.size() == 0){
			log.debug("版本回退:当前无任何版本");
			throw new RuntimeException("当前无任何版本,无法回退");
		}
		String currentVersion = (String) versionList.get(0).get("VERSION");
		String lastVersion = (String) versionList.get(1).get("VERSION");
		
		//参数准备
	    FtpDto ftpDto = SessionUtil.getFtpParams();
		Trans trans = null;
		
		try{

			// 根据远程IP地址,用户名,密码创建命令执行对象
			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(),ftpDto.getUserName(),ftpDto.getPassword());
			// 拼接命令
			String command = "cd " + ftpDto.getFtpRootPath() + ";rm -rf "+Constant.CONF+" ;rm -rf "+Constant.ENV+" ;unzip -qo " + Constant.BACK + lastVersion + "/env_V"+lastVersion+".zip";
			String result = cmdUtil.execMsg(command);
			if(result.indexOf(Constant.ERROR) >= 0){
				log.error("解压失败--->"+result);
				throw new Exception(result);
			}
			
		}catch(Exception e){
			log.error("版本回退报错--->"+e);
			throw new Exception("版本回退错误："+e.getMessage());
		}finally{
			// 关闭资源
			if(trans != null){
				trans.close();
			}
		}
		
		// 版本回退后数据库删除最新版本
		params.put("VERSION", currentVersion);
		coreService.deleteObject("ftpFileUpload.deleteFileInfoByVersion", params, FrameConfigKey.DEFAULT_DATASOURCE);
	}
 
	/**
	 * 业务程序包入库
	 * @param trans
	 * @param ftpPath
	 * @param fileName
	 * @param formMap
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	private void insertServicePackage(Trans trans,String ftpPath,String fileName,Map formMap) throws Exception{
		List<Map<String,String>> insertParams = new ArrayList<>();
		
		List<HashMap<String,String>> versionList = coreService.queryForList("ftpFileUpload.queryFileInfo", formMap, FrameConfigKey.DEFAULT_DATASOURCE);
		if(BlankUtil.isBlank(versionList)){
			throw new RuntimeException("查询业务版本失败!");
		}
		
		String serviceId = versionList.get(0).get("ID");
		formMap.put("SERVER_ID", serviceId);
		// ocs_v0.0.1
		String fileNameWithoutPostfix = fileName.substring(0, fileName.lastIndexOf(StringTool.object2String(formMap.get("fileSuffixType"))));
		// _v0.0.1
		String versionPostfix = fileNameWithoutPostfix.substring(fileNameWithoutPostfix.toLowerCase().lastIndexOf("_v"));
		// 0.0.1
		String version = versionPostfix.substring(versionPostfix.toLowerCase().indexOf("_v") + 2);
		
		//包类型
		String packageType = StringTool.object2String(formMap.get("PACKAGE_TYPE"));
		String newFilePackagePath = ftpPath + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath(packageType) 
				+ Constant.RELEASE_DIR + fileNameWithoutPostfix + "/*";
		
		
		Vector<FileRecord> fileRecordList = trans.getFileList(newFilePackagePath);
		for(FileRecord fileRecord : fileRecordList){
			Map<String,String> insertMap = new HashMap<>();
			String tempName = fileRecord.getFileName();
			//集群类型
			String clusterType = tempName.substring(0,tempName.lastIndexOf(StringTool.object2String(formMap.get("fileSuffixType"))));
			// route_v0.0.1
			String taskCode = clusterType + versionPostfix;
			// route_v0.0.1.tar.gz
			String defineFileName = taskCode + StringTool.object2String(formMap.get("fileSuffixType"));
			insertMap.put("TASK_CODE", taskCode);
			insertMap.put("TASK_NAME", tempName);
			insertMap.put("DEFINE_FILE_NAME", defineFileName);
			insertMap.put("CLUSTER_TYPE", clusterType);
			insertMap.put("VERSION", version);
			insertMap.put("SERVER_ID", serviceId);
			
			List<HashMap<String, String>> codeList = coreService.queryForList("deployTask.queryTaskCodeByServiceId", insertMap, FrameConfigKey.DEFAULT_DATASOURCE);
		    //如果存在， 则不插入， 用原来 的记录
		    if(BlankUtil.isBlank(codeList)){
		    	insertParams.add(insertMap);
		    }else{
		    	coreService.updateObject("deployTask.updateDeployTaskFileName", insertMap, FrameConfigKey.DEFAULT_DATASOURCE);
		    }
		}
		// 数据库插入
		coreService.insertObject("deployTask.insertTask", insertParams, FrameConfigKey.DEFAULT_DATASOURCE);
	}

	/**
	 * 删除业务程序版本
	 * @param queryMap 业务参数
	 * @param dbKey 数据库Key
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void deleteBusinessPackage(Map<String, Object> queryMap, String dbKey) throws Exception {
		log.debug("删除业务程序包版本, 业务参数: " + queryMap.toString() + ", dbKey: " + dbKey);
		
		try {
			//判断业务程序是否有正在运行的实例
			Map<String, Object> resultMap = coreService.queryForObject2New("taskProgram.queryServiceForDelete", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
			int sum = ((Number)resultMap.get("SUM")).intValue();
			if(sum > 0){
				throw new RuntimeException("该版本有业务程序正在运行,无法删除!");
			}
			
			//获取部署主机信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			String command = "cd " + ftpDto.getFtpRootPath() + Constant.BUSS ;
			
			List<Map<String,String>> paramsList = (List<Map<String, String>>) queryMap.get("ids");
			List<Map<String,String>> idList = new ArrayList<>();
			for(int i = 0;i < paramsList.size(); i++){
				String fileName = paramsList.get(i).get("FILE_NAME");
				String fileNameNosuffix =  paramsList.get(i).get("NAME");
				String version = paramsList.get(i).get("VERSION");
				//包类型
				String packageType = paramsList.get(i).get("PACKAGE_TYPE");
				
				// 删除ftp目录文件夹下的压缩包及解压后的文件夹
				command  = command + packageType + ";rm -rf " + fileNameNosuffix +  "* " + Constant.RELEASE_DIR + fileNameNosuffix;
				log.debug("删除业务版本， 执行命令删除部署主机信息, 命令: " + command);
				String rstStr = cmdUtil.execMsg(command);
				log.debug("删除业务版本, 执行删除部署主机信息命令, 返回结果: " + rstStr);
				
				
				// 删除conf配置文件夹下的对应文件夹
				command = "cd "+ ftpDto.getFtpRootPath() + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + packageType +"; rm -rf " + fileNameNosuffix;
				log.debug("删除业务版本， 执行命令删除部署主机配置文件信息, 命令: " + command);
				rstStr = cmdUtil.execMsg(command);
				log.debug("删除业务版本， 执行命令删除部署主机配置文件信息, 返回结果: " + rstStr);
				
				//查询版本部署主机列表, 删除所有部署主机信息
				Map<String, Object> versionQueryMap = new HashMap<String, Object>();
				versionQueryMap.put("VERSION", version);
				versionQueryMap.put("PACKAGE_TYPE", packageType);
				List<HashMap<String,Object>> hostInfoList = coreService.queryForList2New("host.queryHostInfoForServiceDelete", versionQueryMap, FrameConfigKey.DEFAULT_DATASOURCE);
				
				// 循环删除个远程主机上的V1.0.2/
				for(HashMap<String,Object> tempMap : hostInfoList){
					String remoteIp = StringTool.object2String(tempMap.get("HOST_IP"));
					String remoteUserName = StringTool.object2String(tempMap.get("SSH_USER"));
					String remotePassword = DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD")));
					
					//集群部署路径
					String deployPath = StringTool.object2String(tempMap.get("CLUSTER_DEPLOY_PATH"));
					
					/**
					 * 删除远程主机上的文件
					 * 	1.根本版本号逐个查询到所有业务类下主机信息
					 *  2.根据版本号逐个删除个远程主机上对应的文件夹  V1.0.2/
					 */
					command = "cd " + FileTool.exactPath(deployPath) + Constant.BUSS + "; rm -rf V" + version ;
					ShellUtils remoteCmd = new ShellUtils(remoteIp, remoteUserName, remotePassword);
					log.debug("删除业务版本, 删除远程主机信息, 执行命令: " + command);
					rstStr = remoteCmd.execMsg(command);
					log.debug("删除业务版本, 删除远程主机信息, 执行命令返回结果: " + rstStr);
				}
				
				
				Map<String, String> tempMap = new HashMap<String, String>();
				tempMap.put("ID", paramsList.get(i).get("ID"));
				idList.add(tempMap);
				
				
				// 删除DCF_BUSSINESS_DEPLOY_LIST中的记录
				Map<String, String> delParams = new HashMap<>();
				delParams.put("VERSION", version);
				delParams.put("PACKAGE_TYPE", packageType);
				int delCount = coreService.deleteObject("businessDeployList.delBusDeployByVersion", delParams, dbKey);
				log.debug("删除业务版本部署主机信息成功, 删除记录数: " + delCount);
				
			}
			//删除版本包上传表数据
			int delCount = coreService.deleteObject("ftpFileUpload.deleteFileInfo", idList, dbKey);
			log.debug("删除版本包记录表数据成功, 删除记录行数: " + delCount);
			
			//删除dcf_ftp_cluster_config 表记录
			delCount = coreService.deleteObject("ftpFileUpload.deleteFileByServiceId", idList, dbKey);
			log.debug("删除部署任务表数据成功, 删除记录数: " + delCount);
			
			// 删除dcf_deploy_task中的记录
			delCount = coreService.deleteObject("deployTask.delTaskByServerId", idList, dbKey);
			log.debug("删除部署任务表数据成功, 删除记录数: " + delCount);
			
			//更新为未部署状态
			coreService.updateObject("deployTask.updateDeployState", idList, dbKey);
			
		} catch (Exception e) {
			log.error("删除业务版本程序包失败, 失败原因: ", e);
			throw new Exception("删除业务程序版本包失败， 失败信息: " + e.getMessage());
		}
	}
	
	
	/**
	 * 删除组件版本包信息
	 * @param queryMap 业务参数
	 * @param dbKey 数据Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> deletePlatformPackage(Map<String, String> queryMap, String dbKey) throws Exception {
		log.debug("删除组件程序包, 业务参数: " + queryMap.toString() + ", dbKey: " + dbKey);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String version = StringTool.object2String(queryMap.get("VERSION"));
		ShellUtils cmdUtil;
		
		try {
			//判断业务程序是否有正在运行的实例
			Map<String, Object> instMap = new HashMap<String, Object>();
			instMap.put("VERSION", version);
			Map<String, Object> resultMap = coreService.queryForObject2New("instConfig.queryInstConfigCount", instMap, FrameConfigKey.DEFAULT_DATASOURCE);
			int sum = ((Number)resultMap.get("RUN_COUNT")).intValue();
			if(sum > 0){
				throw new RuntimeException("该版本有程序正在运行,无法删除!");
			}
			FtpDto ftpDto = SessionUtil.getFtpParams();
			//查询部署主机信息
			if(BlankUtil.isBlank(ftpDto)){
				throw new RuntimeException("部署服务器查询失败，请检查配置！");
			}

			
			//1.删除部署主机信息
			cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			String command = "cd " + ftpDto.getFtpRootPath() + Constant.ENV + "; rm -rf "+version;
			log.debug("删除组件程序版本包信息, 删除部署主机信息, 执行命令: " + command);
			
			String rstStr = cmdUtil.execMsg(command);
			log.debug("删除组件程序版本包信息, 删除部署主机信息, 执行命令返回结果: " + rstStr);
			
			//2.删除已部署该版本主机下的env下版本信息
			//已部署主机
			List<HashMap<String, String>> deployHostList=coreService.queryForList("deployVersion.selectDeployHostByVersion", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
			List<String> successHostList = new ArrayList<String>();
			List<String> errorHostList = new ArrayList<String>();
			if(!BlankUtil.isBlank(deployHostList)){
				String remoteIp = null;
				for(int i=0;i<deployHostList.size();i++){
					try {
						Map<String,String> deployHost=deployHostList.get(i);
						remoteIp = deployHost.get("HOST_IP");
						String remoteUserName = deployHost.get("SSH_USER");
						String remotePassword = DesTool.dec(deployHost.get("SSH_PASSWD"));
						String deployPath = StringTool.object2String(deployHost.get("CLUSTER_DEPLOY_PATH"));
						String hostCmmond="cd "+ FileTool.exactPath(deployPath) + Constant.Tools+Constant.ENV + ";rm -rf "+version;
						ShellUtils remoteCmd = new ShellUtils(remoteIp, remoteUserName, remotePassword);
						log.debug("删除组件程序版本包信息, 删除远程主机信息, 执行命令: " + hostCmmond + ", 所在主机: " + remoteIp);
						
						rstStr = remoteCmd.execMsg(hostCmmond);
						log.debug("删除组件程序版本包信息, 删除远程主机信息, 执行命令返回结果: " + rstStr);
						
						successHostList.add(remoteIp);
					} catch (Exception e) {
						log.error("组件版本删除, 当前主机信息删除失败, 失败原因: ", e);
						errorHostList.add(remoteIp);
					}
				}
			}
			
			String msg = "";
			if (!BlankUtil.isBlank(successHostList)) {
				msg += "\n已删除【" + StringUtils.join(successHostList, ",") + "】主机相关文件!";
			}
			if (!BlankUtil.isBlank(errorHostList)) {
				msg += "\n删除【" + StringUtils.join(errorHostList, ",") + "】主机相关文件失败!";
			}
			
			//3.删除版本发布表数据
			int delCount = coreService.deleteObject("ftpFileUpload.deleteFileInfo", queryMap, dbKey);
			log.debug("删除组件程序版本包信息, 删除版本表【DCF_FTP_FILE_SERVER】记录成功， 删除记录数: " + delCount);
			
			//4.删除dcf_deploy_inst_config表数据
			delCount = coreService.deleteObject("instConfig.deleteConfigByVersion", queryMap, dbKey);
			log.debug("删除组件程序版本包信息, 删除组件实例表【DCF_DEPLOY_INST_CONFIG】记录成功， 删除记录数: " + delCount);
			
			//5.删除dcf_deploy_version_upgrade表数据库
			delCount = coreService.deleteObject("deployVersion.deleteDeployVersionByVersion", queryMap, dbKey);
			log.debug("删除组件程序版本包信息, 删除组件部署表【DCF_DEPLOY_VERSION_UPGRADE】记录成功， 删除记录数: " + delCount);
			
			//6.删除dcf_release_version_upgrade表数据
			delCount = coreService.deleteObject("releaseVersion.deleteReleaseVersion", queryMap, dbKey);
			log.debug("删除组件程序版本包信息, 删除组件版本子程序表【DCF_RELEASE_VERSION_UPGRADE】记录成功， 删除记录数: " + delCount);
			
			returnMap.put("message", "删除框架版本成功！"+msg);
		} catch (Exception e) {
			log.error("组件程序版本删除失败，失败原因: ", e);
			throw new Exception("组件程序版本删除失败， 失败原因: " + e.getMessage());
		}
		return returnMap;
	}
	
	/**
	 * 插入数据库
	 * @param formMap
	 * @param dbKey
	 * @throws Exception
	 */
	private void addFtpServer(Map formMap, String dbKey) throws Exception {
		log.debug("组件/业务版本上传， 添加版本数据库信息， 业务参数: " + formMap);
		String fileName = (String) formMap.get("FILE_NAME");
		String name = "";
		if(fileName.lastIndexOf(".zip") > -1){
			name = fileName.substring(0,fileName.lastIndexOf(".zip"));
		} else if(fileName.lastIndexOf(".tar.gz") > -1){
			name = fileName.substring(0,fileName.lastIndexOf(".tar.gz"));
		}
		formMap.put("NAME", name);
		
		List<HashMap<String, String>> versionList = coreService.queryForList("ftpFileUpload.queryFileVersionList", formMap, dbKey);
		if(!BlankUtil.isBlank(versionList)){
			HashMap<String, String> fileInfo = versionList.get(0);
			formMap.put("sameVersion","true");
			fileInfo.put("FILE_NAME", fileName);
			fileInfo.put("DESCRIPTION", StringTool.object2String(formMap.get("DESCRIPTION")));
			coreService.updateObject("ftpFileUpload.updateFileInfo", fileInfo, dbKey);
		}else{
			coreService.insertObject("ftpFileUpload.insertFileInfo", formMap, dbKey);
		}
	}
	
	@Override
	public List queryFileTree(Map<String, String> params) throws Exception {
		String fileName = params.get("NAME");
		// 获取素有参数信息
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		// 读取配置文件中的路径
		// public/bp/ftpfiles/ocs/ocs_v0.0.6/ocs_v0.0.6
		String path = ftpDto.getFtpRootPath() + Constant.BUSS + fileName + "/" + fileName;

		log.debug("服务器文件目录:" + path);

		files = new Vector<FileRecord>();
		Trans trans = null;
		// 登录文件服务器
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			if(trans.isExistPath(path)){// 如果存在该目录,则已经解压过
				
			}else{// 如果不存在,则需解压
				ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
				String cmd = "cd " + ftpDto.getFtpRootPath() + Constant.BUSS + fileName + "; tar -xvf *.tar.gz"
						+ fileName ;
				String resultStr = cmdUtil.execMsg(cmd);
				if (resultStr.indexOf("error") >= 0) {
					throw new Exception("文件解压失败," + resultStr);
				}
			}

			// 创建一个根目录
			FileRecord file = new FileRecord();
			String rootId = UUID.randomUUID().toString();
			file.setCurrId(rootId);
			file.setFileName(fileName);
			file.setFilePath(path);
			file.setParentId(null);
			file.setFileType('D');
			String isRoot = "false";
			// 遍历文件路径，查找节点
			getPath(trans, path, rootId, isRoot);
			// 打日志
			for (int i = 0; i < files.size(); i++) {
				log.debug("服务器上文件列表:" + files.get(i).getFileName());
			}
		} catch (Exception e) {
			log.error("获取服务器上所有文件列表失败-->" + e);
			throw new Exception(e.getMessage());
		} finally {
			if(trans != null){
				trans.close();
			}
		}
		return files;
	}

	/**
	 * 迭代查询给于路径下的所有文件详情
	 * 
	 * @param trans
	 * 链接类型
	 * @param subPath
	 * 当前目录
	 * @param parentId
	 * 父id
	 */
	private void getPath(Trans trans, String subPath, String parentId,
			String isRoot) throws Exception {
		try {
			Vector<FileRecord> subFile = null;
			// 获取子目录下的所有文件列表以及目录
			subFile = trans.getFileList(subPath);
			if (subFile != null && subFile.size() > 0) {
				for (int i = 0; i < subFile.size(); i++) {
					// 获取到每一个文件对象
					FileRecord file = subFile.get(i);
					// 给于文件对象一个随机id
					String currentId = UUID.randomUUID().toString();
					file.setCurrId(currentId);
					// 给于文件对象父节点的id
					file.setParentId(parentId);
					// 将对象添加到List当中
					files.add(subFile.get(i));
					if (!file.isFile()) {
						// 新目录（查询出的当前文件夹的路径）
						String newPath;
						if (isRoot == "true") {
							newPath = subPath + file.getFileName();
						} else {
							newPath = subPath + "/" + file.getFileName();
						}
						getPath(trans, newPath, currentId, "false");
					}
				}
			}
		} catch (Exception e) {
			log.error("获取" + subPath + "下的子目录失败-->" + e);
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 远程主机版本上传
	 * @param params 业务参数
	 * @return List 文件列表
	 */
	@Override
	public List<FileRecord> queryRemoteFiles(Map<String, String> params) throws Exception {
		log.debug("版本上传，获取远程主机文件列表， 参数：" + params);
		Map<String,String> remoteConfigMap = SessionUtil.getConfigByGroupCode("WEB_REMOTE_FILE_CFG");
		String ip = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_IP");
		String user = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_USER");
		String pwd = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PASSWD");
		String path = remoteConfigMap.get("WEB_CHOOSE_REMOTE_FILE_PATH");
		Trans trans = null;
		String ftp_type = SessionUtil.getConfigValue("FTP_TYPE");
		try {
			trans = FTPUtils.getFtpInstance(ip, user, pwd, ftp_type);
			trans.login();
			Vector<FileRecord> fileList  = trans.getFileList(path);
			log.info("远程主机文件列表： " + fileList + ", 远程主机目录： " + path);
			List<FileRecord> files = new ArrayList<FileRecord>();
			String flag = params.get("page_type");
		    for(FileRecord file :fileList){
		    	//1-组件类型    2-业务类型，业务类型支持zip或者tar.gz文件
		    	if(file.getFileName().lastIndexOf(".zip")> 0 && BusinessConstant.PARAMS_BUS_1.equals(flag)){
		    		files.add(file);
		    	} else if((file.getFileName().lastIndexOf(".tar.gz")> 0 || file.getFileName().lastIndexOf(".zip")> 0) 
		    			&& BusinessConstant.PARAMS_BUS_2.equals(flag)){
		    		files.add(file);
		    	}
		    }

		    //按照文件名称升序排序
			if (CollectionUtils.isNotEmpty(files)) {
				Collections.sort(files, new Comparator<FileRecord>() {
					@Override
					public int compare(FileRecord record1, FileRecord record2) {
						FileRecord e1 = (FileRecord) record1;
						FileRecord e2 = (FileRecord) record2;
						return e1.getFileName().compareTo(e2.getFileName());
					}
				});
			}

		    log.info("远程主机版本列表:" + files);
		    return files;
		} catch (Exception e) {
			log.error("获取服务器上所有文件列表失败-->", e);
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {

		String aa = "^([\\s\\S]*)[_,-][v,V]\\d{1,2}\\.\\d\\.\\d.\\d([\\s\\S]*)$";
		Pattern pattern = Pattern.compile(aa);
		Matcher matcher = pattern.matcher("a_V15.1.1dd.3");
		// 字符串是否与正则表达式相匹配
		boolean rs = matcher.matches();
		System.out.println(rs);
	}
}
