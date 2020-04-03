package com.tydic.util;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.ftp.FileTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@Component
public class SessionUtil {

	private static CoreService coreService;

	@Autowired
	public void setCoreService(CoreService coreService){
		SessionUtil.coreService = coreService;
	}

	/**
	 * 项目部署目录
	 * @param request
	 * @return
	 */
	public static String getWebRootPath(HttpServletRequest request){
		String webPath = request.getSession().getServletContext().getRealPath("/");
		return webPath;
	}
	public static FtpDto getFtpParams(){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("GROUP_CODE","WEB_FTP_CONFIG");
		List<Map<String,Object>>  returnList = coreService.queryForList3New("config.queryConfigList",queryMap,FrameConfigKey.DEFAULT_DATASOURCE);
		if(returnList != null && !returnList.isEmpty()){
			FtpDto ftpDto = new FtpDto();
			for(int i = 0 ; i < returnList.size();i++){
				if(FtpDto.FTP_USERNAME.equals(returnList.get(i).get("CONFIG_NAME"))){
					ftpDto.setUserName(StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
				}else if(FtpDto.FTP_PASSWD.equals(returnList.get(i).get("CONFIG_NAME"))){
					ftpDto.setPassword(StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
				}else if(FtpDto.FTP_IP.equals(returnList.get(i).get("CONFIG_NAME"))){
					ftpDto.setHostIp(StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
				}else if(FtpDto.FTP_ROOT_PATH.equals(returnList.get(i).get("CONFIG_NAME"))){
					ftpDto.setFtpRootPath(FileTool.exactPath(StringTool.object2String(returnList.get(i).get("CONFIG_VALUE"))));
				}else if(FtpDto.FTP_TYPE.equals(returnList.get(i).get("CONFIG_NAME"))){
					ftpDto.setFtpType(StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
				}
			}
			return ftpDto;
		}
		return null;
	}
	
	public static String getConfigValue(String key){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("CONFIG_NAME",key);
		Map<String,Object>  returnMap = coreService.queryForObject2New("config.queryConfigList",queryMap,FrameConfigKey.DEFAULT_DATASOURCE);
		if(returnMap != null && !returnMap.isEmpty()){
			return StringTool.object2String(returnMap.get("CONFIG_VALUE"));
		}
		return "";
	}
	public static String getConfigValue(String key,String groupCode){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("CONFIG_NAME",key);
		queryMap.put("GROUP_CODE",groupCode);
		Map<String,Object>  returnMap = coreService.queryForObject2New("config.queryConfigList",queryMap,FrameConfigKey.DEFAULT_DATASOURCE);
		if(returnMap != null && !returnMap.isEmpty()){
			return StringTool.object2String(returnMap.get("CONFIG_VALUE"));
		}
		return "";
	}

	public static Map<String,String> getConfigByGroupCode(String groupCode){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		Map<String,String> returnMap = new HashMap<String,String>();
		queryMap.put("GROUP_CODE",groupCode);
		List<Map<String,Object>>  returnList = coreService.queryForList3New("config.queryConfigList",queryMap,FrameConfigKey.DEFAULT_DATASOURCE);
		if(returnList != null && !returnList.isEmpty()){
			for(int i = 0 ; i < returnList.size();i++){
				returnMap.put(StringTool.object2String(returnList.get(i).get("CONFIG_NAME")),StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
			}
		}
		return returnMap;
	}

	public static List<Map<String,Object>> getConfigListByGroupCode(String groupCode){
		Map<String,Object> queryMap = new HashMap<String,Object>();
		queryMap.put("GROUP_CODE",groupCode);
		List<Map<String,Object>>  returnList = coreService.queryForList3New("config.queryConfigList",queryMap,FrameConfigKey.DEFAULT_DATASOURCE);
		return returnList;
	}
	public static Map<String,String> getConfigMap(){
		Map<String,String> returnMap = new HashMap<String,String>();
		List<Map<String,Object>>  returnList = coreService.queryForList3New("config.queryConfigList",null,FrameConfigKey.DEFAULT_DATASOURCE);
		if(returnList != null && !returnList.isEmpty()){
			for(int i = 0 ; i < returnList.size();i++){
				returnMap.put(StringTool.object2String(returnList.get(i).get("CONFIG_NAME")),StringTool.object2String(returnList.get(i).get("CONFIG_VALUE")));
			}
		}
		return returnMap;
	}


	/**
	 * 生成文件名
	 * @param request
	 * @return
	 */
	public static String getFileName(HttpServletRequest request){
		String localTmpPath = request.getSession().getServletContext().getRealPath("/");
		return  localTmpPath+"dccpweb/bp_" + System.currentTimeMillis()+".txt";
	}
	
	public static void main(String[] args) {
		String envPath = "$P/bussess/$V/ssss";
		 envPath = envPath.replace("$V", "V1.0.1").replace("$P", "/public/aa/").replace("//", "/");
		 System.out.println(envPath);
		 
	}
}
