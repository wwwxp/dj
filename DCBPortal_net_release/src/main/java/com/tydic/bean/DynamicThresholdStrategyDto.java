package com.tydic.bean;

import java.util.ArrayList;
import java.util.List;

public class DynamicThresholdStrategyDto implements Cloneable {

	private String strategyId;
	private String clusterId;
	private String taskProgramId;
	private String operatorType;
	private String quotaType;
	private String conditionParam;
	private String conditionValue;

	//策略对应的预测阀值
	List<DynamicHostNodePredictionDto> predictioList = new ArrayList<DynamicHostNodePredictionDto>();
	
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

	public List<DynamicHostNodePredictionDto> getPredictioList() {
		return predictioList;
	}

	public void setPredictioList(List<DynamicHostNodePredictionDto> predictioList) {
		this.predictioList = predictioList;
	}
}
