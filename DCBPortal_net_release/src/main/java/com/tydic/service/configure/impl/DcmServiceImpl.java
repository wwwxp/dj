package com.tydic.service.configure.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.service.CoreService;
import com.tydic.common.BusParamsHelper;
import com.tydic.service.configure.DcmService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.ShellUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [DcmServiceImpl]     
  * @Description:  [采集程序启停管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-19 上午8:36:46]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-19 上午8:36:46]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class DcmServiceImpl implements DcmService {
	
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;
	
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DcmServiceImpl.class);
	
	//常量
	private static final String RST_STATE = "state";

	/**
	 * 采集r程序启停
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateRunAndStopHost(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("采集程序启停， 业务参数: " + param.toString() + ", dbKey: " + dbKey);
		//返回对象
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//业务集群ID
		String clusterId = StringTool.object2String(param.get("CLUSTER_ID"));
		//业务集群类型
		String clusterType = StringTool.object2String(param.get("CLUSTER_TYPE"));
		//获取当前业务集群部署路径
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("获取业务集群信息失败, 请检查！");
		}
		//业务组件部署根目录
		String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
		
		//需要启停的主机列表
		List<Map<String, Object>> paramList = (List<Map<String, Object>>) param.get("HOST_LIST");
		//当前业务程序启停版本目录
		String versionDir = StringTool.object2String("V" + paramList.get(0).get("VERSION")) + "/";
		 
		//当前主机信息
		Map<String, Object> hostMap = null;
		//命令执行返回结果
		String resultStr = "";
		//当前程序操作类型
		String runAndStop = "启动";
		
		 try {
			// 远程主机登录ssh
			hostMap = coreService.queryForObject2New("host.queryHostList", paramList.get(0), dbKey);
			String hostIp = StringTool.object2String(hostMap.get("HOST_IP"));
			String hostUser = StringTool.object2String(hostMap.get("SSH_USER"));
			String hostPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
			ShellUtils cmdUtil = new ShellUtils(hostIp, hostUser, hostPwd);
			
			for (Map<String, Object> paramMap : paramList) {
				// 查询出数据库得到脚本名称
				//HashMap<String, Object> resultMap = coreService.queryForObject2New("program.queryProgramList", paramMap, dbKey);
				String shName = StringTool.object2String(paramMap.get("SCRIPT_SH_NAME"));
				
				// 周边程序启动命令
				String cmd = Constant.SERVICE_SH;
				String currentAction = "";
				if (BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag"))) {
					currentAction = "start";
				} else if (BusinessConstant.PARAMS_STOP_FLAG.equals(param.get("flag"))) {
					currentAction = "stop";
				}
				//业务程序配置文件路径
				String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG;
				//业务程序路径
				String busPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.BIN;
				//业务程序脚本
				String exShName = shName;
				//业务程序启动类型
				String busAction = currentAction;
				String execCmd = MessageFormat.format(cmd, busPath, exShName, busAction, "");
				execCmd = execCmd.replace("$P", busCfgPath);
				log.debug("采集程序启停命令: " + execCmd);
				
				// 执行命令返回结果
				resultStr = cmdUtil.execMsg(execCmd);
				log.debug("采集程序启停结果: " + resultStr);
				
				if (resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >= 0
						|| resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >= 0) {
					resultStr = resultStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
					throw new RuntimeException(resultStr);
				}

				Map<String,String> updateMap = new HashMap<String,String>();
				updateMap.put("RUN_STATE", StringTool.object2String(param.get("RUN_STATE")));
				updateMap.put("HOST_ID", StringTool.object2String(paramMap.get("HOST_ID")));
				updateMap.put("TASK_ID", StringTool.object2String(paramMap.get("TASK_ID")));
				updateMap.put("PROGRAM_NAME", StringTool.object2String(paramMap.get("PROGRAM_NAME")));
				if(BusinessConstant.PARAMS_START_FLAG.equals(param.get("flag"))) {
					String id = StringTool.object2String(paramMap.get("ID"));
					if (BlankUtil.isBlank(id) || BusinessConstant.PARAMS_UNDEFINED.equals(id)) {
						paramMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
						BusParamsHelper.insertProgram(coreService, paramMap, dbKey);
					} else {
						// 更新状态
						updateMap.put("ID", StringTool.object2String(paramMap.get("ID")));
						BusParamsHelper.updateProgram(coreService, updateMap, dbKey);
					}
				 } else {
					 updateMap.put("ID", StringTool.object2String(paramMap.get("ID")));
					 BusParamsHelper.updateProgram(coreService, updateMap,dbKey);
					 runAndStop = "停止";
				 }
				 returnMap.put("info", runAndStop+"成功");
				 returnMap.put("reason", "主机【"+StringTool.object2String(hostMap.get("HOST_IP")+"】"+runAndStop+"成功！"));
				 returnMap.put("flag", "success");
			}
		} catch (Exception e) {
			log.error("采集程序启停失败， 失败原因: ", e);
			returnMap.put("info", "主机【"+StringTool.object2String(hostMap.get("HOST_IP")+"】"+runAndStop+"失败"));
			returnMap.put("reason", resultStr);
			returnMap.put("flag", BusinessConstant.PARAMS_RST_ERROR);
		}
		 return returnMap;
	}

	/**
	 * 检查采集程序运行状态
	 * @param param 业务参数
	 * @param dbKey 数据库Key
	 * @return Map 返回对象
	 */
	@Override
	public Map<String, Object> updateCheckHostState(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("检查采集程序运行状态, 参数: " + params.toString() + ", dbKey: " + dbKey);
		//业务集群ID
		String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
		//业务集群类型
		String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
		//获取当前业务集群部署路径
		Map<String, Object> queryClusterMap = new HashMap<String, Object>();
		queryClusterMap.put("CLUSTER_ID", clusterId);
		queryClusterMap.put("CLUSTER_TYPE", clusterType);
		Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			throw new RuntimeException("获取业务集群信息失败, 请检查！");
		}
		//业务组件部署根目录
		String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
				
		//当前程序运行状态
		String runState = StringTool.object2String(params.get("RUN_STATE"));
		//程序运行版本目录
		String versionDir = StringTool.object2String("V" + params.get("VERSION")) + "/";
		
		//状态检查执行返回结果对象
		Map<String, Object> result = new HashMap<String, Object>();
		// 如果页面传递记录中没有HOST_ID,则说明该程序未在该主机上
		if (BlankUtil.isBlank(StringTool.object2String(params.get("HOST_ID")))) {
			result.put(RST_STATE, BusinessConstant.PARAMS_BUS_3);
			return result;
		}
		
		try {
			// 远程主机登录ssh
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("HOST_ID", params.get("HOST_ID"));
			Map<String, Object> hostMap = coreService.queryForObject2New("host.queryHostList", queryMap, dbKey);
			String hostIp = StringTool.object2String(hostMap.get("HOST_IP"));
			String hostUser = StringTool.object2String(hostMap.get("SSH_USER"));
			String hostPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
			ShellUtils cmdUtil = new ShellUtils(hostIp, hostUser, hostPwd);
			
			// 查询出数据库得到脚本名称
			//HashMap<String,Object> resultMap = coreService.queryForObject2New("program.queryProgramList", params, dbKey);
			String shName = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
			
			// 组装命令
			String cmd = Constant.SERVICE_SH;
			//业务程序配置文件路径
			String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG;
			//业务程序路径
			String busPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.BIN;
			//业务程序脚本
			String exShName = shName;
			//业务程序启动类型
			String busAction = "check";
			//业务程序版本目录
			String busVerPath = FileTool.exactPath(appRootPath) + Constant.BUSS + versionDir;
			//检查执行命令
			String execCmd = MessageFormat.format(cmd, busPath, exShName, busAction, busVerPath);
			execCmd = execCmd.replace("$P", busCfgPath);
			log.debug("采集程序状态检查, 执行命令: " + execCmd);
			
			// 执行命令返回结果
			String resultStr = cmdUtil.execMsg(execCmd);
			log.debug("采集程序状态检查执行命令结果; " + resultStr);
	
			if(resultStr.toLowerCase().indexOf(Constant.FLAG_ERROR) >=0 
					 || resultStr.toLowerCase().indexOf(ResponseObj.FAILED) >=0){
				resultStr = resultStr.replaceAll(Constant.FLAG_RECOVERT_ERROR, "").replaceAll("((\r\n)|\n)[\\s\t ]*(\\1)+", "$1").replaceAll("^((\r\n)|\n)", "");
				result.put(RST_STATE, BusinessConstant.PARAMS_BUS_4);
				throw new RuntimeException(resultStr);
			}
			
			if(resultStr.toLowerCase().contains("not exists")){  // 检查出未运行
				result.put(RST_STATE, BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
				if(BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(runState)){// 若当前状态为1,则更新状态
					params.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
					BusParamsHelper.updateProgram(coreService, params,dbKey);
					result.put("info", "当前程序未运行,已同步数据库");
				}
			} else {  // 检查出已运行
				result.put(RST_STATE, BusinessConstant.PARAMS_START_STATE_ACTIVE);
				if(!BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(runState)){// 若当前状态为0或Null,则更新状态
					params.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
					BusParamsHelper.updateOrInsertProgram(coreService, params, dbKey);
					result.put("info", "当前程序正在运行,已同步数据库");
				}
				result.put("process", resultStr.substring(resultStr.lastIndexOf("PID=") + 4));
			}
		} catch (Exception e) {
			log.debug("采集执行状态检查失败，失败原因: ", e);
			result.put("info", "检查执行失败！");
			result.put("reason", e.getMessage());
		}
		return result;
	}
}
