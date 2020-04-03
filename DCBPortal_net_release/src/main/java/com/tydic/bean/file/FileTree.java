package com.tydic.bean.file;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FileTree {
	private FileFilter filter;
	private String rootPath;
	private  ThreadLocal<List<FileTreeNode>> tree =new ThreadLocal<List<FileTreeNode>>(){
		 @Override 
	        protected List initialValue() { 
	            return new ArrayList(); 
	        } 
		
	};
	
	public  FileTree(String rootPath,FileFilter filter) {
		super();
		this.filter=filter;
		File dir = new File(rootPath); 
		
		if(!dir.exists()){
			throw new RuntimeException("文件目录不存在："+dir.getAbsolutePath());
		}
		this.rootPath = rootPath;
		FileTreeNode node=new FileTreeNode();
   	    node.setId(0);
   	    node.setName(dir.getName());
   	    node.setDirectory(true);
        try {
			node.setPath(URLEncoder.encode(dir.getAbsolutePath(),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        tree.get().add(node);
        node.setParentId(-1);
	}

	/**
	 * 生成文件树数据
	 * @param path
	 */
	private void listFileTree(String path) { 
        File dir = new File(path); 
        File[] files = dir.listFiles(filter); 
        
        if (files == null) 
            return; 
        for (int i = 0; i < files.length; i++) { 
        	 FileTreeNode node=new FileTreeNode();
        	 node.setId(tree.get().size());
        	 node.setName(files[i].getName());
        	 node.setDirectory(false);
             try {
            	 //对路径进行编码
				node.setPath(URLEncoder.encode(files[i].getAbsolutePath(),"UTF-8"));
				 int parentId= findParentIdByParentPath(URLEncoder.encode(files[i].getParent(),"UTF-8"));
	             if(parentId != -1){
	            	 node.setParentId(parentId);
	             }
	             node.setParentPath(URLEncoder.encode(files[i].getParent(),"UTF-8"));
	             tree.get().add(node); 
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
            
             
             
             
            if (files[i].isDirectory()) { 
            	node.setDirectory(true);
            	listFileTree(files[i].getAbsolutePath()); 
            } 
        } 
	}
	/**
	 * 根据父目录路径查找父目录ID
	 * @param path
	 * @return
	 */
	private int findParentIdByParentPath(String path){
		for(int i=0;i<tree.get().size();i++){
			FileTreeNode node=tree.get().get(i);
			boolean isEqual=node.getPath().equals(path);
			if(isEqual){
				return node.getId();
			}
		}
		 return -1;
		
	}
	/**
	 * 获取文件树数据
	 * @return
	 */
	public List<FileTreeNode>  getFilesTress(){
		listFileTree(rootPath);
		return tree.get();
		
	}
}
