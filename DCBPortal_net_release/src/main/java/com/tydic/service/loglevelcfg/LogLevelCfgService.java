package com.tydic.service.loglevelcfg;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface LogLevelCfgService {

	public void update(Map<String,Object> param,String dbKey) throws Exception;
	public void sendMsg(Map<String,Object> param) throws Exception;
	
	/**
	 * 删除日志级别
	 * @param paramsList
	 * @param dbKey
	 * @throws Exception
	 */
	public Map<String, Object> delLogLevel(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 新增日志级别
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addLogLevel(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

	
	/**
	 * 修改日志级别配置信息
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> updateLogLevel(Map<String, Object> params, String dbKey, HttpServletRequest request) throws Exception;

}
