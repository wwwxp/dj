package com.tydic.bean;

import java.util.HashMap;
import java.util.Map;

public class DynamicHostNodePredictionDto {
	private String strategyId;
	private String clusterId;
	private String taskProgramId;
	private String operatorType;
	private String quotaType;
	private String conditionParam;
	private String conditionValue;
	
	// 主机IP
	private String hostIp;
	// 是否触发扩展或者收缩
	private String triggerRst;
	// 预测时间
	private String predictionTime;
	// 预测多少天后扩容
	private int days;
	// 预测CPU值
	private Double predictionCpu;
	// 预测Mem值
	private Double predictionMem;
	// 预测Disk值
	private Double predictionDisk;
	// 预测业务量值
	private Double predictionBuss;
	// 预测保存对象（）
	private Map<String, Object> predictionMap = new HashMap<String, Object>();

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getTriggerRst() {
		return triggerRst;
	}

	public void setTriggerRst(String triggerRst) {
		this.triggerRst = triggerRst;
	}

	public String getPredictionTime() {
		return predictionTime;
	}

	public void setPredictionTime(String predictionTime) {
		this.predictionTime = predictionTime;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public Double getPredictionCpu() {
		return predictionCpu;
	}

	public void setPredictionCpu(Double predictionCpu) {
		this.predictionCpu = predictionCpu;
	}

	public Double getPredictionMem() {
		return predictionMem;
	}

	public void setPredictionMem(Double predictionMem) {
		this.predictionMem = predictionMem;
	}

	public Double getPredictionDisk() {
		return predictionDisk;
	}

	public void setPredictionDisk(Double predictionDisk) {
		this.predictionDisk = predictionDisk;
	}

	public Double getPredictionBuss() {
		return predictionBuss;
	}

	public void setPredictionBuss(Double predictionBuss) {
		this.predictionBuss = predictionBuss;
	}

	public Map<String, Object> getPredictionMap() {
		return predictionMap;
	}

	public void setPredictionMap(Map<String, Object> predictionMap) {
		this.predictionMap = predictionMap;
	}

	public String getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(String strategyId) {
		this.strategyId = strategyId;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getTaskProgramId() {
		return taskProgramId;
	}

	public void setTaskProgramId(String taskProgramId) {
		this.taskProgramId = taskProgramId;
	}

	public String getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}

	public String getQuotaType() {
		return quotaType;
	}

	public void setQuotaType(String quotaType) {
		this.quotaType = quotaType;
	}

	public String getConditionParam() {
		return conditionParam;
	}

	public void setConditionParam(String conditionParam) {
		this.conditionParam = conditionParam;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}

	@Override
	public String toString() {
		return "DynamicHostNodePredictionDto [strategyId=" + strategyId
				+ ", clusterId=" + clusterId + ", taskProgramId="
				+ taskProgramId + ", operatorType=" + operatorType
				+ ", quotaType=" + quotaType + ", conditionParam="
				+ conditionParam + ", conditionValue=" + conditionValue
				+ ", hostIp=" + hostIp + ", triggerRst=" + triggerRst
				+ ", predictionTime=" + predictionTime + ", days=" + days
				+ ", predictionCpu=" + predictionCpu + ", predictionMem="
				+ predictionMem + ", predictionDisk=" + predictionDisk
				+ ", predictionBuss=" + predictionBuss + ", predictionMap="
				+ predictionMap + "]";
	}

}
