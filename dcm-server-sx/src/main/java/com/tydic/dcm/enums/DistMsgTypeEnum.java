package com.tydic.dcm.enums;

public enum DistMsgTypeEnum {

	DIST_MSG_MQ("mq", "分发消息到MQ"), 
	DIST_MSG_JSTORM("jstorm", "分发消息调用jstorm服务接口"), 
	DIST_MSG_TABLE("table", "分发消息写入表记录");

	// 分发消息类型:mq,jstorm,table
	private String type;
	private String desc;

	DistMsgTypeEnum(String type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
