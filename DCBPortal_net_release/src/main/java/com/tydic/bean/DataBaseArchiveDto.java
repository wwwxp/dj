package com.tydic.bean;

public class DataBaseArchiveDto {
	//JOB_ID
	private String jobId;
	// 归档任务id
	private String archiveId;
	// 源数据源id
	private String sourDsId;
	// 源数据源 名
	private String sourDsName;
	// 目标数据源id
	private String destDsId;
	// 目标数据源名
	private String destDsName;
	// 源数据源类型 
	private String sourDsType;
	// 目标数据库类型
	private String destDsType;
	//源数据库表名
	private String sourTableName;
	//目标数据库表名
	private String destTableName;
	//执行sql规则
	private String filterRule;
	//归档规则
	private String execRuleStr;
	
	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public String getSourDsId() {
		return sourDsId;
	}
	public void setSourDsId(String sourDsId) {
		this.sourDsId = sourDsId;
	}
	public String getDestDsId() {
		return destDsId;
	}
	public void setDestDsId(String destDsId) {
		this.destDsId = destDsId;
	}
	public String getSourDsType() {
		return sourDsType;
	}
	public void setSourDsType(String sourDsType) {
		this.sourDsType = sourDsType;
	}
	public String getDestDsType() {
		return destDsType;
	}
	public void setDestDsType(String destDsType) {
		this.destDsType = destDsType;
	}
	public String getSourTableName() {
		return sourTableName;
	}
	public void setSourTableName(String sourTableName) {
		this.sourTableName = sourTableName;
	}
	public String getDestTableName() {
		return destTableName;
	}
	public void setDestTableName(String destTableName) {
		this.destTableName = destTableName;
	}
	public String getFilterRule() {
		return filterRule;
	}
	public void setFilterRule(String filterRule) {
		this.filterRule = filterRule;
	}
	public String getExecRuleStr() {
		return execRuleStr;
	}
	public void setExecRuleStr(String execRuleStr) {
		this.execRuleStr = execRuleStr;
	}
	public String getSourDsName() {
		return sourDsName;
	}
	public void setSourDsName(String sourDsName) {
		this.sourDsName = sourDsName;
	}
	public String getDestDsName() {
		return destDsName;
	}
	public void setDestDsName(String destDsName) {
		this.destDsName = destDsName;
	}
	public DataBaseArchiveDto() {
		super();
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public DataBaseArchiveDto(String archiveId,String sourDsId,String destDsId,
			String filterRule,String execRuleStr) {
		this.archiveId = archiveId;
		this.sourDsId = sourDsId;
		this.destDsId = destDsId;
		this.filterRule = filterRule;
		this.execRuleStr = execRuleStr;
		
	}
	@Override
	public String toString() {
		StringBuilder builderStr = new StringBuilder();
		builderStr.append("\n\n源数据库:").append(sourDsName).append("\n源表：").append(sourTableName).append("\n\n目标数据库：")
				.append(destDsName).append("\n目标表：").append(destTableName).append("\n\n归档规则：").append(filterRule);
		return builderStr.toString();
	}
	
}
