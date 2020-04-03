package com.tydic.bean;

public class FtpDto {
	public static String FTP_USERNAME = "FTP_USERNAME";
	public static String FTP_PASSWD = "FTP_PASSWD";
	public static String FTP_TYPE = "FTP_TYPE";
	public static String FTP_ROOT_PATH = "FTP_ROOT_PATH";
	public static String FTP_IP = "FTP_IP";

	private String userName ;
	private String password ;
	private String ftpType ;
	private String ftpRootPath ;
	private String hostIp;
	private Integer timeout;

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFtpType() {
		return ftpType;
	}

	public void setFtpType(String ftpType) {
		this.ftpType = ftpType;
	}

	public String getFtpRootPath() {
		return ftpRootPath;
	}

	public void setFtpRootPath(String ftpRootPath) {
		this.ftpRootPath = ftpRootPath;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	@Override
	public String toString() {
		return "FtpDto{" +
				"userName='" + userName + '\'' +
				", password='" + password + '\'' +
				", ftpType='" + ftpType + '\'' +
				", ftpRootPath='" + ftpRootPath + '\'' +
				", hostIp='" + hostIp + '\'' +
				'}';
	}
}
