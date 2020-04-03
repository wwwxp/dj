package com.tydic.util;

import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.util.ftp.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TpsFileUtils {
	//public static Trans trans = null;
	static {
		//trans = getClient();
	}

	public static Map<String,Object> getData() {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		List<String> ips =  new ArrayList<String>();
		Map<String,Object> ipMap = new HashMap<String,Object>();
		Trans trans = null;
		try {
			//if(trans ==null /*|| !trans.isLogin()*/){
			 trans = getClient();
			//}
			String filePath = "/project/bill03/app/data/wrcdr";
			if(trans.isExistPath(filePath)){
				Vector<FileRecord> fileRecords = trans.getFileList(filePath);
				 for(FileRecord file : fileRecords){
				 	if(file.isDirectory()){
				 		continue;
					}

					String filePathName = file.getFilePath()+"/" + file.getFileName();
				 	String num = FileUtil.readInputStream(trans.get(filePathName));
				 	if(StringUtils.isEmpty(num)){
						num = "0";
					}
					 ipMap.put(file.getFileName(),num);
					 ips.add(file.getFileName());
					 //dataList.add(ipMap);
				 }
			}
			resultMap.put("DATA",ipMap);
			resultMap.put("IPS",ips);

			resultMap.put("TIME",DateUtil.getCurrent("HHmm"));
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(trans!= null){
				trans.close();
			}
		}
		return null ;
	}
	public static Trans getClient(){
		Trans sftp = null;
		try {
			sftp = FTPUtils.getFtpInstance("192.168.161.16","testbill","testbillt","sftp");
			sftp.login();
		} catch (Exception e) {
			if(sftp !=null){
				sftp.close();
			}
			e.printStackTrace();
		} finally {

		}
		return sftp;
	}
	public static void main(String [] args){
		while(true){
			System.out.println(getData());
			try {
				Thread.sleep(500000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
