package com.tydic.service.clustermanager.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.DeployService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.clustermanager.impl]    
  * @ClassName:    [DeployServiceImpl]     
  * @Description:  [组件部署实现操作类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:17:24]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:17:24]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
@SuppressWarnings("all")
public class DeployServiceImpl implements DeployService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DeployServiceImpl.class);
	
	/**
	 * 远程文件列表
	 */
	Vector<FileRecord> files = null;
	
	//JDK目录
	private final String JDK = "jdk";
	
	/**
	 * 核心Service
	 */
	@Resource
	public CoreService coreService;

	/**
	 * 组件部署
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return String
	 */
	@Override
	public String updateDeployHost(Map<String, Object> param, String dbKey) throws Exception {
		log.info("组件部署， 参数:" + param.toString() + ", dbKey: " + dbKey);
		// 获取从数据库中配置好的本地文件跟目录
		String rootPath = StringTool.object2String(param.get("rootPath"));
		//临时目录
		String tmpPath = rootPath + Constant.TMP + System.currentTimeMillis();
		// 拼接字符串准备
		StringBuffer resultStrBuf = new StringBuffer();
		param.put("GROUP_CODE", "WEB_FTP_CONFIG");
		// 获取 文件ftp服务 器
		List ftpList = coreService.queryForList2New("config.queryConfigList", param, dbKey);
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String ftpPath = ftpDto.getFtpRootPath();
		Trans src = null;
		Trans remote = null;

		Map<String, Object> queryParam = new HashMap<>();

		try {
			if (!BlankUtil.isBlank(ftpDto)) {
				src = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(),ftpDto.getFtpType());
				src.login();
			} else {
				throw new RuntimeException("部署主机信息为空， 请检查部署主机是否已经配置！");
			}
			
			//需要部署的主机列表，一次部署一台，list参数大小始终为1
			List<Map<String, String>> list = (List) param.get("paramList");
			int flag = 0;
			if (!BlankUtil.isBlank(list)) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, String> tmpMap = list.get(i);
					//获取部署集群路径
					String clusterId = StringTool.object2String(tmpMap.get("CLUSTER_ID"));
					//集群类型
					String clusterType = StringTool.object2String(tmpMap.get("CLUSTER_TYPE"));
					//集群类型
					String clusterCode = StringTool.object2String(tmpMap.get("CLUSTER_CODE"));
					//部署版本
					String version = StringTool.object2String(tmpMap.get("VERSION"));
					//主机ID
					String hostId = StringTool.object2String(tmpMap.get("HOST_ID"));
					//部署主机ID
					String id = StringTool.object2String(tmpMap.get("ID"));

					//根据主机ID，查询主机信息
					Map<String, Object> queryHostMap = new HashMap<String, Object>();
					queryHostMap.put("HOST_ID", hostId);
					Map hostMap = coreService.queryForObject2New("host.queryHostById", queryHostMap, dbKey);
					if (BlankUtil.isBlank(hostMap) || hostMap.isEmpty()) {
						throw new RuntimeException("主机信息查询失败,请检查！");
					}
					
					//根据集群ID查询集群信息
					Map<String, Object> queryClusterMap = new HashMap<String, Object>();
					queryClusterMap.put("CLUSTER_ID", clusterId);
					queryClusterMap.put("CLUSTER_TYPE", clusterType);
					Map clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
					if (BlankUtil.isBlank(clusterMap) || clusterMap.isEmpty() || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
						throw new RuntimeException("集群信息查询失败, 请检查！");
					}
					//集群部署路径
					String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
					//集群名称
					String clusterName = StringTool.object2String(clusterMap.get("CLUSTER_NAME"));
					
					try {
						//获取部署主机信息
						String remoteIp = StringTool.object2String(hostMap.get("HOST_IP"));
						String remoteUserName = StringTool.object2String(hostMap.get("SSH_USER"));
						String remotePasswd = StringTool.object2String(hostMap.get("SSH_PASSWD"));
						if (!BlankUtil.isBlank(remotePasswd)) {
							remotePasswd = DesTool.dec(remotePasswd);
						} else {
							throw new RuntimeException(remoteIp + "主机密码为空, 请检查！");
						}
						tmpMap.put("HOST_IP", remoteIp);
						tmpMap.put("SSH_USER", remoteUserName);
						tmpMap.put("SSH_PASSWD", remotePasswd);
						log.debug("部署远程主机信息:【IP:" + remoteIp + ", userName: " + remoteUserName + ", 连接方式:" + SessionUtil.getConfigValue("FTP_TYPE") + "】");
						remote = FTPUtils.getFtpInstance(remoteIp, remoteUserName, remotePasswd, SessionUtil.getConfigValue("FTP_TYPE"));
						remote.login();
						log.debug("登陆部署主机成功，主机信息 :【IP:" + remoteIp + ", 登录用户名：" + remoteUserName + "】");
						
						//获取部署主机deploy.sh脚本路径
						String srcFile = ftpPath + Constant.DEPLOY_SH_FILE_NAME;
						log.debug("文件下载目录：" + srcFile);
						
						//获取部署主机路径
						String remoteFile = FileTool.exactPath(appRootPath) + Constant.Tools + Constant.DEPLOY_SH_FILE_NAME;
						log.debug("文件上传目录：" + remoteFile);
						
						//本地临时目录文件
						String localDeployFilePath = tmpPath + "/deploy.sh";
						src.get(srcFile, localDeployFilePath);
						log.debug("部署主机拉取到本地成功， 本地文件路径: " + localDeployFilePath);
						
						remote.put(localDeployFilePath, remoteFile);
						log.debug("上传deploy.sh脚本成功， 远程主机路径: " + remoteFile);
						
						// 获取配置中的命令
						String shellCommand = Constant.DEPLOY_SH;
						
						String targetPath = StringTool.object2String(clusterMap.get("TARGET_SH_PATH"));
						if (!BlankUtil.isBlank(targetPath)) {
							targetPath += FileTool.exactPath(version);
						} else {
							targetPath = FileTool.exactPath(version);
						}
						
						//通过代码方式直接部署程序
						/////////////////////////////////////////通过代码拉取程序包 START////////////////////////////////////////////////////
						//推送auto.sh文件
						ShellUtils cmdUtil = new ShellUtils(remoteIp, remoteUserName, remotePasswd);
						resultStrBuf.append("get [auto.sh] -------------------- Start\n");
						String sourceFilePath = ftpPath + "auto.sh";
						String targetFilePath = appRootPath + Constant.Tools + "auto.sh";
						InputStream autoFileStream = src.get(sourceFilePath);
						remote.put(autoFileStream, targetFilePath);
						autoFileStream.close();
						src.completePendingCommand();
						
						String command = "cd " + (appRootPath + Constant.Tools) + ";chmod a+x auto.sh";
						String executeRst = cmdUtil.execMsg(command);
						resultStrBuf.append("get [auto.sh] -------------------- Success\n");
						log.debug("部署时拉取脚本文件成功...");
						
						//判断组件是否需要安装JDK，如果组件需要安装JDK检查环境变量中是否已经存在，如果不存在则推送JDK版本包
						String installJDK = StringTool.object2String(clusterMap.get("INSTALL_JDK"));
						String isNeedJDK = BusinessConstant.PARAMS_BUS_0;
						if (!BlankUtil.isBlank(installJDK) && BusinessConstant.PARAMS_BUS_1.equals(installJDK)) {
							//判断JDK是否已经安装，如果安装就不需要在推送JDK
							String checkJdkCmd = "cat ~/.bash_profile | grep JAVA_HOME=";
							String jdkRst = cmdUtil.execMsg(checkJdkCmd);
							log.debug("版本部署， 检查远程主机JDK是否安装，命令直接结果： " + jdkRst);
							if (BlankUtil.isBlank(jdkRst)) {
								//判断JDK目录是否存在，如果不存在在创建目录
								String jdkTargetPath = appRootPath + Constant.Tools + StringTool.object2String(clusterMap.get("TARGET_SH_PATH"));
								boolean isExist = remote.isExistPath(jdkTargetPath);
								if (!isExist) {
									command = "mkdir -p " + jdkTargetPath;
									executeRst = cmdUtil.execMsg(command);
									resultStrBuf.append("mkdir ["+jdkTargetPath+"] -------------------- Success\n");
								}
								log.debug("部署"+clusterType+", 生成JDK目标目录成功， 目标目录 ---> " + jdkTargetPath);
								
								//判断JDK是否存在，如果存在则不需要再上传
								Boolean isExists = false;
								Vector<FileRecord> fileList = remote.getFileList(jdkTargetPath);
								for (int j=0; j<fileList.size(); j++) {
									if (JDK.equalsIgnoreCase(fileList.get(j).getFileName()) 
											&& String.valueOf(FileRecord.DIR).equalsIgnoreCase(String.valueOf(fileList.get(j).getFileType()))) {
										isExists = true;
										break;
									}
								}
								//JDK安装文件不存在，则下载
								try {
									if (!isExists) {
										resultStrBuf.append("get [JDK] -------------------- Start\n");
										//上传文件
										String jdkSourcePath = String.valueOf(clusterMap.get("SOURCE_SH_FILE"));
										if (!BlankUtil.isBlank(jdkSourcePath) && jdkSourcePath.indexOf("/") != -1) {
											jdkSourcePath = jdkSourcePath.substring(0, jdkSourcePath.lastIndexOf("/"));
										}
										sourceFilePath = ftpPath + FileTool.exactPath(jdkSourcePath) + "jdk.zip";
										targetFilePath = FileTool.exactPath(jdkTargetPath) + "jdk.zip";
										InputStream inputStream = src.get(sourceFilePath);
										remote.put(inputStream, targetFilePath);
										inputStream.close();
										src.completePendingCommand();
										resultStrBuf.append("get [JDK] -------------------- Success\n");
									}
									isNeedJDK = BusinessConstant.PARAMS_BUS_1;
									log.debug("部署时拉取JDK程序包成功...");
								} catch (Exception e) {
									log.error("JDK压缩包不存在，上传失败，请手动安装JDK");
									log.error("", e);
								}
							}
						}
						
						//创建远程目录
						resultStrBuf.append("get ["+clusterType+"] -------------------- Start\n");
						//创建目录(版本包放置的目录)
						String targetRootPath = appRootPath + Constant.Tools + targetPath;
						boolean isExist = remote.isExistPath(targetRootPath);
						if (!isExist) {
							command = "mkdir -p " + targetRootPath;
							executeRst = cmdUtil.execMsg(command);
							resultStrBuf.append("create ["+targetRootPath+"] -------------------- Success\n");
						}
						
						//创建zookeeper的myid放置的目录
						if (Constant.ZOOKEEPER.equals(clusterType)) {


						//创建myid ，在启动那里实现
						/*	String serverId = remoteIp.substring(remoteIp.lastIndexOf(".") + 1,remoteIp.length());
							String myidPath = appRootPath + Constant.Tools +Constant.ENV + FileTool.exactPath(version) + Constant.VERSION_ZOOKEEPER_DATA_DIR;

							String myidCommand ="mkdir -p " +myidPath +";echo " + serverId + " > " + myidPath + "/myid";;
							executeRst = cmdUtil.execMsg(myidCommand);
							resultStrBuf.append("create [myid] folder-------------------- Success\n");*/
						}
						
						//组件包名称
						String fileName = clusterType + ".zip";
						
						//上传文件
						sourceFilePath = ftpPath + FileTool.exactPath(Constant.ENV) + FileTool.exactPath(version) + fileName;
						log.debug("组件部署, 源组件包路径: " + sourceFilePath);
						
						targetFilePath = appRootPath + Constant.Tools + targetPath + fileName;
						log.debug("组件部署, 远程主机部署目录: " + targetFilePath);
						
						InputStream inputStream = src.get(sourceFilePath);
						remote.put(inputStream, targetFilePath);
						//在FTP中远程文件通过流返回时，在关闭流后必须执行completePendingCommand方法才能不影响下次请求
						inputStream.close();
						src.completePendingCommand();
						
						resultStrBuf.append("get ["+clusterType+"] -------------------- Success\n");
						log.debug("部署时拉取" + clusterType + "程序包成功 ...");
						/////////////////////////////////////////通过代码拉取程序包 END////////////////////////////////////////////////////
						
						//调用部署脚本
						String deployRootPath = FileTool.exactPath(appRootPath) + Constant.Tools;
						String deployParams = clusterType + " " +  targetFilePath + " " + isNeedJDK + " " + Constant.ENV;
						shellCommand = MessageFormat.format(shellCommand, deployRootPath, deployParams);
						//cd /public/bp/singDeploy/ZK/tools/; chmod a+x deploy.sh;./deploy.sh zookeeper /public/bp/singDeploy/ZK/tools/env/1.0.0/zookeeper.zip
						// 根据远程IP地址,用户名,密码创建命令执行对象
						cmdUtil = new ShellUtils(remoteIp, remoteUserName, remotePasswd);
						String resultStr = cmdUtil.execMsg(shellCommand);
						log.info("调用部署脚本，执行结果: " + resultStr);
						resultStrBuf.append(resultStr);
						if (resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0
								|| resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
							log.error("执行脚本失败--->" + resultStr);
							throw new Exception(resultStr);
						}
						
						//判断当前集群配置文件是否存在，如果不存在则重模板文件中复制 (版本发布服务器集群配置文件创建)
						String cfgBasePath = ftpPath + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR;
						String clusterCfgPath =  cfgBasePath + FileTool.exactPath(clusterType) + clusterCode;
						boolean isExistPath = src.isExistPath(clusterCfgPath);
						if (!isExistPath) {
							//创建当前集群配置文件目录
							List<FileRecord> templetList = new ArrayList<FileRecord>();
							String templetPath = cfgBasePath + FileTool.exactPath(clusterType) + Constant.CLUSTER_DEFAULT;
							getAllFileList(src, templetList, templetPath, true);
							if (!BlankUtil.isBlank(templetList)) {
								for (FileRecord file : templetList) {
									if (String.valueOf(FileRecord.FILE).equals(String.valueOf(file.getFileType()))) {
										//当前集群配置文件目录
										String clusterCfgRealPath = FileTool.exactPath(clusterCfgPath) + FileTool.exactPath(file.getFilePath().replace(templetPath, "")) + file.getFileName();
										//本地临时目录
										String localPath = FileTool.exactPath(tmpPath) + file.getFileName();
										//将远程模板文件下载到本地临时目录
										src.get(FileTool.exactPath(file.getFilePath()) + file.getFileName(), localPath);
										//将临时目录文件上传到部署主机配置文件目录
										src.put(localPath, clusterCfgRealPath);
									}
								}
							}
							log.debug("集群模板配置文件创建成功...");
						}

						try {
							//分发组件配置文件,将部署主机集群下的配置文件分发到对应主机,只分发对应集群配置文件(远程主机配置文件推送)
							List<FileRecord> files = new ArrayList<FileRecord>();
							getAllFileList(src, files, clusterCfgPath, true);
							log.debug("获取配置文件 --->" + (files == null ? 0 : files.size()));
							// 将文件服务器上对应分类下的配置文件发送到远程主机对应目录中
							String remoteFilePath = FileTool.exactPath(appRootPath) + Constant.Tools + Constant.CONF + clusterType;
							for (FileRecord file : files) {
								if (String.valueOf(FileRecord.FILE).equals(String.valueOf(file.getFileType()))) {
									//获取部署主机配置文件目录
									String hostPath = FileTool.exactPath(remoteFilePath) + FileTool.exactPath(file.getFilePath().replace(clusterCfgPath, ""))+ file.getFileName();
									//本地临时保存配置文件目录
									String localPath = FileTool.exactPath(tmpPath) + file.getFileName();
									//将部署主机配置文件下载到本地
									src.get(FileTool.exactPath(file.getFilePath()) + file.getFileName(),localPath);
									//将本地配置文件上传到远程对应主机
									remote.put(localPath, hostPath);
								}
							}
						} catch (Exception e) {
							log.error("动态传送" + clusterType + "配置文件失败--->", e);
							resultStrBuf.append("动态传送【" + clusterType + "】配置文件失败,error.").append("\n");
							return resultStrBuf.toString();
						} 
						
						//添加版本发布数据表
						Map<String, String> paramsMap = new HashMap<>();
						paramsMap.put("CLUSTER_ID", clusterId);
						paramsMap.put("CLUSTER_TYPE", clusterType);
						paramsMap.put("VERSION", version);
						paramsMap.put("HOST_ID", hostId);
						paramsMap.put("STATE", Constant.STATE_ACTIVE);
						//判断当前版本是否部署过，如果当前主机部署过当前类型版本，则修改数据，否则添加数据
						List<HashMap<String, String>> versionList = coreService.queryForList("deployVersion.queryDeployVersionByUniqueCondition", paramsMap, dbKey);
						if (!BlankUtil.isBlank(versionList) && !versionList.isEmpty()) {
							coreService.updateObject("deployVersion.updateDeployVersion", paramsMap, dbKey);
						} else {
							coreService.insertObject("deployVersion.addDeployVersion", paramsMap, dbKey);
						}
						
						//修改部署状态
						Map<String, String> updateMap = new HashMap<String, String>();
						updateMap.put("ID", id);
						updateMap.put("VERSION", version);
						coreService.updateObject("deployHome.updateHostStateEffectiveById",updateMap, dbKey);
						resultStrBuf.append("主机【" + (hostMap.get("HOST_IP") + "(" + hostMap.get("SSH_USER") + ")】部署 -------- Success\n\n" ));
					} catch (Exception e) {
						log.error("部署" + hostMap.get("HOST_IP")+ "【"+clusterType+"】失败， 失败原因:", e);
						throw new Exception(hostMap.get("HOST_IP") + ":" + e.getMessage()+"\n");
					} finally {
						if (remote != null) {
							remote.close();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("部署失败, 失败原因:", e);
			throw new Exception("error:\n"+e.getMessage() + "\n");
		} finally {
			if (src != null) {
				src.close();
			}
			try{
				FileUtil.deleteFile(tmpPath);
			}catch(Exception e){
				log.error("删除本地临时文件失败， 失败原因:", e);
			}
		}
		log.info("返回json:" + resultStrBuf.toString());
		return resultStrBuf.toString();
	}
	
	/**
	 * 递归获取所有的文件
	 * @param path
	 * @param isNext
	 * @return
	 */
	private void getAllFileList(Trans trans, List<FileRecord> fileList, String path, boolean isNext) throws Exception {
		try {
			List<FileRecord> firstList = trans.getFileList(path);
			for (int i=0; i<firstList.size(); i++) {
				fileList.add(firstList.get(i));
				if (String.valueOf(FileRecord.DIR).equals(String.valueOf(firstList.get(i).getFileType()))) {
					String fileName = firstList.get(i).getFileName();
					String filePath = FileTool.exactPath(firstList.get(i).getFilePath()) + fileName;
					getAllFileList(trans, fileList, filePath, isNext);
				}
			}
		} catch (Exception e) {
			log.error("获取配置文件失败 ---> ", e);
			throw e;
		}
	}

	/**
	 * 删除组件主机以及远程目录
	 * 
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return String
	 */
	@Override
	public String deteleHostAndPath(Map<String, String> param, String dbKey) throws Exception {
		log.debug("删除主机, 参数: " + param.toString() + ", dbKey: " + dbKey);
		//集群ID
		String clusterId = StringTool.object2String(param.get("CLUSTER_ID"));
		//集群编码
		String clusterCode = StringTool.object2String(param.get("CLUSTER_CODE"));
		//集群类型
		String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		//主机ID
		String hostId = StringTool.object2String(param.get("HOST_ID"));
		String returnResult = "false";
		try {
			// 删除主机
			Map<String, String> delParams = new HashMap<String, String>();
			delParams.put("ID", StringTool.object2String(param.get("ID")));
			int delCount = coreService.deleteObject("deployHome.deleteHostInDeploy", delParams, dbKey);
			log.debug("删除部署表信息成功, 删除记录: " + delCount);
			
			//删除部署记录表数据
			Map<String, String> deployMaps = new HashMap<String, String>();
			deployMaps.put("CLUSTER_ID", clusterId);
			deployMaps.put("CLUSTER_TYPE", clusterType);
			deployMaps.put("HOST_ID", hostId);
			int delCnt = coreService.deleteObject("deployVersion.delDeployListByHost", deployMaps, dbKey);
			log.debug("删除组件部署记录表数据成功， 删除记录: " + delCnt);
			
			returnResult = "true";
		} catch (Exception e) {
			log.error("删除主机失败, 失败原因:", e);
			throw new Exception("删除主机以及远程目录失败-->" + e.getMessage());
		}
		return returnResult;

	}

	@Override
	public List queryFileTree(Map<String, String> params) throws Exception {
		// 查询map
		// 获取 文件ftp服务 器
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String filename = params.get("filename");
		// 读取配置文件中的路径
		String path = ftpDto.getFtpRootPath() + Constant.BUSS + filename + "/";

		log.debug("服务器文件目录:" + path);

		files = new Vector<FileRecord>();
		Trans trans = null;
		// 登录文件服务器
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			String cmd = "cd " + ftpDto.getFtpRootPath() + Constant.BUSS + "; unzip -qo "
					+ filename + ".zip";
			String resultStr = cmdUtil.execMsg(cmd);
			if (resultStr.indexOf("error") >= 0) {
				throw new Exception("文件解压失败," + resultStr);
			}

			// 创建一个根目录
			FileRecord file = new FileRecord();
			String rootId = UUID.randomUUID().toString();
			file.setCurrId(rootId);
			file.setFileName(filename);
			file.setFilePath(ftpDto.getFtpRootPath() + Constant.BUSS + filename);
			file.setParentId(null);
			file.setFileType('D');
			files.add(file);
			String isRoot = "true";
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
			trans.close();
		}
		return files;
	}

	/**
	 * 迭代查询给于路径下的所有文件详情
	 * 
	 * @param ftp_type
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
	 * 集群划分--添加业务主机划分
	 * @param paramsMap
	 * @param dbKey
	 */
	@Override
	public String insertBusiness(Map<String, Object> paramsMap, String dbKey) throws Exception {
		log.debug("添加业务主机划分, 参数：" + paramsMap.toString() + ", dbKey: " + dbKey);
		
		//获取当前业务集群ID
		String clusterId = StringTool.object2String(paramsMap.get("CLUSTER_ID"));
		//获取业务集群关联组件集群ID
		String refClusterId = StringTool.object2String(paramsMap.get("REF_CLUSTER_ID"));
		//获取业务集群关联组件集群类型
		String refClusterType = StringTool.object2String(paramsMap.get("REF_CLUSTER_TYPE"));
		//获取集群主机列表
		List<Map<String, String>> hostList = (List<Map<String, String>>) paramsMap.get("HOST_LIST");
		//添加主机集群
		coreService.insertObject("deployHome.insertChosenHost", hostList, dbKey);
		return null;
	}

	/**
	 * 批量删除主机划分
	 */
	@Override
	public String delHostBatchPartition(Map<String, Object> paramsList, String dbKey) throws Exception {
		log.debug("批量删除集群划分主机, 参数:" + paramsList.toString() + ", dbKey:" + dbKey);
		
		//要删除的主机列表
		List<Map<String, Object>> hostList = (List<Map<String, Object>>) paramsList.get("HOST_LIST");
		if (!BlankUtil.isBlank(hostList)) {
			for (int i=0; i<hostList.size(); i++) {
				Map<String, Object> paramsMap = hostList.get(i);
				//集群ID
				String clusterId = StringTool.object2String(paramsMap.get("CLUSTER_ID"));
				//集群类型
				String clusterType = StringTool.object2String(paramsMap.get("CLUSTER_TYPE"));
				//主机ID
				String hostId = StringTool.object2String(paramsMap.get("HOST_ID"));
				//主机IP
				String hostIp = StringTool.object2String(paramsMap.get("HOST_IP"));
				try {
					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put("HOST_ID", hostId);
					queryMap.put("CLUSTER_ID", clusterId);
					queryMap.put("CLUSTER_TYPE", clusterType);
					List<HashMap<String, String>> programeList = coreService.queryForList("taskProgram.queryProgramRunForClusterDeleted", queryMap, dbKey);
					if (!BlankUtil.isBlank(programeList)) {
						throw new RuntimeException("该集群下【" + hostIp + "】主机有程序正在运行,无法移除");
					}
	
					// 删除dcf_host_deploy表中的记录
					int delDeployCnt = coreService.deleteObject("deployHome.deleteDeployHostByHostId", queryMap, dbKey);
					log.debug("删除业务部署表数据，当前主机所有部署版本记录都会删掉， 删除记录: " + delDeployCnt);
					
					// 删除dcf_task_program表中关联程序的记录
					int delInstCnt = coreService.deleteObject("taskProgram.delProgramAboutCluster", queryMap, dbKey);
					log.debug("删除业务实例表数据， 当前主机所有版本数据都会删除， 删除记录: " + delInstCnt);
					
					//删除部署记录表数据
					int delBusCnt = coreService.deleteObject("businessDeployList.delBusDeployListByHost", queryMap, dbKey);
					log.debug("删除业务部署记录表数据成功， 删除记录: " + delBusCnt);
				} catch (Exception e) {
					log.error("业务主机划分删除失败, 失败原因:", e);
					throw new Exception("移除划分主机失败，" + e.getMessage());
				}
			}
		}
		return "";
	}
	
	/**
	 * 集群划分--添加supervisor主机
	 */
	@Override
	public String insertSupervisor(Map<String, Object> paramsMap, String dbKey) throws Exception {
		log.debug("添加Jstorm集群主机, 参数：" + paramsMap.toString() + ", dbKey: " + dbKey);
		//获取当前集群ID
		String clusterId = StringTool.object2String(paramsMap.get("CLUSTER_ID"));
		//获取集群主机列表
		List<Map<String, String>> hostList = (List<Map<String, String>>) paramsMap.get("HOST_LIST");
		//添加主机集群
		coreService.insertObject("deployHome.insertChosenHost", hostList, dbKey);
		return null;
	}


	public static void main(String[] args) throws IOException {

		// FileUtils.forceMkdir(new File("d:/bp/dccp"));
		double a = 8000 * 3 * (0.075 - 0.003) / 12;
		System.out.println(a);
	}

	/**
	 * 集群划分--删除业务类主机
	 */
	@Override
	public String deleteServiceHost(Map paramsMap, String dbKey) throws Exception {
		log.debug("删除业务类主机, 参数:" + paramsMap.toString() + ", dbKey:" + dbKey);
		//集群ID
		String clusterId = StringTool.object2String(paramsMap.get("CLUSTER_ID"));
		//集群类型
		String clusterType = StringTool.object2String(paramsMap.get("CLUSTER_TYPE"));
		//主机ID
		String hostId = StringTool.object2String(paramsMap.get("HOST_ID"));
		//部署ID
		String id = StringTool.object2String(paramsMap.get("ID"));
		try {
			Map<String, String> queryMap = new HashMap<String, String>();
			queryMap.put("HOST_ID", hostId);
			queryMap.put("CLUSTER_ID", clusterId);
			queryMap.put("CLUSTER_TYPE", clusterType);
			List<HashMap<String, String>> programeList = coreService.queryForList("taskProgram.queryProgramRunForClusterDeleted", queryMap, dbKey);
			if (!BlankUtil.isBlank(programeList)) {
				throw new RuntimeException("该集群下当前主机有程序正在运行,无法删除");
			}

			// 删除dcf_host_deploy表中的记录
			queryMap.put("ID", id);
			int delDeployCnt = coreService.deleteObject("deployHome.deleteHostInDeploy", queryMap, dbKey);
			log.debug("删除业务部署表数据，当前主机所有部署版本记录都会删掉， 删除记录: " + delDeployCnt);
			
			// 删除dcf_task_program表中关联程序的记录
			int delInstCnt = coreService.deleteObject("taskProgram.delProgramAboutCluster", queryMap, dbKey);
			log.debug("删除业务实例表数据， 当前主机所有版本数据都会删除， 删除记录: " + delInstCnt);
			
			//删除部署记录表数据
			int delBusCnt = coreService.deleteObject("businessDeployList.delBusDeployListByHost", queryMap, dbKey);
			log.debug("删除业务部署记录表数据成功， 删除记录: " + delBusCnt);
		} catch (Exception e) {
			log.error("业务主机划分删除失败, 失败原因:", e);
			throw new Exception("删除业务类主机失败" + e.getMessage());
		}
		return null;
	}
}
