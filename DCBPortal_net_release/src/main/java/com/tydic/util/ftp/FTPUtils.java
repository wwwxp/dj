package com.tydic.util.ftp;

import com.tydic.bean.FtpDto;
import com.tydic.bp.common.utils.tools.BlankUtil;

/**
 * Ftp帮助类
 * 
 * @author Yuanh
 * 
 */
public class FTPUtils {

	public static final Integer TIMEOUT_DEF_MS = 15000;
	public static final Integer CLIENT_TRY_TIME_OUT = 3;

	//连接超时时间
	public static final int CONNECT_TIME_OUT = 60000;

	/**
	 * 获取Ftp实例
	 * 
	 * @param ftpDto
	 * @return
	 */
	public static Trans getFtpInstance(FtpDto ftpDto) {
		Trans src = null;
		if (!BlankUtil.isBlank(ftpDto)) {
			Integer timeOut = ftpDto.getTimeout()==null? CONNECT_TIME_OUT : ftpDto.getTimeout();
			if ("sftp".equals(ftpDto.getFtpType())) {
				src = new SftpTran(ftpDto.getHostIp(), 22, ftpDto.getUserName(), ftpDto.getPassword(), timeOut);
			} else {
				src = new FtpTran(ftpDto.getHostIp(), 21, ftpDto.getUserName(), ftpDto.getPassword(), timeOut);
			}
		}
		return src;
	}

	/**
	 * 创建ftp实例
	 *
	 * @param ftpIp
	 * @param ftpUserName
	 * @param ftpPasswd
	 * @param ftpType
	 * @return
	 */
	public static Trans getFtpInstance(String ftpIp, String ftpUserName, String ftpPasswd, String ftpType) {
		return getFtpInstance(ftpIp,ftpUserName,ftpPasswd,ftpType, CONNECT_TIME_OUT);
	}

	public static Trans getFtpInstance(String ftpIp, String ftpUserName, String ftpPasswd, String ftpType,int timeout) {
		Trans src = null;
		if ("sftp".equals(ftpType)) {
			src = new SftpTran(ftpIp, 22, ftpUserName, ftpPasswd, timeout);
		} else {
			src = new FtpTran(ftpIp, 21, ftpUserName, ftpPasswd, timeout);
		}
		return src;
	}

	public static void tryLogin(Trans trans) throws Exception {
		int i = CLIENT_TRY_TIME_OUT;
		while (i-- > 0) {
			try {
				trans.login();
				break;
			} catch (Exception e) {
				e.getMessage().toUpperCase().contains("TIMEOUT");
				if (i <= 0) {
					throw e;
				}
			}
		}
	}
}
