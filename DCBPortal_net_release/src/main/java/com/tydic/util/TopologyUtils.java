package com.tydic.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.generated.ComponentSummary;
import backtype.storm.generated.MetricInfo;
import backtype.storm.generated.MetricSnapshot;
import backtype.storm.generated.Nimbus.Client;
import backtype.storm.generated.SupervisorSummary;
import backtype.storm.generated.SupervisorWorkers;
import backtype.storm.generated.TaskSummary;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.generated.TopologyMetric;
import backtype.storm.generated.TopologySummary;
import backtype.storm.generated.WorkerSummary;

import com.alibaba.jstorm.ui.model.UIComponentMetric;
import com.alibaba.jstorm.ui.model.UINettyMetric;
import com.alibaba.jstorm.ui.model.UITaskMetric;
import com.alibaba.jstorm.ui.model.UIUserDefinedMetric;
import com.alibaba.jstorm.ui.model.UIWorkerMetric;
import com.alibaba.jstorm.ui.model.graph.ChartSeries;
import com.alibaba.jstorm.ui.utils.UIMetricUtils;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.google.common.collect.Lists;

/**
 * 拓扑监控工具类
 * @author tu
 *
 */
@SuppressWarnings("all")
public class TopologyUtils {
	private static final Logger LOG = LoggerFactory.getLogger(TopologyUtils.class);
	
	public static List<UIUserDefinedMetric> userDefinedMetrics = Lists.newArrayList();
	
	/**
	 * 通过拓扑Id获取TopologyInfo实例并赋值给工具类属性topologyInfo
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static TopologyInfo getTopologyInfo(Client client,String topologyId) throws Exception{
		return client.getTopologyInfo(topologyId);
	}
	
	/**
	 * 获取topologyInfo下的TopologyMetric实例并赋值给工具类属性topologyMetric
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static TopologyMetric getTopologyMetric(Client client,String topologyId) throws Exception{
		return getTopologyInfo(client,topologyId).get_metrics();
	}
	
	/**
	 * 获取topologyInfo下的topologySummary实例并赋值给工具类属性topologySummary
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static TopologySummary getTopologySummary(Client client,String topologyId) throws Exception{
		return getTopologyInfo(client,topologyId).get_topology();
	}
	
	/**
	 * 通过Client获取SupervisorWorkers实例并赋值给工具类属性supervisorWorkers
	 * @param clusterName
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static SupervisorWorkers getSupervisorWorkers(Client client,String host) throws Exception{
		return client.getSupervisorWorkers(host);
	}
	
	/**
	 * 获取supervisorWorkers下的SupervisorSummary实例并赋值给工具类属性supervisorSummary
	 * @param clusterName
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static SupervisorSummary getSupervisorSummary(Client client,String host) throws Exception{
		return getSupervisorWorkers(client,host).get_supervisor();
	}
	
	/**
	 * 获取topologyInfo下的ComponentSummary列表并赋值给工具类属性componentSummaryList
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static List<ComponentSummary> getComponentSummaryList(Client client,String topologyId) throws Exception{
		return getTopologyInfo(client,topologyId).get_components();
	}
	
	/**
	 * 获取supervisorWorkers下的WorkerSummary列表并赋值给工具类属性workerSummaryList
	 * @param clusterName
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static List<WorkerSummary> getWorkerSummaryList(Client client,String host) throws Exception{
		return getSupervisorWorkers(client,host).get_workers();
	}
	
	/**
	 * 获取topologyInfo下的TaskSummary列表并赋值给工具类属性taskSummaryList
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static List<TaskSummary> getTaskSummaryList(Client client,String topologyId) throws Exception{
		return getTopologyInfo(client,topologyId).get_tasks();
	}
	
	/**
	 * 通过不同的标记获取topologyMetric下不同的MetricInfo实例并赋值给工具类属性metricInfo
	 * @param metricName
	 * @param clusterName
	 * @param topologyId
	 * @return
	 * @throws Exception
	 */
	public static MetricInfo getMetricInfo(String metricName,Client client,String topologyId) throws Exception{
		MetricInfo metricInfo = null ;
		if(metricName.equals("component")){
			metricInfo = getTopologyMetric(client,topologyId).get_componentMetric();
		}else if(metricName.equals("topology")){
			metricInfo = getTopologyMetric(client,topologyId).get_topologyMetric();
		}else if(metricName.equals("worker")){
			metricInfo = getTopologyMetric(client,topologyId).get_workerMetric();
		}else if(metricName.equals("task")){
			metricInfo = getTopologyMetric(client,topologyId).get_taskMetric();
		}else if(metricName.equals("stream")){
			metricInfo = getTopologyMetric(client,topologyId).get_streamMetric();
		}else if(metricName.equals("netty")){
			metricInfo = getTopologyMetric(client,topologyId).get_nettyMetric();
		}else {
			metricInfo = null;
		}
		
		return metricInfo;
	}
	
	// 将UIComponent统计信息列表中的数据处理,返回前台相应数据
	public static List<Map<String,Object>> getComponentData(List<UIComponentMetric> metricList) {
	    List<Map<String,Object>> resultList = new ArrayList<>();
	    if (metricList == null && metricList.size() < 1) {
	    	return resultList;
	    }
	    
		// 循环遍历每个metric
		for(UIComponentMetric metric : metricList){
			Map tempMap = metric.getMetrics();
			// 添加一些返回参数
			tempMap.put("componentName",metric.getComponentName());
			tempMap.put("type",metric.getType());
			tempMap.put("parallel",String.valueOf(metric.getParallel()));
			tempMap.put("errors",metric.getErrors());
			
			resultList.add(tempMap);
		}
	    
	    return resultList;
    }
	
	/**
	 *  将UIWorkerMetric列表中的数据处理,返回前台相应数据
	 * @param metricList
	 * @return
	 */
	public static List<Map<String,Object>> getWorkerData(List<UIWorkerMetric> metricList) {
		List<Map<String,Object>> resultList = new ArrayList<>();
		if (metricList == null && metricList.size() < 1) {
			return resultList;
		}
		
		// 循环遍历每个metric
		for(UIWorkerMetric metric : metricList){
			Map tempMap = metric.getMetrics();
			// 添加一些返回参数
			tempMap.put("host", metric.getHost());
			tempMap.put("port", metric.getPort());
			tempMap.put("topology", metric.getTopology());
			
			// 修改一些返回参数
			String memoryUsed = (String) tempMap.get("MemoryUsed");
			if(memoryUsed != null && !"".equals(memoryUsed)){
				String MemoryUsed = UIUtils.prettyFileSize(Long.parseLong(memoryUsed));
				tempMap.put("MemoryUsed", MemoryUsed);
			}
			
			resultList.add(tempMap);
		}
		
		return resultList;
	}
	
	/**
	 * 将UIWorkerMetric列表中的数据处理,返回前台相应数据
	 * @param metricList
	 * @return
	 */
	public static List<Map<String,Object>> getTaskData(List<UITaskMetric> metricList) {
		List<Map<String,Object>> resultList = new ArrayList<>();
		if (metricList == null && metricList.size() < 1) {
			return resultList;
		}
		
		// 循环遍历每个metric
		for(UITaskMetric metric : metricList){
			Map tempMap = metric.getMetrics();
			// 添加一些返回参数
			tempMap.put("host", metric.getTaskId());
			
			resultList.add(tempMap);
		}
		
		return resultList;
	}
	
	/**
	 * 模拟UIMetricUtils的方法
	 * @param nettyMetrics
	 * @param host
	 * @param window
	 * @return
	 */
	public static List<UINettyMetric> getNettyData(MetricInfo nettyMetrics, String host, int window) {
        HashMap<String, UINettyMetric> nettyData = new HashMap<>();
        if (nettyMetrics == null || nettyMetrics.get_metrics_size() == 0) {
            return new ArrayList<>(nettyData.values());
        }
        for (Map.Entry<String, Map<Integer, MetricSnapshot>> metric : nettyMetrics.get_metrics().entrySet()) {
            String name = metric.getKey();
            String[] split_name = name.split("@");


            String metricName = UIMetricUtils.extractMetricName(split_name);
            String connection = null;
            if (metricName != null) {
                connection = metricName.substring(metricName.indexOf(".") + 1);
                metricName = metricName.substring(0, metricName.indexOf("."));
            }
            MetricSnapshot snapshot = metric.getValue().get(window);

            UINettyMetric netty;
            if (nettyData.containsKey(connection)) {
                netty = nettyData.get(connection);
            } else {
                netty = new UINettyMetric(host, connection);
                nettyData.put(connection, netty);
            }
            netty.setMetricValue(snapshot, metricName);
        }
        return new ArrayList<>(nettyData.values());
    }
	
	/**
	 * 将UINettyMetric列表中的数据处理,返回前台相应数据
	 * @param metricList
	 * @param host
	 * @param port
	 * @return
	 */
	public static List<Map<String,Object>> getNettyData(List<UINettyMetric> metricList,String host,String port) {
		List<Map<String,Object>> resultList = new ArrayList<>();
		String fromStr = host + ":" + port;
		if (metricList == null && metricList.size() < 1) {
			return resultList;
		}
		
		// 循环遍历每个metric
		for(UINettyMetric metric : metricList){
			// 筛选对应信息
			if(metric.getFromWorker().equals(fromStr)){
				Map tempMap = metric.getMetrics();
				// 添加一些返回参数
				tempMap.put("from", metric.isFrom());
				tempMap.put("fromWorker", metric.getFromWorker());
				tempMap.put("to", metric.isTo());
				tempMap.put("toWorker", metric.getToWorker());
				tempMap.put("connection", metric.getConnection());
				
				resultList.add(tempMap);
			}
		}
		
		return resultList;
	}
	
	/**
	 * 将UINettyMetric列表中的数据处理,返回前台相应数据
	 * @param metricList
	 * @param host
	 * @param port
	 * @return
	 */
	public static List<Object> getChartSeriesData(List<ChartSeries> metricList,String host,String port) {
		List<Object> resultList = new ArrayList<>();
		String fromStr = "NettyCliSendSpeed." + host + ":" + port;
		if (metricList == null && metricList.size() < 1) {
			return resultList;
		}
		
		// 循环遍历每个metric
		for(ChartSeries chartSeries : metricList){
			if(chartSeries.getName().split("->")[0].equals(fromStr)){
				resultList.add(chartSeries);
			}
		}
		
		return resultList;
	}
}
