package com.tydic.dcm.ftran;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

public interface Trans {

	//字节缓冲区大小
	public final int BUFFER_SIZE = 2 * 1024 * 1024;//2MB

	/**
	 * 判断目录是否存在
	 *
	 * @param path
	 * @return
	 */
	public boolean isExistPath(String path);

	/**
	 * 获取文件列表
	 * @param path
	 * @param fileNamePattern:文件名匹配规则，列举所有设置为null，ftp设置此规则有可能失败
	 * @return
	 * @throws Exception
	 */
	public Vector<FileRecord> getFileList(String path,String fileNamePattern) throws Exception;

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
	 * ftp重连操作
	 *
	 * @Title: reconnect
	 * @Description: ftp重连操作
	 * @return: void
	 * @author: tianjc
	 * @date: 2017年12月3日 下午4:12:16
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public boolean reconnect();

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
	 *
	 * @param isPasvMode
	 * @return
	 * @throws Exception
	 */
	public void setPasvMode(Boolean isPasvMode) throws Exception;

	/**
	 * 获取FTP远程文件字节流
	 *
	 * @param remotePath
	 * @return
	 * @throws Exception
	 */
	public ByteArrayOutputStream getFileStream(String remotePath) throws Exception;

	/**
	 * 上传文件流到ftp
	 *
	 * @param srcInputStream
	 * @param dstPath
	 * @return
	 * @throws Exception
	 */
	public boolean putFileStream(InputStream srcInputStream, String dstPath) throws Exception;

}
