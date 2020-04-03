package com.tydic.service.configure.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.common.ComParamsHelper;
import com.tydic.service.configure.SwitchMasterStandbyAbmService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.XmlTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [SwitchMasterStandbyAbmServiceImpl]     
  * @Description:  [ABM灰度升级]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-8-29 下午2:54:20]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-8-29 下午2:54:20]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class SwitchMasterStandbyAbmServiceImpl implements SwitchMasterStandbyAbmService {
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SwitchMasterStandbyAbmServiceImpl.class);

	/**
	 * 设置默认编码
	 */
	private static final String DEFAULT_ENCODE = "UTF-8";
	
	/**
	 * 从数据字典表获取本地网列表
	 */
	private static final String LATN_LIST = "LATN_LIST";
	
	/**
	 * 节点名称
	 */
	private static final String LATN_ID = "latn_id";
	
	/**
	 * 版本切换节点名
	 */
	private static final String SWITCH_FILE_UP_NODE = "Up";
	
	
	/**
	 * 灰度升级：获取本地网
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<HashMap<String, Object>> getLatnElement(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("灰度升级， 获取本地网信息， 参数: " + param.toString() + ", dbKey: " + dbKey);
		
		//获取所有的本地网
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("GROUP_CODE", LATN_LIST);
		List<HashMap<String, Object>> latnList = coreService.queryForList2New("config.queryConfigList", queryMap, dbKey);
		
		//解析配置文件，获取配置文件中本地网列表数据
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//版本切换配置文件
		String switchConfig = StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));

		FtpDto ftpDto = SessionUtil.getFtpParams();
		String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + packageType;
		String deployPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessPath) + switchConfig;
		
		Trans trans  = FTPUtils.getFtpInstance(ftpDto);
		trans.login();
		InputStream in = trans.get(deployPath);
		Document xmlFile=XmlTool.read(in);
		in.close();
		trans.completePendingCommand();
		Element rootEle=xmlFile.getRootElement();
		List<Element> upEles=rootEle.elements(SWITCH_FILE_UP_NODE);
		Element editUpEle = null;
		
		//获取程序所有GROUP
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("解析版本切换配置文件 , 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		for(int i=0;i<upEles.size();i++){
			String topValue=upEles.get(i).attribute("ServGroup").getValue();
			if(topValue.equals(programGroup)){
				editUpEle=upEles.get(i);
				break;
			}
		}
		
		List<Map<String,String>> valList = new ArrayList<Map<String,String>>();
		if (editUpEle != null) {
			Element latnEle = XmlTool.getChildElement(editUpEle, "latn_id");
			if (latnEle != null) {
				Iterator<Element> iter = editUpEle.elementIterator();
				while(iter.hasNext()) {
					Element childEle = iter.next();
					if(LATN_ID.equalsIgnoreCase(childEle.getName())){
						Map<String,String> attr=new HashMap<String,String>();
						attr.put("latnId", childEle.attribute("value").getValue());
						attr.put("Current", childEle.attribute("Current").getValue());
						valList.add(attr);
					}
				}
			}
		}
		
		for (int i=0; i<latnList.size(); i++) {
			Map<String, Object> latnMap = latnList.get(i);
			latnMap.put("isUsed", BusinessConstant.PARAMS_BUS_0);
			for (int j=0; j<valList.size(); j++) {
				Map<String, String> configMap = valList.get(j);
				if (latnMap.get("CONFIG_VALUE").equals(configMap.get("latnId"))) {
					latnMap.put("isUsed", BusinessConstant.PARAMS_BUS_1);
					break;
				}
			}
		}
		
		log.debug("获取本地网列表，本地网列表信息： " + latnList);
		
		return latnList;
	}

	/**
	 * 程序操作：灰度升级(改为1， current和next不对调，加子节点,子节点current是父类的next值)
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateGreyUpgrade(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("灰度升级开始, 业务参数: " + param + ", dbKey: " + dbKey);
		//灰度升级配置文件名称
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		//临时根目录
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		//包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序组
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("灰度升级, 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		
		//获取本地网列表
		List<HashMap<String, Object>> latnList = (List<HashMap<String, Object>>) param.get("latn_element");
		
		//集群ID
		String localTmpPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
		
		//获取集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}

		String docStr = null;
		Trans trans = null; 
		InputStream bin = null;
		try{
			//获取版本发布服务器上版本切换配置文件
			FtpDto ftpDto = SessionUtil.getFtpParams();

			String busformPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
			String deployswitchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath) + FileTool.exactPath(packageType) + fileName;
			log.debug("灰度升级, 部署主机配置文件路径: " + deployswitchPath);
			
			trans  = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			trans.get(deployswitchPath, localTmpPath); 
			//将远程主机文件解析成XML文件
			Document doc = XmlTool.read(new File(localTmpPath), DEFAULT_ENCODE);
			Element rootElement = XmlTool.getRootElement(doc);
			Iterator<Element> iter = rootElement.elementIterator();
			Element topicUpNodeEle = null;
			while(iter.hasNext()) {
				Element childEle = iter.next();
				if(SWITCH_FILE_UP_NODE.equalsIgnoreCase(childEle.getName())){
					if (programGroup.equalsIgnoreCase(childEle.attribute("ServGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该UP节点的ServGroup属性，请检查!");
			}

			//删除Up所有子节点
			Iterator<Element> iterEle = XmlTool.getIterator(topicUpNodeEle);
			while(iterEle.hasNext()) {
				topicUpNodeEle.remove(iterEle.next());
			}

			//修改Up节点数据
			String currentValue = topicUpNodeEle.attribute("current").getValue();
			String nextValue = topicUpNodeEle.attribute("next").getValue();
			
			if (!BlankUtil.isBlank(latnList) && !latnList.isEmpty()) {
				for (int i=0; i<latnList.size(); i++) {
					String latnId = StringTool.object2String(latnList.get(i));
					//循环创建节点
					Element servEle = topicUpNodeEle.addElement("latn_id");
					servEle.addAttribute("value", latnId)
					.addAttribute("Current", nextValue);
				}
			}

			topicUpNodeEle.addAttribute("upgradestate",Constant.UPGRADE_STATE_FLAG_0);
			//.addAttribute("current", nextValue).addAttribute("next", currentValue)
			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			 
			//修改当前业务包类型下面的配置文件
			String switchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
			trans.put(bin, switchPath);
			log.debug("版本升级修改配置文件操作， 同步版本发布服务器【" + fileName + "】文件成功...");
			
//			查询所有的包类型,同步sp_switch.xml文件
//			Map<String, Object> qryParams = new HashMap<String, Object>();
//			qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
//			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
//			for (int j=0; j<ftpList.size(); j++) {
//				String configValue = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
//				
//				//判断包类型下是否存在版本切换配置文件，如果不存在则不需要同步上传
//				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue);
//				Vector<FileRecord> fileList = trans.getFileList(switchPath);
//				if (!BlankUtil.isBlank(fileList)) {
//					for (int i=0; i<fileList.size(); i++) {
//						String spSwitchFileName = fileList.get(i).getFileName();
//						String fileType = StringTool.object2String(fileList.get(i).getFileType());
//						if (spSwitchFileName.equalsIgnoreCase(fileName) && "F".equals(fileType)) {
//							String switchName = switchPath + fileName;
//							bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//							trans.put(bin, switchName);
//							break;
//						}
//					}
//				}
//				//String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue) + fileName;
//				//bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//				//trans.put(bin, switchPath);
//			}
//			log.debug("灰度升级,同步版本发布服务器" + fileName + "文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
		}catch(Exception e){
			log.error("灰度升级失败, 失败原因: ", e);
			throw new Exception(e.getMessage());
		}finally {
			if(trans != null) {
				trans.close();
			}
			if(bin != null){
				bin.close();
			}
		}
		
		//远程主机操作
		for (int i=0; i<switchHostList.size(); i++) {
			Trans sftClient = null;
			InputStream inputStream = null;
			String hostIp = null;
			try {
				Map<String, Object> singleHost = switchHostList.get(i);
				hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
				String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
				
				String remotePath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
				String remoteFinalPath = FileTool.exactPath(remotePath) + FileTool.exactPath(Constant.BUSS) + fileName;
				log.debug("灰度升级, 远程部署主机配置文件路径: " + remoteFinalPath + ", 当前主机：" + hostIp);
				
				sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
				sftClient.login();
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("灰度发布【" + fileName + "】文件修改成功，当前主机:" + hostIp);
			} catch (Exception e) {
				log.error("灰度升级修改远程主机文件失败, 失败信息: ", e);
				throw new RuntimeException("Topology配置文件修改失败， 当前主机IP：" + hostIp);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sftClient != null) {
					sftClient.close();
				}
			}
		}

		log.debug("灰度升级配置文件修改成功...");
		//数据库操作
		multiOperationDatabase(programGroup, Constant.SWITCH_UPGRADE, dbKey);
		//返回值
		Map<String,Object> returnMap=new HashMap<String,Object>();
		returnMap.put("rstCode", "0");
		return returnMap;
	}
	
	/**
	 * 程序操作：灰度升级(改为1， current和next不对调，加子节点,子节点current是父类的next值)
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateGreyUpgradeConfig(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("修改灰度升级配置文件， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//灰度升级配置文件名称
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		
		//包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序组
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("查看版本切换配置文件，业务包类型: " + packageType + ", 程序Group: " + programGroup);
		
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		
		//本地网列表
		List<HashMap<String, Object>> latnList = (List<HashMap<String, Object>>) param.get("latn_element");
		//集群ID
		String localTmpPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
		
		//获取需要分发的业务集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}
		
		String docStr = null;
		Trans trans = null; 
		InputStream bin = null;
		FtpDto ftpDto = SessionUtil.getFtpParams();
		try{
			//获取 sp_switch.xml文件到ftp主机


			String busformPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;

			String deployswitchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath) + FileTool.exactPath(packageType) + fileName;
			log.debug("部署主机配置文件路径: " + deployswitchPath);
			
			trans  = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			trans.get(deployswitchPath, localTmpPath); 
			//将远程主机文件解析成XML文件
			Document doc = XmlTool.read(new File(localTmpPath), DEFAULT_ENCODE);
			Element rootElement = XmlTool.getRootElement(doc);
			Iterator<Element> iter = rootElement.elementIterator();
			Element topicUpNodeEle = null;
			while(iter.hasNext()) {
				Element childEle = iter.next();
				if(SWITCH_FILE_UP_NODE.equalsIgnoreCase(childEle.getName())){
					if (programGroup.equalsIgnoreCase(childEle.attribute("ServGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该UP节点的ServGroup属性，请检查!");
			}

			//删除Up所有子节点
			Iterator<Element> iterEle = XmlTool.getIterator(topicUpNodeEle);
			while(iterEle.hasNext()) {
				topicUpNodeEle.remove(iterEle.next());
			}

			//修改Up节点数据
			String currentValue = topicUpNodeEle.attribute("current").getValue();
			String nextValue = topicUpNodeEle.attribute("next").getValue();
			if (!BlankUtil.isBlank(latnList) && !latnList.isEmpty()) {
				for (int i=0; i<latnList.size(); i++) {
					//循环创建节点
					Element latnEle = topicUpNodeEle.addElement("latn_id");
					String latnId=StringTool.object2String(latnList.get(i));
					latnEle.addAttribute("value",latnId).addAttribute("Current", nextValue);
				}
			}
			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			
			//同步修改版本发布服务器对应的版本包类型
			String switchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
			trans.put(bin, switchPath);
			log.debug("版本升级修改配置文件操作， 同步版本发布服务器【" + fileName + "】文件成功...");
				
			//查询所有的包类型,同步sp_switch.xml文件
//			Map<String, Object> qryParams = new HashMap<String, Object>();
//			qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
//			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
//			for (int j=0; j<ftpList.size(); j++) {
//				String configValue = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
//				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue) + fileName;
//				bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//				trans.put(bin, switchPath);
//			}
//			log.debug("灰度升级修改配置文件操作,同步版本发布服务器sp_switch.xml文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
		}catch(Exception e){
			log.error("灰度升级配置修改失败, 失败原因: ", e);
			throw new Exception(e.getMessage());
		}finally {
			if(trans != null) {
				trans.close();
			}
			if(bin != null){
				bin.close();
			}
		}

		//远程主机操作
		for (int i=0; i<switchHostList.size(); i++) {
			Trans sftClient = null;
			InputStream inputStream = null;
			String hostIp = null;
			try {
				Map<String, Object> singleHost = switchHostList.get(i);
				hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
				String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
				
				String remotePath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
				String remoteFinalPath = FileTool.exactPath(remotePath) + Constant.BUSS + fileName;
				log.debug("灰度升级配置文件修改, 文件路径: " + remoteFinalPath);
				
				sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpDto.getFtpType());
				sftClient.login();
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("灰度配置文件【" + fileName + "】修改成功，当前主机:" + hostIp);
			} catch (Exception e) {
				log.error("灰度升级修改配置文件失败, 异常信息: ", e);
				throw new RuntimeException("Topology配置文件修改失败， 当前主机IP：" + hostIp);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sftClient != null) {
					sftClient.close();
				}
			}
		}
		log.debug("灰度升级配置文件修改成功...");
		//返回值
		Map<String,Object> returnMap=new HashMap<String,Object>();
		returnMap.put("rstCode", "0");
		return returnMap;
	}

	/**
	 * 程序操作：正式发布(操作是把upgradestate= "0"改为1,交换next和current)
	 * @param param
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> updateOfficialLaunch(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("版本正式发布， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		//灰度升级配置文件
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
				
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		//业务包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("正式发布, 业务包类型: " + packageType + ", 程序Group: " + programGroup + ", 配置文件名称: " + fileName);
		
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		
		//本地临时文件
		String localTmpPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
		
		//获取需要分发的业务集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}
		FtpDto ftpDto = SessionUtil.getFtpParams();
		//操作远程主机
		Trans trans = null;
		String docStr = null;
		InputStream bin = null;
        try{
        	
    		//将灰度升级配置文件文件 重版本发布服务器拉取下来

			String busformPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
			trans  = FTPUtils.getFtpInstance(ftpDto);
			
			String deployHostPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath) + FileTool.exactPath(packageType) + fileName;
			log.debug("部署主机配置文件路径: " + deployHostPath);
			
			trans.login();
			trans.get(deployHostPath, localTmpPath); 
			//将远程主机文件解析成XML文件
			Document doc = XmlTool.read(new File(localTmpPath), DEFAULT_ENCODE);
			
			Element rootElement = XmlTool.getRootElement(doc);
			Iterator<Element> iter = rootElement.elementIterator();
			Element topicUpNodeEle = null;
			while(iter.hasNext()) {
				Element childEle = iter.next();
				if(SWITCH_FILE_UP_NODE.equalsIgnoreCase(childEle.getName())){
					if (programGroup.equalsIgnoreCase(childEle.attribute("ServGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该UP节点的ServGroup属性，请检查!");
			}

			//修改Up节点数据
			String upgradestateValue = topicUpNodeEle.attribute("upgradestate").getValue();
			if(upgradestateValue.equals(Constant.UPGRADE_STATE_FLAG_1)){
				//throw new RuntimeException("该topology已发布，请检查!");
			}

			//将Up节点Current节点和next节点数据进行对调
			String currentValue = topicUpNodeEle.attribute("current").getValue();
			String nextValue = topicUpNodeEle.attribute("next").getValue();
			topicUpNodeEle.addAttribute("current", nextValue)
			.addAttribute("next", currentValue)
			.addAttribute("upgradestate",Constant.UPGRADE_STATE_FLAG_1);

			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			
			//同步修改版本发布服务器对应的版本包类型
			String switchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
			trans.put(bin, switchPath);
			log.debug("版本升级修改配置文件操作， 同步版本发布服务器【" + fileName + "】文件成功...");
			 
			//查询所有的包类型,同步sp_switch.xml文件
//			Map<String, Object> qryParams = new HashMap<String, Object>();
//			qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
//			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
//			for (int j=0; j<ftpList.size(); j++) {
//				String configValue = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
//				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue) + fileName;
//				bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//				trans.put(bin, switchPath);
//			}
//			log.debug("正式发布,同步版本发布服务器sp_switch.xml文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
        }catch(Exception e){
        	log.debug("正式发布解析配置文件失败, 失败信息: ", e);
        	throw new RuntimeException(e.getMessage());
        }finally {
        	if(trans != null) {
				trans.close();
			}
			if(bin != null){
				bin.close();
			}
		}
	 
		for (int i=0; i<switchHostList.size(); i++) {
			Trans sftClient = null;
			InputStream inputStream = null;
			String hostIp  = null;
			try {
				Map<String, Object> singleHost = switchHostList.get(i);
				hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
				String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
				sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpDto.getFtpType());
				sftClient.login();
				
				String remotePath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
				String remoteFinalPath = FileTool.exactPath(remotePath) + Constant.BUSS + fileName;
				log.debug("正式发布配置文件分发路径: " + remoteFinalPath);
				
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("正式发布【" + fileName +"】文件修改成功,当前主机IP:" + hostIp);
			} catch (RuntimeException e) {
				log.error("正式发布, 修改远程主机" + fileName + "文件失败， 失败原因: ", e);
				throw e;
			} catch (Exception e) {
				log.error("正式发布-->修改Topology配置文件失败", e);
				throw new RuntimeException("Topology配置文件修改失败， 当前主机IP：" + hostIp);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sftClient != null) {
					sftClient.close();
				}
			}
		}
		log.debug("正式发布成功...");
		//数据库操作
    	multiOperationDatabase(programGroup,Constant.SWITCH_LAUNCH,dbKey);
    	log.debug("正式发布更新表成功...");
		//返回值
		returnMap.put("rstCode", "0");
		return returnMap;
	}

	/**
	 * 程序操作：回退（操作是把current和next对调）
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> updateRollback(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("版本切换回退， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//返回值
		Map<String, Object> returnMap=new HashMap<String, Object>();
		//灰度升级配置文件
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		
		//本地临时文件
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		String localTmpPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
		
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		//获取需要分发的业务集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}
		
		//业务包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
				
		//程序信息
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("版本回退, 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		
		//操作远程主机
		Trans trans = null;
		String docStr = null;
		InputStream bin = null;
		String currentValue = "";
		String nextValue = "";
		FtpDto ftpDto = SessionUtil.getFtpParams();
        try{
    		//将sp_switch.xml文件同步到ftp主机

			String busformPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;

			String deployswitchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			trans  = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			trans.get(deployswitchPath, localTmpPath); 
			//将远程主机文件解析成XML文件
			Document doc = XmlTool.read(new File(localTmpPath), DEFAULT_ENCODE);
			Element rootElement = XmlTool.getRootElement(doc);
			Iterator<Element> iter = rootElement.elementIterator();
			Element topicUpNodeEle = null;
			while(iter.hasNext()) {
				Element childEle = iter.next();
				if(SWITCH_FILE_UP_NODE.equalsIgnoreCase(childEle.getName())){
					if (programGroup.equalsIgnoreCase(childEle.attribute("ServGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}
			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该UP节点的ServGroup属性，请检查!");
			}
			//修改Up节点数据
			currentValue = topicUpNodeEle.attribute("current").getValue();
			nextValue = topicUpNodeEle.attribute("next").getValue();
			//在一行点击回退， 则只需要把upgradestate 改成1
            String actionFlag = StringTool.object2String(param.get("actionFlag"));
            topicUpNodeEle.addAttribute("upgradestate",Constant.UPGRADE_STATE_FLAG_1);
            if("row1back".equals(actionFlag)){
            	returnMap.put("curr", currentValue);
            	returnMap.put("next", nextValue);
            } else {
            	topicUpNodeEle.addAttribute("current", nextValue);
    			topicUpNodeEle.addAttribute("next", currentValue);
    			returnMap.put("curr", nextValue);
    			returnMap.put("next", currentValue);
            }
			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			
			//同步修改版本发布服务器对应的版本包类型
			String switchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
			trans.put(bin, switchPath);
			log.debug("版本升级修改配置文件操作， 同步版本发布服务器【" + fileName + "】文件成功...");
			
			//查询所有的包类型,同步sp_switch.xml文件
//			Map<String, Object> qryParams = new HashMap<String, Object>();
//			qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
//			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
//			for (int j=0; j<ftpList.size(); j++) {
//				String configValue = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
//				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue) + fileName;
//				bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//				trans.put(bin, switchPath);
//			}
//			log.debug("同步版本发布服务器sp_switch.xml文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
        }catch(Exception e){
        	log.error("灰度升级， 回退失败， 失败信息: ", e);
        	throw new RuntimeException(e.getMessage());
        }finally {
        	if(trans != null) {
				trans.close();
			}
			if(bin != null){
				bin.close();
			}
		}
		for (int i=0; i<switchHostList.size(); i++) {
			Trans sftClient = null;
			InputStream inputStream = null;
			String hostIp = null;
			try {
				Map<String, Object> singleHost = switchHostList.get(i);
				hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
				String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
				sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, ftpDto.getFtpType());
				sftClient.login();
				
				String remotePath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
				String remoteFinalPath = FileTool.exactPath(remotePath) + Constant.BUSS + fileName;
				
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("版本回退【" + fileName + "】文件修改成功，当前修改主机ip:" + hostIp);
			} catch (RuntimeException e) {
				log.error("灰度升级， 回退修改主机配置文件失败， 失败信息: ", e);
				throw e;
			} catch (Exception e) {
				log.error("回退-->修改Topology配置文件失败, 异常信息: ", e);
				throw new RuntimeException("Topology配置文件修改失败， 当前主机IP：" + hostIp);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sftClient != null) {
					sftClient.close();
				}
			}
		}
		log.debug("回退成功...");
		//数据库操作
    	multiOperationDatabase(programGroup,Constant.SWITCH_BACK,dbKey);
    	log.debug("版本切换回退更新表成功...");
		//返回值
		returnMap.put("rstCode", "0");
		return returnMap;
	}
	
	/**
	 * 数据库操作
	 * @param program_code
	 * @param action_flag
	 * @param dbKey
	 */
	private void multiOperationDatabase(String program_code,String action_flag,String dbKey) throws Exception {
		Map<String,String> mapperParam=new HashMap<String,String>();
		mapperParam.put("TOPOLOGY_GROUP", program_code);
		mapperParam.put("ACTION_FLAG", action_flag);
		try {
			List record=coreService.queryForList("masterStandby.queryMasterStandby", mapperParam, dbKey);
			boolean isExist=(record.size()>0)?true:false;
			if(isExist){
				coreService.updateObject("masterStandby.updateMasterStandby", mapperParam, dbKey);
			}else{
				coreService.insertObject("masterStandby.insertMasterStandby", mapperParam, dbKey);
			}
		} catch (Exception e) {
			throw new Exception("数据库操作失败，请检查！失败原因：", e);
		}
	}
}
