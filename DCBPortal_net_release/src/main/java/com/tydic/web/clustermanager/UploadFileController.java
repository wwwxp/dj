package com.tydic.web.clustermanager;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.UploadFileService;
import com.tydic.util.SessionUtil;
import com.tydic.util.ftp.FileRecord;

@Controller
@RequestMapping("/uploadFTP")
public class UploadFileController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(UploadFileController.class);

	@Autowired
	public CoreService coreService;
	
	@Autowired
	private UploadFileService uploadFileService;

	/**
	 * 文件上传
	 * @param uFile
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/fileUpload",produces = {"text/html;charset=UTF-8"})
    @ResponseBody
    public String insertDeployHost(@RequestParam MultipartFile uFile, HttpServletRequest request,HttpServletResponse response) throws Exception {
    	log.debug("开始版本上传...");  
		
		//获取页面传递的其他信息
		Map<String, String> formMap = this.getParams("FILE_NAME,uploadType,remoteFile,VERSION,FILE_TYPE,BUS_CLUSTER_ID,fileSuffixType,DESCRIPTION,PACKAGE_TYPE",request);
		formMap.put("webRootPath", SessionUtil.getWebRootPath(request));
		PrintWriter out =response.getWriter();
		// 上传文件并解压
		try{
			String fileInfo = uploadFileService.insertFileUpload(uFile, formMap, this.getDbKey(request));
			out.println(JSON.Encode(formMap));
			return fileInfo;
		}catch(Exception e){
			log.error("业务版本包上传异常, 异常信息~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
			log.error("业务版本包上传异常, 异常信息: ", e);
//			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
			out.println(JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage())));
			return null;
		}finally {
			out.flush();
			out.close();
		}
	}
	
	/**
	 * 版本回退
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/backVersion",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateBackVersion(HttpServletRequest request) throws Exception {
		log.debug("收到用户的文件上传请求");  
		try{			
			uploadFileService.deleteOrBack(getParamsMap(request), this.getDbKey(request));
		}catch(Exception e){
			log.error("版本回退错误---->", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return "";
	}
	
	/**
	 * 查询远程目录的文件
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryRemoteFile",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String updateRemoteFiles(HttpServletRequest request) throws Exception {
		log.debug("收到查询远程文件请求");  
		//获取页面传递的其他信息
		Map<String,String> params = this.getParameterMap(request);
		// 上传文件并解压
		try{			
			List<FileRecord> files = uploadFileService.queryRemoteFiles(params);
			String result = JSON.Encode(files);
			log.info("返回数据：" + result);
			return result;
		}catch(Exception e){
			log.error("版本回退错误---->",e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 删除框架版本包
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/deletePlatformPackage",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String deletePlatformPackage(HttpServletRequest request) throws Exception {
		log.debug("删除组件程序包开始..."); 
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try{		
			returnMap=uploadFileService.deletePlatformPackage(this.getParamsMap(request), this.getDbKey(request));
		}catch(Exception e){
			log.error("删除组件程序包异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("删除组件程序包结束..."); 
		return JSON.Encode(returnMap);
	}
	
	/**
	 * 删除业务程序包
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/delete",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delete(HttpServletRequest request) throws Exception {
		log.debug("删除业务程序包开始...");  
		Map<String, Object> queryMap = new HashMap<String, Object>();
		try{			
			List<Map<String,String>> paramsList = getParamsList(request);
			queryMap.put("ids", paramsList);
			uploadFileService.deleteBusinessPackage(queryMap, this.getDbKey(request));
		}catch(Exception e){
			log.error("删除业务程序版本异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("删除业务程序包结束...");  
		return "";
	}
	
	/**
	 * 查看文件树
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/fileContent",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String fileContent(HttpServletRequest request) throws Exception {
		List resultList = null;
		try {
			resultList=uploadFileService.queryFileTree(getParamsMap(request));
		} catch (Exception e) {
			log.error("读取文件列表失败, 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		return JSON.Encode(resultList);
	}
	
}
