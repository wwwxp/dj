package com.tydic.service.configure.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bean.RecursiveFile;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.common.ComParamsHelper;
import com.tydic.service.configure.ConfigureManagerService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.Collator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [ConfigureManagerServiceImpl]     
  * @Description:  [组件、业务配置文件管理实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:08:25]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:08:25]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class ConfigureManagerServiceImpl implements ConfigureManagerService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ConfigureManagerServiceImpl.class);
	@Resource
	private CoreService coreService;
	//配置文件修改默认字符集编码
	private static final String DEFAULT_ENCODING = "UTF-8";

	
	/**
	 * realse树加载
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List loadFileTree(Map<String, String> params, String dbKey) throws Exception {
		log.debug("获取部署主机配置文件目录， 参数: " + params.toString() + ", dbKey: " + dbKey);
		//获取ftp主机信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		//配置文件类型(业务类型:SERVICE 组件类型:PLATFORM)
		String page_type = params.get("page_type");
		// 读取配置文件中的路径
		String path = "";
		// 判断前台传过来的是 “平台配置” 还是 “ 业务配置”
		if (page_type.equalsIgnoreCase("PLATFORM")) {
			//${rootPath}/conf/platform_config/release/
			path = FileTool.exactPath(ftpDto.getFtpRootPath()) + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR;
		} else if (page_type.equalsIgnoreCase("SERVICE")) {
			String packageType = StringTool.object2String(params.get("bus_package_type"));
			path = FileTool.exactPath(ftpDto.getFtpRootPath()) + Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + packageType;
		}
		log.debug("部署主机配置文件目录:" + path);

		List<FileRecord> allFilesList = new ArrayList<FileRecord>();
		Trans trans = null;
		ShellUtils cmdUtil=null;
		// 协议判断
		try {
			//登录部署主机
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			
			//组件配置
			if(page_type.equalsIgnoreCase("PLATFORM")) {
				// 创建一个根目录
				FileRecord rootFile = new FileRecord();
				String rootId = UUID.randomUUID().toString().replaceAll("-", "");
				rootFile.setCurrId(rootId);
				rootFile.setFileName(Constant.RELEASE);
				//rootFile.setFilePath(rootPath + Constant.PLAT_CONF);
				rootFile.setFilePath(path);
				rootFile.setParentId(Constant.ROOT_NODE_FLAG);
				rootFile.setFileType('D');
				rootFile.setFileLevel("R");
				allFilesList.add(rootFile);
				
				//没有配置文件的集群进行模版复制初始化
				Map<String,Map<String,String>> componentsPath = new HashMap<String,Map<String,String>>();
				Map<String, Map<String, String>> clusterDeployPath = new HashMap<String, Map<String, String>>();
				cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
				
				//创建所有的集群，这个逻辑修改为在部署的时候创建没有的集群
				Map<String, String> queryMap = new HashMap<String, String>();
				queryMap.put("TYPE", "1");
				List<HashMap<String, String>> clusters = coreService.queryForList("serviceType.queryAllDeploy", queryMap, dbKey);
//				List<HashMap<String, String>> clusters = new ArrayList<HashMap<String, String>>();
				for(HashMap<String, String> cluster :clusters){
					 clusterDeployPath.put(cluster.get("CLUSTER_CODE"), cluster);
//					 String template = path+"/"+cluster.get("CLUSTER_TYPE")+"/"+Constant.CLUSTER_DEFAULT;
					 String clusterPath = path+"/"+cluster.get("CLUSTER_TYPE")+"/"+cluster.get("CLUSTER_CODE");
					 clusterPath = clusterPath.replaceAll("/+", "/");
					 Map<String,String> map = componentsPath.get(cluster.get("CLUSTER_TYPE"));
					 if(map == null){
						 map = new HashMap<String,String>();
						 componentsPath.put(cluster.get("CLUSTER_TYPE"),map);
					 }
					 

					 map.put(cluster.get("CLUSTER_CODE"),clusterPath);
				 }
				 
				//格式化树节点
				//框架类判断节点是否被启动
				List<HashMap<String,String>> runCconfig = coreService.queryForList("instConfig.queryInstConfigList", null, dbKey);
				Map<String, String> instConfigMap = new HashMap<String, String>();
				for(HashMap<String,String> item : runCconfig){
					String clusterCode = StringTool.object2String(item.get("CLUSTER_CODE"));
					//String appRootPathFinal = clusterDeployPath.get(clusterCode).get("CLUSTER_DEPLOY_PATH")+Constant.Tools+Constant.CONF;
					String appRootPathFinal = item.get("CLUSTER_DEPLOY_PATH") + Constant.Tools + Constant.CONF;
					instConfigMap.put(clusterCode, item.get("FILE_PATH").replace(appRootPathFinal, ""));
				}
					
			     RecursiveFile recursiveFiles = new RecursiveFile(allFilesList);
			     recursiveFiles.setUsedMarks(instConfigMap);
//			     recursiveFiles.setClusterMap(clusterDeployPath);
				 Vector<FileRecord> componentDirectorys = trans.getFileList(path);
				 if (!BlankUtil.isBlank(componentDirectorys)) {
					 //历遍二级节点，即组件类型标识
					 for(FileRecord componentDirectory : componentDirectorys){
							// 给于文件对象一个随机id
							String componentCurrentId = UUID.randomUUID().toString();
							componentDirectory.setCurrId(componentCurrentId);
							// 给于文件对象父节点的id
							componentDirectory.setParentId(rootId);
							
							String targetPath = FileTool.exactPath(componentDirectory.getFilePath()) + componentDirectory.getFileName();
							componentDirectory.setTargetPath(targetPath);
							// 将对象添加到List当中
							allFilesList.add(componentDirectory);
							
							if(componentDirectory.isFile()){
								continue;
							}
							
							//获取集群类型
							Map<String,String> clusterPaths = componentsPath.get(componentDirectory.getFileName());
							//历遍三级节点，即组件类型下的集群节点
						    Vector<FileRecord> clusterDirectorys = trans.getFileList(targetPath);
						    
						    if(clusterPaths != null ){
						    	for(FileRecord clusterDirectory: clusterDirectorys){
									// 给于文件对象一个随机id
									String clusterCurrentId = UUID.randomUUID().toString();
									clusterDirectory.setCurrId(clusterCurrentId);
									// 给于文件对象父节点的id
									clusterDirectory.setParentId(componentCurrentId);
									
									targetPath = FileTool.exactPath(clusterDirectory.getFilePath()) + clusterDirectory.getFileName();
									clusterDirectory.setTargetPath(targetPath);
									
									// 将对象添加到List当中
									allFilesList.add(clusterDirectory);
									if(clusterDirectory.isFile()){
										continue;
									}
									String clusterPath = clusterPaths.get(clusterDirectory.getFileName());
									if(clusterPath != null){
										Map<String, String> clusterInfo =clusterDeployPath.get(clusterDirectory.getFileName());
										clusterDirectory.setClusterRoot(true);
										clusterDirectory.setClusterType(componentDirectory.getFileName());
										
										//jstorm支持创建文件
										if (componentDirectory.getFileName().equals(Constant.JSTORM)) {
											clusterDirectory.setClusterRoot(false);
										}
										
										clusterDirectory.setClusterId(clusterInfo.get("CLUSTER_ID"));
										clusterDirectory.setClusterCode(clusterDirectory.getFileName());
										clusterDirectory.setDesc(clusterInfo.get("CLUSTER_NAME"));
										
										//设置目录显示
										clusterDirectory.setFileName(clusterInfo.get("CLUSTER_NAME") +"(" + clusterDirectory.getFileName() + ")");
										recursiveFiles.treeList(trans, clusterPath, clusterCurrentId, clusterDirectory);
									}else{
										FileRecord tmpFileRecord = null;
										if(clusterDirectory.getFileName().equals(Constant.CLUSTER_DEFAULT)){
											tmpFileRecord = clusterDirectory;
										}
										recursiveFiles.treeList(trans, clusterDirectory.getFilePath()+"/"+clusterDirectory.getFileName(), clusterCurrentId,tmpFileRecord);
									}
								} 
						    }else{
						    	recursiveFiles.treeList(trans, componentDirectory.getFilePath()+"/"+componentDirectory.getFileName(), componentCurrentId,null);
						    }
						}
					 //对结果文件进行排序
						Collections.sort(allFilesList, new Comparator<FileRecord>() {
							@Override
							public int compare(FileRecord fileObj1, FileRecord fileObj2) {
								String prevFileName = fileObj1.getFileName();
								String nextFileName = fileObj2.getFileName();
								Collator collator = Collator.getInstance(Locale.CHINA);
								return collator.compare(prevFileName, nextFileName);
							}
						});
				 }
			} else if(page_type.equalsIgnoreCase("SERVICE")){
				// 创建一个根目录
				FileRecord rootFile = new FileRecord();
				String rootId = UUID.randomUUID().toString().replaceAll("-", "");
				rootFile.setCurrId(rootId);
				rootFile.setFileName(Constant.RELEASE);
				//rootFile.setFilePath(rootPath + Constant.BUSS_CONF);
				rootFile.setFilePath(path);
				rootFile.setParentId(Constant.ROOT_NODE_FLAG);
				rootFile.setFileType('D');
				rootFile.setFileLevel("R");
				allFilesList.add(rootFile);
				
				//判断目录是否存在，如果存在获取目录下的子节点数据
				boolean isExists = trans.isExistPath(path);
				log.debug("当前目录: " + path + ", 在部署主机是否存在: " + isExists);

				if (isExists) {
					RecursiveFile recursiveFiles = new RecursiveFile(allFilesList);
					recursiveFiles.treeList(trans, path, rootId, null);
				}
				//映射目录中文名称
				if(CollectionUtils.isNotEmpty(allFilesList)){
					List<HashMap<String,Object>> busClusterList = coreService.queryForList2New("busMainCluster.queryUpdateMainClusterInfo", null, FrameConfigKey.DEFAULT_DATASOURCE);
					Map<String,Object> queryMap = new HashMap<>();
					queryMap.put("GROUP_CODE","LATN_LIST");
					List<HashMap<String,Object>> latnList = coreService.queryForList2New("config.queryConfigList", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);

					for(int i=0;i<allFilesList.size();++i){
						FileRecord file = allFilesList.get(i);
						
						if(file.isDirectory()){
							String fileName = file.getFileName();
							//业务主集群名称
							for(int j=0;j<busClusterList.size();++j){
								Map<String,Object> map = busClusterList.get(j);
								String busClusterId = ObjectUtils.toString(map.get("BUS_CLUSTER_ID"));
								String busClusterName = ObjectUtils.toString(map.get("BUS_CLUSTER_NAME"));
								String busClusterCode = ObjectUtils.toString(map.get("BUS_CLUSTER_CODE"));
								if(StringUtils.equals(fileName, busClusterCode)){
									file.setFileName(busClusterName + "(" + busClusterCode + ")");
									file.setBusMainClusterId(busClusterId);
									break;
								}
							}

							//本地网名称
							for(int j=0;j<latnList.size();++j){
								Map<String,Object> map = latnList.get(j);
								
								String NAME = ObjectUtils.toString(map.get("CONFIG_NAME"));
								String CODE = ObjectUtils.toString(map.get("CONFIG_VALUE"));
								
								if(StringUtils.equals(fileName, CODE)){
									file.setFileName(NAME + "(" + CODE + ")");
									break;
								}
							}
						}
					}

					//获取部署的业务版本,只有部署了当前版本的业务程序才显示对应的配置文件  ======START================
					/**
					List<Map<String, Object>> deployBusList = coreService.queryForList3New("businessDeployList.queryFilterBusDeployList", null, FrameConfigKey.DEFAULT_DATASOURCE);
					for(int i=0;i<allFilesList.size();++i){
						FileRecord file = allFilesList.get(i);
						if (StringUtils.isNotBlank(file.getBusMainClusterId())) {
							String currNodeId = file.getCurrId();
							for (int j=0; j<allFilesList.size(); j++) {
								String parentNodeId = allFilesList.get(j).getParentId();
								String childNodeId = allFilesList.get(j).getCurrId();
								String currFileName = allFilesList.get(j).getFileName();
								if (StringUtils.equals(currNodeId, parentNodeId)) {
									if (isIPLegal(currFileName)) {
										for (int k=0; k<allFilesList.size(); k++) {
											if (StringUtils.equals(allFilesList.get(k).getParentId(), childNodeId)) {
												String filePath = file.getFilePath();
												//获取当前集群名称
												String currName = allFilesList.get(k).getFileName();
												//获取对应版本号
												String currVersion = filePath.substring(filePath.indexOf("_V") + 2);
												if (StringUtils.isNotBlank(filePath) && filePath.endsWith("/")) {
													filePath = filePath.substring(0, filePath.length() - 1);
												}
												String packageTypePath = filePath.substring(0, filePath.lastIndexOf("/"));
												String currPackageType = packageTypePath.substring(packageTypePath.lastIndexOf("/") + 1);

												boolean isDeployed = false;
												for (int m=0; m<deployBusList.size(); m++) {
													String clusterType = StringTool.object2String(deployBusList.get(m).get("CLUSTER_TYPE"));
													String clusterVersion = StringTool.object2String(deployBusList.get(m).get("VERSION"));
													String packageType = StringTool.object2String(deployBusList.get(m).get("PACKAGE_TYPE"));
													String persionConf = StringTool.object2String(deployBusList.get(m).get("CLUSTER_ELE_PERSONAL_CONF"));
													if (StringUtils.equals(persionConf, BusinessConstant.PARAMS_BUS_1)
															&& StringUtils.equals(currName, clusterType)
															&& StringUtils.equals(currVersion, clusterVersion)
															&& StringUtils.equals(currPackageType, packageType)) {
														isDeployed = true;
														break;
													}
												}
												if (!isDeployed) {
													getNodeChildrenNoFilter(allFilesList, allFilesList.get(j));
													allFilesList.get(j).setFilterFlag(BusinessConstant.PARAMS_BUS_1);
												}
											}
										}
									} else {
										String filePath = file.getFilePath();
										//获取当前集群名称
										String currName = allFilesList.get(j).getFileName();
										//获取对应版本号
										String currVersion = filePath.substring(filePath.indexOf("_V") + 2);
										if (StringUtils.isNotBlank(filePath) && filePath.endsWith("/")) {
											filePath = filePath.substring(0, filePath.length() - 1);
										}
										String packageTypePath = filePath.substring(0, filePath.lastIndexOf("/"));
										String currPackageType = packageTypePath.substring(packageTypePath.lastIndexOf("/") + 1);
										boolean isDeployed = false;
										for (int m=0; m<deployBusList.size(); m++) {
											String clusterType = StringTool.object2String(deployBusList.get(m).get("CLUSTER_TYPE"));
											String clusterVersion = StringTool.object2String(deployBusList.get(m).get("VERSION"));
											String packageType = StringTool.object2String(deployBusList.get(m).get("PACKAGE_TYPE"));
											if (StringUtils.equals(currName, clusterType)
													&& StringUtils.equals(currVersion, clusterVersion)
													&& StringUtils.equals(currPackageType, packageType)) {
												isDeployed = true;
												break;
											}
										}
										if (!isDeployed) {
											getNodeChildrenNoFilter(allFilesList, allFilesList.get(j));
											allFilesList.get(j).setFilterFlag(BusinessConstant.PARAMS_BUS_1);
										}
									}
								}
							}
						}
					}
					for (int i=0; i<allFilesList.size(); i++) {
						if (StringUtils.equals(allFilesList.get(i).getFilterFlag(), BusinessConstant.PARAMS_BUS_1)) {
							allFilesList.remove(allFilesList.get(i));
							i--;
						}
					}
					**/
					//==================================END========================================

				}
				//对结果文件进行排序
				Collections.sort(allFilesList, new Comparator<FileRecord>() {
					@Override
					public int compare(FileRecord fileObj1, FileRecord fileObj2) {
						String prevFileName = fileObj1.getFileName();
						String nextFileName = fileObj2.getFileName();
						Collator collator = Collator.getInstance(Locale.CHINA);
						return collator.compare(nextFileName, prevFileName);
					}
				});


				//将Billing模板提取到前面
				if (!BlankUtil.isBlank(allFilesList)) {
					//根据角色权限过滤
					String clusterSwitch = SystemProperty.getContextProperty("cluster.permission.switch");
					String empeeId = params.get("EMPEE_ID");
					if (StringUtils.equals(clusterSwitch.toLowerCase(), "yes") && !StringUtils.equals(empeeId, BusinessConstant.PARAMS_BUS_1)) {
						Map<String, Object> roleMap = new HashMap<String, Object>();
						roleMap.put("EMPEE_ID", empeeId);
						List<HashMap<String, Object>> clusterList = coreService.queryForList2New("busMainCluster.queryRoleBusMainClusterListByState", roleMap, dbKey);
						for (HashMap clusterMap : clusterList) {
							String busClusterId = StringTool.object2String(clusterMap.get("BUS_CLUSTER_ID"));
							for (FileRecord file : allFilesList) {
								String fileBusClusterId = file.getBusMainClusterId();
								if (StringUtils.equals(busClusterId, fileBusClusterId)) {
									getNodeParentNoFilter(allFilesList, file);
									getNodeChildrenNoFilter(allFilesList, file);
								}
							}
						}
						for (int i=0; i< allFilesList.size(); i++) {
							if (!StringUtils.equals(allFilesList.get(i).getFilterFlag(), BusinessConstant.PARAMS_BUS_1)) {
								allFilesList.remove(allFilesList.get(i));
								i--;
							}
						}
					}

					//查询业务类型
					Map<String, Object> queryMap = new HashMap<String, Object>();
					queryMap.put("TYPE", BusinessConstant.PARAMS_BUS_3);
					List<HashMap<String, Object>> busList = coreService.queryForList2New("clusterEleDefine.queryClusterEleList", queryMap, dbKey);
					List<String> busTypeList = new ArrayList<String>();
					busTypeList.add(Constant.CLUSTER_DEFAULT);
					busTypeList.add(Constant.T_DEFAULT);
					if (!BlankUtil.isBlank(busList)) {
						for (int i=0; i<busList.size(); i++) {
							String busTypeName = StringTool.object2String(busList.get(i).get("CLUSTER_TYPE"));
							busTypeList.add(busTypeName);
						}
					}
					//业务类型模板排序
					for (int i=0; i<allFilesList.size(); i++) {
						FileRecord currentRecord = allFilesList.get(i);
						String fileName = currentRecord.getFileName();
						if (busTypeList.contains(fileName)) {
							allFilesList.remove(currentRecord);
							allFilesList.add(0, currentRecord);
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("获取服务器上所有文件列表失败-->" , e);
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		log.debug("返回值结果：" + (allFilesList == null ? 0 : allFilesList.size()));
		return allFilesList;
	}

	/**
	 * 判断是否为IP地址
	 * @param ipStr
	 * @return
	 */
	public static boolean isIPLegal(String ipStr){
		if(StringUtils.isEmpty(ipStr)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");
		Matcher matcher = pattern.matcher(ipStr);
		return matcher.find();
	}
	/**
	 * 当前节点的父节点不过滤移除
	 *
	 * @param allFilesList
	 * @param file
	 */
	private void getNodeParentNoFilter(List<FileRecord> allFilesList, FileRecord file) {
		if (file != null) {
			file.setFilterFlag(BusinessConstant.PARAMS_BUS_1);
			FileRecord parentFileRecord = this.getCurrFileRecord(allFilesList, file.getParentId());
			getNodeParentNoFilter(allFilesList, parentFileRecord);
		}
	}

	/**
	 * 获取当前文件父文件
	 *
	 * @param allFilesList
	 * @param fileId
	 * @return
	 */
	private FileRecord getCurrFileRecord(List<FileRecord> allFilesList, String fileId) {
		FileRecord currFile = null;
		for (FileRecord fileRecord : allFilesList) {
			if (StringUtils.equals(fileRecord.getCurrId(), fileId)) {
				currFile = fileRecord;
				break;
			}
		}
		return currFile;
	}

	/**
	 * 当前节点的字节点不过滤移除
	 *
	 * @param allFilesList
	 * @param file
	 */
	private void getNodeChildrenNoFilter(List<FileRecord> allFilesList, FileRecord file) {
		if (file != null) {
			file.setFilterFlag(BusinessConstant.PARAMS_BUS_1);
			for (FileRecord fileRecord : allFilesList) {
				if (StringUtils.equals(fileRecord.getParentId(), file.getCurrId())) {
					getNodeChildrenNoFilter(allFilesList, fileRecord);
				}
			}
		}
	}

	/**
	 * 获取部署主机配置文件内容
	 * 
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, String> getFileContent(Map<String, String> params, String dbKey) throws Exception {
		log.debug("获取配置文件内容， 参数: " + params + ", dbKey: " + dbKey);
		// 获取所有参数信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String webRootPath = params.get("webRootPath");
		
		Map<String, String> cont = new HashMap<String, String>();
		
		//文件名称
		String fileName = params.get("fileName");
		//文件路径
		String filePath = params.get("filePath");
		// 目录拼接
		String path = FileTool.exactPath(filePath) + fileName;
		log.debug("配置文件路径: " + path);
		
		//获取部署真实目录
		String clusterId = StringTool.object2String(params.get("clusterId"));
		String clusterType = StringTool.object2String(params.get("clusterType"));
		if (!BlankUtil.isBlank(clusterId)) {
			Map<String, Object> queryClusterMap = new HashMap<String, Object>();
			queryClusterMap.put("CLUSTER_ID", clusterId);
			queryClusterMap.put("CLUSTER_TYPE", clusterType);
			Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryServiceTypeList", queryClusterMap, dbKey);
			if (!BlankUtil.isBlank(clusterMap) && !BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
				String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
				clusterType = StringTool.object2String(clusterMap.get("CLUSTER_TYPE"));
				cont.put("REAL_DEPLOY_PATH", appRootPath + Constant.Tools + Constant.CONF + clusterType);
			}
		}
		
		// 文件内容
		String fileContent = "";
		String localPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" ;
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();

			trans.get(path, localPath+ fileName);
			// 将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath+ fileName);
			// 去掉尾部所有空格
			int len = fileContent.length();
			int st = 0;
			char[] val = fileContent.toCharArray();
			while ((st < len) && (val[len - 1] <= ' ')) {
				len--;
			}
			fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
			log.debug("获取" + path + "文件内容成功！" );
		} catch (Exception e) {
			log.error("获取" + path + "文件内容失败-->", e);
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		cont.put("fileContent", fileContent);
		try {
			//删除本地文件
			FileUtil.deleteFile(localPath+ fileName);
			FileUtil.deleteFile(localPath);
			log.debug("删除本地文件目录/文件成功:" + localPath);
		} catch (Exception e) {
			log.error("删除本地文件目录/文件失败:" + localPath);
		}
		log.debug("获取配置文件内容结束...");
		return cont;
	}
	
	/**
	 * 获取部署主机业务配置文件内容(该方法和组件配置文件获取区分是因为该方法需要获取到配置文件远程主机路径)
	 * 
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, String> getFileBusContent(Map<String, String> params, String dbKey) throws Exception {
		log.debug("获取业务配置文件内容， 参数: " + params + ", dbKey: " + dbKey);
		// 获取所有参数信息
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		String webRootPath = params.get("webRootPath");
		
		Map<String, String> cont = new HashMap<String, String>();
		
		//文件名称
		String fileName = params.get("fileName");
		//文件路径
		String filePath = params.get("filePath");
		// 目录拼接
		String path = FileTool.exactPath(filePath) + fileName;
		log.debug("配置文件路径: " + path);
		
		//查询所有的业务主集群列表
		List<HashMap<String, Object>> busMainClusterList = coreService.queryForList2New("busMainCluster.queryBusMainClusterList", null, dbKey);
		Map<String, Object> busMainClusterMap = new HashMap<String, Object>();
		String [] pathArray = null;
		if (!BlankUtil.isBlank(filePath) && filePath.indexOf("/") != -1) {
			pathArray = filePath.split("/");
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<busMainClusterList.size(); j++) {
					String busClusterCode = StringTool.object2String(busMainClusterList.get(j).get("BUS_CLUSTER_CODE"));
					if (!BlankUtil.isBlank(pathArray[i]) && busClusterCode.equals(pathArray[i].trim())) {
						busMainClusterMap = busMainClusterList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取业务主集群信息:" + busMainClusterMap.toString());
		
		
		//获取业务小版本信息
		Map<String, Object> clusterMap = new HashMap<String, Object>();
		if (!BlankUtil.isBlank(busMainClusterMap) && !busMainClusterMap.isEmpty()) {
			//获取关联小集群
			Map<String, Object> relationMap = new HashMap<String, Object>();
			relationMap.put("BUS_CLUSTER_ID", busMainClusterMap.get("BUS_CLUSTER_ID"));
			List<HashMap<String, Object>> busRelationClusterList = coreService.queryForList2New("busRelationClusterList.queryClusterByBusClusterId", relationMap, dbKey);
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<busRelationClusterList.size(); j++) {
					String clusterType = StringTool.object2String(busRelationClusterList.get(j).get("CLUSTER_TYPE"));
					if (!BlankUtil.isBlank(pathArray[i]) && clusterType.equals(pathArray[i].trim())) {
						clusterMap = busRelationClusterList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取业务子集群信息:" + clusterMap.toString());
		
		//获取版本信息
		Map<String, Object> versionMap = new HashMap<String, Object>();
		if (!BlankUtil.isBlank(clusterMap) && !clusterMap.isEmpty()) {
			//获取关联小集群
			Map<String, Object> versionQueryMap = new HashMap<String, Object>();
			versionQueryMap.put("CLUSTER_ID", clusterMap.get("CLUSTER_ID"));
			versionQueryMap.put("CLUSTER_TYPE", clusterMap.get("CLUSTER_TYPE"));
			List<HashMap<String, Object>> versionList = coreService.queryForList2New("deployHome.queryClusterTypeDeployVersionList", versionQueryMap, dbKey);
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<versionList.size(); j++) {
					String versionName = StringTool.object2String(versionList.get(j).get("NAME"));
					if (!BlankUtil.isBlank(pathArray[i]) && versionName.equals(pathArray[i].trim())) {
						versionMap = versionList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取版本信息: " + versionMap.toString());
		if (clusterMap != null && !clusterMap.isEmpty() && versionMap != null && !versionMap.isEmpty()) {
			String deployPath = StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH"));
			String version = StringTool.object2String(versionMap.get("VERSION"));
			String realDeployPath = FileTool.exactPath(deployPath) + FileTool.exactPath(Constant.BUSS) + FileTool.exactPath("V" + version) + Constant.CFG;
			cont.put("REAL_DEPLOY_PATH", realDeployPath);
		}
		
		//判断当前配置文件是否为版本切换配置文件
		List<String> switchList = getSwitchFileList(dbKey);
		if (switchList.contains(fileName)) {
			cont.put("REAL_DEPLOY_PATH", "${集群根目录}/" + FileTool.exactPath(Constant.BUSS));
		}
		
		// 文件内容
		String fileContent = "";
		String localPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" ;
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();

			trans.get(path, localPath+ fileName);
			// 将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath+ fileName);
			// 去掉尾部所有空格
			int len = fileContent.length();
			int st = 0;
			char[] val = fileContent.toCharArray();
			while ((st < len) && (val[len - 1] <= ' ')) {
				len--;
			}
			fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
			log.debug("获取" + path + "文件内容成功！" );
		} catch (Exception e) {
			log.error("获取" + path + "文件内容失败-->", e);
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		cont.put("fileContent", fileContent);
		try {
			//删除本地文件
			FileUtil.deleteFile(localPath+ fileName);
			FileUtil.deleteFile(localPath);
			log.debug("删除本地文件目录/文件成功:" + localPath);
		} catch (Exception e) {
			log.error("删除本地文件目录/文件失败:" + localPath);
		}
		log.debug("获取业务配置文件内容结束...");
		return cont;
	}
	
	/**
	 * 获取远程主机配置文件内容
	 * 
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map返回对象
	 */
	@Override
	public Map<String, String> showConfigContentByHost(Map<String, String> params, String dbKey) throws Exception {
		log.debug("获取远程主机配置文件内容， 参数: " + params + ", dbKey: " + dbKey);
		// 获取所有参数信息
		String webRootPath = params.get("webRootPath");
		String fileName = params.get("fileName");
		String filePath = params.get("filePath");
		
		// 文件内容
		String fileContent = "";
		String localPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" ;
		Map<String, String> cont = new HashMap<String, String>();
		Trans trans = null;
		try {
			// 需要分发的主机列表
			HashMap<String,String> host= coreService.queryForObject("host.queryHostById", params, dbKey);
			if(host == null || host.isEmpty()){
				log.error("查询远程主机信息失败...");
				throw new RuntimeException("未查询到相关主机信息，请检查！");
			}
			
			trans = FTPUtils.getFtpInstance(host.get("HOST_IP"), host.get("SSH_USER"),
					DesTool.dec(host.get("SSH_PASSWD")), SessionUtil.getConfigValue("FTP_TYPE"));
			trans.login();

			trans.get(filePath, localPath+ fileName);
			// 将文件转成字符串
			fileContent = FileUtil.readFileUnicode(localPath+ fileName);
			// 去掉尾部所有空格
			int len = fileContent.length();
			int st = 0;
			char[] val = fileContent.toCharArray();
			while ((st < len) && (val[len - 1] <= ' ')) {
				len--;
			}
			fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
			log.debug("获取" + filePath + "文件内容成功！" );
		} catch (Exception e) {
			log.error("获取" + filePath + "文件内容失败-->" , e);
			throw new Exception("配置文件不存在, 请检查！");
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		cont.put("fileContent", fileContent);
		try {
			//删除本地文件
			FileUtil.deleteFile(localPath+ fileName);
			FileUtil.deleteFile(localPath);
			log.debug("删除本地文件目录/文件成功:" + localPath);
		} catch (Exception e) {
			log.error("删除本地文件目录/文件失败:" + localPath);
		}
		log.debug("查询组件配置文件结束!");
		return cont;
	}
	
	/**
	 * 业务：保存文件
	 * 
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateSaveFileContents(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("业务配置文件修改， 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		//部署主机信息
		FtpDto ftpDto = SessionUtil.getFtpParams();

		//修改文件名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//修改文件路径
		String serverFilePath = FileTool.exactPath((StringTool.object2String(params.get("filePath"))));
		//修改文件内容
		String new_content = StringTool.object2String(params.get("new_content"));
		
		String hostTopPath = StringTool.object2String(params.get("topPath"));
		//配置文件所在版本
		String VERSION = StringTool.object2String(params.get("VERSION"));
		// 版本路径
		String versionDir = FileTool.exactPath("V" + VERSION);
		
		//获取主机IP，当修改的是IP下的配置文件只需要分发到对应IP就可以了，否则分发到当前集群所有配置文件
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		String hostIp = StringTool.object2String(params.get("HOST_IP"));
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("CLUSTER_ID", clusterId);
		queryMap.put("CLUSTER_TYPE", clusterType);
		queryMap.put("HOST_IP", hostIp);
		queryMap.put("VERSION", VERSION);
		
		
		List<HashMap<String, Object>> hostList = null;
		//版本切换配置文件处理
		boolean isSwitch = false;
		
		if (BlankUtil.isBlank(clusterType)) {
			//判断配置文件是否为版本切换配置文件，如果为版本切换配置文件则查询所有路由主机
			List<HashMap<String, Object>> switchList = getSwitchFileDetailList(dbKey);
			for (int i=0; i<switchList.size(); i++) {
				String switchConfigFileName = StringTool.object2String(switchList.get(i).get("SWITCH_CONFIG_FILE"));
				if (switchConfigFileName.equalsIgnoreCase(fileName)) {
					//查询所有需要分发的主机列表
					String switchClusterType = StringTool.object2String(switchList.get(i).get("SWITCH_CLUSTER_TYPE"));
					if (!BlankUtil.isBlank(switchClusterType)) {
						Map<String, Object> queryRouteMap = new HashMap<String, Object>();
						queryRouteMap.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
						hostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryRouteMap, dbKey);
						isSwitch = true;
					}
				}
			}
			
			if (!isSwitch) {
				hostList = coreService.queryForList2New("ftpFileUpload.queryUploadHostList", queryMap, dbKey);
			}
		} else {
			//查询需要分发的远程主机
			hostList = coreService.queryForList2New("ftpFileUpload.queryUploadHostList", queryMap, dbKey);
		}
				
		Trans trans = null;
		Map<String, Object> cont = new HashMap<String, Object>();
		InputStream is = null;
		try {
			// 部署主机目录
			String path = serverFilePath + fileName;
			log.debug("保存到部署主机上的文件目录:" + path);
			
			// 将修改文件分发到部署主机
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			is = new ByteArrayInputStream(new_content.getBytes(DEFAULT_ENCODING));
			trans.login();
			trans.put(is, path);
			
			if (!BlankUtil.isBlank(hostList)) {
				
				// 分发到主机的文件目标路径
				String host_path = "";
				if (!isSwitch) {
					//获取当前集群部署根目录
					Map<String, Object> queryClusterMap = new HashMap<String, Object>();
					queryClusterMap.put("CLUSTER_ID", clusterId);
					queryClusterMap.put("CLUSTER_TYPE", clusterType);
					Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
					if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
						throw new RuntimeException("集群信息查询失败, 请检查！");
					}
					//组件部署根目录
					final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
					
					//只保存到nimbus主机，rebalance文件
					if(params.get("is_doRebalance") !=null){
						host_path = appRootPath + Constant.Tools + Constant.ENV + Constant.JSTORM_DIR + Constant.BIN + versionDir + fileName; 
					} else {
						// 路径：
						// /public/bp/dccp/business/V0.0.1（版本名）/cfg/sm.sql.xml（文件名）
						String codeP = "/"+params.get("CLUSTER_TYPE") +"/";
						String subPath = serverFilePath.substring(serverFilePath.lastIndexOf(codeP)+codeP.length(),serverFilePath.length());
						host_path = appRootPath + Constant.BUSS + versionDir + Constant.CFG_DIR + subPath + fileName;
					}
				}
				
				List<String> successHostList = new ArrayList<String>();
				List<String> errorHostList = new ArrayList<String>();
				// 分发文件(保存到其它主机上)
				for (int i = 0; i < hostList.size(); i++) {
					Map<String, Object> hostMap = hostList.get(i);
					
					//远程主机信息
					String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
					String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
					String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
					String ftpType = ftpDto.getFtpType();
					
					//sp_switch.xml配置文件分发路径
					if (isSwitch) {
						String deployPath = StringTool.object2String(hostMap.get("CLUSTER_DEPLOY_PATH"));
						host_path = FileTool.exactPath(deployPath) + Constant.BUSS + fileName;
					}
					
					Trans tran = null;
					try {
						tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpType);
						tran.login();
						log.debug("分发远程主机， 目录：" + host_path);
						
						//主机名
						String hostName = StringTool.object2String(hostMap.get("HOST_NAME"));
						String fileContent = new_content.replaceAll("$HOST_IP",  sshIp).replaceAll("$HOST_NAME",  hostName);
						is = new ByteArrayInputStream(fileContent.getBytes(DEFAULT_ENCODING));
						tran.put(is, host_path);
						successHostList.add(sshIp);
					} catch (Exception e) {
						errorHostList.add(sshIp);
						log.error("远程主机配置文件分发失败， 失败信息：", e);
					} finally {
						if (tran != null) {
							tran.close();
						}
					}
				}

				String msg = "保存成功！";
				if (!BlankUtil.isBlank(successHostList)) {
					msg += "文件分发到【" + StringUtils.join(successHostList, ",") + "】主机成功!";
				}

				if (!BlankUtil.isBlank(errorHostList)) {
					msg += "\n保存到【" + StringUtils.join(errorHostList, ",") + "】主机失败!";
				}
				
				cont.put("successNum", successHostList.size());
				cont.put("errorNum", errorHostList.size() );
				cont.put("isSuccess", msg);
			} else {
				cont.put("isSuccess", "文件保存成功，但该文件无对应主机，未能分发。");
			}
		} catch (Exception e) {
			log.error("文件保存到服务器上失败-->" , e);
			throw new Exception("文件保存失败！" + e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
			if (is != null) {
				is.close();
			}
		}
		return cont;
	}
	
	/**
	 * 业务：保存文件
	 * 
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateSaveBusFileContents(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("业务配置文件修改， 参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String, Object> cont = new HashMap<String, Object>();


		//修改文件名称
		String fileName = StringTool.object2String(params.get("FILE_NAME"));
		//修改文件路径
		String filePath = FileTool.exactPath((StringTool.object2String(params.get("FILE_PATH"))));

		String targetPath = StringTool.object2String(params.get("TARGET_PATH"));
		//修改文件内容
		String newContent = StringTool.object2String(params.get("NEW_CONTENT"));
		//前台中将+转义成  %2b,现场偶尔没有将 %2b转化为+，手动转化
		if (StringUtils.isNotBlank(newContent) && newContent.toUpperCase().indexOf("%2B") >0) {
			newContent = newContent.replace("%2b", "+").replace("%2B", "+");
		}

		//判断登录用户是否有权限修改配置文件
		//String empeeId =  StringTool.object2String(params.get("EMPEE_ID"));
		Map<String,Object> queryPriviMap = new HashMap<String,Object>();
		//如果 登录 账号 是1， 则属于超及用户， 则可以任意修改
		String fileIsSwitch = StringUtils.defaultIfBlank(SystemProperty.getContextProperty("file.permission.switch"),"no");
		if(!StringUtils.equals(fileIsSwitch,"no")){
			if(!BusinessConstant.PARAMS_BUS_1.equals(StringTool.object2String(params.get("EMPEE_ID")))) {
				queryPriviMap.put("EMPEE_ID", params.get("EMPEE_ID"));
				queryPriviMap.put("FILE_PATH", targetPath);

				List<HashMap<String,Object>> privilegeList = coreService.queryForList2New("userCfgFileMapper.queryUserRoleByEmpeeId", queryPriviMap, dbKey);
				if(privilegeList ==null || privilegeList.isEmpty()){
					cont.put("isSuccess", "该用户没有权限修改此文件，请联系管理员！");
					return cont;
				}
			}
		}
		//查询所有的业务主集群列表
		List<HashMap<String, Object>> busMainClusterList = coreService.queryForList2New("busMainCluster.queryBusMainClusterList", null, dbKey);
		Map<String, Object> busMainClusterMap = new HashMap<String, Object>();
		String [] pathArray = null;
		if (!BlankUtil.isBlank(filePath) && filePath.indexOf("/") != -1) {
			pathArray = filePath.split("/");
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<busMainClusterList.size(); j++) {
					String busClusterCode = StringTool.object2String(busMainClusterList.get(j).get("BUS_CLUSTER_CODE"));
					if (!BlankUtil.isBlank(pathArray[i]) && busClusterCode.equals(pathArray[i].trim())) {
						busMainClusterMap = busMainClusterList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取业务主集群信息:" + busMainClusterMap.toString());
		
		
		//获取业务小版本信息
		Map<String, Object> clusterMap = new HashMap<String, Object>();
		if (!BlankUtil.isBlank(busMainClusterMap) && !busMainClusterMap.isEmpty()) {
			//获取关联小集群
			Map<String, Object> relationMap = new HashMap<String, Object>();
			relationMap.put("BUS_CLUSTER_ID", busMainClusterMap.get("BUS_CLUSTER_ID"));
			List<HashMap<String, Object>> busRelationClusterList = coreService.queryForList2New("busRelationClusterList.queryClusterByBusClusterId", relationMap, dbKey);
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<busRelationClusterList.size(); j++) {
					String clusterType = StringTool.object2String(busRelationClusterList.get(j).get("CLUSTER_TYPE"));
					if (!BlankUtil.isBlank(pathArray[i]) && clusterType.equals(pathArray[i].trim())) {
						clusterMap = busRelationClusterList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取业务子集群信息:" + clusterMap.toString());
		
		//获取版本信息
		Map<String, Object> versionMap = new HashMap<String, Object>();
		if (!BlankUtil.isBlank(clusterMap) && !clusterMap.isEmpty()) {
			//获取关联小集群
			Map<String, Object> versionQueryMap = new HashMap<String, Object>();
			versionQueryMap.put("CLUSTER_ID", clusterMap.get("CLUSTER_ID"));
			versionQueryMap.put("CLUSTER_TYPE", clusterMap.get("CLUSTER_TYPE"));
			List<HashMap<String, Object>> versionList = coreService.queryForList2New("deployHome.queryClusterTypeDeployVersionList", versionQueryMap, dbKey);
			for (int i=(pathArray.length-1); i>=0; i--) {
				boolean isOk = false;
				for (int j=0; j<versionList.size(); j++) {
					String versionName = StringTool.object2String(versionList.get(j).get("NAME"));
					if (!BlankUtil.isBlank(pathArray[i]) && versionName.equals(pathArray[i].trim())) {
						versionMap = versionList.get(j);
						isOk = true;
						break;
					}
				}
				if (isOk) {
					break;
				}
			}
		}
		log.debug("获取版本信息: " + versionMap.toString());
		
		//是否需要根据主机IP分发文件
		String personConf = StringTool.object2String(clusterMap.get("PERSONAL_CONF"));
		List<HashMap<String, Object>> hostList = null;
		
		//版本切换配置文件处理
		boolean isSwitch = false;
		if (BlankUtil.isBlank(clusterMap)) {
			//判断配置文件是否为版本切换配置文件，如果为版本切换配置文件则查询所有路由主机
			List<HashMap<String, Object>> switchList = getSwitchFileDetailList(dbKey);
			for (int i=0; i<switchList.size(); i++) {
				String switchConfigFileName = StringTool.object2String(switchList.get(i).get("SWITCH_CONFIG_FILE"));
				if (switchConfigFileName.equalsIgnoreCase(fileName)) {
					//查询所有需要分发的主机列表
					String switchClusterType = StringTool.object2String(switchList.get(i).get("SWITCH_CLUSTER_TYPE"));
					if (!BlankUtil.isBlank(switchClusterType)) {
						Map<String, Object> queryRouteMap = new HashMap<String, Object>();
						queryRouteMap.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
						hostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryRouteMap, dbKey);
						isSwitch = true;
					}
				}
			}
		}
		
		if (!isSwitch) {
			//查询需要分发的远程主机
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("CLUSTER_ID", clusterMap.get("CLUSTER_ID"));
			queryMap.put("CLUSTER_TYPE", clusterMap.get("CLUSTER_TYPE"));
			queryMap.put("VERSION", versionMap.get("VERSION"));
			hostList = coreService.queryForList2New("ftpFileUpload.queryUploadHostList", queryMap, dbKey);
			
			//判断当前集群是否根据主机IP分发
			if (BusinessConstant.PARAMS_BUS_1.equals(personConf)) {
				HashMap<String, Object> hostMap = null;
				for (int i=(pathArray.length-1); i>=0; i--) {
					boolean isOk = false;
					for (int j=0; j<hostList.size(); j++) {
						String hostIp = StringTool.object2String(hostList.get(j).get("HOST_IP"));
						if (!BlankUtil.isBlank(pathArray[i]) && hostIp.equals(pathArray[i].trim())) {
							hostMap = hostList.get(j);
							isOk = true;
							break;
						}
					}
					if (isOk) {
						break;
					}
				}
				hostList.clear();
				if (hostMap != null && !hostMap.isEmpty()) {
					hostList.add(hostMap);
				}
			}
			log.debug("需要分发的主机: " + hostList == null ? "" : hostList.toString());
		}
				
		Trans trans = null;
		InputStream is = null;
		try {
			//部署主机信息
			FtpDto ftpDto = SessionUtil.getFtpParams();
			// 将修改文件分发到部署主机
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			
			//版本切换配置文件需要分发到所有的部署主机文件列表
			if (isSwitch) {
				String rootPath= ftpDto.getFtpRootPath();
				String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
				
				//查询所有的包类型
				List<Map<String, Object>> configList = SessionUtil.getConfigListByGroupCode("WEB_BUS_PACKAGE_TYPE");
				for (int j=0; j<configList.size(); j++) {
					String packageType = StringTool.object2String(configList.get(j).get("CONFIG_VALUE"));
					String spSwitchPath = FileTool.exactPath(rootPath) + FileTool.exactPath(businessPath) + FileTool.exactPath(packageType);
				
					boolean isExistSwitch = Boolean.FALSE;
					if(trans.isExistPath(spSwitchPath)) {
						Vector<FileRecord> fileList = trans.getFileList(spSwitchPath);
						for (int k=0; k<fileList.size(); k++) {
							String spFileName = fileList.get(k).getFileName();
							if (fileName.equals(spFileName)) {
								isExistSwitch = Boolean.TRUE;
								break;
							}
						}
					}
					if (isExistSwitch) {
						//部署主机sp_switch.xml配置文件路径
						String switchConfigFile = FileTool.exactPath(rootPath) + FileTool.exactPath(businessPath) 
								+ FileTool.exactPath(packageType) + fileName;
						//XML文件对象流
						is = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
						trans.put(is, switchConfigFile);
						log.debug("sp_switch.xml文件部署主机同步成功， 路径: " + switchConfigFile);
					}
				}
			} else {
				// 部署主机目录
				String path = FileTool.exactPath(filePath) + fileName;
				log.debug("保存到部署主机上的文件目录:" + path);
				is = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
				trans.put(is, path);
			}
			if (!BlankUtil.isBlank(hostList) && !hostList.isEmpty()) {
				//远程主机部署路径
				String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
				//远程主机部署版本
				String versionDir = FileTool.exactPath("V" + versionMap.get("VERSION"));
				
				List<String> successHostList = new ArrayList<String>();
				List<String> errorHostList = new ArrayList<String>();
				// 分发文件(保存到其它主机上)
				for (int i = 0; i < hostList.size(); i++) {
					Map<String, Object> hostMap = hostList.get(i);
					log.debug("分发主机信息: " + hostMap.toString());
					
					//远程主机信息
					String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
					String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
					String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
					String ftpType = ftpDto.getFtpType();
					
					//集群名称
					String clusterName = StringTool.object2String(hostMap.get("CLUSTER_NAME"));
					
					// 分发到主机的文件目标路径
					String host_path = "";

					//在目标主机的配置文件加上对应的目录，比如 default目录，在目录加上/bss03/bss_net/app/dcm/business/V1.0.0.0/cfg/default
					//当前就是dcm 在配置文件目录再加目录
					/*String clusterType = StringTool.object2String(clusterMap.get("CLUSTER_TYPE"));
					if("dcm".equals(clusterType)){
						String parentDiv = StringTool.object2String(params.get("parentDiv"));
						host_path = appRootPath + Constant.BUSS + versionDir + Constant.CFG_DIR + FileTool.exactPath(parentDiv) + fileName;
					}else{
						host_path = appRootPath + Constant.BUSS + versionDir + Constant.CFG_DIR + fileName;
					}*/
					host_path = appRootPath + Constant.BUSS + versionDir + Constant.CFG_DIR + fileName;
					//版本切换配置文件分发路径
					if (isSwitch) {
						String deployPath = StringTool.object2String(hostMap.get("CLUSTER_DEPLOY_PATH"));
						host_path = FileTool.exactPath(deployPath) + Constant.BUSS + fileName;
					}
					
					Trans tran = null;
					try {
						tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpType);
						tran.login();
						log.debug("分发远程主机， 目录：" + host_path);
						
						//主机名
						String hostName = StringTool.object2String(hostMap.get("HOST_NAME"));
						String fileContent = newContent.replaceAll("$HOST_IP",  sshIp).replaceAll("$HOST_NAME",  hostName);
						is = new ByteArrayInputStream(fileContent.getBytes(DEFAULT_ENCODING));
						tran.put(is, host_path);
						successHostList.add(sshIp+"(" + clusterName + ")");
					} catch (Exception e) {
						errorHostList.add(sshIp + "(" + clusterName + ")");
						log.error("远程主机配置文件分发失败， 失败信息：", e);
					} finally {
						if (tran != null) {
							tran.close();
						}
					}
				}

				String msg = "保存成功！";
				if (!BlankUtil.isBlank(successHostList)) {
					msg += "文件分发到【" + StringUtils.join(successHostList, "，") + "】主机成功!";
				}

				if (!BlankUtil.isBlank(errorHostList)) {
					msg += "\n保存到【" + StringUtils.join(errorHostList, "，") + "】主机失败!";
				}
				
				cont.put("successNum", successHostList.size());
				cont.put("errorNum", errorHostList.size() );
				cont.put("isSuccess", msg);
			} else {
				cont.put("successNum", 1);
				cont.put("errorNum", 0);
				cont.put("isSuccess", "文件保存成功，但该文件无对应主机，未能分发。");
			}
		} catch (Exception e) {
			log.error("文件保存到服务器上失败-->" , e);
			throw new Exception("文件保存失败！" + e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
			if (is != null) {
				is.close();
			}
		}
		return cont;
	}

	/**
	 * 平台：保存、分发修改后的文件
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateDistributeFileContent(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("修改配置文件分发开始， 参数: " + params.toString() + ", dbKey: " + dbKey);

		FtpDto ftpDto = SessionUtil.getFtpParams();
		String newContent = StringTool.object2String(params.get("new_content"));
		//前台中将+转义成  %2b,现场偶尔没有将 %2b转化为+，手动转化
		if (StringUtils.isNotBlank(newContent) && newContent.toUpperCase().indexOf("%2B") >0) {
			newContent = newContent.replace("%2b", "+").replace("%2B", "+");
		}

		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		//String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(params.get("clusterCode"));
		String filePath = StringTool.object2String(params.get("filePath"));
		String fileName = StringTool.object2String(params.get("fileName"));
		String targetPath = StringTool.object2String(params.get("targetPath"));
		String hostIpDir = StringTool.object2String(params.get("parentNodeName"));
		
		//判断需要分发的主机列表
		String fileName_ip = "";
		if(hostIpDir.contains("_") && !Constant.CLUSTER_DEFAULT.equals(hostIpDir)){
			fileName_ip = hostIpDir.substring(0, hostIpDir.indexOf("_"));
			//判断是否符合为IP表达式 (IPV6校验存在问题，不需要进行)
			//if (ComParamsHelper.isMatchIp(fileName_ip)) {
			//	params.put("HOST_IP", fileName_ip);
			//}
		}
		// 需要分发的主机列表
		List<HashMap<String, Object>> hostList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> queryHostList = coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", params, dbKey);
		hostList.addAll(queryHostList);
		if (StringUtils.isNotBlank(fileName_ip) && CollectionUtils.isNotEmpty(queryHostList)) {
			for (int i=0; i<queryHostList.size(); i++) {
				if (StringUtils.equals(fileName_ip, org.apache.commons.lang3.ObjectUtils.toString(queryHostList.get(i).get("HOST_IP"), ""))) {
					hostList.clear();
					hostList.add(queryHostList.get(i));
					break;
				}
			}
		}
		
		// 目录拼接
		String path = FileTool.exactPath(filePath) + fileName;
		log.debug("保存到远程服务器上的文件目录:" + path);
		
		///public/bp/AH_DCBPortal/conf/platform_config/release/rocketmq/rocketMQCode/broker/192.168.161.26_01/broker-a-s.properties
		String replaceStr = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(Constant.PLAT_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(clusterType) + FileTool.exactPath(clusterCode);
		String fileSuffix = targetPath.replace(replaceStr, "");
		log.debug("修改的配置文件路径: " + fileSuffix);
		
		Trans trans = null;
		ByteArrayInputStream is = null;
		Map<String, Object> cont = new HashMap<String, Object>();
		try {
			// 保存配置文件到部署主机
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			is = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
			trans.put(is, path);
			
			if (!BlankUtil.isBlank(hostList)) {
				List<String> successHostList = new ArrayList<String>();
				List<String> errorHostList = new ArrayList<String>();
				
				for (int i = 0; i < hostList.size(); i++) {
					Map<String, Object> host = hostList.get(i);
					
					//获取集群远程主机部署路径
					String clusterDeployPath = FileTool.exactPath(StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH")));
					String remotePath = clusterDeployPath + FileTool.exactPath(Constant.Tools) + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(clusterType) + fileSuffix;
					log.debug("远程主机配置文件路径: " + remotePath);
					
					Trans tran = null;
					ByteArrayInputStream iss = null;
					// 分发到主机的文件目标路径
					try {
						String hostIp = StringTool.object2String(host.get("HOST_IP"));
						String sshUser = StringTool.object2String(host.get("SSH_USER"));
						String sshPwd = DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD")));
						String ftpType = ftpDto.getFtpType();
						tran = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpType);
						tran.login();
						
						// 如是存在多台机器，要加此代码，不然会是保存的空文件
						log.info(host.get("CLUSTER_CODE")+"集群_"+host.get("HOST_IP") +":"+host.get("SSH_USER")+ "分发文件路径：" + remotePath);
						
						iss = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
						tran.put(iss, remotePath);
						log.info(host.get("CLUSTER_CODE")+"集群_"+host.get("HOST_IP") +":"+host.get("SSH_USER") + "分发文件成功！");
						successHostList.add(StringTool.object2String("【集群:"+host.get("CLUSTER_CODE")+" 主机"+host.get("HOST_IP")+" 用户:"+host.get("SSH_USER")+"】"));
					} catch (Exception e) {
						errorHostList.add(StringTool.object2String("【集群:"+host.get("CLUSTER_CODE")+" 主机:"+host.get("HOST_IP")+" 用户:"+host.get("SSH_USER")+"】"));
						log.error("文件分发失败-->" , e);
					} finally {
						if (tran != null) {
							tran.close();
						}
						if (iss != null) {
							iss.close();
						}
					}
				}
				String msg = "保存内容成功！";
				if (successHostList.size() > 0) {
					msg += "同步保存到" + StringUtils.join(successHostList, ",") + "成功!";
				}
				if (errorHostList.size() > 0) {
					msg += "\n同步保存到" + StringUtils.join(errorHostList, ",") + "失败!";
				}
				cont.put("successNum", successHostList.size());
				cont.put("errorNum", errorHostList.size() );
				cont.put("isSuccess", msg);
			} else {
				cont.put("isSuccess", "文件保存成功！但部署主机不存在，尚未同步。");
			}
		} catch (Exception e) {
			log.error("文件保存到服务器上失败-->" , e);
			throw new Exception("文件保存失败！" + e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
			if (is != null) {
				is.close();
			}
		}
		return cont;
	}

	/**
	 * 组件新建配置文件
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateCreateFile(Map<String, Object> params,String dbKey) throws Exception {
		log.debug("组件创建文件， 参数: " + params.toString() + ", dbKey: " + dbKey);
		//选择节点名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//新创建节点名称
		String newFileName = StringTool.object2String(params.get("newFileName"));
		//选择节点路径
		String filePath = StringTool.object2String( params.get("filePath"));
		//新创建节点类型
		String newFileType = StringTool.object2String(params.get("newFileType"));
		//集群类型
		String cluster_type=StringTool.object2String( params.get("CLUSTER_TYPE"));
		//集群ID
		String cluster_id=StringTool.object2String( params.get("CLUSTER_ID"));
		//集群编码
		String cluster_code=StringTool.object2String( params.get("CLUSTER_CODE"));
		String targetPath=StringTool.object2String( params.get("targetPath"));
		//拷贝文件内容
		String copyFilesNames = StringTool.object2String(params.get("copyFilesNames"));
		
		// 连接服务器的参数
	   FtpDto ftpDto = SessionUtil.getFtpParams();
		Map<String, Object> cont = new HashMap<String, Object>();
		//新增文件路径(文件服务器)
		String new_path = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + newFileName;
		// 连接用于上传文件或者文件夹
		// 链接用于下载文件列表以及指定文件的内容
		Trans trans = null;
		ByteArrayInputStream is = null;

		// 要新建的是文件
		if (newFileType.equals("file")) {
			try {
				trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans.login();
				
				
				//事前判断路径下是否有重复的文件名
				if(trans.isExistPath(new_path)){
					Vector<FileRecord> fileList = trans.getFileList(new_path);
					for (int i=0; i<fileList.size(); i++) {
						if (fileList.get(i).getFileName().equals(newFileName)) {
							throw new RuntimeException("文件名已存在，请检查！");
						}
					}
				}
				
				String content = "请编辑内容！";
				//如果拷贝文件不为空则重拷贝文件获取文件内容
				Trans copyTrans = null;
				if (!BlankUtil.isBlank(copyFilesNames)) {
					try {
						content = "";
						String copyPath = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + copyFilesNames;
						log.debug("组件新增配置文件， 获取模板内容， 模板文件路径: " + copyPath);
						copyTrans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
						copyTrans.login();
						InputStream inStream = copyTrans.get(copyPath);
						int len = 0;
						byte [] bytes = new byte[1024];
						while((len = inStream.read(bytes)) != -1) {
							String tmpStr = new String(bytes, DEFAULT_ENCODING);
							content += tmpStr;
						}
						inStream.close();
						copyTrans.completePendingCommand();
					} catch (Exception e) {
						content = "请编辑内容！";
					} finally {
						if (copyTrans != null) {
							copyTrans.close();
						}
					}
				}
				// 将内容放入新的文件
				is = new ByteArrayInputStream(content.getBytes(DEFAULT_ENCODING));
				trans.put(is, new_path);
				log.debug("创建组件配置文件， 新创建文件路径: " + new_path);
				
				Map<String,Object> queryMap=new HashMap<String,Object>();
				queryMap.put("CLUSTER_TYPE",cluster_type);
				queryMap.put("CLUSTER_ID", cluster_id );
				if(fileName.contains("_") && fileName.contains("(") && fileName.contains(")")){
					int beginIndex=fileName.indexOf("(",0);
					int endIndex=fileName.indexOf(")",0);
					String _ip=fileName.substring(0,beginIndex);
					String _user=fileName.substring(beginIndex+1,endIndex);
					if (!BlankUtil.isBlank(_ip) && ComParamsHelper.isMatchIp(_ip)) {
						queryMap.put("HOST_IP", _ip);
						queryMap.put("SSH_USER", _user);
					}
				}
				
				///public/bp/AH_DCBPortal/conf/platform_config/release/rocketmq/rocketMQCode/broker/192.168.161.26_01/broker-a-s.properties
				String replaceStr = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(Constant.PLAT_CONF)
						+ Constant.RELEASE_DIR + FileTool.exactPath(cluster_type) + FileTool.exactPath(cluster_code);
				String fileSuffix = targetPath.replace(replaceStr, "");
				log.debug("新增配置文件， 分发到远程主机文件: " + fileSuffix);
				
				//查询主机信息
				List<HashMap<String,Object>> hostList=coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", queryMap,FrameConfigKey.DEFAULT_DATASOURCE );
				if(hostList!=null && hostList.size()>0){
					List<String> successHostList = new ArrayList<String>();
					List<String> errorHostList = new ArrayList<String>();
					for (int i = 0; i < hostList.size(); i++) {
						Map<String, Object> host = hostList.get(i);
						
						String  host_path = FileTool.exactPath(StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH"))) + FileTool.exactPath(Constant.Tools) 
								+ FileTool.exactPath(Constant.CONF) + FileTool.exactPath(cluster_type) + fileSuffix;
						log.debug("分发新创建的配置文件， 分发路径: " + host_path + ", 主机IP: " + host.get("HOST_IP"));
						host_path = host_path.replaceAll("/+", "/");
						Trans tran = null;
						ByteArrayInputStream iss = null;
						// 分发到主机的文件目标路径
						try {
							tran = FTPUtils.getFtpInstance(StringTool.object2String(host.get("HOST_IP")), StringTool.object2String(host.get("SSH_USER")),
									DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD"))), ftpDto.getFtpType());
							tran.login();
							if(tran.isExistPath(host_path)){
								log.info(host.get("HOST_IP") + "保存文件：" + host_path+"/"+newFileName);
								
								iss = new ByteArrayInputStream(content.getBytes(DEFAULT_ENCODING));
								tran.put(iss, host_path+"/"+newFileName);
								successHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
								log.info(host.get("HOST_IP") + "已保存文件：");
							}
						} catch (Exception e) {
							errorHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
							log.error("文件分发失败-->", e);
						} finally {
							if (tran != null) {
								tran.close();
							}
							if (iss != null) {
								is.close();
							}
						}
					}
					String msg = "新建成功！";
					if (successHostList.size() > 0) {
						msg += "\n同步到" + StringUtils.join(successHostList, ",") + "成功!";
					}

					if (errorHostList.size() > 0) {
						msg += "\n同步到" + StringUtils.join(errorHostList, ",") + "失败!";
					}
					cont.put("successNum", successHostList.size());
					cont.put("errorNum", errorHostList.size() );
					cont.put("isSuccess", msg);
				}else{
					cont.put("isSuccess", "新建文件成功！但部署主机不存在，尚未同步。");
				}
			} catch (Exception e) {
				log.error("新建文件失败-->" , e);
				cont.put("isSuccess", "新建文件失败！");
				throw new Exception(e.getMessage());
			} finally {
				if (is != null) {
					is.close();
				}
				if (trans != null) {
					trans.close();
				}
			}
		}
		return cont;
	}
	
	/**
	 * 业务创建配置文件
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateCreateServiceFile(Map<String, Object> params) throws Exception {
		log.debug("新建业务配置项， 参数: " + params);
		//选择节点类型
		String fileType = StringTool.object2String(params.get("fileType"));
		//选择节点名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//新创建节点名称
		String newFileName = StringTool.object2String(params.get("newFileName"));
		//文件路径
		String filePath = StringTool.object2String(params.get("filePath"));
		//新创建节点类型
		String newFileType = StringTool.object2String(params.get("newFileType"));
		//拷贝文件名称
		String copyFileName = StringTool.object2String(params.get("copyFilesNames"));
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();

		String path = FileTool.exactPath(filePath);
		log.debug("文件目录:" + path);

		String copyPath = filePath;

		Map<String, Object> cont = new HashMap<String, Object>();
		// 连接用于上传文件或者文件夹
		Trans trans2 = null;
		// 链接用于下载文件列表以及指定文件的内容
		Trans trans = null;
//		String copyName;
		InputStream is = null;

		// 要新建的是文件
		if (newFileType.equals("file")) {
			try {
				trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans.login();
				if (fileType.equals("D")) {// 在目录上创建的
					/*if (Constant.RELEASE.equals(fileName) && getSwitchFileList(FrameConfigKey.DEFAULT_DATASOURCE).contains(newFileName)) {
						path = FileTool.exactPath(path) + newFileName;
						String content = "请编辑内容！";
						// 将内容转成字节流
						is = new ByteArrayInputStream(content.getBytes(DEFAULT_ENCODING));
					} else {*/
						path +=  newFileName; //新创建节点完整路径
						if (!BlankUtil.isBlank(copyFileName)) {
							copyPath += "/" + copyFileName;
							log.debug("新创建文件内容来自文件: " + copyPath);
							// 以流的形式获取到要复制的文件的内容
							is = trans.get(copyPath);
						} else {
	//						Vector<FileRecord> fileList = trans.getFileList(copyPath);
	//						if (fileList.size() > 0) {
	//							// 默认取文件夹下的第一个文件作为复制内容的目标文件
	//							copyName = fileList.get(0).getFileName();
	//							// 要复制内容的文件的地址
	//							copyPath += "/" + copyName;
	//							log.debug("复制内容的文件目录:" + copyPath);
	//							// 以流的形式获取到要复制的文件的内容
	//							is = trans.get(copyPath);
	//						} else {
								String content = "请编辑内容！";
								// 将内容转成字节流
								is = new ByteArrayInputStream(content.getBytes(DEFAULT_ENCODING));
	//						}
						//}
					}
				} else if (fileType.equals("F")) {// 在文件上创建的(拷贝该文件的内容)
					path += newFileName;
					copyPath += "/" + fileName;
					log.debug("复制内容的文件目录:" + copyPath);
					// 以流的形式获取到要复制的文件的内容
					is = trans.get(copyPath);
				}

				// 将内容放入新的文件
				trans2 = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans2.login();
				trans2.put(is, path);
				cont.put("isSuccess", "新建文件成功！");
			} catch (Exception e) {
				log.error("新建文件失败-->" , e);
				cont.put("isSuccess", "新建文件失败！");
				throw new Exception(e.getMessage());
			} finally {
				if (is != null) {
					is.close();
					if (trans != null) {
						trans.completePendingCommand();
					}
				}
				if (trans2 != null) {
					trans2.close();
				}
				if (trans != null) {
					trans.close();
				}
			}
			// 要新建的是文件夹
		} else if (newFileType.equals("folder")) {
			try {
				path +=  newFileName;
				trans2 = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans2.login();
				trans2.mkdir(path);
				cont.put("optFlag", "0");//成功
				cont.put("isSuccess", "新建文件夹成功！");
			} catch (Exception e) {
				cont.put("optFlag", "1");//失败
				log.error("新建文件、文件夹失败-->" , e);
				cont.put("isSuccess", "新建文件夹失败！");
				throw new Exception(e.getMessage());
			} finally {
				if (trans2 != null) {
					trans2.close();
				}
			}
		}
		return cont;
	}
	
	
	
	/**
	 * 业务创建配置文件
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateBatchBussFile(Map<String, Object> params) throws Exception {
		log.debug("批量新增， 参数: " + params);
		//本地网
		String latns = StringTool.object2String(params.get("LATNS"));
		//要copy的文件名
		String copyFileName = StringTool.object2String(params.get("FILE_NAMES"));
		 
		//文件路径
		//String filePath = StringTool.object2String(params.get("FILE_PATH"));
		
		//整个文件路径
		String fullPath = StringTool.object2String(params.get("FULL_PATH"));
		//临时保存目录
		String localPath =StringTool.object2String(params.get("webRootPath")) + Constant.TMP+ System.currentTimeMillis();
		
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();

		// filePath = FileTool.exactPath(filePath);
		log.debug("文件目录:" + fullPath);

	 

		Map<String, Object> cont = new HashMap<String, Object>();
		 
		// 链接用于下载文件列表以及指定文件的内容
		Trans trans = null; 

			try {
				trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans.login();
				
				 String [] ids = latns.split(",");
				 for(String latnid : ids){
					 String path = FileTool.exactPath(FileTool.exactPath(fullPath) +latnid);
					  boolean isExist = trans.isExistPath(path);
					 if(!isExist){
						trans.mkdir(path);
					 }
					 String [] copyNames = copyFileName.split(",");
					 for(String copyName : copyNames){
						 if(StringUtils.isNotBlank(copyName)){
						     String tmpName = FileTool.exactPath(localPath)+copyName;
						     String prefix = copyName.substring(0, copyName.lastIndexOf("."));
							 String suffix = copyName.substring(copyName.lastIndexOf("."),copyName.length());
							 trans.get(FileTool.exactPath(fullPath) +copyName , tmpName);
							 String putFile  = path+ prefix+"_"+ latnid + suffix;
							 trans.put(tmpName, putFile);
						 }
					 }
				 }
				 
				cont.put("isSuccess", "新建文件成功！");
			} catch (Exception e) {
				log.error("新建文件失败-->" , e);
				cont.put("isSuccess", "新建文件失败！");
				throw new Exception(e.getMessage());
			} finally {
				 
				if (trans != null) {
					trans.close();
				}
			}
			 
		
		return cont;
	}
	
	
	/**
	 * 平台：重命名文件
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateRenameFile(Map<String, Object> params,String dbKey) throws Exception {
		log.debug("重命令配置文件名称， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String fileName = params.get("fileName").toString();
		String filePath = params.get("filePath").toString();
		String newFileName = params.get("newFileName").toString();
		String cluster_type=StringTool.object2String( params.get("CLUSTER_TYPE"));
		String cluster_id=StringTool.object2String( params.get("CLUSTER_ID"));
		String cluster_code=StringTool.object2String( params.get("CLUSTER_CODE"));
		String targetPath=StringTool.object2String( params.get("targetPath"));		
		
		//源文件
		String sourceFile = FileTool.exactPath(filePath) + fileName;
		//文件服务器目录
		String targetFile = FileTool.exactPath(filePath) + newFileName;
		//返回值
		Map<String, Object> cont = new HashMap<String, Object>();
		
		Trans trans = null;
		try {
			if (sourceFile.equals(targetFile)) {
				throw new RuntimeException("重命名后的文件名称不能和源文件名称一致，请输入重命名后文件名称！");
			}
			
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			if(!trans.isExistPath(sourceFile)){
				throw new RuntimeException("该文件不存在，不能重命名！");
			}
			//分发目录
			List<HashMap<String, Object>> hostList=coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", params,FrameConfigKey.DEFAULT_DATASOURCE );
			if(!BlankUtil.isBlank(hostList)){
				
				String replaceStr = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(Constant.PLAT_CONF)
						+ Constant.RELEASE_DIR + FileTool.exactPath(cluster_type) + FileTool.exactPath(cluster_code);
				String fileSuffix = targetPath.replace(replaceStr, "");
				log.debug("新增配置文件， 分发到远程主机文件: " + fileSuffix);
				
				List<String> successHostList = new ArrayList<String>();
				List<String> errorHostList = new ArrayList<String>();
				for (int i = 0; i < hostList.size(); i++) {
					Map<String, Object> host = hostList.get(i);
					String deployPath = StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH"));
					
					Trans tran = null;
					String disPath = FileTool.exactPath(deployPath) + FileTool.exactPath(Constant.Tools) 
							+ FileTool.exactPath(Constant.CONF) + FileTool.exactPath(cluster_type) + fileSuffix;
					disPath = disPath.replaceAll("/+", "/");
					String disPath_half = disPath.substring(0, disPath.lastIndexOf("/"));
					log.debug("重命令配置文件， 文件路径: " + disPath);
					
					
					Map<String, Object> renameParam=new HashMap<String, Object>();
					renameParam.put("FILE_PATH", disPath);
					renameParam.put("FILE_PATH_HALF", disPath_half);
					
					//查询是否有正在运行的
					HashMap<String, Object> queryResult = coreService.queryForObject2New("instConfig.queryRunCountByPath", renameParam, dbKey);
					Long RUN_COUNT = Long.valueOf(StringTool.object2String(queryResult.get("RUN_COUNT")));
					if(RUN_COUNT != null && RUN_COUNT>0){
						throw new RuntimeException("该文件已使用，不能重命名！");
					}
					
					// 分发到主机的文件目标路径
					try {
						tran = FTPUtils.getFtpInstance(StringTool.object2String(host.get("HOST_IP")), StringTool.object2String(host.get("SSH_USER")),
								DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD"))),ftpDto.getFtpType());
						
						tran.login();
						if(tran.isExistPath(disPath_half)){
							log.debug("重命名【"+host.get("HOST_IP")+"】文件--start-->目录:"+ disPath);
							tran.rename(disPath, disPath_half+"/"+newFileName);
							log.debug("重命名【"+host.get("HOST_IP")+"】文件--success");
							successHostList.add(StringTool.object2String(host.get("HOST_IP")));
						}
						
					} catch (Exception e) {
						errorHostList.add(StringTool.object2String(host.get("HOST_IP")));
						log.error("重命名文件失败-->" , e);
					} finally {
						if (tran != null) {
							tran.close();
						}
					}
				}
				
				//部署主机配置文件名称变更
				trans.rename(sourceFile, targetFile);
				log.debug("部署主机重命名配置文件成功， 源文件: " + targetFile + ", 目标文件: " + targetFile);
				
				String msg = "文件重命名成功。";
				if (successHostList.size() > 0) {
					msg += "同步到【" + StringUtils.join(successHostList, ",") + "】主机成功!";
				}
				if (errorHostList.size() > 0) {
					msg += "\n同步到【" + StringUtils.join(errorHostList, ",") + "】主机失败!";
				}
				
				cont.put("successNum", successHostList.size());
				cont.put("errorNum", errorHostList.size() );
				cont.put("isSuccess", msg);
				
			}else{
				//文件服务器目录
				trans.rename(sourceFile, targetFile);
				cont.put("isSuccess", "重命名文件成功！但部署主机不存在，尚未同步。");
			}
		} catch (Exception e) {
			log.error("重命名文件失败-->" , e);
			cont.put("isSuccess", "重命名文件失败！");
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return cont;
	}

	/**
	 * 组件:删除配置文件
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateDeleteFile(Map<String, Object> params,String dbKey) throws Exception {
		log.debug("删除配置文件或者目录， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String fileName = StringTool.object2String(params.get("fileName"));
		String filePath = StringTool.object2String(params.get("filePath"));
		String deleteType=StringTool.object2String(params.get("deleteType"));
		String deleteTypeName=StringTool.object2String(params.get("deleteTypeName"));
		String cluster_type=StringTool.object2String(params.get("CLUSTER_TYPE"));
		String cluster_code=StringTool.object2String(params.get("CLUSTER_CODE"));
		String cluster_id=StringTool.object2String(params.get("CLUSTER_ID"));
		String targetPath=StringTool.object2String( params.get("targetPath"));
		String hostIpDir=StringTool.object2String(params.get("hostIpDir"));

		String path = FileTool.exactPath(filePath) + fileName;
		log.debug("删除配置文件， 源文件路径: " + path);
		
		
		Map<String, Object> cont = new HashMap<String, Object>();
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			//删除文件服务器上的
			if(deleteType.equalsIgnoreCase("folder")){
				//查询是否为空文件夹
				Vector<FileRecord> fileList = trans.getFileList(path);
				//不为空返回错误
				if(fileList!=null && fileList.size()>0){
					throw new Exception("该"+deleteTypeName+"存在文件，不能删除！");
				}
				hostIpDir=fileName;
			}
			//String rootPathForepart = rootPath + Constant.CONF + Constant.PLAT_CONF + Constant.RELEASE_DIR;
			
			Map<String,Object> queryMap=new HashMap<String,Object>();
			queryMap.put("CLUSTER_TYPE",cluster_type);
			queryMap.put("CLUSTER_ID", cluster_id );
			if(hostIpDir.contains("_")){
				String hostIp = hostIpDir.substring(0, hostIpDir.indexOf("_"));
				if (ComParamsHelper.isMatchIp(hostIp)) {
					queryMap.put("HOST_IP", hostIp);
				}
			}
			
			String replaceStr = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(Constant.PLAT_CONF)
					+ Constant.RELEASE_DIR + FileTool.exactPath(cluster_type) + FileTool.exactPath(cluster_code);
			String fileSuffix = targetPath.replace(replaceStr, "");
			log.debug("新增配置文件， 分发到远程主机文件: " + fileSuffix);
			
			//查询主机信息
			List<HashMap<String,Object>> hostList=coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", queryMap,FrameConfigKey.DEFAULT_DATASOURCE );			
			if(!BlankUtil.isBlank(hostList)){
				List<String> successHostList = new ArrayList<String>();
				List<String> errorHostList = new ArrayList<String>();
				for (int i = 0; i < hostList.size(); i++) {
					Map<String, Object> host = hostList.get(i);
					String deployPath = FileTool.exactPath(StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH")));
					
					String distPath = deployPath + FileTool.exactPath(Constant.Tools) 
							+ FileTool.exactPath(Constant.CONF) + FileTool.exactPath(cluster_type) + fileSuffix;
					log.debug("删除组件配置文件， 文件目录: " + distPath);
					
					distPath = distPath.replaceAll("/+", "/");
					String disPath_half = distPath.substring(0, distPath.lastIndexOf("/"));
					
					Map<String, Object> deleteParam=new HashMap<String, Object>();
					deleteParam.put("FILE_PATH", distPath);
					deleteParam.put("FILE_PATH_HALF", disPath_half);
					//查询是否有正在运行的
					HashMap<String, Object> queryResult=coreService.queryForObject2New("instConfig.queryRunCountByPath", deleteParam, dbKey);
					Long RUN_COUNT = Long.valueOf(StringTool.object2String(queryResult.get("RUN_COUNT")));
					if(RUN_COUNT != null && RUN_COUNT>0){
						throw new RuntimeException("该"+deleteTypeName+"已使用，不能删除！");
					}
					
					//1.删除数据库
					log.debug("删除数据库表信息--start");
					Map<String, String> delParam=new HashMap<String, String>();
					delParam.put("FILE_PATH", distPath);
					delParam.put("FILE_PATH_HALF", disPath_half);
					coreService.deleteObject("instConfig.deleteConfigByFilePath", delParam, dbKey);
					log.debug("删除数据库表信息--success");
					
					Trans tran = null;
					// 分发到主机的文件目标路径
					try {
						tran = FTPUtils.getFtpInstance(StringTool.object2String(host.get("HOST_IP")), StringTool.object2String(host.get("SSH_USER")),
								DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD"))), ftpDto.getFtpType());
						tran.login();
						if(tran.isExistPath(disPath_half)){
							log.debug("删除【"+host.get("HOST_IP")+"】文件--start-->目录:" + distPath);
							SSHRemoteCmdUtil cmdUtil = new SSHRemoteCmdUtil(StringTool.object2String(host.get("HOST_IP")), StringTool.object2String(host.get("SSH_USER")), DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD"))), null);
							String rstStr = cmdUtil.execMsg("rm -rf "+distPath);
							log.debug("删除远程配置文件， 删除结果: " + rstStr);
							log.debug("删除【"+host.get("HOST_IP")+"】文件--success");
							successHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
						}
					} catch (Exception e) {
						errorHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
						log.error("删除失败-->" , e);
					} finally {
						if (tran != null) {
							tran.close();
						}
					}
				}
				String msg = "删除成功。";
				if (successHostList.size() > 0) {
					msg += "同步到" + StringUtils.join(successHostList, ",") + "成功!";
				}
				if (errorHostList.size() > 0) {
					msg += "\n同步到" + StringUtils.join(errorHostList, ",") + "失败!";
				}
				cont.put("successNum", successHostList.size());
				cont.put("errorNum", errorHostList.size() );
				cont.put("isSuccess", msg);
			}else{
				cont.put("isSuccess", "删除"+deleteTypeName+"成功!但部署主机不存在，尚未同步。");
			}
			
			//2.文件服务器删除操作
			log.debug("删除文件服务器相关文件--start-->路径："+path);
			trans.delete(path);
			log.debug("删除文件服务器相关文件--success");
		} catch (Exception e) {
			log.error("删除"+deleteTypeName+"-->" , e);
			cont.put("isSucess", "删除"+deleteTypeName+"失败，请检查！");
			throw new Exception("配置删除失败，原因：" + e.getMessage());
		}finally{
			if(trans!=null){
				trans.close();
			}
		}
		return cont;
	}

	/**
	 * 删除sentinel目录下的所有实例
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> deleteSentinelInstance(Map<String, Object> params) throws Exception {
		log.debug("删除sentinel目录下的实例， 业务参数: " + params.toString());
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();
		String filePath = params.get("targetPath").toString();
		filePath = filePath.replaceAll("\\(", "\\\\(").replaceAll("\\)","\\\\)");
		Map<String, Object> cont = new HashMap<String, Object>();
		try {
			if (!StringUtils.endsWith(filePath.toLowerCase(), "sentinel")  && !StringUtils.endsWith(filePath.toLowerCase(), "endtinel/")) {
				throw new Exception("危险操作,当前选择的目录(" + filePath + ")无法清空，请确认");
			}

			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			StringBuilder exeCmd = new StringBuilder();
			exeCmd.append("cd ").append(filePath).append(";");
			exeCmd.append("rm -rf `ls | grep -v '^default$'`");
			String rstStr = cmdUtil.execMsg(exeCmd.toString());
			log.debug("删除sentinel目录下的实例， 删除命令: " + exeCmd + ",结果: " + rstStr);
			cont.put("optFlag", "0");//成功
			cont.put("isSuccess", "删除成功！");
		} catch (Exception e) {
			cont.put("optFlag", "1");//失败
			cont.put("isSuccess", "删除失败！");
			log.error("删除sentinel目录下的实例失败" , e);
			throw new Exception("删除配置文件失败，原因：" + e.getMessage());
		}
		return cont;
	}

	/**
	 * 删除Redis目录下的所有实例
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> deleteBatchRedisFile(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("批量删除Redis实例目录， 业务参数: " + params.toString());
		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();
		Map<String, Object> cont = new HashMap<String, Object>();
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			log.info("批量删除Redis实例，登录版本发布服务成功,版本发布服务器主机IP:" + ftpDto.getHostIp()
					+ ", 用户名: " + ftpDto.getUserName() + ",目录: " + ftpDto.getFtpRootPath());

			StringBuffer succBuffer = new StringBuffer();
			String succStr = "";
			String failStr = "";
			List<String> succList = new ArrayList<>();
			List<Map<String, Object>> redisInstList = (List<Map<String, Object>>)params.get("REDIS_INST_LIST");
			if (CollectionUtils.isNotEmpty(redisInstList)) {
				String clusterType = ObjectUtils.toString(redisInstList.get(0).get("CLUSTER_TYPE"));
				String clusterCode = ObjectUtils.toString(redisInstList.get(0).get("CLUSTER_CODE"));
				String redisInstPath = ftpDto.getFtpRootPath() + FileTool.exactPath(Constant.CONF) + FileTool.exactPath(Constant.PLAT_CONF)
						+ Constant.RELEASE_DIR + FileTool.exactPath(clusterType) + FileTool.exactPath(clusterCode) + Constant.REDIS_DIR;

				log.info("批量删除Redis实例，redis实例目录: " + redisInstPath);
				for (Map<String, Object> redisInstMap : redisInstList) {
					String redisInstName = ObjectUtils.toString(redisInstMap.get("fileName"), "");
					String redisPath = redisInstPath + redisInstName;


					try {
						if (redisInstName.equalsIgnoreCase(Constant.T_DEFAULT)) {
							log.debug("批量删除Redis实例，default目录不能删除");
							continue;
						}

						//先删除目录下子文件
						Vector<FileRecord> fileList = trans.getFileList(redisPath);
						if (CollectionUtils.isNotEmpty(fileList)) {
							for (int i=0; i<fileList.size(); i++) {
								String childrenFilePath = FileTool.exactPath(redisPath) + fileList.get(i).getFileName();
								boolean subDelRet = trans.delete(childrenFilePath);
								log.debug("批量删除Redis实例，删除子文件: " + childrenFilePath + ", 结果: " + subDelRet);
							}
						}

						//删除空目录
						boolean delRet = trans.delete(redisPath);
						log.info("批量删除Redis实例，删除版本发布服务器实例目录: " + redisPath + ", 结果:" + delRet);
						succBuffer.append(redisInstName).append(",");
						if (!delRet) {
							failStr += redisInstName + ",";
						} else {
							succList.add(redisInstName);
						}
					} catch (Exception e) {
						log.error("批量删除实例失败，实例目录: " + redisPath);
						failStr += redisInstName + ",";
					}
				}
				if (StringUtils.isNotBlank(succBuffer.toString())) {
					succStr = succBuffer.toString().substring(0, succBuffer.toString().length() - 1);
				}
			}
			if (StringUtils.isNotBlank(failStr)) {
				cont.put("optFlag", "1");//成功
				cont.put("isSuccess", "批量删除Redis实例部分失败,失败实例有: " + (StringUtils.isNotBlank(failStr) ? failStr.substring(0, failStr.length() - 1) : ""));
				cont.put("failStr", failStr);
				cont.put("succList", succList);
			} else {
				cont.put("optFlag", "0");//成功
				cont.put("isSuccess", "批量删除Redis实例成功!");
				cont.put("successFile", succStr);
			}
		} catch (Exception e) {
			cont.put("optFlag", "1");//失败
			cont.put("isSuccess", "批量删除Redis实例全部失败!");
			log.error("批量删除Redis实例失败" , e);
			throw new Exception("批量删除Redis实例失败，原因：" + e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return cont;
	}
	
	/**
	 * 删除业务文件
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateDeleteServiceFile(Map<String, Object> params) throws Exception {
		log.debug("删除业务配置文件或者目录， 业务参数: " + params.toString());
		// 连接服务器的参数
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		String filePath = params.get("filePath").toString();
		filePath = filePath.replaceAll("\\(", "\\\\(").replaceAll("\\)","\\\\)");
		log.debug("删除文件目录:" + filePath);

		Map<String, Object> cont = new HashMap<String, Object>();
		try {
			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
			String cmd = "rm -rf " + filePath;
			String rstStr = cmdUtil.execMsg(cmd);
			log.debug("删除业务配置文件， 删除命令直接结果: " + rstStr);
			cont.put("optFlag", "0");//成功
			cont.put("isSucess", "删除成功！");
		} catch (Exception e) {
			cont.put("optFlag", "1");//失败
			cont.put("isSucess", "删除失败！");
			log.error("删除文件/文件夹失败-->" , e);
			throw new Exception("删除配置文件失败，原因：" + e.getMessage());
		}
		return cont;
	}
	
	/**
	 * 查询对应主机目录下所有的文件列表
	 */
	@Override
	public List loadFileListByFolder(Map<String, Object> params,String dbKey ) throws Exception {
		log.debug("查询远程主机目录下所有的文件列表, 参数为: " + params.toString() + ", dbKey: " + dbKey);
		
		Trans trans = null;
		String fullPath = null;
		List<FileRecord> fileList = new ArrayList<FileRecord>();
		try {
			//获取主机ID
			String hostId = StringTool.object2String(params.get("HOST_ID"));
			//获取根目录
			fullPath = StringTool.object2String(params.get("FILE_PATH"));
			log.debug("远程主机目录: " + fullPath);
			
			//根据主机ID查询主机信息
			Map<String, Object> queryHostMap = new HashMap<String, Object>();
			queryHostMap.put("HOST_ID", hostId);
			Map<String, Object> hostMap = coreService.queryForObject2New("host.queryHostById", queryHostMap, dbKey);
			if (BlankUtil.isBlank(hostMap)) {
				throw new RuntimeException("查询目录下文件列表失败!");
			}
			log.debug("获取远程主机信息成功, 远程主机信息: " + hostMap.toString());
			
			String remoteIp = StringTool.object2String(hostMap.get("HOST_IP"));
			String remoteUserName = StringTool.object2String(hostMap.get("SSH_USER"));
			String remotePasswd = StringTool.object2String(hostMap.get("SSH_PASSWD"));
			if (!BlankUtil.isBlank(remotePasswd)) {
				remotePasswd = DesTool.dec(remotePasswd);
			} else {
				throw new RuntimeException(remoteIp + "主机密码为空, 请检查！");
			}
			trans = FTPUtils.getFtpInstance(remoteIp, remoteUserName, remotePasswd, SessionUtil.getConfigValue("FTP_TYPE"));
			trans.login();
			
			RecursiveFile recursiveFile = new RecursiveFile(fileList);
			recursiveFile.treeList(trans, fullPath, null, null);
			log.debug("获取远程主机目录文件列表成功， 远程主机: " + remoteIp + ", 远程目录: " + fullPath 
						+ ", 文件数量: " + (fileList == null ? 0 : fileList.size()));
		} catch (Exception e) {
			log.error("获取目录文件列表失败,目录为:" + fullPath + ", 失败原因: ", e);
			throw new Exception("获取服务器上【" + fullPath + "】目录下文件列表失败！");
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return fileList;
	}
	
	/**
	 * 查找指定目录下子节点
	 */
	@Override
	public List loadFilesUnderGivenPath(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查找指定目录下文件列表, 业务参数:  " + params.toString() + ", dbKey: " + dbKey);
		Vector<FileRecord> retFiles = null ;
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//目录名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//目录路径
		String filePath = StringTool.object2String(params.get("filePath"));
		//最终文件目录
		String finalPath = FileTool.exactPath(filePath) + fileName;
		log.debug("部署主机查找目录: " + finalPath);
		
		Trans trans = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			// 根据文件路径，查找节点
			retFiles = trans.getFileList(finalPath);
			log.debug("部署主机目录文件列表数量: " + (retFiles == null ? 0 : retFiles.size()));
		} catch (Exception e) {
			log.error("获取部署服务器上 " + finalPath + "目录下文件列表失败, 失败原因: ", e);
			throw new Exception("获取服务器上【" + finalPath + "】目录下子节点失败！");
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return retFiles;
	}
	
	/**
	 * 平台：新建实例
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> addCreateAndCopyFolder(Map<String, Object> params,String dbKey ) throws Exception {
		log.debug("创建实例, 参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String, Object> cont = new HashMap<String, Object>();
		//选中节点名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//创建实例名称
		String newFileName = StringTool.object2String(params.get("newFileName"));
		//复制文件名称 
		String copyFilesNames=StringTool.object2String(params.get("copyFilesNames"));
		String filePath =StringTool.object2String(params.get("filePath"));
		//临时保存目录
		String webRootPath =StringTool.object2String(params.get("webRootPath"));
		
		//集群类型
		String cluster_type =StringTool.object2String(params.get("CLUSTER_TYPE"));
		//集群ID
		String cluster_id =StringTool.object2String(params.get("CLUSTER_ID"));
		//集群编码
		String cluster_code=StringTool.object2String( params.get("CLUSTER_CODE"));
		//String targetPath=StringTool.object2String( params.get("targetPath"));
		String host_id =StringTool.object2String(params.get("HOST_ID"));
		
		// 连接服务器的参数
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		//default路径
		String defaultFilePath = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + Constant.DEFAULT;
		
		//新建实例路径
		String new_path = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + newFileName;
		
		Trans trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
		trans.login();
		
		//判断新增实例是否存在，如果存在则不能新增
		if (trans.isExistPath(new_path)) {
			throw new RuntimeException("新增实例已经存在，请重新输入！");
		}
		
		//部署主机
		ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
		String [] copyFiles = copyFilesNames.split(",");
		for(String copyFile : copyFiles){
			copyFile = copyFile.trim();
			if(!BlankUtil.isBlank(copyFile)){
				cmdUtil.execMsg("mkdir -p "+new_path+"; cp -rf  " + FileTool.exactPath(defaultFilePath) + copyFile+" "+new_path);
			}
		}
		
		//获取当前集群部署主机
		Map<String,Object> queryMap=new HashMap<String,Object>();
		queryMap.put("CLUSTER_TYPE",cluster_type);
		queryMap.put("CLUSTER_ID", cluster_id);
		queryMap.put("HOST_ID", host_id);
		//查询主机信息
		List<HashMap<String,Object>> deployHostList=coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", queryMap,FrameConfigKey.DEFAULT_DATASOURCE );
		if(!BlankUtil.isBlank(deployHostList)){
			List<String> successHostList = new ArrayList<String>();
			List<String> errorHostList = new ArrayList<String>();
			//本地临时保存目录
			String localPath = webRootPath + Constant.TMP + System.currentTimeMillis()  ;
			for (int i = 0; i < deployHostList.size(); i++) {
				Map<String, Object> host = deployHostList.get(i);
				String remotePath = "";
				
				String clusterDeployPath = StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH"));
				if(cluster_code.equals(fileName)){
				   remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type) + newFileName;
				}else{
					remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type) 
							+ FileTool.exactPath(fileName) + newFileName;
				}
				String hostIp = StringTool.object2String(host.get("HOST_IP"));
				String sshUser = StringTool.object2String(host.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD")));
				String ftpType = ftpDto.getFtpType();
				log.debug("远程主机IP: " + hostIp + ", 分发到远程主机路径: " + remotePath);
				
				Trans upTran = null;
				try {
					upTran = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpType);
					upTran.login();
					for(String copyFile : copyFiles){
						copyFile = copyFile.trim();
						if(!BlankUtil.isBlank(copyFile)){
							
							String tempPath = FileTool.exactPath(localPath) + copyFile;
							trans.get(FileTool.exactPath(new_path) + copyFile, tempPath);
							
							upTran.put(tempPath, remotePath+"/"+copyFile);
							new File(tempPath).deleteOnExit();
						}
					}
					log.info(host.get("HOST_IP") + "复制所选文件成功："+remotePath);
					successHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
				} catch (Exception e) {
					errorHostList.add("【集群:"+host.get("CLUSTER_CODE")+" 主机："+host.get("HOST_IP")+" 用户："+host.get("SSH_USER")+" 】");
					log.error("文件复制失败-->", e);
				} finally {
					if (trans != null) {
						trans.close();
					}
					if (upTran != null) {
						upTran.close();
					}
				}
			}
			String msg = "新建实例成功。";
			if (successHostList.size() > 0) {
				msg += "同步到" + StringUtils.join(successHostList, ",") + "成功!";
			}
			if (errorHostList.size() > 0) {
				msg += "\n同步到" + StringUtils.join(errorHostList, ",") + "失败!";
			}
			cont.put("successNum", successHostList.size());
			cont.put("errorNum", errorHostList.size() );
			cont.put("isSuccess", msg);
		}
		
		return cont;
	}

	/**
	 * 平台：批量新建实例
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> addBatchFileAndFolder(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("批量创建实例, 参数: " + params.toString() + ", dbKey: " + dbKey);
		//返回处理结果
		List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
		//选中节点名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//创建实例名称
		String newFileName = StringTool.object2String(params.get("newFileNames"));
		//选中主机IP
		String instHostIp = StringTool.object2String(params.get("hostIp"));

		//复制文件名称
		String copyFilesNames=StringTool.object2String(params.get("copyFilesNames"));
		String filePath =StringTool.object2String(params.get("filePath"));
		//临时保存目录
		String webRootPath =StringTool.object2String(params.get("webRootPath"));

		//集群类型
		String cluster_type =StringTool.object2String(params.get("CLUSTER_TYPE"));
		//集群ID
		String cluster_id =StringTool.object2String(params.get("CLUSTER_ID"));
		//集群编码
		String cluster_code=StringTool.object2String( params.get("CLUSTER_CODE"));
		String host_id =StringTool.object2String(params.get("HOST_ID"));

		// 连接服务器的参数
		FtpDto ftpDto = SessionUtil.getFtpParams();

		//版本发布服务器FTP/SFTP连接对象
		Trans trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
		trans.login();

		//版本发布服务器SSH连接对象
		ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());

		String [] instNameList = null;
		//当文件名称中包含，表示需要批量添加文件
		if (!BlankUtil.isBlank(newFileName) && newFileName.indexOf(",") != -1) {
			instNameList = newFileName.split(",");
		} else {
			instNameList = new String[]{newFileName};
		}
		try {
			for (int x=0; x<instNameList.length; x++) {
				String singleInstanceName = BlankUtil.isBlank(instHostIp) ? instNameList[x].trim() : (instHostIp + "_" + instNameList[x].trim());
				if (BlankUtil.isBlank(singleInstanceName)) {
					continue;
				}
				//获取实例模板文件
				String defaultFilePath = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + Constant.DEFAULT;

				//新建实例路径
				String new_path = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + singleInstanceName;

				//判断新增实例是否存在，如果存在则不能新增
				if (trans.isExistPath(new_path)) {
					throw new RuntimeException("新增实例已经存在，请重新输入！");
				}


				//将版本发布服务器配置文件默认配置复制一份
				String [] copyFiles = copyFilesNames.split(",");
				if (BlankUtil.isBlank(copyFilesNames)) {
					cmdUtil.execMsg("mkdir -p " + new_path);
				} else {
					for(String copyFile : copyFiles){
						copyFile = copyFile.trim();
						if(!BlankUtil.isBlank(copyFile)){
							String execCmd = "mkdir -p " + new_path + "; cp -rf  " + FileTool.exactPath(defaultFilePath) + copyFile + " "+ new_path;
							String retStr = cmdUtil.execMsg(execCmd);
							log.debug("版本发布服务器复制文件命令:" + execCmd + ", 结果:" + retStr);
						}
					}
				}


				//获取当前集群部署主机
				Map<String,Object> queryMap=new HashMap<String,Object>();
				queryMap.put("CLUSTER_TYPE",cluster_type);
				queryMap.put("CLUSTER_ID", cluster_id);
				queryMap.put("HOST_ID", host_id);
				//查询主机信息
				List<HashMap<String,Object>> deployHostList=coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", queryMap,FrameConfigKey.DEFAULT_DATASOURCE );
				log.info("部署主机列表信息:" + deployHostList.toString());

				//DCA sentinel需要修改配置文件信息
				if (CollectionUtils.isNotEmpty(deployHostList)) {
					log.info("当前新增实例信息:fileName:" + fileName + ", 实例名称:" + singleInstanceName);
					if (StringUtils.equals(cluster_type, Constant.DCA) && StringUtils.equalsIgnoreCase(fileName, "sentinel")) {
						for(String copyFile : copyFiles){
							for (int i=0; i<deployHostList.size(); i++) {
								String hostIp = Objects.toString(deployHostList.get(i).get("HOST_IP"),"");
								String hostNetCard = Objects.toString(deployHostList.get(i).get("HOST_NET_CARD"),"");
								if (singleInstanceName.startsWith(hostIp + "_") && !StringTool.isIPV4Legal(hostIp)) {
									ByteArrayInputStream outFileStream = null;
									try {
										//修改该配置文件
										String updateFilePath = FileTool.exactPath(new_path) + copyFile;
										InputStream fileStream = trans.get(updateFilePath);
										String replaceFlagStr = "sentinel monitor";
										String fileContent = FileUtil.readInputStream(fileStream, replaceFlagStr, hostIp, hostNetCard);

										fileContent = StringUtils.defaultString(fileContent, "");
										outFileStream = new ByteArrayInputStream(fileContent.getBytes());
										trans.put(outFileStream, updateFilePath);
										outFileStream.close();
									} finally {
										if (outFileStream != null) {
											try{
												outFileStream.close();
											} catch (Exception e) {

											}
										}
									}
								}
							}
						}
					}
				}


				if(!BlankUtil.isBlank(deployHostList)){
					List<String> successHostList = new ArrayList<String>();
					List<String> errorHostList = new ArrayList<String>();
					//本地临时保存目录
					String localPath = webRootPath + Constant.TMP + System.currentTimeMillis()  ;
					for (int i = 0; i < deployHostList.size(); i++) {
						Map<String, Object> host = deployHostList.get(i);
						String remotePath = "";

						String clusterDeployPath = StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH"));
						if(cluster_code.equals(fileName)){
							remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type) + singleInstanceName;
						}else{
							remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type)
									+ FileTool.exactPath(fileName) + singleInstanceName;
						}
						String hostIp = StringTool.object2String(host.get("HOST_IP"));
						String sshUser = StringTool.object2String(host.get("SSH_USER"));
						String sshPwd = DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD")));
						String ftpType = ftpDto.getFtpType();
						log.debug("远程主机IP: " + hostIp + ", 分发到远程主机路径: " + remotePath);

						Trans upTran = null;
						try {
							//远程主机FTP对象
							upTran = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpType);
							upTran.login();

							for(String copyFile : copyFiles){
								copyFile = copyFile.trim();
								if(!BlankUtil.isBlank(copyFile)){

									String tempPath = FileTool.exactPath(localPath) + copyFile;
									trans.get(FileTool.exactPath(new_path) + copyFile, tempPath);

									upTran.put(tempPath, FileTool.exactPath(remotePath) + copyFile);
									new File(tempPath).deleteOnExit();
								}
							}
							log.info(host.get("HOST_IP") + "复制所选文件成功："+remotePath);
							successHostList.add("【集群:"+host.get("CLUSTER_CODE")+"， 主机："+host.get("HOST_IP")+"， 用户："+host.get("SSH_USER")+" 】");
						} catch (Exception e) {
							errorHostList.add("【集群:"+host.get("CLUSTER_CODE")+"， 主机："+host.get("HOST_IP")+"， 用户："+host.get("SSH_USER")+" 】");
							log.error("文件复制失败-->", e);
						} finally {
							if (upTran != null) {
								upTran.close();
							}
						}
					}
					String msg = "新建【" + singleInstanceName +"】实例成功。";
					if (successHostList.size() > 0) {
						msg += "同步到" + StringUtils.join(successHostList, ",") + "成功!";
					}
					if (errorHostList.size() > 0) {
						msg += "\n同步到" + StringUtils.join(errorHostList, ",") + "失败!";
					}
					Map<String, Object> rstMap = new HashMap<String, Object>();
					rstMap.put("instName", singleInstanceName);
					rstMap.put("successNum", successHostList.size());
					rstMap.put("successList", successHostList);

					rstMap.put("errorNum", errorHostList.size());
					rstMap.put("errorList", errorHostList);
					rstMap.put("isSuccess", msg);
					rstList.add(rstMap);
				}
			}
		} finally {
			if (trans != null) {
				trans.close();
			}
		}

		Map<String, Object> finalRstMap = new HashMap<String, Object>();
		if (!BlankUtil.isBlank(rstList)) {
			int errorTotal = 0;
			List<String> errorTotalList = new ArrayList<String>();
			List<String> successTotalList = new ArrayList<String>();

			int instTotal = rstList.size();
			for (int i=0; i<instTotal; i++) {
				String errorCount = StringTool.object2String(rstList.get(i).get("errorNum"));
				if (!BlankUtil.isBlank(errorCount) && Integer.parseInt(errorCount) > 0) {
					List<String> errorList = (List<String>) rstList.get(i).get("errorList");
					errorTotalList.addAll(errorList);
					errorTotal++;
				}

				String successCount = StringTool.object2String(rstList.get(i).get("successNum"));
				if (!BlankUtil.isBlank(successCount) && Integer.parseInt(successCount) > 0) {
					List<String> successList = (List<String>) rstList.get(i).get("successList");
					successTotalList.addAll(successList);
				}
			}
			String finalMsg = "本次新增实例共：" + instTotal + "个，其中添加并且分发成功实例：" + (instTotal - errorTotal)
					+ "个，添加失败或者分发失败实例：" + errorTotal + "，分发远程主机个数：" + (errorTotalList.size() + successTotalList.size());

			if (errorTotal > 0 && instTotal != errorTotal) {   //部分失败
				finalMsg += "，下面是添加失败详细信息：" + errorTotalList.toString();
				finalRstMap.put("RST_CODE", "0");
			} else if (instTotal == errorTotal) {   //全部失败
				finalMsg += "，下面是添加失败详细信息：" + errorTotalList.toString();
				finalRstMap.put("RST_CODE", "1");
			} else {
				finalRstMap.put("RST_CODE", "2");
			}
			finalRstMap.put("RST_MSG", finalMsg);
		}
		return finalRstMap;
	}


	/**
	 * 批量新建redis实例
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> createBatchRedisFileAndFolder(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("批量创建redis实例, 参数为: " + params.toString() + ", dbKey: " + dbKey);
		//返回处理结果
		List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
		//选中节点名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//从list中获取相应的值
		List<Map<String, Object>> listData =(List<Map<String, Object>>)params.get("listData");
		Map<String, Object> finalRstMap = new HashMap<String, Object>();
		for(int i = 0;i< listData.size(); i++) {
			Map<String, Object> map = listData.get(i);
			String instHostIp = (String) map.get("hostIp");
			String newFileName = (String) map.get("port");

			//复制文件名称
			String copyFilesNames = StringTool.object2String(params.get("copyFilesNames"));
			String filePath = StringTool.object2String(params.get("filePath"));
			//临时保存目录
			String webRootPath = StringTool.object2String(params.get("webRootPath"));

			//集群类型
			String cluster_type = StringTool.object2String(params.get("CLUSTER_TYPE"));
			//集群ID
			String cluster_id = StringTool.object2String(params.get("CLUSTER_ID"));
			//集群编码
			String cluster_code = StringTool.object2String(params.get("CLUSTER_CODE"));

			Map<String, Object> selectMap = new HashMap<String, Object>();
			selectMap.put("HOST_IP", instHostIp);
			selectMap.put("CLUSTER_CODE", cluster_code);
			List<HashMap<String, Object>> hostIDList = coreService.queryForList2New("deployHome.queryHostIDByHostIP", selectMap, FrameConfigKey.DEFAULT_DATASOURCE);
			String hostID = "";
			if (!BlankUtil.isBlank(hostIDList)) {
				Map<String, Object> hostIDMap = hostIDList.get(0);
				hostID = StringTool.object2String(hostIDMap.get("HOST_ID"));

			// 连接服务器的参数
		    FtpDto ftpDto = SessionUtil.getFtpParams();

			String[] instNameList = null;
			//当文件名称中包含，表示需要批量添加文件
			if (!BlankUtil.isBlank(newFileName) && newFileName.indexOf(",") != -1) {
				instNameList = newFileName.split(",");
			} else {
				instNameList = new String[]{newFileName};
			}
			for (int x = 0; x < instNameList.length; x++) {
				String singleInstanceName = BlankUtil.isBlank(instHostIp) ? instNameList[x].trim() : (instHostIp + "_" + instNameList[x].trim());
				if (BlankUtil.isBlank(singleInstanceName)) {
					continue;
				}
				//获取实例模板文件
				String defaultFilePath = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + Constant.DEFAULT;

				//新建实例路径
				String new_path = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + singleInstanceName;

				Trans trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
				trans.login();

				//判断新增redis实例是否存在，如果存在则不能新增。
				if (trans.isExistPath(new_path)) {
					throw new RuntimeException("新增的redis实例已经存在，请重新选择！");
				}

				//部署主机
				ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword());
				//String [] copyFiles = copyFilesNames.split(",");
				if (BlankUtil.isBlank(copyFilesNames)) {
					cmdUtil.execMsg("mkdir -p " + new_path);
				} else {
					cmdUtil.execMsg("mkdir -p " + new_path + "; cp -rf  " + FileTool.exactPath(defaultFilePath)+ copyFilesNames + " " + new_path);
				}

				//获取当前集群部署主机
				Map<String, Object> queryMap = new HashMap<String, Object>();
				queryMap.put("CLUSTER_TYPE", cluster_type);
				queryMap.put("CLUSTER_ID", cluster_id);
				queryMap.put("HOST_ID", hostID);
				//查询主机信息
				List<HashMap<String, Object>> deployHostList = coreService.queryForList2New("deployHome.queryDeployHostAllCodeList", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
				if (!BlankUtil.isBlank(deployHostList)) {
					List<String> successHostList = new ArrayList<String>();
					List<String> errorHostList = new ArrayList<String>();
					//本地临时保存目录
					String localPath = webRootPath + Constant.TMP + System.currentTimeMillis();
					for (int j = 0; j < deployHostList.size(); j++) {
						Map<String, Object> host = deployHostList.get(j);
						String remotePath = "";

						String clusterDeployPath = StringTool.object2String(host.get("CLUSTER_DEPLOY_PATH"));
						if (cluster_code.equals(fileName)) {
							remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type) + singleInstanceName;
						} else {
							remotePath = FileTool.exactPath(clusterDeployPath) + Constant.Tools + Constant.CONF + FileTool.exactPath(cluster_type)
									+ FileTool.exactPath(fileName) + singleInstanceName;
						}
						String hostIp = StringTool.object2String(host.get("HOST_IP"));
						String sshUser = StringTool.object2String(host.get("SSH_USER"));
						String sshPwd = DesTool.dec(StringTool.object2String(host.get("SSH_PASSWD")));
						String ftpType = ftpDto.getFtpType();
						log.debug("远程主机IP: " + hostIp + ", 分发到远程主机路径: " + remotePath);

						Trans upTran = null;
						try {
							upTran = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpType);
							upTran.login();
							if (!BlankUtil.isBlank(copyFilesNames)) {
								String tempPath = FileTool.exactPath(localPath) + copyFilesNames;
								trans.get(FileTool.exactPath(new_path) + copyFilesNames, tempPath);

								upTran.put(tempPath, FileTool.exactPath(remotePath) + copyFilesNames);
								new File(tempPath).deleteOnExit();
							}
							log.info(host.get("HOST_IP") + "复制所选文件成功：" + remotePath);
							successHostList.add("【集群:" + host.get("CLUSTER_CODE") + " 主机：" + host.get("HOST_IP") + " 端口：" + newFileName + " 】");
						} catch (Exception e) {
							errorHostList.add("【集群:" + host.get("CLUSTER_CODE") + " 主机：" + host.get("HOST_IP") + " 端口：" + newFileName + " 】");
							log.error("文件复制失败-->", e);
						} finally {
							if (trans != null) {
								trans.close();
							}
							if (upTran != null) {
								upTran.close();
							}
						}
					}
					String msg = "新建【" + singleInstanceName + "】实例成功。";
					if (successHostList.size() > 0) {
						msg += "同步到" + StringUtils.join(successHostList, ",") + "成功!";
					}
					if (errorHostList.size() > 0) {
						msg += "\n同步到" + StringUtils.join(errorHostList, ",") + "失败!";
					}

					Map<String, Object> rstMap = new HashMap<String, Object>();
					rstMap.put("instName", singleInstanceName);
					rstMap.put("successNum", successHostList.size());
					rstMap.put("errorNum", errorHostList.size());
					rstMap.put("errorList", errorHostList);
					rstMap.put("isSuccess", msg);
					rstList.add(rstMap);
				} else {
					finalRstMap.put("RST_CODE", "2");
					finalRstMap.put("RST_MSG", "该集群尚未部署任何主机，版本发布服务器文件同步成功！");
					//return finalRstMap;
				}
			}

			}else{
				finalRstMap.put("RST_CODE", "1");
				finalRstMap.put("RST_MSG", "该集群尚未部署【"+instHostIp+"】主机，版本发布服务器文件同步失败！");
			}

			if (!BlankUtil.isBlank(rstList)) {
				int errorTotal = 0;
				List<String> errorTotalList = new ArrayList<String>();

				int instTotal = rstList.size();
				for (int k = 0; k < instTotal; k++) {
					String errorCount = StringTool.object2String(rstList.get(k).get("errorNum"));
					if (!BlankUtil.isBlank(errorCount) && Integer.parseInt(errorCount) > 0) {
						List<String> errorList = (List<String>) rstList.get(k).get("errorList");
						errorTotalList.addAll(errorList);
						errorTotal++;
					}
				}
				String finalMsg = "本次新增实例共：" + instTotal + "个，其中添加并且分发成功实例：" + (instTotal - errorTotal)
						+ "个，添加失败或者分发失败实例：" + errorTotal;

				if (errorTotal > 0 && instTotal != errorTotal) {   //部分失败
					finalMsg += "\n下面是添加失败详细信息：\n" + errorTotalList.toString();
					finalRstMap.put("RST_CODE", "0");
				} else if (instTotal == errorTotal) {   //全部失败
					finalMsg += "\n下面是添加失败详细信息：\n" + errorTotalList.toString();
					finalRstMap.put("RST_CODE", "1");
				} else {
					finalRstMap.put("RST_CODE", "2");
				}
				finalRstMap.put("RST_MSG", finalMsg);
			}
		}
		return finalRstMap;
	}

	/**
	 * 获取版本切换配置文件列表
	 * @param dbKey
	 * @return
	 */
	private List<String> getSwitchFileList(String dbKey) {
		log.debug("获取版本切换对应的配置文件文件列表， dbKey： " + dbKey);
		List<String> configList = new ArrayList<String>();
		List<HashMap<String, Object>> switchConfigList = coreService.queryForList2New("masterStandby.querySwitchConfigList", null, dbKey);
		if (!BlankUtil.isBlank(switchConfigList)) {
			for (int i=0; i<switchConfigList.size(); i++) {
				HashMap<String, Object> switchMap = switchConfigList.get(i);
				configList.add(StringTool.object2String(switchMap.get("SWITCH_CONFIG_FILE")));
			}
		}
		log.debug("获取版本切换配置文件，配置文件有： " + configList);
		return configList;
	}
	
	/**
	 * 获取版本切换配置文件列表
	 * @param dbKey
	 * @return
	 */
	private List<HashMap<String, Object>> getSwitchFileDetailList(String dbKey) {
		log.debug("获取版本切换对应的配置文件文件列表， dbKey： " + dbKey);
		List<HashMap<String, Object>> switchConfigList = coreService.queryForList2New("masterStandby.querySwitchConfigList", null, dbKey);
		log.debug("获取版本切换配置文件列表，列表数量： " + (switchConfigList == null ? 0 : switchConfigList.size()));
		return switchConfigList;
	}
	
	/**
	 * 新增文件或者目录
	 * 
	 * @param params 业务参数
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, String> addFile(Map<String, Object> params) throws Exception {
		log.debug("新增文件或者目录, 业务参数: " + params.toString());
		
		// 连接服务器的参数
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		//文件名称
		String fileName = StringTool.object2String(params.get("fileName"));
		//文件目录
		String filePath = FileTool.exactPath(StringTool.object2String(params.get("filePath")));
		String path = filePath + fileName;
		log.debug("新增文件目录:" + path);
		
		String action = StringTool.object2String(params.get("flag"));
		Map<String, String> cont = new HashMap<String, String>();
		try {
			ShellUtils cmdUtil = new ShellUtils(ftpDto.getHostIp(),ftpDto.getUserName(),ftpDto.getPassword());
			
			String cmd = "";
			if(BusinessConstant.PARAMS_BUS_1.equals(action)){
				cmd = "mkdir -p " + path;
			} else if(BusinessConstant.PARAMS_BUS_2.equals(action)){
				cmd = "touch " + path;
			}
			cmdUtil.execMsg(cmd);
			cont.put("optFlag", "0");//成功
			cont.put("isSucess", "新增成功！");
		} catch (Exception e) {
			log.error("新增文件/文件夹失败-->" , e);
			cont.put("optFlag", "1");
			cont.put("isSucess", "新增失败，请检查！");
			throw new Exception("新增失败，原因：" + e.getMessage());
		}
		return cont;
	}

	public static void main(String[] args) {
		 String aa = "/public/dfdsf/cccc/billing/rebalance/aa.txt";
		 System.out.println(aa.lastIndexOf("/billing/"));
		 String pp = aa.substring(aa.lastIndexOf("/billing/")+"/billing/".length(),aa.length());
		 System.out.println(pp.substring(0, pp.lastIndexOf("aa.txt")));
	}

	@Override
	public List loadScriptTree(Map<String, String> params, String dbKey) throws Exception {
		log.debug("获取部署主机配置文件目录， 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		//获取部署主机信息
		 FtpDto ftpDto = SessionUtil.getFtpParams();
		
		String projectRootPath = StringTool.object2String(params.get("projectRootPath"));
		//配置文件类型(业务类型:SERVICE 组件类型:PLATFORM)
		String page_type = params.get("page_type");
		// 读取配置文件中的路径
		String path = "";
		List<FileRecord> allFilesList = new ArrayList<FileRecord>();
		Trans trans = null;
		// 协议判断
		try {
			//登录部署主机
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			
			// 创建一个根目录
			FileRecord rootFile = new FileRecord();
			String rootId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			rootFile.setCurrId(rootId);
			rootFile.setFileName(Constant.RELEASE);
			rootFile.setFilePath(path);
			rootFile.setParentId(Constant.ROOT_NODE_FLAG);
			rootFile.setFileType('D');
			rootFile.setFileLevel("R");
			allFilesList.add(rootFile);
			
			//组件脚本存放目录
			path = ftpDto.getFtpRootPath();
			Vector<FileRecord> fileList = null;
			if(trans.isExistPath(path)){
				fileList = trans.getFileList(path);
			}
			
			// 创建一个组件脚本目录
			FileRecord componentNode = new FileRecord();
			String componentNodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			componentNode.setCurrId(componentNodeId);
			componentNode.setFileName("组件脚本");
			componentNode.setFilePath(path);
			componentNode.setParentId(rootId);
			componentNode.setFileType(FileRecord.DIR);
			allFilesList.add(componentNode);
			if(CollectionUtils.isNotEmpty(fileList)){
				for(int i=0;i<fileList.size();++i){
					FileRecord file = fileList.get(i);
					String fileName = file.getFileName();
					
					//组件配置文件
					if(StringUtils.endsWith(fileName, ".sh")){
						String nodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
						file.setCurrId(nodeId);
						file.setParentId(componentNodeId);
						file.setFileType(FileRecord.FILE);
						
						allFilesList.add(file);
					}
					
				}
			}
			
			//业务脚本存放目录
			String businessPath = ftpDto.getFtpRootPath() + "/business/";
			// 创建一个业务脚本目录
			FileRecord businessNode = new FileRecord();
			String businessNodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
			businessNode.setCurrId(businessNodeId);
			businessNode.setFileName("业务脚本");
			businessNode.setFilePath(businessPath);
			businessNode.setParentId(rootId);
			businessNode.setFileType(FileRecord.DIR);
			allFilesList.add(businessNode);
			
			//业务类型包
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("GROUP_CODE",params.get("GROUP_CODE"));
			List<HashMap<String,Object>> bussinessPackageList = coreService.queryForList2New("config.queryConfigList", param, dbKey);
			
			if(CollectionUtils.isNotEmpty(bussinessPackageList)){
				for(int index=0;index<bussinessPackageList.size();++index){
					HashMap<String,Object> bussinessPackage = bussinessPackageList.get(index);
					String packageType = ObjectUtils.toString(bussinessPackage.get("CONFIG_VALUE"));
					
					FileRecord bussinessPackageNode = new FileRecord();
					String bussinessPackageId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
					bussinessPackageNode.setCurrId(bussinessPackageId);
					bussinessPackageNode.setFileName(ObjectUtils.toString(bussinessPackage.get("CONFIG_NAME")));
					///{rootPath}/business/{PACKAGE_TYPE}
					path = businessPath + ObjectUtils.toString(bussinessPackage.get("CONFIG_VALUE")) + "/";
					bussinessPackageNode.setFilePath(path);
					bussinessPackageNode.setParentId(businessNodeId);
					bussinessPackageNode.setFileType(FileRecord.DIR);
					allFilesList.add(bussinessPackageNode);
					
					fileList.clear();
					if(trans.isExistPath(path + "/release/")){
						fileList = trans.getFileList(path + "/release/");
					}
					if(CollectionUtils.isNotEmpty(fileList)){
						for(int i=0;i<fileList.size();++i){
							FileRecord file = fileList.get(i);
							if(file.isDirectory()){
								path = file.getFilePath() + "/" + file.getFileName() + "/bin/";
								
								if(!trans.isExistPath(path)){
									continue;
								}
								
								Vector<FileRecord> scriptFileList = trans.getFileList(path);
								if(CollectionUtils.isNotEmpty(scriptFileList)){
									// 创建一个业务脚本目录
									FileRecord scriptNode = new FileRecord();
									String scriptNodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
									scriptNode.setCurrId(scriptNodeId);
									scriptNode.setFileName(file.getFileName());
									scriptNode.setFilePath(file.getFilePath());
									scriptNode.setParentId(bussinessPackageId);
									scriptNode.setFileType(FileRecord.DIR);
									allFilesList.add(scriptNode);
									
									for(int j=0;j<scriptFileList.size();++j){
										FileRecord scriptFile = scriptFileList.get(j);
										String fileName = scriptFile.getFileName();
										
										//组件配置文件
										if(StringUtils.endsWith(fileName, ".sh")){
											String nodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
											scriptFile.setCurrId(nodeId);
											scriptFile.setParentId(scriptNodeId);
											scriptFile.setClusterType(file.getFileName());
											scriptFile.setFileType(FileRecord.FILE);
											scriptFile.setDesc(packageType);//记录业务包类型
											allFilesList.add(scriptFile);
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			
			
			//按照文件名称升序排序
			Collections.sort(allFilesList, new Comparator<FileRecord>() {
	            public int compare(FileRecord left, FileRecord right) {
					FileRecord lrcd = left;
					FileRecord rrcd = right;
					return lrcd.getFileName().compareTo(rrcd.getFileName());
	            }
	        });
			
			//将Billing模板提取到前面
			if (!BlankUtil.isBlank(allFilesList)) {
				//查询业务类型
				Map<String, Object> queryMap = new HashMap<String, Object>();
				queryMap.put("TYPE", BusinessConstant.PARAMS_BUS_3);
				List<HashMap<String, Object>> busList = coreService.queryForList2New("clusterEleDefine.queryClusterEleList", queryMap, dbKey);
				List<String> busTypeList = new ArrayList<String>();
				if (!BlankUtil.isBlank(busList)) {
					for (int i=0; i<busList.size(); i++) {
						String busTypeName = StringTool.object2String(busList.get(i).get("CLUSTER_TYPE"));
						busTypeList.add(busTypeName);
					}
				}
				
				for (int i=0; i<allFilesList.size(); i++) {
					FileRecord currentRecord = allFilesList.get(i);
					String fileName = currentRecord.getFileName();
					if (busTypeList.contains(fileName)) {
						allFilesList.remove(currentRecord);
						allFilesList.add(0, currentRecord);
					}
				}
			}
		} catch (Exception e) {
			log.error("获取服务器上所有文件列表失败-->" , e);
			throw new Exception(e.getMessage());
		} finally {
			if (trans != null) {
				trans.close();
			}
		}
		return allFilesList;
	}

	@Override
	public Map<String, Object> updateSaveScript(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("修改组件/业务脚本文件分发开始， 参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String,Object> resultMap = new HashMap<String,Object>();
		
		 FtpDto ftpDto = SessionUtil.getFtpParams();

		String newContent = StringTool.object2String(params.get("NEW_CONTENT"));
		//前台中将+转义成  %2b,现场偶尔没有将 %2b转化为+，手动转化
		if (StringUtils.isNotBlank(newContent) && newContent.toUpperCase().indexOf("%2B") >0) {
			newContent = newContent.replace("%2b", "+").replace("%2B", "+");
		}
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		String packageType = StringTool.object2String(params.get("PACKAGE_TYPE"));//业务包类型
		String filePath = StringTool.object2String(params.get("FILE_PATH"));
		String fileName = StringTool.object2String(params.get("FILE_NAME"));
		
		//保存脚本文件到中心存储服务器
		Trans trans = null;
		ByteArrayInputStream is = null;
		try{
			trans = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			trans.login();
			String targetPath = filePath + "/" + fileName;
			is = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
			trans.put(is, targetPath);
		}catch(Exception e){
			log.error("推送脚本文件到中央主机失败",e);
			resultMap.put("isSuccess", "推送脚本文件到中央主机失败");
			return resultMap;
		} finally {
			if(trans != null){
				trans.close();
			}
			if (is != null) {
				is.close();
			}
		}
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("CLUSTER_TYPE", clusterType);
		param.put("PACKAGE_TYPE", packageType);
		List<HashMap<String, Object>> hostList = coreService.queryForList2New("deployHome.queryDeployHostByClusterType", param, dbKey);
		if(CollectionUtils.isEmpty(hostList)){
			log.info("没有需要分发脚本的主机");
			resultMap.put("isSuccess", "没有主机需要推送");
			return resultMap;
		} else {
			//推送脚本文件到部署主机
			List<String> successHostList = new ArrayList<String>();
			List<String> errorHostList = new ArrayList<String>();
			for(int i=0;i<hostList.size();++i){
				HashMap<String,Object> hostMap = hostList.get(i);
				String targetPath = FileTool.exactPath(ObjectUtils.toString(hostMap.get("CLUSTER_DEPLOY_PATH")));
				//组件类型 1表示组件  3表示业务
				String clusterEleType = ObjectUtils.toString(hostMap.get("CLUSTER_ELE_TYPE"));
				if(BusinessConstant.PARAMS_BUS_3.equals(clusterEleType)){
					String version = ObjectUtils.toString(hostMap.get("VERSION"));
					targetPath += FileTool.exactPath(Constant.BUSS) + FileTool.exactPath("V" + version) + Constant.BIN + fileName;
				} else {
					targetPath += FileTool.exactPath(Constant.Tools) + fileName;
				}
				
				String HOST_IP = ObjectUtils.toString(hostMap.get("HOST_IP"));
				String SSH_USER = ObjectUtils.toString(hostMap.get("SSH_USER"));
				String SSH_PASSWD = ObjectUtils.toString(hostMap.get("SSH_PASSWD"));
				
				ByteArrayInputStream iss = null;
				try{
					trans = FTPUtils.getFtpInstance(HOST_IP,SSH_USER,DesTool.dec(SSH_PASSWD),ftpDto.getFtpType());
					trans.login();
					iss = new ByteArrayInputStream(newContent.getBytes(DEFAULT_ENCODING));
					trans.put(iss, targetPath);
					successHostList.add(HOST_IP);
					//successHostList.add(HOST_IP + ":" + SSH_PORT + "@" + targetPath + "<br/>");
				}catch(Exception e){
					log.error("推送脚本文件到部署主机失败",e);
					errorHostList.add(HOST_IP);
					//errorHostList.add(HOST_IP + ":" + SSH_PORT + "@" + targetPath + "<br/>");
				} finally {
					if(trans != null){
						trans.close();
					}
					if (iss != null) {
						iss.close();
					}
				}
			}
			
			String msg = "";
			if (successHostList.size() > 0) {
				HashSet<String> uniqueList = new HashSet<String>(successHostList);
				log.info("同步保存到" + StringUtils.join(successHostList, ",") + "成功!");
				msg += "<label style='float:left'>同步成功， 成功主机【"+uniqueList.size()+"】台， 分别为：</label><br/>" + StringUtils.join(uniqueList, "、");
			}
			if (errorHostList.size() > 0) {
				HashSet<String> uniqueList = new HashSet<String>(errorHostList);
				log.error("同步保存到" + StringUtils.join(errorHostList, ",") + "失败!");
				msg += "同步失败，失败主机【"+uniqueList.size()+"】台，分别为：<br/>" + StringUtils.join(uniqueList, "、");
			}
			resultMap.put("successNum", successHostList.size());
			resultMap.put("errorNum", errorHostList.toArray());
			resultMap.put("isSuccess", msg);
			return resultMap;
		}
	}
}
