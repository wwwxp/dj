package com.tydic.quartz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.tydic.bean.FtpDto;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.tydic.bean.ClusterNodeQuotaDto;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.FileUtil;
import com.tydic.util.HttpClientUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;


/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.job.impl]    
  * @ClassName:    [DynamicThresholdQuartz]     
  * @Description:  [动态节点伸缩触发判断]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-7 下午2:53:16]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-7 下午2:53:16]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@DisallowConcurrentExecution
public class DynamicThresholdQuartz implements Job {
	//节点伸缩日志对象
	private static Logger logger = Logger.getLogger(DynamicThresholdQuartz.class);
	//调用主机资源URL配置
	private final String GET_HOST_QUOTA_URL = SystemProperty.getContextProperty("host.quota.month.url");
	
	//默认数据源
	private final String dbKey = FrameConfigKey.DEFAULT_DATASOURCE;
	
	/**
	 * 批价中心
	 */
	private static final String [] rateCenterList = new String[]{"dcbilling"};
	
	//核心Servive对象
	private CoreService coreService = (CoreService) SpringContextUtil.getBean("coreService");

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
			logger.info("业务参数为空，无法检测是否需要出发节点伸缩...");
			return;
		}
		
		//获取节点伸缩策略配置
		Map<String, Object> queryCfgParams = new HashMap<String, Object>();
		queryCfgParams.put("STRATEGY_ID", jobParams.get("BUS_ID"));
		Map<String, Object> cfgMap = coreService.queryForObject2New("expendStrategyConfig.queryStrategyConfigById", queryCfgParams, dbKey);
		
		//判断是否需要生成伸缩扩展报告
		String forecastReportId = StringTool.object2String(jobParams.get("EXTENDED_FIELD"));
		queryCfgParams.put("FORECAST_REPORT_ID", forecastReportId);
		List<HashMap<String, Object>> expansionList = coreService.queryForList2New("expansionReport.queryExpansionReport", queryCfgParams, dbKey);
		if (!BlankUtil.isBlank(expansionList)) {
			logger.info("该预测报告已经生成伸缩报告， 无需再次生成...");
			return;
		}
		
		//判断是否需要进行伸缩扩展
		if (BusinessConstant.PARAMS_BUS_7.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {  //动态扩展
			this.hasNeedExpendNode(forecastReportId, cfgMap);
		} else if (BusinessConstant.PARAMS_BUS_8.equals(StringTool.object2String(cfgMap.get("OPERATOR_TYPE")))) {  //动态收缩
			this.hasNeedUnexpendNode(forecastReportId, cfgMap);
		}
	}
	
	/**
	 * 判断当前满足动态扩展节点
	 * @param forecastReportId 预测报告ID
	 * @param cfgMap 当前扩展策略配置
	 */
	private void hasNeedExpendNode(String forecastReportId, Map<String, Object> cfgMap) {
		logger.info("判断当前策略配置是否满足节点扩展， 业务参数: " + cfgMap);
		
		//业务程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		
		//获取集群主机信息
		HashSet<String> hostList = new HashSet<String>();
		this.getTopoHost(taskProgramId, hostList);
		
		//获取主机集群资源信息
		List<ClusterNodeQuotaDto> hostQuotaList = new ArrayList<ClusterNodeQuotaDto>();
		this.getHostQuotaFromDB(cfgMap, hostList, hostQuotaList);
		
		//判断当前集群主机资源是否满足扩展需求
		Map<String, Object> triggerRetMap = this.getExpendTriggerResult(cfgMap, hostList, hostQuotaList);
		
		if (Boolean.parseBoolean(StringTool.object2String(triggerRetMap.get("TRIGGER_RESULT")))) {
			//触发扩展的节点数据
			ClusterNodeQuotaDto nodeDto = (ClusterNodeQuotaDto) triggerRetMap.get("TRIGGER_NODE");
			//添加扩展报告
			this.addExpandReport(forecastReportId, nodeDto, cfgMap, hostList, hostQuotaList);
			//将预测报告状态置为失效
			this.updateForecastReport(forecastReportId);
			//将定时任务删除
			this.delJobTask(forecastReportId);
		}
	}
	
	/**
	 * 根据预测报告ID删除定时任务
	 * @param forecastReportId
	 */
	private void delJobTask(String forecastReportId) {
		logger.info("删除伸缩报告定时任务， 预测报告ID: " + forecastReportId);
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("EXTENDED_FIELD", forecastReportId);
		paramsMap.put("BUS_TYPE", BusinessConstant.PARAMS_BUS_4);
		
		Object taskId = null;
		List<HashMap<String, Object>> jobTaskList = coreService.queryForList2New("jobTaskBus.queryJobTaskByID", paramsMap, dbKey);
		if (!BlankUtil.isBlank(jobTaskList)) {
			taskId = jobTaskList.get(0).get("TASK_ID");
		}
		coreService.deleteObject2New("jobTaskBus.delJobTaskByReportId", paramsMap, dbKey);
		logger.info("删除定时任务关联表成功， 删除任务参数: " + paramsMap);
		
		paramsMap.put("ID", taskId);
		coreService.deleteObject2New("jobtaskcfg.delTask", paramsMap, dbKey);
		logger.info("删除定时任务配置信息成功， 删除参数: " + paramsMap);
		logger.info("删除伸缩报告定时任务完成， 预测报告ID: ");
	}
	
	/**
	 * 修改预测报告
	 * @param forecastReportId
	 */
	private void updateForecastReport(String forecastReportId) {
		logger.info("节点扩展报告输出成功 ，修改预测报告状态, 预测报告ID: " + forecastReportId);
		Map<String, Object> updateParamsMap = new HashMap<String, Object>();
		updateParamsMap.put("STATUS", BusinessConstant.PARAMS_BUS_2);
		updateParamsMap.put("ID", forecastReportId);
		coreService.updateObject2New("forecastReport.updateStatusByID", updateParamsMap, dbKey);
		logger.info("修改预测报告状态成功， 预测报告ID： " + forecastReportId);
	}
	
	/**
	 * 判断当前业务是否满足动态收缩
	 * @param forecastReportId 预测报告ID
	 * @param cfgMap 当前扩展策略配置
	 */
	private void hasNeedUnexpendNode(String forecastReportId, Map<String, Object> cfgMap) {
		logger.info("判断当前策略配置是否满足节点收缩， 业务参数: " + cfgMap);
		
		//业务程序ID
		String taskProgramId = StringTool.object2String(cfgMap.get("TASK_PROGRAM_ID"));
		
		//获取集群主机信息
		HashSet<String> hostList = new HashSet<String>();
		this.getTopoHost(taskProgramId, hostList);
		
		//获取主机集群资源信息
		List<ClusterNodeQuotaDto> hostQuotaList = new ArrayList<ClusterNodeQuotaDto>();
		this.getHostQuotaFromDB(cfgMap, hostList, hostQuotaList);
		
		//判断当前集群主机资源是否满足扩展需求
		Map<String, Object> triggerRetMap = this.getUnexpendTriggerResult(cfgMap, hostList, hostQuotaList);
		
		if (Boolean.parseBoolean(StringTool.object2String(triggerRetMap.get("TRIGGER_RESULT")))) {
			//触发收缩的节点数据
			ClusterNodeQuotaDto nodeDto = (ClusterNodeQuotaDto) triggerRetMap.get("TRIGGER_NODE");
			//添加扩展报告
			this.addUnexpandReport(forecastReportId, nodeDto, cfgMap, hostList, hostQuotaList);
			//将预测报告状态置为失效
			this.updateForecastReport(forecastReportId);
			//将定时任务删除
			this.delJobTask(forecastReportId);
		}
	}
	
	/**
	 * 添加业务收缩报告
	 * @param forecastReportId 预测报告ID
	 * @param nodeDto
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	private void addUnexpandReport(String forecastReportId, ClusterNodeQuotaDto nodeDto, Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("预测成功，添加收缩报告开始， 业务参数: " + cfgMap);
		Map<String, Object> paramsMap = this.getExpendMapParams(forecastReportId, nodeDto, cfgMap, BusinessConstant.PARAMS_BUS_8);
		coreService.insertObject2New("expansionReport.addExpansionReport", paramsMap, dbKey);
		logger.info("添加业务收缩报告成功， 业务ID: " + cfgMap.get("TASK_PROGRAM_ID"));
	}
	
	/**
	 * 添加业务扩展报告
	 * @param forecastReportId 预测报告ID
	 * @param nodeDto
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	private void addExpandReport(String forecastReportId, ClusterNodeQuotaDto nodeDto, Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("预测成功，添加扩展报告开始， 业务参数: " + cfgMap);
		Map<String, Object> paramsMap = this.getExpendMapParams(forecastReportId, nodeDto, cfgMap, BusinessConstant.PARAMS_BUS_7);
		coreService.insertObject2New("expansionReport.addExpansionReport", paramsMap, dbKey);
		logger.info("添加业务扩展报告成功， 业务ID: " + cfgMap.get("TASK_PROGRAM_ID"));
	}
	
	/**
	 * 生成伸缩报告对象
	 * @param forecastReportId 预测报告ID
	 * @param nodeDto
	 * @param cfgMap
	 * @param operatorType
	 */
	private Map<String, Object> getExpendMapParams(String forecastReportId, ClusterNodeQuotaDto nodeDto, Map<String, Object> cfgMap, String operatorType) {
		Map<String, Object> reportParamsMap = new HashMap<String, Object>();
		//业务程序ID
		reportParamsMap.put("TASK_PROGRAM_ID", cfgMap.get("TASK_PROGRAM_ID"));
		//操作类型 	7：节点扩展   8:节点收缩
		reportParamsMap.put("OPERATOR_TYPE", operatorType);
		
		int cpuValue = new Double(nodeDto.getCpuRateSum()).intValue();
		int memValue = new Double(nodeDto.getMemoryRateSum()).intValue();
		int diskValue = new Double(nodeDto.getDiskTotalRate()).intValue();
		int bussValue = new Double(nodeDto.getBussSum()).intValue();
		
		//触发阀值的主机CPU
		reportParamsMap.put("CPU", cpuValue);
		//触发阀值的主机内存
		reportParamsMap.put("MEM", memValue);
		//触发阀值的主机磁盘
		reportParamsMap.put("DISK", diskValue);
		//触发阀值的业务量
		reportParamsMap.put("BUSS_VOLUME", bussValue);
		//报告确认时间，添加报告时不填写
		reportParamsMap.put("EXEC_TIME", null);
		//报告执行凡是 1：立即执行  2：定时自行
		reportParamsMap.put("ACTION_TYPE", "");
		
		//获取建议扩展节点个数
		this.getAdviceClusterNodeNum(nodeDto, cfgMap, operatorType);
		
		//建议伸缩扩展节点个数
		reportParamsMap.put("ADVISE_NODE_COUNT", cfgMap.get("ADVISE_NODE_COUNT"));
		if (BusinessConstant.PARAMS_BUS_7.equals(operatorType)) {
			//描述信息
			StringBuilder builder = new StringBuilder();
			builder.append("动态扩展节点报告生成成功，当前集群触发扩展节点【").append(nodeDto.getHostIp()).append("】信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			builder.append("，建议扩展节点个数：").append(cfgMap.get("ADVISE_NODE_COUNT"));
			reportParamsMap.put("RESULT_DESC", builder.toString());
		} else if (BusinessConstant.PARAMS_BUS_8.equals(operatorType)) {
			//描述信息
			StringBuilder builder = new StringBuilder();
			builder.append("动态收缩节点报告生成成功，当前集群触发收缩节点【").append(nodeDto.getHostIp()).append("】信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			builder.append("，建议收缩节点个数：").append(cfgMap.get("ADVISE_NODE_COUNT"));
			//描述信息
			reportParamsMap.put("RESULT_DESC", builder.toString());
		}
		//定时任务关联JOB
		reportParamsMap.put("JOB_ID", "");
		//节点扩展备用主机
		reportParamsMap.put("HOST_IPS", "");
		//触发报告执行状态
		reportParamsMap.put("EXEC_STATUS", BusinessConstant.PARAMS_BUS_0);
		//触发报告关联策略ID
		reportParamsMap.put("STRATEGY_ID", cfgMap.get("STRATEGY_ID"));
		//预测报告ID
		reportParamsMap.put("FORECAST_REPORT_ID", forecastReportId);
		logger.info("生成节点收缩报告参数， 返回结果: " + reportParamsMap);
		return reportParamsMap;
	}
	
	/**
	 * 获取建议伸缩节点个数，当前节点数据和预测阀值相差10%收缩或者添加1个节点
	 * @param nodeDto
	 * @param cfgMap
	 * @param operatorType
	 */
	private void getAdviceClusterNodeNum(ClusterNodeQuotaDto nodeDto, Map<String, Object> cfgMap, String operatorType) {
		logger.info("获取建议伸缩集群节点个数， 业务参数: " + cfgMap + ", 伸缩节点信息: " + nodeDto);
		
		//当前策略阀值
		String conditionValue = StringTool.object2String(cfgMap.get("CONDITION_VALUE"));
		Double conditionValNum = Double.parseDouble(conditionValue);
		
		//当前策略指标类型
		String quotaType = StringTool.object2String(cfgMap.get("QUOTA_TYPE"));
		
		int nodeCount = 0;
		if (BusinessConstant.PARAMS_BUS_7.equals(operatorType)) {  //扩展操作
			if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {  //CPU
				nodeCount = (int)(((Double)nodeDto.getCpuRateSum() - conditionValNum)/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {  //内存
				nodeCount = (int)(((Double)nodeDto.getMemoryRateSum() - conditionValNum)/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {  //磁盘
				nodeCount = (int)(((Double)nodeDto.getDiskTotalRate() - conditionValNum)/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {  //业务量
				nodeCount = (int)(((Double)nodeDto.getBussSum() - conditionValNum)/10000)+1;
			}
		} else if (BusinessConstant.PARAMS_BUS_8.equals(operatorType)) {
			if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {  //CPU
				nodeCount = (int)((conditionValNum - (Double)nodeDto.getCpuRateSum())/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {  //内存
				nodeCount = (int)((conditionValNum - (Double)nodeDto.getMemoryRateSum())/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {  //磁盘
				nodeCount = (int)((conditionValNum - (Double)nodeDto.getDiskTotalRate())/10)+1;
			} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {  //业务量
				nodeCount = (int)((conditionValNum - (Double)nodeDto.getBussSum())/10000)+1;
			}
		}
		cfgMap.put("ADVISE_NODE_COUNT", nodeCount);
		logger.info("获取集群建议伸缩个数， 返回结果: " + nodeCount);
	}
	
	/**
	 * 获取业务程序集群节点资源信息
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getExpendTriggerResult(Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("判断业务程序是否触发节点扩展开始， 业务参数: " + cfgMap);
		//指标类型
		String quotaType = StringTool.object2String(cfgMap.get("QUOTA_TYPE"));
		//条件类型    1:集群主机资源最大>=    2:集群主机平均资源占比>=  
		String conditionParams = StringTool.object2String(cfgMap.get("CONDITION_PARAM"));
		//条件对应的值
		String conditionValue = StringTool.object2String(cfgMap.get("CONDITION_VALUE"));
		Double conditionNum = Double.parseDouble(StringTool.object2String(conditionValue));
		
		//方法处理结果返回对象
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		//是否触发节点扩展
		Boolean isTrigger = Boolean.FALSE;
		//触发节点扩展的主机信息
		ClusterNodeQuotaDto nodeQuotaDto = null;
		
		if (conditionParams.equals(BusinessConstant.PARAMS_BUS_1)) {
			List<HashMap<String, Object>> minQuotaList = HttpClientUtil.getMaxQuotaValue(hostQuotaList, quotaType);
			logger.info("判断业务程序是否触发节点扩展， 集群主机资源最大值参数: " + minQuotaList);
			for (HashMap<String, Object> quotaMap : minQuotaList) {
				String triggerValue = StringTool.object2String(quotaMap.get("MIN_VALUE"));
				if(quotaType.equals(BusinessConstant.PARAMS_BUS_4)){
					String [] tValues = triggerValue.split("-");
					if(Double.parseDouble(tValues[0]) >= conditionNum || Double.parseDouble(tValues[1]) >= conditionNum){
						//触发节点扩展的DTO对象
						List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
						nodeQuotaDto = hostNodeList.get(0);
						isTrigger = Boolean.TRUE;
						break;
					}
				}else{
					if (Double.parseDouble(triggerValue) >= conditionNum) {
						//触发节点扩展的DTO对象
						List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
						nodeQuotaDto = hostNodeList.get(0);
						isTrigger = Boolean.TRUE;
						break;
					}
				}
			}
		} else if (conditionParams.equals(BusinessConstant.PARAMS_BUS_2)) {
			List<HashMap<String, Object>> avgQuotaList =  HttpClientUtil.getAvgQuotaValue(hostQuotaList, quotaType);
			logger.info("判断业务程序是否触发节点扩展， 集群主机资源平均值参数: " + avgQuotaList);
			for (HashMap<String, Object> quotaMap : avgQuotaList) {
				String triggerValue = StringTool.object2String(quotaMap.get("AGV_VALUE"));
				if (Double.parseDouble(triggerValue) >= conditionNum) {
					//触发节点扩展的DTO对象
					List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
					nodeQuotaDto = hostNodeList.get(0);
					isTrigger = Boolean.TRUE;
					break;
				}
			}
		}
		retMap.put("TRIGGER_RESULT", isTrigger);
		retMap.put("TRIGGER_NODE", nodeQuotaDto);
		logger.info("判断业务程序是否触发节点扩展接触， 返回结果: " + retMap);
		return retMap;
	}
	
	/**
	 * 判断当前策略是否满足节点收缩
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getUnexpendTriggerResult(Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("判断业务程序是否触发节点收缩开始， 业务参数: " + cfgMap);
		//指标类型
		String quotaType = StringTool.object2String(cfgMap.get("QUOTA_TYPE"));
		//条件类型    1:集群主机资源最大>=    2:集群主机平均资源占比>=  
		String conditionParams = StringTool.object2String(cfgMap.get("CONDITION_PARAM"));
		//条件对应的值
		String conditionValue = StringTool.object2String(cfgMap.get("CONDITION_VALUE"));
		Double conditionNum = Double.parseDouble(StringTool.object2String(conditionValue));
		
		//方法处理结果返回对象
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		//是否触发节点扩展
		Boolean isTrigger = Boolean.FALSE;
		//触发节点扩展的主机信息
		ClusterNodeQuotaDto nodeQuotaDto = null;
		
		if (conditionParams.equals(BusinessConstant.PARAMS_BUS_1)) {
			List<HashMap<String, Object>> maxQuotaList = HttpClientUtil.getMaxQuotaValue(hostQuotaList, quotaType);
			logger.info("判断业务程序是否触发节点收缩， 集群主机资源最大值参数: " + maxQuotaList);
			for (HashMap<String, Object> quotaMap : maxQuotaList) {
				String triggerValue = StringTool.object2String(quotaMap.get("MAX_VALUE"));
				if(quotaType.equals(BusinessConstant.PARAMS_BUS_4)){
					String [] tValues = triggerValue.split("-");
					if(Double.parseDouble(tValues[0]) <= conditionNum || Double.parseDouble(tValues[1]) <= conditionNum){
						//触发节点扩展的DTO对象
						List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
						nodeQuotaDto = hostNodeList.get(0);
						isTrigger = Boolean.TRUE;
						break;
					}
				}else{
					if (Double.parseDouble(triggerValue) <= conditionNum) {
						//触发节点扩展的DTO对象
						List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
						nodeQuotaDto = hostNodeList.get(0);
						isTrigger = Boolean.TRUE;
						break;
					}
				}
			}
		} else if (conditionParams.equals(BusinessConstant.PARAMS_BUS_2)) {
			List<HashMap<String, Object>> avgQuotaList =  HttpClientUtil.getAvgQuotaValue(hostQuotaList, quotaType);
			logger.info("判断业务程序是否触发节点收缩， 集群主机资源平均值参数: " + avgQuotaList);
			for (HashMap<String, Object> quotaMap : avgQuotaList) {
				String triggerValue = StringTool.object2String(quotaMap.get("AGV_VALUE"));
				if (Double.parseDouble(triggerValue) <= conditionNum) {
					//触发节点扩展的DTO对象
					List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) quotaMap.get("HOST_LIST");
					nodeQuotaDto = hostNodeList.get(0);
					isTrigger = Boolean.TRUE;
					break;
				}
			}
		}
		retMap.put("TRIGGER_RESULT", isTrigger);
		retMap.put("TRIGGER_NODE", nodeQuotaDto);
		logger.info("判断业务程序是否触发节点收缩， 返回结果: " + retMap);
		return retMap;
	}
	
	/**
	 * 获取业务程序集群节点资源信息
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	private void getHostQuotaFromDB(Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("获取主机资源信息， 主机列表: " + hostList + ", 业务参数信息: " + cfgMap);
		//设置连续获取节点资源次数
		String conditionCunt = StringTool.object2String(cfgMap.get("CONDITION_COUNT"));
		if (BlankUtil.isBlank(conditionCunt)) {
			conditionCunt = BusinessConstant.PARAMS_BUS_1;
		}
		
		//获取节点资源数据，重数据库获取
		String [] hostArray = new String[hostList.size()];
		String [] hostQuotaArray = hostList.toArray(hostArray);
		String queryHostParams = "";
		for (String hostStr : hostQuotaArray) {
			queryHostParams += "'" + hostStr + "',"; 
		}
		if (StringUtils.isNotBlank(queryHostParams)) {
			queryHostParams = queryHostParams.substring(0, queryHostParams.length() - 1);
		}
		
		//查询业务量数据
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("TASK_PROGRAM_ID", cfgMap.get("TASK_PROGRAM_ID"));
		queryMap.put("HOST_IPS", queryHostParams);
		queryMap.put("ROW_COUNT", conditionCunt);
		List<HashMap<String, Object>> busiValueList = coreService.queryForList2New("forecastReport.queryBusiValueList", queryMap, dbKey);
		if (!BlankUtil.isBlank(busiValueList)) {
			for (HashMap<String, Object> hashMap : busiValueList) {
				String batchNo = String.valueOf(hashMap.get("BATCH_NO"));
				String hostIp = String.valueOf(hashMap.get("HOST_IP"));
				double busValue = Double.parseDouble(String.valueOf(hashMap.get("BUS_VALUE")));
				double cpuValue = Double.parseDouble(String.valueOf(hashMap.get("CPU_VALUE")));
				double diskValue = Double.parseDouble(String.valueOf(hashMap.get("DISK_VALUE")));
				double memValue = Double.parseDouble(String.valueOf(hashMap.get("MEM_VALUE")));
				double ioInValue = Double.parseDouble(String.valueOf(hashMap.get("IO_IN_VALUE")));
				double ioOutValue = Double.parseDouble(String.valueOf(hashMap.get("IO_OUT_VALUE")));
				ClusterNodeQuotaDto nodeQuotaDto = new ClusterNodeQuotaDto(batchNo, hostIp, cpuValue, diskValue, memValue, ioOutValue, ioInValue, busValue);
				hostQuotaList.add(nodeQuotaDto);
			}
		}
		logger.info("主机资源信息: " + hostQuotaList.toString());
	}
	
	/**
	 * 获取业务程序集群节点资源信息
	 * @param cfgMap
	 * @param hostList
	 * @param hostQuotaList
	 */
	@SuppressWarnings("unchecked")
	private void getHostQuotaFromES(Map<String, Object> cfgMap, HashSet<String> hostList, List<ClusterNodeQuotaDto> hostQuotaList) {
		logger.info("获取主机资源信息， 主机列表: " + hostList + ", 业务参数信息: " + cfgMap);
		//设置连续获取节点资源次数
		String conditionCunt = StringTool.object2String(cfgMap.get("CONDITION_COUNT"));
		if (BlankUtil.isBlank(conditionCunt)) {
			conditionCunt = BusinessConstant.PARAMS_BUS_1;
		}
		
		String [] hostArray = new String[hostList.size()];
		String [] hostQuotaArray = hostList.toArray(hostArray);
		
		//查询业务量数据
		Map<String, Object> busValue = new HashMap<String, Object>();
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("TASK_PROGRAM_ID", cfgMap.get("TASK_PROGRAM_ID"));
		List<HashMap<String, Object>> busiValueList = coreService.queryForList2New("forecastReport.queryBusiValueListWithES", queryMap, dbKey);
		if (!BlankUtil.isBlank(busiValueList)) {
			for (HashMap<String, Object> hashMap : busiValueList) {
				busValue.put(StringTool.object2String(hashMap.get("BATCH_NO")), hashMap.get("BUS_VALUE"));
			}
		}
		
		String busiType = "GTH";
		if (Arrays.asList(rateCenterList).contains(cfgMap.get("PROGRAM_TYPE"))) {
			busiType = "RATE";
		}
		//RATE 批价中心   GTH ：采预中心吞吐量
		NameValuePair daysPair = new NameValuePair("day", conditionCunt);
		NameValuePair busiPair = new NameValuePair("busiType", busiType);
		NameValuePair [] valuePairs = new NameValuePair[]{daysPair, busiPair};
		logger.info("获取主机资源信息， 业务参数: 【" + valuePairs[0] + "】, 【" + valuePairs[1] + "】，主机列表:" + Arrays.asList(hostQuotaArray).toString());
		
		String responseStr = HttpClientUtil.postHttp(GET_HOST_QUOTA_URL, hostQuotaArray, valuePairs);
		if(BlankUtil.isBlank(responseStr)){
			throw new RuntimeException("获取集群节点指标信息失败， 无法预测节点阀值触发时间点！");
		}
		JSONObject hostInfoJson = (JSONObject) JSONObject.parse(responseStr);
		//业务数据
		//Object busiValue = hostInfoJson.get("busiValue");
		
		List<Map<String,Object>> resultList= (List<Map<String,Object>>)hostInfoJson.get("hostInfo");
		for (Map<String, Object> hostMap : resultList) {
			String hostIp = StringTool.object2String(hostMap.get("hostIp"));
			String batchNo = StringTool.object2String(hostMap.get("day"));
			double cpuRateSum = Double.parseDouble(StringTool.object2String(hostMap.get("cpu_percent")));
			double diskTotalRate = Double.parseDouble(StringTool.object2String(hostMap.get("disk_used_percent")));
			double memoryRateSum = Double.parseDouble(StringTool.object2String(hostMap.get("memory_used_percent")));
			double bussRateSum = Double.parseDouble(StringTool.object2String(BlankUtil.isBlank(busValue.get(batchNo)) ? 0 : busValue.get(batchNo)));
			double networkOutRateSum = 0.0;
			double networkInRateSum = 0.0;
			ClusterNodeQuotaDto nodeQuotaDto = new ClusterNodeQuotaDto(batchNo, hostIp, cpuRateSum, diskTotalRate, memoryRateSum, networkOutRateSum, networkInRateSum, bussRateSum);
			hostQuotaList.add(nodeQuotaDto);
		}
		logger.info("主机资源信息: " + hostQuotaList.toString());
	}
	
	/**
	 * 业务程序重新负载 - 修改配置文件
	 * @param taskProgramId
	 * @param hostList
	 */
	private void getTopoHost(String taskProgramId, HashSet<String> hostList) {
		//修改Topology配置文件，新增work分配主机列表
		//根据任务ID查询配置文件
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("TASK_PROGRAM_ID", taskProgramId);
		HashMap<String, Object> taskProgramMap = coreService.queryForObject2New("taskProgram.queryTaskProgram", queryParams, dbKey);
		logger.info("业务程序重新负载，业务程序信息: " + taskProgramMap.toString());
		
		//查询版本发布服务器信息
		FtpDto ftpDto = SessionUtil.getFtpParams();
		//业务配置文件
		String configFileName = StringTool.object2String(taskProgramMap.get("CONFIG_FILE"));
		String packageType = StringTool.object2String(taskProgramMap.get("PACKAGE_TYPE"));
		String versionName = StringTool.object2String(taskProgramMap.get("NAME"));
		String clusterType = StringTool.object2String(taskProgramMap.get("CLUSTER_TYPE"));
		String busClusterCode = StringTool.object2String(taskProgramMap.get("BUS_CLUSTER_CODE"));
		
		//版本发布服务器配置文件真实路径
		String realPath = ftpDto.getFtpRootPath() + Constant.CONF + FileTool.exactPath(Constant.BUSS_CONF)
				+ Constant.RELEASE_DIR + FileTool.exactPath(packageType) + FileTool.exactPath(versionName) 
				+ FileTool.exactPath(busClusterCode)  + FileTool.exactPath(clusterType);
		String realFilePath = realPath + configFileName;
		Trans trans = null;
		InputStream fileStream = null;
		try {
			trans = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			fileStream = trans.get(realFilePath);
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
				hostList.add(StringTool.object2String(hostArray.get(0)));
			}
		}catch(Exception e){
			Log.error("获取业务程序节点集群失败 ，失败原因: ", e);
			throw new RuntimeException("获取业务程序节点集群失败！");
		}finally {
			if (trans != null) {
				trans.close();
			}
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					logger.error("流关闭失败 ，失败原因: ", e);
				}
			}
		}
	}
}
