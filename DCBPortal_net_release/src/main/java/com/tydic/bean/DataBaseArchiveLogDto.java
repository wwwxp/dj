package com.tydic.bean;

public class DataBaseArchiveLogDto {
	
	private String archiveId;
	//批次号
	private String batchNum;
	//结果
	private String archiveResult;
	//源表
	private String sourTableName;
	//目标表
	private String destTableName;
	
	
	public DataBaseArchiveLogDto() {
		super();
	}
	public DataBaseArchiveLogDto(String archiveId,String batchNum,String archiveResult) {
		this.archiveId = archiveId;
		this.batchNum = batchNum;
		this.archiveResult = archiveResult;
	}

	public String getArchiveId() {
		return archiveId;
	}

	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}

	public String getBatchNum() {
		return batchNum;
	}

	public void setBatchNum(String batchNum) {
		this.batchNum = batchNum;
	}

	public String getArchiveResult() {
		return archiveResult;
	}

	public void setArchiveResult(String archiveResult) {
		this.archiveResult = archiveResult;
	}
	public String getSourTableName() {
		return sourTableName;
	}
	public void setSourTableName(String sourTableName) {
		this.sourTableName = sourTableName;
	}
	public String getDestTableName() {
		return destTableName;
	}
	public void setDestTableName(String destTableName) {
		this.destTableName = destTableName;
	}
	@Override
	public String toString() {
		return "[archiveId:"+archiveId+",batchNum:"+batchNum+"]";
	}
	
}
