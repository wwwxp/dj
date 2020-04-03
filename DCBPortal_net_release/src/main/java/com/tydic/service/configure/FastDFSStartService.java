package com.tydic.service.configure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FastDFSStartService {
	
	/**
	 * 启动fastdfs
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startFastDFS(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	
	/**
	 * 停止fastdfs
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopFastDFS(List<Map<String, String>> map, String dbKey) throws Exception; 
	
	/**
	 * 获取配置文件列表
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getCurrentPathFileList(Map<String, String> map, String dbKey) throws Exception; 
	
	/**
	 * 保存用户初始化数据
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addOperator(Map<String, Object> map, String dbKey) throws Exception; 
	
	/**
	 * 查询用户初始化数据
	 * @param map
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> queryOperator(Map<String, Object> map, String dbKey) throws Exception; 
}
