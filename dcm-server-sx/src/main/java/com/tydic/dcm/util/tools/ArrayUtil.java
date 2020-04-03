package com.tydic.dcm.util.tools;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ArrayUtil {

	/**
	 * 获取Vector长度
	 * 
	 * @param list
	 * @return
	 */
	public static int getSize(Vector<?> list) {
		return list == null ? 0 : list.size();
	}

	/**
	 * 获取List集合长度
	 * 
	 * @param list
	 * @return
	 */
	public static int getSize(List<?> list) {
		return list == null ? 0 : list.size();
	}

	/**
	 * 获取HashTable集合长度
	 * 
	 * @param list
	 * @return
	 */
	public static int getSize(Hashtable<String, ?> list) {
		return list == null ? 0 : list.size();
	}
	
	/**
	 * 获取对象数组长度 
	 * @param list
	 * @return
	 */
	public static int getSize(Object [] list) {
		return list == null ? 0 : list.length;
	}

	public static void main(String[] args) {
		System.out
				.println(ArrayUtil.getSize(new Vector<Map<String, Object>>()));
	}
}
