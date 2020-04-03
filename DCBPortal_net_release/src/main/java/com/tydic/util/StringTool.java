package com.tydic.util;

import com.tydic.bp.common.utils.tools.BlankUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
		//return (((Object)obj).toString()).trim();
		return String.valueOf(obj).trim();
	}
	/**
	 * 判断版本号谁大， version1比version2 返回1  version1和version2相等 返回0 version1和version2小 返回-1 
	 * @param version1  当前上传版本包 1.2.1.1
	 * @param version2  历史最新版本   1.1.1.1
	 * @return
	 */
	public static int compareVersion(String version1, String version2) {
	    if (version1.equals(version2)) {
	        return 0;
	    }
	    String[] version1Array = version1.split("\\.");
	    String[] version2Array = version2.split("\\.");
	    int index = 0;
	    //获取最小长度值
	    int minLen = Math.min(version1Array.length, version2Array.length);
	    int diff = 0;
	    //循环判断每位的大小
	    while (index < minLen && (diff = Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
	        index++;
	    }
	    if (diff == 0) {
	        //如果位数不一致，比较多余位数
	        for (int i = index; i < version1Array.length; i++) {
	            if (Integer.parseInt(version1Array[i]) > 0) {
	                return 1;
	            }
	        }

	        for (int i = index; i < version2Array.length; i++) {
	            if (Integer.parseInt(version2Array[i]) > 0) {
	                return -1;
	            }
	        }
	        return 0;
	    } else {
	        return diff > 0 ? 1 : -1;
	    }
	}

	/**
	 * 判断是否为IPV4地址
	 * @param ipStr
	 * @return
	 */
	public static boolean isIPV4Legal(String ipStr){
		if(StringUtils.isEmpty(ipStr)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.([0-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");
		Matcher matcher = pattern.matcher(ipStr);
		return matcher.find();
	}
}