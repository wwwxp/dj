package com.tydic.dcm.util.tools;

import com.tydic.bp.common.utils.tools.BlankUtil;

/**
 * 客户端操作返回编码
 * @author Yuanh
 *
 */
public enum ClientResultEnum {

	//采集返回值枚举
	AUTO_COLL_OK("AUTO_COLL_OK", "自动采集命令执行成功!"),
	HAND_COLL_OK("HAND_COLL_OK", "手动采集命令执行成功!"),
	HAND_COLL_TIPS("HAND_COLL_TIPS", "链路正在自动采集，不能够执行手动采集， 请稍后重试!"),
	REAL_COLL_OK("REAL_COLL_OK", "立即采集命令执行成功!"),
	REAL_COLL_TIPS("REAL_COLL_TIPS","链路正在自动采集，不能够执行立即采集， 请稍后重试!"),
	STOP_COLL_OK("STOP_COLL_OK", "停止自动采集命令执行成功!"),
	
	//分发返回枚举
	AUTO_DIST_OK("AUTO_DIST_OK", "自动分发命令执行成功!"),
	HAND_DIST_OK("HAND_DIST_OK", "手动分发命令执行成功!"),
	HAND_DIST_TIPS("HAND_DIST_TIPS", "链路正在自动分发，不能够执行手动分发， 请稍后重试!"),
	REAL_DIST_OK("REAL_DIST_OK", "立即分发命令执行成功!"),
	REAL_DIST_TIPS("REAL_DIST_TIPS", "链路正在自动分发，不能够执行立即分发， 请稍后重试!"),
	STOP_DIST_OK("STOP_COLL_OK", "停止自动分发命令执行成功!"),
	EXCEPTION_DIST_OK("EXCEPTION_DIST_OK", "分发异常回收命令执行成功!");
	
	
	private String rstCode;
	private String rstMsg;
	
	private ClientResultEnum(String rstCode, String rstMsg) {
		this.rstCode = rstCode;
		this.rstMsg = rstMsg;
	}
	
	/**
	 * 根据枚举编码获取枚举对应的提示信息
	 * @param rstCode
	 * @return
	 */
	public static String getRstMsgByCode(String rstCode) {
		if (BlankUtil.isBlank(rstCode)) {
			return "";
		}
		ClientResultEnum [] clientEnumList = ClientResultEnum.values();
		if (clientEnumList != null && clientEnumList.length > 0) {
			for (int i=0; i<clientEnumList.length; i++) {
				if (rstCode.equals(clientEnumList[i].getRstCode())){
					return clientEnumList[i].getRstMsg();
				}
			}
		}
		return "";
	}

	public String getRstCode() {
		return rstCode;
	}

	public void setRstCode(String rstCode) {
		this.rstCode = rstCode;
	}

	public String getRstMsg() {
		return rstMsg;
	}

	public void setRstMsg(String rstMsg) {
		this.rstMsg = rstMsg;
	}
	
	public static void main(String[] args) {
		System.out.println(ClientResultEnum.getRstMsgByCode(ClientResultEnum.STOP_COLL_OK.getRstCode()));
	}
	
}
 