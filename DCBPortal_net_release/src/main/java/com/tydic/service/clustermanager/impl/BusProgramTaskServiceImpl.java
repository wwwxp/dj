package com.tydic.service.clustermanager.impl;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.clustermanager.BusProgramTaskService;
import com.tydic.util.*;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;
import com.tydic.util.log.LoggerUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple to Introduction
 *
 * @ProjectName: [DCBPortal_net_release]
 * @Package: [com.tydic.service.clustermanager.impl]
 * @ClassName: [BusProgramTaskServiceImpl]
 * @Description: [组件集群配置管理]
 * @Author: [Yuanh]
 * @CreateDate: [2017-6-15 下午4:10:46]
 * @UpdateUser: [Yuanh]
 * @UpdateDate: [2017-6-15 下午4:10:46]
 * @UpdateRemark: [说明本次修改内容]
 * @Version: [v1.0]
 */
@Service
@SuppressWarnings("all")
public class BusProgramTaskServiceImpl implements BusProgramTaskService {
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(BusProgramTaskServiceImpl.class);

    @Resource
    public CoreService coreService;

    //配置文件来源， local:部署主机  remote:远程主机
    public static final Boolean CONFIG_FROM_LOCAL = Boolean.TRUE;


    /**
     * 业务程序状态管理-获取业务程序状态检查中用到的配置文件
     *
     * @param params
     * @param dbKey
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getBusTargetConfigList(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("获取业务程序配置文件列表信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //集群ID
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //集群类型
        String clusterType = StringTool.object2String(params.get("PROGRAM_TYPE"));
        String webRootPath = StringTool.object2String(params.get("webRootPath"));
        String fileName = StringTool.object2String(params.get("CONFIG_FILE"));
        //获取当前启停业务集群信息
        Map<String, Object> queryClusterMap = new HashMap<String, Object>();
        queryClusterMap.put("CLUSTER_ID", clusterId);
        queryClusterMap.put("CLUSTER_TYPE", clusterType);
        queryClusterMap.put("HOST_ID", params.get("HOST_ID"));
        Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
        if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
            throw new RuntimeException("该实例没有对应的业务集群, 请检查！");
        }
        //组件集群编码
        String clusterCode = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_CODE")));
        //是否根据主机IP拆分配置文件
        String personConfig = StringTool.object2String(clusterMap.get("PERSONAL_CONF"));
        //集群部署路径
        String deployPath = StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH"));
        //获取业务主集群信息
        String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
        //获取当前启停业务集群信息
        Map<String, Object> queryBusClusterMap = new HashMap<String, Object>();
        queryBusClusterMap.put("BUS_CLUSTER_ID", busClusterId);
        Map<String, Object> busClusterMap = coreService.queryForObject2New("busMainCluster.queryBusMainClusterList", queryBusClusterMap, dbKey);
        if (BlankUtil.isBlank(busClusterMap)) {
            throw new RuntimeException("业务主集群信息查询失败, 请检查！");
        }
        Map<String, Object> rstMap;
		try {
			//获取业务主集群编码
			String busClusterCode = StringTool.object2String(busClusterMap.get("BUS_CLUSTER_CODE"));
			//当前程序版本, 如:18.0.0.1
			String version = StringTool.object2String(params.get("VERSION"));

			//当前程序版本
			String hostIp = StringTool.object2String(params.get("HOST_IP"));
			log.debug("当前集群ID: " + clusterId + ", 是否区分IP: " + personConfig + ", 配置文件来自: " + (CONFIG_FROM_LOCAL ? "部署主机" : "远程主机"));

			//提供重部署主机读取配置文件列表和重远程主机读取配置文件列表
			FtpDto ftpDto = SessionUtil.getFtpParams();

			//包类型
			String packageType = StringTool.object2String(params.get("PACKAGE_TYPE"));
			//当前程序所属包名,如:DIC-BIL-DUCC-SH_V18.0.0.1
			String packageName = StringTool.object2String(params.get("NAME"));

			//部署主机配置文件路径
			String businessCfgPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + FileTool.exactPath(packageType);

			Trans src = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
			src.login();
			log.debug("登录部署主机成功...");
			String configPath = "";
			if (BusinessConstant.PARAMS_BUS_1.equals(personConfig)) {
			    configPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessCfgPath) + FileTool.exactPath(packageName)
			            + FileTool.exactPath(busClusterCode) + FileTool.exactPath(hostIp) + FileTool.exactPath(clusterType);
			} else {
			    configPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessCfgPath) + FileTool.exactPath(packageName)
			            + FileTool.exactPath(busClusterCode) + FileTool.exactPath(clusterType);
			}
			String localPath = webRootPath + Constant.TMP + System.currentTimeMillis();//flag
			
			String sourceFile = configPath + fileName;
			String localFile = FileTool.exactPath(localPath) + fileName;
			log.debug("获取版本发布服务配置文件， 版本发布服务器文件: " + sourceFile  + ", 本地临时文件: " + localFile);
			src.get(sourceFile, localFile);
			// 将文件转成字符串
			String fileContent = FileUtil.readFileUnicode(localFile);
	//		// 去掉尾部所有空格
	//		int len = fileContent.length();
	//		int st = 0;
	//		char[] val = fileContent.toCharArray();
	//		while ((st < len) && (val[len - 1] <= ' ')) {
	//			len--;
	//		}
	//		fileContent = (len < fileContent.length()) ? fileContent.substring(st, len) : fileContent;
			log.debug("获取" + configPath + "文件内容成功！");
			rstMap = new HashMap<String, Object>();
			rstMap.put("fileContent", fileContent);
			
			//删除本地临时文件
			new File(localFile).delete();
		} catch (Exception e) {
			log.error("获取业务实例配置文件失败， 失败原因：", e);
			throw new RuntimeException("获取业务实例配置文件失败， 失败原因:" + e.getMessage());
		}
        return rstMap;
    }


    /**
     * 获取业务程序配置文件列表
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> getBusConfigList(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("获取业务程序配置文件列表信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //集群ID
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //集群类型
        String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
        String fileFlag = StringTool.object2String(params.get("fileFlag"));
        
        //本地网
        String latnId = StringTool.object2String(params.get("LATN_ID"));
        //获取当前启停业务集群信息
        Map<String, Object> queryClusterMap = new HashMap<String, Object>();
        queryClusterMap.put("CLUSTER_ID", clusterId);
        queryClusterMap.put("CLUSTER_TYPE", clusterType);
        Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryClusterMap, dbKey);
        if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
            throw new RuntimeException("业务集群信息查询失败, 请检查！");
        }
        //组件集群编码
        String clusterCode = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_CODE")));
        //是否根据主机IP拆分配置文件
        String personConfig = StringTool.object2String(clusterMap.get("PERSONAL_CONF"));
        //集群部署路径
        String deployPath = StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH"));

        //获取业务主集群信息
        String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
        //获取当前启停业务集群信息
        Map<String, Object> queryBusClusterMap = new HashMap<String, Object>();
        queryBusClusterMap.put("BUS_CLUSTER_ID", busClusterId);
        Map<String, Object> busClusterMap = coreService.queryForObject2New("busMainCluster.queryBusMainClusterList", queryBusClusterMap, dbKey);
        if (BlankUtil.isBlank(busClusterMap)) {
            throw new RuntimeException("业务主集群信息查询失败, 请检查！");
        }
        String configPath = "";
        //获取业务主集群编码
        String busClusterCode = StringTool.object2String(busClusterMap.get("BUS_CLUSTER_CODE"));
        //当前程序版本, 如:18.0.0.1
        String version = StringTool.object2String(params.get("VERSION"));
        //当前程序所属包名,如:DIC-BIL-DUCC-SH_V18.0.0.1
        String packageName = StringTool.object2String(params.get("NAME"));
        //当前程序版本
        String hostIp = StringTool.object2String(params.get("HOST_IP"));
        log.debug("当前集群ID: " + clusterId + ", 是否区分IP: " + personConfig + ", 配置文件来自: " + (CONFIG_FROM_LOCAL ? "部署主机" : "远程主机"));

        Vector<FileRecord> fileList = new Vector<FileRecord>();

        //提供重部署主机读取配置文件列表和重远程主机读取配置文件列表
        if (CONFIG_FROM_LOCAL) {
            //获取部署主机配置文件列表
			FtpDto ftpDto = SessionUtil.getFtpParams();
            //包类型
            String packageType = StringTool.object2String(params.get("PACKAGE_TYPE"));
            //部署主机配置文件路径
            String businessCfgPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + FileTool.exactPath(packageType);



            Trans src = FTPUtils.getFtpInstance(ftpDto.getHostIp(), ftpDto.getUserName(), ftpDto.getPassword(), ftpDto.getFtpType());
            src.login();
            log.debug("登录部署主机成功...");

           
            if (BusinessConstant.PARAMS_BUS_1.equals(personConfig)) {
                configPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessCfgPath) + FileTool.exactPath(packageName)
                        + FileTool.exactPath(busClusterCode) + FileTool.exactPath(hostIp) + FileTool.exactPath(clusterType);
                queryClusterMap.put("HOST_ID", params.get("HOST_ID"));
            } else {
                configPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessCfgPath) + FileTool.exactPath(packageName)
                        + FileTool.exactPath(busClusterCode) + FileTool.exactPath(clusterType);
            }
            //获取本地网下面的配置文件
            if(StringUtils.isNotBlank(latnId)){
            	configPath += FileTool.exactPath(latnId);
            }
            //获取当前主机信息
            boolean isExists = src.isExistPath(configPath);
            log.debug("判断部署主机配置文件目录是否存在， 目录: " + configPath + ", 返回结果:" + isExists);
            if (isExists) {
            	//获取所有文件，包括子目录文件
            	if(StringUtils.equals(fileFlag, "ALL")){
            		 src.getAllFileList(fileList, configPath, "-1");
            	}else{
            		fileList = src.getFileList(configPath);
            	}
            	
                log.debug("部署主机配置文件路径: " + configPath + ", 配置文件数量: " + (fileList == null ? 0 : fileList.size()));
            }
        } else {
            //获取远程主机配置文件信息
            if (BusinessConstant.PARAMS_BUS_1.equals(personConfig)) {
                queryClusterMap.put("HOST_ID", params.get("HOST_ID"));
                //根据主机ID查询主机信息
                Map<String, Object> tempMap = coreService.queryForObject2New("host.queryHostList", queryClusterMap, dbKey);
                String sshUser = StringTool.object2String(tempMap.get("SSH_USER"));
                String sshPwd = DesTool.dec(StringTool.object2String(tempMap.get("SSH_PASSWD")));
                Trans src = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
                src.login();
                //远程主机配置文件路径
                 configPath = FileTool.exactPath(deployPath) + FileTool.exactPath(Constant.BUSS)
                        + FileTool.exactPath("V" + version) + Constant.CFG;
                log.debug("获取远程主机配置文件列表, 主机IP: " + hostIp + ", 配置文件路径: " + configPath + ", 是否需要根据IP查找: " + personConfig);
                //获取当前主机信息
                boolean isExists = src.isExistPath(configPath);
                log.debug("判断部署主机配置文件目录是否存在， 目录: " + configPath + ", 返回结果:" + isExists);
                if (isExists) {
                	//获取所有文件，包括子目录文件
                	if(StringUtils.equals(fileFlag, "ALL")){
                		 src.getAllFileList(fileList, configPath, "-1");
                	}else{
                		fileList = src.getFileList(configPath);
                	}
                }
            } else {
                //获取当前集群主机列表
                Map<String, Object> qryParams = new HashMap<String, Object>();
                qryParams.put("CLUSTER_ID", clusterId);
                qryParams.put("CLUSTER_TYPE", clusterType);
                List<HashMap<String, Object>> hostList = coreService.queryForList2New("deployHome.queryHostByDeploy", qryParams, dbKey);
                if (!BlankUtil.isBlank(hostList)) {
                    for (int i = 0; i < hostList.size(); i++) {
                        Map<String, Object> hostMap = hostList.get(i);
                        String sshHostIp = StringTool.object2String(hostMap.get("HOST_IP"));
                        String sshUser = StringTool.object2String(hostMap.get("SSH_USER"));
                        String sshPwd = DesTool.dec(StringTool.object2String(hostMap.get("SSH_PASSWD")));
                        Trans src = FTPUtils.getFtpInstance(sshHostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
                        src.login();
                        //远程主机配置文件路径
                         configPath = FileTool.exactPath(deployPath) + FileTool.exactPath(Constant.BUSS)
                                + FileTool.exactPath("V" + version) + Constant.CFG;
                        log.debug("获取远程主机配置文件列表, 主机IP: " + hostIp + ", 配置文件路径: " + configPath);
                        //获取当前主机信息
                        boolean isExists = src.isExistPath(configPath);
                        log.debug("判断部署主机配置文件目录是否存在， 目录: " + configPath + ", 返回结果:" + isExists);
                        if (isExists) {
                        	//获取所有文件，包括子目录文件
                        	if(StringUtils.equals(fileFlag, "ALL")){
                        		 src.getAllFileList(fileList, configPath, "-1");
                        	}else{
                        		fileList = src.getFileList(configPath);
                        	}
                            break;
                        }
                    }
                }
            }
        }
        log.debug("获取配置文件列表成功， 配置文件数量: " + (fileList == null ? 0 : fileList.size()));

        if (!BlankUtil.isBlank(fileList)) {
        	//查询当前集群配置文件使用情况
            String[] files = null;
            HashMap<String, Object> configMap = coreService.queryForObject2New("taskProgram.queryProgramConfigList", queryClusterMap, dbKey);
            if (configMap != null && !configMap.isEmpty()) {
                String configFiles = StringTool.object2String(configMap.get("CONFIG_FILE_LIST"));
                if (!BlankUtil.isBlank(configFiles) && configFiles.indexOf(",") != -1) {
                    files = configFiles.split(",");
                } else {
                    files = new String[]{configFiles};
                }
            }
            
        	String diffIpConfig = StringTool.object2String(configMap.get("CLUSTER_ELE_PERSONAL_CONF"));
        	String runJstormConfig = StringTool.object2String(configMap.get("CLUSTER_ELE_RUN_JSTORM"));
            for (int i = 0; i < fileList.size(); i++) {
                FileRecord record = fileList.get(i);
                record.setFileNameExt(record.getFileName() + "（未使用）");
                //区分IP或者运行在Jstorm中的配置文件不能复用
                if (!BlankUtil.isBlank(files) && (BusinessConstant.PARAMS_BUS_1.equals(diffIpConfig) || BusinessConstant.PARAMS_BUS_1.equals(runJstormConfig))) {
	                for (int j = 0; j < files.length; j++) {
	                    if (record.getFileName().equals(files[j])) {
	                        record.setIsUsed(true);
	                        fileList.remove(record);
	                        i--;
	                        break;
	                    }
	                }
                } else if (!BlankUtil.isBlank(files)) {   //不区分IP的业务配置文件可以复用
                	for (int j = 0; j < files.length; j++) {
	                    if (record.getFileName().equals(files[j])) {
	                        record.setIsUsed(true);
	                        record.setFileNameExt(record.getFileName() + "（已使用）");
	                        break;
	                    }   
	                }
                }
            }
        }
        
      //映射目录中文名称
		if(CollectionUtils.isNotEmpty(fileList)){
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("GROUP_CODE","LATN_LIST");
			List<HashMap<String,Object>> latnList = coreService.queryForList2New("config.queryConfigList", queryMap, FrameConfigKey.DEFAULT_DATASOURCE);

			for(int i=0;i<fileList.size();++i){
				FileRecord file = fileList.get(i);
				
				if(file.isDirectory()){
					String fileName = file.getFileName();
					
					 
					for(int j=0;j<latnList.size();++j){
						Map<String,Object> map = latnList.get(j);
						
						String NAME = ObjectUtils.toString(map.get("CONFIG_NAME"));
						String CODE = ObjectUtils.toString(map.get("CONFIG_VALUE"));
						
						if(StringUtils.equals(fileName, CODE)){
							file.setFileNameExt(NAME + "(" + CODE + ")");
							break;
						}
					}
				}
			}
		}

        Map<String, Object> rstMap = new HashMap<String, Object>();
        rstMap.put("FILES_LIST", fileList);
        rstMap.put("configFilePath", configPath);
        return rstMap;
    }

    /**
     * 获取可启动程序
     *
     * @param params
     * @param dbKey
     * @return
     */
    public Map<String, Object> getBusProgramListWithHost(Map<String, Object> params, String dbKey) {
        log.debug("获取可启动程序， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //查询业务实例运行状态
        String programState = StringTool.object2String(params.get("QUERY_PROGRAM_STATE"));
        //集群ID
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //主机ID
        String hostId = StringTool.object2String(params.get("HOST_ID"));
        //当前版本
        String taskId = StringTool.object2String(params.get("TASK_ID"));
        //返回的程序列表
        List<Map<String, Object>> rstList = new ArrayList<Map<String, Object>>();
        String hostIds = StringTool.object2String(params.get("QUERY_HOST_IDS"));
        String latnIds = StringTool.object2String(params.get("QUERY_LATN_IDS"));
        if(!BlankUtil.isBlank(hostIds)){
        	if(hostIds.split(",")[0].length() <=2){
        		params.remove("QUERY_HOST_IDS");
        	}
        }
        if(!BlankUtil.isBlank(latnIds)){
        	if(latnIds.split(",")[0].length() <=2){
        		params.remove("QUERY_LATN_IDS");
        	}
        }
        //查询当前主机所有程序
        List<HashMap<String, Object>> programList = coreService.queryForList2New("taskProgram.queryBusProgramListForStart", params, dbKey);
        if (!BlankUtil.isBlank(programList)) {
            //获取当前版本存在任务(当前版本已经存在的业务实例)
            List<Map<String, Object>> selfData = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < programList.size(); i++) {
                Map<String, Object> programMap = programList.get(i);
                if (taskId.equals(StringTool.object2String(programMap.get("TASK_ID")))) {
                    programMap.put("ID", programMap.get("ID"));
                    programMap.put("REAL_ID", programMap.get("ID"));
                    selfData.add(programMap);
                    programList.remove(programMap);
                    i--;
                }
            }
            //当前版本程序有自己的程序实例
            List<Map<String, Object>> otherData = new ArrayList<Map<String, Object>>();
            for (int j = 0; j < programList.size(); j++) {
                Map<String, Object> programMap = programList.get(j);
                //程序编码
                String programCode = StringTool.object2String(programMap.get("PROGRAM_CODE"));
                //程序所属版本
                String programTaskId = StringTool.object2String(programMap.get("TASK_ID"));
                //程序启动脚本
                String programScriptShName = StringTool.object2String(programMap.get("SCRIPT_SH_NAME"));
                //程序运行主机
                String programHostId = StringTool.object2String(programMap.get("HOST_ID"));
                //本地网信息
                String latnId = StringTool.object2String(programMap.get("LATN_ID"));
                //程序别名
                String programAlias = StringTool.object2String(programMap.get("PROGRAM_ALIAS"));
                //程序描述
                String programDesc = StringTool.object2String(programMap.get("PROGRAM_DESC"));
                
                //当前该程序在自己启动的程序中存在,需要对比所有前台展示的字段，只有当所有字段均一致才过滤不展示
                boolean isExists = false;
                for (int k = 0; k < selfData.size(); k++) {
                    if (StringTool.object2String(selfData.get(k).get("PROGRAM_CODE")).equals(programCode)
                            && StringTool.object2String(selfData.get(k).get("PROGRAM_ALIAS")).equals(programAlias)
                            && StringTool.object2String(selfData.get(k).get("LATN_ID")).equals(latnId)
                            && StringTool.object2String(selfData.get(k).get("HOST_ID")).equals(programHostId)
                            && StringTool.object2String(selfData.get(k).get("SCRIPT_SH_NAME")).equals(programScriptShName)
                            && StringTool.object2String(selfData.get(k).get("PROGRAM_DESC")).equals(programDesc)) {
                        isExists = true;
                        break;
                    }
                }
                if (isExists) {
                    continue;
                }

                // && !BusinessConstant.PARAMS_BUS_1.equals(programState)
                if (BlankUtil.isBlank(otherData)) {
                    programMap.put("REAL_ID", programMap.get("ID"));
                    programMap.put("ID", "");
                    programMap.put("TASK_ID", taskId);
                    programMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
                    otherData.add(programMap);
                } else {
                	boolean isPass = true;
                    for (int k = 0; k < otherData.size(); k++) {
                        String otherProgramCode = StringTool.object2String(otherData.get(k).get("PROGRAM_CODE"));
                        String otherTaskId = StringTool.object2String(otherData.get(k).get("TASK_ID"));
                        String otherScriptShName = StringTool.object2String(otherData.get(k).get("SCRIPT_SH_NAME"));
                        String otherProgramHostId = StringTool.object2String(otherData.get(k).get("HOST_ID"));
                        //本地网信息
                        String otherLatnId = StringTool.object2String(otherData.get(k).get("LATN_ID"));
                        //程序别名
                        String otherProgramAlias = StringTool.object2String(otherData.get(k).get("PROGRAM_ALIAS"));
                        //程序描述
                        String otherProgramDesc = StringTool.object2String(otherData.get(k).get("PROGRAM_DESC"));

                        //只有当展示所有的字段均一致才过滤不显示，否则均显示出来
                        if (programCode.equals(otherProgramCode)
                        		&& programHostId.equals(otherProgramHostId)
                                //&& programTaskId.equals(otherTaskId)
                                && programScriptShName.equals(otherScriptShName)
                                && programAlias.equals(otherProgramAlias)
                                && latnId.equals(otherLatnId)
                                && programDesc.equals(otherProgramDesc)) {
                        	isPass = false;
                        	log.info(programCode + "--->" + otherProgramCode + "," 
                            		+ programHostId + "--->" + otherProgramHostId + ","
                            		+ programScriptShName+ "--->" + otherScriptShName);
                        	log.info("当前业务实例已经存在，过滤...");
                            break;
                        }
                    }
                    if (isPass) {
                        programMap.put("REAL_ID", programMap.get("ID"));
                    	programMap.put("ID", "");
                        programMap.put("TASK_ID", taskId);
                        programMap.put("RUN_STATE", BusinessConstant.PARAMS_STOP_STATE_ACTIVE);
                        otherData.add(programMap);
                    }
                }
            }
            rstList.addAll(selfData);
            log.debug("当前版本已经包含的实例程序：" + selfData.size());

            rstList.addAll(otherData);
            log.debug("继承其他版本业务实例个数: " + otherData.size());
        }

        //过滤查询条件为运行状态的数据
        if (BusinessConstant.PARAMS_BUS_1.equals(programState) && !BlankUtil.isBlank(rstList)) {
        	for (int i = 0; i < rstList.size(); i++) {
				if (BusinessConstant.PARAMS_STOP_STATE_ACTIVE.equals(StringTool.object2String(rstList.get(i).get("RUN_STATE")))) {
					rstList.remove(rstList.get(i));
					i--;
				} 
			}
        }
        if (BusinessConstant.PARAMS_BUS_0.equals(programState) && !BlankUtil.isBlank(rstList)) {
            for (int i = 0; i < rstList.size(); i++) {
                //if (StringUtils.isBlank(StringTool.object2String(rstList.get(i).get("ID")))) {
                if (BusinessConstant.PARAMS_START_STATE_ACTIVE.equals(StringTool.object2String(rstList.get(i).get("RUN_STATE")))) {
                    rstList.remove(rstList.get(i));
                    i--;
                }
            }
        }
        int stopStatus = 0;
        int runStatus = 0 ;
        for (int i = 0; i < rstList.size(); i++) {
			if (BusinessConstant.PARAMS_STOP_STATE_ACTIVE.equals(StringTool.object2String(rstList.get(i).get("RUN_STATE")))) {
				stopStatus++;
			}else{
				runStatus ++;
			}
		}

		//对程序列表进行排序
        rstList = rstList.stream().sorted(Comparator.comparing(BusProgramTaskServiceImpl::comparingByHostIp)
                .thenComparing(Comparator.comparing(BusProgramTaskServiceImpl::comparingByProgramAlias))).collect(Collectors.toList());
        log.debug("查询程序信息， 可启动程序总数: " + (rstList.size()));
        
        //返回查询的程序列表
        Map<String, Object> rstMap = new HashMap<String, Object>();
        rstMap.put("PROGRAM_LIST", rstList);
        rstMap.put("stopStatus", stopStatus);
        rstMap.put("runStatus", runStatus);
        rstMap.put("countRow", rstList.size());
        
      //根据集群ID查询当前集群是否运行在Jstorm中
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("CLUSTER_ID", clusterId);
        Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryMap, dbKey);
		if (!BlankUtil.isBlank(clusterMap) && !BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
			//业务组件部署根目录
			String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
			//版本号
            String version = StringTool.object2String("V"+params.get("VERSION"));
			//业务程序配置文件路径
			String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(version) + Constant.CFG;
			rstMap.put("cfgFilePath", busCfgPath);
		}
        return rstMap;
    }

    private static String comparingByProgramAlias(Map<String, Object> map){
        return StringTool.object2String(map.get("PROGRAM_ALIAS"));
    }

    private static String comparingByHostIp(Map<String, Object> map){
        return StringTool.object2String(map.get("HOST_IP"));
    }

    /**
     * 添加业务程序
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> insetBusProgramTask(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("添加程序实例信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        Map<String, Object> rstMap = new HashMap<String, Object>();
        try {
            //程序类型
            String programType = StringTool.object2String(params.get("PROGRAM_TYPE"));
            //集群ID
            String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
            //集群类型
            String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
            //业务主集群ID
            String busClusterId = StringTool.object2String(params.get("BUS_CLUSTER_ID"));
            //版本号
            String version = StringTool.object2String(params.get("VERSION"));
            //根据集群ID查询当前集群是否运行在Jstorm中
            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put("CLUSTER_TYPE", clusterType);
            List<HashMap<String, Object>> clusterTypeList = coreService.queryForList2New("clusterEleDefine.queryClusterEleList", queryMap, dbKey);
            queryMap.put("CLUSTER_ID", clusterId);
            Map<String, Object> clusterMap = coreService.queryForObject2New("serviceType.queryClusterById", queryMap, dbKey);
    		if (BlankUtil.isBlank(clusterMap) || BlankUtil.isBlank(clusterMap.get("CLUSTER_DEPLOY_PATH"))) {
    			throw new RuntimeException("获取业务集群信息失败, 请检查！");
    		}
    		
    		//业务组件部署根目录
    		String appRootPath = FileTool.exactPath(StringTool.object2String(clusterMap.get("CLUSTER_DEPLOY_PATH")));
    		String versionDir = StringTool.object2String("V" + version) + "/";
    		//业务程序配置文件路径
			String busCfgPath = FileTool.exactPath(appRootPath) + Constant.BUSS + FileTool.exactPath(versionDir) + Constant.CFG;

            //运行在Jstorm中的程序不需要主机ID信息，否则其他的程序都需要和主机ID关联
            if (!BlankUtil.isBlank(clusterTypeList) && StringTool.object2String(clusterTypeList.get(0).get("RUN_JSTORM")).equals(BusinessConstant.PARAMS_BUS_1)) {
                //添加程序任务信息
            	String programCode = StringTool.object2String(params.get("PROGRAM_CODE"));
            	
            	String finalProgramName = programCode + "-" + version;
            	
                Map<String, String> addParams = new HashMap<String, String>();
                addParams.put("CLUSTER_ID", clusterId);
                addParams.put("BUS_CLUSTER_ID", busClusterId);
                addParams.put("PROGRAM_CODE", programCode);
                addParams.put("PROGRAM_NAME", finalProgramName);
                addParams.put("HOST_ID", "");
                addParams.put("TASK_ID", StringTool.object2String(params.get("TASK_ID")));
                addParams.put("RUN_STATE", BusinessConstant.PARAMS_BUS_0);
                addParams.put("SCRIPT_SH_NAME", StringTool.object2String(params.get("SCRIPT_SH_NAME")));
                addParams.put("CONFIG_FILE", StringTool.object2String(params.get("CONFIG_FILE")));
                addParams.put("CONFIG_FILE_PATH", FileUtil.exactPath(busCfgPath) + StringTool.object2String(params.get("CONFIG_FILE")));
                addParams.put("PROGRAM_TYPE", programType);
                addParams.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));
                addParams.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
                coreService.insertObject("taskProgram.insertTaskProgram", addParams, dbKey);
            } else {
                String hostStr = StringTool.object2String(params.get("HOST_ID"));

                String[] hostArray = null;
                //获取当前集群部署的所有主机列表
                if (BlankUtil.isBlank(hostStr)) {
                    Map<String, Object> queryMaps = new HashMap<String, Object>();
                    queryMaps.put("CLUSTER_ID", clusterId);
                    queryMaps.put("CLUSTER_TYPE", clusterType);
                    List<HashMap<String, Object>> hostList = coreService.queryForList2New("deployHome.queryDeployHostByCodeAndHost", params, dbKey);
                    if (!BlankUtil.isBlank(hostList)) {
                        hostArray = new String[hostList.size()];
                        for (int i = 0; i < hostList.size(); i++) {
                            hostArray[i] = StringTool.object2String(hostList.get(i).get("HOST_ID"));
                        }
                    }
                } else {
                    if (hostStr.indexOf(",") != -1) {
                        hostArray = hostStr.split(",");
                    } else {
                        hostArray = new String[]{hostStr};
                    }
                }
                	
                
                //批量添加实例
                List<Map<String, String>> addBatchParams = new ArrayList<Map<String, String>>();
                for (int i = 0; i < hostArray.length; i++) {
                	Map<String,Object> indexMap = coreService.queryForObject2New("taskProgram.queryTaskPidIndex", null, dbKey);
                    Map<String, String> addMap = new HashMap<String, String>();
                    addMap.put("CLUSTER_ID", clusterId);
                    addMap.put("BUS_CLUSTER_ID", busClusterId);
                    addMap.put("PROGRAM_CODE", StringTool.object2String(params.get("PROGRAM_CODE")));
                    addMap.put("PROGRAM_NAME", StringTool.object2String(params.get("PROGRAM_NAME")));
                    addMap.put("HOST_ID", hostArray[i]);
                    addMap.put("TASK_ID", StringTool.object2String(params.get("TASK_ID")));
                    addMap.put("RUN_STATE", BusinessConstant.PARAMS_BUS_0);
                    
                    String scirptssh = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
                    String newscirptssh = scirptssh;
                    Map<String, Object> defineMap = coreService.queryForObject2New("programDefine.queryProgramList", params, dbKey);
        			if(defineMap !=null && StringUtils.equals(StringTool.object2String(defineMap.get("MULTI_PROCESS")),"1")){
        				String pid = StringTool.object2String(indexMap.get("PID"));
        				String actionFlag = StringTool.object2String(params.get("actionFlag"));
        				if(StringUtils.equals("copy", actionFlag) && scirptssh.indexOf("tid-") >0){
        					newscirptssh = scirptssh.substring(0,scirptssh.lastIndexOf(" ")+1);
        				}
        				
        				//为周边程序加一个随机码，做检查时，能分辨出来
            			if(scirptssh.lastIndexOf("\"")>0){
            				newscirptssh = newscirptssh.substring(0, newscirptssh.length()-1);
            				newscirptssh += " "+ pid +"\"";
            			}else{
            				newscirptssh += " "+ pid ;
            			}
        			}

        			//将最后的空格去掉
        			if (newscirptssh.endsWith(" \"")) {
                        newscirptssh = newscirptssh.substring(0, newscirptssh.length() - 1).trim()+"\"";
                    }
                    addMap.put("SCRIPT_SH_NAME", newscirptssh);
                    addMap.put("CONFIG_FILE", StringTool.object2String(params.get("CONFIG_FILE")));
                    addMap.put("CONFIG_FILE_PATH", StringTool.object2String(params.get("CONFIG_FILE")).replace("$P",FileUtil.exactPath(busCfgPath)));
                    addMap.put("PROGRAM_TYPE", programType);
                    addMap.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));
                    addMap.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
                    addMap.put("PROGRAM_DESC", StringTool.object2String(params.get("PROGRAM_DESC")));
                    addBatchParams.add(addMap);
                }
                log.debug("本次可添加程序实例个数: " + (addBatchParams.size()));

                //批量添加数据
                if (!BlankUtil.isBlank(addBatchParams)) {
                    coreService.insertBatchObject("taskProgram.addBatchTaskProgram", addBatchParams, dbKey);
                }
            }
            log.debug("程序实例添加成功...");
            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
            rstMap.put(Constant.RST_STR, "添加程序实例成功！");
        } catch (Exception e) {
            log.error("添加程序实例失败, 失败原因: ", e);
            throw new RuntimeException("添加程序实例失败！");
            //rstMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
            //rstMap.put(Constant.RST_STR, "添加程序实例失败！");
        }
        return rstMap;
    }

    /**
     * 业务程序版本删除（只删除当前业务程序版本，该程序其他版本业务程序不删除，需要和业务启停界面删除区分开来）
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> deleteCurrVersionBusProgramTask(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("状态检查删除集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        Map<String, Object> rstMap = new HashMap<String, Object>();

        try {
            //判断该集群是否已经被部署
            String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
            //主机ID
            String hostId = StringTool.object2String(params.get("HOST_ID"));
            //程序编码
            String programCode = StringTool.object2String(params.get("PROGRAM_CODE"));
            //启停脚本
            String scriptShName = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
            //ID
            String ID = StringTool.object2String(params.get("ID"));

            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put("CLUSTER_ID", clusterId);
            queryMap.put("HOST_ID", hostId);
            queryMap.put("PROGRAM_CODE", programCode);
            queryMap.put("SCRIPT_SH_NAME", scriptShName);
            queryMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
            //程序别名
            queryMap.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
            //程序名称
            queryMap.put("PROGRAM_NAME", StringTool.object2String(params.get("PROGRAM_NAME")));
            //本地网ID
            queryMap.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));
            //程序实例ID
            queryMap.put("ID", ID);
            List<HashMap<String, Object>> list = coreService.queryForList2New("taskProgram.queryTaskProgramListById", queryMap, dbKey);
            if (!BlankUtil.isBlank(list)) {
                String versionStr = "";
                for (int i = 0; i < list.size(); i++) {
                    String currVersion = StringTool.object2String(list.get(i).get("VERSION"));
                    //提出重复版本
                    if (versionStr.indexOf(currVersion + ",") != -1) {
                        continue;
                    }
                    versionStr += currVersion + ", ";
                }
                versionStr = versionStr.substring(0, versionStr.length() - 2);
                throw new RuntimeException("当前程序实例在版本【" + versionStr + "】存在运行实例， 请先停止对应版本实例再删除！");
            }

            //删除程序任务表
            Map<String, String> delParams = new HashMap<String, String>();
            delParams.put("PROGRAM_CODE", programCode);
            delParams.put("SCRIPT_SH_NAME", scriptShName);
            delParams.put("HOST_ID", hostId);
            delParams.put("CLUSTER_ID", clusterId);
            delParams.put("ID", ID);
            //程序别名
            delParams.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
            //程序名称
            delParams.put("PROGRAM_NAME", StringTool.object2String(params.get("PROGRAM_NAME")));
            //本地网ID
            delParams.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));

            int delTaskCount = 0;
            if (StringUtils.isNotBlank(ID)) {
                delTaskCount = coreService.deleteObject("taskProgram.delProgramTaskByID", delParams, dbKey);
            } else {
                delTaskCount = coreService.deleteObject("taskProgram.delProgramTask", delParams, dbKey);
            }
            log.debug("删除程序进程表信息成功, 删除的记录数据: " + delTaskCount);
            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
            rstMap.put(Constant.RST_STR, "程序实例删除成功！");
        } catch (Exception e) {
            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
            rstMap.put(Constant.RST_STR, "程序实例删除失败， 失败原因: " + e.getMessage());
            throw e;
        }
        return rstMap;
    }

    /**
     * 删除集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> deleteBusProgramTask(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("删除集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        Map<String, Object> rstMap = new HashMap<String, Object>();

        try {
            //判断该集群是否已经被部署
            String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
            //主机ID
            String hostId = StringTool.object2String(params.get("HOST_ID"));
            //程序编码
            String programCode = StringTool.object2String(params.get("PROGRAM_CODE"));
            //启停脚本
            String scriptShName = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
            //ID
            String ID = StringTool.object2String(params.get("ID"));

            Map<String, Object> queryMap = new HashMap<String, Object>();
            queryMap.put("CLUSTER_ID", clusterId);
            queryMap.put("HOST_ID", hostId);
            queryMap.put("PROGRAM_CODE", programCode);
            queryMap.put("SCRIPT_SH_NAME", scriptShName);
            queryMap.put("RUN_STATE", BusinessConstant.PARAMS_START_STATE_ACTIVE);
            //程序别名
            queryMap.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
            //程序名称
            queryMap.put("PROGRAM_NAME", StringTool.object2String(params.get("PROGRAM_NAME")));
            //本地网ID
            queryMap.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));
            //程序实例ID
            queryMap.put("ID", ID);
            List<HashMap<String, Object>> list = coreService.queryForList2New("taskProgram.queryTaskProgramListById", queryMap, dbKey);
            if (!BlankUtil.isBlank(list)) {
                String versionStr = "";
                for (int i = 0; i < list.size(); i++) {
                    String currVersion = StringTool.object2String(list.get(i).get("VERSION"));
                    //提出重复版本
                    if (versionStr.indexOf(currVersion + ",") != -1) {
                        continue;
                    }
                    versionStr += currVersion + ", ";
                }
                versionStr = versionStr.substring(0, versionStr.length() - 2);
                throw new RuntimeException("当前程序实例在版本【" + versionStr + "】存在运行实例， 请先停止对应版本实例再删除！");
            }

            //删除程序任务表
            Map<String, String> delParams = new HashMap<String, String>();
            delParams.put("PROGRAM_CODE", programCode);
            delParams.put("SCRIPT_SH_NAME", scriptShName);
            delParams.put("HOST_ID", hostId);
            delParams.put("CLUSTER_ID", clusterId);
            delParams.put("ID", ID);
            //程序别名
            delParams.put("PROGRAM_ALIAS", StringTool.object2String(params.get("PROGRAM_ALIAS")));
            //程序名称
            delParams.put("PROGRAM_NAME", StringTool.object2String(params.get("PROGRAM_NAME")));
            //本地网ID
            delParams.put("LATN_ID", StringTool.object2String(params.get("LATN_ID")));

            int delTaskCount = 0;
            if (StringUtils.isNotBlank(ID)) {
                delTaskCount = coreService.deleteObject("taskProgram.delProgramTaskByID", delParams, dbKey);

                //删除其他版本相同程序数据
                int delOtherVerCount = coreService.deleteObject("taskProgram.delProgramTask", delParams, dbKey);
                log.debug("删除当前程序其他版本记录数: " + delOtherVerCount);
            } else {
                delTaskCount = coreService.deleteObject("taskProgram.delProgramTask", delParams, dbKey);
            }
            log.debug("删除程序进程表信息成功, 删除的记录数据: " + delTaskCount);

            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
            rstMap.put(Constant.RST_STR, "程序实例删除成功！");
        } catch (Exception e) {
            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
            rstMap.put(Constant.RST_STR, "程序实例删除失败， 失败原因: " + e.getMessage());
            throw e;
        }  
        return rstMap;
    }
    
    /**
     * 删除业务程序实例
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> deleteBusTopologyProgramTask(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("删除集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);

        Map<String, Object> rstMap = new HashMap<String, Object>();

        try {
            //判断该集群是否已经被部署
            String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
            //程序编码
            String programCode = StringTool.object2String(params.get("PROGRAM_CODE"));
            //启停脚本
            String scriptShName = StringTool.object2String(params.get("SCRIPT_SH_NAME"));
            //ID
            String ID = StringTool.object2String(params.get("ID"));

            //删除程序任务表
            Map<String, String> delParams = new HashMap<String, String>();
            delParams.put("PROGRAM_CODE", programCode);
            delParams.put("SCRIPT_SH_NAME", scriptShName);
            delParams.put("CLUSTER_ID", clusterId);
            delParams.put("ID", ID);
            int delTaskCount = coreService.deleteObject("taskProgram.delProgramTaskByID", delParams, dbKey);
            log.debug("删除程序进程表信息成功, 删除的记录数据: " + delTaskCount);

            rstMap.put(Constant.RST_CODE, Constant.RST_CODE_SUCCESS);
            rstMap.put(Constant.RST_STR, "程序实例删除成功！");
        } catch (Exception e) {
        	log.error("程序实例删除失败",e);
        	 rstMap.put(Constant.RST_CODE, Constant.RST_CODE_FAILED);
             rstMap.put(Constant.RST_STR, "程序实例删除失败！");
        }
        return rstMap;
    }


    /**
     * 修改集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> updateBusProgramTask(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("修改集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
        //集群ID
        String clusterId = StringTool.object2String(params.get("CLUSTER_ID"));
        //集群类型
        String clusterType = StringTool.object2String(params.get("CLUSTER_TYPE"));
        //集群编码
        String clusterCode = StringTool.object2String(params.get("CLUSTER_CODE"));

        //查询集群编码是否已经被使用
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("CLUSTER_CODE", clusterCode);
        queryMap.put("CLUSTER_ID", params.get("CLUSTER_ID"));
        List<HashMap<String, Object>> list = coreService.queryForList2New("serviceType.queryBusProgramTaskForUpdate", queryMap, dbKey);
        if (!BlankUtil.isBlank(list)) {
            throw new RuntimeException("修改后的集群编码已经被使用，请重新输入！");
        }

        //集群名称
        String clusterName = StringTool.object2String(params.get("CLUSTER_NAME"));
        //添加集群信息
        Map<String, String> updateParams = new HashMap<String, String>();
        updateParams.put("CLUSTER_ID", clusterId);
        updateParams.put("CLUSTER_NAME", clusterName);
        updateParams.put("CLUSTER_CODE", clusterCode);
        updateParams.put("SEQ", StringTool.object2String(params.get("SEQ")));
        updateParams.put("M2DB_INSTANCE", StringTool.object2String(params.get("M2DB_INSTANCE")));
        updateParams.put("CLUSTER_DEPLOY_PATH", StringTool.object2String(params.get("CLUSTER_DEPLOY_PATH")));
        coreService.updateObject("serviceType.updateBusProgramTask", updateParams, dbKey);
        log.debug("集群信息修改成功, 集成名称: " + clusterName);
        return null;
    }


	@Override
	public Map<String, Object> updateTaskCell(Map<String, Object> params, String dbKey) throws Exception {

        //获取业务程序集成列表
        List<Map<String, Object>> busProgramList = null;

        String flag = StringTool.object2String( params.get("flag") );
        if(StringUtils.equals(flag, "update")){
            params.put("REAL_ID", params.get("ID"));

            //修改复制版本的业务程序信息
            String realID = StringTool.object2String(params.get("REAL_ID"));
            if (StringUtils.isNotBlank(realID)) {
                Map<String, Object> qryMap = new HashMap<>();
                qryMap.put("ID", realID);
                qryMap.put("CLUSTER_ID", params.get("CLUSTER_ID"));
                busProgramList = coreService.queryForList3New("taskProgram.queryExtAddBusProgramList", qryMap, dbKey);
                log.debug("当前程序信息: " + busProgramList);
            }

            coreService.updateObject2New("taskProgram.updateProgramCell", params, dbKey);
        } else {
            //修改复制版本的业务程序信息
            String realID = StringTool.object2String(params.get("REAL_ID"));
            if (StringUtils.isNotBlank(realID)) {
                Map<String, Object> qryMap = new HashMap<>();
                qryMap.put("ID", realID);
                qryMap.put("CLUSTER_ID", params.get("CLUSTER_ID"));
                busProgramList = coreService.queryForList3New("taskProgram.queryExtAddBusProgramList", qryMap, dbKey);
                log.debug("当前程序继承版本信息: " + busProgramList);
            }

            //新增当前版本程序任务表记录
            coreService.insertObject2New("taskProgram.insertTaskProgram", params, dbKey);
        }

        if (CollectionUtils.isNotEmpty(busProgramList)) {
            //获取当前程序所有的版本列表
            Map<String, Object> programMap = busProgramList.get(0);

            Map<String, Object> qryProgramMap = new HashMap<>();
            qryProgramMap.put("QUERY_FLAG", BusinessConstant.PARAMS_BUS_1);
            qryProgramMap.put("HOST_ID", programMap.get("HOST_ID"));
            qryProgramMap.put("PROGRAM_ALIAS", programMap.get("PROGRAM_ALIAS"));
            qryProgramMap.put("PROGRAM_NAME", programMap.get("PROGRAM_NAME"));
            qryProgramMap.put("PROGRAM_CODE", programMap.get("PROGRAM_CODE"));
            qryProgramMap.put("CLUSTER_ID", programMap.get("CLUSTER_ID"));
            qryProgramMap.put("SCRIPT_SH_NAME", programMap.get("SCRIPT_SH_NAME"));
            qryProgramMap.put("LATN_ID", programMap.get("LATN_ID"));
            qryProgramMap.put("PROGRAM_DESC", programMap.get("PROGRAM_DESC"));
            qryProgramMap.put("TASK_ID", params.get("TASK_ID"));
            List<Map<String, Object>> taskBusProgramList = coreService.queryForList3New("taskProgram.queryExtAddBusProgramList", qryProgramMap, dbKey);
            log.debug("修改其他版本程序信息:" + taskBusProgramList);
            if (CollectionUtils.isNotEmpty(taskBusProgramList)) {
                for (int i=0; i<taskBusProgramList.size(); i++) {
                    params.put("ID", taskBusProgramList.get(i).get("ID"));
                    if (StringUtils.equals(flag, "update")) {
                        //update下不需要设置修改字段值，本身就包含
                    } else {
                        params.put("PROGRAM_ALIAS_CELL", "PROGRAM_ALIAS");
                        params.put("PROGRAM_DESC_CELL", "PROGRAM_DESC");
                        params.put("SCRIPT_SH_NAME_CELL", "SCRIPT_SH_NAME");
                    }
                    coreService.updateObject2New("taskProgram.updateProgramCell", params, dbKey);
                }
            }
        }
        return null;
	}


	/**
	* @Description: 查询业务程序启停日志信息
	* @return Map
	* @author yuanhao
	* @date 2019-12-05 17:23
	*/
    @Override
    public Map<String, Object> queryLogDetail(Map<String, Object> params, String dbKey) throws Exception {
        log.debug("查询业务程序启停日志信息，参数:" + params + ", dbKey: " + dbKey);

        //处理结果返回信息
        Map<String, Object> retMap = new HashMap<>();

        try {
            //获取日志文件输出目录
            String logPath = SystemProperty.getContextProperty(Constant.BUSS_TASK_LOG_PATH);
            if (StringUtils.isBlank(logPath)) {
                logPath = LoggerUtils.DEFAULT_LOG_PATH;
            }
            //日志文件名称 ID+programCode+.log
            String programTaskId = ObjectUtils.toString(params.get("ID"));
            if (StringUtils.isBlank(programTaskId)) {
                retMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                retMap.put("retMsg", "该业务程序尚未启停过，无上次操作日志!");
                return retMap;
            }

            String programCode = ObjectUtils.toString(params.get("PROGRAM_CODE"));
            String logFileName = programTaskId + "_" + programCode + ".log";
            String logFilePath = StringUtils.removeEnd(logPath, "/") + "/" + logFileName;
            log.info("业务程序启停，日志文件: " + logFilePath);

            File file = new File(logFilePath);
            String fileContent = "";
            StringBuffer buffer = new StringBuffer();
            if (file.exists()) {
                FileInputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;
                try {
                    inputStream = new FileInputStream(file);
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    String lineStr = null;
                    while ((lineStr = bufferedReader.readLine()) != null) {
                        buffer.append(lineStr).append("\n");
                    }
                    fileContent = new String(buffer.toString().getBytes(LoggerUtils.LOG_ENCODING_GBK), LoggerUtils.DEFAULT_LOG_ENCODING);
                    fileContent = fileContent.replace("[ERROR]", "<label style='color:red;'>[ERROR]</label>").replace("[ WARN]", "<label style='color:yellow;'>[ WARN]</label>");
                    retMap.put("retCode", BusinessConstant.PARAMS_BUS_1);
                    retMap.put("retMsg", fileContent);
                } catch (Exception e) {
                    log.error("解析日志文件失败， 失败原因:", e);
                    retMap.put("retCode", BusinessConstant.PARAMS_BUS_0);
                    retMap.put("retMsg", "日志文件获取失败，请检查!");
                } finally {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } else {
                retMap.put("retCode", BusinessConstant.PARAMS_BUS_2);
                retMap.put("retMsg", "该业务程序尚未启停过，无上次操作日志!");
            }
        } catch (Exception e) {
            log.error("日志文件获取失败， 失败原因: ", e);
            retMap.put("retCode", BusinessConstant.PARAMS_BUS_0);
            retMap.put("retMsg", "日志文件获取失败，请检查!");
        }
        return retMap;
    }


	public static void main(String[] args) {
		String aa = "scirptssh.xml";
		System.out.println(aa.substring(0,aa.length()-1));
	}

}
