package com.tydic.bp.util;

import com.tydic.bp.common.utils.tools.DateUtil;
import lombok.Data;

import java.util.Date;

/**
 * 远程主机文件Dto对象
 * 
 * @author Yuanh
 * 
 */
@Data
public class FileRecord {

	protected static final String DEFAULT_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

	// 目录
	public static final char DIR = 'D';
	// 文件
	public static final char FILE = 'F';
	// 未知类型
	public static final char UNK = 'U';

	// 文件类型
	private char fileType;
	// 文件绝对路径
	private String filePath;
	// 文件名称
	private String fileName;
	// 文件大小
	private long fileLength;
	// 文件创建时间
	private Date time;
	// 源文件备份目录
	private String oriPathBak;

	// 分发任务表SourceId
	private String sourceId;
	// 分发任务表RecId
	private String recId;
	
	//判断文件是否在列表中
	private transient String checkCondition;

	public FileRecord() {
		fileLength = -1;
		time = new Date(0);
	}

	public boolean isDirectory() {
		return fileType == DIR;
	}

	public boolean isFile() {
		return fileType == FILE;
	}

	public String time(String fmt) {
		if (fmt == null)
			fmt = DEFAULT_TIME_FMT;
		return DateUtil.format(time, fmt);
	}

	@Override
	public String toString(){
		return fileName;
	}

}
