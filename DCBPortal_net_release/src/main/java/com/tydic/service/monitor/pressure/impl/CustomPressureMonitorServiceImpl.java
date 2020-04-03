package com.tydic.service.monitor.pressure.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import PluSoft.Utils.JSON;
import backtype.storm.generated.ClusterSummary;
import backtype.storm.generated.ComponentSummary;
import backtype.storm.generated.MetricInfo;
import backtype.storm.generated.MetricSnapshot;
import backtype.storm.generated.TaskSummary;
import backtype.storm.generated.TopologyInfo;
import backtype.storm.utils.NimbusClient;

import com.alibaba.jstorm.ui.model.TaskEntity;
import com.alibaba.jstorm.ui.model.UIComponentMetric;
import com.alibaba.jstorm.ui.model.UISummaryMetric;
import com.alibaba.jstorm.ui.model.UIUserDefinedMetric;
import com.alibaba.jstorm.ui.model.UIWorkerMetric;
import com.alibaba.jstorm.ui.utils.NimbusClientManager;
import com.alibaba.jstorm.ui.utils.UIMetricUtils;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.alibaba.jstorm.utils.JStormUtils;
import com.google.common.collect.Lists;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.service.monitor.pressure.CustomPressureMonitorService;
import com.tydic.web.monitor.CustomResourceMonitorController;

@Service("customPressureMonitorService")
public class CustomPressureMonitorServiceImpl implements CustomPressureMonitorService{
	private static Logger log = LoggerFactory.getLogger(CustomResourceMonitorController.class);

	/**
	 * 获取拓扑摘要信息
	 */
	@Override
	public Map getTopologySummary(Map<String, String> params)throws Exception {
		Map resultMap = new HashMap();
		 String clusterName=params.get("clusterName");
		 NimbusClient client = null;
		 
			client = NimbusClientManager.getNimbusClient(clusterName);
	         ClusterSummary clusterSummary = client.getClient().getClusterInfo();
	         List<Map<String, Object>> topologies=JStormUtils.thriftToMap(clusterSummary.get_topologies());
	         for(int i=0;i<topologies.size();i++){
	        	 Map<String, Object> topologie = topologies.get(i);
	        	 String uptimeSecs=(String)topologie.get("uptimeSecs");
	        	 int uptime = JStormUtils.parseInt(uptimeSecs, 0);
                String output = UIUtils.prettyUptime(uptime);
                topologie.put("uptimeSecs", output);
	         };
	         resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
	         resultMap.put(FrameParamsDefKey.PAGE_SIZE, topologies.size());
	         resultMap.put(FrameParamsDefKey.TOTAL, topologies.size());
	         resultMap.put(FrameParamsDefKey.DATA,  topologies);
		
		return resultMap;
	}

	/**
	 * 查询拓扑配置信息
	 */
	@Override
	public Map topologyConf(Map<String, String> params) throws Exception {
		 Map resultMap = new HashMap(); 
		 String clusterName=params.get("clusterName");
		 String topologyId=params.get("topologyId");
		 Map conf=UIUtils.getTopologyConf(clusterName, topologyId);
		 List list= new ArrayList();
		 Set confKey= conf.keySet();
		 Iterator iterator=confKey.iterator();
		 while(iterator.hasNext()){
			 Map item= new HashMap();
			 String key=(String)iterator.next();
			 item.put("key", key);
			 item.put("value", conf.get(key));
			 list.add(item);
		 }
		 resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
         resultMap.put(FrameParamsDefKey.PAGE_SIZE, list.size());
         resultMap.put(FrameParamsDefKey.TOTAL, list.size());
         resultMap.put(FrameParamsDefKey.DATA,  list);
         
         return resultMap;
		
	}

	/**
	 * 查询topologyState
	 */
	@Override
	public Map topologyState(Map<String, String> params) throws Exception {
				Map resultMap = new HashMap();
		 		String clusterName=params.get("clusterName");
		 		String topologyId=params.get("topologyId");
	 			NimbusClient client = NimbusClientManager.getNimbusClient(clusterName);
	            TopologyInfo topologyInfo = client.getClient().getTopologyInfo(topologyId);

	            MetricInfo topologyMetrics = topologyInfo.get_metrics().get_topologyMetric();
                System.out.println(JSON.Encode(topologyMetrics));
	           
	            int window = UIUtils.parseWindow(null);
	            UISummaryMetric topologyData = getTopologyData(topologyMetrics, window);
	            Map<String, String> metrics=topologyData.getMetrics();
	            //表数据转换
            	String memoryUsed=metrics.get("MemoryUsed");
            	long size = UIUtils.parseLong(memoryUsed, 0);
            	memoryUsed = UIUtils.prettyFileSize(size);
            	metrics.put("MemoryUsed", memoryUsed);
	            List list= new ArrayList();
	            list.add(metrics);
	            resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
	            resultMap.put(FrameParamsDefKey.PAGE_SIZE, list.size());
	            resultMap.put(FrameParamsDefKey.TOTAL, list.size());
	            resultMap.put(FrameParamsDefKey.DATA,  list);
	            return resultMap;
	}
		 
	private UISummaryMetric getTopologyData(MetricInfo info, int window) {
	    UISummaryMetric topoMetric = new UISummaryMetric();
	    if (info != null) {
	        for (Map.Entry<String, Map<Integer, MetricSnapshot>> metric : info.get_metrics().entrySet()) {
	            String name = metric.getKey();
	            String[] split_name = name.split("@");
	            String metricName = UIMetricUtils.extractMetricName(split_name);
	
	            if (!metric.getValue().containsKey(window)) {
	                log.info("topology snapshot {} missing window:{}", metric.getKey(), window);
	                continue;
	            }
	            MetricSnapshot snapshot = metric.getValue().get(window);
	
	            topoMetric.setMetricValue(snapshot, metricName);
	        }
	    }
	    return topoMetric;
	}

	/**
	 * 查询componentMetrics信息
	 */
	@Override
	public Map componentMetrics(Map<String, String> params) throws Exception {
		Map resultMap = new HashMap();
 		String clusterName=params.get("clusterName");
 		String topologyId=params.get("topologyId");
 		String win=params.get("win");
 		if(win == null || win.equals("")){
 			win=null;
 		}
		NimbusClient client = NimbusClientManager.getNimbusClient(clusterName);
        TopologyInfo topologyInfo = client.getClient().getTopologyInfo(topologyId);
       
        int window = UIUtils.parseWindow(win);
        MetricInfo componentMetrics = topologyInfo.get_metrics().get_componentMetric();
        List<UIUserDefinedMetric> userDefinedMetrics = Lists.newArrayList();
        List<UIComponentMetric> componentData = getComponentData(componentMetrics, window,
                topologyInfo.get_components(), userDefinedMetrics);
        List<String> componentHead = UIMetricUtils.sortHead(componentData, UIComponentMetric.HEAD);
//        System.out.println("componentData:"+JSON.Encode(componentData));
        
        List list= new ArrayList();
        for(int i=0;i<componentData.size();i++){
        	UIComponentMetric componentMetric=componentData.get(i);
        	Map tmp =componentMetric.getMetrics();
        	tmp.put("parallel", componentMetric.getParallel());
        	tmp.put("componentName", componentMetric.getComponentName());
        	tmp.put("errors", componentMetric.getErrors());
        	list.add(tmp);
        	
        }
        resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
        resultMap.put(FrameParamsDefKey.PAGE_SIZE, list.size());
        resultMap.put(FrameParamsDefKey.TOTAL, list.size());
        resultMap.put(FrameParamsDefKey.DATA,  list);
        return resultMap;
}

	private List<UIComponentMetric> getComponentData(MetricInfo info,
			int window, List<ComponentSummary> componentSummaries,
			List<UIUserDefinedMetric> userDefinedMetrics) {
		Map<String, UIComponentMetric> componentData = new HashMap<>();
		if (info != null) {
			for (Map.Entry<String, Map<Integer, MetricSnapshot>> metric : info
					.get_metrics().entrySet()) {
				String name = metric.getKey();
				String[] split_name = name.split("@");
				String compName = UIMetricUtils
						.extractComponentName(split_name);
				String metricName = UIMetricUtils.extractMetricName(split_name);
				String group = UIMetricUtils.extractGroup(split_name);
				String parentComp = null;
				if (metricName != null && metricName.contains(".")) {
					parentComp = metricName.split("\\.")[0];
					metricName = metricName.split("\\.")[1];
				}

				if (!metric.getValue().containsKey(window)) {
					log.info("component snapshot {} missing window:{}",
							metric.getKey(), window);
					continue;
				}
				MetricSnapshot snapshot = metric.getValue().get(window);

				if (group != null && group.equals("udf")) {
					UIUserDefinedMetric udm = new UIUserDefinedMetric(
							metricName, compName);
					udm.setValue(UIMetricUtils.getMetricValue(snapshot));
					udm.setType(snapshot.get_metricType());
					userDefinedMetrics.add(udm);
				} else {
					UIComponentMetric compMetric;
					if (componentData.containsKey(compName)) {
						compMetric = componentData.get(compName);
					} else {
						compMetric = new UIComponentMetric(compName);
						componentData.put(compName, compMetric);
					}
					compMetric.setMetricValue(snapshot, parentComp, metricName);
				}
			}
		}
		// merge sub metrics
		for (UIComponentMetric comp : componentData.values()) {
			comp.mergeValue();
		}
		// combine the summary info into metrics
		TreeMap<String, UIComponentMetric> ret = new TreeMap<>();
		for (ComponentSummary summary : componentSummaries) {
			String compName = summary.get_name();
			UIComponentMetric compMetric;
			if (componentData.containsKey(compName)) {
				compMetric = componentData.get(compName);
				compMetric.setParallel(summary.get_parallel());
				compMetric.setType(summary.get_type());
				compMetric.setErrors(summary.get_errors());
			} else {
				compMetric = new UIComponentMetric(compName,
						summary.get_parallel(), summary.get_type());
				compMetric.setErrors(summary.get_errors());
				componentData.put(compName, compMetric);
			}
			String key = compMetric.getType() + compName;
			if (compName.startsWith("__")) {
				key = "a" + key;
			}
			compMetric.setSortedKey(key);
			ret.put(key, compMetric);
		}
		return new ArrayList<>(ret.descendingMap().values());
	}

	/**
	 * 查询workerMetrics信息
	 */
	@Override
	public Map workerMetrics(Map<String, String> params) throws Exception {

		Map resultMap = new HashMap();
 		String clusterName=params.get("clusterName");
 		String topologyId=params.get("topologyId");
 		String win=params.get("win");
 		if(win == null || win.equals("")){
 			win=null;
 		}
		NimbusClient client = NimbusClientManager.getNimbusClient(clusterName);
        TopologyInfo topologyInfo = client.getClient().getTopologyInfo(topologyId);
       
        int window = UIUtils.parseWindow(win);
        MetricInfo workerMetrics = topologyInfo.get_metrics().get_workerMetric();
       List<UIWorkerMetric> workerData = getWorkerData(workerMetrics, topologyId, window);
//      model.addAttribute("workerData", workerData);
        List<String> workerHead = UIMetricUtils.sortHead(workerData, UIWorkerMetric.HEAD);
//
//        System.out.println("workerData:"+JSON.Encode(workerData));
//        
        List list= new ArrayList();
        for(int i=0;i<workerData.size();i++){
        	UIWorkerMetric worker=workerData.get(i);
        	Map<String, String> tmp =worker.getMetrics();
        	  //表数据转换
        	String memoryUsed=tmp.get("MemoryUsed");
        	long size = UIUtils.parseLong(memoryUsed, 0);
        	memoryUsed = UIUtils.prettyFileSize(size);
        	tmp.put("MemoryUsed", memoryUsed);
        	tmp.put("host",worker.getHost() );
        	tmp.put("port", worker.getPort());
        	list.add(tmp);
        	
        }
        resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
        resultMap.put(FrameParamsDefKey.PAGE_SIZE, list.size());
        resultMap.put(FrameParamsDefKey.TOTAL, list.size());
        resultMap.put(FrameParamsDefKey.DATA,  list);
        return resultMap;

	}
 
	 private List<UIWorkerMetric> getWorkerData(MetricInfo info, String topology, int window) {
	        Map<String, UIWorkerMetric> workerData = new HashMap<>();
	        if (info != null) {
	            for (Map.Entry<String, Map<Integer, MetricSnapshot>> metric : info.get_metrics().entrySet()) {
	                String name = metric.getKey();
	                String[] split_name = name.split("@");
	                String host = UIMetricUtils.extractComponentName(split_name);
	                String port = UIMetricUtils.extractTaskId(split_name);
	                String key = host + ":" + port;
	                String metricName = UIMetricUtils.extractMetricName(split_name);


	                if (!metric.getValue().containsKey(window)) {
	                    log.info("worker snapshot {} missing window:{}", metric.getKey(), window);
	                    continue;
	                }
	                MetricSnapshot snapshot = metric.getValue().get(window);

	                UIWorkerMetric workerMetric;
	                if (workerData.containsKey(key)) {
	                    workerMetric = workerData.get(key);
	                } else {
	                    workerMetric = new UIWorkerMetric(host, port, topology);
	                    workerData.put(key, workerMetric);
	                }
	                workerMetric.setMetricValue(snapshot, metricName);
	            }
	        }
	        return new ArrayList<>(workerData.values());
	    }

	 /**
	  * 查询taskStats信息
	  */
	@Override
	public Map taskStats(Map<String, String> params) throws Exception {


		Map resultMap = new HashMap();
 		String clusterName=params.get("clusterName");
 		String topologyId=params.get("topologyId");
 		String win=params.get("win");
 		if(win == null || win.equals("")){
 			win=null;
 		}
		NimbusClient client = NimbusClientManager.getNimbusClient(clusterName);
        TopologyInfo topologyInfo = client.getClient().getTopologyInfo(topologyId);
       
        int window = UIUtils.parseWindow(win);
        MetricInfo workerMetrics = topologyInfo.get_metrics().get_workerMetric();
        List<TaskEntity> taskData = getTaskEntities(topologyInfo);
//
        System.out.println("taskData:"+JSON.Encode(taskData));
//        
        List list= new ArrayList();
        for(int i=0;i<taskData.size();i++){
        	TaskEntity task=taskData.get(i);
        	Map tmp = new HashMap();
        	tmp.put("component", task.getComponent());
        	tmp.put("errors", task.getErrors());
        	tmp.put("host", task.getHost());
        	tmp.put("status", task.getStatus());
        	tmp.put("type", task.getType());
        	tmp.put("port", task.getPort());
        	tmp.put("task_id", task.getId());
        	tmp.put("uptime", task.getUptime());
        	list.add(tmp);
        	
        }
        resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
        resultMap.put(FrameParamsDefKey.PAGE_SIZE, list.size());
        resultMap.put(FrameParamsDefKey.TOTAL, list.size());
        resultMap.put(FrameParamsDefKey.DATA,  list);
        return resultMap;

	
	}
	
	private List<TaskEntity> getTaskEntities(TopologyInfo topologyInfo) {
        Map<Integer, TaskEntity> tasks = new HashMap<>();
        for (TaskSummary ts : topologyInfo.get_tasks()) {
            tasks.put(ts.get_taskId(), new TaskEntity(ts));
        }
        for (ComponentSummary cs : topologyInfo.get_components()) {
            String compName = cs.get_name();
            String type = cs.get_type();
            for (int id : cs.get_taskIds()) {
                if (tasks.containsKey(id)) {
                    tasks.get(id).setComponent(compName);
                    tasks.get(id).setType(type);
                } else {
                    log.info("missing task id:{}", id);
                }
            }
        }
        return new ArrayList<>(tasks.values());
    }

}
