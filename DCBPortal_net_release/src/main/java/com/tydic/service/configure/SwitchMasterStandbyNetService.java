package com.tydic.service.configure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SwitchMasterStandbyNetService {
	/**
	 * 获取运行的Topology
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getRunningTopologyList(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * 获取待升级的Topology
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getUpgradeTopologyList(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * 获取运行的Topology
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getRunningTopologyNodeList(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * 灰度发布
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> startNodeVersionUpgrade(Map<String, Object> param, String dbKey) throws Exception;
	
//	/**
//	 * 正式发布
//	 * @param param
//	 * @param dbKey
//	 * @return
//	 * @throws Exception
//	 */
//	public HashMap<String, Object> startAllNodeVersionUpgrade(Map<String, Object> param,String dbKey) throws Exception;
}
