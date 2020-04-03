package com.tydic.dcm.util.tools;

import java.util.Calendar;
import java.util.Date;

import com.tydic.bp.common.utils.tools.DateUtil;

public class TimeTool {

	/**
	 * 用于返回当月的所在旬，如2008年6月3日，这返回200806_01;
	 * 
	 * @return result String形式的旬；
	 */
	public static String getTenDays() {
		String result = null;
		int day = Calendar.getInstance().get(Calendar.DATE);
		String month = DateUtil.getCurrent(DateUtil.dateMonthPattern);
		if ((day >= 1) && (day <= 9))
			result = month + "_01";
		else if ((day >= 10) && (day <= 19))
			result = month + "_02";
		else
			result = month + "_03";
		return result;
	}
	
	/**
	 * 获取当前时间毫秒数
	 * @return
	 */
	public static long getTime() {
		return (new Date()).getTime();
	}
	
	/**
	 * 获取当前时间所在年份
	 * @return
	 */
	public static int getYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	/**
	 * 获取当前时间月份
	 * @return
	 */
	public static int getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}
}