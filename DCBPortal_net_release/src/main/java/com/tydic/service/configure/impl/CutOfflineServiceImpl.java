package com.tydic.service.configure.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;

import com.tydic.bean.FtpDto;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.configure.CutOfflineService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.Constant;
import com.tydic.util.FileUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import com.tydic.util.XmlTool;
import com.tydic.util.ftp.FTPUtils;
import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.FileTool;
import com.tydic.util.ftp.Trans;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.configure.impl]    
  * @ClassName:    [CutOfflineServiceImpl]     
  * @Description:  [切离线实现类]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:08:38]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:08:38]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
public class CutOfflineServiceImpl implements CutOfflineService {
	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(CutOfflineServiceImpl.class);

	/**
	 * 设置默认编码
	 */
	private static final String DEFAULT_ENCODE = "UTF-8";
	
	
	/**
	 * serv切离线值
	 */
	public static String SERV_FLAG = "2";
	
	/**
	 * net切离线值
	 */
	public static String NET_FLAG = "1";
	
	/**
	 * serv不切离线值
	 */
	public static String NOT_FLAG = "0";
	
	//切离线
	public static final String PARAMS_SWITCH_CUT = "cut";
	public static final String PARAMS_SWITCH_NOT_CUT = "notcut";

	/**
	 * 获取sp_switch.xml已有号段、网元信息
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getExistNumOrNetInfo(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("切离线获取网元信息, 参数: " + param.toString() + ", dbKey: " + dbKey);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Trans trans = null;
		InputStream in=null;
		try {
			//获取sp_switch.xml文件
			FtpDto ftpDto = SessionUtil.getFtpParams();

			String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
			String fileName= Constant.SP_SWITCH;
			
			trans  = FTPUtils.getFtpInstance(ftpDto);
			trans.login();
			
			//获取部署了route类型的包类型
			String packageType = "";
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
			List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", queryMap, dbKey);
			for (int i=0; i<ftpList.size(); i++) {
				String configValue = StringTool.object2String(ftpList.get(i).get("CONFIG_VALUE"));
				String spSwitchPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessPath) + FileTool.exactPath(configValue);
				if(trans.isExistPath(spSwitchPath)) {
					Vector<FileRecord> fileList = trans.getFileList(spSwitchPath);
					for (int j=0; j<fileList.size(); j++) {
						if (fileName.equals(fileList.get(j).getFileName())) {
							packageType = configValue;
							break;
						}
					}
				}
				if (!BlankUtil.isBlank(packageType)) {
					break;
				}
			}
			
			
			//部署主机sp_switch.xml文件路径
			String deployPath = ftpDto.getFtpRootPath() + FileTool.exactPath(businessPath) + FileTool.exactPath(packageType) + fileName;
			log.debug("获取远程sp_switch.xml文件信息， 文件路径: " + deployPath);
			
			in = trans.get(deployPath);
			Document xmlFile=XmlTool.read(in,DEFAULT_ENCODE);
			in.close();
			trans.completePendingCommand();
			Element rootEle=xmlFile.getRootElement();
			Element offEle=rootEle.element("offline");
			if(BlankUtil.isBlank(offEle)){
				return returnMap;
				//throw new Exception("查找配置文件offline节点失败或不存在该节点，请检查文件！");
			}

			//号段
			Element numEle=XmlTool.getChildElement(offEle, "serv");
			//网元
			Element netEle=XmlTool.getChildElement(offEle, "net");
			//存在号段
			if(!BlankUtil.isBlank(numEle)){
				boolean numState=false;
				List<Map<String,String>> valList=new ArrayList<Map<String,String>>();
				Iterator<Element> iter = offEle.elementIterator();
				while(iter.hasNext()) {
					Element childEle = iter.next();
					if(childEle.getName().equals("serv")){
						Map<String,String> attr=new HashMap<String,String>();
						attr.put("busType", childEle.attribute("servtype").getValue());
						attr.put("startNum", childEle.attribute("begin").getValue());
						attr.put("endNum", childEle.attribute("end").getValue());
						attr.put("flag", childEle.attribute("flag").getValue());
						valList.add(attr);

						if(!childEle.attribute("flag").getValue().equalsIgnoreCase(NOT_FLAG)){
							numState=true;
						}
					}
				}
				returnMap.put("numState", numState);
				returnMap.put("numInfo", valList);
			}

			if(!BlankUtil.isBlank(netEle)){//存在网元
				boolean netState=false;
				List<String> valList=new ArrayList<String>();
				Iterator<Element> iter = offEle.elementIterator();
				while(iter.hasNext()) {
					Element childEle = iter.next();
					if(childEle.getName().equals("net")){
						valList.add(childEle.attribute("netpeer").getValue());
						if(!childEle.attribute("flag").getValue().equalsIgnoreCase(NOT_FLAG)){
							netState=true;
						}
					}
				}
				returnMap.put("netState",netState );
				returnMap.put("netInfo", valList);
			}
		} catch (Exception e) {
			log.error("切离线配置文件初始信息获取失败, 失败信息: ", e);
			throw new RuntimeException("切离线配置文件初始信息获取失败！");
		}finally{
			if(in!=null){
				in.close();
				if (trans != null) {
					trans.completePendingCommand();
				}
			}
			if(trans!=null){
				trans.close();
			}
		}
		return returnMap;
	}


	/**
	 * 操作：切离线(改flag---->按照业务+号段切:2;按照网元切:1)/不切离线(改flag---->0)
	 * @param param
	 * @param dbKey
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updateCutOffline(Map<String, Object> param, String dbKey) throws Exception {
		log.debug("切离线/不切离线开始, 业务参数: " + param + ", dbKey: " + dbKey);

		//两种值：num(busType/begin_num/end_num)与ele(net_element)
		String radioType= StringTool.object2String(param.get("radioType"));
		//号段
		List<Map<String,Object>> seqList = (List<Map<String, Object>>) param.get("numData");
		//网元
		List<String> element = (List<String>)param.get("net_element");
		//操作类型：切离线或者不切离线
		String type= StringTool.object2String(param.get("type"));
		
		//打日志用参数
		String returnType="";
		if(PARAMS_SWITCH_CUT.equals(type)){
			returnType="切离线";
		}else if(PARAMS_SWITCH_NOT_CUT.equals(type)){
			returnType="不切离线";
		}

		//获取sp_switch.xml文件
		String fileName= Constant.SP_SWITCH;
		Document xmlFile;

		//拿route主机
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put("CLUSTER_TYPE", "'route'");
		List<HashMap<String,Object>> routeHostList = coreService.queryForList2New("host.queryRouteHostByCluster", queryMap, dbKey);
		if (BlankUtil.isBlank(routeHostList)) {
			throw new RuntimeException(returnType+":查询路由主机失败或无已部署路由主机,请检查!");
		}

		Trans sftClient = null;
		Trans trans = null;
		InputStream in = null;
		InputStream inputStream = null;
		Boolean isUpdated = Boolean.FALSE;
		//修改后保存到路由主机
		for (int i=0; i<routeHostList.size(); i++) {
			try {
				Map<String, Object> singleHost = routeHostList.get(i);
				String hostIp = StringTool.object2String(singleHost.get("HOST_IP"));
				String sshUser = StringTool.object2String(singleHost.get("SSH_USER"));
				String sshPwd = DesTool.dec(StringTool.object2String(singleHost.get("SSH_PASSWD")));
				
				//当前集群部署根目录
				String deployPath = StringTool.object2String(singleHost.get("CLUSTER_DEPLOY_PATH"));
				String remotePath = FileTool.exactPath(deployPath) + Constant.BUSS + fileName;
				try {
					sftClient = FTPUtils.getFtpInstance(hostIp, sshUser, sshPwd, SessionUtil.getConfigValue("FTP_TYPE"));
					sftClient.login();
					in = sftClient.get(remotePath);
				} catch (Exception e) {
					throw new RuntimeException(returnType+"：主机【"+hostIp+"】查找配置文件失败或不存在该文件，请检查！");
				}

				xmlFile = XmlTool.read(in, DEFAULT_ENCODE);
				in.close();
				sftClient.completePendingCommand();
				
				Element rootEle=xmlFile.getRootElement();
				Element offEle=rootEle.element("offline");
				if(BlankUtil.isBlank(offEle)){
					//throw new RuntimeException(returnType+"：主机【"+hostIp+"】查找配置文件offline节点失败或不存在该节点，请检查文件！");
					//如果不存在该节点，则自己创建， 不往外抛
					offEle = rootEle.addElement("offline");
				}

				//开始修改文件
				String notFlag = NOT_FLAG;
				//修改offline子节点数据
				if (BusinessConstant.PARAMS_SWITCH_NUM.equalsIgnoreCase(radioType)) {//号段
					Iterator<Element> iterEle = XmlTool.getIterator(offEle);
					while(iterEle.hasNext()) {
						Element childEle=iterEle.next();
						if(childEle.getName().equalsIgnoreCase("serv")){
							//删除serv节点
							offEle.remove(childEle);
						}else{
							//修改net的flag为0
							childEle.addAttribute("flag", notFlag);
						}
					}
					if (!BlankUtil.isBlank(seqList) && !seqList.isEmpty()) {
						for (int j=0; j<seqList.size(); j++) {
							Map<String, Object> seqMap = seqList.get(j);
							String busType = StringTool.object2String(seqMap.get("busType"));
							String startNum = StringTool.object2String(seqMap.get("startNum"));
							String endNum = StringTool.object2String(seqMap.get("endNum"));
							String servFlag = SERV_FLAG;

							//循环创建节点
							Element servEle = offEle.addElement("serv");
							//不切
							if(PARAMS_SWITCH_NOT_CUT.equalsIgnoreCase(type)){
								servEle.addAttribute("servtype", busType)
								.addAttribute("begin", startNum)
								.addAttribute("end", endNum)
								.addAttribute("flag", notFlag);
							} else if (PARAMS_SWITCH_CUT.equalsIgnoreCase(type)){//切离线
								servEle.addAttribute("servtype", busType)
								.addAttribute("begin", startNum)
								.addAttribute("end", endNum)
								.addAttribute("flag", servFlag);
							}
						}
					}
				}else if (BusinessConstant.PARAMS_SWITCH_ELE.equalsIgnoreCase(radioType)) {//网元
					Iterator<Element> iterEle = XmlTool.getIterator(offEle);
					while(iterEle.hasNext()) {
						Element childEle=iterEle.next();
						if(childEle.getName().equalsIgnoreCase("net")){
							//删除net节点
							offEle.remove(childEle);
						}else{
							//修改serv的flag为0
							childEle.addAttribute("flag", notFlag);
						}
					}
					String netFlag = NET_FLAG;
					if (!BlankUtil.isBlank(element) && !element.isEmpty()) {
						for (int j=0; j<element.size(); j++) {
							String netValue = element.get(j);
							//循环创建节点
							Element netEle = offEle.addElement("net");

							if(PARAMS_SWITCH_NOT_CUT.equalsIgnoreCase(type)){
								netEle.addAttribute("netpeer", netValue)
								.addAttribute("flag", notFlag);
							} else if (PARAMS_SWITCH_CUT.equalsIgnoreCase(type)){//切离线
								netEle.addAttribute("netpeer", netValue)
								.addAttribute("flag", netFlag);
							}
						}
					}
				} else {
					throw new RuntimeException(returnType+"：网元/号段类型识别错误，请检查！");
				}

				//将DOM文件写入到远程主机
				String docStr = XmlTool.xmltoString(xmlFile, DEFAULT_ENCODE);
				inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
				sftClient.put(inputStream, remotePath);
				log.debug(returnType+"：ftp主机【sp_switch.xml】文件修改成功，ftp主机："+hostIp);

				//保存到文件服务器
				if (!isUpdated) {
					
					isUpdated = Boolean.TRUE;
					//将sp_switch.xml文件同步到ftp主机
					//获取文件服务器相关信息
					String rootPath=SessionUtil.getConfigValue("FTP_ROOT_PATH");
					String businessPath = Constant.CONF + Constant.BUSS_CONF + Constant.RELEASE_DIR;
					
					//部署主机信息
					FtpDto ftpDto = SessionUtil.getFtpParams();

					trans  = FTPUtils.getFtpInstance(ftpDto);
					trans.login();
					
					//查询所有的包类型
					Map<String, Object> qryParams = new HashMap<String, Object>();
					qryParams.put("GROUP_CODE", "WEB_BUS_PACKAGE_TYPE");
					List<HashMap<String, Object>> ftpList = coreService.queryForList2New("config.queryConfigList", qryParams, dbKey);
					for (int j=0; j<ftpList.size(); j++) {
						String packageType = StringTool.object2String(ftpList.get(j).get("CONFIG_VALUE"));
						String spSwitchPath = FileTool.exactPath(rootPath) + FileTool.exactPath(businessPath) + FileTool.exactPath(packageType);
					
						boolean isExistSpSwitch = Boolean.FALSE;
						if(trans.isExistPath(spSwitchPath)) {
							Vector<FileRecord> fileList = trans.getFileList(spSwitchPath);
							for (int k=0; k<fileList.size(); k++) {
								String spFileName = fileList.get(k).getFileName();
								if (fileName.equals(spFileName)) {
									isExistSpSwitch = Boolean.TRUE;
									break;
								}
							}
						}
						
						if (isExistSpSwitch) {
							//部署主机sp_switch.xml配置文件路径
							String filePath = FileTool.exactPath(rootPath) + FileTool.exactPath(businessPath) 
									+ FileTool.exactPath(packageType) + fileName;
							//XML文件对象流
							inputStream = new ByteArrayInputStream(docStr.getBytes(DEFAULT_ENCODE));
							
							trans.put(inputStream, filePath);
							log.debug(returnType+":ftp主机【sp_switch.xml】文件修改成功，ftp主机：" + ftpDto.getHostIp() + ", sp_switch.xml文件路径: " + filePath);
						}
					}
				}
			} catch (RuntimeException e) {
				log.error(returnType + "异常， 异常信息: ", e);
				throw e;
			} catch (Exception e) {
				log.error(returnType+"：修改Topology配置文件失败");
				throw new Exception(returnType+"：修改Topology配置文件失败！");
			}finally{
				if (in != null) {
					in.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (trans != null) {
					trans.close();
				}
				if(sftClient!=null){
					sftClient.close();
				}
			}
		}

		log.debug(returnType+"成功！");
		//返回值
		Map<String,Object> returnMap=new HashMap<String,Object>();
		returnMap.put("rstCode", "0");
		return returnMap;
	}
	
	public static void main(String[] args) throws Exception {
		File file = new File("D:\\a.xml");
		InputStream in = new FileInputStream(file);
		Document xmlFile = XmlTool.read(in,DEFAULT_ENCODE);
		Element rootEle=xmlFile.getRootElement();
		Element offEle=rootEle.element("offline");
		if(BlankUtil.isBlank(offEle)){
			 
			//如果不存在该节点，则自己创建， 不往外抛
			offEle = rootEle.addElement("offline");
		}
		Iterator<Element> iterEle = XmlTool.getIterator(offEle);
		while(iterEle.hasNext()) {
			Element childEle=iterEle.next();
			if(childEle.getName().equalsIgnoreCase("serv")){
				//删除serv节点
				offEle.remove(childEle);
			}else{
				//修改net的flag为0
				childEle.addAttribute("flag", "0");
			}
		}
		Element servEle = offEle.addElement("serv");
		servEle.addAttribute("servtype", "0");
		servEle.addAttribute("begin", "111111");
		servEle.addAttribute("end", "222222");
		//将DOM文件写入到远程主机
		String docStr = XmlTool.xmltoString(xmlFile, DEFAULT_ENCODE);
		FileUtil.writeFile("D:\\a.xml", docStr,false);
	}
}
