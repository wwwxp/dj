package com.tydic.service.develop.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import PluSoft.Utils.JSON;

import com.tydic.bean.file.FileTree;
import com.tydic.bean.file.FileTreeFilter;
import com.tydic.bean.file.FileTreeNode;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.service.develop.DevelopHandleService;
import com.tydic.util.FileUtil;
import com.tydic.util.SessionUtil;
import com.tydic.util.ZipCompressor;

@Service("developHandleService")
public class DevelopHandleServiceImpl implements DevelopHandleService {
	
/**
 * 查询文件目录树信息
 */
@Override
public List listFilesTree(Map<String, String> params)
		throws Exception {
	String rootPath = SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"develop";
//	String rootPath=SystemProperty.getContextProperty("develop.file.root");
	String fileType=SystemProperty.getContextProperty("develop.file.type");
	//开发测试用
//  String rootPath=params.get("developTestPath")+"js";
	FileTreeFilter filter=new FileTreeFilter();
	filter.setFileType(fileType.split(","));
	FileTree tree=new FileTree(rootPath,filter);
	List<FileTreeNode> treeNodes=tree.getFilesTress();
	return treeNodes;
}
	

/**
 * 新建文件或目录
 */
@Override
public Map createDirectoryOrFile(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	System.out.println("oldfilepath:"+JSON.Encode(params));
	Object directory=params.get("directory");
	String type=params.get("type");
	String oldfilepath=params.get("path");
	
	String newFile=params.get("fileName");
	String parentPath=params.get("parentPath");
	boolean isSuccess=false;
	File file=new File(oldfilepath+File.separator+newFile);
	if(type.equals("directory") ){
		isSuccess=file.mkdirs();
	}else if(type.equals("file")){
		isSuccess=file.createNewFile();
	}else if(type.equals("topology")){
		String templatePath = SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"develop"+File.separator+"template";
//		String templatePath=SystemProperty.getContextProperty("develop.template.directory");
		File templateFile= new File(templatePath);
		File[] templateFiles = templateFile.listFiles();
		for(int i=0;i<templateFiles.length;i++){
			FileUtil.copyFile(templateFiles[i].getAbsolutePath(), file.getAbsolutePath());
		}
		
	}
	
	resultMap.put("isSuccess", isSuccess);

	return resultMap;
}

/**
 * 重命名文件或目录
 */
@Override
public Map renameDirectoryOrFile(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	System.out.println("oldfilepath:"+JSON.Encode(params));
	Object directory=params.get("directory");
	String oldfilepath=params.get("path");
	String newFile=params.get("fileName");
	String parentPath=params.get("parentPath");
	boolean isSuccess=FileUtil.renameFile(oldfilepath, parentPath+File.separator+newFile);
	resultMap.put("isSuccess", isSuccess);

	return resultMap;
}

/**
 * 删除文件或目录
 */
@Override
public Map deleteDirectoryOrFile(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	String path=params.get("path");
	FileUtil.deleteFile(path);
	resultMap.put("isSuccess", true);
	return resultMap;
}

/**
 * 
 * 打开在线代码编辑页面
 */
@Override
public Map openDevelopFile(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	String filePath=params.get("path") == null ?"":params.get("path");
	if(filePath.equals(""))return resultMap;
	String fileContent=FileUtil.readFileUnicode(filePath);
	resultMap.put("content", fileContent.trim());
	return resultMap;
}

/**
 * 保存代码内容
 */
@Override
public Map saveDevelopFile(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	resultMap.put("isSuccess", false);
	String filePage=params.get("filePage") == null ?"":params.get("filePage");
	String path=params.get("path") == null ?"":params.get("path");
	FileUtil.writeFileUnicode(path, filePage, false);
	resultMap.put("isSuccess", true);
	return resultMap;
}

/**
 * 发布
 */
@Override
public Map release(Map<String, String> params) throws Exception {
	Map resultMap = new HashMap();
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); 
	resultMap.put("isSuccess", false);
	String path=params.get("path") == null ?"":params.get("path");
	String release=params.get("release") == null ?"":params.get("release");
	String releaseRootPath = SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"release";
//	String releaseCommonPath = SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"common";
//	String tmpPath=System.getProperty("DCBPortal.root")+"tmpFile"+File.separator+df.format(new Date()) + "_" + new Random().nextInt(1000);
//	String topologyRootPath= tmpPath+File.separator+new File(path).getName();
////	String releaseRootPath=SystemProperty.getContextProperty("develop.release.root");
//	if(!release.endsWith(".zip")){
//		release+=".zip";
//	}
//	FileUtil.copyFile(path, tmpPath);
//	
//	File templateFile= new File(releaseCommonPath);
//	File[] templateFiles = templateFile.listFiles();
//	for(int i=0;i<templateFiles.length;i++){
//		FileUtil.copyFile(templateFiles[i].getAbsolutePath(), topologyRootPath);
//	}
//	String dest=releaseRootPath+File.separator+release;
//	ZipCompressor.zip(topologyRootPath, dest.replaceAll(File.separator+"+", File.separator));
//	FileUtil.deleteFile(tmpPath);
	if(!release.endsWith(".zip")){
		release+=".zip";
	}
	String dest=releaseRootPath+File.separator+release;
	ZipCompressor.zip(path, dest.replaceAll(File.separator+"+", File.separator));
	resultMap.put("isSuccess", true);
	
	return resultMap;
}

public static void main(String[] args){
	try {
		String a="F:/TMP/develop\\\\template";
		System.out.println(a.replaceAll("\\\\+", "/"));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
