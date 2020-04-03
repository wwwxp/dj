package com.tydic.bp.util;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Vector;

/**
 * 
 * @author Yuanh
 * 
 */
@Slf4j
public class FileTool {

	/**
	 * 显示本地文件列表
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static java.util.Vector<FileRecord> list(String path) throws IOException {
		File dir = new File(path);
		java.util.Vector<FileRecord> rsList = new java.util.Vector<FileRecord>();
		String[] list = dir.list();
		if (list == null) {
			throw new IOException("no exists path : " + path);
		}
		path = FileTool.exactPath(dir.getAbsolutePath());
		for (int i = 0; i < list.length; i++) {
			File f = new File(path + list[i]);
			FileRecord record = new FileRecord();
			if (f.isDirectory())
				record.setFileType(FileRecord.DIR);
			else if (f.isFile())
				record.setFileType(FileRecord.FILE);
			record.setFilePath(path);
			record.setFileName(list[i]);
			record.setTime(new java.util.Date(f.lastModified()));
			record.setFileLength(f.length());
			rsList.add(record);

		}
		return rsList;
	}



	/**
	 * 本地文件删除
	 * @param fileName
	 * @return
	 */
	public static boolean delete(String fileName) {
		File of = new File(fileName);
		return of.delete();
	}

	/**
	 * 文件重命令
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean rename(String from, String to) {
		File of = new File(from);
		File rf = new File(to);
		return of.renameTo(rf);
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean exists(String fileName) {
		File f = new File(fileName);
		return f.exists();
	}

	/**
	 * 判断文件是否绝对路径
	 *
	 * @param fileName
	 * @return
	 */
	public static boolean isAbsolute(String fileName) {
		File f = new File(fileName);
		return f.isAbsolute();
	}

	/**
	 * 为目录添加分割符
	 *
	 * @param path
	 * @return
	 */
	public static String exactPath(String path) {
		if (path == null)
			return null;
		else if (path.endsWith(File.separator))
			return path;
		else if (path.endsWith("/") || path.endsWith("\\"))
			return path;
		else
			//return path + "/";
			return path + File.separator;
	}
	
	/**
	 * 获取文件名称
	 * 
	 * @param filePath 文件路径名称
	 * @return
	 */
	public static String getFileNameByPath(String filePath) {
		if (BlankUtil.isBlank(filePath)) {
			return null;
		} else if (filePath.indexOf(File.separator) != -1) {
			return filePath.substring(filePath.lastIndexOf(File.separator) + File.separator.length());
		} else if (filePath.indexOf("/") != -1) {
			return filePath.substring(filePath.lastIndexOf("/") + 1);
		} else if (filePath.indexOf("\\") != -1) {
			return filePath.substring(filePath.lastIndexOf("\\") + 2);
		} 
		return null;
	}
	
	/**
	 * 本地文件复制
	 * 
	 * @param src   源文件
	 * @param dst   目标文件
	 * @throws IOException
	 */
	public static void copyFile(String src, String dst) throws IOException {
		File srcFile = new File(src);
		File dstFile = new File(dst);
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(dstFile);
		int len;
		byte [] bytes = new byte[1024 * 1024];
		while ((len = fis.read(bytes)) != -1) {
			fos.write(bytes, 0, len);
		}
		fos.flush();
		fis.close();
		fos.close();
	}
	
	/** 
	 * 复制一个目录及其子目录、文件到另外一个目录 
	 * 
	 * @param src 
	 * @param dest 
	 * @throws IOException 
	 */  
	public static void copyFolder(File src, File dest) throws IOException {  
	    if (src.isDirectory()) {  
	        if (!dest.exists()) {  
	            dest.mkdir();  
	        }  
	        String files[] = src.list();  
	        for (String file : files) {  
	            File srcFile = new File(src, file);  
	            File destFile = new File(dest, file);  
	            // 递归复制  
	            copyFolder(srcFile, destFile);  
	        }  
	    } else {  
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	        byte[] buffer = new byte[1024 * 1024];  
	        int length;  
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        out.flush();
	        in.close();  
	        out.close();  
	    }  
	}  
	
	public static void main(String[] args) {
		System.out.println("Start Copy :" + DateUtil.getCurrent());
//		try {
//			FileTool.copyFile("F:/testcoll/101488/20160714/102010_win64_x64_database.zip", "F:/testcoll/101488/bak/20160714/bbabxb.zip");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		File file  = new File("F:/testcoll/101488/20160714/abs");
		if (!file.exists()) {
			if(!file.mkdir()) {
				System.out.println("没有创建目录呢");
			}
		}
		System.out.println("end Copy: " + DateUtil.getCurrent());
	}

	/**
	 * 获取文件列表(含子文件)
	 * @param path
	 * @return
	 */
	public static Vector<FileRecord> getSubList(String path, FileFilter filter){

		Vector<FileRecord> fileList = new Vector<>();
		File curFile = new File(path);
		getSubFileList(curFile,filter,fileList);
		return fileList;
	}

	private static void getSubFileList(File file,FileFilter filter,Vector<FileRecord> fileList){
		File[] list = file.listFiles(filter);
		if(list.length == 0){
			return;
		}
		for(int i=0,length=list.length;i<length;++i){
			if(list[i].isDirectory()){
				getSubFileList(list[i],filter,fileList);
			}else{
				FileRecord record = new FileRecord();
				record.setFileType(FileRecord.FILE);
				record.setFilePath(list[i].getPath());
				record.setFileName(list[i].getName());
				record.setTime(new java.util.Date(list[i].lastModified()));
				record.setFileLength(list[i].length());
				fileList.add(record);
			}
		}
	}
}