package com.tydic.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.bean]    
  * @ClassName:    [ClusterNodeDto]     
  * @Description:  [节点伸缩DTO]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-7 下午3:51:11]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-7 下午3:51:11]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
public class ClusterNodeDto {
	private String strategyId;
	//业务集群ID
	private String clusterId;
	//业务集群类型
	private String clusterType;
	//业务集群编码
	private String clusterCode;
	
	//业务集群关联的Jstorm集群ID
	private String jstormClusterId;
	//业务集群关联的Jstorm集群类型
	private String jstormClusterType;
	//业务集群关联的Jstorm集群编码
	private String jstormClusterCode;
	//版本号
	private String version;
	private String msg;
	
	//集群已部署主机列表
	private List<HashMap<String, Object>> deployHostList = new ArrayList<HashMap<String, Object>>();
	//集群已部署主机列表IP
	private List<String> deployHostIPArray = new ArrayList<String>();
	
	//集群未部署主机列表
	private List<HashMap<String, Object>> backupHostList = new ArrayList<HashMap<String, Object>>();
	//集群未部署主机列表IP
	private List<String> backupHostIPArray = new ArrayList<String>();
	
	private List<ClusterNodeQuotaDto> ruleList = new ArrayList<ClusterNodeQuotaDto>();
	
	private String ruleStr;
	
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public List<HashMap<String, Object>> getDeployHostList() {
		return deployHostList;
	}
	public void setDeployHostList(List<HashMap<String, Object>> deployHostList) {
		this.deployHostList = deployHostList;
	}
	public List<HashMap<String, Object>> getBackupHostList() {
		return backupHostList;
	}
	public void setBackupHostList(List<HashMap<String, Object>> backupHostList) {
		this.backupHostList = backupHostList;
	}
	public List<String> getDeployHostIPArray() {
		return deployHostIPArray;
	}
	public void setDeployHostIPArray(List<String> deployHostIPArray) {
		this.deployHostIPArray = deployHostIPArray;
	}
	public List<String> getBackupHostIPArray() {
		return backupHostIPArray;
	}
	public void setBackupHostIPArray(List<String> backupHostIPArray) {
		this.backupHostIPArray = backupHostIPArray;
	}
	
	public String getClusterType() {
		return clusterType;
	}
	public void setClusterType(String clusterType) {
		this.clusterType = clusterType;
	}
	
	public String getClusterCode() {
		return clusterCode;
	}
	public void setClusterCode(String clusterCode) {
		this.clusterCode = clusterCode;
	}
	public String getJstormClusterId() {
		return jstormClusterId;
	}
	public void setJstormClusterId(String jstormClusterId) {
		this.jstormClusterId = jstormClusterId;
	}
	public String getJstormClusterType() {
		return jstormClusterType;
	}
	public void setJstormClusterType(String jstormClusterType) {
		this.jstormClusterType = jstormClusterType;
	}
	public String getJstormClusterCode() {
		return jstormClusterCode;
	}
	public void setJstormClusterCode(String jstormClusterCode) {
		this.jstormClusterCode = jstormClusterCode;
	}
	
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getStrategyId() {
		return strategyId;
	}
	public void setStrategyId(String strategyId) {
		this.strategyId = strategyId;
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	
	/**
	 * @return the ruleList
	 */
	public List<ClusterNodeQuotaDto> getRuleList() {
		return ruleList;
	}
	/**
	 * @param ruleList the ruleList to set
	 */
	public void setRuleList(List<ClusterNodeQuotaDto> ruleList) {
		this.ruleList = ruleList;
	}
	/**
	 * @return the ruleStr
	 */
	public String getRuleStr() {
		return ruleStr;
	}
	/**
	 * @param ruleStr the ruleStr to set
	 */
	public void setRuleStr(String ruleStr) {
		this.ruleStr = ruleStr;
	}
	@Override
	public String toString() {
		return "ClusterNodeDto [clusterId=" + clusterId + ", clusterType="
				+ clusterType + ", deployHostList=" + deployHostList
				+ ", deployHostIPArray=" + deployHostIPArray
				+ ", backupHostList=" + backupHostList + ", backupHostIPArray="
				+ backupHostIPArray + "]";
	}

}
