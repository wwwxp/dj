package com.tydic.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Simple to Introduction
 * 
 * @ProjectName: [DCBPortal_v1.0]
 * @Package: [com.tydic.bean]
 * @ClassName: [DynamicHostQuotaDto]
 * @Description: [动态主机指标]
 * @Author: [Yuanh]
 * @CreateDate: [2018-3-28 下午4:52:43]
 * @UpdateUser: [Yuanh]
 * @UpdateDate: [2018-3-28 下午4:52:43]
 * @UpdateRemark: [说明本次修改内容]
 * @Version: [v1.0]
 * 
 */
public class DynamicHostQuotaDto {

	private String hostIp;
	private List<String> batchNoList = new ArrayList<String>();
	private List<Double> cpuList = new ArrayList<Double>();
	private List<Double> memList = new ArrayList<Double>();
	private List<Double> diskList = new ArrayList<Double>();
	private List<Double> bussList = new ArrayList<Double>();

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
	//预测保存对象（）
	private Map<String, Object> predictionMap = new HashMap<String, Object>();
	
	
	
	
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

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public List<Double> getCpuList() {
		return cpuList;
	}

	public void setCpuList(List<Double> cpuList) {
		this.cpuList = cpuList;
	}

	public List<Double> getMemList() {
		return memList;
	}

	public void setMemList(List<Double> memList) {
		this.memList = memList;
	}

	public List<Double> getDiskList() {
		return diskList;
	}

	public void setDiskList(List<Double> diskList) {
		this.diskList = diskList;
	}

	public List<Double> getBussList() {
		return bussList;
	}

	public void setBussList(List<Double> bussList) {
		this.bussList = bussList;
	}

	public List<String> getBatchNoList() {
		return batchNoList;
	}

	public void setBatchNoList(List<String> batchNoList) {
		this.batchNoList = batchNoList;
	}

	@Override
	public String toString() {
		return "DynamicHostQuotaDto [hostIp=" + hostIp + ", batchNoList="
				+ batchNoList + ", cpuList=" + cpuList + ", memList=" + memList
				+ ", diskList=" + diskList + ", bussList=" + bussList + "]";
	}

	
}
