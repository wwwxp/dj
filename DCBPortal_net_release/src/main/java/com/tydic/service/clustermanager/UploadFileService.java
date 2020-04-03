package com.tydic.service.clustermanager;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.tydic.util.ftp.FileRecord;

/**
 * ftp或sftp文件上传
 * @author 
 *
 */
public interface UploadFileService {

	/**
	 * 登录sftp或ftp并上传
	 * @param uFile
	 * @param formMap
	 * @return
	 * @throws Exception
	 */
	public String insertFileUpload(MultipartFile uFile, Map<String, String> formMap, String dbKey) throws Exception;

	/**
	 * 版本回退
	 * @param params
	 * @throws Exception
	 */
	public void deleteOrBack(Map<String, String> params, String dbKey) throws Exception;
	
	/**
	 * 查询文件
	 * @param params
	 * @throws Exception
	 */
	public List<FileRecord> queryRemoteFiles(Map<String,String> params) throws Exception;

	/**
	 * 删除业务程序包
	 * @param queryMap
	 * @throws Exception
	 */
	public void deleteBusinessPackage(Map<String, Object> queryMap, String dbKey) throws Exception;
	
	/**
	 * 删除框架版本包
	 * @param queryMap
	 * @throws Exception
	 */
	public Map<String, Object> deletePlatformPackage(Map<String, String> queryMap, String dbKey) throws Exception;
	

	public List queryFileTree(Map<String, String> paramsMap) throws Exception;

	
	
}
