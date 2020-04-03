package com.tydic.dcm.util.condition;

import java.util.Date;
import java.util.Hashtable;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;

public class MsgItem {
	protected static final String KEY_TIME = "$time";
	protected static final String DEFUALT_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

	protected String name;
	protected String desc;

	protected MsgItem(String msg) {
		int pos = msg.indexOf("@");
		if (pos >= 0) {
			name = msg.substring(0, pos).trim();
			desc = msg.substring(pos + 1).trim();
		} else {
			name = msg.trim();
		}
	}

	/**
	 * 获取项对应的值
	 * 
	 * @param params
	 * @return
	 */
	protected String format(Hashtable<String, String> params) {
		if (KEY_TIME.equals(name)) {
			if (BlankUtil.isBlank(desc)) {
				desc = DateUtil.allPattern;
			}
			return DateUtil.format(new Date(), desc);
		} else {
			return params.get(name);
		}
	}

	public String toString() {
		if (desc == null)
			return "<" + name + ">";
		else
			return "<" + name + "@" + desc + ">";
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

}
