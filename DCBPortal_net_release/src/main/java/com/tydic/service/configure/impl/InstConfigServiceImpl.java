package com.tydic.service.configure.impl;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.configure.InstConfigService;
import com.tydic.util.*;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [InstConfigServiceImpl]     
  * @Description:  [组件状态检查，实例删除实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:10:32]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:10:32]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class InstConfigServiceImpl implements InstConfigService {
	/**
	 * DCAS启停日志
	 */
	private static Logger logger = Logger.getLogger(InstConfigServiceImpl.class);
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 删除停止实例数据
	 * @param params 参数
	 * @param dbKey 数据源
	 * @return Map
	 */
	@Override
	public Map<String, Object> deleteInstConfig(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("删除停止实例，参数: " + params.toString() + ", dbKey: " + dbKey);
		//如果是dmdb，调脚本删除创建的目录
		String deployType = StringTool.object2String(params.get("DEPLOY_TYPE"));
		//启动类型
		String deployFileType = StringTool.object2String(params.get("DEPLOY_FILE_TYPE"));
		//删除实例脚本命令
		final String autoFilePath = StringTool.object2String(params.get("autoFile"));
		//集群部署根目录
		String appRootPath = FileTool.exactPath(StringTool.object2String(params.get("CLUSTER_DEPLOY_PATH")));
		//版本号
		String version = StringTool.object2String(params.get("VERSION"));
		//实例路径
		String instPath = StringTool.object2String(params.get("INST_PATH"));
		//实例ID
		String instId = StringTool.object2String(params.get("INST_ID"));
		//主机ID
		String hostId = StringTool.object2String(params.get("HOST_ID"));
		//返回参数
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Constant.DMDB.equals(deployType) && Constant.MAIN_PATTERN.equals(deployFileType)) {
			//查询当前主模式下是否存在route_pattern或者instance_pattern模式，如果存在则不能删除，必须先删除子模式
			String dmdbPath = appRootPath + Constant.Tools + Constant.CONF + Constant.DMDB_DIR;
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("DMDB_PATH", dmdbPath);
			queryParams.put("DEPLOY_TYPE", Constant.DMDB);
			queryParams.put("INST_ID", instId);
			queryParams.put("HOST_ID", hostId);
			List<HashMap<String, Object>> dmdbList = coreService.queryForList2New("instConfig.queryDmdbMainRefPatternList", queryParams, dbKey);
			if (!BlankUtil.isBlank(dmdbList)) {
				throw new RuntimeException("不能删除当前主模式，请先删除对应Instance模式数据！");
			}
		}
		
		//数据库删除操作标识符
		boolean isSuccess=true;
		if(Constant.DMDB.equals(deployType)){
			isSuccess=false;
			//脚本命令
			String autoFile = "";
			String command="";
			//脚本信息
			String configPath = "";
			if (StringUtils.equals(deployFileType, "sync_pattern")
					|| StringUtils.equals(deployFileType, "mgr_pattern")
					|| StringUtils.equals(deployFileType, "watcher_pattern")
					|| StringUtils.equals(deployFileType, "movesync_pattern")) {
				configPath = FileTool.exactPath("instance_pattern") + instPath;
			} else {
				configPath = FileTool.exactPath(deployFileType) + instPath;
			}
			autoFile = MessageFormat.format(autoFilePath, Constant.DMDB, deployFileType, configPath, version, StringTool.object2String(System.nanoTime()));
			//查询主机信息
			final Map<String, Object> hostMap = coreService.queryForObject2New("host.queryHostList", params, dbKey);
			if(hostMap==null || hostMap.isEmpty()){
				throw new RuntimeException("主机信息获取失败，请检查！");
			}
			
			command += "cd " + appRootPath + Constant.Tools + ";" + autoFile;
		  	ShellUtils cmdUtil = new ShellUtils(String.valueOf(hostMap.get("HOST_IP")),
		  			String.valueOf(hostMap.get("SSH_USER")), DesTool.dec(String.valueOf(hostMap.get("SSH_PASSWD"))));
			 
		  	 String result =  cmdUtil.execMsg(command);
		  	 logger.debug("执行删除DMDB远程目录命令，命令: " + command + ", 执行结果: " + result);
		  	
			 
			 resultMap.put("HOST_ID",String.valueOf(hostMap.get("HOST_ID")));
			 resultMap.put("HOST_IP",String.valueOf(hostMap.get("HOST_IP")));
			 
			 if(result.indexOf(Constant.OPERATOR_FAILED) >=0){
				 resultMap.put(Constant.ERROR,result);
			 }else{
				 isSuccess=true;
			 }
		}
		//数据库操作
		if(isSuccess){
			//删除数据库信息
			Map<String, String> delMap = new HashMap<String, String>();
			delMap.put("INST_ID", StringTool.object2String(params.get("INST_ID")));
			coreService.deleteObject("instConfig.deleteConfigByInstId", delMap, dbKey);
			resultMap.put(Constant.SUCCESS,"删除成功！");
		}
		logger.debug("删除组件实例完成，返回结果: " + resultMap.toString());
		return resultMap;
	}
	
	/**
	 * 查询实例状态记录
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<Map<String, Object>> queryInstConfigTreeData(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询实例配置Tree数据， 业务参数: " + params + ", dbKey: " + dbKey);
		List<Map<String, Object>> clusterTypeList = new ArrayList<Map<String, Object>>();
		
		//查询租户对应的集群信息
		List<HashMap<String, Object>> clusterList = coreService.queryForList2New("serviceType.queryAllDeploy", params, dbKey);
		logger.debug("查询组件集群列表， 查询结果: "  + (clusterList == null ? 0 : clusterList.size()));
		
		if (!BlankUtil.isBlank(clusterList)) {
			//获取集群类型
			HashSet<String> clusterSet = new HashSet<String>();
			for (HashMap<String, Object> clusterMap : clusterList) {
				String clusterType = StringTool.object2String(clusterMap.get("CLUSTER_TYPE"));
				if (!clusterSet.contains(clusterType)) {
					clusterSet.add(clusterType);
				}
			}
			
			for (String clusterTypeStr : clusterSet) {
				Map<String, Object> clusterTypeNodeMap = new HashMap<String, Object>();
				String clusterTypeNodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
				clusterTypeNodeMap.put("PARENT_NODE_ID", null);
				clusterTypeNodeMap.put("NODE_ID", clusterTypeNodeId);
				clusterTypeNodeMap.put("NODE_TEXT", clusterTypeStr);
				clusterTypeNodeMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_1);
				clusterTypeNodeMap.put("CLUSTER_TYPE", clusterTypeStr);
				clusterTypeList.add(clusterTypeNodeMap);
				
				for (HashMap<String, Object> clusterMap : clusterList) {
					String clusterType = StringTool.object2String(clusterMap.get("CLUSTER_TYPE"));
					String clusterID = StringTool.object2String(clusterMap.get("CLUSTER_ID"));
					String clusterName = StringTool.object2String(clusterMap.get("CLUSTER_NAME"));
					if (clusterTypeStr.equals(clusterType)) {
						clusterMap.put("PARENT_NODE_ID", clusterTypeNodeId);
						clusterMap.put("NODE_ID", clusterID);
						clusterMap.put("NODE_TEXT", clusterName);
						clusterMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_2);
						clusterTypeList.add(clusterMap);
					}
				}
			}
		}
		logger.debug("查询实例配置Tree数据结束, 返回记录长度: " + clusterTypeList.size());
		return clusterTypeList;
	}

	/**
	 * 查询实例状态记录
	 * @param params 业务参数
	 * @param dbKey 数据库Key
	 * @return List
	 */
	@Override
	public List<Map<String, Object>> querybusInstConfigTreeData(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询实例配置Tree数据， 业务参数: " + params + ", dbKey: " + dbKey);
		List<Map<String, Object>> clusterTypeList = new ArrayList<Map<String, Object>>();

		//查询租户对应的集群信息
		List<HashMap<String, Object>> clusterList = coreService.queryForList2New("serviceType.queryAllDeploy", params, dbKey);
		logger.debug("查询组件集群列表， 查询结果: "  + (clusterList == null ? 0 : clusterList.size()));

		if (!BlankUtil.isBlank(clusterList)) {
			//获取集群类型
			Map<String, Object> clusterSet = new HashMap<String, Object>();
			clusterSet.put("PARENT_NODE_ID", -1);
			clusterSet.put("NODE_ID", UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
			clusterSet.put("NODE_TEXT", "全部");
			clusterSet.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_1);
			clusterSet.put("CLUSTER_TYPE", "");
			clusterTypeList.add(clusterSet);

			for (HashMap<String, Object> clusterMap : clusterList) {
				Map<String, Object> clusterTypeNodeMap = new HashMap<String, Object>();
				String clusterTypeNodeId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
				clusterTypeNodeMap.put("PARENT_NODE_ID", clusterSet.get("NODE_ID"));
				clusterTypeNodeMap.put("NODE_ID", clusterTypeNodeId);
				clusterTypeNodeMap.put("NODE_TEXT", clusterMap.get("CLUSTER_NAME"));
				clusterTypeNodeMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_2);
				clusterTypeNodeMap.put("CLUSTER_TYPE", clusterMap.get("CLUSTER_TYPE"));
				clusterTypeList.add(clusterTypeNodeMap);
			}

		}
		logger.debug("查询实例配置Tree数据结束, 返回记录长度: " + clusterTypeList.size());
		return clusterTypeList;
	}

	/**
	 * @Description: 查询业务程序启停日志信息
	 * @return Map
	 * @author yuanhao
	 * @date 2019-12-05 17:23
	 */
	@Override
	public Map<String, Object> queryInstConfigLogDetail(Map<String, Object> params, String dbKey) throws Exception {
		logger.debug("查询组件实例程序启停日志信息，参数:" + params + ", dbKey: " + dbKey);

		//处理结果返回信息
		Map<String, Object> retMap = new HashMap<>();
		try {
			String logFilePath = "";
			String instId = ObjectUtils.toString(params.get("INST_ID"),"");
			String clusterType = ObjectUtils.toString(params.get("CLUSTER_TYPE"), "");
			if (StringUtils.isNotBlank(instId)) {
				String logPath = this.getLogPath(clusterType);
				Map<String, Object> logNameMap = this.getLogName(instId, dbKey);
				String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"),"");
				logFilePath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
				logger.info("组件启停操作，日志文件: " + logFilePath);
			} else {
				String clusterId = ObjectUtils.toString(params.get("CLUSTER_ID"));
				String deployType = ObjectUtils.toString(params.get("CLUSTER_TYPE"));

				//获取集群部署根目录
				Map<String, Object> queryClusterMap = new HashMap<String, Object>();
				queryClusterMap.put("CLUSTER_ID", clusterId);
				queryClusterMap.put("CLUSTER_TYPE", deployType);
				Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
				if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
					retMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
					retMap.put("retMsg", "获取日志文件失败，请检查集群根目录配置是否正确!");
					return retMap;
				}
				//组件部署根目录
				final String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));

				//主机ID
				String hostId = ObjectUtils.toString(params.get("HOST_ID"));
				//部署版本
				String version = ObjectUtils.toString(params.get("VERSION"));
				//配置文件目录
				String filePath = ObjectUtils.toString(params.get("INST_PATH"), "");
				String deployFileType = ObjectUtils.toString(params.get("DEPLOY_FILE_TYPE"));
				String startAutoFilePath = appRootPath + Constant.Tools + Constant.CONF + deployType + "/" + filePath;

				//M2DB组件是根据实例ID来生成日志文件
				if (StringUtils.equalsIgnoreCase(deployType, Constant.M2DB)) {
					startAutoFilePath = filePath;
				}

				String logPath = this.getLogPath(deployType);
				logger.info("日志输出目录: " + logPath);

				Map<String, Object> logNameMap = this.getLogName(clusterId, deployType, hostId, version, deployFileType, startAutoFilePath);
				logger.info("日志输出文件信息: " + logNameMap.toString());

				String logShaName = ObjectUtils.toString(logNameMap.get("LOG_NAME_SHA"),"");
				logFilePath = StringUtils.removeEnd(logPath, "/") + "/" + logShaName;
				logger.info("组件启停操作，日志文件: " + logFilePath);
			}

			File file = new File(logFilePath);
			String fileContent = "";
			StringBuffer buffer = new StringBuffer();
			if (file.exists()) {
				FileInputStream inputStream = null;
				InputStreamReader inputStreamReader = null;
				BufferedReader bufferedReader = null;
				try {
					inputStream = new FileInputStream(file);
					inputStreamReader = new InputStreamReader(inputStream);
					bufferedReader = new BufferedReader(inputStreamReader);
					String lineStr = null;
					while ((lineStr = bufferedReader.readLine()) != null) {
						buffer.append(lineStr).append("\n");
					}
					fileContent = new String(buffer.toString().getBytes(LoggerUtils.LOG_ENCODING_GBK), LoggerUtils.DEFAULT_LOG_ENCODING);
					fileContent = fileContent.replace("[ERROR]", "<label style='color:red;'>[ERROR]</label>").replace("[ WARN]", "<label style='color:yellow;'>[ WARN]</label>");
					retMap.put("retCode", BusinessConstant.PARAMS_BUS_1);
					retMap.put("retMsg", fileContent);
				} catch (Exception e) {
					logger.error("解析日志文件失败， 失败原因:", e);
					retMap.put("retCode", BusinessConstant.PARAMS_BUS_0);
					retMap.put("retMsg", "日志文件获取失败，请检查!");
				} finally {
					if (bufferedReader != null) {
						bufferedReader.close();
					}
					if (inputStreamReader != null) {
						inputStreamReader.close();
					}
					if (inputStream != null) {
						inputStream.close();
					}
				}
			} else {
				retMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
				retMap.put("retMsg", "该实例尚未启停过，无上次操作日志!");
			}
		} catch (Exception e) {
			logger.error("日志文件获取失败， 失败原因: ", e);
			retMap.put("retCode", BusinessConstant.PARAMS_BUS_0);
			retMap.put("retMsg", "日志文件获取失败，请检查!");
		}
		return retMap;
	}


	/**
	* @Description: 查询日志文件名称
	* @return String
	* @author yuanhao
	* @date 2019-12-13 16:15
	*/
	public Map<String, Object> getLogName(String instId, String dbKey) {
		logger.info("获取组件启停日志文件名称， 参数: instId:" + instId + ", dbKey: " + dbKey);
		Map<String, Object> retMap = new HashMap<>();
		//根据inst_id字段查询实例表数据
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("INST_ID", instId);
		Map<String, Object> instMap = coreService.queryForObject2New("instConfig.queryInstConfigById", queryMap, dbKey);
		String clusterId = ObjectUtils.toString(instMap.get("CLUSTER_ID"), "");
		String clusterType = ObjectUtils.toString(instMap.get("DEPLOY_TYPE"), "");
		String hostId = ObjectUtils.toString(instMap.get("HOST_ID"), "");
		String version = ObjectUtils.toString(instMap.get("VERSION"), "");
		String deployFileType = ObjectUtils.toString(instMap.get("DEPLOY_FILE_TYPE"), "");
		String startAutoFilePath = ObjectUtils.toString(instMap.get("FILE_PATH"));
		String instPath = ObjectUtils.toString(instMap.get("INST_PATH"));
		String logInitName = "";
		if (StringUtils.equalsIgnoreCase(clusterType, Constant.M2DB)) {
			logInitName = clusterId + "#" + hostId + "#" + version + "#" + deployFileType + "#" + instPath;
		} else {
			logInitName = clusterId + "#" + hostId + "#" + version + "#" + deployFileType + "#" + startAutoFilePath;
		}
		String logShaName = SHATool.getSHA1StrJava(logInitName);
		logShaName += "_" + deployFileType + ".log";
		retMap.put("LOG_NAME_STR", logInitName);
		retMap.put("LOG_NAME_SHA", logShaName);
		logger.info("获取组件启停日志文件名称，返回结果： " + retMap.toString());
		return retMap;
	}

	/**
	 * @Description: 查询日志文件名称
	 * @param  clusterId
	 * @param  clusterType
	 * @param  hostId
	 * @param version
	 * @param  deployFileType
	 * @param startAutoFilePath
	 * @return String
	 * @author yuanhao
	 * @date 2019-12-13 16:15
	 */
	public Map<String, Object> getLogName(String clusterId, String clusterType, String hostId, String version, String deployFileType, String startAutoFilePath) {
		logger.info("获取组件启停日志文件名称， 参数: clusterId:" + clusterId + ", clusterType: " + clusterType + ", hostId: " + hostId + ", version: " + version + ", deployFileType: " + deployFileType + ", startAutoFilePath: " + startAutoFilePath);
		Map<String, Object> retMap = new HashMap<>();
		if (StringUtils.isNotBlank(startAutoFilePath) && startAutoFilePath.indexOf("//") > -1) {
            startAutoFilePath = startAutoFilePath.replace("//","/");
        }
		String logInitName = clusterId + "#" + hostId + "#" + version + "#" + deployFileType + "#" + startAutoFilePath;
		String logShaName = SHATool.getSHA1StrJava(logInitName);
		logShaName += "_" + deployFileType + ".log";
		retMap.put("LOG_NAME_STR", logInitName);
		retMap.put("LOG_NAME_SHA", logShaName);
		logger.info("获取组件启停日志文件名称，返回结果： " + retMap.toString());
		return retMap;
	}



	/**
	* @Description: TODO
	* @return String 日志文件目录
	* @author yuanhao
	* @date 2019-12-13 16:11
	*/
	public String getLogPath(String clusterType) {
		//获取日志文件输出目录
		String logPath = SystemProperty.getContextProperty(Constant.COMPONENT_TASK_LOG_PATH);
		if (StringUtils.isBlank(logPath)) {
			logPath = "../logs/" + clusterType + "/";
		}
		logger.info("获取组件启停日志文件目录，返回结果: " + logPath);
		return logPath;
	}
}
