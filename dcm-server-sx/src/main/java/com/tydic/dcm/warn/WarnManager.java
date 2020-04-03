package com.tydic.dcm.warn;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.util.jdbc.JdbcUtil;

/**
 * 告警管理
 * @author Yuanh
 *
 */
public class WarnManager {

	/**
	 * 告警管理日志
	 */
	private static Logger logger = Logger.getLogger(WarnManager.class);
	//采集文件失败
	public static final String TRAN_WARN_COLL_FAIL = "COLL_FAIL";
	//采集文件大小不一致
	public static final String TRAN_WARN_CFSIZE_NEQU = "CFSIZE_NEQU";
	//采集文件大小不正常
	public static final String TRAN_WARN_CFSIZE_NNOR = "CFSIZE_NNOR";
	//采集文件数量不正常
	public static final String TRAN_WARN_CFCONT_NNOR = "CFCONT_NNOR";
	//采集文件序号不连续
	public static final String TRAN_WARN_CFSEQ_NNOR ="CFSEQ_NNOR";
	//采集文件时间不正确
	public static final String TRAN_WARN_CFTIME_NNOR = "CFTIME_NNOR";
	//采集链路参数缺失
	public static final String TRAN_WARN_CFPARAM_NNOR = "CFPARAM_NNOR";
	
	//分发文件失败
	public static final String TRAN_WARN_DIST_FAIL = "DIST_FAIL";
	//分发文件大小不一致
	public static final String TRAN_WARN_DFSIZE_NEQU = "DFSIZE_NEQU";
	//分发文件大小不正常
	public static final String TRAN_WARN_DFSIZE_NNOR = "DFSIZE_NNOR";
	//分发文件数目不正常
	public static final String TRAN_WARN_DFCONT_NNOR = "DFCONT_NNOR";
	//分发文件序号不连续
	public static final String TRAN_WARN_DFSEQ_NNOR = "DFSEQ_NNOR";
	//分发文件时间不正确
	public static final String TRAN_WARN_DFTIME_NNOR = "DFTIME_NNOR";
	//分发链路参数缺失
	public static final String TRAN_WARN_DFPARAM_NNOR = "DFPARAM_NNOR";
	
	//后继处理错误
	public static final String COLL_LATE_HANDLE_FAIL = "COLL_LATE_FAIL";
	//FTP错误
	public static final String CONN_FTP_ERROR = "CONN_FTP_ERROR";
	//交换机长时间无文件生成
	public static final String SWITCH_NO_FILE = "SWITCH_NO_FILE";
	
	/**
	 * 添加告警日志表
	 * 
	 * @param addr_id
	 * @param warn_code
	 * @param link_id
	 * @param file_name
	 * @param content
	 */
	public static void tranWarn(String addr_id, String warn_code,
			String link_id, String file_name, String content) {
		logger.debug("begin add tranWarn log, addr_id ---> " +addr_id + ", warn_code ---> " + warn_code 
				+ ", linkId ---> " + link_id + ", file_name ---> " + file_name + ", content ---> " + content);
		Map<String, Object> warnParams = new HashMap<String, Object>();
		warnParams.put("WARN_CODE", warn_code);
		warnParams.put("DEV_ID", link_id);
		warnParams.put("FILE_NAME", file_name);
		warnParams.put("WARN_TIME", DateUtil.getCurrent(DateUtil.allPattern));
		warnParams.put("CONTENT", content);
		warnParams.put("STATE", "no-handle");
		int count = JdbcUtil.insertObject("collectMapper.addDcTranWarn", warnParams, FrameConfigKey.DEFAULT_DATASOURCE);
		if (count <= 0) {
			int execTime = 0;
			while(count > 0 || execTime >= 3) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count = JdbcUtil.insertObject("collectMapper.addDcTranWarn", warnParams, FrameConfigKey.DEFAULT_DATASOURCE);
				execTime++;
			}
		}
		logger.debug("end add tranWarn log, linkId:" + link_id);
	}
}
