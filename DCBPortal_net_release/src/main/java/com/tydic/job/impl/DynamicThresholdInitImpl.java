package com.tydic.job.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.minlog.Log;
import com.tydic.bean.*;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.bp.common.utils.tools.StringTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.job.impl]    
  * @ClassName:    [DynamicThresholdInitImpl]     
  * @Description:  [动态节点收缩扩展功能]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-30 上午9:45:19]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-30 上午9:45:19]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service("dynamicThreshold")
public class DynamicThresholdInitImpl {
	
	/**
	 * coreService对象
	 */
	@Autowired
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger logger = Logger.getLogger(DynamicThresholdInitImpl.class);

	/** 日期格式：MMdd*/
    public static final String datePattern = "MMdd";
    
	/**
	 * 默认数据源
	 */
	private static final String dbKey = FrameConfigKey.DEFAULT_DATASOURCE;
	
	/**
	 * 获取指标数据URL
	 */
	private static final String quotaUrl = SystemProperty.getContextProperty("host.quota.month.url");
	
	/**
	 * 批价中心
	 */
	private static final String [] rateCenterList = new String[]{"dcbilling"};
	
	/**
	 * 获取主机资源最近30天
	 */
	private static final String DEFAULT_QUOTA_DAYS = "30";
	
	/**
	 * 预测未来90天结果值
	 */
	private static final int EXPECT_MAX_DAYS = 90;
	
	/**
	 * 默认执行间隔时间
	 */
	private static final int DEFAULT_CRON_EXEC_SEC = 120;
	
	/**
	 * 预测时间前几天执行定时任务扫描
	 */
	private static final int DEFAULT_EXEC_PREV_DAYS = -5;
	
	/**
	 * 预测时间后几天执行定时任务扫描
	 */
	private static final int DEFAULT_EXEC_NEXT_DAYS = 5;
	
	/**
	 * 二次平滑指数
	 */
	private static final Double modulus = 0.7D;
	
	/**
	 * 动态扫描，获取业务对象，生成预测报告
	 */
	public void scanTaskJob() {
		logger.info("定时预测启动...");
		
		//扩容操作
		initExpendClusterData();
		
		//缩容操作
		initUnexpendClusterData();
		
	}

	/**
	 * 动态扩展管理
	 */
	private void initExpendClusterData() {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("OPERATOR_TYPE", BusinessConstant.PARAMS_BUS_7);
		
		//获取动态扩展配置
		List<DynamicThresholdDto> dynamicList = new ArrayList<DynamicThresholdDto>();
		this.getDynamicThresholdList(queryMap, dynamicList);
		
		//预测判断主机是否能触发阀值
		for (DynamicThresholdDto thresholdDto : dynamicList) {
			//动态阀值指标信息
			List<DynamicHostQuotaDto> hostQuotaList = new ArrayList<DynamicHostQuotaDto>();
			
			//触发阀值的策略配置信息
			List<DynamicHostNodePredictionDto> triggerStrategyList = new ArrayList<DynamicHostNodePredictionDto>();
			//不触发阀值的策略配置信息
			List<DynamicHostNodePredictionDto> unTriggerStrategyList = new ArrayList<DynamicHostNodePredictionDto>();
			
			//获取主机连续一个月主机资源
			//this.getHostQuotaListFromDB(thresholdDto, hostQuotaList);
			this.getHostQuotaListFromES(thresholdDto, hostQuotaList);

			//判断主机资源类型是否达到阀值，如果未达到阀值预测是否会达到阀值
			this.getForecastHostQuota(thresholdDto, hostQuotaList, triggerStrategyList, unTriggerStrategyList);
			logger.debug("触发策略列表：" + (triggerStrategyList == null ? 0 : triggerStrategyList.size()) + ", 不触发策略列表:" + (unTriggerStrategyList == null ? 0: unTriggerStrategyList.size()));
			//预测在最近一个月将会达到阀值，如果达到阀值则添加扩展报告并且生成扩展节点定时任务
			if (!BlankUtil.isBlank(triggerStrategyList)) {
				//添加预测扩展报告
				String forecastReportId = this.addExpendForecastReport(BusinessConstant.PARAMS_BUS_1, thresholdDto, triggerStrategyList);
				//添加扩容定时器
				this.addJobTask(forecastReportId, thresholdDto, triggerStrategyList);
			} else {
				//添加扩展报告
				this.addExpendForecastReport(BusinessConstant.PARAMS_BUS_0, thresholdDto, unTriggerStrategyList);
			}
		}
	}
	
	/**
	 * 动态收缩管理
	 */
	private void initUnexpendClusterData() {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("OPERATOR_TYPE", BusinessConstant.PARAMS_BUS_8);
		
		//获取动态收缩配置
		List<DynamicThresholdDto> dynamicList = new ArrayList<DynamicThresholdDto>();
		this.getDynamicThresholdList(queryMap, dynamicList);
		
		//预测判断主机是否能触发阀值
		for (DynamicThresholdDto thresholdDto : dynamicList) {
			//动态阀值指标信息
			List<DynamicHostQuotaDto> hostQuotaList = new ArrayList<DynamicHostQuotaDto>();
			
			//触发阀值的策略配置信息
			List<DynamicHostNodePredictionDto> triggerStrategyList = new ArrayList<DynamicHostNodePredictionDto>();
			//不触发阀值的策略配置信息
			List<DynamicHostNodePredictionDto> unTriggerStrategyList = new ArrayList<DynamicHostNodePredictionDto>();
			
			//获取主机连续一个月主机资源
			//this.getHostQuotaListFromDB(thresholdDto, hostQuotaList);
			this.getHostQuotaListFromES(thresholdDto, hostQuotaList);
			
			//判断主机资源类型是否达到阀值，如果未达到阀值预测是否会达到阀值
			this.getContractsForecastHostQuota(thresholdDto, hostQuotaList, triggerStrategyList, unTriggerStrategyList);
			
			if(!BlankUtil.isBlank(triggerStrategyList)) {
				//添加扩容报告
				String forecaseReportId = this.addUnexpendForecastReport(BusinessConstant.PARAMS_BUS_1, thresholdDto, triggerStrategyList);
				//添加收缩定时器
				this.addJobTask(forecaseReportId, thresholdDto, triggerStrategyList);
			} else {
				//添加收缩报告
				this.addUnexpendForecastReport(BusinessConstant.PARAMS_BUS_0, thresholdDto, unTriggerStrategyList);
			}
		}
	}
	
	/**
	 * 查询并且构建策略配置信息，当前业务程序包含业务集群主机、业务包含配置规则
	 * 
	 * @param queryMap 查询参数
	 * @param dynamicList 业务程序集合对象
	 * @return
	 */
	private void getDynamicThresholdList(Map<String, Object> queryMap, List<DynamicThresholdDto> dynamicList) {
		logger.info("查询策略配置， 业务参数: " + queryMap);
		//查询所有的扩展配置
		List<HashMap<String, Object>> clusterNodeList = coreService.queryForList2New("expendStrategyConfig.queryProgramNodeList", queryMap, dbKey);
		if (!BlankUtil.isBlank(clusterNodeList)) {
			for (HashMap<String, Object> nodeMap : clusterNodeList) {
				//业务程序ID
				String taskProgramId = StringTool.object2String(nodeMap.get("TASK_PROGRAM_ID"));
				
				boolean isExist = false;
				for (DynamicThresholdDto dynamicDto : dynamicList) {
					if (taskProgramId.equals(dynamicDto.getTaskProgramId())) {
						dynamicDto.getStrategyList().add(this.getThresholdStrategyDto(nodeMap));
						//查询业务程序对应的主机列表
						//dynamicDto.getHostList().add(StringTool.object2String(nodeMap.get("HOST_IP")));
						isExist = true;
						break;
					}
				}
				
				//当前业务程序不存在，创建DTO对象并且赋值添加到集合中
				if (!isExist) {
					DynamicThresholdDto thresholdDto = new DynamicThresholdDto();
					thresholdDto.setClusterId(StringTool.object2String(nodeMap.get("CLUSTER_ID")));
					thresholdDto.setTaskProgramId(StringTool.object2String(nodeMap.get("TASK_PROGRAM_ID")));
					thresholdDto.setProgramType(StringTool.object2String(nodeMap.get("CLUSTER_TYPE")));
					thresholdDto.getStrategyList().add(this.getThresholdStrategyDto(nodeMap));
					//查询业务程序对应的主机列表
					//thresholdDto.getHostList().add(StringTool.object2String(nodeMap.get("HOST_IP")));
					dynamicList.add(thresholdDto);
				}
			}
			
			//设置当前业务程序真实的主机IP列表
			for (DynamicThresholdDto dynamicDto : dynamicList) {
				String taskProgramId = dynamicDto.getTaskProgramId();
				this.getTopoHost(taskProgramId, dynamicDto.getHostList());
			}
		}
		logger.info("查询策略配置结束， 结果: " + dynamicList.toString());
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
	
	/**
	 * 添加定时任务
	 * @param forecaseReportId 预测报告ID
	 * @param thresholdDto 动态策略DTO对象
	 * @param strategyList
	 */
	private void addJobTask(String forecaseReportId, DynamicThresholdDto thresholdDto, List<DynamicHostNodePredictionDto> strategyList) {
		logger.info("预测触发阀值， 添加定时任务开始, 业务程序ID: " + thresholdDto.getTaskProgramId() + ", 预测报告ID: " + forecaseReportId);
		
		DynamicHostNodePredictionDto strategyDto = strategyList.get(0);
		String strategyId = strategyList.get(0).getStrategyId();
		//添加定时任务，在触发阀值的前后5天添加定时任务，每个5分钟执行一次
		Map<String, Object> jobMap = new HashMap<String, Object>();
		jobMap.put("TASK_NAME", strategyId + "触发任务");
		jobMap.put("TASK_TYPE", BusinessConstant.PARAMS_BUS_2);
		jobMap.put("CRON_EXP", DEFAULT_CRON_EXEC_SEC);
		jobMap.put("CRON_DESC", "每隔: " + DEFAULT_CRON_EXEC_SEC + "秒执行一次");
		jobMap.put("CRON_START_TIME", this.addDate(DateUtil.parse(strategyDto.getPredictionTime(), DateUtil.allPattern), Calendar.DAY_OF_MONTH, DEFAULT_EXEC_PREV_DAYS));
		jobMap.put("CRON_END_TIME", this.addDate(DateUtil.parse(strategyDto.getPredictionTime(), DateUtil.allPattern), Calendar.DAY_OF_MONTH, DEFAULT_EXEC_NEXT_DAYS));
		jobMap.put("CRON_STATUS", BusinessConstant.PARAMS_BUS_1);
		coreService.insertObject2New("jobtaskcfg.insertTask", jobMap, dbKey);
		logger.info("定时任务定义表添加成功， 添加业务参数: " + jobMap);
		
		//添加定时任务执行计划
		String taskId = StringTool.object2String(jobMap.get("TASK_ID"));
		Map<String, Object> taskJobMap = new HashMap<String, Object>();
		taskJobMap.put("TASK_ID", taskId);
		taskJobMap.put("BUS_TYPE", BusinessConstant.PARAMS_BUS_4);
		taskJobMap.put("BUS_ID", strategyDto.getStrategyId());
		taskJobMap.put("EXTENDED_FIELD", forecaseReportId);
		coreService.insertObject2New("jobTaskBus.insert", taskJobMap, dbKey);
		logger.info("定时任务关联添加成功， 添加业务参数: " + taskJobMap);
	}
	
	/**
	 * 添加扩展预测报告
	 * @param status
	 * @param thresholdDto
	 * @param strategyList
	 */
	private String addExpendForecastReport(String status, DynamicThresholdDto thresholdDto, List<DynamicHostNodePredictionDto> strategyList) {
		logger.info("添加动态扩展预测报告开始， 业务程序ID： " + thresholdDto.getTaskProgramId());
		
		DynamicHostNodePredictionDto strategyDto = strategyList.get(0);
		Map<String, Object> addMap = new HashMap<String, Object>();
		addMap.put("TASK_PROGRAM_ID", strategyDto.getTaskProgramId());
		addMap.put("STRATEGY_ID", strategyDto.getStrategyId());
		addMap.put("OPERATOR_TYPE", strategyDto.getOperatorType());
		
		int cpuValue = new Double(strategyDto.getPredictionCpu()).intValue();
		int memValue = new Double(strategyDto.getPredictionMem()).intValue();
		int diskValue = new Double(strategyDto.getPredictionDisk()).intValue();
		int bussValue = new Double(strategyDto.getPredictionBuss()).intValue();
		
		addMap.put("CPU", cpuValue);
		addMap.put("MEM", memValue);
		addMap.put("DISK", diskValue);
		addMap.put("BUSS_VOLUME", bussValue);
		addMap.put("PREDICTION_TIME", strategyDto.getPredictionTime());
		if (BusinessConstant.PARAMS_BUS_1.equals(status)) {
			StringBuilder builder = new StringBuilder();
			builder.append("当前集群预计在：").append(strategyDto.getPredictionTime()).append(" 触发阀值,即在");
			builder.append(strategyDto.getDays()).append("天后触发节点扩展");
			builder.append("，预计触发扩展阀值时节点[").append(strategyDto.getHostIp()).append("]信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			addMap.put("RESULT_DESC", builder.toString());
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("当前集群在未来").append(EXPECT_MAX_DAYS).append("天不会触发扩展阀值，在第").append(EXPECT_MAX_DAYS).append("天预计集群最大消耗主机资源节点[").append(strategyDto.getHostIp()).append("]信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			addMap.put("RESULT_DESC", builder.toString());
		}
		addMap.put("STATUS", status);
		//报告依据字段
		addMap.put("PREDICTION_DATA", JSON.toJSONString(strategyDto.getPredictionMap()));
		coreService.insertObject2New("forecastReport.addForecastReport", addMap, dbKey);
		logger.info("动态动态扩展预测报告生成， 报告参数: " + addMap.toString());
		logger.info("添加动态扩展报告完成， 业务程序ID： " + thresholdDto.getTaskProgramId());
		
		return StringTool.object2String(addMap.get("ID"));
	}
	
	/**
	 * 添加收缩预测报告
	 * @param status
	 * @param thresholdDto
	 * @param strategyList
	 */
	private String addUnexpendForecastReport(String status, DynamicThresholdDto thresholdDto, List<DynamicHostNodePredictionDto> strategyList) {
		logger.info("添加节点收缩预测报告开始， 业务程序ID： " + thresholdDto.getTaskProgramId());
		
		DynamicHostNodePredictionDto strategyDto = strategyList.get(strategyList.size() - 1);
		Map<String, Object> addMap = new HashMap<String, Object>();
		addMap.put("TASK_PROGRAM_ID", strategyDto.getTaskProgramId());
		addMap.put("STRATEGY_ID", strategyDto.getStrategyId());
		addMap.put("OPERATOR_TYPE", strategyDto.getOperatorType());
		int cpuValue = new Double(strategyDto.getPredictionCpu()).intValue();
		int memValue = new Double(strategyDto.getPredictionMem()).intValue();
		int diskValue = new Double(strategyDto.getPredictionDisk()).intValue();
		int bussValue = new Double(strategyDto.getPredictionBuss()).intValue();
		
		addMap.put("CPU", cpuValue);
		addMap.put("MEM", memValue);
		addMap.put("DISK", diskValue);
		addMap.put("BUSS_VOLUME", bussValue);
		addMap.put("PREDICTION_TIME", strategyDto.getPredictionTime());
		if (BusinessConstant.PARAMS_BUS_1.equals(status)) {
			StringBuilder builder = new StringBuilder();
			builder.append("当前集群预计在：").append(strategyDto.getPredictionTime()).append(" 触发阀值,即在");
			builder.append(strategyDto.getDays()).append("天后触发节点收缩");
			builder.append("，预计触发收缩阀值时节点[").append(strategyDto.getHostIp()).append("]信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			addMap.put("RESULT_DESC", builder.toString());
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("当前集群在未来").append(EXPECT_MAX_DAYS).append("天不会触发收缩阀值，在第").append(EXPECT_MAX_DAYS).append("天预计集群最大消耗主机资源节点[").append(strategyDto.getHostIp()).append("]信息如下：");
			builder.append("CPU使用率：").append(cpuValue);
			builder.append("%，内存使用率：").append(memValue);
			builder.append("%，磁盘使用率：").append(diskValue);
			builder.append("%，业务量：").append(bussValue);
			addMap.put("RESULT_DESC", builder.toString());
		}
		addMap.put("STATUS", status);
		
		//报告依据字段
		addMap.put("PREDICTION_DATA", JSON.toJSONString(strategyDto.getPredictionMap()));
				
		coreService.insertObject2New("forecastReport.addForecastReport", addMap, dbKey);
		logger.info("动态收缩预测报告生成， 报告参数: " + addMap.toString());
		logger.info("添加动态收缩报告完成， 业务程序ID： " + thresholdDto.getTaskProgramId());
		return StringTool.object2String(addMap.get("ID"));
	}
	
	/**
	 * 根据预测时间进行排序，判断那个策略最新执行扩缩容
	 * @param thresholdDto 
	 * @param quotaList
	 * @param triggerStrategyList 触发规则的策略配置信息
	 * @return triggerList 返回触发阀值的任务
	 */
	private void getForecastHostQuota(DynamicThresholdDto thresholdDto, List<DynamicHostQuotaDto> quotaList, 
			List<DynamicHostNodePredictionDto> triggerStrategyList, List<DynamicHostNodePredictionDto> unTriggerStrategyList) {
		logger.info("获取动态扩展配置规则预期阀值， 当前业务: " + thresholdDto.getTaskProgramId());
		
		//当前业务程序所有策略配置
		List<DynamicThresholdStrategyDto> strategyList = thresholdDto.getStrategyList();

		// 计算每个策略对应的阀值
		for (DynamicThresholdStrategyDto strategyDto : strategyList) {
			this.getFocecaseResult(strategyDto, quotaList);
		}

		// 获取策略中最早的触发时间点
		for (DynamicThresholdStrategyDto strategyDto : strategyList) {
			List<DynamicHostNodePredictionDto> predictionList = strategyDto.getPredictioList();
			for (DynamicHostNodePredictionDto dynamicHostNodePredictionDto : predictionList) {
				if (BusinessConstant.PARAMS_BUS_1.equals(dynamicHostNodePredictionDto.getTriggerRst())) {
					triggerStrategyList.add(dynamicHostNodePredictionDto);
				} else {
					unTriggerStrategyList.add(dynamicHostNodePredictionDto);
				}
			}
		}
		Collections.sort(triggerStrategyList, new Comparator<DynamicHostNodePredictionDto>() {
			@Override
			public int compare(DynamicHostNodePredictionDto dto1, DynamicHostNodePredictionDto dto2) {
				long dto1PredictionTimes = DateUtil.parse(dto1.getPredictionTime(), DateUtil.allPattern).getTime();
				long dto2PredictionTimes = DateUtil.parse(dto2.getPredictionTime(), DateUtil.allPattern).getTime();
				return (int) (dto1PredictionTimes - dto2PredictionTimes);
			}
		});
	}
	
	/**
	 * 根据预测时间，判断那个主机节点最慢达到收缩阀值
	 * @param thresholdDto 
	 * @param quotaList
	 * @param triggerStrategyList 触发规则的策略配置信息
	 * @return triggerList 返回触发阀值的任务
	 */
	private void getContractsForecastHostQuota(DynamicThresholdDto thresholdDto, List<DynamicHostQuotaDto> quotaList, 
			List<DynamicHostNodePredictionDto> triggerStrategyList, List<DynamicHostNodePredictionDto> unTriggerStrategyList) {
		logger.info("获取动态收缩配置规则预期阀值， 当前业务: " + thresholdDto.getTaskProgramId());
		
		//当前业务程序所有对应的策略信息
		List<DynamicThresholdStrategyDto> strategyList = thresholdDto.getStrategyList();

		// 计算每个策略对应的阀值
		for (DynamicThresholdStrategyDto strategyDto : strategyList) {
			this.getContractsFocecaseResult(strategyDto, quotaList);
		}

		// 获取策略中最早的触发时间点
		for (DynamicThresholdStrategyDto strategyDto : strategyList) {
			List<DynamicHostNodePredictionDto> predictionList = strategyDto.getPredictioList();
			for (DynamicHostNodePredictionDto dynamicHostNodePredictionDto : predictionList) {
				if (BusinessConstant.PARAMS_BUS_1.equals(dynamicHostNodePredictionDto.getTriggerRst())) {
					triggerStrategyList.add(dynamicHostNodePredictionDto);
				} else {
					unTriggerStrategyList.add(dynamicHostNodePredictionDto);
				}
			}
		}
		Collections.sort(triggerStrategyList, new Comparator<DynamicHostNodePredictionDto>() {
			@Override
			public int compare(DynamicHostNodePredictionDto dto1, DynamicHostNodePredictionDto dto2) {
				long dto1PredictionTimes = DateUtil.parse(dto1.getPredictionTime(), DateUtil.allPattern).getTime();
				long dto2PredictionTimes = DateUtil.parse(dto2.getPredictionTime(), DateUtil.allPattern).getTime();
				return (int)(dto2PredictionTimes - dto1PredictionTimes);
			}
		});
	}
	
	/**
	 * 预测主机资源超过阀值的时间点
	 * @param strategyDto  策略Dto
	 * @param hostQuotaList 主机资源信息
	 */
	private void getContractsFocecaseResult(DynamicThresholdStrategyDto strategyDto, List<DynamicHostQuotaDto> hostQuotaList) {
		logger.info("节点收缩，获取配置规则预期阀值， 配置规则ID: " + strategyDto.getStrategyId() + ", 集群ID: " + strategyDto.getClusterId());
		//获取策略配置的阀值
		String conditionValue = strategyDto.getConditionValue();
		Double thresholdValue = Double.parseDouble(conditionValue);
		
		//主机资源类型
		String quotaType = strategyDto.getQuotaType();
		
		for (DynamicHostQuotaDto quotaDto : hostQuotaList) {
			List<Double> quotaList = null;
			if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) { // CPU
				// 判断当前主机CPU资源是否达到阀值
				quotaList = quotaDto.getCpuList();
			} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) { // 内存
				quotaList = quotaDto.getMemList();
			} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) { // 磁盘
				quotaList = quotaDto.getDiskList();
			} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) { // 业务量
				quotaList = quotaDto.getBussList();
			}
//			Double cpuAvg = this.getQuotaAvg(quotaList);
//			logger.info("主机资源平均信息， 主机IP: " + quotaDto.getHostIp() + ", 指标类型: " + quotaType + ", 平均值: " + cpuAvg + ", 当前配置规则阀值： " + thresholdValue);
//			if (thresholdValue >= cpuAvg) {
//				strategyDto.setTriggerRst(BusinessConstant.PARAMS_BUS_1);
//				strategyDto.setPredictionTime(DateUtil.getCurrent(DateUtil.allPattern));
//			} else {
				//预测当前时间下一个月是否会触发扩容(默认初始值为1000D)
				Double currPredictionVal = 100000000000D;
				int days = 1;				
				List<Map<String, Object>> predictionQuotaList = new ArrayList<Map<String, Object>>(); 
				while (thresholdValue < currPredictionVal && days < EXPECT_MAX_DAYS) {
					currPredictionVal = this.getExpectPredictionData(quotaList, days, modulus);
					if (!BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {
						currPredictionVal = currPredictionVal > 100 ? 100D : currPredictionVal;
					}
					logger.info("收缩预测主机资源信息，主机IP: " + quotaDto.getHostIp() + ", 指标类型: " + quotaType + ", 预测批次: " + days + ", 预测值: " + currPredictionVal);
					//预测对象
					Map<String, Object> quotaPredMap = new HashMap<String, Object>();
					quotaPredMap.put("BATCH_NO", getCurrAddDate(days));
					quotaPredMap.put("PREDICTION_VALUE", currPredictionVal.intValue());
					predictionQuotaList.add(quotaPredMap);
					
					days++;
				}
				logger.info("节点收缩，主机IP: " + quotaDto.getHostIp() + ", 阀值: " + thresholdValue + ", 预测最新值: " + currPredictionVal + ", 预测天数: " + days + "， 预测指标: " + quotaType);
				
				
				//设置指标对象
				Map<String, Object> predictionMap = new HashMap<String, Object>();
				predictionMap.put("HOST_IP", quotaDto.getHostIp());
				predictionMap.put("DATA", predictionQuotaList);
				predictionMap.put("QUOTA_TYPE", quotaType);
				
				//预测结果，每个主机预测结果
				DynamicHostNodePredictionDto predictionDto = new DynamicHostNodePredictionDto();
				predictionDto.setHostIp(quotaDto.getHostIp());
				predictionDto.setClusterId(strategyDto.getClusterId());
				predictionDto.setConditionParam(strategyDto.getConditionParam());
				predictionDto.setConditionValue(strategyDto.getConditionValue());
				predictionDto.setOperatorType(strategyDto.getOperatorType());
				predictionDto.setTaskProgramId(strategyDto.getTaskProgramId());
				predictionDto.setStrategyId(strategyDto.getStrategyId());
				predictionDto.setQuotaType(strategyDto.getQuotaType());
				predictionDto.setPredictionMap(predictionMap);
				
				//在本周期内触发阀值
				if (currPredictionVal <= thresholdValue) {
					predictionDto.setTriggerRst(BusinessConstant.PARAMS_BUS_1);
					predictionDto.setPredictionTime(this.addDate(DateUtil.getCurrentDate(), Calendar.DAY_OF_MONTH, days));
				} else {
					predictionDto.setTriggerRst(BusinessConstant.PARAMS_BUS_0);
					predictionDto.setPredictionTime(this.addDate(DateUtil.getCurrentDate(), Calendar.DAY_OF_MONTH, EXPECT_MAX_DAYS));
				}
				//CPU预测值
				Double predictionCpu = this.getExpectPredictionData(quotaDto.getCpuList(), days, modulus);
				predictionCpu = predictionCpu > 100 ? 100D : predictionCpu;	
				predictionDto.setPredictionCpu(predictionCpu);
				
				//内存预测值
				Double predictionMem = this.getExpectPredictionData(quotaDto.getMemList(), days, modulus);
				predictionMem = predictionMem > 100 ? 100D : predictionMem;
				predictionDto.setPredictionMem(predictionMem);
				
				//磁盘预测值
				Double predictionDisk = this.getExpectPredictionData(quotaDto.getDiskList(), days, modulus);
				predictionDisk = predictionDisk > 100 ? 100D : predictionDisk;	
				predictionDto.setPredictionDisk(predictionDisk);
				
				//业务量预测值
				predictionDto.setPredictionBuss(this.getExpectPredictionData(quotaDto.getBussList(), days, modulus));
				//预测天数
				predictionDto.setDays(days);
				//该策略对应的主机预测信息
				strategyDto.getPredictioList().add(predictionDto);
				logger.info("节点收缩，策略配置触发信息: " + predictionDto);
			}
//		}
	}
	
	/**
	 * 预测主机资源超过阀值的时间点
	 * @param strategyDto  策略Dto
	 * @param hostQuotaList 主机资源信息
	 */
	private void getFocecaseResult(DynamicThresholdStrategyDto strategyDto, List<DynamicHostQuotaDto> hostQuotaList) {
		logger.info("获取配置规则预期阀值， 配置规则ID: " + strategyDto.getStrategyId() + ", 集群ID: " + strategyDto.getClusterId());
		//获取策略配置的阀值
		String conditionValue = strategyDto.getConditionValue();
		Double thresholdValue = Double.parseDouble(conditionValue);
		
		//主机资源类型
		String quotaType = strategyDto.getQuotaType();
		
		//预测指标对象
		for (DynamicHostQuotaDto quotaDto : hostQuotaList) {
			List<Double> quotaList = null;
			if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) { // CPU
				// 判断当前主机CPU资源是否达到阀值
				quotaList = quotaDto.getCpuList();
			} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) { // 内存
				quotaList = quotaDto.getMemList();
			} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) { // 磁盘
				quotaList = quotaDto.getDiskList();
			} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) { // 业务量
				quotaList = quotaDto.getBussList();
			}
//			Double cpuAvg = this.getQuotaAvg(quotaList);
//			logger.info("主机资源平均信息， 主机IP: " + quotaDto.getHostIp() + ", 指标类型: " + quotaType + ", 平均值: " + cpuAvg + ", 当前配置规则阀值： " + thresholdValue);
//			if (thresholdValue <= cpuAvg) {
//				strategyDto.setTriggerRst(BusinessConstant.PARAMS_BUS_1);
//				strategyDto.setPredictionTime(DateUtil.getCurrent(DateUtil.allPattern));
//			} else {
				//预测当前时间下一个月是否会触发扩容
				Double currPredictionVal = 0.0;
				int days = 1;
				List<Map<String, Object>> predictionQuotaList = new ArrayList<Map<String, Object>>(); 
				while (thresholdValue >= currPredictionVal && days < EXPECT_MAX_DAYS) {
					currPredictionVal = this.getExpectPredictionData(quotaList, days, modulus);
					if(!BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {
						currPredictionVal = currPredictionVal > 100 ? 100D : currPredictionVal;	
					}
					logger.info("扩展预测主机资源信息，主机IP: " + quotaDto.getHostIp() + ", 指标类型: " + quotaType + ", 预测批次: " + days + ", 预测值: " + currPredictionVal);
					//预测对象
					Map<String, Object> quotaPredMap = new HashMap<String, Object>();
					quotaPredMap.put("BATCH_NO", getCurrAddDate(days));
					quotaPredMap.put("PREDICTION_VALUE", currPredictionVal.intValue());
					predictionQuotaList.add(quotaPredMap);
					days++;
				}
				logger.info("主机IP: " + quotaDto.getHostIp() + ", 阀值: " + thresholdValue + ", 预测最新值: " + currPredictionVal + ", 预测天数: " + days + "， 预测指标: " + quotaType);
				
				
				//设置指标对象
				Map<String, Object> predictionMap = new HashMap<String, Object>();
				predictionMap.put("HOST_IP", quotaDto.getHostIp());
				predictionMap.put("DATA", predictionQuotaList);
				predictionMap.put("QUOTA_TYPE", quotaType);
				
				//预测结果，每个主机预测结果
				DynamicHostNodePredictionDto predictionDto = new DynamicHostNodePredictionDto();
				predictionDto.setHostIp(quotaDto.getHostIp());
				predictionDto.setClusterId(strategyDto.getClusterId());
				predictionDto.setConditionParam(strategyDto.getConditionParam());
				predictionDto.setConditionValue(strategyDto.getConditionValue());
				predictionDto.setOperatorType(strategyDto.getOperatorType());
				predictionDto.setTaskProgramId(strategyDto.getTaskProgramId());
				predictionDto.setStrategyId(strategyDto.getStrategyId());
				predictionDto.setQuotaType(strategyDto.getQuotaType());
				
				predictionDto.setPredictionMap(predictionMap);
				
				//在本周期内触发阀值
				if (currPredictionVal > thresholdValue) {
					predictionDto.setTriggerRst(BusinessConstant.PARAMS_BUS_1);
					predictionDto.setPredictionTime(this.addDate(DateUtil.getCurrentDate(), Calendar.DAY_OF_MONTH, days));
				} else {
					predictionDto.setTriggerRst(BusinessConstant.PARAMS_BUS_0);
					predictionDto.setPredictionTime(this.addDate(DateUtil.getCurrentDate(), Calendar.DAY_OF_MONTH, EXPECT_MAX_DAYS));
				}
				
				//CPU预测值
				Double predictionCpu = this.getExpectPredictionData(quotaDto.getCpuList(), days, modulus);
				predictionCpu = predictionCpu > 100 ? 100D : predictionCpu;
				predictionDto.setPredictionCpu(predictionCpu);
				
				//内存预测值
				Double predictionMem = this.getExpectPredictionData(quotaDto.getMemList(), days, modulus);
				predictionMem = predictionMem > 100 ? 100D : predictionMem;
				predictionDto.setPredictionMem(predictionMem);
				
				//磁盘预测值
				Double predictionDisk = this.getExpectPredictionData(quotaDto.getDiskList(), days, modulus);
				predictionDisk = predictionDisk > 100 ? 100D : predictionDisk;
				predictionDto.setPredictionDisk(predictionDisk);
				
				//业务量预测值
				predictionDto.setPredictionBuss(this.getExpectPredictionData(quotaDto.getBussList(), days, modulus));
				//预测天数
				predictionDto.setDays(days);
				//该策略对应的主机预测信息
				strategyDto.getPredictioList().add(predictionDto);
				logger.info("策略配置触发信息: " + predictionDto);
//			}
		}
	}
	
	/**
	 * 变更时间
	 * @param date
	 * @param calendarField
	 * @param amount
	 * @return
	 */
    private String addDate(Date date, int calendarField, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return DateUtil.format(c.getTime(), DateUtil.allPattern);
    }
	
	/**
	 * 获取当前月最大天数
	 * @return
	 */
	private int getCurrMaxDays() {
		Calendar cal = Calendar.getInstance();
		int maxDate = cal.getActualMaximum(Calendar.DATE);
		return maxDate;
	}
	
	/**
	 * 获取当前时间天剑天数
	 * @param days
	 * @return
	 */
	private String getCurrAddDate(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, days);
		return DateUtil.format(cal.getTime(), datePattern); 
	}
	
	/**
	 * 预测达到大致的时间点(一天每台一条数据)
	 * @param list
	 * @param days
	 * @param modulus
	 * @return
	 */
	private Double getExpectPredictionData(List<Double> list, int days, Double modulus) {
		if (list.size() < 2 || modulus <= 0 || modulus >= 1) {
			return null;
		}
		Double modulusLeft = 1 - modulus;
		Double lastIndex = list.get(0);
		Double lastSecIndex = list.get(0);
		for (Double data : list) {
			lastIndex = modulus * data + modulusLeft * lastIndex;
			lastSecIndex = modulus * lastIndex + modulusLeft * lastSecIndex;
		}
		Double a = 2 * lastIndex - lastSecIndex;
		Double b = (modulus / modulusLeft) * (lastIndex - lastSecIndex);
		return (a + b * days) < 0 ? 0 : (a + b * days);
	}
	
	
	/**
	 * 获取集群中主机资源信息
	 * @param thresholdDto
	 * @return
	 */
	private void getHostQuotaListFromDB(DynamicThresholdDto thresholdDto, List<DynamicHostQuotaDto> dynamicQuotaList) {
		logger.info("获取指标数据信息， 参数： " + thresholdDto.toString());
		String [] hostStrArray = new String[thresholdDto.getHostList().size() - 1];
		String [] ips = thresholdDto.getHostList().toArray(hostStrArray);
		logger.info("获取主机资源信息， 主机: " +  Arrays.asList(ips).toString() + ", 获取主机时间长度: " + EXPECT_MAX_DAYS + ", 访问资源地址: " + quotaUrl + "， 业务程序ID: " + thresholdDto.getTaskProgramId());

		String queryHostParams = "";
		for (String hostStr : ips) {
			queryHostParams += "'" + hostStr + "',"; 
		}
		if (StringUtils.isNotBlank(queryHostParams)) {
			queryHostParams = queryHostParams.substring(0, queryHostParams.length() - 1);
		}
		
		//主机资源集合列表
		List<ClusterNodeQuotaDto> quotaList = new ArrayList<ClusterNodeQuotaDto>();
		HashSet<String> hostSet = new HashSet<String>();
		
		//查询业务量数据
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("TASK_PROGRAM_ID", thresholdDto.getTaskProgramId());
		queryMap.put("HOST_IPS", queryHostParams);
		queryMap.put("ROW_COUNT", EXPECT_MAX_DAYS);
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
				quotaList.add(nodeQuotaDto);
				hostSet.add(hostIp);
			}
		}
		
		if (!BlankUtil.isBlank(quotaList)) {
			//根据批次号进行升序排序
			Collections.sort(quotaList, new Comparator<ClusterNodeQuotaDto>() {
				@Override
				public int compare(ClusterNodeQuotaDto nodeDto1, ClusterNodeQuotaDto nodeDto2) {
					int batchNo1 = Integer.parseInt(nodeDto1.getBatchNo());
					int batchNo2 = Integer.parseInt(nodeDto2.getBatchNo());
					return batchNo1 - batchNo2;
				}
			});
			
			for (String hostIp : hostSet) {
				DynamicHostQuotaDto hostQuotaDto = new DynamicHostQuotaDto();
				hostQuotaDto.setHostIp(hostIp);
				
				for (ClusterNodeQuotaDto quotaDto : quotaList) {
					String quotaHostIp = quotaDto.getHostIp();
					if (hostIp.equals(quotaHostIp)) {
						hostQuotaDto.getCpuList().add(quotaDto.getCpuRateSum());
						hostQuotaDto.getMemList().add(quotaDto.getMemoryRateSum());
						hostQuotaDto.getDiskList().add(quotaDto.getDiskTotalRate());
						hostQuotaDto.getBussList().add(quotaDto.getBussSum());
						hostQuotaDto.getBatchNoList().add(quotaDto.getBatchNo());
					}
				}
				dynamicQuotaList.add(hostQuotaDto);
			}
		}
	}
	
	/**
	 * 获取集群中主机资源信息
	 * @param thresholdDto
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void getHostQuotaListFromES(DynamicThresholdDto thresholdDto, List<DynamicHostQuotaDto> dynamicQuotaList) {
		logger.info("获取指标数据信息， 参数： " + thresholdDto.toString());
		String [] hostStrArray = new String[thresholdDto.getHostList().size() - 1];
		String [] ips = thresholdDto.getHostList().toArray(hostStrArray);
		logger.info("获取主机资源信息， 主机: " +  Arrays.asList(ips).toString() + ", 获取主机时间长度: " + DEFAULT_QUOTA_DAYS + ", 访问资源地址: " + quotaUrl + "， 业务程序ID: " + thresholdDto.getTaskProgramId());

		//主机资源集合列表
		List<ClusterNodeQuotaDto> quotaList = new ArrayList<ClusterNodeQuotaDto>();
		HashSet<String> hostSet = new HashSet<String>();
		
		//查询业务量数据
		Map<String, Object> busValue = new HashMap<String, Object>();
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("TASK_PROGRAM_ID", thresholdDto.getTaskProgramId());
		List<HashMap<String, Object>> busiValueList = coreService.queryForList2New("forecastReport.queryBusiValueListWithES", queryMap, dbKey);
		if (!BlankUtil.isBlank(busiValueList)) {
			for (HashMap<String, Object> hashMap : busiValueList) {
				busValue.put(StringTool.object2String(hashMap.get("BATCH_NO")), hashMap.get("BUS_VALUE"));
			}
		}
		
		try {
			String busiType = "GTH";
			if (Arrays.asList(rateCenterList).contains(thresholdDto.getProgramType())) {
				busiType = "RATE";
			}
			//RATE 批价中心   GTH ：采预中心吞吐量
			NameValuePair daysPair = new NameValuePair("day", DEFAULT_QUOTA_DAYS);
			NameValuePair busiPair = new NameValuePair("busiType", busiType);
			NameValuePair [] valuePairs = new NameValuePair[]{daysPair, busiPair};
			logger.info("获取主机资源信息， 业务参数: 【" + valuePairs[0] + "】, 【" + valuePairs[1] + "】，主机列表:" + Arrays.asList(ips).toString());
			
			String responseStr = HttpClientUtil.postHttp(quotaUrl, ips, valuePairs);
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
				quotaList.add(nodeQuotaDto);
				hostSet.add(hostIp);
			}
			logger.info("主机资源信息: " + quotaList.toString());
		} catch (RuntimeException e) {
			logger.error("-->获取指标信息失败， 失败原因: ", e);
			throw e;
		} catch (Exception e) {
			logger.error("获取指标信息失败， 失败原因:", e);
			throw new RuntimeException("获取集群节点指标信息失败， 无法预测节点阀值触发时间点！");
		}
		
//		for (String ip : ips) {
//			List<ClusterNodeQuotaDto> quotaList1 = new ArrayList<ClusterNodeQuotaDto>();
//			CreateData.addTempData(quotaList1, ip);
//			quotaList.addAll(quotaList1);
//			hostSet.add(ip);
//		}
			
		if (!BlankUtil.isBlank(quotaList)) {
			//根据批次号进行升序排序
			Collections.sort(quotaList, new Comparator<ClusterNodeQuotaDto>() {
				@Override
				public int compare(ClusterNodeQuotaDto nodeDto1, ClusterNodeQuotaDto nodeDto2) {
					int batchNo1 = Integer.parseInt(nodeDto1.getBatchNo());
					int batchNo2 = Integer.parseInt(nodeDto2.getBatchNo());
					return batchNo1 - batchNo2;
				}
			});
			
			for (String hostIp : hostSet) {
				DynamicHostQuotaDto hostQuotaDto = new DynamicHostQuotaDto();
				hostQuotaDto.setHostIp(hostIp);
				
				for (ClusterNodeQuotaDto quotaDto : quotaList) {
					String quotaHostIp = quotaDto.getHostIp();
					if (hostIp.equals(quotaHostIp)) {
						hostQuotaDto.getCpuList().add(quotaDto.getCpuRateSum());
						hostQuotaDto.getMemList().add(quotaDto.getMemoryRateSum());
						hostQuotaDto.getDiskList().add(quotaDto.getDiskTotalRate());
						hostQuotaDto.getBussList().add(quotaDto.getBussSum());
						hostQuotaDto.getBatchNoList().add(quotaDto.getBatchNo());
					}
				}
				dynamicQuotaList.add(hostQuotaDto);
			}
		}
	}
	
	/**
	 * 策略配置Dto
	 * @param nodeMap
	 * @return
	 */
	public DynamicThresholdStrategyDto getThresholdStrategyDto(HashMap<String, Object> nodeMap) {
		DynamicThresholdStrategyDto strategyDto = new DynamicThresholdStrategyDto();
		strategyDto.setStrategyId(StringTool.object2String(nodeMap.get("STRATEGY_ID")));
		strategyDto.setClusterId(StringTool.object2String(nodeMap.get("CLUSTER_ID")));
		strategyDto.setTaskProgramId(StringTool.object2String(nodeMap.get("TASK_PROGRAM_ID")));
		strategyDto.setQuotaType(StringTool.object2String(nodeMap.get("QUOTA_TYPE")));
		strategyDto.setOperatorType(StringTool.object2String(nodeMap.get("OPERATOR_TYPE")));
		strategyDto.setConditionParam(StringTool.object2String(nodeMap.get("CONDITION_PARAM")));
		strategyDto.setConditionValue(StringTool.object2String(nodeMap.get("CONDITION_VALUE")));
		logger.info("策略配置信息： " + strategyDto);
		return strategyDto;
	}
}
