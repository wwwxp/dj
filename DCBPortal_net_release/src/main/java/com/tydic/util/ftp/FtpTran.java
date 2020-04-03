package com.tydic.util.ftp;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.util.Constant;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
 
 

/**
 * Ftp连接类
 * @author Yuanh
 *
 */
public class FtpTran implements Trans {
	//日志对象
	private static Logger logger = Logger.getLogger(FtpTran.class);
	//传输编码方式
	public static final String FTP_ENCODE_UTF8 = "UTF-8";
	public static final String FTP_ENCODE_ISO = "ISO-8859-1";
	//传输缓存大小
	public static final int FTP_BUFFER_SIZE = 1024 * 1024;
	//下载文件、上传文件重复尝试次
	public static final int FTP_GET_PUT_TRY_COUNT = 2;
	//FTP重试登录此时
	public static final int FTP_LOGIN_TRY_COUNT = 0;
	//ftpClient对象
	private FTPClient tranClient = null;
	//主机IP
	private String ip;
	//主机端口
	private int port;
	//登录用户名
	private String userName;
	//登录密码
	private String password;
	//ftp连接超时时间
	private int timeout;
	
	//主被动模式
	private Boolean isPasvMode = true;
	
	/**
	 * Ftp对象
	 * 
	 * @param ip 主机IP
	 * @param port 主机端口
	 * @param userName 登录账户名称
	 * @param password 登录账户密码
	 */
	public FtpTran(String ip, int port, String userName, String password, int timeout) {
		this.ip = StringTool.isIPV4Legal(ip) ? StringUtils.trim(ip) : StringUtils.trim(ip) + StringUtils.trim(Constant.LOCAL_NET_CARD);
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.timeout = timeout;
	}
	
	/**
	 * 判断目录是否存在
	 * @param filePath
	 * @return
	 */
	public boolean isExistPath(String filePath) {
		logger.debug("begin check path exist, path: " + filePath);
		Boolean existFlag = true;
		try {
			//先切换到根目录
			tranClient.changeWorkingDirectory("/");
			//切换到参数对应的目录，用来判断目录是否存在
			existFlag = tranClient.changeWorkingDirectory(filePath);
		} catch (Exception e) {
			existFlag = false;
			logger.error("check path exists, filePath: " + filePath, e);
		}
		logger.debug("end check path exist, result: " + existFlag);
		return existFlag;
	}
	
	/**
	 * 重复获取远程文件列表
	 * @param filePath
	 * @return
	 */
	private Map<String, Object> retry_getFileList(String filePath) {
		Map<String, Object> rstMap = new HashMap<String, Object>();
		Boolean rst = Boolean.TRUE;
		FTPFile [] ftpFiles = null;
		try {
			ftpFiles = tranClient.listFiles(filePath);
			rstMap.put("ftpFiles", ftpFiles);
		} catch (Exception e) {
			rst = Boolean.FALSE;
			logger.error("retry get file list fail, filePath: " + filePath 
					+ ", ftpFiles size: " + (ftpFiles == null ? 0 : ftpFiles.length) , e);
		}
		rstMap.put("rst", rst);
		return rstMap;
	}
	
	/**
	 * 获取文件路径
	 * 
	 * @param filePath
	 * @return
	 * @throws Exception 
	 */
	public Vector<FileRecord> getFileList(String filePath) throws Exception {
		logger.debug("begin get ftp remote file list, path: " + filePath);
		Vector<FileRecord> fileVectors = new Vector<FileRecord>();
		try {
			tranClient.changeWorkingDirectory(filePath);
			
			//如果尝试3次获取远程文件列表失败，切换模式进行再次获取
			int tryCount = 0;
			Map<String, Object> rstMap = retry_getFileList(filePath);
			Boolean rst = Boolean.valueOf(StringTool.object2String(rstMap.get("rst")));
			while (!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
				rstMap = retry_getFileList(filePath);
				rst = Boolean.valueOf(StringTool.object2String(rstMap.get("rst")));
				tryCount++;
				logger.info("get ftp file list fail, retry times: " + tryCount 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
						+ ", filePath: " + filePath);
				Thread.sleep(200);
			}
			
			tryCount = 0;
			logger.info("retry_getFileList method execute result at first, rst: " + rst 
					+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
					+ ", filePath: " + filePath);
			
			//上一个ftp模式获取文件列表失败后将ftpClient对象关闭，将ftp主被动模式切换，重新创建一个FtpClient对象
			//重新获取文件列表，如果还是失败进行重复获取，如果重复获取还是失败则直接抛出异常信息
			if (!rst) {
				//将ftpClient对象重新打开一个
				this.close();
				//切换模式
				this.setPasvMode(this.getPasvMode() == true ? Boolean.FALSE : Boolean.TRUE);
				this.login();
				tranClient.changeWorkingDirectory(filePath);
				rstMap = retry_getFileList(filePath);
				rst = Boolean.valueOf(StringTool.object2String(rstMap.get("rst")));
				while(!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
					rstMap = retry_getFileList(filePath);
					rst = Boolean.valueOf(StringTool.object2String(rstMap.get("rst")));
					tryCount++;
					logger.info("get ftp file list fail after change transfer mode, retry times: " + tryCount 
							+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
							+ ", filePath: " + filePath);
					Thread.sleep(200);
				}
				logger.info("retry_getFileList method execute result at second, rst: " + rst 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
						+ ", filePath: " + filePath);
			}
			
			//获取远程文件列表失败，直接抛出异常信息
			if (!rst) {
				throw new RuntimeException("get ftp file list fail！");
			}
			
			//获取远程文件列表
			FTPFile [] ftpFiles = (FTPFile[]) rstMap.get("ftpFiles");
			logger.info("get ftp remote file list ok, ftpFiles size: " + (ftpFiles == null ? 0 : ftpFiles.length) + ", filePath: " + filePath);
			if (ftpFiles != null && ftpFiles.length > 0) {
				for (int i=0; i<ftpFiles.length; i++) {
					String fileName = ftpFiles[i].getName();
					long fileLength = ftpFiles[i].getSize();
					Date fileTime = ftpFiles[i].getTimestamp().getTime();
					
					//排除父目录
					if (".".equals(fileName) || "..".equals(fileName)) {
						continue;
					}
					
					FileRecord fileRecord = new FileRecord();
					fileRecord.setFileName(fileName);
					
					//只获取普通文件或者目录文件
					if (ftpFiles[i].isFile()) {
						fileRecord.setFileType(FileRecord.FILE);
					} else if (ftpFiles[i].isDirectory()) {
						fileRecord.setFileType(FileRecord.DIR);
					} else {
						logger.debug("file type is error, fileName:" + fileName);
						continue;
					}
					
					fileRecord.setFileLength(fileLength);
					fileRecord.setFilePath(FileTool.exactPath(filePath));
					String targetPath = FileTool.exactPath(fileRecord.getFilePath()) + fileRecord.getFileName();
					fileRecord.setTargetPath(targetPath);
					
					fileRecord.setTime(fileTime);
					fileVectors.add(fileRecord);
				}
			}
		} catch (Exception e) {
			logger.error("get ftp remote file list fail！", e);
			throw e;
		}
		logger.debug("end get ftp remote file list, file list size:" + (fileVectors == null ? 0 : fileVectors.size()));
		return fileVectors;
	}

	/**
	 * Ftp登录
	 */
	public void login() throws Exception {
		if (tranClient != null && tranClient.isConnected()) {
			return;
		}
		try {
			tranClient = new FTPClient();
			//将ftp发送命名和接收命名输出到log4j
			//tranClient.addProtocolCommandListener(new ProtocolCmdListenerImpl(devId));
			//连接Ftp
			tranClient.connect(ip, port);
			//ftp登录
			Boolean isLoginSuccess = tranClient.login(userName, password);
			logger.debug("ftp login, IP:" + this.ip + ", PORT: " + this.port 
					+ ", USERNAME: " + this.userName + ", PASSWORD: " + this.password 
					+ ", Login Result: " + isLoginSuccess);
			
			int tryLoginCount = 0;
			while (!isLoginSuccess && tryLoginCount < FTP_LOGIN_TRY_COUNT) {
				this.close();
				tranClient = new FTPClient();
				//将ftp发送命名和接收命名输出到log4j
				//tranClient.addProtocolCommandListener(new ProtocolCmdListenerImpl(devId));
				//连接Ftp
				tranClient.connect(ip, port);
				
				isLoginSuccess = tranClient.login(userName, password);
				tryLoginCount++;
				Thread.sleep(200);
				logger.debug("ftp retry login, IP:" + this.ip + ", PORT: " + this.port 
						+ ", USERNAME: " + this.userName + ", PASSWORD: " + this.password 
						+ ", Retry Login Result: " + isLoginSuccess);
			}
			//检查是否连接上Ftp
			int replyCode = tranClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				this.close();
				logger.error("FTP server refused connection. IP:" + this.ip 
						+ ", PORT:" + this.port + ", USERNAME: " + this.userName 
						+ ", PASSWORD: " + this.password + ", ReplyCode: " + replyCode);
				throw new RuntimeException("ftp server connection fail.");
			}
			
			//设置ftp传输模式
			//Java中通常内网用被动模式,外网连接用主动模式
			if (this.isPasvMode) {
				tranClient.enterLocalPassiveMode();
			} else {
				tranClient.enterLocalActiveMode();
			}
			//设置读取文件缓存
			tranClient.setBufferSize(FTP_BUFFER_SIZE);
			//FTP编码方式
			tranClient.setControlEncoding(FTP_ENCODE_UTF8);
			//Ftp文件传输方式
			tranClient.setFileType(FTP.BINARY_FILE_TYPE);
			//设置Ftp连接读取数据超时时间
			tranClient.setDataTimeout(1000*60*2);
			logger.debug("ftp server connection success, IP:" + this.ip + "\tport:" + this.port);
		} catch (Exception e) {
			logger.error("ftp server connection fail, IP:" + this.ip + "\tport:" + this.port,e);
			this.close();
			throw e;
		}
	}
	
	/**
	 * 重试下载文件
	 * @param remotePath
	 * @param localPath
	 * @return
	 */
	private Boolean retry_get(String remotePath, String localPath) {
		Boolean rst = Boolean.TRUE;
		FileOutputStream fos = null;
		try {
			//创建本地文件
			File localFile = new File(localPath);
			if(!localFile.exists()){
				localFile.getParentFile().mkdirs();
				localFile.createNewFile();
			}
			
			fos = new FileOutputStream(localFile);
			boolean isTransferOK = tranClient.retrieveFile(remotePath, fos);
			logger.debug("retry get file, fileName:" + remotePath + ", result:" + isTransferOK);
			if (!isTransferOK) {
				throw new RuntimeException("ftp file transfer fail.");
			}
			fos.flush();
		} catch (Exception e) {
			logger.error("retry get file fail, fileName: " + remotePath, e);
			rst = Boolean.FALSE;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("fos close fail.", e);
				}
			}
		}
		return rst;
	}
	
	/**
	 * 下载远程文件到本地目录
	 * 
	 * @param remotePath 远程目录+文件名称
	 * @param localPath  本地目录+文件名称
	 */
	public void get(String remotePath, String localPath) throws Exception {
		logger.debug("begin get ftp remote files, remotePath: " + remotePath + ", localPath: " + localPath);
		try {
			//循环遍历3次下载文件，如果3次下载文件失败切换模式进行再次下载
			int tryCount = 0;
			Boolean rst = retry_get(remotePath, localPath);
			while (!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
				rst = retry_get(remotePath, localPath);
				tryCount++;
				logger.info("download file fail, retry times: " + tryCount 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
						+ ", fileName: " + remotePath);
				Thread.sleep(200);
			}
			tryCount = 0;
			logger.info("retry_get method execute result at first, rst: " + rst 
					+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
					+ ", fileName: " + remotePath);
			
			//判断本地文件是否存在
			if (!rst) {
				//将ftpClient对象重新打开一个
				this.close();
				//切换模式
				this.setPasvMode(this.getPasvMode() == true ? Boolean.FALSE : Boolean.TRUE);
				this.login();
				rst = retry_get(remotePath, localPath);
				while(!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
					rst = retry_get(remotePath, localPath);
					tryCount++;
					logger.info("download file fail after change transfer mode, retry times: " + tryCount 
							+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
							+ ", fileName: " + remotePath);
					Thread.sleep(200);
				}
				logger.info("retry_get method execute result at second, rst: " + rst 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
						+ ", fileName: " + remotePath);
			}
			
			if (!rst) {
				throw new RuntimeException("ftp file transfer fail.");
			}
		} catch (Exception e) {
			logger.error("ftp file transfer fail.", e);
			throw e;
		} 
		logger.debug("end get ftp remote file to local ok.");
	}
	
	/**
	 * 上传本地文件到远程主机
	 * 
	 * @param localPath  本地文件(包含文件绝对路径+文件名)
	 * @param remotePath 远程文件(包含文件绝对路径+文件名)
	 * @return
	 */
	@Override
	public void put(String localPath, String remotePath) throws Exception {
		logger.debug("ftp begin put file to remote path, localPath: " + localPath + ", remotePath: " + remotePath);
		try {
			int tryCount = 0;
			Boolean rst = retry_put(localPath, remotePath);
			while (!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
				rst = retry_put(localPath, remotePath);
				tryCount++;
				logger.info("upload file fail, retry times: " + tryCount 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
						+ ", fileName: " + localPath);
				Thread.sleep(200);
			}
			tryCount = 0;
			logger.info("retry_put method execute result at first, rst: " + rst 
					+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
					+ ", fileName: " + localPath);
			
			//判断本地文件是否存在
			if (!rst) {
				//将ftpClient对象重新打开一个
				this.close();
				//切换模式
				this.setPasvMode(this.getPasvMode()==true ? Boolean.FALSE : Boolean.TRUE);
				this.login();
				rst = retry_put(localPath, remotePath);
				while(!rst && tryCount < FTP_GET_PUT_TRY_COUNT) {
					rst = retry_put(localPath, remotePath);
					tryCount++;
					logger.info("upload file fail after change transfer mode, retry times: " + tryCount 
							+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT")
							+ ", fileName: " + localPath);
					Thread.sleep(200);
				}
				logger.info("retry_put method execute result at second, rst: " + rst 
						+ ", transfer mode: " + (this.getPasvMode() ? "PASV" : "PORT") 
						+ ", fileName: " + localPath);
			}
			
			if (!rst) {
				throw new RuntimeException("ftp file upload fail.");
			}
		} catch (Exception e) {
			logger.error("ftp put file to remote path fail.", e);
			throw e;
		}
		logger.debug("ftp end put file to remote path.");
	}
	
	/**
	 * 重试上传文件
	 * @param localPath
	 * @param remotePath
	 * @return
	 */
	private Boolean retry_put(String localPath, String remotePath) {
		FileInputStream fis = null;
		Boolean rst = Boolean.TRUE;
		try {
			//判断远程目录是否存在，如果不存在，创建远程目录
			String path = remotePath.substring(0,remotePath.lastIndexOf("/")+1);
			//创建远程目录
			this.mkdir(path);
			//本地文件
			File file = new File(localPath);
			//将本地文件转化为文件输入流
			fis = new FileInputStream(file);
			//向远程主机Ftp写入
			Boolean isUploadOk = tranClient.storeFile(new String(remotePath.getBytes(FTP_ENCODE_UTF8), FTP_ENCODE_ISO), fis);
			logger.debug("retry upload file, fileName: " + localPath + ", result:" + isUploadOk);
			if (!isUploadOk) {
				throw new RuntimeException("upload file fail.");
			}
		} catch (Exception e) {
			rst = Boolean.FALSE;
			logger.error("retry upload file fail, fileName: " + localPath, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("fis close fail.", e);
				}
			}
		}
		return rst;
	}
	
	/**
	 * 删除文件
	 * @param filePath
	 * @return
	 */
	public boolean delete(String filePath) {
		logger.debug("begin ftp delete remote file, file path:" + filePath);
		boolean result=false;
		try {
			result = tranClient.deleteFile(filePath);
			if(!result){
				result = tranClient.removeDirectory(filePath);
			}
		} catch (IOException e) {
			logger.error("delete remote fail fail.", e);
		}
		logger.debug("end ftp delete remote file, result:"+result);
		return result;
	}
	
	/**
	 * 文件重命令
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 */
	public boolean rename(String sourceFile, String targetFile) {
		logger.debug("begin rename file, source file:" + sourceFile + ", target file:" + targetFile);
		boolean result = true;
		try {
			if (!BlankUtil.isBlank(sourceFile) && !BlankUtil.isBlank(targetFile)) {
				if (sourceFile.equals(targetFile)) {
					result = true;
				} else {
					result = tranClient.rename(sourceFile, targetFile);
				}
			}
		} catch (IOException e) {
			logger.error("file rename fail.", e);
			result = false;
		}
		logger.debug("end rename file, result:" + result);
		return result;

	}
	
	/**
	 * 切换目录
	 * 
	 * @param remotePath
	 * @return
	 */
	public boolean cd(String remotePath) {
		logger.debug("ftp begin change file dir, remotePath:" + remotePath);
		boolean result = true;
		try {
			result = tranClient.changeWorkingDirectory(remotePath);
		} catch (IOException e) {
			result = false;
			logger.error("change file dir fail." , e);
		}
		logger.debug("ftp end change file dir, result:" + result);
		return result;
	}
	
	/**
	 * 创建目录(可以创建多级目录)
	 * @param dirName
	 */
	public boolean mkdir(String dirName) {
		logger.debug("ftp begin mkdir, dir:" + dirName);
		boolean rst = true;
		try {
			Vector<String> filePaths = StringTool.tokenStringChar(dirName, File.separator+"//\\");
			if (filePaths != null && filePaths.size() > 0) {
				String tempPath = "/";
				for (int i=0; i<filePaths.size(); i++) {
					tempPath += filePaths.get(i);
					tempPath = FileTool.exactPath(tempPath);
					if (!isExistPath(tempPath)) {
						tranClient.makeDirectory(tempPath);
					}
				}
			}
			String finalDirName = FileTool.exactPath(dirName);
			rst = isExistPath(finalDirName);
		} catch (IOException e) {
			rst = false;
			logger.error("mkdir fail.", e);
		}
		logger.debug("ftp end mkdir, result:" + rst);
		return rst;
	}
	
	/**
	 * 关闭Ftp连接
	 */
	public void close() {
		try {
			if (tranClient != null && tranClient.isConnected()) {
				tranClient.disconnect();
			}
		} catch (IOException e) {
			logger.error("close ftp fail, this server:" + this.ip + "\t this port:" + this.port,e);
		}
		logger.debug("close ftp success, this server:" + this.ip + "\t this port:" + this.port);
	}
	
	/**
	 * 获取当前ftp传输模式
	 * 
	 * @return 当前传输模式
	 */
	@Override
	public boolean getPasvMode() throws Exception {
		logger.debug("get ftp tran mode, isPasvMode: " + this.isPasvMode);
		return this.isPasvMode;
	}

	/**
	 * 设置当前ftp传输模式
	 * 
	 * @param isPasvMode 传输模式
	 */
	@Override
	public void setPasvMode(Boolean isPasvMode) throws Exception {
		logger.debug("set ftp tran mode, isPasvMode: " + isPasvMode);
		this.isPasvMode = isPasvMode;
	}
	
//	@Override
//	public String toString() {
//		return "FtpTran [ip=" + ip + ", port=" + port + ", userName="
//				+ userName + ", password=" + password + "]";
//	}

	/**
	 * FTP下载远程主机文件
	 * @param src 远程主机文件
	 * @return 远程文件流对象
	 */
	@Override
	public InputStream get(String src) throws Exception {
		logger.debug("begin get ftp remote files, remotePath: " + src );
		try {
			InputStream in = tranClient.retrieveFileStream(src);
			if (in == null) {
				//关闭当前连接对象
				this.close();
				//重新创建Ftp连接对象
				this.login();
				//重新获取文件流对象
				in = this.tranClient.retrieveFileStream(src);
			}
			
			if (in == null) {
				throw new RuntimeException("获取ftp文件流失败，请检查文件是否存在!");
			}
			
			//主动调用该方法，能解决再次读文件null的情况
			//tranClient.getReply();
			return in;
		} catch (Exception e) {
			logger.error("ftp file transfer fail.", e);
			throw e;
		}  
	}
	
	/**
	 * 当执行get(String src)方法后，在关闭流之后必须执行该方法
	 * completePendingCommand()会一直在等FTP Server返回226 Transfer complete，
	 * 但是FTP Server只有在接受到InputStream执行close方法时，才会返回。所以先要执行close方法
	 * @throws Exception
	 */
	@Override
	public void completePendingCommand() throws Exception {
		this.tranClient.completePendingCommand();
	}
	
	/**
	 * 将本地文件推送到远程主机
	 *  enterLocalPassiveMode 被动模式
	 *  enterLocalActiveMode 主动模式
	 */
	@Override
	public void put(InputStream input, String remotePath) throws Exception {
		logger.debug("begin get ftp remote files, remotePath: " + remotePath );
		try {
			 String path = remotePath.substring(0,remotePath.lastIndexOf("/")+1);
			 this.mkdir(path);
			 
			 //切换到被动模式，测试上传是否成功，如果上传不成功，切换到主动模式
			 tranClient.enterLocalPassiveMode();
			 Boolean isSuccOK = tranClient.storeFile(new String(remotePath.getBytes(FTP_ENCODE_UTF8), FTP_ENCODE_ISO), input);
			 logger.debug("当前FTP模式为被动模式，上传结果： " + (isSuccOK ? "成功" : "失败"));
			 
			 if (!isSuccOK) {
				 tranClient.enterLocalActiveMode();
				 isSuccOK = tranClient.storeFile(new String(remotePath.getBytes(FTP_ENCODE_UTF8), FTP_ENCODE_ISO), input);
				 logger.debug("当前FTP模式为主动模式， 上传文件结果： " + (isSuccOK ? "成功" : "失败"));
				 if (!isSuccOK) {
					 throw new IOException("Can't upload file '" + remotePath + "' to FTP server. Check FTP permissions and path.");
				 }
			 }
		} catch (Exception e) {
			logger.error("ftp file transfer fail.", e);
			throw e;
		} finally {
			if(input !=null){
				try{
					input.close();
				}catch(Exception e){
					
				}
			}
		}
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
					String filePath = firstList.get(i).getFilePath() + "/" + fileName;
					getAllFileList(fileList, filePath, firstList.get(i).getCurrId());
				}
			}
		} catch (Exception e) {
			logger.error("获取文件失败 --->", e);
			throw e;
		}
		
	}

	public static void main(String[] args) throws Exception {

		FtpTran tt1 = new FtpTran("192.168.161.26", 21, "bp_dcf", "dic123", 60000);
		tt1.login();
		System.out.println(tt1  + " tt1--->"+ tt1.tranClient.toString());
		
		FtpTran tt = new FtpTran("192.168.161.25", 21, "bp_dcf", "dic123", 60000);
		tt.login();
		System.out.println(tt  + " tt--->"+ tt.tranClient.toString());

		// tt1.get("/public/bp/YH/error.log",
		// "D:\\Personal\\Desktop\\error错误日志333.log");

		System.out.println("startDate --->" + DateUtil.getCurrent());
		InputStream stream = tt1.get("/public/bp/test/env/6.0.0/dca.zip");
		tt.put(stream, "/public/bp/YH/dca.zip");
		stream.close();
		tt1.tranClient.completePendingCommand();

		InputStream stream2 = tt1.get("/public/bp/test/env/6.0.0/dmdb.zip");

		System.out.println("endDate --->" + DateUtil.getCurrent());
		System.out.println(tt1  + " tt1111--->"+ tt1.tranClient.toString());
		try {

			// File file = new File("D:\\Personal\\Desktop\\error错误日志.log");
			// FileInputStream input = new FileInputStream(file);
			// tt.put(input, "/public/bp/YH/error错误日志.log");

			// tt.put("D:\\Personal\\Desktop\\error错误日志333.log",
			// "/public/bp/YH/error44.log");

			System.out.println("测试主句。。。。");
			System.out.println("Put startDate --->" + DateUtil.getCurrent());

			tt.put(stream2, "/public/bp/YH/dmdb.zip");
			stream2.close();
			tt1.tranClient.completePendingCommand();
			System.out.println("Put endDate --->" + DateUtil.getCurrent());
			System.out.println("推送成功...");
			
			System.out.println(tt  + " tttttt--->"+ tt.tranClient.toString());
			
			boolean isExists = tt1.isExistPath("/public/bp/test/conf/platform_config/release/dca/FTP/aa/aa.txt");
			boolean isExists2 = tt1.isExistPath("/public/bp/test/conf/platform_config/release/dca/FTP");
			boolean isExists3 = tt1.isExistPath("/public/bp/test/conf/platform_config/release/dca/FTP");
			System.out.println("isExists ----> " + isExists + " --->" + isExists2 + " --->" + isExists3);
			
			boolean isTTExists = tt.isExistPath("/public/bp/test/conf/platform_config/release/dca/FTP");
			System.out.println("isTTExists ----> " + isTTExists);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tt1.close();
			tt.close();
		}
	}

}
