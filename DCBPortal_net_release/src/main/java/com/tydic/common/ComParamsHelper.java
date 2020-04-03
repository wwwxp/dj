package com.tydic.common;

import com.tydic.bp.common.utils.tools.BlankUtil;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.common]    
  * @ClassName:    [ComParamsHelper]     
  * @Description:  [组件相关公共函数]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-7-10 上午10:47:54]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-7-10 上午10:47:54]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
public class ComParamsHelper {

	/**
	 * 判断当前字段是否符合IP
	 * @param str
	 * @return
	 */
	public static Boolean isMatchIp(String str) {
		if (str.indexOf(".") != -1 && str.replace(".", "").matches("^[0-9]*$")) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * 获取集群类型字符串
	 * @param clusterType
	 * @return
	 */
	public static String revertStr(String clusterType) {
		String str = "";
		if (!BlankUtil.isBlank(clusterType) && clusterType.indexOf(",") != -1) {
			String  [] clusterList = clusterType.split(",");
			for (int i=0; i<clusterList.length; i++) {
				str += "'" + clusterList[i] + "',";
			}
			str = str.substring(0, str.length() - 1);
		} else {
			str = "'" + clusterType  + "'";
		}
		return str;
	}
	
	
	
	
	
	
	
	
	
	
}
