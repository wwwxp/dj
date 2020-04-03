package com.tydic.dcm.enums;

/**
 * 文件存储介质 ：（local本地文件系统，dfs分布式文件系统）
 * 
 * @ClassName: FileStoreTypeEnum
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Prject: dcm-server-ah_new
 * @author: tianjc
 * @date 2017年12月14日 上午10:31:57
 */
public enum FileStoreTypeEnum {
	FILE_STORE_LOCAL("local", "本地文件系统存储"), 
	FILE_STORE_DFS("dfs", "分布式文件系统存储"),
	FILE_STORE_FAST_DFS("fastdfs", "分布式文件系统存储(Fastdfs)");

	// 文件系统类型
	private String fileStoreType;
	// 任务描述
	private String desc;

	FileStoreTypeEnum(String fileStoreType, String desc) {
		this.fileStoreType = fileStoreType;
		this.desc = desc;
	}

	public String getFileStoreType() {
		return fileStoreType;
	}

	public void setFileStoreType(String fileStoreType) {
		this.fileStoreType = fileStoreType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
