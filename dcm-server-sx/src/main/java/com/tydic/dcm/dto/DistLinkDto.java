package com.tydic.dcm.dto;

import java.util.Hashtable;

import com.tydic.dcm.enums.DistTaskTypeEnum;
import com.tydic.dcm.enums.FileStoreTypeEnum;
import com.tydic.dcm.task.DistTask;

/**
 * 分发链路参数、链路属性
 * 
 * @author Yuanh
 *
 */
public class DistLinkDto implements Cloneable {

	private Long devId;
	private String devName;
	private Long addrId;
	private String subType;
	private String remark;
	private String validFlag;
	private String latnId;
	private String collDevId;
	private String beginTime;
	private String endTime;
	private String parentFlag;
	private String runState;
	private String linkErr;
	private String stateBeginTime;

	// 1MQ分发 2ftp分发
	private int tskType = DistTaskTypeEnum.DIST_FTP.getTskType();
	// 文件存储方式
	private String fileStoreType = FileStoreTypeEnum.FILE_STORE_LOCAL.getFileStoreType();

	// 链路参数必填项是否缺失
	private Boolean isMissParams = Boolean.FALSE;
	// 链路参数级别提示
	private String tipsMsg;
	// 链路参数
	public Hashtable<String, Object> linkParams = new Hashtable<String, Object>();;

	public DistLinkDto() {
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

	public String getLatnId() {
		return latnId;
	}

	public void setLatnId(String latnId) {
		this.latnId = latnId;
	}

	public String getCollDevId() {
		return collDevId;
	}

	public void setCollDevId(String collDevId) {
		this.collDevId = collDevId;
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

	public String getParentFlag() {
		return parentFlag;
	}

	public void setParentFlag(String parentFlag) {
		this.parentFlag = parentFlag;
	}

	public String getRunState() {
		return runState;
	}

	public void setRunState(String runState) {
		this.runState = runState;
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

	public int getTskType() {
		return tskType;
	}

	public void setTskType(int tskType) {
		this.tskType = tskType;
	}

	public String getFileStoreType() {
		return fileStoreType;
	}

	public void setFileStoreType(String fileStoreType) {
		this.fileStoreType = fileStoreType;
	}

	@Override
	public String toString() {
		return "DistLinkDto [devId=" + devId + ", devName=" + devName + ", addrId=" + addrId + ", subType=" + subType
				+ ", remark=" + remark + ", validFlag=" + validFlag + ", latnId=" + latnId + ", collDevId=" + collDevId
				+ ", beginTime=" + beginTime + ", endTime=" + endTime + ", parentFlag=" + parentFlag + ", runState="
				+ runState + ", linkErr=" + linkErr + ", stateBeginTime=" + stateBeginTime + ", tskType=" + tskType
				+ ", fileStoreType=" + fileStoreType + ", isMissParams=" + isMissParams + ", tipsMsg=" + tipsMsg
				+ ", linkParams=" + linkParams + "]";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
