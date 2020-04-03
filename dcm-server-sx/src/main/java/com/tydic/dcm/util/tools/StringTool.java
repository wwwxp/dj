package com.tydic.dcm.util.tools;

import java.util.StringTokenizer;
import java.util.Vector;

import com.tydic.bp.common.utils.tools.BlankUtil;


/**
 * 字符串分割帮助类
 * @author Yuanh
 *
 */
public class StringTool {
	
	/**
	 * 对字符串进行分割,默认分割方式是  \t\n\r\f
	 * @param str
	 * @return
	 */
	public static Vector<String> tokenStringChar(String str) {
		return tokenStringChar(str, " \t\n\r\f");
	}

	/**
	 * 按照参数制定方式分割字符串
	 * @param str
	 * @param delim
	 * @return
	 */
	public static Vector<String> tokenStringChar(String str, String delim) {
		StringTokenizer stk = new StringTokenizer(str, delim);
		Vector<String> vc = new Vector<String>();
		if (stk.countTokens() > 0)
			while (stk.hasMoreTokens())
				vc.add(stk.nextToken());
		return vc;
	}

	/**
	 * 对字符串进行分割
	 * @param str
	 * @param sp
	 * @return
	 */
	public static Vector<String> tokenString(String str, String sp) {
		Vector<String> items = new Vector<String>();
		while (str.length() > 0) {
			if (sp.length() == 0) {
				items.add(str);
				break;
			} else {
				int pos = str.indexOf(sp);
				if (pos >= 0) {
					if (pos > 0)
						items.add(str.substring(0, pos));
					str = str.substring(pos + sp.length());
				} else {
					items.add(str);
					break;
				}
			}
		}
		return items;
	}
	
	/**
	 * 转化字符串
	 * @param str
	 * @return
	 */
	public static String nullObject2String(String str) {
		if (BlankUtil.isBlank(str)) {
			return "";
		}
		return str.trim();
	}
	
	/**
	 * 将Map对应的String值转化为字串
	 * @param obj
	 * @return
	 */
	public static String object2String(Object obj) {
		if (BlankUtil.isBlank(obj)) {
			return "";
		}
		return (((Object)obj).toString()).trim();
	}
}