package com.tydic.util.ftp;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.util.Constant;
import com.tydic.util.FileUtil;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
 

/**
 * Sftp连接实现类
 * @author Yuanh
 *
 */
public class SftpTran implements Trans {
	
	private static Logger logger = Logger.getLogger(SftpTran.class);
	public Session session = null;
	public ChannelSftp channel = null;
	
	/**
	 * Sftp主机
	 */
	private String ip;
	
	/**
	 * Sftp端口
	 */
	private int port;
	
	/**
	 * Sftp登录用户
	 */
	private String userName;
	
	/**
	 * Sftp登录密码
	 */
	private String password;
	
	/**
	 * Sftp连接超时时间
	 */
	private int timeout;
	
	
	/**
	 * 
	 * @param ip Sftp主机
	 * @param port Sftp端口
	 * @param userName Sftp登录账号
	 * @param password Sftp登录密码
	 * @param timeout Sftp连接超时时间
	 */
	public SftpTran(String ip, int port, String userName, String password, int timeout) {
		this.ip = StringTool.isIPV4Legal(ip) ? StringUtils.trim(ip) : StringUtils.trim(ip) + StringUtils.trim(Constant.LOCAL_NET_CARD);
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.timeout = timeout;
	}

	/**
	 * 判断目录是否存在
	 * 
	 * @param filePath
	 * @return
	 */
	@Override
	public boolean isExistPath(String filePath) {
		boolean result = true;
		try {
			//channel.ls(filePath);
			channel.stat(filePath);
		} catch (SftpException e) {
			result = false;
		}
		logger.debug("check path exist nor not, path:" + filePath + ", result:" + result);
		return result;
	}

	/**
	 * 获取目录下文件列表
	 * 
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Vector<FileRecord> getFileList(String path) throws Exception  {
		logger.info("begin get sftp remote file list, path:" + path);
		
		Vector<FileRecord> rsList = new Vector<FileRecord>();
		try {
			Vector<LsEntry> fileList = channel.ls(path);
			logger.info("sftp list remote file ok, remote list size:" + (fileList == null ? 0 : fileList.size()));
			
			if (fileList != null && fileList.size() > 0) {
				int len = fileList.size();
				for (int i=0; i<len; i++) {
					LsEntry lsEntry = fileList.get(i);
					//获取Sftp文件列表
					String fileName = lsEntry.getFilename();
					if (".".equals(fileName) || "..".equals(fileName)) {
						continue;
					}
					Date mtime = new Date();
					try{
						String mtimeStr = lsEntry.getAttrs().getMtimeString();
						SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US);
						 mtime = dateFormat.parse(mtimeStr);
					}catch(Exception ex){
						logger.error(ex);
					}
					long fileLength = lsEntry.getAttrs().getSize();
					String name=lsEntry.getLongname();
					Vector<String> fileData = StringTool.tokenStringChar(lsEntry.getLongname());
					String fileType = fileData.get(1);
					
					//创建FileRecord对象
					FileRecord fileRecord = new FileRecord();
					fileRecord.setFileName(fileName);
					fileRecord.setFileLength(fileLength);
					fileRecord.setTime(DateUtil.formatToDate(mtime, DateUtil.allPattern));
					fileRecord.setFilePath(path);
					String targetPath = FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName();
					fileRecord.setTargetPath(targetPath);
					if (ParamsConstant.SFTP_FILE_TYPE.equals(fileType)) {//获取到值为1，文件
						fileRecord.setFileType(FileRecord.FILE);
					}else{//非1，文件夹
						fileRecord.setFileType(FileRecord.DIR);
					}
					rsList.add(fileRecord);
				}
			}
		} catch (Exception e) {
			logger.error("sftp get files fail.", e);
			throw e;
		}
		logger.info("end get sftp remote file list, rsList size: " + (rsList == null ? 0 : rsList.size()));
		return rsList;
	}
	
	/**
	 * 关闭Sftp连接
	 * 
	 * @return
	 */
	@Override
	public void close() {	
		 
		
		if (channel != null) {
			if (channel.isConnected()) {
				channel.disconnect();
				logger.info("sftp closed success...");
			} else if (channel.isClosed()) {
				logger.info("sftp is closed already...");
			}
			try {
				if (session != null) {
					session.disconnect();
					logger.info("sftp session closed success...");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
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
		logger.debug("begin get sftp remote files, remotePath:" + remotePath + "\t localPath:" + localPath);
		FileOutputStream fos = null;
		try {
			File file = new File(localPath);
			if(!file.exists()){
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			channel.get(remotePath, fos);
			fos.flush();
		} catch (Exception e) {
			logger.error("sftp get files fail.", e);
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		logger.debug("end get sftp remote files");
	}
	
	/**
	 * Sftp下载文件得到文件流
	 * 
	 * @param src
	 * @return
	 */
	public InputStream get(String src) throws Exception {
		try {
			InputStream  input = channel.get(src);
			return input;
		} catch (Exception e) {
			logger.error("sftp get files fail.", e);
			throw e;
		} finally {
		}
	}
	/**
	 * Sftp通过文件流上传目标文件
	 * 
	 * @param remotePath
	 * @param remotePath
	 * @return
	 */
	public void put(InputStream input,String remotePath) throws Exception {
		try {
			
			/**
			 * 传输模式
			 * OVERWRITE:完全覆盖模式,这是JSch的默认文件传输模式，即如果目标文件已经存在，传输的文件将完全覆盖目标文件，产生新的文件
			 * RESUME:恢复模式,如果文件已经传输一部分，这时由于网络或其他任何原因导致文件传输中断，如果下一次传输相同的文件，则会从上一次中断的地方续传
			 * APPEND:追加模式,如果目标文件已存在，传输的文件将在目标文件后追加
			 */
			 String path = remotePath.substring(0,remotePath.lastIndexOf("/")+1);
			 mkdir(path);
			channel.put(input,remotePath, ChannelSftp.OVERWRITE);
		}
		catch (Exception e) {
			logger.error("sftp put file to remote fail.", e);
			throw e;
		}
		logger.debug("sftp end put file to remote path");
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
		logger.debug("sftp begin put file to remote path, localPath: " + localPath + "\tremotePath: " + remotePath);
		try {
			/**
			 * 传输模式
			 * OVERWRITE:完全覆盖模式,这是JSch的默认文件传输模式，即如果目标文件已经存在，传输的文件将完全覆盖目标文件，产生新的文件
			 * RESUME:恢复模式,如果文件已经传输一部分，这时由于网络或其他任何原因导致文件传输中断，如果下一次传输相同的文件，则会从上一次中断的地方续传
			 * APPEND:追加模式,如果目标文件已存在，传输的文件将在目标文件后追加
			 */
			 
			 String path = remotePath.substring(0,remotePath.lastIndexOf("/")+1);
			 mkdir(path);
			channel.put(localPath, remotePath, ChannelSftp.OVERWRITE);
		} 
		catch (Exception e) {
			logger.error("sftp put file to remote fail.", e);
			throw e;
		}
		logger.debug("sftp end put file to remote path");
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
		logger.debug("delete file, path:" + path);
		boolean result = true;
		try {
			channel.rm(path);
		} catch (Exception e) {
			result = false;
			//logger.error("delete file fail.",e);
			try {
				result = true;
				channel.rmdir(path);
			} catch (Exception e1) {
				result = false;
				logger.error("delete file dir fail.", e);
				throw e;
			}
		}
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
		logger.debug("begin sftp rename, sourceFile: " + sourceFile + "\t targetFile: " + targetFile);
		boolean result = true;
		try {
			channel.rename(sourceFile, targetFile);
		} catch (Exception e) {
			result = false;
			logger.error("file rename fail.",e);
			throw e;
		}
		logger.info("end sftp rename ok!");
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
		logger.debug("sftp begin change file dir, remotePath:" + remotePath);
		boolean result = true;
		try {
			channel.cd(remotePath);
		} catch (Exception e) {
			result = false;
			logger.error("sftp change file dir fail.", e);
			throw e;
		}
		logger.debug("sftp end change file dir, result:" + result);
		return result;
	}
	
	/**
	 * 创建目录(可以创建多级目录)
	 * @param dirName
	 * @return
	 * @throws Exception 
	 */
	@Override
	public boolean mkdir(String dirName) throws Exception {
		logger.debug("sftp begin make dir, dir:" + dirName);
		boolean result = true;
		try {
			Vector<String> filePaths = StringTool.tokenStringChar(dirName, File.separator+"//\\");
			if (filePaths != null && filePaths.size() > 0) {
				String tempPath = "/";
				for (int i=0; i<filePaths.size(); i++) {
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
			logger.error("sftp make dir fail.",e);
			throw e;
		}
		logger.debug("sftp end make dir, result:" + result);
		return result;
	}

	
	/**
	 * Sftp登录
	 * 
	 * @return
	 */
	@Override
	public void login() throws Exception {
		logger.debug("begin to login sftp");
		JSch jSch = new JSch();
	    session = jSch.getSession(userName, ip, port);
		logger.debug("sftp session create OK, ip:" + ip + ", userName: " + userName + ", port: " + port);
		
		session.setPassword(password);
		
		//设置Sftp连接参数信息
		Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config); // 为Session对象设置properties
        if (!BlankUtil.isBlank(timeout)) {
        	session.setTimeout(timeout); // 设置timeout时间
        }
        session.connect();
        logger.debug("sftp session connected.");
        
        Channel sftpChannel = session.openChannel("sftp");
        sftpChannel.connect();
        channel = (ChannelSftp)sftpChannel;
        logger.debug("sftp channel connected.");
        logger.debug("sftp connected successfully, info:" + this.toString());
	}

	@Override
	public boolean getPasvMode() throws Exception {
		return false;
	}

	@Override
	public void setPasvMode(Boolean isPasvMode) throws Exception {
		logger.debug("sftp set tran mode, isPasvMode: " + isPasvMode);
	}

	@Override
	public void completePendingCommand() throws Exception {
		
	}
	
	@Override
	public String toString() {
		return "SftpTran [session=" + session + ", channel=" + channel
				+ ", ip=" + ip + ", port=" + port + ", userName=" + userName
				+ ", password=" + password + ", timeout=" + timeout + "]";
	}

	
	public static void main(String[] args) throws Exception {
		SftpTran tran = new SftpTran("192.168.161.89", 22, "bp", "bp", 6000);
		tran.login();
		//tran.get("/public/bp/dccp/platform_config/release/jstorm/storm.yaml","d:/dccp/tmp/bb/aa/storm.yaml");
		//tran.get("/public/bp/dccp/platform_config/release/zookeeper/zoo.cfg","d:/dccp/tmp/bb/aa/zoo.cfg");
		boolean a = tran.isExistPath("/public/bp/dccp/dataDir/myid1");
		System.out.println(a);
		//tran.put("d:/bp/4444.txt", "/public/bp/dccp/111.txt");
		//tran.put("d:/dccp/tmp/bb/aa/storm.yaml","/public/bp/dccp/platform_config/template/storm.yaml");

		tran.close();
		 
	}

	@Override
	public void getAllFileList(List<FileRecord> fileList, String path, String parentId) throws Exception{
		try {
			List<FileRecord> firstList = this.getFileList(path);
			for (int i=0; i<firstList.size(); i++) {
				//设置当前文件ID
				firstList.get(i).setCurrId(UUID.randomUUID().toString());
				//设置当前节点父节点
				firstList.get(i).setParentId(parentId);
				//将当前节点加入到列表
				fileList.add(firstList.get(i));
				//判断当前文件是否为目录，如果为目录则判断是否包含子文件，递归遍历
				if (String.valueOf(FileRecord.DIR).equals(String.valueOf(firstList.get(i).getFileType()))) {
					String fileName = firstList.get(i).getFileName();
					String filePath = FileUtil.exactPath(firstList.get(i).getFilePath()) + fileName;
					getAllFileList(fileList, filePath, firstList.get(i).getCurrId());
				}
			}
		} catch (Exception e) {
			logger.error("获取文件失败 --->", e);
			throw e;
		}
		
	}
}
