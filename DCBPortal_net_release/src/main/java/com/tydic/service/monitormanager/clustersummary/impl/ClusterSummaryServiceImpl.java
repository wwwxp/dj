package com.tydic.service.monitormanager.clustersummary.impl;

import backtype.storm.generated.ClusterSummary;
import backtype.storm.utils.NimbusClient;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.jstorm.ui.model.*;
import com.alibaba.jstorm.ui.utils.NimbusClientManager;
import com.alibaba.jstorm.ui.utils.UIUtils;
import com.esotericsoftware.minlog.Log;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.monitormanager.clustersummary.ClusterSummaryService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.PropertiesUtils;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.SftpTran;
import com.tydic.util.ftp.Trans;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.monitormanager.clustersummary.impl]    
  * @ClassName:    [ClusterSummaryServiceImpl]     
  * @Description:  [集群摘要处理类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:02:12]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:02:12]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service("clusterSummaryService")
public class ClusterSummaryServiceImpl implements ClusterSummaryService{
	
	private static Logger log = LoggerFactory.getLogger(ClusterSummaryServiceImpl.class);
	
	private static final String FILE_SWITCH_CLOSED = "1";

	// 组件名称
	private static final String COMPONENT_TYPE_NIMBUS = "nimbus";
	private static final String COMPONENT_TYPE_JSTORM = "jstorm";

	@Autowired
	private CoreService coreService;

	/**
	 * 查询集群列表信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> getBusClusterList(Map<String, Object> params) throws Exception {
		log.info("查询业务集群列表，业务参数：{}", params);
		int pageSize = Integer.parseInt(com.tydic.bp.common.utils.tools.StringTool.object2String(params.get("PAGE_SIZE")));
		int pageIndex = Integer.parseInt(com.tydic.bp.common.utils.tools.StringTool.object2String(params.get("PAGE_INDEX")));
		//超级管理员展示所有集信息
		if(BusinessConstant.PARAMS_BUS_1.equals(StringTool.object2String(params.get("EMPEE_ID")))){
			params.put("PERMISSION_SWITCH", "no");
		}else{
			params.put("PERMISSION_SWITCH", SystemProperty.getContextProperty("cluster.permission.switch"));
		}
		Map<String, Object> retMap = coreService.queryPageList2New("busMainCluster.queryBusMainClusterRelationJstormList", pageSize, pageIndex, params, FrameConfigKey.DEFAULT_DATASOURCE);
		return retMap;
	}

	/**
	 * 同步Yaml配置文件
	 */
	public void initStormYaml(String filePath, String ... args) {
		try {
			synchronized (ClusterSummaryServiceImpl.class) {
				log.debug("解析Nimbus Yaml配置文件开始, 本地配置文件路径: " + filePath);
				Map<String, Object> yamlMap = new HashMap<String, Object>();
				List<Map<String, Object>> yamlList = new ArrayList<Map<String, Object>>();

				// 获取ZK集群信息
				Map<String, Object> queryMap = new HashMap<String, Object>();
				queryMap.put("DEPLOY_FILE_TYPE", COMPONENT_TYPE_NIMBUS);
				queryMap.put("DEPLOY_TYPE", COMPONENT_TYPE_JSTORM);
				List<HashMap<String, Object>> nimbusList = coreService.queryForList2New("clusterConfig.queryNimbusClusterList",
								queryMap, FrameConfigKey.DEFAULT_DATASOURCE);
				if (!BlankUtil.isBlank(nimbusList)) {
					for (int i = 0; i < nimbusList.size(); i++) {
						String hostIp = StringTool.object2String(nimbusList.get(i).get("HOST_IP"));
						String sshPort = StringTool.object2String(nimbusList.get(i).get("SSH_PORT"));
						String sshUser = StringTool.object2String(nimbusList.get(i).get("SSH_USER"));
						String sshPwd = StringTool.object2String(nimbusList.get(i).get("SSH_PASSWD"));
						String yamlPath = StringTool.object2String(nimbusList.get(i).get("FILE_PATH"));
						if (!BlankUtil.isBlank(sshPwd)) {
							sshPwd = DesTool.dec(sshPwd);
						} else {
							log.debug("请配置主机密码, 主机: " + hostIp);
							continue;
						}
						try{
							//登录Nimbus所在主机解析Yaml配置文件
							Trans trans = new SftpTran(hostIp, Integer.parseInt(sshPort), sshUser, sshPwd, 8000);
							trans.login();
							InputStream in = trans.get(yamlPath);
							Yaml yaml = new Yaml();
							Map<String, Object> remoteYamlMap = (Map<String, Object>) yaml.load(in);
							in.close();
							trans.completePendingCommand();
							Object zkServers = remoteYamlMap.get("storm.zookeeper.servers");
							Object zkRoot = remoteYamlMap.get("storm.zookeeper.root");
							Object zkPort = remoteYamlMap.get("storm.zookeeper.port");
							log.debug("获取远程Nimbus配置文件信息: zkServers: "
									+ zkServers.toString() + ", zkRoot: " + zkRoot
									+ ", zkPort: " + zkPort);

							//判断该Nimbus是否为备机(主备配置文件一直), 直接跳过
							if (!BlankUtil.isBlank(yamlList)) {
								boolean isExists = false;
								for (int j = 0; j < yamlList.size(); j++) {
									String oldZkRoot = StringTool.object2String(yamlList.get(j).get("zkRoot")).trim();
									log.debug("ZK根目录： " + zkRoot + "， 上一个ZK根目录： " + oldZkRoot);
									String newZkRoot = StringTool.object2String(zkRoot).trim();
									;
									if (oldZkRoot.equalsIgnoreCase(newZkRoot)) {
										isExists = true;
										break;
									}
								}
								if (isExists) {
									continue;
								}
							}

							Map<String, Object> tempMap = new HashMap<String, Object>();
							tempMap.put("name", StringTool.object2String(zkRoot).replaceAll("/", ""));
							tempMap.put("zkRoot", zkRoot);
							tempMap.put("zkPort", zkPort);
							tempMap.put("zkServers", zkServers);

							yamlList.add(tempMap);
						}catch (Exception e){
							continue;
						}
					}
					yamlMap.put("ui.clusters", yamlList);
					log.debug("生成的内容：" + yamlMap);
					PropertiesUtils.updateYamlFile(filePath, yamlMap);
				} else {
					yamlMap.put("ui.clusters", yamlList);
					log.debug("生成的内容：" + yamlMap);
					PropertiesUtils.updateYamlFile(filePath, yamlMap);
				}
				log.debug("Yaml配置文件同步完成...");
			}
		} catch (Exception e) {
			log.error("解析Yaml配置文件失败", e);
		}
	}
	
	/**
	 * 解析Storm.yaml配置文件，获取集群配置项
	 */
	@Override
	public List<Map<String, Object>> getClusterList(Map<String, String> params) throws Exception {
		log.debug("获取集群信息, 参数:" + params.toString());
		
		// 获取Jstorm配置文件
		String filePath = URLDecoder.decode(ClusterSummaryServiceImpl.class.getResource("/").getPath(), "UTF-8") + "/storm.yaml";
		// 是否自定义配置文件路径
		String fileSwitch = Constant.FILE_SWITCH;
		if (FILE_SWITCH_CLOSED.equals(fileSwitch)) {
			filePath = Constant.STORAM_YAML_PATH;
		}
					
		//同步配置文件
		initStormYaml(filePath);
		
		//集群信息
		List<Map<String, Object>> clusterList = new ArrayList<Map<String, Object>>();
		try {
			Yaml yaml = new Yaml();
			Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(new FileReader(filePath));
			List<Map<String, Object>> yamlClusterList = (List<Map<String, Object>>) yamlMap.get("ui.clusters");
			if (!BlankUtil.isBlank(yamlClusterList)) {
				for (int i=0; i<yamlClusterList.size(); i++) {
					String clusterName = StringTool.object2String(yamlClusterList.get(i).get("name"));
					String zkRoot = StringTool.object2String(yamlClusterList.get(i).get("zkRoot"));
					String zkPort = StringTool.object2String(yamlClusterList.get(i).get("zkPort"));
					//如果ZK根目录为空则直接过来该集群
					if (BlankUtil.isBlank(zkRoot)) {
						continue;
					}
					Map<String, Object> clusterMap = new HashMap<>();
					clusterMap.put("CLUSTER_NAME", clusterName);
					clusterMap.put("ZK_ROOT", zkRoot);
					clusterMap.put("ZK_PORT", zkPort);
					clusterList.add(clusterMap);
				}
			}
		} catch (Exception e) {
			log.error("获取集群信息失败", e);
		}
		return clusterList;
	}
	
	/**
	 * 获取表格信息
	 */
	@Override
	public Map<String, Object> showCluster(Map<String, Object> params) throws Exception {
		log.debug("集群摘要， 获取集群信息， 参数: " + params.toString());
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		//Jstorm集群名称（对应的是dcf_service_type.CLUSTER_CODE字段）
		String clusterName = StringTool.object2String(params.get("clusterName"));
		
		//转译特殊字符
		String name=StringEscapeUtils.escapeHtml(clusterName);
		
		// 获取Jstorm配置文件
		String filePath = URLDecoder.decode(ClusterSummaryServiceImpl.class.getResource("/").getPath(), "UTF-8") + "/storm.yaml";
		// 是否自定义配置文件路径
		String fileSwitch = Constant.FILE_SWITCH;
		if (FILE_SWITCH_CLOSED.equals(fileSwitch)) {
			filePath = Constant.STORAM_YAML_PATH;
		}
		//同步配置文件
		initStormYaml(filePath);
		log.debug("storm.yaml文件更新完成!");
		
		NimbusClient client = null;
		
		try {
			if (StringUtils.isNotBlank(name)) {
				client = NimbusClientManager.getNimbusClient(name);

				ClusterSummary clusterSummary = client.getClient().getClusterInfo();

				//获取集群信息
				ClusterEntity clusterInfo = UIUtils.getClusterEntity(clusterSummary, name);
				List<ClusterEntity> clusterData = new ArrayList<ClusterEntity>();
				clusterData.add(clusterInfo);

				//获取拓扑信息
				List<TopologyEntity> topologyData = UIUtils.getTopologyEntities(clusterSummary);
				log.debug("获取Topology信息: " + topologyData);

				//获取nimbus信息
				List<NimbusEntity> nimbusData = UIUtils.getNimbusEntities(clusterSummary);
				log.debug("获取Nimbus信息: " + nimbusData);

				//获取supervisor信息
				List<SupervisorEntity> supervisorData = UIUtils.getSupervisorEntities(clusterSummary);
				log.debug("获取Supervisor信息: " + supervisorData);

				//获取zookeeper信息
				List<ZooKeeperEntity> zookeeperData = UIUtils.getZooKeeperEntities(name);
				log.debug("获取Zookeeper信息: " + supervisorData);

				//封装返回值
				resultMap.put("clusterData", clusterData);
				resultMap.put("topologyData", topologyData);
				resultMap.put("nimbusData", nimbusData);
				resultMap.put("supervisorData", supervisorData);
				resultMap.put("zookeeperData", zookeeperData);
			}
		} catch (Exception e) {
			Log.error("获取topology信息失败， 失败原因:", e);
		} finally{
			if(client !=null){
				client.close();
			}
		}
		return resultMap;
	}
	
	/**
	 *  拓扑信息--获取配置信息
	 */
	@Override
	public List showTopConfInfo(Map<String, String> params) throws Exception {
		String clusterName=params.get("clusterName");
		String topologyId=params.get("topologyId");
		
		List list;
		try {
			Map conf=UIUtils.getTopologyConf(clusterName, topologyId);

			list = new ArrayList();
			Set confKey= conf.keySet();
			Iterator iterator=confKey.iterator();
			while(iterator.hasNext()){
				Map item= new HashMap();
				String key=(String)iterator.next();
				Object value=conf.get(key);
				
				item.put("key", key);
				item.put("value",String.format("%s", value));
				list.add(item);
			}
		} catch (Exception e) {
			throw new Exception("获取配置信息失败!");
		}
		return list;
	}
	
	/**
	 * nimbus信息--获取配置信息
	 */
	@Override
	public List showNimConfInfo(Map<String, String> params) throws Exception {
		String clusterName=params.get("clusterName");
		
		List list;
		try {
			Map conf=UIUtils.getNimbusConf(clusterName);
			System.out.println(conf);
			list = new ArrayList();
			Set confKey= conf.keySet();
			Iterator iterator=confKey.iterator();
			while(iterator.hasNext()){
				Map item= new HashMap();
				String key=(String)iterator.next();
				item.put("key", key);
				item.put("value", String.format("%s", conf.get(key)));
				list.add(item);
			}
		} catch (Exception e) {
			throw new Exception("获取配置信息失败!");
		}
		return list;
	}
	
	/**
	 * supervisor信息--获取配置信息
	 */
	@Override
	public List showSupConfInfo(Map<String, String> params) throws Exception {
		
		String clusterName=params.get("clusterName");
		String host=params.get("host");
		//获取端口
		Integer port=UIUtils.getSupervisorPort(clusterName);
		//获取supervisor信息

		List list;
		try {
			String data=UIUtils.getSupervisorConf(host, port).getData();
			//强string类型转换成jsonObject以便在转换成map
			JSONObject dataObject = JSONObject.parseObject(data);
			Map dataMap = (Map)dataObject;
			
			list = new ArrayList();
			Set confKey= dataMap.keySet();
			Iterator iterator=confKey.iterator();
			while(iterator.hasNext()){
				Map item= new HashMap();
				String key=(String)iterator.next();
				item.put("key", key);
				item.put("value", String.format("%s", dataMap.get(key)));
				list.add(item);
			}
		} catch (Exception e) {
			throw new Exception("获取配置信息失败!");
		}
		return list;
	}
	
}
