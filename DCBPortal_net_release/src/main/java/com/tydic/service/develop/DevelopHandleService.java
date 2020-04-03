package com.tydic.service.develop;

import java.util.List;
import java.util.Map;

public interface DevelopHandleService {
	/**
	 * 查询文件目录树信息
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public List listFilesTree(Map<String, String> params)throws Exception;

	/**
	 * 新建文件或目录
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map createDirectoryOrFile(Map<String, String> params)throws Exception ;

	/**
	 * 重命名文件或目录
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map renameDirectoryOrFile(Map<String, String> params) throws Exception ;

	/**
	 * 删除文件或目录
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map deleteDirectoryOrFile(Map<String, String> params) throws Exception;
	
	/**
	 * 打开在线代码编辑页面
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map openDevelopFile(Map<String, String> params) throws Exception;
	
	/**
	 * 保存代码内容
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map saveDevelopFile(Map<String, String> params) throws Exception;
	
	/**
	 * 发布
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Map release(Map<String, String> params) throws Exception;
	
}
