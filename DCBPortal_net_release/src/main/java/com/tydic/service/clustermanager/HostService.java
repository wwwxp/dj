package com.tydic.service.clustermanager;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface HostService {

	public void deleteHost(List<Map<String, String>> paramsList, String dbKey)
			throws Exception;

	/**
	 * 新增主机
	 * 
	 * @param param
	 */
	public void insertHost(Map<String, String> param, String dbKey)
			throws Exception;

	/**
	 * 查询主机
	 * 
	 * @param param
	 */
	public Map<String, String> queryHostInfo(Map<String, String> param,
			String dbKey) throws Exception;

	/**
	 * 修改主机
	 * 
	 * @param param
	 */
	public void updateHost(Map<String, String> param, String dbKey)
			throws Exception;

	/**
	 * 修改修改主机密码
	 *
	 * @param param
	 */
	public Map<String, Object> updatePasswdBatch(Map<String, Object> param, String dbKey) throws Exception;


	/**
	 * 批量导入excel表格
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> queryHostInfoForExcel(MultipartFile uFile, HttpServletRequest request, Map<String, String> param, String dbKey) throws Exception;



}
