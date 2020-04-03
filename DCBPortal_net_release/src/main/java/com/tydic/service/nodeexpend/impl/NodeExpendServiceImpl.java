package com.tydic.service.nodeexpend.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.QuartzConstant;
import com.tydic.bp.QuartzManager;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.nodeexpend.NodeExpendService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.StringTool;

@Service
public class NodeExpendServiceImpl implements NodeExpendService {

	private static Logger logger = Logger.getLogger(NodeExpendServiceImpl.class);

	/**
	 * 核心Service对象
	 */
	@Resource
	private CoreService coreService;
	
	@Resource
	private QuartzManager quartzManager;

	/**
	 * 查询实例状态记录
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @return List
	 */
	@Override
	public List<HashMap<String, Object>> queryClusterTreeList(Map<String, Object> params, String dbKey)
			throws Exception {
		logger.debug("查询实例配置Tree数据， 业务参数: " + params + ", dbKey: " + dbKey);
		List<HashMap<String, Object>> clusterList = new ArrayList<HashMap<String, Object>>();

		// 查询所有的Jstorm运行集群
		List<HashMap<String, Object>> taskList = coreService.queryForList2New("taskProgram.queryTaskProgramRunTopology",
				params, dbKey);
		logger.debug("Topology实例， 查询结果: " + (taskList == null ? 0 : taskList.size()));

		if (!BlankUtil.isBlank(taskList)) {

			List<HashMap<String, Object>> clusterProgramList = new ArrayList<HashMap<String, Object>>();
			// 集群（第一层）
			HashSet<String> clusterSet = new HashSet<String>();
			for (HashMap<String, Object> clusterMap : taskList) {
				String clusterId = StringTool.object2String(clusterMap.get("CLUSTER_ID"));
				if (clusterSet.size() == 0 || clusterSet.isEmpty() || !clusterSet.contains(clusterId)) {
					HashMap<String, Object> addClusterMap = new HashMap<>();
					addClusterMap.put("NODE_ID", UUID.randomUUID().toString());
					addClusterMap.put("PARENT_NODE_ID", "");
					addClusterMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_1);
					addClusterMap.put("NODE_NAME", clusterMap.get("CLUSTER_NAME"));
					addClusterMap.put("CLUSTER_ID", clusterMap.get("CLUSTER_ID"));
					addClusterMap.put("CLUSTER_NAME", clusterMap.get("CLUSTER_NAME"));
					addClusterMap.put("CLUSTER_CODE", clusterMap.get("CLUSTER_CODE"));
					clusterSet.add(clusterId);
					clusterProgramList.add(addClusterMap);
					clusterList.add(addClusterMap);
				}
			}

			// 版本（第二层）
			for (HashMap<String, Object> clusterMap : clusterProgramList) {
				String clusterId = StringTool.object2String(clusterMap.get("CLUSTER_ID"));
				String nodeId = StringTool.object2String(clusterMap.get("NODE_ID"));
				List<HashMap<String, Object>> versionList = new ArrayList<>();
				HashSet<String> versionSet = new HashSet<String>();
				for (HashMap<String, Object> versionMap : taskList) {
					String versionClusterId = StringTool.object2String(versionMap.get("CLUSTER_ID"));
					if (clusterId.equals(versionClusterId)) {
						String taskId = StringTool.object2String(versionMap.get("TASK_ID"));
						if (versionSet.size() == 0 || versionSet.isEmpty() || !versionSet.contains(taskId)) {
							HashMap<String, Object> addClusterMap = new HashMap<>();
							addClusterMap.put("NODE_ID", UUID.randomUUID().toString());
							addClusterMap.put("PARENT_NODE_ID", nodeId);
							addClusterMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_2);
							addClusterMap.put("NODE_NAME", versionMap.get("VERSION"));
							addClusterMap.put("CLUSTER_ID", versionMap.get("CLUSTER_ID"));
							addClusterMap.put("CLUSTER_NAME", versionMap.get("CLUSTER_NAME"));
							addClusterMap.put("CLUSTER_CODE", versionMap.get("CLUSTER_CODE"));
							addClusterMap.put("TASK_ID", versionMap.get("TASK_ID"));
							versionSet.add(taskId);
							versionList.add(addClusterMap);
							clusterList.add(addClusterMap);
						}
					}
				}

				// Topology（第三层）
				if (!BlankUtil.isBlank(versionList)) {
					for (HashMap<String, Object> versionMap : versionList) {
						String versionNodeId = StringTool.object2String(versionMap.get("NODE_ID"));
						String versionTaskId = StringTool.object2String(versionMap.get("TASK_ID"));
						String versionClusterId = StringTool.object2String(versionMap.get("CLUSTER_ID"));
						for (HashMap<String, Object> programMap : taskList) {
							String programTaskId = StringTool.object2String(programMap.get("TASK_ID"));
							String programClusterId = StringTool.object2String(programMap.get("CLUSTER_ID"));
							if (versionTaskId.equals(programTaskId) && versionClusterId.equals(programClusterId)) {
								HashMap<String, Object> addProgramMap = new HashMap<>();
								addProgramMap.put("NODE_ID", UUID.randomUUID().toString());
								addProgramMap.put("PARENT_NODE_ID", versionNodeId);
								addProgramMap.put("NODE_LEVEL", BusinessConstant.PARAMS_BUS_3);
								addProgramMap.put("NODE_NAME", programMap.get("PROGRAM_NAME"));
								addProgramMap.put("CLUSTER_ID", versionMap.get("CLUSTER_ID"));
								addProgramMap.put("CLUSTER_NAME", versionMap.get("CLUSTER_NAME"));
								addProgramMap.put("CLUSTER_CODE", versionMap.get("CLUSTER_CODE"));
								addProgramMap.put("TASK_ID", versionMap.get("TASK_ID"));
								addProgramMap.put("TASK_PROGRAM_ID", programMap.get("TASK_PROGRAM_ID"));
								clusterList.add(addProgramMap);
							}
						}
					}
				}
			}
		}
		logger.debug("查询实例配置Tree数据结束, 返回记录长度: " + clusterList.size());
		return clusterList;
	}

	/**
	 * 新增阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> addThresholdConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		logger.debug("弹性伸缩扩展配置， 新增阀值配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.insertObject2New("expendStrategyConfig.addExpendStrategyConfig", params, dbKey);
		} catch (Exception e) {
			logger.error("新增阀值配置失败， 失败原因: ", e);
			throw new RuntimeException("新增阀值配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("新增阀值配置成功...");
		return new HashMap<>();
	}

	/**
	 * 修改阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> updateThresholdConfig(Map<String, Object> params, String dbKey,
			HttpServletRequest request) throws Exception {
		logger.debug("弹性伸缩扩展配置， 修改阀值配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.updateObject2New("expendStrategyConfig.updateExpendStrategyConfig", params, dbKey);
		} catch (Exception e) {
			logger.error("修改阀值配置失败， 失败原因: ", e);
			throw new RuntimeException("修改阀值配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("修改阀值配置成功...");
		return new HashMap<>();
	}

	/**
	 * 删除阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> delThresholdConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		logger.debug("弹性伸缩扩展配置， 删除阀值配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.deleteObject2New("expendStrategyConfig.delExpendStrategyConfig", params, dbKey);
			coreService.deleteObject2New("expendStrategyConfig.delExpendJobBus", params, dbKey);
		} catch (Exception e) {
			logger.error("删除阀值配置失败， 失败原因: ", e);
			throw new RuntimeException("删除阀值配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("删除阀值配置成功...");
		return new HashMap<>();
	}

	/**
	 * 新增阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> addTimingConfig(List<Map<String, String>> params, String dbKey,
			HttpServletRequest request) throws Exception {
		logger.debug("弹性定时扩展配置， 新增定时配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.insertObject("expendStrategyConfig.addExpendStrategyConfig", params, dbKey);

			for (Map<String, String> param : params) {
				param.put("TASK_ID", param.get("JOB_ID"));
				param.put("BUS_ID", param.get("STRATEGY_ID"));
				param.put("BUS_TYPE","1");
			}
			coreService.insertObject("jobTaskBus.insert", params, dbKey);
		} catch (Exception e) {
			logger.error("新增定时配置失败， 失败原因: ", e);
			throw new RuntimeException("新增定时配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("新增定时配置成功...");
		return new HashMap<>();
	}

	/**
	 * 新增手动配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> addManualConfig(List<Map<String, String>> params, String dbKey,
			HttpServletRequest request) throws Exception {
		logger.debug(" 新增手动配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.insertObject("expendStrategyConfig.addExpendStrategyConfig", params, dbKey);

		} catch (Exception e) {
			logger.error("新增手动配置失败， 失败原因: ", e);
			throw new RuntimeException("新增手动配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("新增手动配置成功...");
		return new HashMap<>();
	}

	/**
	 * 修改阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> updateTimingConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		logger.debug("定时配置， 修改定时配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.updateObject2New("expendStrategyConfig.updateExpendTimingConfig", params, dbKey);
			Map<String, String> param = new HashMap<String,String>();
			param.put("TASK_ID", StringTool.object2String(params.get("CRON_EXP")));
			param.put("BUS_ID", StringTool.object2String(params.get("STRATEGY_ID")));
			param.put("BUS_TYPE","1");
			coreService.deleteObject2New("expendStrategyConfig.delExpendJobBus", params, dbKey);
			coreService.insertObject("jobTaskBus.insert", param, dbKey);
			
		} catch (Exception e) {
			logger.error("修改定时配置失败， 失败原因: ", e);
			throw new RuntimeException("修改定时配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("修改定时配置成功...");
		return new HashMap<>();
	}

	/**
	 * 修改阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> updateManualConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		logger.debug("定时配置， 修改定时配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.updateObject2New("expendStrategyConfig.updateExpendManualConfig", params, dbKey);
		} catch (Exception e) {
			logger.error("修改定时配置失败， 失败原因: ", e);
			throw new RuntimeException("修改定时配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("修改定时配置成功...");
		return new HashMap<>();
	}

	/**
	 * 删除阀值配置
	 * 
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @param request
	 */
	@Override
	public Map<String, Object> delTimingConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		logger.debug("弹性伸缩扩展配置， 删除阀值配置， 业务参数: " + params.toString() + "， dbKey: " + dbKey);
		try {
			coreService.deleteObject2New("expendStrategyConfig.delExpendStrategyConfig", params, dbKey);
		} catch (Exception e) {
			logger.error("删除阀值配置失败， 失败原因: ", e);
			throw new RuntimeException("删除阀值配置失败， 失败原因: " + e.getMessage());
		}
		logger.debug("删除阀值配置成功...");
		return new HashMap<>();
	}

	@Override
	public Map<String, Object> addExecConfig(Map<String, Object> params, String dbKey, HttpServletRequest request)
			throws Exception {
		try {
			quartzManager.addJobStartNow( UUID.randomUUID().toString(), "com.tydic.quartz.ClusterNodeManageQuartz", QuartzConstant.BUS_PARAMS,
					params);

		} catch (Exception e) {
			logger.error("addExecConfig, 失败 ---> ", e);
			throw new RuntimeException("执行失败， 失败原因: " + e.getMessage());
		}
		return new HashMap<>();
	}
	
	
	@Override
	public Map<String, Object> updateNodeexpendJob(Map<String, Object> params, String dbKey)
			throws Exception {
		try {
			coreService.updateObject2New("expendStrategyConfig.updateExpendManualConfig", params, dbKey);
			 params.put("ACTION_TYPE","1");
			 params.put("HOST_IPS",params.get("BACKUP_HOSTS"));
			 params.put("EXEC_STATUS","1");
			coreService.updateObject2New("expansionReport.updateStatusById", params, dbKey);
			params.put("BUS_ID", params.get("STRATEGY_ID"));
			params.put("BUS_TYPE", "5");
			params.put("EXTENDED_FIELD", params.get("ID"));
			quartzManager.addJobStartNow( UUID.randomUUID().toString(), "com.tydic.quartz.ClusterNodeManageQuartz", QuartzConstant.BUS_PARAMS,
					params);

		} catch (Exception e) {
			logger.error("addExecConfig, 失败 ---> ", e);
			throw new RuntimeException("执行失败， 失败原因: " + e.getMessage());
		}
		return new HashMap<>();
	}
	
	@Override
	public Map<String, Object> updateExecImmJob(Map<String, Object> params, String dbKey)
			throws Exception {
		try {
			   coreService.updateObject2New("expendStrategyConfig.updateExpendManualConfig", params, dbKey);
		 
				params.put("TASK_ID", params.get("JOB_ID"));
				params.put("BUS_ID", params.get("STRATEGY_ID"));
				params.put("BUS_TYPE","5");
				params.put("EXTENDED_FIELD", params.get("ID"));
			  coreService.insertObject2New("jobTaskBus.insert", params, dbKey);
			    params.put("ACTION_TYPE","2");
			    params.put("HOST_IPS",params.get("BACKUP_HOSTS"));
			    params.put("EXEC_STATUS","1");
			  coreService.updateObject2New("expansionReport.updateStatusById", params, dbKey);

		} catch (Exception e) {
			logger.error("addExecConfig, 失败 ---> ", e);
			throw new RuntimeException("执行失败， 失败原因: " + e.getMessage());
		}
		return new HashMap<>();
	}
}
