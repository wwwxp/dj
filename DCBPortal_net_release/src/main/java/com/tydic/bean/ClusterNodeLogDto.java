package com.tydic.bean;

import java.io.Serializable;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_v1.0]   
  * @Package:      [com.tydic.bean]    
  * @ClassName:    [ClusterNodeLogDto]     
  * @Description:  [节点伸缩日志记录DTO]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2018-3-9 上午10:27:58]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2018-3-9 上午10:27:58]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
public class ClusterNodeLogDto implements Serializable {
	private static final long serialVersionUID = 1L;

	// 策略ID
	private String strategyId;
	// 触发结果
	private String triggerResult;
	// 主机IP列表
	private String hostIpList;
	// 主机ID列表
	private String hostIdList;
	// 执行过程信息输出
	private String execMessage;
	// 执行结果
	private String execResult;
	
	private String ruleMsg;
	private String hostNormMsg;

	public ClusterNodeLogDto() {
		super();
	}

	public ClusterNodeLogDto(String strategyId, String triggerResult,
			String hostIpList, String hostIdList, String execMessage,
			String execResult,String ruleMsg,String hostNormMsg) {
		super();
		this.strategyId = strategyId;
		this.triggerResult = triggerResult;
		this.hostIpList = hostIpList;
		this.hostIdList = hostIdList;
		this.execMessage = execMessage;
		this.execResult = execResult;
		this.ruleMsg = ruleMsg;
		this.hostNormMsg = hostNormMsg;
	}

	/**
	 * @return the ruleMsg
	 */
	public String getRuleMsg() {
		return ruleMsg;
	}

	/**
	 * @param ruleMsg the ruleMsg to set
	 */
	public void setRuleMsg(String ruleMsg) {
		this.ruleMsg = ruleMsg;
	}

	/**
	 * @return the hostNormMsg
	 */
	public String getHostNormMsg() {
		return hostNormMsg;
	}

	/**
	 * @param hostNormMsg the hostNormMsg to set
	 */
	public void setHostNormMsg(String hostNormMsg) {
		this.hostNormMsg = hostNormMsg;
	}

	public String getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(String strategyId) {
		this.strategyId = strategyId;
	}

	public String getTriggerResult() {
		return triggerResult;
	}

	public void setTriggerResult(String triggerResult) {
		this.triggerResult = triggerResult;
	}

	public String getHostIpList() {
		return hostIpList;
	}

	public void setHostIpList(String hostIpList) {
		this.hostIpList = hostIpList;
	}

	public String getHostIdList() {
		return hostIdList;
	}

	public void setHostIdList(String hostIdList) {
		this.hostIdList = hostIdList;
	}

	public String getExecMessage() {
		return execMessage;
	}

	public void setExecMessage(String execMessage) {
		this.execMessage = execMessage;
	}

	public String getExecResult() {
		return execResult;
	}

	public void setExecResult(String execResult) {
		this.execResult = execResult;
	}

}
