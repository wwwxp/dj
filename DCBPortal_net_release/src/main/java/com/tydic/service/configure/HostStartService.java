package com.tydic.service.configure;

import com.tydic.common.BusException;

import java.util.List;
import java.util.Map;

public interface HostStartService {

	/**
	 * M2DB表刷新
	 * 
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<String> updateM2dbRefreshTables(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * M2DB数据刷新
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<String> updateM2dbRefreshMem(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * M2DB数据导入
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<String> updateM2dbInputTable(Map<String, Object> param, String dbKey) throws Exception;

	/**
	 * 主机进程状态检查
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> checkProcessState(Map<String, Object> params) throws Exception;

	/**
	 * 修改主机进程
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateProcessState(Map<String, Object> params, String dbKey) throws Exception;


	/**
	 * 组件批量状态检查
	 * @return
	 */
	public Map<String, Object> batchCheckStatus(List<Map<String, Object>> params, String dbKey) throws BusException;

}
