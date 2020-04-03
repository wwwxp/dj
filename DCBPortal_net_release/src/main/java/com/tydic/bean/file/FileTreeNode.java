package com.tydic.bean.file;


public class FileTreeNode {
	//文件ID
	private int id;
	//父节点ID
	private int parentId;
	//文件名称
	private String name;
	//是否是目录
	private boolean directory;
	//文件路径，如：E:\jstorm.txt ,D:\apache-tomcat-6.0.41\bin
	private String path;
	//文件父目录路径，如：D:\apache-tomcat-6.0.41\bin
	private String parentPath;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isDirectory() {
		return directory;
	}
	public void setDirectory(boolean isDirectory) {
		this.directory = isDirectory;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
//	@Override
//	public int compareTo(Object o) {
//		// TODO Auto-generated method stub
//		FileTreeNode fileTreeNode = (FileTreeNode)o;
//		return fileTreeNode.name.compareTo(this.name);
//	}
	
	

}
