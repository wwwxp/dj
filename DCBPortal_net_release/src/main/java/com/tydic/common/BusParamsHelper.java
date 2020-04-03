package com.tydic.common;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.Constant;
import com.tydic.util.StringTool;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共用函数
 * @author Yuanh
 *
 */
public class BusParamsHelper {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(BusParamsHelper.class);
	
	/**
	 * 获取 环境变量
	 * @param jstormPath Jstorm组件部署根目录
	 * @param busPath   业务部署根目录
	 * @param busVersion   业务版本
	 * @param dbKey 数据库Key
	 * @param jsVersion Jstorm版本
	 * @return
	 */
	public static String getEnvParam(CoreService coreService, String jstormPath, String jsVersion, String busPath, String busVersion, String dbKey){
		log.debug("获取业务参数信息， Jstorm部署根目录: " + jstormPath + ", 业务部署根目录: " + busPath + ", Jstorm部署版本: " + jsVersion + ", dbKey: " + dbKey + ". 业务部署版本: " + busVersion);
		List<HashMap<String, Object>> envList = coreService.queryForList2New("environments.queryEnvList", null, dbKey);
		String envStr ="";
		if(!BlankUtil.isBlank(envList)){
			for(int i = 0 ; i < envList.size() ;i++){
				Map<String, Object> envMap = envList.get(i);
				String envPath =  StringTool.object2String(envMap.get("ENV_VALUE")).trim();
				envPath = envPath.replaceAll("\\$V", busVersion).replaceAll("\\$P", busPath).replaceAll("\\/\\/", "/").replaceAll("\\$BV", jsVersion).replaceAll("\\$CP", jstormPath);
				if(i == envList.size()-1){
					envStr += StringTool.object2String(envMap.get("ENV_NAME")).trim() +"=" +  envPath;
				}else{
					envStr += StringTool.object2String(envMap.get("ENV_NAME")).trim() +"=" +  envPath+"|";
				}
			}
		}
		log.debug("获取业务参数信息成功， 参数: " + envStr);
		return envStr;
	}
	
	/**
	 * 获取 环境变量
	 * @param jstormPath Jstorm组件部署根目录
	 * @param busPath   业务部署根目录
	 * @param busVersion   业务版本
	 * @param dbKey 数据库Key
	 * @param jsVersion Jstorm版本
	 * @param busClusterId 业务主集群ID
	 * @return
	 */
	public static synchronized String getEnvParam(CoreService coreService, String busClusterId, String jstormPath, String jsVersion, String busPath, String busVersion, String dbKey){
		log.debug("获取业务参数信息， Jstorm部署根目录: " + jstormPath + ", 业务部署根目录: " + busPath + ", Jstorm部署版本: " + jsVersion + ", dbKey: " + dbKey + ". 业务部署版本: " + busVersion);
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("BUS_CLUSTER_ID", busClusterId);
		
		List<HashMap<String, Object>> envList = coreService.queryForList2New("environments.queryRunTopologyEnvList", queryMap, dbKey);
		String envStr ="";
		if(!BlankUtil.isBlank(envList)){
			String m2dbDeployPath = "";
			String m2dbLastVersion = "";
			for(int i = 0 ; i < envList.size() ;i++){
				Map<String, Object> envMap = envList.get(i);
				String envPath =  StringTool.object2String(envMap.get("ENV_VALUE")).trim();
				
				//判断是否包含M2DB环境变量
				if (envPath.indexOf("$MV") != -1 || envPath.indexOf("$MP") != -1) {
					if (BlankUtil.isBlank(m2dbDeployPath) && BlankUtil.isBlank(m2dbLastVersion)) {
						//根据业务主集群查询关联的M2DB集群
						queryMap.put("CLUSTER_TYPE", Constant.M2DB);
						List<HashMap<String, Object>> m2dbList = coreService.queryForList2New("environments.queryM2dbListByBusClusterId", queryMap, dbKey);
						log.debug("当前业务集群ID:" + busClusterId + "，关联M2DB集群：" + (m2dbList == null ? "" : m2dbList.toString()));
						if (!BlankUtil.isBlank(m2dbList)) {
							//查询当前M2DB最新的启动实例
							String m2dbClusterId = StringTool.object2String(m2dbList.get(0).get("CLUSTER_ID"));
							m2dbDeployPath = StringTool.object2String(m2dbList.get(0).get("CLUSTER_DEPLOY_PATH"));
							queryMap.put("M2DB_CLUSTER_ID", m2dbClusterId);
							List<HashMap<String, Object>> m2dbVersionList = coreService.queryForList2New("environments.queryM2dbLastVersionList", queryMap, dbKey);
							log.debug("当前业务集群ID：" + busClusterId + "，关联M2DB集群启动最新版本：" + m2dbVersionList.toString());
							if (!BlankUtil.isBlank(m2dbVersionList)) {
								m2dbLastVersion = StringTool.object2String(m2dbVersionList.get(0).get("VERSION"));
							}
						}
					}
					log.debug("当前业务集群ID：" + busClusterId + "，M2DB根目录：" + m2dbDeployPath + "，M2DB最新版本： " + m2dbLastVersion);
					envPath = envPath.replaceAll("\\$MP", m2dbDeployPath).replaceAll("\\$MV", m2dbLastVersion);
				}
				
				
				envPath = envPath.replaceAll("\\$V", busVersion).replaceAll("\\$P", busPath).replaceAll("\\/\\/", "/").replaceAll("\\$BV", jsVersion).replaceAll("\\$CP", jstormPath);
				if(i == envList.size()-1){
					envStr += StringTool.object2String(envMap.get("ENV_NAME")).trim() +"=" +  envPath;
				}else{
					envStr += StringTool.object2String(envMap.get("ENV_NAME")).trim() +"=" +  envPath+"|";
				}
			}
		}
		log.debug("获取业务参数信息成功， 参数: " + envStr);
		return envStr;
	}
	
	/**
	 * 新增任务程序
	 * @param coreService Service对象
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void insertProgram(CoreService coreService, Map param, String dbKey){
		log.debug("添加任务程序， 参数: " + param + ", dbKey: " + dbKey);
		coreService.insertObject("taskProgram.insertTaskProgram", param, dbKey);
	}

	/**
	 * 新增任务程序(包含主键ID)
	 * @param coreService Service对象
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void insertTaskProgramWithNewId(CoreService coreService, Map param, String dbKey){
		log.debug("添加任务程序， 参数: " + param + ", dbKey: " + dbKey);
		coreService.insertObject("taskProgram.insertTaskProgramWithNewId", param, dbKey);
	}


	/**
	 * 修改任务程序
	 * @param coreService Service对象
	 * @param param
	 * @param dbKey
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void updateProgram(CoreService coreService, Map param, String dbKey){
		log.debug("修改任务程序， 参数: " + param + ", dbKey: " + dbKey);
		coreService.updateObject("taskProgram.updateProgramRunState", param, dbKey);
	}

	/**
	 * 新增或者修改任务程序
	 * @param coreService Service对象
	 * @param param
	 * @param dbKey
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void updateOrInsertProgram(CoreService coreService, Map param, String dbKey) {
		log.debug("添加或者修改任务程序， 参数: " + param + ", dbKey: " + dbKey);
		Map<String,Object> isExistMap = coreService.queryForObject2New("taskProgram.queryBillingOrRentProgramCount", param, dbKey);
		Long count= Long.parseLong(StringTool.object2String(isExistMap.get("SUM")));
		if(count>0){
			log.debug("修改任务程序， 参数: " + param + ", dbKey: " + dbKey);
			coreService.updateObject("taskProgram.updateProgramRunState", param, dbKey);
		}else{
			log.debug("添加任务程序， 参数: " + param + ", dbKey: " + dbKey);
			coreService.insertObject("taskProgram.insertTaskProgram", param, dbKey);
		}
	}
}
