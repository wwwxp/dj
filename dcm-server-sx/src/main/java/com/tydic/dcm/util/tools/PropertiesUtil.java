package com.tydic.dcm.util.tools;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.utils.properties.SystemProperty;

/**
 * 资源文件Util类
 * @author Yuanh
 *
 */
public class PropertiesUtil {

	/**
	 * 获取资源文件Key对应的Value，如果Value为空则直接返回默认值
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getValueByKey(String key, String defaultValue) {
		String keyValue = SystemProperty.getContextProperty(key);
		if (BlankUtil.isBlank(keyValue)) {
			keyValue = defaultValue;
		}
		return keyValue;
	}
	
	/**
	 * 获取资源文件Key对应的Value，如果Value为空则直接返回默认值
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean getValueByKey(String key, boolean defaultValue) {
		String keyValue = SystemProperty.getContextProperty(key);
		if (!BlankUtil.isBlank(keyValue)) {
			return Boolean.parseBoolean(keyValue);
		}
		return defaultValue;
	}
	
	/**
	 * 获取资源文件Key对应的Value，没有默认值
	 * @param key
	 * @return
	 */
	public static String getValueByKey(String key) {
		String keyValue = SystemProperty.getContextProperty(key);
		return keyValue;
	}
}
