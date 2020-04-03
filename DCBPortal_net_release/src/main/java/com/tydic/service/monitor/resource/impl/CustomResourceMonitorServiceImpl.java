package com.tydic.service.monitor.resource.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import backtype.storm.generated.ClusterSummary;
import backtype.storm.generated.MetricInfo;
import backtype.storm.generated.MetricSnapshot;
import backtype.storm.generated.SupervisorWorkers;
import backtype.storm.generated.WorkerSummary;
import backtype.storm.utils.NimbusClient;

import com.alibaba.jstorm.ui.model.UIWorkerMetric;
import com.alibaba.jstorm.ui.utils.NimbusClientManager;
import com.alibaba.jstorm.ui.utils.UIDef;
import com.alibaba.jstorm.ui.utils.UIMetricUtils;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.alibaba.jstorm.utils.JStormUtils;
import com.alibaba.jstorm.utils.NetWorkUtils;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.service.monitor.resource.CustomResourceMonitorService;
import com.tydic.web.monitor.CustomResourceMonitorController;

@Service("customResourceMonitorService")
public class CustomResourceMonitorServiceImpl implements
		CustomResourceMonitorService {
	private static Logger log = LoggerFactory.getLogger(CustomResourceMonitorController.class);
	
	/**
	 * 查询workerMetrics信息
	 */
	@Override
	public Map workerMetrics(Map<String, String> params)throws Exception {
		 Map resultMap = new HashMap(); ;
		 String clusterName=params.get("clusterName");
		 String host=params.get("host");
		 host = NetWorkUtils.host2Ip(host);
	     int window = UIUtils.parseWindow(null);
		 NimbusClient client = null;
	        try {
	            client = NimbusClientManager.getNimbusClient(clusterName);
	            SupervisorWorkers supervisorWorkers = client.getClient().getSupervisorWorkers(host);
	            //get worker metrics
	            Map<String, MetricInfo> workerMetricInfo = supervisorWorkers.get_workerMetric();
	          //get worker summary
	            List<WorkerSummary> workerSummaries = supervisorWorkers.get_workers();
	            
	            List<UIWorkerMetric> workerMetrics = getWorkerMetrics(workerMetricInfo, workerSummaries, host, window);
	            List<String> workerHead=UIMetricUtils.sortHead(workerMetrics, UIWorkerMetric.HEAD);
	            resultMap.put("workerHead", workerHead);
	            resultMap.put("workerMetrics", workerMetrics);
	            
	            //表数据转换
	            for(int i=0;i<workerMetrics.size();i++){
	            	UIWorkerMetric workerMetric=workerMetrics.get(i);
	            	Map<String, String>  metrics=workerMetric.getMetrics();
	            	String memoryUsed=metrics.get("MemoryUsed");
	            	long size = UIUtils.parseLong(memoryUsed, 0);
	            	memoryUsed = UIUtils.prettyFileSize(size);
	            	metrics.put("MemoryUsed", memoryUsed);
	            }
	            //表头转换
	            List<String>  workerHeadWrap = new ArrayList<String>();
	            for(int i=0;i<workerHead.size();i++){
	            	String head=workerHead.get(i);
	            	if (UIDef.HEAD_MAP.containsKey(head)) {
	            		workerHeadWrap.add(UIDef.HEAD_MAP.get(head));
	                } else {
	                	workerHeadWrap.add(head);
	                }
	            }
	            resultMap.put("workerHeadWrap", workerHeadWrap);
	            
	        } catch (Exception e) {
	            NimbusClientManager.removeClient(clusterName);
	            log.error(e.getMessage(), e);
	        }
	        return resultMap;
	}
	
	
	public static  List<UIWorkerMetric> getWorkerMetrics(Map<String, MetricInfo> workerMetricInfo,
            List<WorkerSummary> workerSummaries, String host, int window) {
	Map<String, UIWorkerMetric> workerMetrics = new HashMap<>();
	try {
		for (MetricInfo info : workerMetricInfo.values()) {
			if (info != null) {
				for (Map.Entry<String, Map<Integer, MetricSnapshot>> metric : info
						.get_metrics().entrySet()) {
					String name = metric.getKey();
					String[] split_name = name.split("@");
					String _host = UIMetricUtils
							.extractComponentName(split_name);
					if (!host.equals(_host))
						continue;

					// only handle the specific host
					String port = UIMetricUtils.extractTaskId(split_name);
					String key = host + ":" + port;
					String metricName = UIMetricUtils
							.extractMetricName(split_name);
					MetricSnapshot snapshot = metric.getValue().get(window);

					UIWorkerMetric workerMetric;
					if (workerMetrics.containsKey(key)) {
						workerMetric = workerMetrics.get(key);
					} else {
						workerMetric = new UIWorkerMetric(host, port);
						workerMetrics.put(key, workerMetric);
					}
					workerMetric.setMetricValue(snapshot, metricName);
				}
			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	for (WorkerSummary ws : workerSummaries) {
		String worker = host + ":" + ws.get_port();
		if (workerMetrics.containsKey(worker)) {
			workerMetrics.get(worker).setTopology(ws.get_topology());
		}
	}

	return new ArrayList<>(workerMetrics.values());
}


	/**
	 * 查询nimbus配置信息
	 */
	@Override
	public Map nimbusConf(Map<String, String> params) throws Exception{
		 Map resultMap = new HashMap(); 
		 String clusterName=params.get("clusterName");
		 Map conf=UIUtils.getNimbusConf(clusterName);
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
	 * 查询Supervisor信息
	 */
	@Override
	public Map getCustomSupervisorInfo(Map<String, String> params) throws Exception {
		 Map resultMap = new HashMap();
		 String clusterName=params.get("clusterName");
		 NimbusClient client = null;
		 
			client = NimbusClientManager.getNimbusClient(clusterName);
	         ClusterSummary clusterSummary = client.getClient().getClusterInfo();
	         List<Map<String, Object>> supervisors=JStormUtils.thriftToMap(clusterSummary.get_supervisors());
	         for(int i=0;i<supervisors.size();i++){
	        	 Map<String, Object> supervisor = supervisors.get(i);
	        	 String uptimeSecs=(String)supervisor.get("uptimeSecs");
	        	 int uptime = JStormUtils.parseInt(uptimeSecs, 0);
                 String output = UIUtils.prettyUptime(uptime);
	        	 supervisor.put("uptimeSecs", output);
	         };
	         resultMap.put(FrameParamsDefKey.PAGE_INDEX, 0);
	         resultMap.put(FrameParamsDefKey.PAGE_SIZE, supervisors.size());
	         resultMap.put(FrameParamsDefKey.TOTAL, supervisors.size());
	         resultMap.put(FrameParamsDefKey.DATA,  supervisors);
		
		return resultMap;
	}


	/**
	 * 查询图表信息
	 */
	@Override
	public List<Map<String,Object>>  getChartsData(Map<String, String> params) {
		 List<Map<String,Object>> host_metrics=null;
		 String clusterName=params.get("clusterName");
		 String host=params.get("host");
		 host = NetWorkUtils.host2Ip(host);
		/* Map<String,Map<String,List<Map<String,Object>>>> clusters_metrics=WorkerMetricJob.clusters_metrics;
		 Map<String,List<Map<String,Object>>> cluster_metrics= clusters_metrics.get(clusterName);
		 if(cluster_metrics != null ){
			 host_metrics=cluster_metrics.get(host);
		 }
		return host_metrics;*/
		 return null;
	}


	
	

}
