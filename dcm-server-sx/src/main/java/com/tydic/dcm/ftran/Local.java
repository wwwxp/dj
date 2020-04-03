package com.tydic.dcm.ftran;

import com.tydic.dcm.util.tools.FileTool;
import org.apache.commons.lang3.ObjectUtils;

/**
 * 本地文件操作类
 * 
 * @author Yuanh
 * 
 */
public class Local {
	protected String curPath;

	public Local() {
		
	}
	
	/**
	 * 本地切换目录
	 * @param path
	 * @throws java.io.IOException
	 */
	public void cd(String path) throws java.io.IOException {
		path = FileTool.exactPath(path);
		if (FileTool.isAbsolute(path)) {
			if (!FileTool.exists(path)) {
				throw new java.io.IOException("no exist this path:" + path);
			}
			curPath = path;
		} else {
			if (!FileTool.exists(curPath + path)) {
				throw new java.io.IOException("no exist this path:" + path);
			}
			curPath = curPath + path;
		}
	}

	/**
	 * 本地删除文件
	 * @param fileName
	 * @throws java.io.IOException
	 */
	public void delete(String fileName) throws java.io.IOException {
		if (!FileTool.isAbsolute(fileName)) {
			fileName = ObjectUtils.toString(curPath, "") + fileName;
		}
		if (!FileTool.delete(fileName)) {
			throw new java.io.IOException("delete fail:" + fileName);
		}
	}

	/**
	 * 本地文件重命令
	 * @param from
	 * @param to
	 * @throws java.io.IOException
	 */
	public void rename(String from, String to) throws java.io.IOException {
		if (!FileTool.isAbsolute(from)) {
			from = curPath + from;
		}
		if (!FileTool.isAbsolute(to)) {
			to = curPath + to;
		}
		//如果重命名后的文件和重命名之前文件一样则不进行重命名直接返回
		if (from.equals(to)) {
			return;
		}
		
		java.io.File ffrom = new java.io.File(from);
		java.io.File fto = new java.io.File(to);
		if (fto.exists()) {
			fto.delete();
		}
		if (!ffrom.renameTo(fto)) {
			throw new java.io.IOException("rename fail:" + from + " " + to);
		}
	}

	/**
	 * 本地文件列表
	 * @return
	 * @throws java.io.IOException
	 */
	public java.util.Vector<FileRecord> list() throws java.io.IOException {
		return FileTool.list(curPath);
	}

	/**
	 * 本地文件指定重命令
	 * @param from
	 * @param to
	 * @throws java.io.IOException
	 */
	public void renameTo(String from, String to) throws java.io.IOException {
		if (!FileTool.isAbsolute(from)) {
			from = curPath + from;
		}
		if (!FileTool.isAbsolute(to)) {
			to = curPath + to;
		}
		FileTool.copyFile(from, to);
	}
}
