package com.tydic.common;

public class BusException extends Exception {
	private static final long serialVersionUID = 5698153208757798186L;

	// 异常编码
	private String errorCode;

	// 异常信息
	private String errorMsg;

	// 异常描述
	private String errorReason;

	public BusException() {
		super();
	}

	public BusException(String errorCode, String errorMsg, String errorReason) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.errorReason = errorReason;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

}
