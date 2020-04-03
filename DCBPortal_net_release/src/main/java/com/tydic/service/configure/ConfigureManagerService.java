package com.tydic.service.configure;

import java.util.List;
import java.util.Map;

public interface ConfigureManagerService {
	/**
	 * 查询ftp服务器下文件目录树信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List loadFileTree(Map<String, String> params,String dbKey)throws Exception;
	
	/**
	 * 获取文件内容（文件服务器）
	 * @param params
	 */
	public Map<String,String> getFileContent(Map<String,String> params,String dbKey)throws Exception;
	
	/**
	 * 获取业务配置文件内容（文件服务器）
	 * @param params
	 */
	public Map<String,String> getFileBusContent(Map<String,String> params,String dbKey)throws Exception;
	
	/**
	 * 获取文件内容（对应主机）
	 * @param params
	 */
	public Map<String,String> showConfigContentByHost(Map<String,String> params,String dbKey)throws Exception;
	
	/**
	 * 保存和分发文件
	 * @param params
	 * @param dbKey
	 */
	public Map<String,Object> updateSaveFileContents(Map<String,Object> params,String dbKey)throws Exception;
	
	/**
	 * 保存和分发业务文件
	 * @param params
	 * @param dbKey
	 */
	public Map<String,Object> updateSaveBusFileContents(Map<String,Object> params,String dbKey)throws Exception;
	
	/**
	 * 平台配置
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> updateDistributeFileContent(Map<String,Object> params,String dbKey)throws Exception;
	
	/**
	 * 新建文件
	 * @param params
	 */
	public Map<String,Object> updateCreateFile(Map<String,Object> params,String dbKey)throws Exception;
	/**
	 * 新建业务文件
	 * @param params
	 */
	public Map<String,Object> updateCreateServiceFile(Map<String,Object> params)throws Exception;
	
	/**
	 *业务新增文件
	 * @param params
	 */
	public Map<String,Object> updateBatchBussFile(Map<String,Object> params)throws Exception;
	
	/**
	 * 重命名文件
	 * @param params
	 */
	public Map<String,Object> updateRenameFile(Map<String,Object> params,String dbKey)throws Exception;
	
	/**
	 * 删除文件
	 * @param params
	 */
	public Map<String,Object> updateDeleteFile(Map<String,Object> params,String dbKey)throws Exception;
	/**
	 * 删除业务文件
	 * @param params
	 */
	public Map<String, Object> updateDeleteServiceFile(Map<String, Object> params) throws Exception;

	/**
	 * 删除sentinel目录实例
	 * @param params
	 */
	public Map<String, Object> deleteSentinelInstance(Map<String, Object> params) throws Exception;

	/**
	 * 批量删除Redis目录实例
	 * @param params
	 */
	public Map<String, Object> deleteBatchRedisFile(Map<String, Object> params, String dbKey) throws Exception;

	/**
	 * 查找指定目录下所有文件
	 * @param params
	 */
	public List loadFileListByFolder(Map<String,Object> params,String dbKey)throws Exception;
	
	/**
	 * 查找指定目录子节点
	 * @param params
	 */
	public List loadFilesUnderGivenPath(Map<String, Object> params,String dbKey)throws Exception;
	
	/**
	 * 新建实例
	 * @param params
	 */
	public Map<String,Object> addCreateAndCopyFolder(Map<String,Object> params,String dbKey) throws Exception;
	
	/**
	 * 批量新建实例
	 * @param params
	 */
	public Map<String,Object> addBatchFileAndFolder(Map<String,Object> params,String dbKey)throws Exception;

	/**
	 * 批量新建redis实例
	 * @param params
	 */
	public Map<String,Object> createBatchRedisFileAndFolder(Map<String,Object> params,String dbKey)throws Exception;

	
	/**
	 * 新增文件
	 * @param params
	 */
	public Map<String,String> addFile(Map<String,Object> params)throws Exception;
	
	/**
	 * 获取脚本文件目录树
	 * @Title: loadScriptTree
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: List
	 * @author: tianjc
	 * @throws Exception 
	 * @date: 2017年6月27日 下午4:55:38
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public List loadScriptTree(Map<String, String> params, String dbKey) throws Exception;

	/**
	 * 组件/业务脚本保存推送
	 * @Title: updateSaveScript
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: Map<String,Object>
	 * @author: tianjc
	 * @date: 2017年6月28日 上午11:04:20
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public Map<String, Object> updateSaveScript(Map<String, Object> params, String dbKey)throws Exception;
	
}
