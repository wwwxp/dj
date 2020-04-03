package com.tydic.dcm.util.condition;

import java.util.Hashtable;
import java.util.Vector;

import com.tydic.bp.common.utils.tools.BlankUtil;

public class MsgFormat {
	// ****************************************************************
	// MsgFormat
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// 1.MsgFormat create never throw Exception
	// 2.format may throw Excepton
	// OPT_NULL_THROWN = true may throw Exception
	// OPT_NULL_THROWN = false naver throw Exception and use OPT_NULL_REPLACE
	// replace null
	// ****************************************************************
	public boolean OPT_NULL_THROWN;
	public String OPT_NULL_REPLACE;
	
	protected Vector<Object> itemList;
	
	/**
	 * 
	 * @param fmt <ori_file_name>_<sysdate><systime>.3
	 */
	public MsgFormat(String fmt) {
		if (BlankUtil.isBlank(fmt)) {
			return;
		}
		
		itemList = new Vector<Object>();
		
		while(fmt.length() > 0) {
			int bpos = fmt.indexOf("<");
			if (bpos >= 0) {
				int epos = fmt.indexOf(">", bpos + 1);
				if (epos > bpos) {
					if (bpos > 0) {
						itemList.add(fmt.substring(0, bpos));
					}
					itemList.add(new MsgItem(fmt.substring(bpos + 1, epos)));
					fmt = fmt.substring(epos + 1);
				} else {
					itemList.add(fmt);
					break;
				}
			} else {
				itemList.add(fmt);
				break;
			}
		}
		
		OPT_NULL_THROWN = false;
		OPT_NULL_REPLACE = "";
	}
	
	/**
	 * 获取重命令后的文件名称
	 * @param params
	 * @return
	 */
	public String format(Hashtable<String, String> params) {
		String retStr = "";
		if (itemList != null && itemList.size() > 0) {
			for (int i=0; i<itemList.size(); i++) {
				Object obj = itemList.get(i);
				if (obj instanceof MsgItem) {
					MsgItem msgItem = (MsgItem) obj;
					String value = msgItem.format(params);
					if (OPT_NULL_THROWN) {
						if (value == null) {
							throw new java.lang.NullPointerException("MsgFormat::format value=null  item:"
							        + msgItem.toString());
						} else {
							retStr += value;
						}
					} else {
						if (value == null) {
							value = OPT_NULL_REPLACE;
						}
						retStr += value;
					}
				} else {
					retStr += obj;
				}
			}
		}
		return retStr;
	}

	public Vector<Object> getItemList() {
		return itemList;
	}
}
