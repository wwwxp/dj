/**   
* @Title: FileRecordConvertor.java 
* @Package com.tydic.dcm.util.tools 
* @Description: TODO(用一句话描述该文件做什么) 
* @author:tydic  
* @date 2016年7月26日 上午11:33:45 
* @version V1.0   
*/
package com.tydic.dcm.util.tools;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.ftran.FileRecord;

/**
 * @ClassName: FileRecordConvertor
 * @Description: TODO(FileRecord的转换器)
 * @Prject: dcm-server_base
 * @author: 田嘉纯
 * @date 2016年7月26日 上午11:33:45
 * Redis中K-V存储规则：devId||fileName||filePath =devID||fileName||fileLength||fileTime||filePath 
 */
public class FileRecordConvertor {
	
	/**
	 * 将FileRecord转换为Redis存储的value格式
	 * @Title: toValueStr
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: String
	 * @author: tydic
	 * @date: 2016年7月27日 下午5:28:30
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public static String toValueStr(String devID, FileRecord record) {
		StringBuilder sb = new StringBuilder(devID);
		
		sb.append("||");
		sb.append(record.getFileName());
		sb.append("||");
		sb.append(record.getFileLength());
		sb.append("||");
		sb.append(DateUtil.format(record.getTime(),DateUtil.allPattern));
		sb.append("||");
		sb.append(record.getFilePath());
		
		return sb.toString();
	}
	/**
	 * 将从Redis中取出的value转换成FileRecord
	 * @Title: toFileRecord
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: FileRecord
	 * @author: tydic
	 * @date: 2016年7月27日 下午5:16:28
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public static FileRecord toFileRecord(String value) {
		//Redis中值的存储方式:devID||fileName||fileLength||fileTime||filePath
		FileRecord record = new FileRecord();
		if(BlankUtil.isBlank(value) == false){
			String[] fileInfos = value.split("||");
			
			record.setFileName(fileInfos[1]);
			record.setFileLength(Long.parseLong(fileInfos[2]));
			record.setTime(DateUtil.parse(fileInfos[3], DateUtil.allPattern));
			record.setFilePath(fileInfos[4]);
		}
		
		return record;
	}
}
