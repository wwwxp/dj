package com.tydic.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.tydic.framework.osgi.commons.lang.ObjectUtils;

/**
 * Map转换工具
 * @ClassName: MapConverter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Prject: DCBPortal_net_release
 * @author: tianjc
 * @date 2017年6月29日 下午7:32:23
 */
public class MapConverter {
	
	public static HashMap<String,String> convert(Map<String,Object> param){
		HashMap<String,String> result = new HashMap<String,String>();
		if(MapUtils.isNotEmpty(param)){
			Set<String> keys = param.keySet();
			for(String key : keys){
				String value = ObjectUtils.toString(param.get(key));
				result.put(key, value);
			}
		}
		
		return result; 
	}
	

}
