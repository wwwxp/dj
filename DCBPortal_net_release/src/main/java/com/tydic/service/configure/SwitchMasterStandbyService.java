package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface SwitchMasterStandbyService {
	/**
	 * 值获取：网元
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public List<Map<String, Object>> getNetElement(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 获取sp_switch.xml已有号段信息
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getExistNumInfo(Map<String, Object> param,String dbKey) throws Exception;
	
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
