package com.tydic.dcm.dto;

import java.util.Vector;

import com.tydic.dcm.ftran.TransItem;

public class WarnSeqDto {

	/**
	 * 链路ID
	 */
	private String devId;

	/**
	 * 文件列表
	 */
	private Vector<TransItem> fileList;

	/**
	 * 链路参数对象
	 */
	private CollLinkDto collLinkDto;

	public WarnSeqDto() {
		super();
	}

	public WarnSeqDto(String devId, Vector<TransItem> fileList,
			CollLinkDto collLinkDto) {
		super();
		this.devId = devId;
		this.fileList = fileList;
		this.collLinkDto = collLinkDto;
	}

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	public Vector<TransItem> getFileList() {
		return fileList;
	}

	public void setFileList(Vector<TransItem> fileList) {
		this.fileList = fileList;
	}

	public CollLinkDto getCollLinkDto() {
		return collLinkDto;
	}

	public void setCollLinkDto(CollLinkDto collLinkDto) {
		this.collLinkDto = collLinkDto;
	}

}
