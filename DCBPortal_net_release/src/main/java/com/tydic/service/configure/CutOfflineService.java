package com.tydic.service.configure;

import java.util.Map;

public interface CutOfflineService {

	/**
	 * 获取sp_switch.xml已有号段、网元信息
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getExistNumOrNetInfo(Map<String, Object> param,String dbKey) throws Exception;
	
	/**
	 * 操作：切离线/不切离线
	 * @param param
	 * @param execKey
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	public Map<String, Object> updateCutOffline(Map<String, Object> param,String dbKey) throws Exception;
	
}
