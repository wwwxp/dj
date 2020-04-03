package com.tydic.dcm.enums;

/**
 * 链路参数模板
 * @ClassName: LinkParameterModuleEnum
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Prject: dcm-server-ah_new
 * @author: tianjc
 * @date 2017年12月14日 上午11:01:04
 */
public enum LinkParameterModuleEnum {

	//采集链路参数模板
	COLL_LOCAL("COLL","采集到本地文件系统"),
	COLL_DFS("COLL_DFS","采集到分布式文件系统"),
	
	//分发链路参数模板
	DIST_FTP("DIST","分发文件到FTP"),
	DIST_MSG("DIST_MSG","分发消息到下游环节");
	
	// 文件系统类型
	private String module;
	// 任务描述
	private String desc;

	LinkParameterModuleEnum(String module, String desc) {
		this.module = module;
		this.desc = desc;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
