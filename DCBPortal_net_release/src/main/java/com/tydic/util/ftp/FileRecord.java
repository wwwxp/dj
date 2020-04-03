package com.tydic.util.ftp;

import java.util.Date;

/**
 * 远程主机文件Dto对象
 * 
 * @author Yuanh
 * 
 */
public class FileRecord {

	protected static final String DEFAULT_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

	// 目录
	public static final char DIR = 'D';
	// 文件
	public static final char FILE = 'F';
	// 未知类型
	public static final char UNK = 'U';

	// 文件类型
	private char fileType;
	// 文件绝对路径
	private String filePath;
	// 文件名称
	private String fileName;
	
	//文件扩展名称 
	private String fileNameExt;
	// 文件大小
	private long fileLength;
	// 文件创建时间
	private Date time;
	// 当前文件id
	private String currId;

	// 父节点
	private String parentId;

	// 文件创建时间
	private Boolean isUsed;

	private String fileLevel;

	private String clusterType;
	private String clusterId;
	private String clusterCode;
	private String isCluster;
	private boolean clusterRoot = false;
	private String targetPath;
	private String desc;

	//业务主集群ID
	private String busMainClusterId;
	//文件表示，如果为0表示过滤移除，为1不过滤移除
	private String filterFlag;

	public char getFileType() {
		return fileType;
	}

	public void setFileType(char fileType) {
		this.fileType = fileType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getCurrId() {
		return currId;
	}

	public void setCurrId(String currId) {
		this.currId = currId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Boolean getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}

	public boolean isDirectory() {
		return fileType == DIR;
	}

	public boolean isFile() {
		return fileType == FILE;
	}

	public String getFileLevel() {
		return fileLevel;
	}

	public void setFileLevel(String fileLevel) {
		this.fileLevel = fileLevel;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getIsCluster() {
		return isCluster;
	}

	public void setIsCluster(String isCluster) {
		this.isCluster = isCluster;
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

	public boolean isClusterRoot() {
		return clusterRoot;
	}

	public void setClusterRoot(boolean clusterRoot) {
		this.clusterRoot = clusterRoot;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	

	public String getFileNameExt() {
		return fileNameExt;
	}

	public void setFileNameExt(String fileNameExt) {
		this.fileNameExt = fileNameExt;
	}

	public String getBusMainClusterId() {
		return busMainClusterId;
	}

	public void setBusMainClusterId(String busMainClusterId) {
		this.busMainClusterId = busMainClusterId;
	}

	public String getFilterFlag() {
		return filterFlag;
	}

	public void setFilterFlag(String filterFlag) {
		this.filterFlag = filterFlag;
	}

	@Override
	public String toString() {
		return "FileRecord [fileType=" + fileType + ", filePath=" + filePath
				+ ", fileName=" + fileName + ", fileLength=" + fileLength
				+ ", time=" + time + ", currId=" + currId + ", parentId="
				+ parentId + ", isUsed=" + isUsed + ", fileLevel=" + fileLevel
				+ ", clusterType=" + clusterType + ", clusterId=" + clusterId
				+ ", clusterCode=" + clusterCode + ", isCluster=" + isCluster
				+ ", clusterRoot=" + clusterRoot + ", targetPath=" + targetPath
				+ ", desc=" + desc + ", busMainClusterId=" + busMainClusterId
				+ ", filterFlag=" + filterFlag + "]";
	}
}
