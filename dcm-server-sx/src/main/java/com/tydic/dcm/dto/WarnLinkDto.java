package com.tydic.dcm.dto;

import java.io.Serializable;

public class WarnLinkDto implements Serializable {

	private static final long serialVersionUID = -602992312322196393L;

	// 链路最新操作文件时间戳
	private Long lastTime;
	// 当前链路地址ID
	private String addrId;
	// 链路名称
	private String devName;
	// 链路添加告警时间间隔
	private int warnInterval;

	public WarnLinkDto() {
		super();
	}

	public WarnLinkDto(Long lastTime, String addrId) {
		super();
		this.lastTime = lastTime;
		this.addrId = addrId;
	}

	public WarnLinkDto(Long lastTime, String addrId, int warnInterval) {
		super();
		this.lastTime = lastTime;
		this.addrId = addrId;
		this.warnInterval = warnInterval;
	}

	public WarnLinkDto(Long lastTime, String addrId, String devName) {
		super();
		this.lastTime = lastTime;
		this.addrId = addrId;
		this.devName = devName;
	}

	public Long getLastTime() {
		return lastTime;
	}

	public void setLastTime(Long lastTime) {
		this.lastTime = lastTime;
	}

	public String getAddrId() {
		return addrId;
	}

	public void setAddrId(String addrId) {
		this.addrId = addrId;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public int getWarnInterval() {
		return warnInterval;
	}

	public void setWarnInterval(int warnInterval) {
		this.warnInterval = warnInterval;
	}
}
