package com.tydic.service.monitormanager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import backtype.storm.generated.ComponentSummary;
import backtype.storm.generated.MetricInfo;
import backtype.storm.generated.Nimbus.Client;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.generated.TopologySummary;
import backtype.storm.generated.WorkerSummary;
import backtype.storm.utils.NimbusClient;

import com.alibaba.jstorm.metric.MetaType;
import com.alibaba.jstorm.ui.model.SupervisorEntity;
import com.alibaba.jstorm.ui.model.TaskEntity;
import com.alibaba.jstorm.ui.model.UIComponentMetric;
import com.alibaba.jstorm.ui.model.UINettyMetric;
import com.alibaba.jstorm.ui.model.UITaskMetric;
import com.alibaba.jstorm.ui.model.UIWorkerMetric;
import com.alibaba.jstorm.ui.model.WorkerEntity;
import com.alibaba.jstorm.ui.model.graph.ChartSeries;
import com.alibaba.jstorm.ui.utils.UIMetricUtils;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.alibaba.jstorm.utils.JStormUtils;
import com.tydic.service.monitormanager.TopologyMonitorService;
import com.tydic.util.TopologyUtils;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.monitormanager.impl]    
  * @ClassName:    [TopologyMonitorServiceImpl]     
  * @Description:  [Topology监控管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-14 上午9:00:25]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-14 上午9:00:25]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service("TopologyMonitorService")
@SuppressWarnings("all")
public class TopologyMonitorServiceImpl implements TopologyMonitorService{
	private static Logger log = LoggerFactory.getLogger(TopologyMonitorServiceImpl.class);
	
	/**
	 * 查询Topology信息
	 * @param params 业务参数
	 * @return Map 
	 */
	public Map<String,Object> queryTopologySummary(Map<String, String> params) throws Exception {
		log.debug("查询Topology信息， 业务参数: " + params.toString());
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		NimbusClient nimbusClient = null;
		Client client = null;
		
		String clusterName = params.get("clusterName");
		String topologyId = params.get("topologyId");
		String win = params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
		    TopologySummary topologySummary = TopologyUtils.getTopologySummary(client,topologyId);
		    // 需要由JstormUtils经过数据处理,才能作为返回Map
		    Map topologySummaryMap = JStormUtils.thriftToMap(topologySummary);
		    int uptime = Integer.parseInt((String) topologySummaryMap.get("uptimeSecs"));
		    topologySummaryMap.put("uptime", UIUtils.prettyUptime(uptime));
		    resultMap.put("topologySummaryMap", topologySummaryMap);
		    
		    List<UIComponentMetric> uIComponenetMetricList = UIMetricUtils.getComponentMetrics(TopologyUtils.getMetricInfo("component",client,topologyId),window, TopologyUtils.getComponentSummaryList(client,topologyId), TopologyUtils.userDefinedMetrics);
		    resultMap.put("uIComponenetMetricList", TopologyUtils.getComponentData(uIComponenetMetricList));
		    
		    // 通过workerMetrics实例与topologyId由UIMetricUtils工具类得到相应UIWorkerMetrics列表
		    List<UIWorkerMetric> uIWorkerMetricList = UIMetricUtils.getWorkerMetrics(TopologyUtils.getMetricInfo("worker",client,topologyId),topologyId,window);
		    resultMap.put("uIWorkerMetricList", TopologyUtils.getWorkerData(uIWorkerMetricList));
		    
		    // 通过TopologyInfo实例由UIUtils工具类得到taskEntity实例列表
		    List<TaskEntity> taskEntityList = UIUtils.getTaskEntities(TopologyUtils.getTopologyInfo(client,topologyId));
		    resultMap.put("taskEntityList", taskEntityList);
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}
		
	    return resultMap;
	}
	
	/**
	 * 查询workerMetrics信息
	 */
	@Override
	public Map queryWorkerMetrics(Map<String, String> params) throws Exception {
		Map resultMap = new HashMap();
		NimbusClient nimbusClient = null;
		Client client = null;

		String clusterName = params.get("clusterName");
		String topologyId = params.get("topologyId");
		String win = params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			// 通过集群名称创建NimbusClient实例
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
			// 通过NimbusClient实例创建Nimbus实例,再通过topologyId得到TopologyInfo实例
			TopologyInfo topologyInfo = TopologyUtils.getTopologyInfo(client,topologyId);
			// 通过TopologyInfo实例得到TopologyMetric实例
			// 通过TopologyMetric实例得到MetricInfo实例,worker统计信息
			MetricInfo workerMetrics = TopologyUtils.getMetricInfo("worker",client,topologyId);
			// 通过workerMetrics实例与topologyId由UIMetricUtils工具类得到相应UIWorkerMetrics列表
			List<UIWorkerMetric> uIWorkerMetricList = UIMetricUtils.getWorkerMetrics(workerMetrics,topologyId,window);
			resultMap.put("uIWorkerMetricList", TopologyUtils.getWorkerData(uIWorkerMetricList));
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}

		return resultMap;
	}
	
	/**
	 * 查询nettyMetrics信息
	 */
	@Override
	public Map queryNettyMetrics(Map<String, String> params) throws Exception {
		Map resultMap = new HashMap();
		NimbusClient nimbusClient = null;
		Client client = null;
		
		String clusterName=params.get("clusterName");
		String topologyId=params.get("topologyId");
		String host = params.get("host");
		String port = params.get("port");
		String win=params.get("win");
		
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
			// 通过NimbusClient实例创建Nimbus实例,再通过topologyId得到TopologyInfo实例
			MetricInfo nettyMetirc = client.getPagingNettyMetrics(topologyId, host, 1);
			List<UINettyMetric> uINettyMetricList = TopologyUtils.getNettyData(nettyMetirc, host, window);
			resultMap.put("uINettyMetricList", TopologyUtils.getNettyData(uINettyMetricList,host,port));
			
			// 获取图表信息
			List<MetricInfo> metricList = client.getMetrics(topologyId, MetaType.NETTY.getT());
			List<ChartSeries> chartSeriesList = UIUtils.getChartSeries(metricList, window);
			resultMap.put("chartSeriesList",TopologyUtils.getChartSeriesData(chartSeriesList,host,port));
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}
		
		return resultMap;
		
	}
	
	/**
	  * 查询单个component及其下挂task的信息
	  */
	@Override
	public Map queryComponentMetric(Map<String, String> params) throws Exception {
		Map resultMap = new HashMap();
		NimbusClient nimbusClient = null;
		Client client = null;

		String clusterName=params.get("clusterName");
		String topologyId=params.get("topologyId");
		String componentName = params.get("componentName");
		
		String win=params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
		    List<ComponentSummary> componentSummaryList = TopologyUtils.getComponentSummaryList(client,topologyId);
		    // 通过componentMetric实例与ComponentName由UIMetricUtils工具类得到UIComponentMetric实例
		    UIComponentMetric uIComponentMetric = UIMetricUtils.getComponentMetric(TopologyUtils.getMetricInfo("component",client,topologyId), window, componentName, componentSummaryList);
		    List<UIComponentMetric> componentMetricTempList = new ArrayList<>();
		    componentMetricTempList.add(uIComponentMetric);
		    resultMap.put("uIComponentMetric", TopologyUtils.getComponentData(componentMetricTempList));
		    
		    // 通过TopologyInfo实例与ComponentName由UIUtils工具类得到taskEntity列表
		    List<TaskEntity> taskEntityList = UIUtils.getTaskEntities(TopologyUtils.getTopologyInfo(client,topologyId), componentName);
		    resultMap.put("taskEntityList",taskEntityList);
		    
		    // 通过NimbusClient实例创建Nimbus实例,传入参数TopologyId与ComponentName得到制定Component下的taskMetric
		    MetricInfo taskMetric = client.getTaskMetrics(topologyId, componentName);
		    // 通过taskMetric实例与ComponentName由UIMetricUtils工具类得到UITaskMetric实例
		    List<UITaskMetric> taskMetricList = UIMetricUtils.getTaskMetrics(taskMetric, componentName, window);
		    resultMap.put("taskMetricList", TopologyUtils.getTaskData(taskMetricList));
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}	   
	    
      return resultMap;
	}
	
	/**
	 * 查询所有component的信息
	 */
	@Override
	public Map queryComponentMetricList(Map<String, String> params) throws Exception {
		Map resultMap = new HashMap();
		NimbusClient nimbusClient = null;
		Client client = null;

		String clusterName=params.get("clusterName");
		String topologyId=params.get("topologyId");
		String componentName = params.get("componentName");
		
		String win=params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
			// 通过TopologyInfo实例得到ComponentSummary列表
			List<ComponentSummary> componentSummaryList = TopologyUtils.getComponentSummaryList(client,topologyId);
			// 通过TopologyMetric实例得到MetricInfo实例,Component统计信息
			MetricInfo componentMetric = TopologyUtils.getMetricInfo("component",client,topologyId);
			// 通过componentMetric实例与ComponentName由UIMetricUtils工具类得到UIComponentMetric实例
			List<UIComponentMetric> uIComponentMetricList = UIMetricUtils.getComponentMetrics(componentMetric, window, componentSummaryList, TopologyUtils.userDefinedMetrics);
			resultMap.put("uIComponentMetric", TopologyUtils.getComponentData(uIComponentMetricList));
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}	   
		
		return resultMap;
	}

	@Override
	public Map<String, Object> querySupervisorWorkers(Map<String, String> params)
			throws Exception {
		Map resultMap = new HashMap();
		NimbusClient nimbusClient = null;
		Client client = null;

		String clusterName=params.get("clusterName");
		String host = params.get("host");
		
		String win=params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);
		
		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
			// 通过SupervisorSummary实例直接构造SupervisorEntity实例
			SupervisorEntity supervisorEntity = new SupervisorEntity(TopologyUtils.getSupervisorSummary(client,host));
			// 偷懒,前台有个字段需要作处理,直接在后台处理并赋给一个无用属性
			supervisorEntity.setIp(supervisorEntity.getSlotsUsed() + "/" + supervisorEntity.getSlotsTotal());
			resultMap.put("supervisorEntity", supervisorEntity);
			
			// 通过SupervisorWorkers实例得到WorkerSummary实例列表
			List<WorkerSummary> workerSummaryList = TopologyUtils.getWorkerSummaryList(client,host);
			// 通过WorkerSummary实例列表由UIUtils工具类创建WorkerEntity实例列表
			List<WorkerEntity> workerEntityList = UIUtils.getWorkerEntities(workerSummaryList);
			resultMap.put("workerEntityList", workerEntityList);
			
			// 通过SupervisorWorkers实例得到对应的workerMetricMap
			Map<String,MetricInfo> workerMetricMap = TopologyUtils.getSupervisorWorkers(client,host).get_workerMetric();
			// 通过workerMetricMap由uiMetricUtils得到UIWorkerMetric列表
			List<UIWorkerMetric> uIWorkerMetricList = UIMetricUtils.getWorkerMetrics(workerMetricMap, workerSummaryList, host, window);
			resultMap.put("uIWorkerMetricList", TopologyUtils.getWorkerData(uIWorkerMetricList));
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> querySupervisorWorkerMetrics(Map<String, String> params)
			throws Exception {
		Map resultMap = new HashMap();
		List tempList = new ArrayList<>();
		NimbusClient nimbusClient = null;
		Client client = null;

		String clusterName=params.get("clusterName");
		String topologyId=params.get("topologyId");
		String host = params.get("host");
		String port = params.get("port");
		
		String win=params.get("win");
		if(win == null || win.equals("")){
			win=null;
		}
		int window = UIUtils.parseWindow(win);

		try{
			nimbusClient = NimbusClient.getConfiguredClient(UIUtils.resetZKConfig(UIUtils.readUiConfig(), clusterName));
			client = nimbusClient.getClient();
			// 通过SupervisorWorkers实例得到WorkerSummary实例列表
			List<WorkerSummary> workerSummaryList = TopologyUtils.getWorkerSummaryList(client,host);
			// 通过WorkerSummary实例列表由UIUtils工具类创建WorkerEntity实例列表
			// 通过SupervisorWorkers实例得到对应的workerMetricMap
			Map<String,MetricInfo> workerMetricMap = TopologyUtils.getSupervisorWorkers(client,host).get_workerMetric();
			// 通过workerMetricMap由uiMetricUtils得到UIWorkerMetric列表
			List<UIWorkerMetric> uIWorkerMetricList = UIMetricUtils.getWorkerMetrics(workerMetricMap, workerSummaryList, host, window);
			for(Map workerMetric : TopologyUtils.getWorkerData(uIWorkerMetricList)){
				if(workerMetric.get("port").equals(port)){
					resultMap.put("workerMetric", workerMetric);
//					WorkerUtils.addWorkerMetric(workerMetric);
				}
			}
			// 获取定时器中的对应集群名中对应ip的数据列表
			/*List<Map<String,Object>> hostDataList = AnotherWorkerMetricJob.clusters_metrics.get(clusterName).get(host);
			List<Map<String,String>> metricDataList = new ArrayList<>();
			for(Map<String,Object> tempMap : hostDataList){
				List<UIWorkerMetric> anotherTempList = (List) tempMap.get("workerMetrics"); 
				String dataTime = ((String) tempMap.get("dataTime")).split(" ")[1];
				for(UIWorkerMetric uIWorkerMetric: anotherTempList){
					if(uIWorkerMetric.getPort().equals(port)){
						Map<String,String> metricDataMap = uIWorkerMetric.getMetrics();
						metricDataMap.put("dataTime", dataTime);
						metricDataList.add(metricDataMap);
					}
				}
			}*/
			
			//resultMap.put("clusters_metrics", AnotherWorkerMetricJob.clusters_metrics);
			//resultMap.put("metricDataList", metricDataList);
//			WorkerUtils.addDate();
//			
//			if(WorkerUtils.isFull()){
//				WorkerUtils.removeWorkerMetric();
//			}
//			resultMap.put("workerMetricList", WorkerUtils.workerMetricList);
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}finally{
			nimbusClient.close();
		}	   
		
		return resultMap;
	}
	
}
