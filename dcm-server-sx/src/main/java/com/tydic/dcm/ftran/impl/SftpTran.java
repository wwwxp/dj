package com.tydic.dcm.ftran.impl;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.DcmSystem;
import com.tydic.dcm.ftran.FileRecord;
import com.tydic.dcm.ftran.Trans;
import com.tydic.dcm.util.tools.ArrayUtil;
import com.tydic.dcm.util.tools.FileTool;
import com.tydic.dcm.util.tools.StringTool;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

/**
 * Sftp连接实现类
 *
 * @author Yuanh
 */
public class SftpTran implements Trans {
	//日志对象
	private static Logger logger = Logger.getLogger(SftpTran.class);

	//Sftp通道对象
	public Session session = null;
	public ChannelSftp channel = null;

	//Sftp登录主机IP
	private String ip;

	//Sftp登录主机PORT
	private int port;

	//Sftp登录用户名
	private String userName;

	//Sftp登录密码
	private String password;

	//Sftp连接超时时间
	private int timeout;

	//链路ID，主要用来输出日志
	private String devId;

	/**
	 * @param ip       Sftp主机
	 * @param port     Sftp端口
	 * @param userName Sftp登录账号
	 * @param password Sftp登录密码
	 * @param timeout  Sftp连接超时时间
	 */
	public SftpTran(String ip, int port, String userName, String password, int timeout) {
		this.ip = ip;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.timeout = timeout;
	}

	/**
	 * @param ip       Sftp主机
	 * @param port     Sftp端口
	 * @param userName Sftp登录账号
	 * @param password Sftp登录密码
	 * @param timeout  Sftp连接超时时间
	 * @param devId    链路ID
	 */
	public SftpTran(String ip, int port, String userName, String password, int timeout, String devId) {
		this.ip = ip;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.timeout = timeout;
		this.devId = devId;
	}

	public static void main(String[] args) throws Exception {
		SftpTran tran = new SftpTran("192.168.161.89", 22, "bp", "bp", 6000, "101488");
		//SftpTran tran = new SftpTran("192.168.161.26", 22, "bp_dcf", "dic123", 6000);
		tran.login();

		ByteArrayOutputStream bos = tran.getFileStream("/public/bp/aaa.txt");

		IOUtils.closeQuietly(bos);
		bos = tran.getFileStream("/public/bp/aaa.txt");
		IOUtils.closeQuietly(bos);
	}

	/**
	 * 判断目录是否存在
	 *
	 * @param filePath
	 * @return
	 */
	@Override
	public boolean isExistPath(String filePath) {
		logger.debug("sftp begin check path exist, path:" + filePath + ", devId: " + devId);
		boolean result = true;
		try {
			channel.cd(filePath);
		} catch (SftpException e) {
			result = false;
			logger.error("sftp check file path exist fail. LINK ID: " + devId, e);
		}
		logger.debug("sftp filePath exist, result:" + result + ", devId: " + devId);
		return result;
	}

	/**
	 * 获取目录下文件列表
	 * @param path
	 * @param fileNamePattern:文件名匹配规则，列举所有设置为null，ftp设置此规则有可能失败
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Vector<FileRecord> getFileList(String path,String fileNamePattern) throws Exception  {
		logger.info("begin get sftp remote file list, remote path: " + path + ",fileNamePattern:" + fileNamePattern + ", devId: " + devId);

		Vector<FileRecord> rsList = new Vector<FileRecord>();
		try {
			String pathPattern = path;
			if(!StringUtils.endsWith(path,"/")){
				pathPattern += "/";
			}
			if(StringUtils.isNotBlank(fileNamePattern)){
				pathPattern += fileNamePattern;
			}

			Vector<LsEntry> fileList = channel.ls(pathPattern);
			logger.info("sftp list remote file ok, remote list size:" + ArrayUtil.getSize(fileList) + ", devId: " + devId);

			if (fileList != null && fileList.size() > 0) {
				int len = fileList.size();
				for (int i=0; i<len; i++) {
					LsEntry lsEntry = fileList.get(i);

					//获取Sftp文件列表
					String fileName = lsEntry.getFilename();
					if (".".equals(fileName) || "..".equals(fileName)) {
						continue;
					}

					long time = lsEntry.getAttrs().getMTime() * 1000L;
					Date mtime = new Date(time);
					long fileLength = lsEntry.getAttrs().getSize();

					//创建FileRecord对象
					FileRecord fileRecord = new FileRecord();
					fileRecord.setFileName(fileName);
					fileRecord.setFileLength(fileLength);
					fileRecord.setTime(DateUtil.formatToDate(mtime, DateUtil.allPattern));
					fileRecord.setFilePath(FileTool.exactPath(path));

					//判断是否为文件或者目录
					if (lsEntry.getAttrs().isDir()) {
						fileRecord.setFileType(FileRecord.DIR);
					} else if (lsEntry.getAttrs().isReg()) {
						fileRecord.setFileType(FileRecord.FILE);
					}else{
						logger.debug("file type error, fileName: " + fileName + ", devId: " + devId);
						continue;
					}

					rsList.add(fileRecord);
				}
			}
		} catch (Exception e) {
			logger.error("sftp get files fail. LINK ID: " + devId, e);
			throw e;
		}
		logger.info("end get sftp remote file list, file list size:" + ArrayUtil.getSize(rsList) + ", devId: " + devId);
		return rsList;
	}

	/**
	 * 关闭Sftp连接
	 *
	 * @return
	 */
	@Override
	public void close() {
		try {
			if (channel != null) {
				if (channel.isConnected()) {
					channel.disconnect();
					logger.info("sftp closed success...");
				} else if (channel.isClosed()) {
					logger.info("sftp is closed already...");
				}
			}
			if (session != null) {
				if (session.isConnected()) {
					session.disconnect();
					logger.info("sftp session closed success...");
				}
			}
		} catch (Exception e) {
			logger.error("sftp session close exception, devId: " + devId, e);
		}
	}

	@Override
	public boolean reconnect() {
		boolean result = false;
		logger.info("begin reconnect sftp,devId:" + this.devId);
		try {
			this.close();

			this.login();

			result = true;
			logger.info("finished reconnect sftp,devId:" + this.devId);
		} catch (Exception e) {
			logger.error("sftp reconnect fail,devId:" + this.devId, e);
		}

		return result;
	}

	/**
	 * Sftp下载文件
	 *
	 * @param remotePath
	 * @param localPath
	 * @return
	 */
	@Override
	public void get(String remotePath, String localPath) throws Exception {
		logger.debug("begin get sftp remote file, remote path: " + remotePath + ", local path: " + localPath + ", devId: " + devId);
		FileOutputStream fos = null;
		try {
			File file = new File(localPath);
			fos = new FileOutputStream(file);
			channel.get(remotePath, fos);
			fos.flush();
		} catch (Exception e) {
			logger.error("sftp get file fail. LINK ID: " + devId, e);
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		logger.debug("end get sftp remote file, devId: " + devId);
	}

	/**
	 * 将本地文件上传到远程主机,将本地文件名为src的文件上传到目标服务器，目标文件名为dst，若dst为目录，则目标文件名将与src文件名相同
	 *
	 * @param localPath  本地文件
	 * @param remotePath 远程目录
	 * @return
	 * @throws Exception
	 */
	public void put(String localPath, String remotePath) throws Exception {
		logger.debug("sftp begin put file, local path: " + localPath + ", remote path: " + remotePath + ", devId: " + devId);
		try {
			/**
			 * 传输模式
			 * OVERWRITE:完全覆盖模式,这是JSch的默认文件传输模式，即如果目标文件已经存在，传输的文件将完全覆盖目标文件，产生新的文件
			 * RESUME:恢复模式,如果文件已经传输一部分，这时由于网络或其他任何原因导致文件传输中断，如果下一次传输相同的文件，则会从上一次中断的地方续传
			 * APPEND:追加模式,如果目标文件已存在，传输的文件将在目标文件后追加
			 */
			channel.put(localPath, remotePath, ChannelSftp.OVERWRITE);
		} catch (Exception e) {
			logger.error("sftp put file error. LINK ID: " + devId, e);
			throw e;
		}
		logger.debug("sftp end put file, devId: " + devId);
	}

	/**
	 * 删除文件
	 *
	 * @param path
	 * @return boolean
	 * @throws Exception
	 */
	@Override
	public boolean delete(String path) throws Exception {
		logger.debug("sftp begin delete file, path: " + path + ", devId: " + devId);
		boolean result = true;
		try {
			//删除文件
			channel.rm(path);
		} catch (Exception e) {
			logger.error("sftp delete file fail. LINK ID: " + devId, e);
			try {
				//删除目录
				channel.rmdir(path);
			} catch (Exception e1) {
				result = false;
				logger.error("sftp delete file directory fail. LINK ID: " + devId, e1);
				throw e;
			}
		}
		logger.debug("sftp end delete file, result: " + result + ", devId: " + devId);
		return result;
	}

	/**
	 * 文件重命令
	 *
	 * @param sourceFile 远程源文件名称
	 * @param targetFile 远程目标文件名称
	 * @return boolean
	 * @throws Exception
	 */
	@Override
	public boolean rename(String sourceFile, String targetFile) throws Exception {
		logger.debug("sftp begin rename file, source file: " + sourceFile + ", target file: " + targetFile + ", devId: " + devId);
		boolean result = true;

		//文件存在，先删除
		try {
			channel.rm(targetFile);
			logger.debug("file exists, delete file first, file: " + targetFile);
		} catch (Exception e) {

		}

		try {
			channel.rename(sourceFile, targetFile);
		} catch (Exception e) {
			result = false;
			logger.error("sftp rename file fail, LINK ID: " + devId, e);
			throw e;
		}
		logger.info("sftp end rename file, result: " + result + ", devId: " + devId);
		return result;
	}

	/**
	 * 切换目录
	 *
	 * @param remotePath 远程目录名称
	 * @return
	 * @throws SftpException
	 */
	public boolean cd(String remotePath) throws Exception {
		logger.debug("sftp begin change file directory, remote path:" + remotePath + ", devId: " + devId);
		boolean result = true;
		try {
			channel.cd(remotePath);
		} catch (Exception e) {
			result = false;
			logger.error("sftp change file directory fail. LINK ID: " + devId, e);
			throw e;
		}
		logger.debug("sftp end change file directory, result:" + result + ", devId: " + devId);
		return result;
	}

	/**
	 * 创建目录(可以创建多级目录)
	 *
	 * @param dirName
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean mkdir(String dirName) throws Exception {
		logger.debug("sftp begin create directory, path: " + dirName + ", devId: " + devId);
		boolean result = true;
		try {
			Vector<String> filePaths = StringTool.tokenStringChar(dirName, File.separator + "//\\");
			if (filePaths != null && filePaths.size() > 0) {
				//在linux中必须根目录/开头
				String tempPath = "/";
				for (int i = 0; i < filePaths.size(); i++) {
					tempPath += filePaths.get(i);
					tempPath = FileTool.exactPath(tempPath);
					if (!isExistPath(tempPath)) {
						channel.mkdir(tempPath);
					}
				}
			}

			String finalDirName = FileTool.exactPath(dirName);
			result = isExistPath(finalDirName);
		} catch (Exception e) {
			result = false;
			logger.error("sftp create directory fail. LINK ID: " + devId, e);
			throw e;
		}
		logger.debug("sftp end create directory, result: " + result + ", devId: " + devId);
		return result;
	}

	/**
	 * Sftp登录
	 *
	 * @return
	 */
	@Override
	public void login() throws Exception {
		logger.debug("sftp begin login, devId: " + devId);

		try {
			JSch jSch = new JSch();
			session = jSch.getSession(userName, ip, port);
			session.setPassword(password);
			//设置Sftp连接参数信息
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			// 为Session对象设置properties
			session.setConfig(config);
			// 设置timeout时间
			if (!BlankUtil.isBlank(timeout)) {
				session.setTimeout(timeout);
			}
			logger.debug("sftp login, IP: " + this.ip + ", PORT: " + this.port
					+ ", USERNAME: " + this.userName + ", PASSWORD: " + this.password + ", devId: " + this.devId);
			session.connect();
			logger.debug("sftp session connected, devId: " + devId);

			Channel sftpChannel = session.openChannel("sftp");
			sftpChannel.connect();
			channel = (ChannelSftp) sftpChannel;
		} catch (Exception e) {
			logger.error("ftp server connection fail, IP:" + this.ip + ", PORT:" + this.port + ", devId: " + devId, e);
			this.close();
			throw e;
		}
		logger.debug("sftp connected successfully, info:" + this.toString());
	}

	@Override
	public boolean getPasvMode() throws Exception {
		return false;
	}

	@Override
	public void setPasvMode(Boolean isPasvMode) throws Exception {

	}

	@Override
	public ByteArrayOutputStream getFileStream(String remotePath) throws Exception {
		logger.debug("begin get sftp remote file, remote path: " + remotePath + ", devId: " + devId);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			channel.get(remotePath, bos);
		} catch (Exception e) {
			IOUtils.closeQuietly(bos);

			throw e;
		}

		logger.debug("end get sftp remote file, devId: " + devId);
		return bos;
	}

	@Override
	public boolean putFileStream(InputStream srcInputStream, String dstPath) throws Exception {
		logger.debug("begin put sftp remote file, remote path: " + dstPath + ", devId: " + devId);
		//临时文件后缀
		//String tmpFileName = dstPath + "." + DcmSystem.random(10000) + ".tmp";

		channel.put(srcInputStream, dstPath);
		logger.debug("begin put to sftp, result: true, devId: " + devId);
		//channel.rename(dstPath, dstPath);

		return true;
	}

	@Override
	public String toString() {
		return "SftpTran [channel=" + channel
				+ ", ip=" + ip + ", port=" + port + ", userName=" + userName
				+ ", password=" + password + ", timeout=" + timeout + ", devId=" + devId + "]";
	}

}
