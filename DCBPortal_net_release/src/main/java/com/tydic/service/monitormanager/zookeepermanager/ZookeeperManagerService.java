package com.tydic.service.monitormanager.zookeepermanager;

import java.util.List;
import java.util.Map;

public interface ZookeeperManagerService {
	/**
	 * 获取集群摘要表格信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> showCluster(Map<String, Object> params, String dbKey)throws Exception;
}
