package com.tydic.dcm.enums;

/**
 * 分发类型:1消息，2ftp
 * @ClassName: DistTaskTypeEnum
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Prject: dcm-server-ah_new
 * @author: tianjc
 * @date 2017年12月14日 上午10:31:57
 */
public enum DistTaskTypeEnum {
	DIST_MSG(1, "消息分发"), DIST_FTP(2, "FTP分发");

	// 任务类型
	private Integer tskType;
	// 任务描述
	private String desc;

	DistTaskTypeEnum(int tskType, String desc) {
		this.tskType = tskType;
		this.desc = desc;
	}

	public Integer getTskType() {
		return tskType;
	}

	public void setTskType(Integer tskType) {
		this.tskType = tskType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
