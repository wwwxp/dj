//package com.tydic.util;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Vector;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.apache.log4j.Logger;
//
//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.StreamGobbler;
//
//import com.jcraft.jsch.Channel;
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//import com.jcraft.jsch.SftpException;
//import com.tydic.bp.common.utils.tools.CommonTool;
//import com.tydic.bp.common.utils.tools.DesTool;
//import com.tydic.bp.core.utils.properties.SystemProperty;
//
//public class SFTPChannel {
//	private static Session session = null;
//	private static Channel channel = null;
//	private static ch.ethz.ssh2.Session ssh2Session = null;
//
//	private static final Logger LOG = Logger.getLogger(SFTPChannel.class);
//
//	/**
//	 * user 用户名 host 主机 port 端口
//	 *
//	 * @throws JSchException
//	 */
//	public static Session getConnect(String user, String passwd, String host,
//			int port,int timeout) throws JSchException{
//		JSch jsch = new JSch(); // 创建JSch对象
//		try {
//			session = jsch.getSession(user, host, port);
//			LOG.debug("Session created.");
//			if (passwd != null) {
//				session.setPassword(passwd); // 设置密码
//			}
//			Properties config = new Properties();
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config); // 为Session对象设置properties
//			session.setTimeout(timeout); // 设置timeout时间
//			session.connect(); // 通过Session建立链接
//			LOG.debug("Session connected.");
//			channel = session.openChannel("sftp"); // 打开SFTP通道
//			LOG.debug("Opening Channel.");
//			channel.connect(); // 建立SFTP通道的连接
//			LOG.debug("Connected successfully to ftpHost = " + host
//					+ ",as ftpUserName = " + user + ", returning: "
//					+ channel);
//		} catch (JSchException e) {
//			throw e;
//		}
//		return session;
//	}
//
//	/**
//	 * user 用户名 host 主机 port 端口
//	 *
//	 * @throws JSchException
//	 */
//	public static Session getConnectForSuperVisor(String user, String passwd, String host,
//			int port,int timeout) throws JSchException{
//		JSch jsch = new JSch(); // 创建JSch对象
//		try {
//			session = jsch.getSession(user, host, port);
//			LOG.debug("Session created.");
//			if (passwd != null) {
//				session.setPassword(passwd); // 设置密码
//			}
//			Properties config = new Properties();
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config); // 为Session对象设置properties
//			session.setTimeout(timeout); // 设置timeout时间
//			session.connect(); // 通过Session建立链接
//			LOG.debug("Session connected.");
//
//		} catch (JSchException e) {
//			throw e;
//		}
//		return session;
//	}
//
//	public static ch.ethz.ssh2.Session getSSH2Session(
//			Map<String, String> sftpDetails) {
//		String hostname = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String username = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		Connection conn = new Connection(hostname);
//		try {
//			conn.connect();
//			boolean isAuthenticated = conn.authenticateWithPassword(username,
//					ftpPassword);
//			if (isAuthenticated == false)
//				throw new IOException("Authentication failed.");
//			ssh2Session = conn.openSession();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return ssh2Session;
//	}
//
//	public static void getChannel(Map<String, String> sftpDetails, int timeout,boolean isMultipleFile) {
//		LOG.debug("sftp:" + sftpDetails);
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String targert = sftpDetails.get(SFTPConstants.SFTP_TAGRTPATH);
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//
//			session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			csftp = (ChannelSftp) channel;
//			Vector<?> vt = csftp.ls(installPath);
//			if (vt.isEmpty()) {
//				csftp.mkdir(installPath);
//			}
//			if(isMultipleFile){
//				String [] files = targert.split(",");
//				for(String file:files){
//					OutputStream out = csftp.put(installPath+file, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//					byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//					int read;
//					if (out != null) {
//						LOG.info("Start to read input stream");
//						InputStream is = new FileInputStream(localpath + file);
//						do {
//							read = is.read(buff, 0, buff.length);
//							if (read > 0) {
//								out.write(buff, 0, read);
//							}
//							out.flush();
//						} while (read >= 0);
//						LOG.info("input stream read done.");
//
//				   }
//				}
//			}else{
//				OutputStream out = csftp.put(installPath+targert, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//				byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//				int read;
//				if (out != null) {
//					LOG.info("Start to read input stream");
//					InputStream is = new FileInputStream(localpath);
//					do {
//						read = is.read(buff, 0, buff.length);
//						if (read > 0) {
//							out.write(buff, 0, read);
//						}
//						out.flush();
//					} while (read >= 0);
//					LOG.info("input stream read done.");
//
//			   }
//			}
//
//			csftp.quit();
//			closeChannel();
//		} catch (Exception e) {
//			e.printStackTrace();
//			csftp.quit();
//			closeChannel();
//			throw new RuntimeException("上传文件失败", e);
//		}
//
//	}
//	/**
//	 * topology 从 web端分发到主机
//	 * @param sftpDetails
//	 * @param timeout
//	 * @param fileName
//	 * @param index
//	 */
//
//	public static void getTopolgoyChannel(Map<String, String> sftpDetails, int timeout,String fileName,int index){
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String targertFileName = sftpDetails.get(SFTPConstants.SFTP_TAGRTPATH);
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		LOG.debug("sftp:" + sftpDetails);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//			FileUtil.writeFile(fileName, "第" + index + "台主机准备中" + System.getProperty("line.separator"));
//			try{
//				session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			}catch(Exception e){
//				FileUtil.writeFile(fileName, ftpHost+" FTP连接失败,失败原因："+e.getMessage() + System.getProperty("line.separator"));
//				throw new RuntimeException("上传文件失败", e);
//			}
//
//			FileUtil.writeFile(fileName, "已连接成功 ，主机：" + ftpHost +System.getProperty("line.separator"));
//			csftp = (ChannelSftp) channel;
//			try{
//				Vector vt = csftp.ls(installPath);
//				if (vt == null || vt.isEmpty()) {
//					LOG.info("目录不存在 ，创建目录：" +installPath);
//					csftp.mkdir(installPath);
//				}
//			}catch(SftpException e){
//				LOG.info("目录不存在 ，需要创建目录：");
//				String filepath = installPath;
//				while(filepath != null){
//					filepath = filepath.substring(0,filepath.lastIndexOf("/"));
//					try{
//						 csftp.ls(filepath);
//						 filepath = null;
//					}catch(Exception e2){
//						try{
//							LOG.info("创建目录:"+filepath);
//							csftp.mkdir(filepath);
//							LOG.info("创建目录成功:"+filepath);
//							filepath = installPath;
//						}catch(Exception e1){
//							LOG.error("目录创建失败:"+e1);
//
//						}
//					}
//				}
//			}
//			OutputStream out = csftp.put(installPath+targertFileName, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//			byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//			int read;
//			if (out != null) {
//				LOG.info("Start to read input stream");
//				FileUtil.writeFile(fileName, "读取文件进行中。。。" +System.getProperty("line.separator"));
//				InputStream is = new FileInputStream(localpath);
//				do {
//					read = is.read(buff, 0, buff.length);
//					if (read > 0) {
//						out.write(buff, 0, read);
//					}
//					out.flush();
//				} while (read >= 0);
//				FileUtil.writeFile(fileName, "复制文件完成。。。" +System.getProperty("line.separator"));
//				LOG.info("input stream read done.");
//			}
//			//FileUtil.writeFile(fileName, "复制文件错误，未找到文件，请检查！" +System.getProperty("line.separator"));
//			Connection conn = new Connection(ftpHost);
//
//			conn.connect();
//			boolean isAuthenticated = conn.authenticateWithPassword(ftpUserName,
//					ftpPassword);
//			if (isAuthenticated == false){
//				throw new IOException("Authentication failed.");
//			}
//
//			ssh2Session = conn.openSession();
//			String stderrInfo = "";
//			StringBuffer commondStr = new StringBuffer("cd ");
//			commondStr.append(installPath)
//			//.append(";rm -rf ").append(targertFileName)
//			.append(";unzip -o ").append(targertFileName);
//			LOG.info(commondStr);
//			ssh2Session.execCommand(commondStr.toString());
//			InputStream stderrInput = new StreamGobbler(ssh2Session.getStderr());
//			BufferedReader  stderrBuff= new BufferedReader(
//					new InputStreamReader(stderrInput));
//			StringBuffer stderrString = new StringBuffer();
//			while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//				stderrString.append(stderrInfo);
//			}
//			if (!stderrString.toString().trim().equals("")){
//				FileUtil.writeFile(fileName, "解压文件失败："+stderrString.toString().trim() +System.getProperty("line.separator"));
//			}else{
//				FileUtil.writeFile(fileName, "解压文件完成。。。" +System.getProperty("line.separator"));
//			}
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			conn.close();
//		} catch (Exception e) {
//			FileUtil.writeFile(fileName, "读取文件失败，部署失败:"+e.getMessage() +System.getProperty("line.separator"));
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			throw new RuntimeException("上传文件失败", e);
//		}finally {
//			FileUtil.writeFile(fileName, "---------------------" +System.getProperty("line.separator"));
//		}
//
//	}
//
//	/**
//	 * zip 从 web端分发到主机
//	 * @param sftpDetails
//	 * @param timeout
//	 * @param fileName
//	 * @param index
//	 */
//
//	public static void getChannelZIP(Map<String, String> sftpDetails, int timeout,boolean isZip){
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword ="";
//		try{
//			ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		}catch(Exception e){
//			e.printStackTrace();
//			throw new RuntimeException("密码错误", e);
//		}
//		String targertFileName = sftpDetails.get(SFTPConstants.SFTP_TAGRTPATH);
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		LOG.debug("sftp:" + sftpDetails);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//			try{
//				session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			}catch(Exception e){
//				throw new RuntimeException("上传文件失败", e);
//			}
//
//			csftp = (ChannelSftp) channel;
//			try{
//				Vector vt = csftp.ls(installPath);
//				if (vt == null || vt.isEmpty()) {
//					LOG.info("目录不存在 ，创建目录：" +installPath);
//					csftp.mkdir(installPath);
//				}
//			}catch(SftpException e){
//				LOG.info("目录不存在 ，需要创建目录：");
//				String filepath = installPath;
//				while(filepath != null){
//					filepath = filepath.substring(0,filepath.lastIndexOf("/"));
//					try{
//						 csftp.ls(filepath);
//						 filepath = null;
//					}catch(Exception e2){
//						try{
//							LOG.info("创建目录:"+filepath);
//							csftp.mkdir(filepath);
//							LOG.info("创建目录成功:"+filepath);
//							filepath = installPath;
//						}catch(Exception e1){
//							LOG.error("目录创建失败:"+e1);
//
//						}
//					}
//				}
//			}
//			OutputStream out = csftp.put(installPath+targertFileName, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//			byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//			int read;
//			if (out != null) {
//				LOG.info("Start to read input stream");
//				InputStream is = new FileInputStream(localpath);
//				do {
//					read = is.read(buff, 0, buff.length);
//					if (read > 0) {
//						out.write(buff, 0, read);
//					}
//					out.flush();
//				} while (read >= 0);
//				LOG.info("input stream read done.");
//			}
//			if(isZip){
//				Connection conn = new Connection(ftpHost);
//
//				conn.connect();
//				boolean isAuthenticated = conn.authenticateWithPassword(ftpUserName,
//						ftpPassword);
//				if (isAuthenticated == false){
//					throw new IOException("Authentication failed.");
//				}
//
//				ssh2Session = conn.openSession();
//				String stderrInfo = "";
//				StringBuffer commondStr = new StringBuffer("cd ");
//				commondStr.append(installPath)
//				//.append(";rm -rf ").append(targertFileName)
//				.append(";unzip -o ").append(targertFileName);
//				LOG.info(commondStr);
//				ssh2Session.execCommand(commondStr.toString());
//				InputStream stderrInput = new StreamGobbler(ssh2Session.getStderr());
//				BufferedReader  stderrBuff= new BufferedReader(
//						new InputStreamReader(stderrInput));
//				StringBuffer stderrString = new StringBuffer();
//				while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//					stderrString.append(stderrInfo);
//				}
//				if (!stderrString.toString().trim().equals("")){
//				}else{
//				}
//				conn.close();
//			}
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//
//		} catch (Exception e) {
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			throw new RuntimeException("上传文件失败", e);
//		}finally {
//		}
//
//	}
//
//	/**
//	 * 硬 件主机 从 web端分发到主机 ，  准备环境
//	 * @param sftpDetails
//	 * @param timeout
//	 * @param fileName
//	 * @param index
//	 */
//
//	public static void getHostEnvChannel(Map<String, String> sftpDetails, int timeout,String fileName,int index,boolean zipFlag) throws Exception{
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String targertFileName = sftpDetails.get(SFTPConstants.SFTP_TAGRTPATH);
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		LOG.debug("sftp:" + sftpDetails);;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//			FileUtil.writeFile(fileName, "第" + index + "台主机准备中" + System.getProperty("line.separator"));
//			try{
//				session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			}catch(Exception e){
//				FileUtil.writeFile(fileName, "FTP连接失败,失败原因："+e.getMessage() + System.getProperty("line.separator"));
//				throw new RuntimeException("上传文件失败", e);
//			}
//
//			FileUtil.writeFile(fileName, "已连接成功 ，主机：" + ftpHost +System.getProperty("line.separator"));
//			csftp = (ChannelSftp) channel;
//			try{
//				Vector vt = csftp.ls(installPath);
//				if (vt == null || vt.isEmpty()) {
//					LOG.info("目录不存在 ，创建目录：" +installPath);
//					csftp.mkdir(installPath);
//				}
//			}catch(SftpException e){
//				LOG.info("目录不存在 ，需要创建目录：");
//				String filepath = installPath;
//				while(filepath != null){
//					filepath = filepath.substring(0,filepath.lastIndexOf("/"));
//					try{
//						 csftp.ls(filepath);
//						 filepath = null;
//					}catch(Exception e2){
//						try{
//							LOG.info("创建目录:"+filepath);
//							csftp.mkdir(filepath);
//							LOG.info("创建目录成功:"+filepath);
//							filepath = installPath;
//						}catch(Exception e1){
//							LOG.error("目录创建失败:"+e1);
//
//						}
//					}
//				}
//
//			}
//			OutputStream out = csftp.put(installPath+targertFileName, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//			byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//			int read;
//			if (out != null) {
//				LOG.info("Start to read input stream");
//				FileUtil.writeFile(fileName, "读取文件进行中。。。" +System.getProperty("line.separator"));
//				InputStream is = new FileInputStream(localpath);
//				do {
//					read = is.read(buff, 0, buff.length);
//					if (read > 0) {
//						out.write(buff, 0, read);
//					}
//					out.flush();
//				} while (read >= 0);
//				FileUtil.writeFile(fileName, "复制文件完成。。。" +System.getProperty("line.separator"));
//				LOG.info("input stream read done.");
//			}
//			if(zipFlag){
//				ssh2Session = getSSH2Session(sftpDetails);
//				String stderrInfo = "";
//				StringBuffer commondStr = new StringBuffer("cd ");
//				commondStr.append(installPath)
//				//.append(";rm -rf ").append(targertFileName)
//				.append(";unzip -o ").append(targertFileName);
//				LOG.info(commondStr);
//				ssh2Session.execCommand(commondStr.toString());
//				LOG.info("命令执行完");
//				InputStream in = ssh2Session.getStdout();
//				//必须要让SSH读取完控制台信息，要不然程序会卡死
//				 processStdout(in, "UTF-8");
//				LOG.info("---------------------------------");
//				String error = processStdout(ssh2Session.getStderr(), "UTF-8");
//				if (!error.equals("")){
//					FileUtil.writeFile(fileName, "解压文件失败："+error +System.getProperty("line.separator"));
//					throw new Exception("文件解压失败");
//				}else{
//					FileUtil.writeFile(fileName, "解压文件完成。。。" +System.getProperty("line.separator"));
//				}
//				ssh2Session.close();
//			}
//			csftp.quit();
//			closeChannel();
//
//		} catch (Exception e) {
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			FileUtil.writeFile(fileName, "读取文件失败，部署失败:"+e.getMessage() +System.getProperty("line.separator"));
//			throw new Exception("上传文件失败", e);
//		}finally {
//			FileUtil.writeFile(fileName, "---------------------" +System.getProperty("line.separator"));
//		}
//
//	}
//
//
//	/**
//	 * 硬 件主机 从 web端整个目录分发 ，  准备环境
//	 * @param sftpDetails
//	 * @param timeout
//	 * @param fileName
//	 * @param index
//	 */
//
//	public static void getChannelbyDir(Map<String, String> sftpDetails, int timeout,String writeFile,int index,boolean zipFlag) throws Exception{
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String targertFileName = sftpDetails.get(SFTPConstants.SFTP_TAGRTPATH);
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		LOG.debug("sftp:" + sftpDetails);
//		LOG.debug("writeFile:" + writeFile);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//			//FileUtil.writeFile(writeFile, "第" + index + "台主机准备中" + System.getProperty("line.separator"));
//			session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			//FileUtil.writeFile(writeFile, "已连接成功 ，主机：" + ftpHost +System.getProperty("line.separator"));
//			csftp = (ChannelSftp) channel;
//			List<String> fileNames ;
//
//			if(zipFlag){
//				fileNames = FileUtil.getFiles(localpath,".zip");
//			}else{
//				fileNames = FileUtil.getFiles(localpath);
//			}
//			LOG.info("获取文件：" +fileNames);
//
//			try{
//				Vector vt = csftp.ls(installPath);
//				if (vt == null || vt.isEmpty()) {
//					LOG.info("目录不存在 ，创建目录：" +installPath);
//					csftp.mkdir(installPath);
//				}
//			}catch(SftpException e){
//				LOG.info("目录不存在 ，创建目录：" +installPath);
//				try{
//					csftp.mkdir(installPath);
//				}catch(Exception e1){
//					LOG.error("目录创建失败:"+e1);
//					throw new Exception("目录不存在, 创建目录失败");
//				}
//			}
//			LOG.info("进入目录：" +installPath);
//		   for(int i = 0 ; i < fileNames.size() ; i ++){
//
//	            OutputStream out = csftp.put(installPath+fileNames.get(i), ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//				byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//				int read;
//				if (out != null) {
//					LOG.info("Start to read input stream");
//					//FileUtil.writeFile(fileNames.get(i), "读取文件进行中。。。" +System.getProperty("line.separator"));
//					InputStream is = new FileInputStream(localpath+fileNames.get(i));
//					do {
//						read = is.read(buff, 0, buff.length);
//						if (read > 0) {
//							out.write(buff, 0, read);
//						}
//						out.flush();
//					} while (read >= 0);
//					//FileUtil.writeFile(fileNames.get(i), "复制文件完成。。。" +System.getProperty("line.separator"));
//					LOG.info("input stream read done.");
//				}
//		    }
//
//			if(zipFlag){
//				LOG.info("解压文件了");
//				ssh2Session = getSSH2Session(sftpDetails);
//				String stderrInfo = "";
//				StringBuffer commondStr = new StringBuffer();
//				commondStr.append("cd ");
//				commondStr.append(installPath);
//				for(int i = 0 ; i < fileNames.size() ; i ++){
//					commondStr.append(";unzip -o ").append(fileNames.get(i));
//				}
//				LOG.info(commondStr);
//				ssh2Session.execCommand(commondStr.toString());
//				InputStream stderrInput = new StreamGobbler(ssh2Session.getStderr());
//				BufferedReader  stderrBuff= new BufferedReader(
//						new InputStreamReader(stderrInput));
//				StringBuffer stderrString = new StringBuffer();
//				while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//					stderrString.append(stderrInfo);
//				}
//				if (!stderrString.toString().trim().equals("")){
//					//FileUtil.writeFile(writeFile, "解压文件失败："+stderrString.toString().trim() +System.getProperty("line.separator"));
//					throw new Exception("文件解压失败");
//				}else{
//					LOG.info("解压完成");
//					//FileUtil.writeFile(writeFile, "解压文件完成。。。" +System.getProperty("line.separator"));
//				}
//
//				ssh2Session.close();
//			}
//			csftp.quit();
//			closeChannel();
//
//		} catch (Exception e) {
//			LOG.error("错误："+e);
//			try{
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			}catch(Exception e1){
//
//			}
//			//FileUtil.writeFile(writeFile, "读取文件失败，部署失败:"+e.getMessage() +System.getProperty("line.separator"));
//			throw new Exception("上传文件失败", e);
//		}finally {
//			//FileUtil.writeFile(writeFile, "---------------------" +System.getProperty("line.separator"));
//		}
//
//	}
//
//	/**
//	 * 硬 件主机 从 web端整个目录分发 ，  准备环境(supervisor使用)
//	 * @param sftpDetails
//	 * @param timeout
//	 * @param fileName
//	 * @param index
//	 */
//	public static void getChannelbyDirForSuperVisor(Map<String, String> sftpDetails, int timeout,String writeFile,int index,boolean zipFlag) throws Exception{
//		String ftpHost = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String port = sftpDetails.get(SFTPConstants.SFTP_REQ_PORT);
//		String ftpUserName = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String localpath = sftpDetails.get(SFTPConstants.SFTP_LOCALPATH);
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		//installPath+="yewenjie/";
//		LOG.debug("sftp:" + sftpDetails);
//		LOG.debug("writeFile:" + writeFile);
//		int ftpPort = SFTPConstants.SFTP_DEFAULT_PORT;
//		if (port != null && !port.equals("")) {
//			ftpPort = Integer.valueOf(port);
//		}
//		ChannelSftp csftp = null;
//		try {
//			session = getConnect(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//			csftp = (ChannelSftp) channel;
//			List<String> fileNames ;
//
//			if(zipFlag){
//				fileNames = FileUtil.getFiles(localpath,".zip");
//			}else{
//				fileNames = FileUtil.getFiles(localpath);
//			}
//			LOG.info("获取文件：" +fileNames);
//
//			try{
//				Vector vt = csftp.ls(installPath);
//				if (vt == null || vt.isEmpty()) {
//					LOG.info("目录不存在 ，创建目录：" +installPath);
//					csftp.mkdir(installPath);
//				}
//			}catch(SftpException e){
//				LOG.info("目录不存在 ，创建目录：" +installPath);
//				try{
//					csftp.mkdir(installPath);
//				}catch(Exception e1){
//					LOG.error("目录创建失败:"+e1);
//					throw new Exception("目录不存在, 创建目录失败");
//				}
//			}
//			LOG.info("进入目录：" +installPath);
//			csftp.quit();
//			closeChannel();
//			ExecutorService pool=Executors.newFixedThreadPool(8);
//			//每批处理的个数
//			int dealTotal = Integer.valueOf(CommonTool.defaultStr(SystemProperty.getContextProperty("dealFileTotal"),"10"));
//			List<String> dealFileName=new ArrayList<String>();
//			LOG.info("fileNames size --->" +fileNames.size());
//		   for(int i = 0 ; i < fileNames.size() ; i ++){
//			    String fileName=fileNames.get(i);
//			    if((dealFileName.size() > 0 && dealFileName.size()%(dealTotal-1) == 0 )|| i == fileNames.size()-1){
//		            dealFileName.add(fileName);
//					Session session = getConnectForSuperVisor(ftpUserName, ftpPassword, ftpHost, ftpPort,timeout);
//					Channel channel = session.openChannel("sftp");
//					channel.connect();
//					csftp = (ChannelSftp) channel;
//					ch.ethz.ssh2.Session ssh2Session = getSSH2Session(sftpDetails);
//					LOG.debug("dealFileName size begin--->"+dealFileName.size());
//		            pool.execute(new WriteFileThread(dealFileName, localpath,installPath,session,ssh2Session,channel,csftp,zipFlag));
//		            dealFileName=new ArrayList<String>();
//			    }else{
//			    	dealFileName.add(fileName);
//			    }
//
//		    }
//		} catch (Exception e) {
//			LOG.error("错误："+e);
//			try{
//			csftp.quit();
//			closeChannel();
//			ssh2Session.close();
//			}catch(Exception e1){
//
//			}
//			throw new Exception("上传文件失败", e);
//		}finally {
//		}
//
//	}
//
//	/**
//	 * 写文件内部类
//	 *
//	 */
//	public static class WriteFileThread implements Runnable{
//		List<String> dealFileName;
//		String localpath;
//		String installPath;
//		Session session;
//		Channel channel;
//		ChannelSftp csftp;
//		ch.ethz.ssh2.Session ssh2Session;
//		boolean zipFlag;
//		public WriteFileThread(List<String> dealFileName,String localpath,String installPath,Session session,
//				ch.ethz.ssh2.Session ssh2Session,Channel channel,ChannelSftp csftp,boolean zipFlag){
//			this.dealFileName = dealFileName;
//			this.localpath = localpath;
//			this.installPath = installPath;
//			this.session=session;
//			this.channel=channel;
//			this.csftp=csftp;
//			this.ssh2Session=ssh2Session;
//			this.zipFlag=zipFlag;
//		}
//
//		@Override
//		public void run() {
//			LOG.debug("WriteFileThread 开始写文件！！！--- > dealFileName="+dealFileName);
//			byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
//			int read;
//			try {
//				for(int i = 0 ; i < dealFileName.size() ; i ++){
//					String fileName=dealFileName.get(i);
//					OutputStream out = csftp.put(installPath+fileName, ChannelSftp.OVERWRITE); // 使用OVERWRITE模式
//
//					if (out != null) {
//						LOG.debug("Start to read input stream");
//						//FileUtil.writeFile(fileNames.get(i), "读取文件进行中。。。" +System.getProperty("line.separator"));
//						InputStream is = new FileInputStream(localpath+fileName);
//						do {
//							read = is.read(buff, 0, buff.length);
//							if (read > 0) {
//								out.write(buff, 0, read);
//							}
//							out.flush();
//						} while (read >= 0);
//						//FileUtil.writeFile(fileNames.get(i), "复制文件完成。。。" +System.getProperty("line.separator"));
//						LOG.debug("WriteFileThread 单个写文件结束！！！--- > fileName="+fileName);
//
//
//					}
//				}
//				if(zipFlag){
//					LOG.info("开始解压文件");
//
//					String stderrInfo = "";
//					StringBuffer commondStr = new StringBuffer();
//					commondStr.append("cd ");
//					commondStr.append(installPath);
//					for(int i = 0 ; i < dealFileName.size() ; i ++){
//						commondStr.append(";unzip -o ").append(dealFileName.get(i));
//					}
//					LOG.info(commondStr);
//					ssh2Session.execCommand(commondStr.toString());
//					InputStream stderrInput = new StreamGobbler(ssh2Session.getStderr());
//					BufferedReader  stderrBuff= new BufferedReader(
//							new InputStreamReader(stderrInput));
//					StringBuffer stderrString = new StringBuffer();
//					while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//						stderrString.append(stderrInfo);
//					}
//					if (!stderrString.toString().trim().equals("")){
//						//FileUtil.writeFile(writeFile, "解压文件失败："+stderrString.toString().trim() +System.getProperty("line.separator"));
//						throw new Exception("文件解压失败");
//					}else{
//						LOG.info("解压完成");
//						//FileUtil.writeFile(writeFile, "解压文件完成。。。" +System.getProperty("line.separator"));
//					}
//				}
//			} catch (Exception e) {
//				LOG.debug("WriteFileThread 写文件异常");
//				LOG.debug("e exception --->"+e);
//				e.printStackTrace();
//			}finally{
//				csftp.quit();
//				if (channel != null) {
//					channel.disconnect();
//				}
//				if (session != null) {
//					session.disconnect();
//				}
//				if(ssh2Session != null){
//					ssh2Session.close();
//				}
//			}
//		}
//	}
//
//	public static void isExists(Map<String, String> sftpDetails) {
//		String hostname = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String username = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		Connection conn = new Connection(hostname);
//		ch.ethz.ssh2.Session sess = null;
//		try {
//			conn.connect();
//			boolean isAuthenticated = conn.authenticateWithPassword(username,
//					ftpPassword);
//			if (isAuthenticated == false)
//				throw new IOException("Authentication failed.");
//			sess = conn.openSession();
//			sess.execCommand("cd " + installPath);
//			InputStream stderr = new StreamGobbler(sess.getStderr());
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(stderr));
//			while (true) {
//				String line = br.readLine();
//				if (line != null) {
//					sess.close();
//					sess = conn.openSession();
//					sess.execCommand("mkdir " + installPath);
//				}
//				break;
//			}
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		conn.close();
//		sess.close();
//	}
//
//	public static boolean exec(Map<String, String> sftpDetails) {
//		String hostname = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String username = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String command = sftpDetails.get(SFTPConstants.SFTP_COMMAND);
//		Connection conn = new Connection(hostname);
//		boolean issuccess = true;
//		try {
//			conn.connect();
//			/* Authenticate */
//			boolean isAuthenticated = conn.authenticateWithPassword(username,
//					ftpPassword);
//			if (isAuthenticated == false)
//				throw new IOException("Authentication failed.");
//			/* Create a session */
//			ch.ethz.ssh2.Session sess = conn.openSession();
//			sess.execCommand(command);
//			// sess.execCommand("cd "+installPath+"/tws;./start.sh "+zookeeperHost+":"+zookeeperPort);
//			System.out
//					.println("Here is some information about the remote host:");
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(stdout));
//			String line = "";
//			while (true) {
//				line = br.readLine();
//				if (line == null)
//					break;
//				LOG.info(line);
//			}
//			if ("".equals(line) || null == line) {
//				issuccess = false;
//			}
//			/* Show exit st atus, if available (otherwise "null") */
//			LOG.info("ExitCode: " + sess.getExitStatus());
//			/* Close this session */
//			sess.close();
//			/* Close the connection */
//			conn.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("启动失败", e);
//		}
//		return issuccess;
//	}
//
//	/**
//	 * 执行相关的命令
//	 *
//	 * @throws JSchException
//	 */
//	public static void execCmd(Map<String, String> sftpDetails) {
//
//		String hostname = sftpDetails.get(SFTPConstants.SFTP_REQ_HOST);
//		String username = sftpDetails.get(SFTPConstants.SFTP_REQ_USERNAME);
//		String ftpPassword = DesTool.dec(sftpDetails.get(SFTPConstants.SFTP_REQ_PASSWORD));
//		String installPath = sftpDetails.get(SFTPConstants.SFTP_INSTALL_PATH);
//		String installFile = sftpDetails.get(SFTPConstants.SFTP_INSTALL_FILE);
//		String zookeeperHost = sftpDetails
//				.get(SFTPConstants.SFTP_ZOOKEEPER_HOST);
//		String zookeeperPort = sftpDetails
//				.get(SFTPConstants.SFTP_ZOOKEEPER_PORT);
//		try {
//			/* Create a connection instance */
//			Connection conn = new Connection(hostname);
//			conn.connect();
//			/* Authenticate */
//			boolean isAuthenticated = conn.authenticateWithPassword(username,
//					ftpPassword);
//			if (isAuthenticated == false)
//				throw new IOException("Authentication failed.");
//			/* Create a session */
//			ch.ethz.ssh2.Session sess = conn.openSession();
//			// sess.execCommand("cd /home/tws1;tar -zxvf tws-1.0.tar.gz;cd /home/tws1/tws-1.0;sh setup.sh /home/tws1/testinstall");
//			LOG.info("cd " + installPath + ";tar -zxvf "
//					+ installFile + ";cd /home/tws1/tws-1.0;sh setup.sh "
//					+ installPath + ";cd " + installPath + "/tws;./start.sh "
//					+ zookeeperHost + ":" + zookeeperPort);
//			sess.execCommand("cd " + installPath + ";tar -zxvf " + installFile
//					+ ";cd " + installPath + "/tws-1.0;sh setup.sh "
//					+ installPath + ";cd " + installPath + "/tws;./start.sh "
//					+ zookeeperHost + ":" + zookeeperPort);
//			System.out
//					.println("Here is some information about the remote host:");
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(stdout));
//			while (true) {
//				String line = br.readLine();
//				if (line == null)
//					break;
//				LOG.info(line);
//			}
//			/* Show exit st atus, if available (otherwise "null") */
//			LOG.info("ExitCode: " + sess.getExitStatus());
//			/* Close this session */
//			sess.close();
//			/* Close the connection */
//			conn.close();
//		} catch (IOException e) {
//			e.printStackTrace(System.err);
//			throw new RuntimeException("execCmd 执行失败", e);
//		}
//	}
//
//	public static void closeChannel() {
//		if (channel != null) {
//			channel.disconnect();
//		}
//		if (session != null) {
//			session.disconnect();
//		}
//	}
//
//	public static String processStdout(InputStream in, String charset) {
//		byte[] buf = new byte[1024];
//		StringBuffer sb = new StringBuffer();
//		try {
//			while (in.read(buf) != -1) {
//				sb.append(new String(buf, charset));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return sb.toString();
//	}
//
//	public static void main(String[] args) throws Exception{
//
//		//sftp:{port=22, username=sh_dca, host=192.168.171.169, localpath=/public/sh_dca/myweb/topology/, installPath=/public/sh_dca/myapp/topology/, password=FEB473BD170FAF758FFB45BF04748D31}
//		 Map<String,String> sftpDetails = new HashMap<String,String>();
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "192.168.171.169");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "22");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "sh_dca");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "FEB473BD170FAF758FFB45BF04748D31");
//		 sftpDetails.put(SFTPConstants.SFTP_LOCALPATH,"D:/tmp/tmpa/");
//		 sftpDetails.put(SFTPConstants.SFTP_INSTALL_PATH, "/public/sh_dca/myapp/topology/");
//		 String filepath = "/home/bp/aa/bb/cc/";
//		 int i = 4;
//			while(filepath != null){
//				i--;
//				filepath = filepath.substring(0,filepath.lastIndexOf("/"));
//				LOG.info(filepath);
//				try{
//					if(i==0){
//					 filepath = null;
//					}
//				}catch(Exception e2){
//
//				}
//			}
//
//		 //getChannelbyDir(sftpDetails,1000,null,1,true);
///*
//		 Map<String,String> sftpDetails = new HashMap<String,String>();
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "192.168.171.169");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "22");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "sh_dca");
//		 sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "FEB473BD170FAF758FFB45BF04748D31");
//
//		 LOG.info("解压文件了...");
//			ssh2Session = getSSH2Session(sftpDetails);
//			List<String> fileNames = new ArrayList<>();
//			fileNames.add("topology_hb_v0.1.zip");
//			fileNames.add("topology_in_v0.1.zip");
//			StringBuffer commondStr = new StringBuffer();
//			commondStr.append("cd ");
//			commondStr.append("/public/sh_dca/myapp/topology/");
//			for(int i = 0 ; i < fileNames.size() ; i ++){
//
//				commondStr.append(";unzip ").append(fileNames.get(i)).append(";rm -rf ").append(fileNames.get(i));;
//
//			}
//			LOG.info(commondStr);
//			ssh2Session.execCommand(commondStr.toString());
//			InputStream stderrInput = new StreamGobbler(ssh2Session.getStderr());
//			BufferedReader  stderrBuff= new BufferedReader(
//					new InputStreamReader(stderrInput));
//			StringBuffer stderrString = new StringBuffer();
//			String stderrInfo;
//			while ((stderrInfo = stderrBuff.readLine()) != null) {//错误流
//				stderrString.append(stderrInfo);
//			}
//			if (!stderrString.toString().trim().equals("")){
//				//FileUtil.writeFile(writeFile, "解压文件失败："+stderrString.toString().trim() +System.getProperty("line.separator"));
//				throw new Exception("文件解压失败");
//			}else{
//				//FileUtil.writeFile(writeFile, "解压文件完成。。。" +System.getProperty("line.separator"));
//			}
//
//			ssh2Session.close();*/
//
//		 return;
//
//		/*
//		 * Map<String,String> sftpDetails = new HashMap<String,String>();
//		 * sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "192.168.161.149");
//		 * sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "22");
//		 * sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "root");
//		 * sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "dic123"); String
//		 * commond = "cd /home/tws1"; try { execCmd(sftpDetails,commond); }
//		 * catch (JSchException e) { // TODO Auto-generated catch block
//		 * e.printStackTrace(); }
//		 */
//		/*String hostname = "192.168.171.162";
//		String username = "storm";
//		String password = "storm01";
//		Connection conn = null;
//		ch.ethz.ssh2.Session sess = null;
//		try {
//			 Create a connection instance
//			conn = new Connection(hostname);
//			conn.connect();
//			 Authenticate
//			boolean isAuthenticated = conn.authenticateWithPassword(username,
//					password);
//			if (isAuthenticated == false)
//				throw new IOException("Authentication failed.");
//			 Create a session
//			sess = conn.openSession();
//			sess.execCommand("cd /home/storm/myweb;nohup ./zip.sh mytest.zip zw.zip > .zw.log&");
//			// 如果使用自定义shell命令则是：sess.execCommand("/home/test.sh");如果是系统shell命令则不需要考虑绝对路径：sess.execCommand("ps aux");
//			// cd /home/tws1;tar -zxvf tws-1.0.tar.gz;sh
//			// /home/tws1/tws-1.0/setup.sh /home/test
//			System.out
//					.println("Here is some information about the remote host:");
//			InputStream stdout = new StreamGobbler(sess.getStdout());
//			BufferedReader br = new BufferedReader(
//					new InputStreamReader(stdout));
//			//LOG.info(br.readLine());
//
//			String line = "";
//			while (true) {
//				line = br.readLine();
//				if (line == null)
//					break;
//				LOG.info(line);
//			}*/
//
//			 /* while(true){ String line = br.readLine(); if (line != null){
//			  sess.close(); sess = conn.openSession();
//			  sess.execCommand("mkdir /home/tws1/testinstall"); } break; }*/
//
//
//	/*	} catch (IOException e) {
//			e.printStackTrace(System.err);
//			System.exit(2);
//		}
//		 Close this session
//		sess.close();
//		 Close the connection
//		conn.close();*/
//	}
//}