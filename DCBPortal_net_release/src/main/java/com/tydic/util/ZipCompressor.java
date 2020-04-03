package com.tydic.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
  
public class ZipCompressor {   
	private static final Log log = LogFactory.getLog(ZipCompressor.class);
	private static final String ENCODE = "GBK";
   

    public static void zip(String srcPath,String destPath) { 
    	
        File srcdir = new File(srcPath);   
        File destFile=new File(destPath);
        if (!srcdir.exists())   
            throw new RuntimeException(srcPath + "不存在！");   
        log.debug("开始压缩【"+srcdir.getAbsolutePath()+"】。。。");
        Project prj = new Project();   
        Zip zip = new Zip();   
        zip.setProject(prj);   
        zip.setDestFile(destFile);   
        FileSet fileSet = new FileSet();   
        fileSet.setProject(prj);   
        fileSet.setDir(srcdir);   
//        fileSet.setIncludes("**/*.js");// 包括哪些文件或文件夹 eg:zip.setIncludes("*.java");   
//        fileSet.setExcludes(...); //排除哪些文件或文件夹   
        zip.addFileset(fileSet);   
        zip.execute();   
        log.debug("成功压缩至【"+destFile.getAbsolutePath()+"】");
    }   
    /****
     * 解压
     * 
     * @param zipPath
     *            zip文件路径
     * @param destinationPath
     *            解压的目的地点
     * @param ecode
     *            文件名的编码字符集
     */
    public static void unZip(String zipPath, String destinationPath) {
    	
     File zipFile = new File(zipPath);
     File destinationFile = new File(destinationPath);
     if (!zipFile.exists())
      throw new RuntimeException("zip file " + zipPath
        + " does not exist.");
     log.debug("开始解压【"+zipFile.getAbsolutePath()+"】。。。");
     Project proj = new Project();
     Expand expand = new Expand();
     expand.setProject(proj);
     expand.setTaskType("unzip");
     expand.setTaskName("unzip");
     expand.setSrc(zipFile);
     expand.setOverwrite(true);
     expand.setDest(destinationFile);
     expand.setEncoding(ENCODE);
     expand.execute();
     log.info("成功解压至【"+destinationFile.getAbsolutePath()+"】");
    }
    
	public static void main(String[] args){
		ZipCompressor.zip("F:\\dicwork\\日报","我的压缩包by ant.zip");
		ZipCompressor.unZip("我的压缩包by ant.zip","unzip");
    }
}  