package com.tydic.util.zk;

import backtype.storm.command.list;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.util.StringTool;
import org.apache.commons.lang.ObjectUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

public class ZkClientUtil {

	/**
	 * 创建ZkClient对象
	 * CuratorFrameworkFactory工厂在创建出一个客户端CuratorFramework实例之后，
	 * 实质上并没有完成会话的创建，而是需要调用CuratorFramework的start()方法来完成会话的创建。
	 *
	 * @return
	 */
	public static CuratorFramework createZkClient(String clusterCode) {

		Properties prop = initZkConfig(clusterCode);
		//Properties prop = new Properties();
		//prop.setProperty("dca.zk.connectString","192.168.161.28:2283,192.168.161.24:2283,192.168.161.25:2283");

		//获取配置文件 zk.properties 创建客户端参数
		int baseSleepTimeMs = Integer.valueOf(prop.getProperty("dca.zk.baseSleepTimeMs", "1000"));
		int maxRetries = Integer.valueOf(prop.getProperty("dca.zk.maxRetries", "3"));
		String connectString = prop.getProperty("dca.zk.connectString");
		int sessionTimeoutMs = Integer.valueOf(prop.getProperty("dca.zk.sessionTimeoutMs", "5000"));
		int connectionTimeoutMs = Integer.valueOf(prop.getProperty("dca.zk.connectionTimeoutMs", "3000"));
		//创建client对象
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString(connectString)
				.sessionTimeoutMs(sessionTimeoutMs)
				.connectionTimeoutMs(connectionTimeoutMs)
				.retryPolicy(retryPolicy)
				.build();
		return client;
	}

	public static Properties initZkConfig(String clusterCode) {
		CoreService coreService = (CoreService) SpringContextUtil.getBean("coreService");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("CLUSTER_CODE", clusterCode);
		params.put("CLUSTER_TYPE", "dca");
		params.put("CFG_TYPE", "zookeeper");
		List<HashMap<String, Object>> list = coreService.queryForList2New("componentsConfig.queryComponentConfigListByClusterCode",
				params, FrameConfigKey.DEFAULT_DATASOURCE);
		if (list != null && list.size() > 0) {
			Properties dsProp = new Properties();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> dsMap = list.get(i);

				// 获取zookeeper配置信息
				String cfgCode = StringTool.object2String(dsMap.get("CFG_CODE"));
				String cfgValue = StringTool.object2String(dsMap.get("CFG_VALUE"));
				dsProp.setProperty(cfgCode, cfgValue);
			}

			return dsProp;

		} else {
			throw new RuntimeException("zookeeper未配置");

		}

	}
	public static void main(String [] args) throws  Exception{
		//加载配置文件
		CuratorFramework client = ZkClientUtil.createZkClient("DCA");
		client.start();
		/*byte [] byty = client.getData().forPath("/DCA/redisCfg");
		String msg = new String(byty);
		System.out.println(msg);
		Map<String,String> ipMap = new HashMap<>();
		Map<String,String> portMap = new HashMap<>();
		JSONObject jsonObject = JSON.parseObject(msg);
		Iterator<Map.Entry<String, Object>> entries = jsonObject.entrySet().iterator();
		while(entries.hasNext()){
			Map.Entry<String, Object> entry = entries.next();
			List<Map<String,Object>> ObjList = (List<Map<String,Object>>)entry.getValue();
			if(ObjList != null ){
				for(Map<String,Object> map : ObjList){
					String ip = ObjectUtils.toString(map.get("ip"));
					String port = ObjectUtils.toString(map.get("port"));
					System.out.println("ip:" + ip + ",port:" + port);
					ipMap.put(ip,"ip");
					portMap.put(port+":"+ip,"ip");
					List<Map<String,Object>> slaveList = (List<Map<String,Object>>)map.get("slave");
					for(Map<String,Object> slaveMap : slaveList){
						String slaveIp = ObjectUtils.toString(slaveMap.get("ip"));
						String slavePort = ObjectUtils.toString(slaveMap.get("port"));
						System.out.println("ip:" + slaveIp + ",port:" + slavePort);
						ipMap.put(slaveIp,"ip");
						portMap.put(slavePort+":"+slaveIp,"ip");
					}
				}
			}
		}

		List<String> ipList = new ArrayList<>();
		Iterator<Map.Entry<String, String>> ipentries = ipMap.entrySet().iterator();
		while (ipentries.hasNext()) {
			Map.Entry<String, String> entry = ipentries.next();
			ipList.add(entry.getKey());
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}




        Map<String,List<Map<String,String>>> portEMap = new HashMap<>();
		Iterator<Map.Entry<String, String>> portentries = portMap.entrySet().iterator();
		while (portentries.hasNext()) {
			Map.Entry<String, String> entry = portentries.next();
			 String [] key = entry.getKey().split(":");
			List<Map<String,String>> list;
			 if(portEMap.containsKey(key[1])){
				  list = portEMap.get(key[1]);
			 } else{
				 list =  new ArrayList<>();
				 portEMap.put(key[1],list);
			 }
			Map<String,String> tmpMap = new HashMap<>();
			tmpMap.put("text",key[0]);
			list.add(tmpMap);
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());


		}

		Collections.sort(ipList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});


		Iterator<Map.Entry<String, List<Map<String,String>>>> portentries1 = portEMap.entrySet().iterator();
		while (portentries1.hasNext()) {
			Map.Entry<String, List<Map<String,String>>> entry = portentries1.next();
			Collections.sort(portEMap.get(entry.getKey()), new Comparator<Map<String,String>>() {
				@Override
				public int compare(Map<String,String> o1, Map<String,String> o2) {
					return o1.get("text").compareTo(o2.get("text"));
				}
			});

		}
		System.out.println(ipList);
		System.out.println(portEMap);*/


		//client.create().forPath("/DCA");
		//client.setData().forPath()
		client.setData().forPath("/DCA/redisCfg","{\t\"BM\": [{\t\t\t\"hid\": \"master01\",\t\t\t\"vnode\": 1000,\t\t\t\"ip\": \"192.168.161.28\",\t\t\t\"port\": 7490,\t\t\t\"state\": \"offline\",\t\t\t\"slave\": [{\t\t\t\t\"ip\": \"192.168.161.24\",\t\t\t\t\"port\": 7490,\t\t\t\t\"state\": \"offline\"\t\t\t}]\t\t},\t\t{\t\t\t\"hid\": \"master03\",\t\t\t\"vnode\": 1000,\t\t\t\"ip\": \"192.168.161.28\",\t\t\t\"port\": 7419,\t\t\t\"state\": \"offline\",\t\t\t\"slave\": [{\t\t\t\t\"ip\": \"192.168.161.24\",\t\t\t\t\"port\": 7409,\t\t\t\t\"state\": \"offline\"\t\t\t}]\t\t}, {\t\t\t\"hid\": \"master04\",\t\t\t\"vnode\": 1000,\t\t\t\"ip\": \"192.168.161.28\",\t\t\t\"port\": 7498,\t\t\t\"state\": \"offline\",\t\t\t\"slave\": [{\t\t\t\t\"ip\": \"192.168.161.24\",\t\t\t\t\"port\": 7497,\t\t\t\t\"state\": \"offline\"\t\t\t}]\t\t}, {\t\t\t\"hid\": \"master06\",\t\t\t\"vnode\": 1000,\t\t\t\"ip\": \"192.168.161.21\",\t\t\t\"port\": 7333,\t\t\t\"state\": \"offline\",\t\t\t\"slave\": [{\t\t\t\t\"ip\": \"192.168.161.21\",\t\t\t\t\"port\": 8333,\t\t\t\t\"state\": \"offline\"\t\t\t}]\t\t}, {\t\t\t\"hid\": \"master04\",\t\t\t\"vnode\": 1000,\t\t\t\"ip\": \"192.168.161.26\",\t\t\t\"port\": 7498,\t\t\t\"state\": \"offline\",\t\t\t\"slave\": [{\t\t\t\t\"ip\": \"192.168.161.21\",\t\t\t\t\"port\": 7497,\t\t\t\t\"state\": \"offline\"\t\t\t}]\t\t}\t],\t\"CRM\": []}".getBytes());
		//client.close();
	}

}
