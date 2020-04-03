package com.tydic.bean;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import com.tydic.util.ftp.FileRecord;
import com.tydic.util.ftp.Trans;

public class RecursiveFile {
	private List<FileRecord> files ;
	private Map<String, String> usedMarks;
	private Map<String, Map<String, String>> clusterMap;
		
	public	RecursiveFile(List<FileRecord> files){
		this.files = files;
	}

	public void treeList(Trans trans, String subPath, String parentId,FileRecord clusterRootFile){
		try {
			Vector<FileRecord> subFile = null;
			// 获取子目录下的所有文件列表以及目录
			subFile = trans.getFileList(subPath);
			if (subFile != null && subFile.size() > 0) {
				for (int i = 0; i < subFile.size(); i++) {
					// 获取到每一个文件对象
					FileRecord file = subFile.get(i);
					String fileAbsolutePath = (file.getFilePath()+"/"+file.getFileName()).replaceAll("/+", "/");
					file.setFilePath(file.getFilePath().replaceAll("/+", "/"));
					// 给于文件对象一个随机id
					String currentId = UUID.randomUUID().toString().replaceAll("-", "");
					file.setCurrId(currentId);
					// 给于文件对象父节点的id
					file.setParentId(parentId);

					if(clusterRootFile != null && clusterRootFile.getClusterCode() != null){
						file.setIsCluster(clusterRootFile.getIsCluster());
						file.setClusterType(clusterRootFile.getClusterType());
						file.setClusterId(clusterRootFile.getClusterId());
						file.setClusterCode(clusterRootFile.getClusterCode());
						file.setDesc(clusterRootFile.getDesc());
						String clusterRootPath = clusterRootFile.getFilePath()+"/"+clusterRootFile.getFileName();
						clusterRootPath = clusterRootPath.replaceAll("/+", "/");
						//file.setTargetPath(fileAbsolutePath.replaceFirst(clusterRootPath, ""));
					}
					file.setTargetPath(fileAbsolutePath);
					// 将对象添加到List当中

					//标记
					if(usedMarks != null && clusterRootFile != null  && clusterRootFile.getClusterCode() != null){
						String instFilePath = usedMarks.get(clusterRootFile.getClusterCode());
						if(instFilePath != null ){
							instFilePath = instFilePath.replaceAll("/+", "/");
							if(fileAbsolutePath.endsWith(instFilePath)){
								file.setIsUsed(true);
							}
						}
					}

					files.add(subFile.get(i));
					if (!file.isFile()) {
						// 新目录（查询出的当前文件夹的路径）
						String newPath;
						if(subPath.endsWith("/")){
							newPath = subPath + file.getFileName();
						}else{
							newPath = subPath + "/" + file.getFileName();
						}
						treeList(trans, newPath, currentId,clusterRootFile);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
		
	public List<FileRecord> getFiles() {
		return files;
	}
	public void setFiles(List<FileRecord> files) {
		this.files = files;
	}
	public Map<String, String> getUsedMarks() {
		return usedMarks;
	}
	public void setUsedMarks(Map<String, String> usedMarks) {
		this.usedMarks = usedMarks;
	}
	public Map<String, Map<String, String>> getClusterMap() {
		return clusterMap;
	}
	public void setClusterMap(Map<String, Map<String, String>> clusterMap) {
		this.clusterMap = clusterMap;
	}
}