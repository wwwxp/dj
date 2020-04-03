package com.tydic.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bean.ClusterNodeDto;
import com.tydic.bean.ClusterNodeQuotaDto;
import com.tydic.bp.common.utils.tools.BlankUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.util]    
  * @ClassName:    [HttpClientUtil]     
  * @Description:  [发送POST请求，目前该类主要用来获取主机资源信息]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-6 下午4:52:38]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-6 下午4:52:38]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
public class HttpClientUtil {
	
	private static Logger logger = Logger.getLogger(HttpClientUtil.class);
	
	public static void main(String[] args) {
//		//查询ip数组
//		String[] ips = new String[]{"192.168.161.12,192.168.161.13,192.168.161.14"};
//		//最新5个批次数据
//		String batchNoCount = "5";
//		//调用http接口查询
//		String response = postHttp("http://192.168.161.14:8088/bmp/hostInfo/getMonitorInfo.action",ips,batchNoCount);
//		System.out.println("response->"+response);
//		List<Map<String,Object>> resultList= (List<Map<String,Object>>)JSONArray.parse(response);
//		if(resultList.size()>0){
//			for (Map<String, Object> hostMap : resultList) {
//				if("200".equals(hostMap.get("code"))){
//					List<Map<String,Object>> hostDetailList = (List<Map<String,Object>>)JSONArray.parse(hostMap.get("result").toString());
//					if(hostDetailList != null && hostDetailList.size()>0){
//						for (Map<String, Object> hostDetail : hostDetailList) {
//							System.out.println("batchNo："+hostDetail.get("batchNo")+" IP："+hostDetail.get("ip")+" CPU使用率（%）："+hostDetail.get("cpuRateSum")
//							+" 磁盘使用率（%）："+hostDetail.get("diskTotalRate")+ " 内存使用率（%）："+hostDetail.get("memoryRateSum")
//							+" 网络输出（M）："+hostDetail.get("networkOutRateSum")+" 网络输入（M）："+hostDetail.get("networkInRateSum"));
//						}
//					}
//				
//				}
//			}
//		}
		
		HttpClientUtil t = new HttpClientUtil();
		t.test();
	}
	public void test(){
		//查询ip数组
		String[] ips = new String[]{"192.168.161.12,192.168.161.14"};
		//最近30天数据
		String day = "30";
		//GTH:采预中心吞吐量 RATE:批价中心吞吐量
		String busiType="RATE";
		NameValuePair valueParam1 = new NameValuePair("day",day);
		NameValuePair valueParam2 = new NameValuePair("busiType",busiType);
		NameValuePair[] nameValuePairArr = new NameValuePair[]{valueParam1,valueParam2};
		//调用http接口查询
		String response = postHttp("http://192.168.161.14:8088/bmp/hostInfo/getMonitorMaxInfo.action",ips,nameValuePairArr);
		System.out.println("response->"+response);
		JSONObject resultMap = (JSONObject)JSONObject.parse(response);
		if(resultMap != null && resultMap.size()>0){
			System.out.println("业务量："+resultMap.get("busiValue"));
			List<Map<String,Object>> resultList= (List<Map<String,Object>>)resultMap.get("hostInfo");
			if(resultList != null && resultList.size()>0){
				for (Map<String, Object> hostMap : resultList) {
						System.out.println(hostMap);
					
				}
			}
		}
	}
	
	/**
	 * 获取主机资源信息
	 * @param nodeDto
	 * @param conditionCount 获取连续几次
	 */
	public static void getRule(String url ,ClusterNodeDto nodeDto,String conditionCount){
		//获取部署主机资源信息
		List<ClusterNodeQuotaDto> hostQuotaList = new ArrayList<ClusterNodeQuotaDto>();
		String[] array = new String[nodeDto.getDeployHostIPArray().size() - 1];
		String [] ips = nodeDto.getDeployHostIPArray().toArray(array);
		
		String responseStr = HttpClientUtil.postHttp(url, ips, conditionCount);
		nodeDto.setRuleStr(responseStr);
		try {
			List<Map<String,Object>> resultList= (List<Map<String,Object>>)JSONArray.parse(responseStr);
			if(BlankUtil.isBlank(responseStr)){
				throw new RuntimeException("获取集群节点指标信息失败");
			}
			if (!BlankUtil.isBlank(resultList)) {
				for (Map<String, Object> hostMap : resultList) {
					if("200".equals(hostMap.get("code"))){
						List<Map<String,Object>> hostDetailList = (List<Map<String,Object>>)JSONArray.parse(hostMap.get("result").toString());
						if (!BlankUtil.isBlank(hostDetailList)) {
							for (Map<String, Object> hostDetail : hostDetailList) {
								String hostIp = StringTool.object2String(hostDetail.get("ip"));
								String batchNo = StringTool.object2String(hostDetail.get("batchNo"));
								double cpuRateSum = Double.parseDouble(StringTool.object2String(hostDetail.get("cpuRateSum")));
								double diskTotalRate = Double.parseDouble(StringTool.object2String(hostDetail.get("diskTotalRate")));
								double memoryRateSum = Double.parseDouble(StringTool.object2String(hostDetail.get("memoryRateSum")));
								double networkOutRateSum = Double.parseDouble(StringTool.object2String(hostDetail.get("networkOutRateSum")));
								double networkInRateSum = Double.parseDouble(StringTool.object2String(hostDetail.get("networkInRateSum")));
								ClusterNodeQuotaDto nodeQuotaDto = new ClusterNodeQuotaDto(batchNo, hostIp, cpuRateSum, diskTotalRate, memoryRateSum, networkOutRateSum, networkInRateSum);
								hostQuotaList.add(nodeQuotaDto);
							}
						}
					}else{
						throw new RuntimeException("指标获取失败，非200");
					}
				}
				nodeDto.setRuleList(hostQuotaList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		/*	ClusterNodeLogDto logDto = new ClusterNodeLogDto(nodeDto.getStrategyId(), BusinessConstant.PARAMS_BUS_1, hosts, null, "获取集群节点指标信息失败,任务结束", BusinessConstant.PARAMS_BUS_0);
			this.addClusterNodeLog(logDto);*/
			throw new RuntimeException(e);
		}
		
	}
	/**
	 * 判断 是否触发规则 
	 * @return
	 */
	public static boolean isKTrigger(List<HashMap<String, Object>> ruleList,ClusterNodeDto nodeDto,String msgFlag){
		
		 
       if(ruleList !=null && ruleList.size() > 0){
			
			String conditionCount = StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT"));
			//连续次数
			if (BlankUtil.isBlank(conditionCount)) {
				conditionCount = BusinessConstant.PARAMS_BUS_5;
			}
			
			boolean isTrigger = false;
			for(int i = 0 ; i < ruleList.size() ;i++){
				//指标类型 1：CPU   2：内存      3：磁盘      4：网络
				String quotaType = StringTool.object2String(ruleList.get(i).get("QUOTA_TYPE"));
				//条件类型    1:集群主机资源最大>=    2:集群主机平均资源占比>=  
				String conditionParams = StringTool.object2String(ruleList.get(i).get("CONDITION_PARAM"));
				//条件对应的值
				String conditionValue = StringTool.object2String(ruleList.get(i).get("CONDITION_VALUE"));
				
				Double conditionNum = Double.parseDouble(StringTool.object2String(conditionValue));
				
				String triggerValue = "";
				String hostIps = "";
				if (conditionParams.equals(BusinessConstant.PARAMS_BUS_1)) {
					List<HashMap<String, Object>> minQuotaList =  getMaxQuotaValue(nodeDto.getRuleList(), quotaType);
					for (HashMap<String, Object> quotaMap : minQuotaList) {
						hostIps = StringTool.object2String(quotaMap.get("HOST_IP"));
						triggerValue = StringTool.object2String(quotaMap.get("MIN_VALUE"));
						if(quotaType.equals(BusinessConstant.PARAMS_BUS_4)){
							
							String [] tValues = triggerValue.split("-");
							if(Double.parseDouble(tValues[0]) >= conditionNum ||
									Double.parseDouble(tValues[1]) >= conditionNum){
								isTrigger = true;
								break;
							}
						}else{
							
							if (Double.parseDouble(triggerValue) >= conditionNum) {
								isTrigger = true;
								break;
							}
						}
					}
				}else{
					 
					List<HashMap<String, Object>> minQuotaList =  getAvgQuotaValue(nodeDto.getRuleList(), quotaType);
					for (HashMap<String, Object> quotaMap : minQuotaList) {
						triggerValue = StringTool.object2String(quotaMap.get("AGV_VALUE"));
						hostIps = StringTool.object2String(quotaMap.get("HOST_IP"));
						if (Double.parseDouble(triggerValue) >= conditionNum) {
							isTrigger = true;
							break;
						}
					}
				}
				if(isTrigger){
				    nodeDto.setMsg( "触发机器:"+hostIps+"," + getQuotaTypeName(quotaType) + ":" + getExpendConditionParams(conditionParams) + "" + conditionValue + "%, 并连续触发: " + conditionCount  + "次, 真实使用率: " + triggerValue + "%<br/>" );
				    break;
				}
				
			}
			if (isTrigger) {
				return true;
			} else {
				nodeDto.setMsg("未达预警值," +msgFlag);
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * 判断 是否触发规则 
	 * @return
	 */
	public static boolean isSTrigger(List<HashMap<String, Object>> ruleList,ClusterNodeDto nodeDto,String msgFalg){
		
       if(ruleList !=null && ruleList.size() > 0){
			
			String conditionCount = StringTool.object2String(ruleList.get(0).get("CONDITION_COUNT"));
			//连续次数
			if (BlankUtil.isBlank(conditionCount)) {
				conditionCount = BusinessConstant.PARAMS_BUS_5;
			}
			
			boolean isTrigger = false;
			for(int i = 0 ; i < ruleList.size() ;i++){
				//指标类型 1：CPU   2：内存      3：磁盘      4：网络
				String quotaType = StringTool.object2String(ruleList.get(i).get("QUOTA_TYPE"));
				//条件类型    1:集群主机资源最大>=    2:集群主机平均资源占比>=  
				String conditionParams = StringTool.object2String(ruleList.get(i).get("CONDITION_PARAM"));
				//条件对应的值
				String conditionValue = StringTool.object2String(ruleList.get(i).get("CONDITION_VALUE"));
				
				Double conditionNum = Double.parseDouble(StringTool.object2String(conditionValue));
				
				String triggerValue = "";
				String hostIps = "";
				if (conditionParams.equals(BusinessConstant.PARAMS_BUS_1)) {
					List<HashMap<String, Object>> minQuotaList =  getMaxQuotaValue(nodeDto.getRuleList(), quotaType);
					for (HashMap<String, Object> quotaMap : minQuotaList) {
						triggerValue = StringTool.object2String(quotaMap.get("MAX_VALUE"));
						hostIps = StringTool.object2String(quotaMap.get("HOST_IP"));
						if(quotaType.equals(BusinessConstant.PARAMS_BUS_4)){
							
							String [] tValues = triggerValue.split("-");
							if(Double.parseDouble(tValues[0]) <= conditionNum ||
									Double.parseDouble(tValues[1]) <= conditionNum){
								isTrigger = true;
								break;
							}
						}else{
							if (Double.parseDouble(triggerValue) <= conditionNum) {
								isTrigger = true;
								
								break;
							}
						}
					}
				}else{
					 
					List<HashMap<String, Object>> minQuotaList =  getAvgQuotaValue(nodeDto.getRuleList(), quotaType);
					for (HashMap<String, Object> quotaMap : minQuotaList) {
						triggerValue = StringTool.object2String(quotaMap.get("AGV_VALUE"));
						hostIps = StringTool.object2String(quotaMap.get("HOST_IP"));
						if (Double.parseDouble(triggerValue) <= conditionNum) {
							isTrigger = true;
							break;
						}
					}
				}
				if(isTrigger){
					  nodeDto.setMsg( "触发机器:"+hostIps+"," + getQuotaTypeName(quotaType) + ":" + getUnexpendConditionParams(conditionParams) + "" + conditionValue + "%, 并连续触发:" + conditionCount  + "次, 真实使用率: " + triggerValue + "%<br/>") ;
				    break;
				}
				
			}
			if (isTrigger) {
				return true;
			} else {
				nodeDto.setMsg("未达预警值,"+msgFalg);
				return false;
			}
			
		}else{
			return false;
		}
		
	}
	
	/**
	 * {id:1, code:'1', text:'集群主机资源>=', order:1, type:"condition_param_expend"},
	 * {id:2, code:'2', text:'集群主机平均资源占比>=', order:2, type:"condition_param_expend"},
	 * {id:3, code:'3', text:'集群主机资源都>=', order:3, type:"condition_param_expend"}, 
	 * @param conditionParams
	 * @return
	 */
	public static String getExpendConditionParams(String conditionParams) {
		String conditionStr = "";
		switch(conditionParams) {
			case BusinessConstant.PARAMS_BUS_1:
				conditionStr = "集群主机资源>=";
				break;
			case BusinessConstant.PARAMS_BUS_2:
				conditionStr = "集群主机平均资源>=";
				break;
			case BusinessConstant.PARAMS_BUS_3:
				conditionStr = "集群主机资源都>=";
				break;
			default :
				break;
		}
		return conditionStr;
	}
	
	public static String getUnexpendConditionParams(String conditionParams) {
		String conditionStr = "";
		switch(conditionParams) {
			case BusinessConstant.PARAMS_BUS_1:
				conditionStr = "集群主机资源<=";
				break;
			case BusinessConstant.PARAMS_BUS_2:
				conditionStr = "集群主机平均资源<=";
				break;
			case BusinessConstant.PARAMS_BUS_3:
				conditionStr = "集群主机资源都<=";
				break;
			default :
				break;
		}
		return conditionStr;
	}
	
	/**
	 * 获取每个节点连续几次指标平均值
	 * @param hostQuotaList
	 * @param quotaType
	 */
	public static List<HashMap<String, Object>> getAvgQuotaValue(List<ClusterNodeQuotaDto> hostQuotaList, final String quotaType) {
		HashSet<String> hostSet = new HashSet<String>();
		for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
			hostSet.add(nodeDto.getHostIp());
		}
		List<HashMap<String, Object>> retList = new ArrayList<HashMap<String, Object>>();
		for (String hostIp : hostSet) {
			HashMap<String, Object> hostMap = new HashMap<String, Object>();
			List<ClusterNodeQuotaDto> hostList = new ArrayList<ClusterNodeQuotaDto>();
			hostMap.put("HOST_IP", hostIp);
			for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
				if (nodeDto.getHostIp().equals(hostIp)) {
					hostList.add(nodeDto);
				}
			}
			hostMap.put("HOST_LIST", hostList);
			retList.add(hostMap);
		}
		
		//获取每个节点在连续的几次指标平均值
		if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				double totalCpu = 0.0;
				for (ClusterNodeQuotaDto nodeDto : hostNodeList) {
					totalCpu += nodeDto.getCpuRateSum();
				}
				hashMap.put("AGV_VALUE", totalCpu/hostNodeList.size());
			}
		} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				double totalMem = 0.0;
				for (ClusterNodeQuotaDto nodeDto : hostNodeList) {
					totalMem += nodeDto.getMemoryRateSum();
				}
				hashMap.put("AGV_VALUE", totalMem/hostNodeList.size());
			}
		} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				double totalDisk = 0.0;
				for (ClusterNodeQuotaDto nodeDto : hostNodeList) {
					totalDisk += nodeDto.getDiskTotalRate();
				}
				hashMap.put("AGV_VALUE", totalDisk/hostNodeList.size());
			}
		} 
		else if (BusinessConstant.PARAMS_BUS_4.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				double totalNetWork = 0.0;
				for (ClusterNodeQuotaDto nodeDto : hostNodeList) {
					totalNetWork += nodeDto.getNetworkInRateSum() + nodeDto.getNetworkOutRateSum();
				}
				hashMap.put("AGV_VALUE", (totalNetWork/2)/hostNodeList.size());
			}
		} 
		return retList;
	}
	
	
	/**
	 * post方式
	 * @param url
	 * @param code
	 * @param batchCount
	 * @return
	 */
	public static String postHttp(String url,String[] code,String batchCount) {
		logger.debug("获取指标数据，URL: " + url + "， 主机IP: " + code.toString() + ", 次数: " + batchCount);
		String responseMsg = "";
		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
		httpClient.getParams().setContentCharset("GBK");
		PostMethod postMethod = new PostMethod(url);
		for (int i = 0; i < code.length; i++) {
			postMethod.addParameter("ips", code[i]);
		}
		postMethod.addParameter("batchCount",batchCount);
		try {
			httpClient.executeMethod(postMethod);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = postMethod.getResponseBodyAsStream();
			int len = 0;
			byte[] buf = new byte[1024];
			while((len=in.read(buf))!=-1){
				out.write(buf, 0, len);
			}
			responseMsg = out.toString("UTF-8");
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		logger.debug("获取指标数据，返回结果: " + responseMsg);
		return responseMsg;
	}
	
	/**
	 * post方式
	 * @param url
	 * @param code
	 * @param nameValuePairArr
	 * @return
	 */
	public static String postHttp(String url,String[] code,NameValuePair[] nameValuePairArr) {
		String responseMsg = "";
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("GBK");
		PostMethod postMethod = new PostMethod(url);
		for (int i = 0; i < code.length; i++) {
			postMethod.addParameter("ips", code[i]);
		}
		postMethod.addParameters(nameValuePairArr);
		try {
			httpClient.executeMethod(postMethod);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = postMethod.getResponseBodyAsStream();
			int len = 0;
			byte[] buf = new byte[1024];
			while((len=in.read(buf))!=-1){
				out.write(buf, 0, len);
			}
			responseMsg = out.toString("UTF-8");
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		return responseMsg;
	
}

	
	/**
	 * 获取连续批次中最小值
	 * @param hostQuotaList
	 * @param quotaType
	 */
	public static List<HashMap<String, Object>> getMaxQuotaValue(List<ClusterNodeQuotaDto> hostQuotaList, final String quotaType) {
		Collections.sort(hostQuotaList, new Comparator<ClusterNodeQuotaDto>() {
			@Override
			public int compare(ClusterNodeQuotaDto quotaDto1, ClusterNodeQuotaDto quotaDto2) {
				if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {
					if(quotaDto1.getCpuRateSum() <= quotaDto2.getCpuRateSum()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {
					if(quotaDto1.getMemoryRateSum() <= quotaDto2.getMemoryRateSum()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {
					if(quotaDto1.getDiskTotalRate() <= quotaDto2.getDiskTotalRate()){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_4.equals(quotaType)) {
					if((quotaDto1.getNetworkInRateSum() + quotaDto1.getNetworkOutRateSum()) <= (quotaDto2.getNetworkInRateSum() + quotaDto2.getNetworkOutRateSum())){
						return -1;
					}
					return 0;
				} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {
					if(quotaDto1.getBussSum() <= quotaDto2.getBussSum()){
						return -1;
					}
					return 0;
				}
				return 0;
			}
			
		});
		
		HashSet<String> hostSet = new HashSet<String>();
		for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
			hostSet.add(nodeDto.getHostIp());
		}
		
		List<HashMap<String, Object>> retList = new ArrayList<HashMap<String, Object>>();
		for (String hostIp : hostSet) {
			HashMap<String, Object> hostMap = new HashMap<String, Object>();
			List<ClusterNodeQuotaDto> hostList = new ArrayList<ClusterNodeQuotaDto>();
			hostMap.put("HOST_IP", hostIp);
			for (ClusterNodeQuotaDto nodeDto : hostQuotaList) {
				if (nodeDto.getHostIp().equals(hostIp)) {
					hostList.add(nodeDto);
				}
			}
			hostMap.put("HOST_LIST", hostList);
			retList.add(hostMap);
		}
		
		//获取每个节点在连续的几次资源数据中最小值
		if (BusinessConstant.PARAMS_BUS_1.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getCpuRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getCpuRateSum());
			}
		} else if (BusinessConstant.PARAMS_BUS_2.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getMemoryRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getMemoryRateSum());
			}
		} else if (BusinessConstant.PARAMS_BUS_3.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getDiskTotalRate());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getDiskTotalRate());
			}
		} else if (BusinessConstant.PARAMS_BUS_4.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getNetworkInRateSum() +"-"+hostNodeList.get(0).getNetworkOutRateSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getNetworkInRateSum() + "-" + hostNodeList.get(hostNodeList.size()-1).getNetworkOutRateSum());
			}
		} else if (BusinessConstant.PARAMS_BUS_5.equals(quotaType)) {
			for (HashMap<String, Object> hashMap : retList) {
				List<ClusterNodeQuotaDto> hostNodeList = (List<ClusterNodeQuotaDto>) hashMap.get("HOST_LIST");
				hashMap.put("MIN_VALUE", hostNodeList.get(0).getBussSum());
				hashMap.put("MAX_VALUE", hostNodeList.get(hostNodeList.size()-1).getBussSum());
			}
		}
		
		for (int i = 0; i < retList.size(); i++) {
			logger.debug("排序后的主机资源信息列表: ---> " + retList.get(i));
		}
		return retList;
	}
	
	public static String getQuotaTypeName(String quotaType) {
		String quotaTypeName = "";
		switch(quotaType) {
			case BusinessConstant.PARAMS_BUS_1:
				quotaTypeName = "CPU";
				break;
			case BusinessConstant.PARAMS_BUS_2:
				quotaTypeName = "内存";
				break;
			case BusinessConstant.PARAMS_BUS_3:
				quotaTypeName = "磁盘";
				break;
			case BusinessConstant.PARAMS_BUS_4:
				quotaTypeName = "网络输入-输出";
				break;
			default :
				break;
		}
		return quotaTypeName;
	}
	
	
}
