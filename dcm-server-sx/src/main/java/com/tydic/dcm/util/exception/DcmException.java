package com.tydic.dcm.util.exception;

import com.tydic.dcm.util.tools.StringTool;

public class DcmException extends Exception {
	private static final long serialVersionUID = 2488657389599604267L;

	// 初始化链路信息失败
	public static final String INIT_LINK_ERR = "10001";
	
	//记录日志表失败
	public static final String RECORD_LOG_ERR = "10002";
	
	//下载文件失败
	public static final String GET_FILE_ERR = "10003";
	
	//Ftp连接错误
	public static final String FTP_CONNECT_ERR = "10004";
	
	//远程目录不存在
	public static final String DIR_NOT_EXISTS_ERR = "10005";
	
	//获取文件列表错误
	public static final String GET_FILE_LIST_ERR = "10006";
	
	//批量分发失败
	public static final String BATCH_DIST_ERR = "10007";
	
	//分发异常处理失败
	public static final String DIST_EXCEPTION_ERR = "10008";
	
	//上传文件失败
	public static final String PUT_FILE_ERR = "10009";
	
	//后续操作失败
	public static final String LATE_OPERATOR_ERR = "10010";
	
	//目录创建失败
	public static final String DIR_CREATE_ERR = "10011";
	
	//未知错误
	public static final String OTHER_ERR = "50000";

	// 告警编码，对应的是告警类型表编码(DC_WARN_TYPE)
	private String warnCode;

	// 错误编码，用来保存到错误信息列的编码
	private String errorCode;

	// 错误信息，最终保存到数据错误信息列的信息应该是(errorCode+":"+errorMsg)
	private String errorMsg;

	// 错误级别
	private String tipsLevel;

	// 异常文件
	private String fileName;

	public DcmException() {
		super();
	}

	public DcmException(String warnCode, String errorCode, String errorMsg) {
		super();
		this.warnCode = warnCode;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public DcmException(String warnCode, String errorCode, String errorMsg,
			String tipsLevel) {
		super();
		this.warnCode = warnCode;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.tipsLevel = tipsLevel;
	}

	public DcmException(String warnCode, String errorCode, String errorMsg,
			String tipsLevel, String fileName) {
		super();
		this.warnCode = warnCode;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.tipsLevel = tipsLevel;
		this.fileName = fileName;
	}

	public String getErrorCode() {
		return StringTool.object2String(errorCode);
	}

	public String getFileName() {
		return StringTool.object2String(fileName);
	}

	public String getErrorMsg() {
		return StringTool.object2String(errorMsg);
	}

	public String getTipsLevel() {
		return StringTool.object2String(tipsLevel);
	}

	public String getWarnCode() {
		return StringTool.object2String(warnCode);
	}

	@Override
	public String toString() {
		return "DcmException [errorCode=" + errorCode + ", errorMsg="
				+ errorMsg + "]";
	}
}
