package com.tydic.service.clustermanager.impl;

import com.tydic.bean.DeployViewDTO;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.DeployViewService;
import com.tydic.util.BusinessConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.service.clustermanager.impl]    
  * @ClassName:    [DeployServiceImpl]     
  * @Description:  [查询业务部署视图]
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:17:24]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:17:24]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Service
@SuppressWarnings("all")
public class DeployViewServiceImpl implements DeployViewService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DeployViewServiceImpl.class);


	/**
	 * 核心service对象
	 */
	@Autowired
	private CoreService coreService;

	/**
	 * 查询业务部署视图数据
	 * @param params
	 * @param dbKey
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<DeployViewDTO> queryDeployViewList(Map<String, Object> params, String dbKey) throws Exception {
		log.info("查询业务部署视图，业务参数：" + params + ", dbKey: " + dbKey);

		String busClusterId = String.valueOf(params.get("BUS_CLUSTER_ID"));

		//返回对象
		List<DeployViewDTO> retList = new ArrayList<DeployViewDTO>();

		//查询业务主集群关联业务集群
		Map<String, Object> queryBusMap = new HashMap<String, Object>();
		queryBusMap.put("BUS_CLUSTER_ID", busClusterId);
		//查询业务程序类型
		List<HashMap<String, Object>> hostCntList = coreService.queryForList2New("deployView.queryDeployViewHostCount", queryBusMap, dbKey);
		if (CollectionUtils.isNotEmpty(hostCntList)) {
			for (HashMap<String, Object> deployMap : hostCntList) {
				DeployViewDTO viewDTO = new DeployViewDTO();
				viewDTO.setClusterId(String.valueOf(deployMap.get("CLUSTER_ID")));
				viewDTO.setClusterName(String.valueOf(deployMap.get("CLUSTER_NAME")));
				viewDTO.setClusterType(String.valueOf(deployMap.get("CLUSTER_TYPE")));
				viewDTO.setClusterFlag(String.valueOf(deployMap.get("TYPE")));
				viewDTO.setHostCount(Integer.valueOf(String.valueOf(deployMap.get("CNT"))));
				viewDTO.setHostList(String.valueOf(deployMap.get("HOST_LIST")));
				retList.add(viewDTO);
			}
		}

		//设置业务部署图数据
		this.getBusDeployViewData(retList, params, dbKey);
		log.debug("组装业务部署数据完成...");

		//设置组件部署图数据
		this.getComponentDeployViewData(retList, params, dbKey);
		log.debug("组装组件部署数据完成...");

		log.debug("部署图，返回结果： ");
		if (CollectionUtils.isNotEmpty(retList)) {
			for (DeployViewDTO viewDTO : retList) {
				System.out.println(viewDTO.toString());
				//log.debug("部署图程序----->" + viewDTO.toString());
			}
		}

		//查询业务主集群关联组件集群
		return retList;
	}

	/**
	 * 获取组件程序部署数据
	 * @param retList
	 * @param params
	 * @param dbKey
	 * @return
	 */
	private void getComponentDeployViewData(List<DeployViewDTO> retList, Map<String, Object> params, String dbKey) {
		//查询业务主集群关联业务集群
		Map<String, Object> queryBusMap = new HashMap<String, Object>();
		queryBusMap.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));

		List<HashMap<String, Object>> deployList = coreService.queryForList2New("deployView.queryComponentDeployViewListByClusterId", queryBusMap, dbKey);
		if (CollectionUtils.isNotEmpty(deployList)) {
			Map<String, Object> clusterMap = new HashMap<>();
			Map<String, Object> clusterInstMap = new HashMap<>();
			for (HashMap<String, Object> deployMap : deployList) {
				String subClusterId = String.valueOf(deployMap.get("CLUSTER_ID"));
				//设置集群实例数量以及每个集群对应的实例列表
				if (clusterMap.containsKey(subClusterId)) {
					clusterMap.put(subClusterId, Integer.valueOf(String.valueOf(clusterMap.get(subClusterId))) + 1);

					List<HashMap<String, Object>> clusterInstList = (List<HashMap<String, Object>>)clusterInstMap.get(subClusterId);
					clusterInstList.add(deployMap);
					clusterInstMap.put(subClusterId, clusterInstList);
				} else {
					clusterMap.put(subClusterId, BusinessConstant.PARAMS_BUS_1);
					List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String, Object>>();
					tempList.add(deployMap);
					clusterInstMap.put(subClusterId, tempList);
				}
			}

			for (DeployViewDTO viewDTO : retList) {
				Object instCount = clusterMap.get(viewDTO.getClusterId());
				Object instList = clusterInstMap.get(viewDTO.getClusterId());
				if (!BlankUtil.isBlank(instCount)) {
					viewDTO.setInstCount(Integer.valueOf(String.valueOf(instCount)));
				}
				if (!BlankUtil.isBlank(instList)) {
					List<HashMap<String, Object>> clusterInstList = (List<HashMap<String, Object>>)clusterInstMap.get(viewDTO.getClusterId());
					if (CollectionUtils.isNotEmpty(clusterInstList)) {
						Map<String, Object> compMap = new HashMap<String, Object>();
						Map<String, Object> compInstMap = new HashMap<>();
						for (HashMap<String, Object> instMap : clusterInstList) {
							String deployFileType = String.valueOf(instMap.get("DEPLOY_FILE_TYPE"));
							if (compMap.containsKey(deployFileType)) {
								int currCnt = Integer.valueOf(String.valueOf(compMap.get(deployFileType)));
								compMap.put(deployFileType, currCnt + 1);

								List<HashMap<String, Object>> instTaskList = (List<HashMap<String, Object>>)compInstMap.get(deployFileType);
								instTaskList.add(instMap);
								compInstMap.put(deployFileType, instTaskList);
							} else {
								compMap.put(deployFileType, BusinessConstant.PARAMS_BUS_1);
								List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String, Object>>();
								tempList.add(instMap);
								compInstMap.put(deployFileType, tempList);
							}
						}

						List<DeployViewDTO.DeploySubProgramDTO> subProgramList = viewDTO.getSubProgramList();

						//设置程序实例个数
						Iterator<String> keyIteratorList = compMap.keySet().iterator();
						while(keyIteratorList.hasNext()) {
							String deployFileType = keyIteratorList.next();
							String instCnt = String.valueOf(compMap.get(deployFileType));
							DeployViewDTO.DeploySubProgramDTO subProgramDTO = viewDTO.new DeploySubProgramDTO();
							subProgramDTO.setName(deployFileType);
							subProgramDTO.setInstCount(Integer.valueOf(instCnt));
							subProgramList.add(subProgramDTO);
						}

						//设置程序实例列表
						Iterator<String> instIteratorList = compInstMap.keySet().iterator();
						while(instIteratorList.hasNext()) {
							String deployFileType = instIteratorList.next();
							Object taskInstObj = compInstMap.get(deployFileType);
							if (!BlankUtil.isBlank(taskInstObj)) {
								List<HashMap<String, Object>> taskInstList = (List<HashMap<String, Object>>) taskInstObj;
								for(DeployViewDTO.DeploySubProgramDTO subProgramDTO : subProgramList) {
									if (StringUtils.equals(deployFileType, subProgramDTO.getName())) {
										subProgramDTO.setInstList(taskInstList);
									}
								}
							}
						}
						viewDTO.setSubProgramList(subProgramList);
					}
				}
			}
		}
	}

	/**
	 * 查询业务程序部署数据
	 * @param retList
	 * @param params
	 * @param dbKey
	 * @return
	 */
	private void getBusDeployViewData(List<DeployViewDTO> retList, Map<String, Object> params, String dbKey) {
		//查询业务主集群关联业务集群
		Map<String, Object> queryBusMap = new HashMap<String, Object>();
		queryBusMap.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));

		List<HashMap<String, Object>> deployList = coreService.queryForList2New("deployView.queryBusDeployViewListByClusterId", queryBusMap, dbKey);
		if (CollectionUtils.isNotEmpty(deployList)) {
			Map<String, Object> clusterMap = new HashMap<>();
			Map<String, Object> clusterInstMap = new HashMap<>();
			for (HashMap<String, Object> deployMap : deployList) {
				String subClusterId = String.valueOf(deployMap.get("CLUSTER_ID"));
				//设置集群实例数量以及每个集群对应的实例列表
				if (clusterMap.containsKey(subClusterId)) {
					clusterMap.put(subClusterId, Integer.valueOf(String.valueOf(clusterMap.get(subClusterId))) + 1);
					List<HashMap<String, Object>> clusterInstList = (List<HashMap<String, Object>>)clusterInstMap.get(subClusterId);
					clusterInstList.add(deployMap);
					clusterInstMap.put(subClusterId, clusterInstList);
				} else {
					clusterMap.put(subClusterId, BusinessConstant.PARAMS_BUS_1);
					List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String, Object>>();
					tempList.add(deployMap);
					clusterInstMap.put(subClusterId, tempList);
				}
			}

			for (DeployViewDTO viewDTO : retList) {
				Object instCount = clusterMap.get(viewDTO.getClusterId());
				Object instList = clusterInstMap.get(viewDTO.getClusterId());
				if (!BlankUtil.isBlank(instCount)) {
					viewDTO.setInstCount(Integer.valueOf(String.valueOf(instCount)));
				}
				if (!BlankUtil.isBlank(instList)) {
					List<HashMap<String, Object>> clusterInstList = (List<HashMap<String, Object>>)clusterInstMap.get(viewDTO.getClusterId());
					if (CollectionUtils.isNotEmpty(clusterInstList)) {
						Map<String, Object> programMap = new HashMap<String, Object>();
						Map<String, Object> programInstMap = new HashMap<>();
						for (HashMap<String, Object> instMap : clusterInstList) {
							String programCode = String.valueOf(instMap.get("PROGRAM_CODE"));
							String taskProgramId = String.valueOf(instMap.get("ID"));
							if (programMap.containsKey(programCode)) {
								int currCnt = Integer.valueOf(String.valueOf(programMap.get(programCode)));
								programMap.put(programCode,  currCnt + 1);
								List<HashMap<String, Object>> instTaskList = (List<HashMap<String, Object>>)programInstMap.get(programCode);
								instTaskList.add(instMap);
								programInstMap.put(programCode, instTaskList);
							} else {
								programMap.put(programCode, BusinessConstant.PARAMS_BUS_1);
								List<HashMap<String,Object>> tempList = new ArrayList<HashMap<String, Object>>();
								tempList.add(instMap);
								programInstMap.put(programCode, tempList);
							}
						}

						List<DeployViewDTO.DeploySubProgramDTO> subProgramList = viewDTO.getSubProgramList();

						//设置程序实例个数
						Iterator<String> keyIteratorList = programMap.keySet().iterator();
						while(keyIteratorList.hasNext()) {
							String programCode = keyIteratorList.next();
							String instCnt = String.valueOf(programMap.get(programCode));
							DeployViewDTO.DeploySubProgramDTO subProgramDTO = viewDTO.new DeploySubProgramDTO();
							subProgramDTO.setName(programCode);
							subProgramDTO.setInstCount(Integer.valueOf(instCnt));
							subProgramList.add(subProgramDTO);
						}

						//设置程序实例列表
						Iterator<String> instIteratorList = programInstMap.keySet().iterator();
						while(instIteratorList.hasNext()) {
							String programCode = instIteratorList.next();
							Object taskInstObj = programInstMap.get(programCode);
							if (!BlankUtil.isBlank(taskInstObj)) {
								List<HashMap<String, Object>> taskInstList = (List<HashMap<String, Object>>) taskInstObj;
								for(DeployViewDTO.DeploySubProgramDTO subProgramDTO : subProgramList) {
									if (StringUtils.equals(programCode, subProgramDTO.getName())) {
										subProgramDTO.setInstList(taskInstList);
									}
								}
							}
						}
						viewDTO.setSubProgramList(subProgramList);
					}
				}
			}
		}
	}
}
