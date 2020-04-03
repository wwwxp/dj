package com.tydic.util.ftp;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public interface Trans {

	/**
	 * 判断目录是否存在
	 * 
	 * @param path
	 * @return
	 */
	public boolean isExistPath(String path);

	/**
	 * 获取文件列表
	 */
	public Vector<FileRecord> getFileList(String path) throws Exception;
	
	/**
	 * 获取所有文件列表
	 */ 
	public void getAllFileList(List<FileRecord> fileList, String path, String parentId) throws Exception;
	 

	/**
	 * 远程主机登录
	 * 
	 * @throws Exception
	 */
	public void login() throws Exception;

	/**
	 * 远程主机调用关闭
	 */
	public void close();

	/**
	 * 将远程主机文件下载到本地
	 * 
	 * @param remotePath
	 * @param localPath
	 * @throws Exception
	 */
	public void get(String remotePath, String localPath) throws Exception;

	/**
	 * 将本地文件上传到远程主机
	 * 
	 * @param localPath
	 * @param remotePath
	 * @throws Exception
	 */
	public void put(String localPath, String remotePath) throws Exception;

	/**
	 * 删除远程文件
	 * 
	 * @param path
	 * @return
	 */
	public boolean delete(String path) throws Exception;

	/**
	 * 文件重命令
	 * 
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 */
	public boolean rename(String sourceFile, String targetFile) throws Exception;

	/**
	 * 切换目录
	 * 
	 * @param remotePath
	 * @return
	 */
	public boolean cd(String remotePath) throws Exception;
	
	/**
	 * 创建目录(可以创建多级目录)
	 * 
	 * @param dirName
	 * @return
	 */
	public boolean mkdir(String dirName) throws Exception;
	
	/**
	 * 获取链路采集模式
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean getPasvMode() throws Exception;

	/**
	 * 设置链路采集模式
	 * @param isPasvMode
	 * @return
	 * @throws Exception
	 */
	public void setPasvMode(Boolean isPasvMode) throws Exception;
	
	/**
	 * 获取远程主机文件流对象
	 * @param src
	 * @return
	 * @throws Exception
	 */
	public InputStream get(String src) throws Exception;
	
	/**
	 * 当执行get(String src)方法后，在关闭流之后必须执行该方法
	 * completePendingCommand()会一直在等FTP Server返回226 Transfer complete，
	 * 但是FTP Server只有在接受到InputStream执行close方法时，才会返回。所以先要执行close方法
	 * @throws Exception
	 */
	public void completePendingCommand() throws Exception;
	
	 /** 
	  * Sftp通过文件流上传目标文件
	 * 
	 * @param remotePath
	 * @param localPath
	 * @return
	 */
	public void put(InputStream input,String remotePath) throws Exception;
	
}
