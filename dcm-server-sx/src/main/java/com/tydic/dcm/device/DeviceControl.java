package com.tydic.dcm.device;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.QuartzConstant;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.dcm.client.Command;
import com.tydic.dcm.client.CommandResult;
import com.tydic.dcm.ftran.CollLink;
import com.tydic.dcm.ftran.DistLink;
import com.tydic.dcm.openapi.response.OfflineCollectResp;
import com.tydic.dcm.task.DistTask;
import com.tydic.dcm.util.condition.Condition;
import com.tydic.dcm.util.exception.DcmException;
import com.tydic.dcm.util.jdbc.JdbcUtil;
import com.tydic.dcm.util.spring.SpringUtil;
import com.tydic.dcm.util.tools.ClientResultEnum;
import com.tydic.dcm.util.tools.ParamsConstant;
import com.tydic.dcm.util.tools.StringTool;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 客户端命名接入
 * @author Yuanh
 *
 */
public class DeviceControl {
	
	private static Logger logger = Logger.getLogger(DeviceControl.class);
	
	/************链路采集方式******************/
	public static final String CTRL_HAND_TRAN = "hand-tran";
	public static final String CTRL_REAL_TRAN = "real-tran";
	public static final String CTRL_OFFLINE_COLL = "offline-coll";
	public static final String CTRL_AUTO_START = "auto-start";
	public static final String CTRL_STOP = "stop";
	public static final String CTRL_HAND_LIST_ORI = "hand-list-ori";
	public static final String CTRL_HAND_LIST_DST = "hand-list-dst";
	public static final String CTRL_RENAME_DST = "rename-dst";
	public static final String CTRL_DELETE_DST = "delete-dst";
	public static final String DEV_DIST_RECALL = "dist_recall";

	public static CommandResult control(Command cmd) {
		String devType = cmd.getParam("dev_type");
		if (ParamsConstant.TYPE_COLL_LINK.equals(devType)) {
			return collLinkCtrl(cmd);
		} else if (ParamsConstant.TYPE_DIST_LINK.equals(devType)) {
			return distLinkCtrl(cmd);
		} else {
			return new CommandResult(cmd, false, "not found dev_type, current dev_type:" + devType);
		}
	}

	/**
	 * 采集链路处理业务
	 * 
	 * @param cmd
	 * @return
	 * @throws Exception 
	 */
	private static CommandResult collLinkCtrl(Command cmd){
		logger.debug("begin collLinkCtrl, cmd:" + (cmd == null ? "null" : cmd.toString()));
		String ctrl = cmd.getParam("ctrl");
		try {
			//自动采集
			if (CTRL_AUTO_START.equals(ctrl)) {
				String linkIds = cmd.getParam("dev_list");
				Vector<String> linkIdList = StringTool.tokenStringChar(linkIds, "|");
				
				for (int i=0; i<linkIdList.size(); i++) {
					String devId = linkIdList.get(i);
					
					//启动采集并且将链路状态修改为运行状态(1为运行状态)
					updateCollLinkStateLevel(devId, ParamsConstant.LINK_TIPS_LEVEL_0, ParamsConstant.COLL_LINK_RUN_STATE_RUN, null);
				}
				return new CommandResult(cmd, true, ClientResultEnum.AUTO_COLL_OK.getRstCode());
			} 
			
			//手动采集文件
			else if (CTRL_HAND_TRAN.equals(ctrl)) {
				//获取链路详细信息(链路属性、链路参数)
				String devId = cmd.getParam("dev_id");
				CollLink link = new CollLink(devId);
				
				//启动手动采集Job
				Map<String, Object> busParams = new HashMap<String, Object>();
				//手动采集链路ID
				busParams.put("devId", devId);
				//手动采集任务类型
				busParams.put("task_type", ParamsConstant.TASK_TYPE_HAND_COLL);
				//手动采集文件列表
				busParams.put("file_list", cmd.getParam("file_list"));
				//是否启用采集过滤条件
				busParams.put("enable_filter", cmd.getParam("enable_filter"));
				
				//判断当前采集任务状态是否正在执行,如果没有正在执行则立即执行一次
				boolean isRunning = SpringUtil.getQuartzManager().judgeJobIsRunning(link.getCollLinkDto().getDevName());
				logger.debug("hand collect, running status: " + isRunning + ", devId: " + devId);
				if (isRunning) {
					return new CommandResult(cmd, true, ClientResultEnum.HAND_COLL_TIPS.getRstCode());
				} else {
					SpringUtil.getQuartzManager().addJobStartNow(link.getCollLinkDto().getDevName(), ParamsConstant.PARAMS_JOB_COLL_CLZ, QuartzConstant.BUS_PARAMS, busParams);
				}
				return new CommandResult(cmd, true, ClientResultEnum.HAND_COLL_OK.getRstCode());
			} 
			
			//立即采集
			else if (CTRL_REAL_TRAN.equals(ctrl)) {
				String linkIds = cmd.getParam("dev_list");
				Vector<String> linkIdList = StringTool.tokenStringChar(linkIds, "|");
				for (int i=0; i<linkIdList.size(); i++) {
					//获取链路详细信息(链路属性、链路参数)
					String devId = linkIdList.get(i);
					CollLink link = new CollLink(devId);
					
					//启动自动采集Job, 只采集一次
					Map<String, Object> busParams = new HashMap<String, Object>();
					busParams.put("devId", devId);
					busParams.put("task_type", ParamsConstant.TASK_TYPE_REAL_COLL);
					
					//判断当前采集任务状态是否正在执行,如果没有正在执行则立即执行一次
					boolean isRunning = SpringUtil.getQuartzManager().judgeJobIsRunning(link.getCollLinkDto().getDevName());
					logger.debug("real collect, running status: " + isRunning + ", devId: " + devId);
					if (isRunning) {
						return new CommandResult(cmd, true, ClientResultEnum.REAL_COLL_TIPS.getRstCode());
					} else {
						SpringUtil.getQuartzManager().addJobStartNow(link.getCollLinkDto().getDevName(), ParamsConstant.PARAMS_JOB_COLL_CLZ, QuartzConstant.BUS_PARAMS, busParams);
					}
				}
				return new CommandResult(cmd, true, ClientResultEnum.REAL_COLL_OK.getRstCode());
			} 
			
			//停止采集链路
			else if (CTRL_STOP.equals(ctrl)) {
				String dev_list = cmd.getParam("dev_list");
				Vector<String> devList = StringTool.tokenStringChar(dev_list, "|");
				for (int i=0; i<devList.size(); i++) {
					//获取采集链路信息
					String devId = devList.get(i);
					CollLink link = new CollLink(devId);
					
					//停止采集Job
					SpringUtil.getQuartzManager().removeJob(link.getCollLinkDto().getDevName(), QuartzConstant.JOB_GROUP_NAME);
					logger.debug("stop collect, remove job: " + link.getCollLinkDto().getDevName() + ", devId: " + devId);
					//停止采集并且将链路状态修改为运行状态(0为运行状态)
					updateCollLinkStateLevel(devId, null, ParamsConstant.COLL_LINK_RUN_STATE_STOP, null);
				}
				return new CommandResult(cmd, true, ClientResultEnum.STOP_COLL_OK.getRstCode());
			} 
			
			//获取远程主机链路对应文件列表
			else if (CTRL_HAND_LIST_ORI.equals(ctrl)) {
				String devId = cmd.getParam("dev_id");
				
				//获取链路详细信息(链路属性、链路参数)
				CollLink link = new CollLink(devId);
				
				//获取客户端查询参数
				String filter_cdt = cmd.getParam("filter_cdt");
				//文件是否已经上传(true/false/all)
				String isColed_flag = cmd.getParam("isColed");
				//开始时间
				String beginTime = cmd.getParam("begin_time");
				//结束时间
				String endTime = cmd.getParam("end_time");
				
				//文件列表过滤条件对象
				Condition cdt = null;
				if (!BlankUtil.isBlank(filter_cdt)) {
					cdt = new Condition(filter_cdt);
				}
				//获取远程目录文件列表
				Vector<Object> list = link.handListOri(cdt, isColed_flag, beginTime, endTime);
				return new CommandResult(cmd, list);
			} 

	        //获取手动采集到本地文件的文件列表
			else if (CTRL_HAND_LIST_DST.equals(ctrl)) {
				String devId = cmd.getParam("dev_id");
				//获取链路详细信息(链路属性、链路参数)
				CollLink link = new CollLink(devId);
				
				//客户端查询参数
				String filter_cdt = cmd.getParam("filter_cdt");
				Condition cdt = null;
				if (!BlankUtil.isBlank(filter_cdt)) {
					cdt = new Condition(filter_cdt);
				}
				//获取采集目标目录文件列表(即采集程序本地文件列表)
				Vector<Object> list = link.handListDst(cdt);
				return new CommandResult(cmd, list);
			}

			//openapi调用离线采集命令
			else if (CTRL_OFFLINE_COLL.equals(ctrl)) {
				String devId = cmd.getParam("dev_id");
				//获取链路详细信息(链路属性、链路参数)
				CollLink link = new CollLink(devId);

				//采集类型
				link.taskType = ParamsConstant.TASK_TYPE_OFFLINE_COLL;

				//获取采集目标目录文件列表(即采集程序本地文件列表)
				OfflineCollectResp resp = link.offlineCollect();
				return new CommandResult(cmd, true, JSONObject.toJSONString(resp));
			}

			//未知的命令
			else {
				return new CommandResult(cmd, false, "not found cmd ctrl. current ctrl:" + ctrl);
			}
		} catch (DcmException e) {
			e.printStackTrace();
			logger.error("client operator fail, command: " + cmd , e);
			return new CommandResult(cmd, false, "operator fail, exception info:" + e.getErrorMsg());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("client operator fail, command: " + cmd , e);
			return new CommandResult(cmd, false, "operator fail, exception info:" + e.getMessage());
		}
	}
	
	/**
	 * 分发链路处理业务(分发链路运行状态: 0:停止, 1:自动分发     2:分发异常)
	 * @param cmd
	 * @return
	 */
	private static CommandResult distLinkCtrl(Command cmd) {
		logger.debug("begin distLinkCtrl, cmd:" + (cmd == null ? "null" : cmd.toString()));
		try {
			//分发类型
			String ctrl = cmd.getParam("ctrl");
			
			//手动分发获取源目录文件列表(本地文件列表)
			if (CTRL_HAND_LIST_ORI.equals(ctrl)) {
				//获取链路详细信息(链路属性、链路参数)
				String devId = cmd.getParam("dev_id");
				DistLink link = new DistLink(devId);
				
				//获取客户端查询参数
				String filter_cdt = cmd.getParam("filter_cdt");
				//是否上传(对应的值:true/false/all)
				String isColed_flag = cmd.getParam("isColed");
				//开始时间
				String beginTime = cmd.getParam("begin_time");
				//结束时间
				String endTime = cmd.getParam("end_time");
				
				//创建过滤条件对象
				Condition cdt = null;
				if (!BlankUtil.isBlank(filter_cdt)) {
					cdt = new Condition(filter_cdt);
				}
				
				//获取远程目录文件列表
				Vector<Object> list = link.handListOri(cdt, isColed_flag, beginTime, endTime);
				return new CommandResult(cmd, list);
			} 
			
			//手动分发获取目标目录文件列表(远程主机文件列表)
			else if (CTRL_HAND_LIST_DST.equals(ctrl)) {
				//获取链路详细信息(链路属性、链路参数)
				String devId = cmd.getParam("dev_id");
				DistLink link = new DistLink(devId);
				
				//获取客户端查询参数
				String filter_cdt = cmd.getParam("filter_cdt");
				
				//创建过滤条件对象
				Condition cdt = null;
				if (!BlankUtil.isBlank(filter_cdt)) {
					cdt = new Condition(filter_cdt);
				}
				//获取远程目录文件列表
				Vector<Object> list = link.handListDst(cdt);
				return new CommandResult(cmd, list);
			} 
			
			//手动分发，文件传输
			else if (CTRL_HAND_TRAN.equals(ctrl)) {
				//获取链路详细信息(链路属性、链路参数)
				String devId = cmd.getParam("dev_id");
				
				//Job业务参数对象
				Map<String, Object> busParams = new HashMap<String, Object>();
				//分发链路ID
				busParams.put("devId", devId);
				//分发类型
				busParams.put("task_type", ParamsConstant.TASK_TYPE_HAND_DIST);
				//手动分发文件列表
				busParams.put("file_list", cmd.getParam("file_list"));
				//是否启用过滤条件
				busParams.put("enable_filter", Boolean.parseBoolean(cmd.getParam("enable_filter")));
				
				//启动分发线程
				Boolean isExists = Boolean.FALSE;
				if (!BlankUtil.isBlank(DistRefreshLinkThrd.rstList)) {
					List<Map<String, Object>> rstList = DistRefreshLinkThrd.rstList;
					for (int i=0; i<rstList.size(); i++) {
						String refreshDevId = StringTool.object2String(rstList.get(i).get("DEV_ID"));
						if (refreshDevId.equals(devId)) {
							isExists = Boolean.TRUE;
							break;
						}
					}
				}
				if (!isExists) {
					DistTask distTask = new DistTask(devId, ParamsConstant.TYPE_DIST_LINK, ParamsConstant.TASK_TYPE_HAND_DIST, busParams);
					distTask.setPriority(Thread.MAX_PRIORITY);
					distTask.setName(devId);
					distTask.start();
				} else {
					return new CommandResult(cmd, true, ClientResultEnum.HAND_DIST_TIPS.getRstCode());
				}
				return new CommandResult(cmd, true, ClientResultEnum.HAND_DIST_OK.getRstCode());
			} 
			
			//立即分发
//			else if (CTRL_REAL_TRAN.equals(ctrl)) {
//				String linkIds = cmd.getParam("dev_list");
//				Vector<String> linkIdList = StringTool.tokenStringChar(linkIds, "|");
//				for (int i=0; i<linkIdList.size(); i++) {
//					//获取链路详细信息(链路属性、链路参数)
//					String devId = linkIdList.get(i);
//					
//					//判断当前链路是否正在自动分发，如果正在自动分发则不能立即分发
//					Boolean isExists = Boolean.FALSE;
//					if (!BlankUtil.isBlank(DistRefreshLinkThrd.rstList)) {
//						List<Map<String, Object>> rstList = DistRefreshLinkThrd.rstList;
//						for (int j=0; j<rstList.size(); j++) {
//							String refreshDevId = StringTool.object2String(rstList.get(j).get("DEV_ID"));
//							if (refreshDevId.equals(devId)) {
//								isExists = Boolean.TRUE;
//								break;
//							}
//						}
//					}
//					if (!isExists) {
//						DistTask distTask = new DistTask(devId, ParamsConstant.TYPE_DIST_LINK, ParamsConstant.TASK_TYPE_REAL_DIST, null);
//						distTask.setPriority(Thread.MAX_PRIORITY);
//						distTask.setName(devId);
//						distTask.start();
//					} else {
//						return new CommandResult(cmd, true, ClientResultEnum.REAL_DIST_TIPS.getRstCode());
//					}
//				}
//				return new CommandResult(cmd, true, ClientResultEnum.REAL_DIST_OK.getRstCode());
//			} 
			
			//自动分发
			else if (CTRL_AUTO_START.equals(ctrl)) {
				String linkIds = cmd.getParam("dev_list");
				Vector<String> linkIdList = StringTool.tokenStringChar(linkIds, "|");
				for (int i=0; i<linkIdList.size(); i++) {
					String devId = linkIdList.get(i);
					//修改链路状态为自动分发状态
					updateDistLinkStateLevel(devId, null, ParamsConstant.DIST_LINK_RUN_STATE_RUN, null);
				}
				return new CommandResult(cmd, true, ClientResultEnum.AUTO_DIST_OK.getRstCode());
			} 
			
			//停止分发链路自动分发
			else if (CTRL_STOP.equals(ctrl)) {
				String dev_list = cmd.getParam("dev_list");
				Vector<String> devList = StringTool.tokenStringChar(dev_list, "|");
				for (int i=0; i<devList.size(); i++) {
					String devId = devList.get(i);
					//修改链路状态为停止状态
					updateDistLinkStateLevel(devId, null, ParamsConstant.DIST_LINK_RUN_STATE_STOP, null);
				}
				return new CommandResult(cmd, true, ClientResultEnum.STOP_DIST_OK.getRstCode());
			}
			
			//分发链路重分发
			else if (DEV_DIST_RECALL.equals(ctrl)) {
				try {
					String dev_list = cmd.getParam("dev_list");
					if(dev_list.trim().endsWith(",")){
						dev_list = dev_list.substring(0, dev_list.length()-1);
					}
					if (!BlankUtil.isBlank(dev_list)) {
						distRecall(dev_list);
					}
				} catch (Exception e) {
					logger.error("distribute recall fail.", e);
					throw e;
				}
				return new CommandResult(cmd, true, ClientResultEnum.EXCEPTION_DIST_OK.getRstCode());
			}
			
			//未知的命令
			else {
				return new CommandResult(cmd, false, "not found cmd ctrl. current ctrl:" + ctrl);
			}
		} catch (DcmException e) {
			e.printStackTrace();
			logger.error("client operator dist fail, command:" + cmd, e);
			return new CommandResult(cmd, false, "operator fail, exception info:" + e.getErrorMsg());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("client operator dist fail, command:" + cmd, e);
			return new CommandResult(cmd, false, "operator fail, exception info:" + e.getMessage());
		}
	}
	
	/**
	 * 修改链路运行状态
	 * @param devId  链路ID
	 * @param tipsLevel 链路提示级别
	 * @param runState  链路运行状态
	 */
	private static void updateCollLinkStateLevel(String devId, String tipsLevel, String runState, String linkError) {
		logger.debug("begin update collect link run state normal, tipsLevel: " + tipsLevel + ", runState: " + runState + ", linkError: " + linkError + ", devId: " + devId);
		Map<String, Object> updateParams = new HashMap<String, Object>();
		updateParams.put("DEV_ID", devId);
		updateParams.put("TIPS_LEVEL", BlankUtil.isBlank(StringTool.object2String(tipsLevel)) ? ParamsConstant.PARAMS_0 : StringTool.object2String(tipsLevel));
		updateParams.put("RUN_STATE", StringTool.object2String(runState));
		updateParams.put("LINK_ERR", StringTool.object2String(linkError));
		JdbcUtil.updateObject("collectMapper.updateCollLinkTipsLevel", updateParams, FrameConfigKey.DEFAULT_DATASOURCE);
		logger.debug("end update collect link run state normal, devId: " + devId);
	}
	
	/**
	 * 修改链路运行状态
	 * @param devId  链路ID
	 * @param tipsLevel 链路提示级别
	 * @param runState  链路运行状态
	 */
	private static void updateDistLinkStateLevel(String devId, String tipsLevel, String runState, String linkError) {
		logger.debug("begin update distribute link run state normal, tipsLevel: " + tipsLevel + ", runState: " + runState + ", linkError: " + linkError + ", devId: " + devId);
		Map<String, Object> updateParams = new HashMap<String, Object>();
		updateParams.put("DEV_ID", devId);
		updateParams.put("TIPS_LEVEL", BlankUtil.isBlank(StringTool.object2String(tipsLevel)) ? ParamsConstant.PARAMS_0 : StringTool.object2String(tipsLevel));
		updateParams.put("RUN_STATE", StringTool.object2String(runState));
		updateParams.put("LINK_ERR", StringTool.object2String(linkError));
		JdbcUtil.updateObject("distributeMapper.updateDistributeLinkTipsLevel", updateParams, FrameConfigKey.DEFAULT_DATASOURCE);
		logger.debug("end update distribute link run state normal, devId: " + devId);
	}
	
	/**
	 * 将分发异常数据重新导入到分发任务表
	 * 
	 * @param dev_list
	 * @throws Exception 
	 */
	private static void distRecall(String dev_list) throws Exception {
		logger.debug("begin distRecall, dev_list:" + dev_list);
		
		DataSourceTransactionManager transactionMgr = (DataSourceTransactionManager) SpringContextUtil.getBean("defaultTransactionManager");
		//设置事物级别为隔离级别
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus statuc = transactionMgr.getTransaction(definition);
		
		try {
			SqlSession sqlSession = SpringUtil.getCoreBaseDao().getSqlSession();
			DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("DEV_IDS", dev_list);

			List list = sqlSession.selectList("distributeMapper.queryDcDistTaskFromAbn",params);
			//将分发异常表数据拷贝到分发任务表
			sqlSession.insert("distributeMapper.addDcDistTaskFromAbn", list);
			logger.info("distribute link recall, add dist task ok, devIds: " + dev_list);
			
			//删除分发异常表数据
			sqlSession.delete("distributeMapper.delDcDistTaskAbnByIds", params);
			logger.info("distribute link recall, delete dist exception task ok, devIds: " + dev_list);
			
			transactionMgr.commit(statuc);
		} catch (Exception e) {
			logger.error("distRecall fail.", e);
			transactionMgr.rollback(statuc);
			throw e;
		}
		logger.debug("end distRecall.");
	}
}
