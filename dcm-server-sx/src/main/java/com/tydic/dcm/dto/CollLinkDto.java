package com.tydic.dcm.dto;

import java.util.Hashtable;

/**
 * 采集链路参数、链路属性
 * 
 * @author Yuanh
 *
 */
public class CollLinkDto {

	// 链路属性
	private Long devId;
	private String devName;
	private Long addrId;
	private String subType;
	private String remark;
	private String validFlag;
	private String fmtFlag;
	private String dateMode;
	private String timeMode;
	private String rltDistId;
	private String beginTime;
	private String endTime;
	private String latnId;
	private String linkErr;
	private String stateBeginTime;
	private String stateEndTime;
	private String runState;

	// 文件存储方式
	private String fileStoreType = "local";

	// 链路参数必填项是否缺失
	private Boolean isMissParams = Boolean.FALSE;
	// 链路参数级别提示
	private String tipsMsg;
	// 链路参数
	public Hashtable<String, Object> linkParams = new Hashtable<String, Object>();

	public CollLinkDto() {

	}

	public Long getDevId() {
		return devId;
	}

	public void setDevId(Long devId) {
		this.devId = devId;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Long getAddrId() {
		return addrId;
	}

	public void setAddrId(Long addrId) {
		this.addrId = addrId;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getValidFlag() {
		return validFlag;
	}

	public void setValidFlag(String validFlag) {
		this.validFlag = validFlag;
	}

	public String getFmtFlag() {
		return fmtFlag;
	}

	public void setFmtFlag(String fmtFlag) {
		this.fmtFlag = fmtFlag;
	}

	public String getDateMode() {
		return dateMode;
	}

	public void setDateMode(String dateMode) {
		this.dateMode = dateMode;
	}

	public String getTimeMode() {
		return timeMode;
	}

	public void setTimeMode(String timeMode) {
		this.timeMode = timeMode;
	}

	public String getRltDistId() {
		return rltDistId;
	}

	public void setRltDistId(String rltDistId) {
		this.rltDistId = rltDistId;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getLatnId() {
		return latnId;
	}

	public void setLatnId(String latnId) {
		this.latnId = latnId;
	}

	public String getLinkErr() {
		return linkErr;
	}

	public void setLinkErr(String linkErr) {
		this.linkErr = linkErr;
	}

	public String getStateBeginTime() {
		return stateBeginTime;
	}

	public void setStateBeginTime(String stateBeginTime) {
		this.stateBeginTime = stateBeginTime;
	}

	public String getStateEndTime() {
		return stateEndTime;
	}

	public void setStateEndTime(String stateEndTime) {
		this.stateEndTime = stateEndTime;
	}

	public String getRunState() {
		return runState;
	}

	public void setRunState(String runState) {
		this.runState = runState;
	}

	public Hashtable<String, Object> getLinkParams() {
		return linkParams == null ? new Hashtable<String, Object>() : linkParams;
	}

	public void setLinkParams(Hashtable<String, Object> linkParams) {
		this.linkParams = linkParams;
	}

	public Boolean getIsMissParams() {
		return isMissParams;
	}

	public void setIsMissParams(Boolean isMissParams) {
		this.isMissParams = isMissParams;
	}

	public String getTipsMsg() {
		return tipsMsg;
	}

	public void setTipsMsg(String tipsMsg) {
		this.tipsMsg = tipsMsg;
	}

	public String getFileStoreType() {
		return fileStoreType;
	}

	public void setFileStoreType(String fileStoreType) {
		this.fileStoreType = fileStoreType;
	}

	@Override
	public String toString() {
		return "CollLinkDto [devId=" + devId + ", devName=" + devName + ", addrId=" + addrId + ", subType=" + subType
				+ ", remark=" + remark + ", validFlag=" + validFlag + ", fmtFlag=" + fmtFlag + ", dateMode=" + dateMode
				+ ", timeMode=" + timeMode + ", rltDistId=" + rltDistId + ", beginTime=" + beginTime + ", endTime="
				+ endTime + ", latnId=" + latnId + ", linkErr=" + linkErr + ", stateBeginTime=" + stateBeginTime
				+ ", stateEndTime=" + stateEndTime + ", runState=" + runState + ", fileStoreType=" + fileStoreType
				+ ", isMissParams=" + isMissParams + ", tipsMsg=" + tipsMsg + ", linkParams=" + linkParams + "]";
	}
}
