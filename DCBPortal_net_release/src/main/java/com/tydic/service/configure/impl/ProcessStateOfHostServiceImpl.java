package com.tydic.service.configure.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.ProcessStateOfHostService;
import com.tydic.util.SSHRemoteCmdUtil;
import com.tydic.util.StringTool;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [ProcessStateOfHostServiceImpl]     
  * @Description:  [主机进程状态检查类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-14 上午8:55:41]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-14 上午8:55:41]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class ProcessStateOfHostServiceImpl implements ProcessStateOfHostService {
	/*
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(ProcessStateOfHostServiceImpl.class);
	@Resource
	private CoreService coreService;
	
	/**
     * 检查运行状态
     * @return
     */
	@Override
	public Map<String, Object> checkHostState(Map<String, Object> params, String dbKey) {
		log.debug("检查主机进程运行状态， 参数: " + params.toString() + ", dbKey: " + dbKey);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String pid = StringTool.object2String(params.get("PID"));
		//String program_code = StringTool.object2String(params.get("PROGRAM_CODE"));
		String program_name = StringTool.object2String(params.get("PROGRAM_NAME"));
		
		
		if(program_name.equals("sp_1") ||program_name.equals("sp_2")){
			program_name = "sp";
		}
		//查询主机信息
		HashMap<String, Object> hostInfo = coreService.queryForObject2New("host.queryHostList", params, FrameConfigKey.DEFAULT_DATASOURCE);
		
		//获取主机信息
		String host = StringTool.object2String(hostInfo.get("HOST_IP"));
		String user = StringTool.object2String(hostInfo.get("SSH_USER"));
		String password = DesTool.dec(StringTool.object2String(hostInfo.get("SSH_PASSWD")));
		SSHRemoteCmdUtil cmdUtil = new SSHRemoteCmdUtil(host,user,password,null);
		String shellCommand = "ps -ef|grep "+pid+"|grep "+program_name+"|grep -v grep";
		log.debug("检查主机状态, 执行命令; " + shellCommand + ", 主机IP: " + host);
		
		String result = cmdUtil.exec(shellCommand);
		log.debug("检查主机状态, 执行命令结果: " + result);
		
		String[] resultArray = result.split("\n");
		String processInfo="";
		if(result.length()>14){
			String info=resultArray[1];
			if(resultArray[1].endsWith(shellCommand)){
				 info=resultArray[2];
			}
			processInfo = info.substring(0, 15);
			resultMap.put("processInfo", processInfo.trim().replaceAll("[\\s]+", ","));
		}
		Pattern.compile("/w+/s+/d+");
		resultMap.put("active", Pattern.matches("[\\w]+[\\s]+[\\d]+", processInfo.trim()));
		
		return resultMap;
	}
	

}
