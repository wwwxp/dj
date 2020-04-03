package com.tydic.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DynamicThresholdDto {

	// 业务程序ID
	private String taskProgramId;
	
	//业务程序类型
	private String programType;

	// 业务集群ID
	private String clusterId;

	// 集群中包含的主机
	private HashSet<String> hostList = new HashSet<String>();

	// 业务集群对应的主机集群
	private List<HashMap<String, Object>> clusterHostList = new ArrayList<HashMap<String, Object>>();

	// 业务对应的动态策略配置
	private List<DynamicThresholdStrategyDto> strategyList = new ArrayList<DynamicThresholdStrategyDto>();

	public HashSet<String> getHostList() {
		return hostList;
	}

	public void setHostList(HashSet<String> hostList) {
		this.hostList = hostList;
	}

	public String getTaskProgramId() {
		return taskProgramId;
	}

	public void setTaskProgramId(String taskProgramId) {
		this.taskProgramId = taskProgramId;
	}

	
	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public List<HashMap<String, Object>> getClusterHostList() {
		return clusterHostList;
	}

	public void setClusterHostList(List<HashMap<String, Object>> clusterHostList) {
		this.clusterHostList = clusterHostList;
	}

	public List<DynamicThresholdStrategyDto> getStrategyList() {
		return strategyList;
	}

	public void setStrategyList(List<DynamicThresholdStrategyDto> strategyList) {
		this.strategyList = strategyList;
	}

	@Override
	public String toString() {
		return "DynamicThresholdDto [taskProgramId=" + taskProgramId
				+ ", clusterId=" + clusterId + ", hostList=" + hostList
				+ ", clusterHostList=" + clusterHostList + ", strategyList="
				+ strategyList + "]";
	}

}
