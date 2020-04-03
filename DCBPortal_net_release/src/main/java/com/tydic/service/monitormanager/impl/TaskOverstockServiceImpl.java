package com.tydic.service.monitormanager.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.jstorm.ui.model.ZookeeperNode;
import com.alibaba.jstorm.ui.utils.ZookeeperManager;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.monitormanager.TaskOverstockService;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Simple to Introduction
 * @ProjectName:  [DCBPortal_net_release]
 * @Package:      [com.tydic.service.monitormanager.impl]
 * @ClassName:    [TaskOverstockServiceImpl]
 * @Description:  [任务运行积压情况查询]
 * @Author:       [Yuanh]
 * @CreateDate:   [2017-12-25 下午4:45:16]
 * @UpdateUser:   [Yuanh]
 * @UpdateDate:   [2017-12-25 下午4:45:16]
 * @UpdateRemark: [说明本次修改内容]
 * @Version:      [v1.0]
 *
 */
@Service
public class TaskOverstockServiceImpl implements TaskOverstockService {

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(TaskOverstockServiceImpl.class);

	/**
	 * ZK服务组节点列表
	 */
	private static final String ZK_SERVICE_REGIST_PATH = "localservice/regist";

	/**
	 * ZK服务组节点列表
	 */
	private static final String ZK_SERVICE_INFO_PATH = "localservice/serviceInfo";

	@Autowired
	private CoreService coreService;

	/**
	 * 查询ZK集群列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> queryZookeeperList(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询ZK集群列表， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

		//查询zk集群
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("CLUSTER_TYPE", "'jstorm'");
		List<Map<String, Object>> jstormList = coreService.queryForList3New("serviceType.queryRelationClusterList", queryMap, dbKey);
		log.debug("jstorm集群列表: " + jstormList);

		//查询所有的zookeeper集群和jstorm集群列表
		Map<String, Object> relaMap = new HashMap<>();
		relaMap.put("CLUSTER_TYPE", "'zookeeper'");
		List<HashMap<String, Object>> clusterList = coreService.queryForList2New("serviceType.queryRelationClusterList", relaMap, dbKey);
		log.debug("zookeeper集群: " + clusterList);

		if (CollectionUtils.isNotEmpty(clusterList) && CollectionUtils.isNotEmpty(jstormList)) {
			for (int k=0; k<jstormList.size(); k++) {
				String jsBusClusterId = ObjectUtils.toString(jstormList.get(k).get("BUS_CLUSTER_ID"));
				List<Map<String, Object>> zookeeperList = clusterList.stream()
						.filter(map -> StringUtils.equalsIgnoreCase(ObjectUtils.toString(map.get("BUS_CLUSTER_ID")), jsBusClusterId))
						.filter(map -> StringUtils.equals(ObjectUtils.toString(map.get("CLUSTER_TYPE")), "zookeeper")).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(zookeeperList)) {
					jstormList.get(k).put("JS_CLUSTER_ID", jstormList.get(k).get("CLUSTER_ID"));
					//zookeeper集群ID
					jstormList.get(k).put("CLUSTER_ID", zookeeperList.get(0).get("CLUSTER_ID"));

					//zk集群名称(jstorm集群编码)
					String zkClusterName = ObjectUtils.toString(zookeeperList.get(0).get("CLUSTER_NAME"));
					String jsClusterCode = ObjectUtils.toString(jstormList.get(k).get("CLUSTER_CODE"));
					jstormList.get(k).put("CLUSTER_INFO", zkClusterName + "(" + jsClusterCode + ")");
				} else {
					jstormList.remove(k);
					k--;
				}
			}

			//对数据进行剔重
			if (CollectionUtils.isNotEmpty(jstormList)) {
				jstormList = jstormList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(map -> ObjectUtils.toString(map.get("CLUSTER_INFO"))))), ArrayList::new));
			}
		}
		log.debug("zookeeper集群: " + jstormList);
		return jstormList;
	}

	/**
	 * 查询ZK集群服务列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> queryZkServiceList(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询ZK集群服务列表， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//根据ZK集群ID查询关联Jstorm集群编码
		List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
		log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

		List<Map<String, Object>> serviceList = new ArrayList<Map<String, Object>>();
		if (!BlankUtil.isBlank(jstormList)) {
			String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));

			//当一个业务主集群绑定多个jstorm集群时，根据前台传入的获取集群，改动最小
			if (jstormList.size() > 1) {
				clusterName = ObjectUtils.toString(params.get("CLUSTER_CODE"));
			}
			List<ZookeeperNode> result;
			try {
				clusterName = StringEscapeUtils.escapeHtml(clusterName);
				result = ZookeeperManager.listZKNodes(clusterName, ZK_SERVICE_REGIST_PATH);
			} catch (Exception e) {
				log.error("获取ZK服务列表失败，失败原因: ", e);
				throw e;
			}
			if (!BlankUtil.isBlank(result)) {
				for (ZookeeperNode zkNode : result) {
					Map<String, Object> nodeMap = new HashMap<String, Object>();
					nodeMap.put("SERVICE_NAME", zkNode.getName());
					nodeMap.put("NODE_DATA", zkNode.getData());
					nodeMap.put("NODE_PATH", zkNode.getPath());
					serviceList.add(nodeMap);
				}
			}
			log.debug("查询ZK集群服务列表结束，服务列表:" + serviceList);
		}

		//根据服务名称升序排列
		Collections.sort(serviceList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> nodeMap1, Map<String, Object> nodeMap2) {
				String serviceName = StringTool.object2String(nodeMap1.get("SERVICE_NAME"));
				String compServiceName = StringTool.object2String(nodeMap2.get("SERVICE_NAME"));
				return serviceName.compareTo(compServiceName);
			}
		});

		return serviceList;
	}


	/**
	 * 查询ZK集群服务组对应的服务列表
	 * @param params
	 * @param dbKey
	 * @throws Exception
	 */
	@Override
	public List<String> queryZkServiceGroupList(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询ZK集群服务列表， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//根据ZK集群ID查询关联Jstorm集群编码
		List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
		log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

		//服务组名称
		String serviceGroupName = StringTool.object2String(params.get("SERVICE_GROUP"));
		//服务组对应的服务列表
		List<String> serviceList = new ArrayList<String>();
		if (!BlankUtil.isBlank(jstormList)) {
			String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));

			//当一个业务主集群绑定多个jstorm集群时，根据前台传入的获取集群，改动最小
			if (jstormList.size() > 1) {
				clusterName = ObjectUtils.toString(params.get("CLUSTER_CODE"));
			}

			try {
				clusterName = StringEscapeUtils.escapeHtml(clusterName);
				String nodeDataStr = ZookeeperManager.getZKNodeData(clusterName, FileTool.exactPath(ZK_SERVICE_INFO_PATH) + serviceGroupName);
				if (!BlankUtil.isBlank(nodeDataStr)) {
					serviceList = JSONObject.parseArray(nodeDataStr, String.class);
				}
			} catch (Exception e) {
				log.error("获取ZK服务组对应服务列表失败，失败原因: ", e);
				throw e;
			}
		}
		return serviceList;
	}

	/**
	 * 查询ZK服务节点信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> queryZkServiceDataList(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询ZK服务节点数据， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//Map<String, Object> retMap = new HashMap<String, Object>();
		//根据ZK集群ID查询关联Jstorm集群编码
		List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
		log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

		List<Map<String, Object>> serviceDataList = new ArrayList<Map<String, Object>>();
		if (!BlankUtil.isBlank(jstormList)) {
			String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));

			//当一个业务主集群绑定多个jstorm集群时，根据前台传入的获取集群，改动最小
			if (jstormList.size() > 1) {
				clusterName = ObjectUtils.toString(params.get("CLUSTER_CODE"));
			}

			//服务名称
			String serviceName = StringTool.object2String(params.get("SERVICE_NAME"));
			String servicePath = ZK_SERVICE_REGIST_PATH + "/" + serviceName;

			List<ZookeeperNode> result;
			try {
				clusterName = StringEscapeUtils.escapeHtml(clusterName);
				result = ZookeeperManager.listZKNodes(clusterName, servicePath);
			} catch (Exception e) {
				log.error("获取ZK服务列表失败，失败原因: ", e);
				throw e;
			}
			if (!BlankUtil.isBlank(result)) {
				for (ZookeeperNode zkNode : result) {
					String nodeName = zkNode.getName();
					String nodePath = servicePath + "/" + nodeName;
					String nodeData = ZookeeperManager.getZKNodeData(clusterName, nodePath);
					if (!BlankUtil.isBlank(nodeData)) {
						String [] nodeList = nodeData.split(";");
						for (String node : nodeList) {
							//任务名称，任务Id, c进程号， pending大小， 执行队列大小，filequeue大小，发送消息总量(向c进程)
							String [] nodeStr = node.split(",");

							Map<String, Object> taskNodeMap = new HashMap<String, Object>();
							taskNodeMap.put("TASK_NAME", nodeStr[0]);
							taskNodeMap.put("TASK_ID", nodeStr[1]);
							taskNodeMap.put("C_PRO_ID", nodeStr[2]);
							taskNodeMap.put("PENDING_SIZE", nodeStr[3]);
							taskNodeMap.put("EXEC_QUENE_SIZE", nodeStr[4]);
							taskNodeMap.put("FILE_QUEUE_SIZE", nodeStr[5]);
							taskNodeMap.put("MSG_COUNT", nodeStr[6]);
							taskNodeMap.put("HOST_IP", nodeName);
							serviceDataList.add(taskNodeMap);
						}
					}
				}

				//根据EXEC_QUENE_SIZE降序排列
				Collections.sort(serviceDataList, new Comparator<Map<String, Object>>() {
					@Override
					public int compare(Map<String, Object> nodeMap1, Map<String, Object> nodeMap2) {
						int taskId1 = Integer.parseInt(StringTool.object2String(nodeMap1.get("EXEC_QUENE_SIZE")));
						int taskId2 = Integer.parseInt(StringTool.object2String(nodeMap2.get("EXEC_QUENE_SIZE")));
						return taskId2 - taskId1;
					}
				});
			}
			log.debug("查询ZK集群服务节点数据结束，返回节点总记录数:" + serviceDataList.size());
		}
		//retMap.put("DATA", serviceDataList);
		log.debug("查询ZK服务积压量结束， 总记录数: " + serviceDataList.size());
		return serviceDataList;
	}

	/**
	 * 查询服务积压情况（根据服务名称分组）
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> queryChartsList(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询服务积压情况，图表展示， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

		//返回服务对象
		List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();

		//查询当前集群所有服务列表
		List<Map<String, Object>> serviceList = this.queryZkServiceList(params, dbKey);
		log.debug("查询服务积压情况，图表展示， 服务列表: " + serviceList);
		if (BlankUtil.isBlank(serviceList)) {
			return rstList;
		}

		//统计每个服务节点数据
		for (int i=0; i<serviceList.size(); i++) {
			Map<String, Object> rstMap = new HashMap<String, Object>();

			String serviceName = StringTool.object2String(serviceList.get(i).get("SERVICE_NAME"));
			//服务名称
			rstMap.put("SERVICE_NAME", serviceName);
			//统计每个服务名称对应的详细数据
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("CLUSTER_ID", params.get("CLUSTER_ID"));
			queryMap.put("SERVICE_NAME", serviceName);
			queryMap.put("CLUSTER_CODE", params.get("CLUSTER_CODE"));  //jstorm集群编码
			List<Map<String, Object>> serviceDataList = this.queryZkServiceDataList(queryMap, dbKey);
			log.debug("查询服务积压情况，图表展示， 服务对应节点信息: " + serviceDataList + ", 服务名称: " + serviceName);

			int totalExecQueneSize = 0;
			//根据任务名称统计队列大小
			Map<String, Object> taskMap = new HashMap<String, Object>();

			HashSet<String> hostIpList = new HashSet<String>();
			if (!BlankUtil.isBlank(serviceDataList)) {
				for (int j=0; j<serviceDataList.size();j++) {

					//任务名称
					String taskName = StringTool.object2String(serviceDataList.get(j).get("TASK_NAME"));

					//执行队列大小(一级缓存积压)
					String execQueneSizeStr = StringTool.object2String(serviceDataList.get(j).get("EXEC_QUENE_SIZE"));
					int execQueneSize = BlankUtil.isBlank(execQueneSizeStr) ? 0 : Integer.parseInt(execQueneSizeStr);

					//二级缓存积压
					String fileQueueSizeStr = StringTool.object2String(serviceDataList.get(j).get("FILE_QUEUE_SIZE"));
					int fileQueueSize = BlankUtil.isBlank(fileQueueSizeStr) ? 0 : Integer.parseInt(fileQueueSizeStr);

					totalExecQueneSize += (execQueneSize + fileQueueSize);

					//任务名称对应执行队列大小
					if (taskMap.containsKey(taskName)) {
						int taskExecQueneSize = Integer.parseInt(StringTool.object2String(taskMap.get(taskName)));
						taskExecQueneSize += (execQueneSize + fileQueueSize);
						taskMap.put(taskName, taskExecQueneSize);
					} else {
						taskMap.put(taskName, (execQueneSize + fileQueueSize));
					}

					//获取主机
					String hostIp = StringTool.object2String(serviceDataList.get(j).get("HOST_IP"));
					//hostIpList.add(hostIp.split(":")[0]);
					hostIpList.add(hostIp);
				}
			}

			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			if (!BlankUtil.isBlank(taskMap) && !taskMap.isEmpty()) {
				Iterator<String> keys = taskMap.keySet().iterator();
				while(keys.hasNext()) {
					Map<String, Object> taskIterMap = new HashMap<String, Object>();
					String keyName = keys.next();
					taskIterMap.put("TASK_NAME", keyName);
					taskIterMap.put("TASK_EXEC_QUENE_SIZE", taskMap.get(keyName));
					taskList.add(taskIterMap);
				}
			}
			//端口个数为0的服务不展示
			if (BlankUtil.isBlank(taskList)) {
				continue;
			}
			//任务队列数据
			rstMap.put("TASK_DATA", taskList);
			//服务关联主机数量
			rstMap.put("HOST_SIZE", hostIpList.size());
			//服务关联主机列表详细信息
			rstMap.put("HOST_LIST", hostIpList);
			//服务执行队列总大小
			rstMap.put("TOTAL_EXEC_QUENE_SIZE", totalExecQueneSize);
			//服务相信信息
			rstMap.put("SERVICE_DATA", serviceDataList);

			rstList.add(rstMap);
		}
		log.debug("查询服务积压情况，图表展示， 查询服务结束，返回结果: " + rstList);
		return rstList;
	}

	/**
	 * 查询ZK服务节点信息
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> queryZkServiceDataListWithGroup(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("查询ZK服务节点数据， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//Map<String, Object> retMap = new HashMap<String, Object>();
		//根据ZK集群ID查询关联Jstorm集群编码
		List<HashMap<String, Object>> jstormList = coreService.queryForList2New("busRelationClusterList.queryClusterListByZkClusterId", params, dbKey);
		log.debug("zookeeper集群关联的Jstorm集群信息为: " + jstormList);

		List<Map<String, Object>> serviceDataList = new ArrayList<Map<String, Object>>();
		if (!BlankUtil.isBlank(jstormList)) {
			String clusterName = StringTool.object2String(jstormList.get(0).get("CLUSTER_CODE"));
			//当一个业务主集群绑定多个jstorm集群时，根据前台传入的获取集群，改动最小
			if (jstormList.size() > 1) {
				clusterName = ObjectUtils.toString(params.get("CLUSTER_CODE"));
			}

			//服务名称
			String serviceName = StringTool.object2String(params.get("SERVICE_NAME"));
			String servicePath = ZK_SERVICE_REGIST_PATH + "/" + serviceName;

			//获取服务组下Worker信息
			List<ZookeeperNode> result;
			try {
				clusterName = StringEscapeUtils.escapeHtml(clusterName);
				result = ZookeeperManager.listZKNodes(clusterName, servicePath);
			} catch (Exception e) {
				log.error("获取ZK服务列表失败，失败原因: ", e);
				throw e;
			}
			if (!BlankUtil.isBlank(result)) {
				for (ZookeeperNode zkNode : result) {
					String nodeName = zkNode.getName();
					String nodePath = servicePath + "/" + nodeName;
					//获取节点信息
					String nodeData = ZookeeperManager.getZKNodeData(clusterName, nodePath);
					log.debug("集群名称: " + clusterName + "节点路径: " + nodePath + "， 节点数据: " + nodeData);
					if (!BlankUtil.isBlank(nodeData)) {
						String [] nodeList = nodeData.split(";");
						for (String node : nodeList) {
							//任务名称，任务Id, c进程号， pending大小， 执行队列大小，filequeue大小，发送消息总量(向c进程)
							String [] nodeStr = node.split(",");

							Map<String, Object> taskNodeMap = new HashMap<String, Object>();
							taskNodeMap.put("TASK_NAME", nodeStr[0]);
							taskNodeMap.put("TASK_ID", nodeStr[1]);
							taskNodeMap.put("C_PRO_ID", nodeStr[2]);
							taskNodeMap.put("PENDING_SIZE", nodeStr[3]);
							taskNodeMap.put("EXEC_QUENE_SIZE", nodeStr[4]);
							taskNodeMap.put("FILE_QUEUE_SIZE", nodeStr[5]);
							taskNodeMap.put("MSG_COUNT", nodeStr[6]);
							taskNodeMap.put("HOST_IP", nodeName);
							serviceDataList.add(taskNodeMap);
						}
					}
				}

				//根据EXEC_QUENE_SIZE降序排列
				Collections.sort(serviceDataList, new Comparator<Map<String, Object>>() {
					@Override
					public int compare(Map<String, Object> nodeMap1, Map<String, Object> nodeMap2) {
						int taskId1 = Integer.parseInt(StringTool.object2String(nodeMap1.get("EXEC_QUENE_SIZE")));
						int taskId2 = Integer.parseInt(StringTool.object2String(nodeMap2.get("EXEC_QUENE_SIZE")));
						return taskId2 - taskId1;
					}
				});
			}
			log.debug("查询ZK集群服务节点数据结束，返回节点总记录数:" + serviceDataList.size());
		}
		//retMap.put("DATA", serviceDataList);
		log.debug("查询ZK服务积压量结束， 总记录数: " + serviceDataList.size());
		return serviceDataList;
	}
}
