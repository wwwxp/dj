package com.tydic.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.tydic.bean.ClusterNodeDto;
import com.tydic.bean.ClusterNodeLogDto;
import com.tydic.bean.FtpDto;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.service.clustermanager.DeployService;
import com.tydic.service.configure.DeployBusTaskService;
import com.tydic.service.configure.JstormStartService;
import com.tydic.service.configure.TopManagerService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.job.impl]    
  * @ClassName:    [ThresholdExpendTask]     
  * @Description:  [集群节点伸缩管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-7 下午2:53:16]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-7 下午2:53:16]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@DisallowConcurrentExecution
public class ClusterNodeManageQuartz implements Job {
	//节点伸缩日志对象
	private static Logger logger = Logger.getLogger(ClusterNodeManageQuartz.class);
	//默认字符集编码UTF-8
	private final String DEFAULT_ENCODING_UTF8 = "UTF-8";
	//调用主机资源URL配置
	private final String GET_HOST_QUOTA_URL = SystemProperty.getContextProperty("host.quota.url");
	//调用主机资源响应结果码
	private final String GET_HOST_QUOTA_RESPONSE_CODE = "200";
	//默认数据源
	private final String dbKey = FrameConfigKey.DEFAULT_DATASOURCE;
	
	private CoreService coreService = (CoreService) SpringContextUtil.getBean("coreService");
	
	//组件部署Service对象
	private DeployService deployService =   (DeployService) SpringContextUtil.getBean("deployServiceImpl");
	
	//Jstorm启动Service对象
	private JstormStartService jstormStartService = (JstormStartService) SpringContextUtil.getBean("jstormStartServiceImpl");
	
	//业务部署Service对象
	private DeployBusTaskService deployBusTaskService = (DeployBusTaskService) SpringContextUtil.getBean("deployBusTaskServiceImpl");
	
	//重新负载Service对象
	private TopManagerService topManagerService = (TopManagerService) SpringContextUtil.getBean("topManagerServiceImpl");

	/**
	 * 定时器触发
	 * @param context
	 * @throws JobExecutionException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//判断条件是否满足触发扩展
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Map<String, Object> jobParams = (Map<String, Object>) dataMap.get(QuartzConstant.BUS_PARAMS);
		if (BlankUtil.isBlank(jobParams) || jobParams.isEmpty()) {
			logger.debug("业务参数为空，无法触发节点伸缩...");
			return;
		}
		
		//获取节点伸缩策略配置
		Map<String, Object> queryCfgParams = new HashMap<String, Object>();
		queryCfgParams.put("STRATEGY_ID", jobParams.get("BUS_ID"));
		Map<String, Object> cfgMap = coreService.queryForObject2New("expendStrategyConfig.queryStrategyConfigById", queryCfgParams, dbKey);
		//查询该图是否在运行中，如果没有 运行， 则不用扩展和收缩，程序退出
		Map<String,Object> taskMap = coreService.queryForObject2New("taskProgram.queryTaskProgramByIdAndRunState", cfgMap, dbKey);
		if(taskMap == null || taskMap.isEmpty()){
			Log.warn("该图没有运行， 不操作");
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(StringTool.object2String(cfgMap.get("STRATEGY_ID")), 
                                  StringTool.object2String(cfgMap.get("OPERATOR_TYPE")), StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "该拓扑图没有运行，任务失败！", BusinessConstant.PARAMS_BUS_0,"未知",null);
			this.addClusterNodeLog(logDto);
			return;
		}
		String isRule = StringTool.object2String(cfgMap.get("IS_RULE"));
		/*if (BusinessConstant.PARAMS_BUS_1.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {         //阀值扩展
			this.initExpendThreshold(cfgMap);
		} else if (BusinessConstant.PARAMS_BUS_2.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {  //阀值收缩
			this.initUnexpendThreshold(cfgMap);
		} else*/ 
		if (BusinessConstant.PARAMS_BUS_3.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))
				||BusinessConstant.PARAMS_BUS_5.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {  //定时扩展
			if(BusinessConstant.PARAMS_BUS_1.equals(isRule)){
				this.initExpendThreshold(cfgMap);//带规则 的扩展
			}else{
				this.initExpendTiming(cfgMap);//定时扩展
			}
			
		} else if (BusinessConstant.PARAMS_BUS_4.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))
				||BusinessConstant.PARAMS_BUS_6.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {  //定时收缩
			if(BusinessConstant.PARAMS_BUS_1.equals(isRule)){
				this.initUnexpendThreshold(cfgMap);//带规则 的收缩
			}else{
				this.initUnexpendTiming(cfgMap);//定时收缩
			}
		} else if (BusinessConstant.PARAMS_BUS_7.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {   //动态扩展
			this.initDynamicExpendTiming(StringTool.object2String(jobParams.get("EXTENDED_FIELD")), cfgMap);//定时扩展
		} else if (BusinessConstant.PARAMS_BUS_8.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {   //动态收缩
			this.initDynamicUnexpendTiming(StringTool.object2String(jobParams.get("EXTENDED_FIELD")), cfgMap);//定时收缩
		}
	}
	
	/**
	 * 动态定时扩展节点
	 * @param expansionReportId 扩展报告ID
	 * @param cfgMap 策略Map对象
	 */
	private void initDynamicExpendTiming(String expansionReportId, Map<String, Object> cfgMap) {
		logger.debug("动态定时扩展节点， 业务参数: " + cfgMap);
		//程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		
		//根据扩展报告ID查询扩展主机列表
//		String backupHosts = "";
//		int hostCount = 0;
//		Map<String, Object> queryMap = new HashMap<String, Object>();
//		queryMap.put("ID", expansionReportId);
//		queryMap.put("EXEC_STATUS", BusinessConstant.PARAMS_BUS_1);
//		List<HashMap<String, Object>> reportList = coreService.queryForList2New("expansionReport.queryExpansionReport", queryMap, dbKey);
//		if (!BlankUtil.isBlank(reportList)) {
//			backupHosts = StringTool.object2String(reportList.get(0).get("HOST_IPS"));
//			String [] hostArray = backupHosts.split(",");
//			hostCount = hostArray.length;
//		}
		
		//备用扩展主机
		String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
		//一次性扩展节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
		String action = "动态节点扩展";
		
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		String programName = StringTool.object2String(cfgMap.get("PROGRAM_NAME"));
		nodeDto.setVersion(programName.substring(programName.lastIndexOf("-")+1,programName.length()));
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		
		try{
			String hosts = this.getTopoHost(taskProgramId);
			logger.debug("动态扩展，获取业务集群部署主机列表成功， 部署主机列表: " + hosts);
			if (!BlankUtil.isBlank(hosts)) {
				nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));
			}
			
			//当前触发扩展策略信息
//			HashMap<String, Object> queryParamsMap = new HashMap<String, Object>();
//			queryParamsMap.put("OPERATOR_TYPE", BusinessConstant.PARAMS_BUS_7);
//			queryParamsMap.put("TASK_PROGRAM_ID", taskProgramId);
//			queryParamsMap.put("CLUSTER_ID", clusterId);
//			queryParamsMap.put("CURR_STARTEGY_ID", cfgMap.get("STRATEGY_ID"));
//			List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", queryParamsMap, dbKey);
//			HttpClientUtil.getRule(GET_HOST_QUOTA_URL, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
//			logger.debug("动态扩展，获取主机指标信息成功， 主机列表: " + nodeDto.getDeployHostIPArray());
//			
//			HttpClientUtil.isKTrigger(ruleList, nodeDto,action);
//			logger.debug("动态扩展，判断当前集群主机是否达到阀值， 只记录日志，不作为节点扩展依据...");
		}catch(Exception e){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "动态扩展，获取运行主机失败， 扩展报告ID: " + expansionReportId + ", 业务参数： " + cfgMap, BusinessConstant.PARAMS_BUS_0,"动态扩展，获取运行主机失败",null);
			this.addClusterNodeLog(logDto);
			//修改扩展报告状态为3：执行失败
			this.updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_3, null);
			Log.error("动态扩展，获取集群主机指标信息失败， 失败原因：", e);
		}
		
		try {
			//设置备用主机
			boolean isHav = this.addBackupHostList(taskProgramId, backupHosts, nodeDto);
			logger.debug("动态扩展，设置业务程序备用主机成功， 新增备用主机: " + backupHosts + ", 业务程序ID: " + taskProgramId); 
			
			 if(isHav){
				 //节点扩展
				 nodeDto.setRuleStr("根据预测结果动态扩展节点");
				 nodeDto.setMsg("根据预测结果动态扩展节点");
				 logger.debug("动态扩展，开始业务程序节点扩展， 业务程序ID: " + taskProgramId + ", 扩展主机台数: " + hostCount + ", 扩展节点依据策略信息: " + nodeDto);
				 this.addAutoExpendNode(taskProgramId, hostCount, nodeDto);
				 logger.debug("动态扩展，业务程序节点扩展完成， 业务程序ID: " + taskProgramId);
				 
				 //修改扩展报告状态为：2 已扩展
				 updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_2, nodeDto.getBackupHostIPArray().toString());
			 }
		} catch (Exception e) {
			logger.error("动态扩展节点失败， 失败原因: ", e);
			this.updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_3, nodeDto.getBackupHostIPArray().toString());
			logger.debug("动态扩展节点失败 ，修改收缩报告状态成功， 报告ID: " + expansionReportId);
		}
	}
	
	/**
	 * 动态定时收缩节点
	 * @param expansionReportId 扩展报告ID
	 * @param cfgMap 策略Map对象
	 */
	private void initDynamicUnexpendTiming(String expansionReportId, Map<String, Object> cfgMap) {
		logger.debug("动态定时收缩节点， 业务参数: " + cfgMap);
		//程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		
		//根据扩展报告ID查询扩展主机列表
//		String backupHosts = "";
//		int hostCount = 0;
//		Map<String, Object> queryMap = new HashMap<String, Object>();
//		queryMap.put("ID", expansionReportId);
//		queryMap.put("EXEC_STATUS", BusinessConstant.PARAMS_BUS_1);
//		List<HashMap<String, Object>> reportList = coreService.queryForList2New("expansionReport.queryExpansionReport", queryMap, dbKey);
//		if (!BlankUtil.isBlank(reportList)) {
//			backupHosts = StringTool.object2String(reportList.get(0).get("HOST_IPS"));
//			String [] hostArray = backupHosts.split(",");
//			hostCount = hostArray.length;
//		}
		
		//备用扩展主机
		String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
		//一次性扩展节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
		String  action ="动态节点收缩";
				
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				//nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		try{
			String hosts = this.getTopoHost(taskProgramId);
			logger.debug("动态收缩，获取业务集群部署主机列表成功， 部署主机列表: " + hosts);
			if (!BlankUtil.isBlank(hosts)) {
				nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));	
			}
			
//			HashMap<String, Object> queryParamsMap = new HashMap<String, Object>();
//			queryParamsMap.put("OPERATOR_TYPE", BusinessConstant.PARAMS_BUS_8);
//			queryParamsMap.put("TASK_PROGRAM_ID", taskProgramId);
//			queryParamsMap.put("CLUSTER_ID", clusterId);
//			queryParamsMap.put("CURR_STARTEGY_ID", cfgMap.get("STRATEGY_ID"));
//			List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", queryParamsMap, dbKey);
//			HttpClientUtil.getRule(GET_HOST_QUOTA_URL, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
//			logger.debug("动态收缩，获取主机指标信息成功， 主机列表: " + nodeDto.getDeployHostIPArray());
//			
//			HttpClientUtil.isSTrigger(ruleList, nodeDto,action);
//			logger.debug("动态收缩，判断当前集群主机是否达到阀值， 只记录日志，不作为节点扩展依据...");
		}catch(Exception e){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,nodeDto.getBackupHostIPArray().toString(), null, "动态收缩，获取运行主机失败，报告ID: " + expansionReportId + ", 业务参数: " + cfgMap, BusinessConstant.PARAMS_BUS_0,"动态收缩，获取运行主机失败",null);
			this.addClusterNodeLog(logDto);
			
			this.updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_3, null);
			Log.error("动态收缩，获取集群主机指标信息失败， 失败原因：", e);
			return;
		}
		
		try {
			//设置收缩节点
			boolean isHav = this.getUnexpendHostList(backupHosts,hostCount , nodeDto);
			logger.debug("动态收缩，设置业务程序备用主机成功， 新增备用主机: " + backupHosts + ", 业务程序ID: " + taskProgramId); 
			if(isHav){
				//自动收缩
				nodeDto.setRuleStr("根据预测结果动态收缩节点");
				 nodeDto.setMsg("根据预测结果动态收缩节点");
				logger.debug("动态收缩，开始业务程序节点扩展， 业务程序ID: " + taskProgramId + ", 扩展主机台数: " + hostCount + ", 扩展节点依据策略信息: " + nodeDto);
				this.addAutoUnexpendNode(taskProgramId,hostCount, nodeDto);
				logger.debug("动态收缩，业务程序节点扩展完成， 业务程序ID: " + taskProgramId);
				
				//判断集群中是否只运行一个图，如果只是一个图，则从集群中删除主机，否则没有动作，保留
				addComponentStop(cfgMap,nodeDto);
				logger.debug("动态收缩，组件运行状态检查， 业务程序ID: " + taskProgramId);
				
				//修改扩展报告状态为：2 已扩展
				updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_2, nodeDto.getBackupHostIPArray().toString());
			}
		} catch (Exception e) {
			logger.error("动态收缩节点失败， 失败原因: ", e);
			this.updateExpansionReport(expansionReportId, BusinessConstant.PARAMS_BUS_3, nodeDto.getBackupHostIPArray().toString());
			logger.debug("动态收缩节点失败 ，修改收缩报告状态成功， 报告ID: " + expansionReportId);
		}
	}

	/**
	 * 修改节点伸缩报告状态
	 * @param expansionReportId 伸缩报告ID
	 * @param execStatus 执行状态   2：执行成功    3：执行失败
	 */
	private void updateExpansionReport(String expansionReportId, String execStatus, String backupHost) {
		logger.debug("修改集群伸缩报告状态,伸缩报告ID： " + expansionReportId + ", 修改状态为: " + execStatus + ", 伸缩扩展成功节点: " + backupHost);
		Map<String, Object> updateParamsMap = new HashMap<String, Object>();
		updateParamsMap.put("ID", expansionReportId);
		updateParamsMap.put("EXEC_STATUS", execStatus);
		updateParamsMap.put("HOST_IPS", backupHost);
		coreService.updateObject2New("expansionReport.updateExpansionReportStatus", updateParamsMap, dbKey);
		logger.debug("修改集群伸缩报告状态成功， 伸缩报告ID: " + expansionReportId);
	}
	
	/**
	 * 定时扩展节点
	 * @param cfgMap
	 */
	public void initExpendTiming(Map<String, Object> cfgMap) {
		logger.debug("节点定时扩展策略配置， 业务参数: " + cfgMap);
		//程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		//备用扩展主机
		String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
		//一次性扩展节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
		String  action ="手动扩展";
		if (BusinessConstant.PARAMS_BUS_3.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))){
			action ="定时扩展";
		}
				
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		String programName = StringTool.object2String(cfgMap.get("PROGRAM_NAME"));
		nodeDto.setVersion(programName.substring(programName.lastIndexOf("-")+1,programName.length()));
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		
		try{
			String hosts = this.getTopoHost(taskProgramId);
			nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));
			cfgMap.put("OPERATOR_TYPE", 1);
			List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", cfgMap, dbKey);
			HttpClientUtil.getRule(GET_HOST_QUOTA_URL, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
			
			HttpClientUtil.isKTrigger(ruleList, nodeDto, action);
			//nodeDto.setMsg(action);
		}catch(Exception e){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "获取主机资源指标失败: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,"主机资源指标获取失败", null);
			this.addClusterNodeLog(logDto);
			Log.error("动态定时节点扩展失败， 失败原因:", e);
		}
		
		//设置备用主机
		boolean isHav = this.addBackupHostList(taskProgramId, backupHosts, nodeDto);
		if(isHav){
			//节点扩展
			this.addAutoExpendNode(taskProgramId, hostCount, nodeDto);
		}
	}

	/**
	 * 定时收缩
	 * @param cfgMap
	 */
	public void initUnexpendTiming(Map<String, Object> cfgMap) {
		logger.debug("节点阀值收缩策略配置， 业务参数: " + cfgMap);
		String  action ="手动收缩";
		if (BusinessConstant.PARAMS_BUS_4.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))){
			action ="定时收缩";
		}
		//程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		//备用扩展主机
		String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
		//一次性扩展节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
				
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				//nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		try{
			String hosts = this.getTopoHost(taskProgramId);
			nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));
			cfgMap.put("OPERATOR_TYPE",2);
			List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", cfgMap, dbKey);
			HttpClientUtil.getRule(GET_HOST_QUOTA_URL, nodeDto, StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT")));
			
			HttpClientUtil.isSTrigger(ruleList, nodeDto,action);
			//nodeDto.setMsg(action);
		}catch(Exception e){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "获取主机资源指标失败: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,"主机资源指标获取失败",null);
			this.addClusterNodeLog(logDto);
			Log.error("获取指标错误",e);
			return;
		}
		//设置收缩节点
		boolean isHav = this.getUnexpendHostList(backupHosts,hostCount , nodeDto);
		if(isHav){
			//自动收缩
			this.addAutoUnexpendNode(taskProgramId,hostCount, nodeDto);
			//判断集群中是否只运行一个图，如果只是一个图，则从集群中删除主机，否则没有动作，保留
			addComponentStop(cfgMap,nodeDto);
		}
		
	}
	/**
	 * 停止
	 * @param cfgMap
	 * @param nodeDto
	 */
	public void addComponentStop(Map<String, Object> cfgMap,ClusterNodeDto nodeDto){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("CLUSTER_ID", cfgMap.get("CLUSTER_ID"));
		 List<HashMap<String,Object>> processList  = coreService.queryForList2New("taskProgram.queryTaskProgramByIdAndRunState", queryMap, dbKey);
		 List<Map<String, String>> stopHostList = new ArrayList<Map<String, String>>();
		 //判断集群中是否只运行一个图，如果只是一个图，则从集群中删除主机，否则没有动作，保留
		 if(processList.size()<=1){
			 List<HashMap<String, Object>>  hostList = nodeDto.getBackupHostList();
			 for(int i = 0 ; i < hostList.size();i++){
				 Map<String,Object> hostMap = hostList.get(i);
				 Map<String,String> tempMap = new HashMap<String,String>();
				 tempMap.put("CLUSTER_ID", StringTool.object2String(cfgMap.get("CLUSTER_ID")));
				 tempMap.put("HOST_ID", StringTool.object2String(hostMap.get("HOST_ID")));
				 HashMap<String,String> instMap = coreService.queryForObject("instConfig.queryInstConfigByClusterIdHostId", tempMap, dbKey);
				 tempMap.put("INST_ID", instMap.get("INST_ID"));
				 tempMap.put("DEPLOY_FILE_TYPE", "supervisor");
				 tempMap.put("autoFile",  Constant.STOP_AUTH_FILE_COMMON);
				 tempMap.put("VERSION", nodeDto.getVersion());
				 tempMap.put("state", "0");
				 tempMap.put("CLUSTER_ID", instMap.get("CLUSTER_ID"));
				 tempMap.put("CLUSTER_ID_BUS", StringTool.object2String(cfgMap.get("CLUSTER_ID")));
				 tempMap.put("INST_PATH", instMap.get("INST_PATH"));
				 tempMap.put("CLUSTER_TYPE", "jstorm");
				 stopHostList.add(tempMap);
			 }
			 try {
				 Map<String,Object> resultCodeMap = jstormStartService.stopJstorm(stopHostList, dbKey);
				if(Constant.RST_CODE_FAILED.equals(resultCodeMap.get(Constant.RST_CODE))){
					logger.error("节点扩展， 组件实例启动出错--->" +resultCodeMap.get(Constant.RST_STR));
					throw new RuntimeException(resultCodeMap.get(Constant.RST_STR).toString());
				}
				logger.debug("节点收缩， 组件实例停止成功...");
			} catch (Exception e) {
				logger.error("节点收缩，组件实例停止失败， 失败原因: ", e);
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(nodeDto.getBackupHostList()), null,  "1、修改收缩配置文件成功\n2、收缩程序启动成功\n3、进程supervisor停止失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
				this.addClusterNodeLog(logDto);
				throw new RuntimeException("节点收缩，组件停止实例失败， 失败原因: " + e.getMessage());
			}
			 coreService.deleteObject("taskProgram.delHostDeployBYHostId", stopHostList, dbKey);
			 coreService.deleteObject("taskProgram.delHostDeployUpgrateBYHostId", stopHostList, dbKey);
			 coreService.deleteObject("taskProgram.updateHostDeployBYHostId", stopHostList, dbKey);
			 ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(nodeDto.getBackupHostList()), null,  "1、修改收缩配置文件成功 \n2、收缩程序启动成功\n3、进程supervisor停止成功\n4、收缩成功并已移除主机\n任务结束 " , BusinessConstant.PARAMS_BUS_1,nodeDto.getMsg(),nodeDto.getRuleStr());
				this.addClusterNodeLog(logDto);
		 }else{
			 ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(nodeDto.getBackupHostList()), null,  "1、修改收缩配置文件成功 \n2、收缩程序启动成功\n3、收缩成功并已移除主机\n任务结束 " , BusinessConstant.PARAMS_BUS_1,nodeDto.getMsg(),nodeDto.getRuleStr());
				this.addClusterNodeLog(logDto);
		 }
		
	}
	
 
	/**
	 * 阀值扩展
	 * @param cfgMap
	 */
	public void initExpendThreshold(Map<String, Object> cfgMap) {
		logger.debug("节点阀值扩展策略配置， 业务参数: " + cfgMap);
		String  action ="手动扩展";
		if (BusinessConstant.PARAMS_BUS_3.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))){
			action ="定时扩展";
		}
		
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				//nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		String programName = StringTool.object2String(cfgMap.get("PROGRAM_NAME"));
		nodeDto.setVersion(programName.substring(programName.lastIndexOf("-")+1,programName.length()));
		//业务程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		cfgMap.put("OPERATOR_TYPE", "1");
		
		List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", cfgMap, dbKey);
		//备用扩展主机
		String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
		
		//一次性扩展节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
		
		if(ruleList !=null && ruleList.size() > 0){
			
			String conditionCount = StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT"));
			//连续次数
			if (BlankUtil.isBlank(conditionCount)) {
				conditionCount = BusinessConstant.PARAMS_BUS_5;
			}
			try{
				String hosts = this.getTopoHost(taskProgramId);
				nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));
				HttpClientUtil.getRule(GET_HOST_QUOTA_URL,nodeDto,conditionCount);
			}catch(Exception e){
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "获取集群节点指标信息失败,任务结束", BusinessConstant.PARAMS_BUS_0,"主机资源指标失败",null);
				this.addClusterNodeLog(logDto);
			}
			//是否触发规则
			boolean isTrigger = HttpClientUtil.isKTrigger(ruleList, nodeDto,action);
			
			if (isTrigger) {
				nodeDto.setMsg(nodeDto.getMsg()+"已触发预警，任务开始\n");
				//设置备用主机
				boolean isHav = this.addBackupHostList(taskProgramId, backupHosts, nodeDto);
				if(isHav){
					//节点扩展
					this.addAutoExpendNode(taskProgramId, hostCount, nodeDto);
				}
				
			} else {
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "未达预警线，任务结束", BusinessConstant.PARAMS_BUS_2,nodeDto.getMsg(),nodeDto.getRuleStr());
				this.addClusterNodeLog(logDto);
			}
			
		}else{
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "未配置阀值，请检查，任务结束", BusinessConstant.PARAMS_BUS_2, "阀值未配置",null);
			this.addClusterNodeLog(logDto);
		}
	}

	/**
	 * 节点收缩
	 * @param cfgMap 收缩配置信息
	 */
	public void initUnexpendThreshold(Map<String, Object> cfgMap) {
		logger.debug("节点定时收缩策略配置， 业务参数: " + cfgMap);
		String  action ="手动收缩";
		if (BusinessConstant.PARAMS_BUS_4.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))){
			action ="定时收缩";
		}
		
		//获取所有集群主机信息
		List<HashMap<String, Object>> clusterHostList = coreService.queryForList2New("expendStrategyConfig.queryClusterNodeList", cfgMap, dbKey);
		ClusterNodeDto nodeDto = new ClusterNodeDto();
		String clusterId = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_ID"));
		String clusterCode = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_CODE"));
		String clusterType = StringTool.object2String(clusterHostList.get(0).get("CLUSTER_TYPE"));
		nodeDto.setStrategyId(StringTool.object2String(cfgMap.get("STRATEGY_ID")));
		nodeDto.setClusterId(clusterId);
		nodeDto.setClusterCode(clusterCode);
		nodeDto.setClusterType(clusterType);
		for (HashMap<String, Object> clusterMap : clusterHostList) {
			if (BlankUtil.isBlank(clusterMap.get("VERSION"))) {
				//nodeDto.getBackupHostList().add(clusterMap);
				//nodeDto.getBackupHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			} else {
				nodeDto.getDeployHostList().add(clusterMap);
				//nodeDto.getDeployHostIPArray().add(StringTool.object2String(clusterMap.get("HOST_IP")));
			}
		}
		String programName = StringTool.object2String(cfgMap.get("PROGRAM_NAME"));
		nodeDto.setVersion(programName.substring(programName.lastIndexOf("-")+1,programName.length()));
		
		//业务程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		
        cfgMap.put("OPERATOR_TYPE", "2");
		
		List<HashMap<String, Object>> ruleList = coreService.queryForList2New("expendStrategyConfig.queryRuleList", cfgMap, dbKey);
		
		//一次性收缩节点台数
		String hostCountStr = StringTool.object2String(cfgMap.get("HOST_COUNT"));
		if (BlankUtil.isBlank(hostCountStr)) {
			hostCountStr = BusinessConstant.PARAMS_BUS_1;
		}
		int hostCount = Integer.parseInt(hostCountStr);
		
		//备用收缩主机
		 String backupHosts = StringTool.object2String(cfgMap.get("BACKUP_HOSTS"));
				
		if(ruleList !=null && ruleList.size() > 0){
			//连续次数
			String conditionCount = StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT"));
			if (BlankUtil.isBlank(conditionCount)) {
				conditionCount = BusinessConstant.PARAMS_BUS_5;
			}
			try{
				//获取主机
				String hosts = this.getTopoHost(taskProgramId);
				nodeDto.setDeployHostIPArray(Arrays.asList(hosts.split(",")));
				HttpClientUtil.getRule(GET_HOST_QUOTA_URL,nodeDto,conditionCount);
			}catch(Exception e){
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "获取集群节点指标信息失败,任务结束", BusinessConstant.PARAMS_BUS_0,"获取主机资源指标失败",null);
				this.addClusterNodeLog(logDto);
				return;
			}
			String execMessage ="";
			boolean isTrigger = HttpClientUtil.isSTrigger(ruleList, nodeDto,action);
			
			if (isTrigger) {
				nodeDto.setMsg(nodeDto.getMsg()+"已触发预警值，任务开始\n");
				//设置要收缩的节点
				boolean isHav = this.getUnexpendHostList(backupHosts, hostCount, nodeDto);
				if(isHav){
					//自动收缩
					this.addAutoUnexpendNode(taskProgramId,hostCount, nodeDto);
					//判断集群中是否只运行一个图，如果只是一个图，则从集群中删除主机，否则没有动作，保留
					addComponentStop(cfgMap,nodeDto);
				}
			
				
			} else {
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_2, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, execMessage+"未达预警值，不进行收缩，任务结束", BusinessConstant.PARAMS_BUS_2,nodeDto.getMsg(),nodeDto.getRuleStr());
				this.addClusterNodeLog(logDto);
			}
			
		}else{
			//未配置阀值，请检查，任务结束
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_2, StringTool.object2String(cfgMap.get("BACKUP_HOSTS")), null, "未配置阀值，请检查，任务结束", BusinessConstant.PARAMS_BUS_2, "阀值未配置", null);
			this.addClusterNodeLog(logDto);
		}
		 
	
	}
	
	/**
	 * 设置要收缩的节点雷彪
	 * @param backupHosts 手动配置收缩节点
	 * @param hostCount 一次性收缩节点数
	 * @param nodeDto 
	 */
	private boolean getUnexpendHostList(String backupHosts,int hostCount,  ClusterNodeDto nodeDto) {
		List<String> list = nodeDto.getDeployHostIPArray();
		 String [] hostIp = backupHosts.split(",");
		 StringBuffer buffer = new StringBuffer();
		 //选出要扩展的机器
		 for(int i = 0 ; i < list.size(); i++){
			 for(int j = 0 ; j < hostIp.length; j++){
				 if(list.get(i).equals(hostIp[j])){
					 buffer.append("'").append(list.get(i)).append("'").append(",");
					 break;
				 }
			 }
			 
		 }
		 if(buffer.length() > 1 && hostIp.length > 0){
			 Map<String, Object> queryParams = new HashMap<String, Object>();
			 queryParams.put("HOST_IPS", buffer.toString().substring(0, buffer.toString().length() - 1));
			 //根据主机IP查询主机信息
		     List<HashMap<String, Object>> backupHostList = coreService.queryForList2New("host.queryHostListByIp", queryParams, dbKey);
		     nodeDto.getBackupHostList().addAll(backupHostList);
			 nodeDto.getBackupHostIPArray().addAll(Arrays.asList((buffer.toString().substring(0, buffer.toString().length() - 1)).split(",")));
			 
			//真实收缩主机数据
			List<HashMap<String, Object>> expendHostList = new ArrayList<HashMap<String, Object>>();
			if (nodeDto.getBackupHostList().size() < hostCount) {
				expendHostList.addAll(nodeDto.getBackupHostList());
			} else {
				for (int i=0; i<hostCount; i++) { 
					expendHostList.add(nodeDto.getBackupHostList().get(i));
				}
			}
			 nodeDto.getBackupHostList().clear();
			 nodeDto.setBackupHostList(expendHostList);
		 }else{
			 nodeDto.getBackupHostList().clear();
			 nodeDto.getBackupHostIPArray().clear();
		 }

		if(nodeDto.getBackupHostIPArray()== null || nodeDto.getBackupHostIPArray().isEmpty()){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,backupHosts, null, "该主机已经收缩，任务不再继续 ", BusinessConstant.PARAMS_BUS_2,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			 return false;
		}
		return true;
		
	}
	
	 
	
	/**
	 * 自动收缩
	 * @param taskProgramId
	 * @param hostCount
	 * @param nodeDto
	 */
	private void addAutoUnexpendNode(String taskProgramId,int hostCount, ClusterNodeDto nodeDto) {
		
		//收缩节点
		List<HashMap<String, Object>> hostQuotaList = nodeDto.getBackupHostList();
		
		//修改配置文件
		//修改Topology配置文件，新增work分配主机列表
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		
		//查询版本发布服务器信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		
		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode) + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		Trans outTrans = null;
		InputStream instram = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			InputStream fileStream = trans.get(realFilePath);
			String buffer = FileUtil.readInputStream(fileStream);
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.trim());
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			
			boolean isFlag =false;
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			int oldHostSize = jsonArray.size();
			int workNum = configJsonObj.getInteger("topology.billing.workgroup.num");
			int index = 0;
			for (HashMap<String, Object> hostMap : hostQuotaList) {
				String hostIp = StringTool.object2String(hostMap.get("HOST_IP"));
				for (Object hostObject : jsonArray) {
					JSONArray hostArray =  (JSONArray) hostObject;
					if (hostArray.get(0).equals(hostIp)) {
						jsonArray.remove(hostObject);
						isFlag = true;
						index ++;
						break;
					}
				}
			}
			int newWorkNum = workNum - (index*(int)Math.floor(workNum / oldHostSize));
			if(newWorkNum<=0){
				newWorkNum = 1;
			}
			configJsonObj.put("topology.billing.workgroup.num", newWorkNum+"");
			//该收缩的机器 已经没有 队列中， 无需收缩
			/*if(isFlag){
				logger.debug("节点收缩，该节点已经不在jstorm中， 无需收缩...");
				ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, null, null, "节点收缩，该节点已经不在jstorm中， 无需收缩... ", BusinessConstant.PARAMS_BUS_1);
				this.addClusterNodeLog(logDto);
				return;
			}
*/			
			//将Json转化为文件
			outTrans = FTPUtils.getFtpInstance(ftpDto);
			outTrans.login();
			String jsonStr = JSON.toJSONString(fileJsonObj);
			jsonStr = JsonFormatTool.formatJson(jsonStr);
			instram = new ByteArrayInputStream(jsonStr.getBytes(this.DEFAULT_ENCODING_UTF8));
			outTrans.put(instram, realFilePath);
			logger.debug("节点收缩，版本发布服务器修改重新负载配置文件成功...");
			
			//将版本发布服务器配置文件分发到远程主机
			String versionDir = FileTool.exactPath("V" + version);
			String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + versionDir + Constant.CFG_DIR + configFileName;		
			
			//查询当前业务集群所有部署的主机
			Map<String, Object> hostQueryParams = new HashMap<String, Object>();
			hostQueryParams.put("CLUSTER_ID", nodeDto.getClusterId());
			List<HashMap<String, Object>> hostList = coreService.queryForList2New("deployHome.queryVersionByClusterId", hostQueryParams, dbKey);
			for (HashMap<String, Object> hostMap : hostList) {
				//远程主机信息
				instram = new ByteArrayInputStream(jsonStr.getBytes(this.DEFAULT_ENCODING_UTF8));
				String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
				Trans tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpDto.getFtpType());
				tran.login();
				tran.put(instram, remotePath);
				tran.close();
				logger.debug("节点收缩，主机: " + sshIp + "， 修改重新负载配置文件成功...");
			}
		} catch (Exception e) {
			logger.error("节点收缩，重新负载配置文件修改失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,getHostStr(nodeDto.getBackupHostList()), null, "1、修改收缩配置文件失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("节点收缩，重新负载配置文件修改失败， 失败原因: " + e.getMessage());
		} finally {
			try {
				if (trans != null) {
					trans.close();
				}
				if (outTrans != null) {
					outTrans.close();
				}
				if (instram != null) {
					instram.close();
				}
			} catch (IOException e) {
				logger.debug("流关闭失败， 失败原因: ", e);
			}
		}
		
		try {
			//重新负载程序
			Map<String, Object> reblanceMap = new HashMap<String, Object>();
			reblanceMap.put("BUS_CLUSTER_ID", taskProgramMap.get("BUS_CLUSTER_ID"));
			reblanceMap.put("CLUSTER_ID", taskProgramMap.get("CLUSTER_ID"));
			reblanceMap.put("CLUSTER_TYPE", taskProgramMap.get("CLUSTER_TYPE"));
			reblanceMap.put("PROGRAM_CODE", taskProgramMap.get("PROGRAM_CODE"));
			reblanceMap.put("VERSION", taskProgramMap.get("VERSION"));
			reblanceMap.put("CONFIG_FILE", configFileName);
			topManagerService.topRebalanceReload(reblanceMap, dbKey);
			logger.debug("节点收缩， 重新负载成功...");
		} catch (Exception e) {
			logger.error("节点收缩，重新负载失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(nodeDto.getBackupHostList()), null, "1、修改收缩配置文件失败\n2、收缩程序启动失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("重新负载失败，失败原因: " + e.getMessage());
		}
	}
	
	/**
	 * 设置备用主机列表
	 * @param taskProgramId
	 * @param nodeDto
	 */
	private boolean addBackupHostList(String taskProgramId, String backupHosts, ClusterNodeDto nodeDto) {
		logger.debug("设置集群备用主机， 业务程序ID: " + taskProgramId + ", 备用主机: " + backupHosts + ", 节点信息: " + nodeDto);
		 List<String> list = nodeDto.getDeployHostIPArray();
		 String [] hostIp = backupHosts.split(",");
		 StringBuffer buffer = new StringBuffer();
		 //选出要扩展的机器
		 for(int i = 0 ; i < hostIp.length; i++){
			 boolean isFlag = true;
			 for(int j = 0 ; j < list.size(); j++){
				 if(list.get(j).equals(hostIp[i])){
					 isFlag = false;
					 break;
				 }
			 }
			 if(isFlag){
				 buffer.append("'").append(hostIp[i]).append("'").append(",");
			 }
		 }
		 if(buffer.length() > 1){
			 Map<String, Object> queryParams = new HashMap<String, Object>();
			 queryParams.put("HOST_IPS", buffer.toString().substring(0, buffer.toString().length() - 1));
			 //根据主机IP查询主机信息
		     List<HashMap<String, Object>> backupHostList = coreService.queryForList2New("host.queryHostListByIp", queryParams, dbKey);
		     nodeDto.getBackupHostList().addAll(backupHostList);
			 nodeDto.getBackupHostIPArray().addAll(Arrays.asList((buffer.toString().substring(0, buffer.toString().length() - 1)).split(",")));
		 }else{
			 nodeDto.getBackupHostList().clear();
			 nodeDto.getBackupHostIPArray().clear();
		 } 
		
		 if(nodeDto.getBackupHostIPArray()== null || nodeDto.getBackupHostIPArray().isEmpty()){
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,backupHosts, null, "该主机已经扩展，任务不再继续 ", BusinessConstant.PARAMS_BUS_2,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			return false;
		 }
		 return true;
	}
	
	/**
	 * 
	 * @param taskProgramId 扩展业务程序ID
	 * @param hostCount 一次性扩展主机台数
	 * @param nodeDto 集群主机信息
	 */
	private void addAutoExpendNode(String taskProgramId, int hostCount, ClusterNodeDto nodeDto) {
		//真实扩展主机数据
		List<HashMap<String, Object>> expendHostList = new ArrayList<HashMap<String, Object>>();
		if (nodeDto.getBackupHostList().size() < hostCount) {
			expendHostList.addAll(nodeDto.getBackupHostList());
		} else {
			for (int i=0; i<hostCount; i++) {
				expendHostList.add(nodeDto.getBackupHostList().get(i));
			}
		}
		
		//根据业务集群ID查询业务集群信息
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_ID", nodeDto.getClusterId());
		HashMap<String, Object> busClusterMap = coreService.queryForObject2New("serviceType.queryServiceTypeList", queryParams, dbKey);
		nodeDto.setClusterType(StringTool.object2String(busClusterMap.get("CLUSTER_TYPE")));
		
		//查询业务集群ID关联的Jstorm集群
		List<HashMap<String, Object>> componentClusterList = coreService.queryForList2New("deployHome.queryClusterByBusClusterId", queryParams, dbKey);
		if (BlankUtil.isBlank(componentClusterList)) {
			logger.debug("业务集群关联的Jstorm集群信息为空，不能扩展！");
			return;
		}
		nodeDto.setJstormClusterId(StringTool.object2String(componentClusterList.get(0).get("CLUSTER_ID")));
		nodeDto.setJstormClusterType(StringTool.object2String(componentClusterList.get(0).get("CLUSTER_TYPE")));
		nodeDto.setJstormClusterCode(StringTool.object2String(componentClusterList.get(0).get("CLUSTER_CODE")));
		logger.debug("当前业务集群信息: " + nodeDto);
		
		List<Map<String, String>> deployHostList = new ArrayList<Map<String, String>>();
		//组件划分
		addComponentPartiton(nodeDto, expendHostList, deployHostList);
		//组件部署
		addComponentDeploy(nodeDto, expendHostList, deployHostList);
		//组件启动
		addComponentStart(nodeDto, expendHostList, deployHostList);
		
		//业务程序划分
		addBusPartition(nodeDto, expendHostList, deployHostList);
		//业务程序部署
		addBusDeploy(taskProgramId, nodeDto, expendHostList, deployHostList);
		//业务程序重新负载
		addBusRebalance(taskProgramId, nodeDto, expendHostList, deployHostList);
		
		ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1,getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动成功\n5、业务划分成功\n6、业务程序部署成功\n7、修改扩展配置文件成功\n8、扩展程序负载成功\n任务执行结束" , BusinessConstant.PARAMS_BUS_1,nodeDto.getMsg(),nodeDto.getRuleStr());
		this.addClusterNodeLog(logDto);
		
	}
	
	/**
	 * 业务程序划分
	 * @param nodeDto
	 * @param expendHostList
	 * @param deployHostList
	 */
	private void addBusPartition(ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		try {
			for (HashMap<String, Object> hostMap : expendHostList) {
				hostMap.put("NAME", hostMap.get("HOST_IP") + "_" + nodeDto.getClusterType());
				hostMap.put("CLUSTER_TYPE", nodeDto.getClusterType());
				hostMap.put("CLUSTER_ID", nodeDto.getClusterId());
				List<HashMap<String, Object>> partitionList = coreService.queryForList2New("deployHome.queryHostPartitionByIp", hostMap, dbKey);
				if (!BlankUtil.isBlank(partitionList)) {
					hostMap.put("ID", partitionList.get(0).get("ID"));
					logger.debug("节点扩展，业务节点扩展划分已经存在，重新加载划分主机...");
				} else {
					coreService.insertObject2New("deployHome.insertChosenHostReturnKey", hostMap, dbKey);
					logger.debug("节点扩展，业务节点扩展划分完成...");
				}
			}
		} catch (Exception e) {
			logger.error("节点扩展，业务划分失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动成功\n4、业务划分失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("借点扩展，业务划分失败， 失败原因: " + e.getMessage());
		}
	}
	
	/**
	 * 业务程序部署
	 * @param nodeDto
	 * @param expendHostList
	 * @param deployHostList
	 */
	private void addBusDeploy(String taskProgramId, ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		//查询业务程序部署最新的版本
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_ID", nodeDto.getClusterId());
		//List<HashMap<String, Object>> busClusterList = coreService.queryForList2New("deployHome.queryVersionByClusterId", queryParams, dbKey);
		//String lastVersion = StringTool.object2String(busClusterList.get(0).get("VERSION"));
		
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		List<HashMap<String, Object>> programPackageTypeList = coreService.queryForList2New("taskProgram.queryProgramPackageType", queryParams, dbKey);
		if (BlankUtil.isBlank(programPackageTypeList)) {
			throw new RuntimeException("节点扩展，业务程序归属包类型为空，请检查， 当前业务集群ID： " + nodeDto.getClusterId());
		}
		String packageType = StringTool.object2String(programPackageTypeList.get(0).get("PACKAGE_TYPE"));
		String busClusterCode = StringTool.object2String(programPackageTypeList.get(0).get("BUS_CLUSTER_CODE"));
		String packageName = StringTool.object2String(programPackageTypeList.get(0).get("NAME"));
		
		//查询业务部署最新版本
		StringBuffer idFailBuffer = new StringBuffer();
		StringBuffer ipFailBuffer = new StringBuffer();
		StringBuffer idSuccBuffer = new StringBuffer();
		StringBuffer ipSuccBuffer = new StringBuffer();
		for (HashMap<String, Object> expendHostMap : expendHostList) {
			Map<String, Object> busParams = new HashMap<String, Object>();
			busParams.put("webRootPath", this.getRealPath());
			busParams.put("CLUSTER_ID", nodeDto.getClusterId());
			busParams.put("CLUSTER_TYPE", nodeDto.getClusterType());
			busParams.put("HOST_ID", expendHostMap.get("HOST_ID"));
			busParams.put("VERSION", nodeDto.getVersion());
			busParams.put("NAME", packageName);
			busParams.put("PACKAGE_TYPE", packageType);
			busParams.put("BUS_CLUSTER_CODE", busClusterCode);
			busParams.put("ID", StringTool.object2String(expendHostMap.get("ID")));
			try {
				deployBusTaskService.updateDistribute(busParams, dbKey);
				idSuccBuffer.append(expendHostMap.get("HOST_IP")).append(":");
				ipSuccBuffer.append("部署成功").append(",");
			} catch (Exception e) {
				idFailBuffer.append(expendHostMap.get("HOST_IP")).append(",");
				ipFailBuffer.append("部署失败").append(",失败原因：").append(e.getMessage()).append(",");
				logger.error("节点扩展，主机部署失败， 主机IP: " + expendHostMap.get("HOST_IP") + ", 失败原因: ", e);
			}
		}
		if (!BlankUtil.isBlank(ipFailBuffer.toString())) {
			String ipStr = "部署成功主机IP: " + (BlankUtil.isBlank(ipSuccBuffer.toString()) ? "无主机部署成功" : ipSuccBuffer.toString().substring(0, ipSuccBuffer.toString().length() -1));
				  ipStr += "；部署失败主机IP: " + ipFailBuffer.toString().substring(0, ipFailBuffer.toString().length() - 1);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动成功\n5、业务划分成功\n6、业务程序部署失败，"+ipFailBuffer.toString(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
		}
		logger.debug("节点扩展，业务程序部署完成...");
	}
	
	/**
	 * 业务程序重新负载 - 修改配置文件
	 * @param nodeDto
	 * @param expendHostList
	 * @param deployHostList
	 */
	private void addBusRebalance(String taskProgramId, ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		//修改Topology配置文件，新增work分配主机列表
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		logger.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());
		
		//查询版本发布服务器信息
	     FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		Trans outTrans = null;
		InputStream instram = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			InputStream fileStream = trans.get(realFilePath);
		    String buffer = FileUtil.readInputStream(fileStream);
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.trim());
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			int workNum = configJsonObj.getInteger("topology.billing.workgroup.num");
			int oldHostSize = jsonArray.size();
			for (HashMap<String, Object> hostMap : expendHostList) {
				List<String> hostList = new ArrayList<String>();
					hostList.add(StringTool.object2String(hostMap.get("HOST_IP")));
					hostList.add("1");
					jsonArray.add(hostList);
				 
			}
			int newWorkNum = (int)Math.ceil(workNum / oldHostSize);
			configJsonObj.put("topology.billing.workgroup.num", workNum+expendHostList.size()*newWorkNum+"");
			//将Json转化为文件
			outTrans = FTPUtils.getFtpInstance(ftpDto);
			outTrans.login();
			String jsonStr = JSON.toJSONString(fileJsonObj);
			jsonStr = JsonFormatTool.formatJson(jsonStr);
			instram = new ByteArrayInputStream(jsonStr.getBytes(this.DEFAULT_ENCODING_UTF8));
			outTrans.put(instram, realFilePath);
			logger.debug("节点扩展，版本发布服务器修改重新负载配置文件成功...");
			
			//将版本发布服务器配置文件分发到远程主机
			String versionDir = FileTool.exactPath("V" + version);
			String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + versionDir + Constant.CFG_DIR + configFileName;		
			
			//查询当前业务集群所有部署的主机
			Map<String, Object> hostQueryParams = new HashMap<String, Object>();
			hostQueryParams.put("CLUSTER_ID", nodeDto.getClusterId());
			List<HashMap<String, Object>> hostList = coreService.queryForList2New("deployHome.queryVersionByClusterId", hostQueryParams, dbKey);
			for (HashMap<String, Object> hostMap : hostList) {
				//远程主机信息
				instram = new ByteArrayInputStream(jsonStr.getBytes(this.DEFAULT_ENCODING_UTF8));
				String sshIp = StringTool.object2String(hostMap.get("HOST_IP"));
				String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
				Trans tran = FTPUtils.getFtpInstance(sshIp, sshUser, sshPwd, ftpDto.getFtpType());
				tran.login();
				tran.put(instram, remotePath);
				tran.close();
				logger.debug("节点扩展，主机: " + sshIp + "， 修改重新负载配置文件成功...");
			}
		} catch (Exception e) {
			logger.error("节点扩展，修改配置文件失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动成功\n5、业务划分成功\n6、业务程序部署成功\n7、修改扩展配置文件失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException(e);
		} finally {
			try {
				if (trans != null) {
					trans.close();
				}
				if (outTrans != null) {
					outTrans.close();
				}
				 
				if (instram != null) {
					instram.close();
				}
			} catch (IOException e) {
				logger.debug("节点扩展，流关闭失败， 失败原因: ", e);
			}
		}
		
		
		try {
			//重新负载程序
			Map<String, Object> reblanceMap = new HashMap<String, Object>();
			reblanceMap.put("BUS_CLUSTER_ID", taskProgramMap.get("BUS_CLUSTER_ID"));
			reblanceMap.put("CLUSTER_ID", taskProgramMap.get("CLUSTER_ID"));
			reblanceMap.put("CLUSTER_TYPE", taskProgramMap.get("CLUSTER_TYPE"));
			reblanceMap.put("PROGRAM_CODE", taskProgramMap.get("PROGRAM_CODE"));
			reblanceMap.put("VERSION", taskProgramMap.get("VERSION"));
			reblanceMap.put("CONFIG_FILE", configFileName);
			topManagerService.topRebalanceReload(reblanceMap, dbKey);
			logger.debug("节点扩展， 重新负载成功...");
		} catch (Exception e) {
			logger.error("节点扩展，重新负载失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, null, null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动成功\n5、业务划分成功\n6、业务程序部署成功\n7、修改扩展配置文件成功\n8、扩展程序负载失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 业务程序重新负载 - 修改配置文件
	 * @param taskProgramId
	 */
	private String getTopoHost(String taskProgramId) {
		//修改Topology配置文件，新增work分配主机列表
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		logger.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());
		
		//查询版本发布服务器信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		
		//业务配置文件
		String deployPath = StringTool.object2String(taskProgramMap.get("CLUSTER_DEPLOY_PATH"));
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String version = StringTool.object2String(taskProgramMap.get("VERSION"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		 
		String hoststr="";
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			InputStream fileStream = trans.get(realFilePath);
		    String buffer = FileUtil.readInputStream(fileStream);
			//配置文件JSON对象
			JSONObject fileJsonObj = JSON.parseObject(buffer.trim());
			//development节点数据
			JSONObject devJsonObj = (JSONObject) fileJsonObj.get("development");
			//config节点数据
			JSONObject configJsonObj = (JSONObject)devJsonObj.get("config");
			//topology.billing.workgroup.supervisor.hostname2rate节点数据
			JSONArray jsonArray = configJsonObj.getJSONArray("topology.billing.workgroup.supervisor.hostname2rate");
			
			for (Object hostObject : jsonArray) {
				JSONArray hostArray =  (JSONArray) hostObject;
				hoststr +=hostArray.get(0)+",";
			}
			char indexc = hoststr.charAt(hoststr.length()-1);
			if(indexc == ','){
				hoststr = hoststr.substring(0,hoststr.length()-1);
			}
			return hoststr;
		}catch(Exception e){
			Log.error("读取失败",e);
			throw new RuntimeException("获取配置文件失败");
		}finally {
			if (trans != null) {
				trans.close();
			}
		}
			
			 
	}
	
	/**
	 * 添加集群扩展结果日志表
	 * @param logDto
	 */
	private void addClusterNodeLog(ClusterNodeLogDto logDto) {
		Map<String, Object> logMap = new HashMap<String, Object>();
		logMap.put("STRATEGY_ID", logDto.getStrategyId());
		logMap.put("TRIGGER_RESULT", logDto.getTriggerResult());
		logMap.put("HOST_IP_LIST", logDto.getHostIpList());
		logMap.put("HOST_ID_LIST", logDto.getHostIdList());
		logMap.put("EXEC_RESULT", logDto.getExecResult());
		logMap.put("EXEC_MESSAGE", logDto.getExecMessage());
		logMap.put("RULE_MSG", logDto.getRuleMsg());
		logMap.put("HOST_NORM_MSG", logDto.getHostNormMsg());
		coreService.insertObject2New("expendStrategyLog.addExpendStrategyLog", logMap, dbKey);
		logger.debug("添加伸缩日志成功...");
	}
	
	/**
	 * 组件划分
	 * @param nodeDto
	 * @param expendHostList
	 * @param deployHostList
	 */
	private void addComponentPartiton(ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		try {
			for (HashMap<String, Object> hostMap : expendHostList) {
				hostMap.put("NAME", hostMap.get("HOST_IP") + "_" + nodeDto.getJstormClusterType());
				hostMap.put("CLUSTER_TYPE", nodeDto.getJstormClusterType());
				hostMap.put("CLUSTER_ID", nodeDto.getJstormClusterId());
				List<HashMap<String, Object>> partitionList = coreService.queryForList2New("deployHome.queryHostPartitionByIp", hostMap, dbKey);
				if (!BlankUtil.isBlank(partitionList)) {
					hostMap.put("ID", partitionList.get(0).get("ID"));
					logger.debug("节点扩展，组件已经划分，重新加载划分组件...");
				} else {
					coreService.insertObject2New("deployHome.insertChosenHostReturnKey", hostMap, dbKey);
					logger.debug("节点扩展，组件划分完成...");
				}
			}
		} catch (Exception e) {
			logger.error("节点扩展，组件划分失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("节点扩展，组件划分失败， 失败原因: " + e.getMessage());
		}
	}
	
	/**
	 * 组件部署
	 * @param nodeDto 业务集群DTO对象
	 * @param expendHostList 扩展主机List
	 * @param deployHostList 部署后主机对象
	 */
	private void addComponentDeploy(ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		try {
			//查询Jstorm部署最新的版本
			Map<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("CLUSTER_ID", nodeDto.getJstormClusterId());
			List<HashMap<String, Object>> componentClusterList = coreService.queryForList2New("deployHome.queryVersionByClusterId", queryParams, dbKey);
			String lastVersion = StringTool.object2String(componentClusterList.get(0).get("VERSION"));
			HashMap<String, Object> params = new HashMap<String, Object>();
			for (Map<String, Object> expendHostMap : expendHostList) {
				Map<String, String> deployHostMap = new HashMap<String, String>();
				deployHostMap.put("CLUSTER_ID", nodeDto.getJstormClusterId());
				deployHostMap.put("CLUSTER_TYPE", nodeDto.getJstormClusterType());
				deployHostMap.put("CLUSTER_CODE", nodeDto.getJstormClusterCode());
				deployHostMap.put("VERSION", lastVersion);
				deployHostMap.put("HOST_ID", StringTool.object2String(expendHostMap.get("HOST_ID")));
				deployHostMap.put("ID", StringTool.object2String(expendHostMap.get("ID")));
				deployHostList.add(deployHostMap);
			}
			params.put("paramList", deployHostList);
			params.put("rootPath", this.getRealPath());
			
			deployService.updateDeployHost(params, dbKey);
			logger.debug("节点扩展，组件部署成功...");
		} catch (Exception e) {
			logger.error("节点扩展，组件部署失败，阀值触发扩展失败， 失败原因：", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("节点扩展，组件部署失败， 失败原因: " + e.getMessage());
		}
	}
	
	/**
	 * 组件启动
	 * @param nodeDto
	 * @param expendHostList
	 * @param deployHostList
	 */
	private void addComponentStart(ClusterNodeDto nodeDto, List<HashMap<String, Object>> expendHostList, List<Map<String, String>> deployHostList) {
		try {
			//启动Jstorm集群Superv实例
			for (Map<String, String> deployHostMap : deployHostList) {
				deployHostMap.put("autoFile", Constant.RUN_AUTH_FILE_EXT);
				deployHostMap.put("localRootPath", this.getRealPath());
				deployHostMap.put("DEPLOY_TYPE", Constant.JSTORM_SUPERVISOR);
				deployHostMap.put("DATA_CLEAR", BusinessConstant.PARAMS_BUS_0);
			}
			Map<String, Object> resultCodeMap = jstormStartService.startJstorm(deployHostList, dbKey);
			if(Constant.RST_CODE_FAILED.equals(resultCodeMap.get(Constant.RST_CODE))){
				logger.error("节点扩展， 组件实例启动出错--->" +resultCodeMap.get(Constant.RST_STR));
				throw new RuntimeException(resultCodeMap.get(Constant.RST_STR).toString());
			}
			logger.debug("节点扩展， 组件实例启动成功...");
		} catch (Exception e) {
			logger.error("节点扩展，组件实例启动失败， 阀值触发扩展失败， 失败原因: ", e);
			ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, getHostStr(expendHostList), null, "1、组件划分成功\n2、组件部署成功\n3、组件实例supervisor启动失败， 失败原因: " + e.getMessage(), BusinessConstant.PARAMS_BUS_0,nodeDto.getMsg(),nodeDto.getRuleStr());
			this.addClusterNodeLog(logDto);
			throw new RuntimeException("节点扩展，组件启动实例失败， 失败原因: " + e.getMessage());
		}
	}
	
	/**
	 * 获取当前项目路径
	 * @return
	 */
	private String getRealPath() {
		return ClusterNodeManageQuartz.class.getResource("/").getPath();
	}
	
	private String getHostStr(List<HashMap<String,Object>> hostList){
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < hostList.size() ;i++){
			sb.append(hostList.get(i).get("HOST_IP")).append(",");
		}
		if(sb.length() > 1){
			return sb.toString().substring(0,sb.length()-1);
		}
		return "";
	}
	public static void main(String[] args) {
		String hosts = "192,193";
		List<String> aa = Arrays.asList(hosts.split(","));
		System.out.println(aa);
		System.out.println(aa.get(0));
		System.out.println(aa.get(1));
	}
	
	 
	
	 
}
