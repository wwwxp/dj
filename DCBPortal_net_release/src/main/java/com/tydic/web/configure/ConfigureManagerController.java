package com.tydic.web.configure;

import PluSoft.Utils.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.ConfigureManagerService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.zk.ZkClientUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.configure]    
  * @ClassName:    [ConfigureManagerController]     
  * @Description:  [平台&组件配置文件操作]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-16 下午2:48:11]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-16 下午2:48:11]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping("/configure")
public class ConfigureManagerController extends BaseController{
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ConfigureManagerController.class);
	
	/**
	 * 配置文件操作Service对象
	 */
	@Autowired
	private ConfigureManagerService configureManagerService;
	
	//平台或者业务
	private static final String PLATFORM = "PLATFORM";
	private static final String SERVICE = "SERVICE";
	
	/**
	 * 从ftp服务器上查询文件目录树数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadFileTree", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String loadFileTree(HttpServletRequest request) {
		log.debug("获取配置文件列表开始...");
		List resultList = null;
		try {
			Map<String,String> params = getParamsMap(request);
			//获取当前根目录
			String rootPath = SessionUtil.getWebRootPath(request);
			//获取用户信息
			Map<String, Object> userMap = (Map<String, Object>)request.getSession().getAttribute("userMap");

			if (userMap != null && !userMap.isEmpty()){
				String empeeId = StringTool.object2String(userMap.get("EMPEE_ID"));
				params.put("projectRootPath",rootPath);
				params.put("EMPEE_ID", empeeId);
				resultList = configureManagerService.loadFileTree(params, this.getDbKey(request));
			}else{
				throw new Exception("用户登录异常，请检查！");
			}
		} catch (Exception e) {
			log.error("获取配置文件列表失败, 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("获取配置文件列表结束...");
		return JSON.Encode(resultList);
	}
	
	/**
	 * 从ftp服务器上查询文件目录树数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadScriptTree", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String loadScriptTree(HttpServletRequest request) {
		log.debug("获取配置文件列表开始...");
		List resultList = null;
		try {
			Map<String,String> params = getParamsMap(request);
			//获取当前根目录
			String rootPath = SessionUtil.getWebRootPath(request);
			params.put("projectRootPath",rootPath);
			resultList = configureManagerService.loadScriptTree(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("获取配置文件列表失败, 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("获取配置文件列表结束...");
		return JSON.Encode(resultList);
	}
	
	
	/**
	 * 获取部署主机配置文件内容
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getFileContent", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String getFileContent(HttpServletRequest request) {
		log.debug("获取部署主机配置文件内容开始...");
		Map<String, String> fileContent;
		try {
			Map<String,String> param = getParamsMap(request);
			param.put("webRootPath", SessionUtil.getWebRootPath(request));
			fileContent=configureManagerService.getFileContent(param,this.getDbKey(request));
		} catch (Exception e) {
			log.error("获取部署主机配置文件内容异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("获取部署主机配置文件内容结束...");
		return JSON.Encode(fileContent);
	}
	
	/**
	 * 获取版本发布服务器业务配置文件(该方法和组件配置文件获取分区是因为需要获取文件远程路径)
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getFileBusContent", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String getFileBusContent(HttpServletRequest request) {
		log.debug("获取部署主机业务配置文件内容开始...");
		Map<String, String> fileContent;
		try {
			Map<String,String> param = getParamsMap(request);
			param.put("webRootPath", SessionUtil.getWebRootPath(request));
			fileContent=configureManagerService.getFileBusContent(param,this.getDbKey(request));
		} catch (Exception e) {
			log.error("获取部署主机业务配置文件内容异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("获取部署主机业务配置文件内容结束...");
		return JSON.Encode(fileContent);
	}
	
	/**
	 * 获取远程主机配置文件内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/showConfigContent", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String showConfigContentByHost(HttpServletRequest request) {
		log.debug("获取远程主机配置文件内容开始...");
		Map<String, String> fileContent;
		try {
			Map<String,String> param = getParamsMap(request);
			param.put("webRootPath", SessionUtil.getWebRootPath(request));
			fileContent=configureManagerService.showConfigContentByHost(param,this.getDbKey(request));
		} catch (Exception e) {
			log.error("获取远程主机配置文件内容异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("获取远程主机配置文件内容结束...");
		return JSON.Encode(fileContent);
	}
	
//	/**
//	 * 业务：保存/分发文件
//	 * 
//	 * @param request
//	 * @return
//	 */
//	@RequestMapping(value = "/saveFileContents", produces = { "application/json;charset=UTF-8" })
//	@ResponseBody
//	public String updateSaveFileContents(HttpServletRequest request) {
//		log.debug("业务配置文件修改保存开始...");
//		Map<String, Object> isSuccess;
//		try {
//			Map<String,Object> param = getParamsMapByObject(request);
//			isSuccess = configureManagerService.updateSaveFileContents(param,this.getDbKey(request));
//		} catch (Exception e) {
//			log.error("业务配置文件修改保存异常， 异常信息: ", e);
//			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
//		}
//		log.debug("业务配置文件修改保存结束...");
//		return JSON.Encode(isSuccess);
//	}
	
	/**
	 * 业务：保存/分发文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveBusFileContents", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateSaveBusFileContents(HttpServletRequest request) {
		log.debug("业务配置文件修改开始...");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> param = getParamsMapByObject(request);
			Map<String,Object> userMap = (Map<String,Object>)request.getSession().getAttribute("userMap");
			if(userMap !=null && !userMap.isEmpty()){
				param.put("EMPEE_ID", userMap.get("EMPEE_ID"));
				isSuccess = configureManagerService.updateSaveBusFileContents(param,this.getDbKey(request));
			}else{
				throw new Exception("用户登录异常，请检查！");
			}
		} catch (Exception e) {
			log.error("业务配置文件修改异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("业务配置文件修改结束...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 平台：保存/分发修改后的文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/distributeFileContent", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateDistributeFileContent(HttpServletRequest request) {
		log.debug("组件配置文件修改保存， 分发开始...");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> param = getParamsMapByObject(request);
			isSuccess=configureManagerService.updateDistributeFileContent(param, this.getDbKey(request));
		} catch (Exception e) {
			log.error("组件配置文件修改保存失败, 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("组件配置文件修改保存， 分发结束...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 组件/业务脚本保存推送
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/updateSaveScript", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateSaveScript(HttpServletRequest request) {
		log.debug("组件/业务脚本文件修改开始...");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> param = getParamsMapByObject(request);
			isSuccess = configureManagerService.updateSaveScript(param,this.getDbKey(request));
		} catch (Exception e) {
			log.error("组件/业务脚本文件修改异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("组件/业务脚本文件修改结束...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 新建文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/createFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String insertCreateFile(HttpServletRequest request) {
		log.debug("新建配置文件开始...");
		Map<String, Object> isSuccess = null;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	String page_type = StringTool.object2String(params.get("page_type"));
        	
        	if(PLATFORM.equals(page_type)){//平台
        		isSuccess=configureManagerService.updateCreateFile(params,this.getDbKey(request));
        	}else if(SERVICE.equals(page_type)){//业务
        		isSuccess=configureManagerService.updateCreateServiceFile(params);
        	}
		} catch (Exception e) {
			log.error("新建配置文件异常，异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("新建配置文件结束...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 新建文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/batchBussFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String insertBussmFile(HttpServletRequest request) {
		log.debug("开始...");
		Map<String, Object> isSuccess = null;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	
        	isSuccess=configureManagerService.updateBatchBussFile(params);
        	 
		} catch (Exception e) {
			log.error("新建配置文件异常，异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("新建配置文件结束...");
		return JSON.Encode(isSuccess);
	}
	
	
	/**
	 * 重命名文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/renameFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateRenameFile(HttpServletRequest request) {
		log.debug("重命名配置文件开始...");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			isSuccess=configureManagerService.updateRenameFile(params,this.getDbKey(request));
		} catch (Exception e) {
			log.error("重命名配置文件异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("重命名配置文件结束...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 删除文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteFile(HttpServletRequest request) {
		log.debug("ConfigureManagerController删除文件/文件夹开始...");
		Map<String, Object> rstMap = null;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
        	String page_type=String.valueOf(params.get("page_type"));
        	if(PLATFORM.equals(page_type)) {//平台
        		rstMap = configureManagerService.updateDeleteFile(params,this.getDbKey(request));
        	}else if(SERVICE.equals(page_type)) {//业务
        		String flag=String.valueOf(params.get("flag"));
        		if("batch".equals(flag)){
        			List<Map<String,Object>> list = (List<Map<String,Object>>)params.get("list");
        			for(int i = 0 ; i < list.size() ;i++){
        				rstMap = configureManagerService.updateDeleteServiceFile(list.get(i));
        			}
        		}else{
        			rstMap = configureManagerService.updateDeleteServiceFile(params);
        		}
        		
        	}
		} catch (Exception e) {
			log.error("ConfigureManagerController删除文件/文件夹失败，原因－－－>", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("ConfigureManagerController删除文件/文件夹成功...");
		return JSON.Encode(rstMap);
	}


	/**
	 * 删除Redis批量目录
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteBatchRedisFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteBatchRedisFile(HttpServletRequest request) {
		log.debug("ConfigureManagerController删除Redis实例目录开始...");
		Map<String, Object> rstMap = null;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			rstMap = configureManagerService.deleteBatchRedisFile(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("ConfigureManagerController删除Redis实例目录失败，原因－－－>", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("ConfigureManagerController删除Redis实例目录成功...");
		return JSON.Encode(rstMap);
	}

	/**
	 * 删除sentinel目录下的所有实例
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteSentinelInstance", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteSentinelInstance(HttpServletRequest request) {
		log.debug("ConfigureManagerController删除sentinel目录下实例开始...");
		Map<String, Object> rstMap = null;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			rstMap = configureManagerService.deleteSentinelInstance(params);
		} catch (Exception e) {
			log.error("ConfigureManagerController删除sentinel目录下实例失败，原因－－－>", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("ConfigureManagerController删除sentinel目录下实例成功...");
		return JSON.Encode(rstMap);
	}

	/**
	 * 查找default下文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadFileListUnderDefault", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String loadFileListUnderDefault(HttpServletRequest request) {
		log.debug("查询默认目录下文件列表开始...");
		List fileList;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			
			//选中节点目录
			//String filePath = StringTool.object2String(params.get("filePath"));
			//选中节点名称(实际上就是一个目录节点名称)
			String fileName = StringTool.object2String(params.get("fileName"));
			//构建一个完整的文件名称
			if(StringUtils.isNotBlank(fileName)){
				params.put("fileName", FileTool.exactPath(fileName) + Constant.DEFAULT);
			}else{
				params.put("fileName",  Constant.DEFAULT);
			}
				
			
			
			//获取节点下default目录文件列表
			//String defaultPath = FileTool.exactPath(filePath) + FileTool.exactPath(fileName) + Constant.DEFAULT;
        	fileList=configureManagerService.loadFilesUnderGivenPath(params,this.getDbKey(request));
		} catch (Exception e) {
			log.error("查询默认目录下文件列表异常, 异常原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查询默认目录下文件列表结束...");
		return JSON.Encode(fileList);
	}
	
	/**
	 * 查找指定目录下所有文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadFileListByFolder", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String loadFileListByFolder(HttpServletRequest request) {
		log.debug("查找远程主机目录节点文件列表开始...");
		List fileList;
		try {
        	fileList=configureManagerService.loadFileListByFolder(getParamsMapByObject(request), this.getDbKey(request));
		} catch (Exception e) {
			log.error("查找远程主机目录节点文件列表异常, 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查找远程主机目录节点文件列表结束...");
		return JSON.Encode(fileList);
	}
	
	
	/**
	 * 查找指定目录下节点文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadFilesUnderGivenPath", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String loadFilesUnderGivenPath(HttpServletRequest request) {
		log.debug("查找当前目录节点文件列表开始...");
		List fileList;
		try {
        	fileList = configureManagerService.loadFilesUnderGivenPath(getParamsMapByObject(request), this.getDbKey(request));
		} catch (Exception e) {
			log.error("查找当前目录节点文件列表失败, 失败原因: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查找当前目录节点文件列表结束...");
		return JSON.Encode(fileList);
	}
	
	
	
	/**
	 * 新建实例（复制default下文件）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/createAndCopyFolder", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String createAndCopyFile(HttpServletRequest request) {
		log.debug("创建实例开始.");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
			isSuccess=configureManagerService.addCreateAndCopyFolder(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("创建实例异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("创建实例结束.");
		return JSON.Encode(isSuccess);
	}

	/**
	 * 新建实例（redis）
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/foundAndCopyFolder", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String foundAndCopyFile(HttpServletRequest request) {
		log.debug("开始创建实例...");
		Map<String, Object> isSuccess;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
			params.put("webRootPath", SessionUtil.getWebRootPath(request));
			isSuccess=configureManagerService.addCreateAndCopyFolder(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("创建实例出现异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("结束创建实例...");
		return JSON.Encode(isSuccess);
	}
	
	/**
	 * 批量新建实例（复制default下文件）
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addBatchFileAndFolder", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addBatchFileAndFolder(HttpServletRequest request) {
		log.debug("批量创建实例开始.....");
		Map<String, Object> rstMap;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
        	params.put("webRootPath", SessionUtil.getWebRootPath(request));
        	rstMap = configureManagerService.addBatchFileAndFolder(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("批量创建实例异常，异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("批量创建实例结束...");
		return JSON.Encode(rstMap);
	}

	/**
	 * 批量新建redis实例（复制default下文件）
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/createBatchRedisFileAndFolder", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String createBatchRedisFileAndFolder(HttpServletRequest request) {
		log.debug("批量创建redis实例开始.");
		Map<String, Object> rstMap;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			// 获取远程主机根目录
			params.put("webRootPath", SessionUtil.getWebRootPath(request));
			rstMap = configureManagerService.createBatchRedisFileAndFolder(params, this.getDbKey(request));
		} catch (Exception e) {
			log.error("批量创建实例发生异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("批量创建实例结束.");
		return JSON.Encode(rstMap);
	}

	/**
	 * 新增文件
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/addFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String addFile(HttpServletRequest request) {
		log.debug("配置文件管理, 新增文件或者目录开始...");
		Map<String, String> rstMap;
		try {
			Map<String,Object> params = getParamsMapByObject(request);
			rstMap = configureManagerService.addFile(params);
		} catch (Exception e) {
			log.error("配置文件管理, 新增文件或者目录异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("配置文件管理, 新增文件或者目录结束...");
		return JSON.Encode(rstMap);
	}



	/**
	 * 从zk获取redis节点信息
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getRedisNodes", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String getRedisNodes(HttpServletRequest request) {
		Map<String,String> paramMap = this.getParamsMap(request);
		log.debug("从zk获取redis节点信息开始...");
		Map<String, Object> resultMap = new HashMap<>();
		CuratorFramework client = null;
		try {
			//String clusterCode = "xiaods001";
			String clusterCode = paramMap.get("clusterCode");
			//加载配置文件
			 client = ZkClientUtil.createZkClient(clusterCode);
			client.start();
			byte [] byty = client.getData().forPath("/"+clusterCode+"/RedisCfg");
			String msg = new String(byty);
			log.debug("zk信息内容："+msg);
			Map<String,String> ipMap = new HashMap<>();
			Map<String,String> portMap = new HashMap<>();
			JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(msg);
			Iterator<Map.Entry<String, Object>> entries = jsonObject.entrySet().iterator();
			while(entries.hasNext()){
				Map.Entry<String, Object> entry = entries.next();
				String key = entry.getKey();
				if("Action".equals(key)){
					continue;
				}
				List<Map<String,Object>> ObjList = (List<Map<String,Object>>)entry.getValue();
				if(ObjList != null || ObjList.size() > 0){
					for(Map<String,Object> map : ObjList){

						String ip = ObjectUtils.toString(map.get("ip"));
						String port = ObjectUtils.toString(map.get("port"));
						if(StringUtils.isNotBlank(ip) && StringUtils.isNotBlank(port)){
							ipMap.put(ip,"ip");
							portMap.put(port+"#"+ip,"ip");
						}

						List<Map<String,Object>> slaveList = (List<Map<String,Object>>)map.get("slaves");
						for(Map<String,Object> slaveMap : slaveList){

							String slaveIp = ObjectUtils.toString(slaveMap.get("ip"));
							String slavePort = ObjectUtils.toString(slaveMap.get("port"));
							if(StringUtils.isNotBlank(slaveIp) && StringUtils.isNotBlank(slavePort)) {
								ipMap.put(slaveIp, "ip");
								portMap.put(slavePort + "#" + slaveIp, "ip");
							}

						}
					}
				}
			}

			List<String> ipList = new ArrayList<>();
			Iterator<Map.Entry<String, String>> ipentries = ipMap.entrySet().iterator();
			while (ipentries.hasNext()) {
				Map.Entry<String, String> entry = ipentries.next();
				ipList.add(entry.getKey());
			}

			Map<String,List<Map<String,String>>> portEMap = new HashMap<>();
			Iterator<Map.Entry<String, String>> portentries = portMap.entrySet().iterator();
			while (portentries.hasNext()) {
				Map.Entry<String, String> entry = portentries.next();
				String [] key = entry.getKey().split("#");
				List<Map<String,String>> list;
				if(portEMap.containsKey(key[1])){
					list = portEMap.get(key[1]);
				} else{
					list =  new ArrayList<>();
					portEMap.put(key[1],list);
				}
				Map<String,String> tmpMap = new HashMap<>();
				tmpMap.put("text",key[0]);
				list.add(tmpMap);
			}

			Collections.sort(ipList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			Iterator<Map.Entry<String, List<Map<String,String>>>> portentries1 = portEMap.entrySet().iterator();
			while (portentries1.hasNext()) {
				Map.Entry<String, List<Map<String,String>>> entry = portentries1.next();
				Collections.sort(portEMap.get(entry.getKey()), new Comparator<Map<String,String>>() {
					@Override
					public int compare(Map<String,String> o1, Map<String,String> o2) {
						return o1.get("text").compareTo(o2.get("text"));
					}
				});

			}
			resultMap.put("ipList",ipList);
			resultMap.put("portsMap",portEMap);
		} catch (Exception e) {
			log.error("zk获取redis 节点信息失败， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}finally {
			if(client !=null) {
				client.close();
			}
		}
		log.debug("zk获取redis 节点信息结束...");
		return JSON.Encode(resultMap);
	}
}
