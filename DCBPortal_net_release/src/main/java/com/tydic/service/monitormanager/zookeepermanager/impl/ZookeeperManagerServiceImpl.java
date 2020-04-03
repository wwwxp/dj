package com.tydic.service.monitormanager.zookeepermanager.impl;

import java.io.FileReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.monitormanager.clustersummary.impl.ClusterSummaryServiceImpl;
import com.tydic.service.monitormanager.zookeepermanager.ZookeeperManagerService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.StringTool;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.monitormanager.zookeepermanager.impl]    
  * @ClassName:    [ZookeeperManagerServiceImpl]     
  * @Description:  [zookeeper管理]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:07:17]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:07:17]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service("zookeeperService")
public class ZookeeperManagerServiceImpl implements ZookeeperManagerService{
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ZookeeperManagerServiceImpl.class);
	
	@Resource
    private CoreService coreService;
	
	/**
	 * 获取Zookeeper信息
	 * @param params 业务参数
	 * @param dbKey
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> showCluster(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("根据集群名称获取zookeeper信息, 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		//获取集群名称
		String clusterName = StringTool.object2String(params.get("clusterName"));
		
		// 获取Jstorm配置文件
		String filePath = URLDecoder.decode(ClusterSummaryServiceImpl.class.getResource("/").getPath(), "UTF-8") + "/storm.yaml";
		// 是否自定义配置文件路径
		String fileSwitch = Constant.FILE_SWITCH;
		if (BusinessConstant.PARAMS_BUS_1.equals(fileSwitch)) {
			filePath = Constant.STORAM_YAML_PATH;
		}
		
		//zookeeper集群信息
		List<Map<String, Object>> zkList = new ArrayList<Map<String, Object>>();
		
		//集群信息
		List<Map<String, Object>> clusterList = new ArrayList<Map<String, Object>>();
		try {
			Yaml yaml = new Yaml();
			Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(new FileReader(filePath));
			List<Map<String, Object>> yamlClusterList = (List<Map<String, Object>>) yamlMap.get("ui.clusters");
			if (!BlankUtil.isBlank(yamlClusterList)) {
				for (int i=0; i<yamlClusterList.size(); i++) {
					String yamlClusterName = StringTool.object2String(yamlClusterList.get(i).get("name"));
					String zkRoot = StringTool.object2String(yamlClusterList.get(i).get("zkRoot"));
					String zkPort = StringTool.object2String(yamlClusterList.get(i).get("zkPort"));
					//Object zkServers = yamlClusterList.get(i).get("zkServers");
					List<String> hostList = (List<String>) yamlClusterList.get(i).get("zkServers");
					//如果ZK根目录为空则直接过来该集群
					if (BlankUtil.isBlank(zkRoot)) {
						continue;
					}
					
					Map<String, Object> clusterMap = new HashMap<>();
					clusterMap.put("name", yamlClusterName);
					clusterMap.put("zkRoot", "/"+yamlClusterName);
					clusterMap.put("clusterName", yamlClusterName);
					clusterMap.put("hostList", hostList);
					clusterMap.put("port", zkPort);
					clusterList.add(clusterMap);
				}
			}
			
			//获取当前集群Zookeeper信息
			if (!BlankUtil.isBlank(clusterList)) {
				for (int i=0; i<clusterList.size(); i++) {
					
					//根据集群名称查询ZK集群
					if (!BlankUtil.isBlank(clusterName)) {
						if (clusterName.equals(clusterList.get(i).get("name"))) {
							Map<String, Object> zkMap = clusterList.get(i);
							
							//查询主机状态
							Map<String, Object> queryParams = new HashMap<String, Object>();
							queryParams.put("HOST_LIST", zkMap.get("hostList"));
							queryParams.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
							List<HashMap<String, Object>> hostList = coreService.queryForList2New("host.queryZkHostList", queryParams, dbKey);
							
							zkMap.put("hostList", hostList);
							zkList.add(zkMap);
						}
					} else {
						//查询所有ZK集群
						Map<String, Object> zkMap = clusterList.get(i);
						//查询主机状态
						Map<String, Object> queryParams = new HashMap<String, Object>();
						queryParams.put("HOST_LIST", zkMap.get("hostList"));
						queryParams.put("CLUSTER_TYPE", Constant.ZOOKEEPER);
						List<HashMap<String, Object>> hostList = coreService.queryForList2New("host.queryZkHostList", queryParams, dbKey);
						zkMap.put("hostList", hostList);
						zkList.add(zkMap);
					}
				}
			}
		} catch (Exception e) {
			log.error("获取集群信息失败", e);
		}
		return zkList;
				
		
//		List resultList = new ArrayList();
//		Map<String, Object> parameter=new HashMap();
//		//集群code
//		String clusterName=params.get("clusterName");
//		
//		List<HashMap<String, String>> clusters=new ArrayList<HashMap<String,String>>();
//		parameter.put("CODE", "zookeeper");
//		Map zkInfo=coreService.queryForObject2New("serviceType.queryServiceTypeList", parameter, dbKey);
//		//判断是否分集群
//		parameter.put("IS_CLUSTER", zkInfo.get("IS_CLUSTER"));
//		
//		//查询集群信息
//		clusters=coreService.queryForList("clusterConfig.queryCluster", params, dbKey);
//		
//		//循环获取集群里的信息
//		for(int i=0;i<clusters.size();i++){
//			Map<String,Object> resultMap=new HashMap<String,Object>();
//			List hostList=new ArrayList();
//			
//			String CLUSTER_CODE=clusters.get(i).get("CLUSTER_CODE");
//			String CLUSTER_NAME=clusters.get(i).get("CLUSTER_NAME");
//			//获取端口信息
//			String port="";
//			List<ZooKeeperEntity> zkEntity=UIUtils.getZooKeeperEntities(CLUSTER_CODE);
//			if(zkEntity.size()>0 && zkEntity!=null){
//				port=zkEntity.get(0).getPort();
//			}
//			//获取主机信息
//			parameter.put("CLUSTER_ID", clusters.get(i).get("ID"));
//			hostList=coreService.queryForList2New("host.queryHostInfoByCluster", parameter, dbKey);
//           
//            resultMap.put("name", CLUSTER_NAME);
//            resultMap.put("zkRoot", "/"+CLUSTER_CODE);
//            resultMap.put("clusterName",CLUSTER_CODE);
//            resultMap.put("hostList", hostList);
//            resultMap.put("port", port);
//            resultList.add(resultMap);
//		}
//		
//		return resultList;
	}
}
