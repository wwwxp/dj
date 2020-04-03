package com.tydic.dcm.ftran;

import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.util.condition.Condition;
import com.tydic.dcm.util.condition.MsgFormat;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;
import com.tydic.dcm.util.tools.TimeTool;

public class Filter {

	private static final int SEQUENCE_LENGTH = 8;
	protected static final String DEDUATE_OCH = ".";
	protected static final String LATE_HANDLE_DELETE = "delete";
	protected static final String LATE_HANDLE_RENAME = "rename";
	
	protected String och;
	protected String unCollNum;
	protected Condition tranCdt;
	protected MsgFormat dstNameRule;
	protected MsgFormat reNameRule;
	
	protected String lateHandleMethod;
	protected String oriLatehandleMethod;
	
	
	public Filter(Hashtable<String, Object> params) throws Exception {
		//ori_file_name like * && ori_file_time %>=20140805140000
		//采集条件
		String transCondition = StringTool.object2String(params.get("transfer_condition"));
		if (BlankUtil.isBlank(transCondition)) {
			tranCdt = null;
		} else {
			//根据||划分为一层集合，&&划分为二层集合
			tranCdt = new Condition(transCondition);
		}
		
		//命名类型
		och = StringTool.object2String(params.get("ori_file_name_ch"));
		if (BlankUtil.isBlank(och)) {
			och = DEDUATE_OCH;
		}
		//暂时不采集文件数
		unCollNum = StringTool.object2String(params.get("uncoll_num"));
		if (BlankUtil.isBlank(unCollNum)) {
			unCollNum = ParamsConstant.PARAMS_0;
		}
		//目标话单处理方式
		lateHandleMethod = StringTool.object2String(params.get("late_handle_method"));
		//文件采集后命名规则
		dstNameRule = new MsgFormat(StringTool.object2String(params.get("dst_file_name_rule")));
		
		//原始话单处理方式
		oriLatehandleMethod = StringTool.object2String(params.get("ori_late_handle_method"));
		//采集后源文件命名
		reNameRule = new MsgFormat(StringTool.object2String(params.get("ori_file_rename_rule")));
	}
	
	/**
	 * 解析Transfer_condition参数
	 * @param record
	 * @return
	 */
	protected Hashtable<String, String> getFileParams(FileRecord record) {
		Hashtable<String, String> items = new Hashtable<String, String>();
		
		long tonowdays = (TimeTool.getTime() - record.getTime().getTime()) / (24 * 3600 * 1000);
		long tonowhours = (TimeTool.getTime() - record.getTime().getTime()) / (3600 * 1000);
		long tonowminutes = (TimeTool.getTime() - record.getTime().getTime()) / (60 * 1000);
		long tonowseconds = (TimeTool.getTime() - record.getTime().getTime()) / 1000;
		
		items.put("ori_file_name", record.getFileName());
		items.put("ori_file_length", "" + record.getFileLength());
		items.put("ori_file_time", DateUtil.format(record.getTime(), DateUtil.fullPattern));
		items.put("tonowdays", "" + tonowdays);
		items.put("tonowhours", "" + tonowhours);
		items.put("tonowminutes", "" + tonowminutes);
		items.put("tonowseconds", "" + tonowseconds);
		items.put("sysdate", DateUtil.getCurrent(DateUtil.datePattern));
		items.put("systime", DateUtil.getCurrent(DateUtil.timePattern));
		java.util.Vector<String> vc = StringTool.tokenStringChar(record.getFileName(), och);
		for (int i = 0; i < vc.size(); i++) {
			items.put("item[" + i + "]", vc.get(i));
		}
		return items;
	}
	
	/**
	 * 检查过滤文件
	 * @param fileRecord
	 * @return
	 */
	public boolean check(FileRecord fileRecord) {
		return check(fileRecord, tranCdt);
	}
	
	/**
	 * 校验文件是否需要过滤
	 * @param fileRecord
	 * @param cdt
	 * @return
	 */
	public boolean check(FileRecord fileRecord, Condition cdt) {
		if (cdt == null) {
			return true;
		} else {
			return cdt.check(this.getFileParams(fileRecord));
		}
	}
	
	/**
	 * 目标文件重命令规则
	 * @param fileRecord
	 * @return
	 */
	public String getDstFileName(FileRecord fileRecord) {
		return dstNameRule.format(this.getFileParams(fileRecord));
	}
	
	/**
	 * 目标文件重命名规则,包含文件序列
	 * @param fileRecord
	 * @param lastSequence
	 * @return
	 */
	public String getDstFileName(FileRecord fileRecord, Long lastSequence) {
		Hashtable<String, String> fileParams =  this.getFileParams(fileRecord);
		fileParams.put("sequence", getSeqStr(lastSequence, SEQUENCE_LENGTH));
		return dstNameRule.format(fileParams);
	}
	
	/**
	 * 获取序列，补位
	 * @param lastSequence
	 * @param seqLength
	 * @return
	 */
	private String getSeqStr(Long lastSequence, int seqLength) {
		StringBuffer buffer = new StringBuffer(SEQUENCE_LENGTH);
		for (int i=0; i<(seqLength - StringTool.object2String(lastSequence).length()); i++) {
			buffer.append(ParamsConstant.PARAMS_0);
		}
		buffer.append(lastSequence);
		return buffer.toString();
	}
	
	/**
	 * 源文件重命令规则
	 * @param fileRecord
	 * @return
	 */
	public String getOriFileName(FileRecord fileRecord) {
		String fileName = reNameRule.format(this.getFileParams(fileRecord));
		if(StringUtils.isBlank(fileName)){
			fileName = fileRecord.getFileName();
		}
		
		return fileName;
	}
}
