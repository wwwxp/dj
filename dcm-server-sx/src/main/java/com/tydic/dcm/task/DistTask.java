package com.tydic.dcm.task;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.tydic.dcm.ftran.DistLink;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;

/**
 * 分发任务
 * @author Yuanh
 *
 */
public class DistTask extends Thread {

	private static Logger logger = Logger.getLogger(DistTask.class);
	
	/**
	 * 分发链路ID
	 */
	private String devId;
	
	/**
	 * 链路类型
	 */
	private String devType;
	
	/**
	 * 任务类型
	 */
	private String taskType;
	
	/**
	 * 业务参数
	 */
	private Map<String, Object> busParams;
	
	/**
	 * 线程计数器
	 */
	private CountDownLatch latch;
	
	/**
	 * 分发链路
	 */
	private DistLink link;
	
	/**
	 * 分发任务构造函数
	 * 
	 * @param devId   分发链路ID
	 * @param devType 分发链路类型
	 * @param taskType  任务类型
	 * @param busParams 业务参数
	 */
	public DistTask(String devId, String devType, String taskType, Map<String, Object> busParams) {
		this.devId = devId;
		this.devType = devType;
		this.taskType = taskType;
		this.busParams = busParams;
	}
	
	/**
	 * 分发任务构造函数
	 * 
	 * @param devId   分发链路ID
	 * @param devType 分发链路类型
	 * @param taskType  任务类型
	 * @param busParams 业务参数
	 * @param latch 计数器
	 */
	public DistTask(String devId, String devType, String taskType, Map<String, Object> busParams, CountDownLatch latch) {
		this.devId = devId;
		this.devType = devType;
		this.taskType = taskType;
		this.busParams = busParams;
		this.latch = latch;
	}
	
	/**
	 * 分发任务构造函数
	 * @Title:DistTask
	 * @Description:
	 * @param link
	 * @param latch
	 */
	public DistTask(DistLink link, String devType, String taskType, CountDownLatch latch){
		this.link = link;
		this.devType = devType;
		this.taskType = taskType;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		try {
			//分发链路
			if (ParamsConstant.TYPE_DIST_LINK.equals(this.devType)) {
				//自动分发
				if (ParamsConstant.TASK_TYPE_AUTO_DIST.equalsIgnoreCase(taskType)) {
					link.autoTransfer();
					logger.debug("auto distribute Ok, devId: " + devId + ", latn_Id_Sub:" + this.link.getLatn_Id_Sub());
				} 
				
				//手动分发
				else if (ParamsConstant.TASK_TYPE_HAND_DIST.equalsIgnoreCase(taskType)) {
					//获取手动采集采集列表
					String file_list = StringTool.object2String(busParams.get("file_list"));
					//是否启动过滤条件
					boolean enableFilter = (Boolean) busParams.get("enable_filter");
					
					//获取采集链路信息
					DistLink link = new DistLink(devId);
					link.handTransfer(file_list, enableFilter);
					logger.debug("hand distribute Ok, devId: " + devId);
				}
				
//				//立即分发
//				else if (ParamsConstant.TASK_TYPE_REAL_DIST.equalsIgnoreCase(taskType)) {
//					DistLink link = new DistLink(devId);
//					link.realTransfer();
//					logger.debug("real distribute Ok, devId: " + devId);
//				} 
			}
		} catch (Exception e) {
			logger.error("distribute exception, devId: " + devId +", taskType:" + taskType, e );
			e.printStackTrace();
		} finally {
			if (latch != null) {
				latch.countDown();
				logger.info("latch count: " + latch.getCount());
			}
		}
	}
}
