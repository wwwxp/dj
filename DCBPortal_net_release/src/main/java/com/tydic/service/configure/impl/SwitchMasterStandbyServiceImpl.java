package com.tydic.service.configure.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
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
import com.tydic.service.configure.SwitchMasterStandbyService;
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
  * @ClassName:    [SwitchMasterStandbyServiceImpl]     
  * @Description:  [切离线功能实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:15:04]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:15:04]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class SwitchMasterStandbyServiceImpl implements SwitchMasterStandbyService {
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(SwitchMasterStandbyServiceImpl.class);

	/**
	 * 设置默认编码
	 */
	private static final String DEFAULT_ENCODE = "UTF-8";
	
	/**
	 * 版本切换节点名
	 */
	private static final String SWITCH_FILE_UP_NODE = "Up";
	
	/**
	 * 灰度升级：网元值获取
	 * @param param
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getNetElement(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("灰度升级， 获取网元信息， 参数: " + param.toString() + ", dbKey: " + dbKey);
		
		String fileSwitch = Constant.FILE_SWITCH;
		String filePath =   URLDecoder.decode(SwitchMasterStandbyServiceImpl.class.getResource("/").getPath(),"UTF-8") +"/ocs_jtopo.xml";
		if(BusinessConstant.PARAMS_BUS_1.equals(fileSwitch)){
			filePath =  Constant.OCS_JTOPO_PATH;
		}
		log.debug("获取网元信息， XML文件路径: " + filePath);
		
		List<Map<String, Object>> netReturnVal=new ArrayList<Map<String, Object>>();
		Document xmlFile=XmlTool.read(new File(filePath));
		Element rootEle=xmlFile.getRootElement();
		Element spEle=rootEle.element("sp");
		Element netsEle=XmlTool.getChildElement(spEle, "nets");
		Iterator<Element> iter = netsEle.elementIterator();
		while(iter.hasNext()) {
			Element childEle = iter.next();
			Map<String, Object> attr = new HashMap<String, Object>();
			attr.put("code", childEle.attribute("id").getValue());
			attr.put("text", childEle.attribute("name").getValue());
			attr.put("ip", childEle.attribute("ip").getValue());
			attr.put("img", childEle.attribute("img").getValue());
			attr.put("dragable", childEle.attribute("dragable").getValue());
			netReturnVal.add(attr);
		}
		log.debug("灰度升级，获取网元信息， 返回结果: " + netReturnVal.toString());
		return netReturnVal;
	}

	/**
	 * 灰度升级：获取sp_switch.xml已有号段信息
	 * @param param
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getExistNumInfo(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("版本切换解析配置文件， 参数: " + param.toString() + ", dbKey: " + dbKey);
		//返回对象
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//灰度升级配置文件名称
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		//包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		Trans trans = null;
		InputStream in = null;
		try {
			//获取sp_switch.xml文件
			FtpDto ftpDto = SessionUtil.getFtpParams();
			String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR + packageType;
			String deployPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessPath) + fileName;
			
			trans  = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			in = trans.get(deployPath);
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
				String topValue=upEles.get(i).attribute("topologyGroup").getValue();
				if(topValue.equals(programGroup)){
					editUpEle=upEles.get(i);
					break;
				}
			}
			//号段
			Element numEle=XmlTool.getChildElement(editUpEle, "serv");
			//网元
			Element netEle=XmlTool.getChildElement(editUpEle, "net");
			//存在号段
			if(numEle!=null){
				List<Map<String,String>> valList=new ArrayList<Map<String,String>>();
				Iterator<Element> iter = editUpEle.elementIterator();
				while(iter.hasNext()) {
					Element childEle = iter.next();
					if(childEle.getName().equals("serv")){
						Map<String,String> attr=new HashMap<String,String>();
						attr.put("busType", childEle.attribute("servtype").getValue());
						attr.put("startNum", childEle.attribute("begin").getValue());
						attr.put("endNum", childEle.attribute("end").getValue());
						attr.put("Current", childEle.attribute("Current").getValue());
						valList.add(attr);
					}
				}
				returnMap.put("numInfo", valList);
			}else if(netEle!=null){//存在网元
				List<String> valList=new ArrayList<String>();
				Iterator<Element> iter = editUpEle.elementIterator();
				while(iter.hasNext()) {
					Element childEle = iter.next();
					if(childEle.getName().equals("net")){
						valList.add(childEle.attribute("netpeer").getValue());
					}
				}
				returnMap.put("netInfo", valList);
			}
		} catch (Exception e) {
			log.error("灰度升级配置文件初始信息获取失败！", e);
			throw new RuntimeException("灰度升级配置文件初始信息获取失败！");
		}finally{
			if(in!=null){
				in.close();
			}
			if(trans!=null){
				trans.close();
			}
		}
		return returnMap;
	}

	/**
	 * 程序操作：灰度升级(改为1， current和next不对调，加子节点,子节点current是父类的next值)
	 * @param param
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateGreyUpgrade(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("灰度升级开始, 业务参数: " + param + ", dbKey: " + dbKey);
		//topology配置文件
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		//包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序组
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		
		log.debug("灰度升级, 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		
		//两种值：num(begin_num/end_num)与ele(net_element)
		String type= StringTool.object2String(param.get("radioType"));
		//号段
		List<Map<String,Object>> seqList = (List<Map<String, Object>>) param.get("numData");
		//网元
		List eleList = (List) param.get("net_element");
		//临时根目录
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		String localTmpPath = webRootPath + Constant.TMP + FileTool.exactPath(StringTool.object2String(System.currentTimeMillis())) + fileName;
		
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
			//获取 sp_switch.xml文件到ftp主机
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
					if (programGroup.equalsIgnoreCase(childEle.attribute("topologyGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该Up节点的topologyGroup属性，请检查!");
			}

			//删除Up所有子节点
			Iterator<Element> iterEle = XmlTool.getIterator(topicUpNodeEle);
			while(iterEle.hasNext()) {
				topicUpNodeEle.remove(iterEle.next());
			}

			//修改Up节点数据
			String currentValue = topicUpNodeEle.attribute("current").getValue();
			String nextValue = topicUpNodeEle.attribute("next").getValue();

			//修改Up子节点数据
			if (BusinessConstant.PARAMS_SWITCH_NUM.equalsIgnoreCase(type)) {
				if (!BlankUtil.isBlank(seqList) && !seqList.isEmpty()) {
					for (int j=0; j<seqList.size(); j++) {
						Map<String, Object> seqMap = seqList.get(j);
						String busType = StringTool.object2String(seqMap.get("busType"));
						String startNum = StringTool.object2String(seqMap.get("startNum"));
						String endNum = StringTool.object2String(seqMap.get("endNum"));
						//循环创建节点
						Element servEle = topicUpNodeEle.addElement("serv");
						servEle.addAttribute("servtype", busType)
						.addAttribute("begin", startNum)
						.addAttribute("end", endNum)
						.addAttribute("Current", nextValue);
					}
				}
			} else if (BusinessConstant.PARAMS_SWITCH_ELE.equalsIgnoreCase(type)) {
				if (!BlankUtil.isBlank(eleList) && !eleList.isEmpty()) {
					for (int j=0; j<eleList.size(); j++) {
						//循环创建节点
						Element netEle = topicUpNodeEle.addElement("net");
						String netValue=StringTool.object2String(eleList.get(j));
						netEle.addAttribute("netpeer",netValue )
						.addAttribute("Current", nextValue);
					}
				}
			}

			topicUpNodeEle.addAttribute("upgradestate",Constant.UPGRADE_STATE_FLAG_0);
			//.addAttribute("current", nextValue).addAttribute("next", currentValue)
			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			 
			//将版本升级配置文件推送到版本发布服务器当前版本包下
			String switchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath)  + FileTool.exactPath(packageType) + fileName;
			bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
			trans.put(bin, switchPath);
			log.debug("版本升级修改配置文件操作， 同步版本发布服务器【" + fileName + "】文件成功...");
			
//			//查询所有的包类型,同步sp_switch.xml文件
//			Map<String, Object> qryParams = new HashMap<String, Object>();
//			qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
//			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
//			for (int j=0; j<ftpList.size(); j++) {
//				String configValue = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
//				
//				//判断包类型下是否存在sp_switch.xml文件，如果不存在则不需要同步上传
////				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue);
////				Vector<FileRecord> fileList = trans.getFileList(switchPath);
////				if (!BlankUtil.isBlank(fileList)) {
////					for (int i=0; i<fileList.size(); i++) {
////						String spSwitchFileName = fileList.get(i).getFileName();
////						String fileType = StringTool.object2String(fileList.get(i).getFileType());
////						if (spSwitchFileName.equalsIgnoreCase(fileName) && "F".equals(fileType)) {
////							String switchName = switchPath + fileName;
////							bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
////							trans.put(bin, switchName);
////							break;
////						}
////					}
////				}
//				String switchPath = FileTool.exactPath(ftpRootPath) + FileTool.exactPath(busformPath)  + FileTool.exactPath(configValue) + fileName;
//				bin = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
//				trans.put(bin, switchPath);
//			}
//			log.debug("灰度升级,同步版本发布服务器sp_switch.xml文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
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
				String remoteFinalPath = FileTool.exactPath(remotePath) + Constant.BUSS + fileName;
				log.debug("灰度升级, 远程部署主机配置文件路径: " + remoteFinalPath);
				
				sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
				sftClient.login();
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("灰度发布【 " + fileName + "】文件修改成功，当前主机:" + hostIp);
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
		//包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序组
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		log.debug("查看版本切换配置文件，业务包类型: " + packageType + ", 程序Group: " + programGroup);
		
		//两种值：num(begin_num/end_num)与ele(net_element)
		String type= StringTool.object2String(param.get("radioType"));
		//号段
		List<Map<String,Object>> seqList = (List<Map<String, Object>>) param.get("numData");
		//网元
		List eleList = (List) param.get("net_element");
		//本地临时保存目录
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
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
		FtpDto ftpDto = SessionUtil.getFtpParams();
		try{
			//获取 sp_switch.xml文件到ftp主机

			String busformPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;

			String deployswitchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(busformPath) + FileTool.exactPath(packageType) + fileName;
			log.debug("部署主机配置文件路径: " + deployswitchPath);
			
			trans = FTPUtils.getFtpInstance(ftpDto);
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
					if (programGroup.equalsIgnoreCase(childEle.attribute("topologyGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该Up节点的topologyGroup属性，请检查!");
			}

			//删除Up所有子节点
			Iterator<Element> iterEle = XmlTool.getIterator(topicUpNodeEle);
			while(iterEle.hasNext()) {
				topicUpNodeEle.remove(iterEle.next());
			}

			//修改Up节点数据
			String currentValue = topicUpNodeEle.attribute("current").getValue();
			String nextValue = topicUpNodeEle.attribute("next").getValue();

			//修改Up子节点数据
			if (BusinessConstant.PARAMS_SWITCH_NUM.equalsIgnoreCase(type)) {
				if (!BlankUtil.isBlank(seqList) && !seqList.isEmpty()) {
					for (int j=0; j<seqList.size(); j++) {
						Map<String, Object> seqMap = seqList.get(j);
						String busType = StringTool.object2String(seqMap.get("busType"));
						String startNum = StringTool.object2String(seqMap.get("startNum"));
						String endNum = StringTool.object2String(seqMap.get("endNum"));
						//循环创建节点
						Element servEle = topicUpNodeEle.addElement("serv");
						servEle.addAttribute("servtype", busType)
						.addAttribute("begin", startNum)
						.addAttribute("end", endNum)
						.addAttribute("Current", nextValue);
					}
				}
			} else if (BusinessConstant.PARAMS_SWITCH_ELE.equalsIgnoreCase(type)) {
				if (!BlankUtil.isBlank(eleList) && !eleList.isEmpty()) {
					for (int j=0; j<eleList.size(); j++) {
						//循环创建节点
						Element netEle = topicUpNodeEle.addElement("net");
						String netValue=StringTool.object2String(eleList.get(j));
						netEle.addAttribute("netpeer",netValue )
						.addAttribute("Current", nextValue);
					}
				}
			}
			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			 
			//将版本升级配置文件推送到版本发布服务器当前版本包下
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
		//灰度升级配置文件名称
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		//业务包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序组
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		log.debug("正式发布, 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		//本地临时保存目录
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		String localTmpPath = webRootPath + Constant.TMP + System.currentTimeMillis() + "/" + fileName;
		
		//获取集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}
		
		//操作远程主机
		Trans trans = null;
		String docStr = null;
		InputStream bin = null;
		FtpDto ftpDto = SessionUtil.getFtpParams();
		try{
        	
    		//将sp_switch.xml文件 到ftp主机
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
					if (programGroup.equalsIgnoreCase(childEle.attribute("topologyGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}

			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该up节点的topologyGroup属性，请检查!");
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
			.addAttribute("upgradestate",Constant.UPGRADE_STATE_FLAG_1 );

			//将DOM文件写入到远程主机
			docStr = XmlTool.xmltoString(doc, DEFAULT_ENCODE);
			
			//将版本升级配置文件推送到版本发布服务器当前版本包下
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
				log.debug("正式发布配置文件分发路径: " + remoteFinalPath);
				
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remoteFinalPath);
				log.debug("正式发布【" + fileName + "】文件修改成功,当前主机IP:" + hostIp);
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
		//灰度升级配置文件名称
		String fileName= StringTool.object2String(param.get("SWITCH_CONFIG_FILE"));
		//版本切换集群类型
		String switchClusterType = StringTool.object2String(param.get("SWITCH_CLUSTER_TYPE"));
		//业务主集群ID
		String busClusterId = StringTool.object2String(param.get("BUS_CLUSTER_ID"));
		//业务包类型
		String packageType = StringTool.object2String(param.get("PACKAGE_TYPE"));
		//程序信息
		String programGroup = StringTool.object2String(param.get("PROGRAM_GROUP"));
		log.debug("版本回退, 业务包类型: " + packageType + ", 程序Group: " + programGroup);
		//本地临时保存目录
		String webRootPath = StringTool.object2String(param.get("webRootPath"));
		String localTmpPath = webRootPath + Constant.TMP + FileTool.exactPath(StringTool.object2String(System.currentTimeMillis())) + fileName;
		
		//获取集群主机列表
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("CLUSTER_TYPE", ComParamsHelper.revertStr(switchClusterType));
		queryParams.put("BUS_CLUSTER_ID", busClusterId);
		List<HashMap<String,Object>> switchHostList = coreService.queryForList2New("deployHome.querySwitchDeployHostList", queryParams, dbKey);
		if (BlankUtil.isBlank(switchHostList)) {
			throw new RuntimeException("版本切换需要分发的主机不存在,请检查!");
		}
		
		//操作远程主机
		Trans trans = null;
		String docStr = null;
		InputStream bin = null;
		String currentValue = "";
		String nextValue = "";
		FtpDto ftpDto = SessionUtil.getFtpParams();
		try{

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
					if (programGroup.equalsIgnoreCase(childEle.attribute("topologyGroup").getValue())) {
						topicUpNodeEle = childEle;
						break;
					}
				}
			}
			if (BlankUtil.isBlank(topicUpNodeEle)) {
				throw new RuntimeException("配置文件上不存在该up节点的topologyGroup属性，请检查!");
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
			
			//将版本升级配置文件推送到版本发布服务器当前版本包下
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
//			log.debug("同步版本发布服务器" + fileName + "文件成功， 包类型数量: " + (ftpList == null ? 0 : ftpList.size()));
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
				log.debug("版本回退【 " + fileName + "】文件修改成功，当前修改主机ip:" + hostIp);
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
