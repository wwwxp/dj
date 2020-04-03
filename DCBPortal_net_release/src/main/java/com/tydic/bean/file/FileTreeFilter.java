package com.tydic.bean.file;

import java.io.File;
import java.io.FileFilter;

public class FileTreeFilter implements FileFilter{
	private String[] fileType;

	@Override
	public boolean accept(File file) {
		String name = file.getName();  
		if(file.isDirectory()) return true;
		
		if(fileType == null || fileType.length <1){
			return true;
		}else if(fileType.length==1){
			
			if(fileType[0].trim().equals("")){
				return true;
			}else{
				if(name.endsWith("."+fileType[0])){
					return true;
				}
			}
		}else{
			
			for(int i=0;i<fileType.length;i++){
				
				if(name.endsWith("."+fileType[i])){
					return true;
				}
			}
		}
		return false;  
            
	}

	public String[] getFileType() {
		return fileType;
	}

	public void setFileType(String[] fileType) {
		this.fileType = fileType;
	}
	
}
