package com.tydic.service.configure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SwitchMasterStandbyAbmService {
	
	/**
	 * 值获取：本地网
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getLatnElement(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 程序操作：灰度升级
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateGreyUpgrade(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 程序操作：正式发布
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateOfficialLaunch(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 程序操作：回退
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateRollback(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 程序操作：灰度升级配置文件修改
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateGreyUpgradeConfig(Map<String,Object> param,String dbKey) throws Exception;
}
