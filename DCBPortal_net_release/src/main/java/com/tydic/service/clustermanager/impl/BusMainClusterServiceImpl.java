package com.tydic.service.clustermanager.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.bp.core.utils.db.CoreBaseDao;
import com.tydic.bp.core.utils.db.DbContextHolder;
import com.tydic.bp.core.utils.properties.SystemProperty;
import com.tydic.bp.core.utils.tools.SpringContextUtil;
import com.tydic.service.clustermanager.BusMainClusterService;
import com.tydic.util.BusinessConstant;
import com.tydic.util.MapConverter;
import com.tydic.util.StringTool;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.*;

/**
 * Simple to Introduction
 *
 * @ProjectName: [DCBPortal_net_release]
 * @Package: [com.tydic.service.clustermanager.impl]
 * @ClassName: [BusMainClusterServiceImpl]
 * @Description: [业务主集群管理类]
 * @Author: [Yuanh]
 * @CreateDate: [2017-6-14 上午8:54:25]
 * @UpdateUser: [Yuanh]
 * @UpdateDate: [2017-6-14 上午8:54:25]
 * @UpdateRemark: [说明本次修改内容]
 * @Version: [v1.0]
 */
@Service
@SuppressWarnings("all")
public class BusMainClusterServiceImpl implements BusMainClusterService {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(BusMainClusterServiceImpl.class);

	@Resource
	public CoreService coreService;

	/**
	 * 删除集群信息
	 *
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @return Map
	 */
	@Override
	public Map<String, Object> deleteBusMainCluster(List<Map<String, String>> list, String dbKey) throws Exception {
		log.debug("删除集群信息， 业务参数: " + list.toString() + ", dbKey: " + dbKey);

		// 返回对象
		Map<String, Object> rstMap = new HashMap<String, Object>();
		Map<String, Object> parameter = new HashMap<String, Object>();
		
		DataSourceTransactionManager transactionMgr = (DataSourceTransactionManager) SpringContextUtil.getBean("defaultTransactionManager");
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);//共用一个事务管理
		TransactionStatus status = transactionMgr.getTransaction(definition);
		// 获取数据库连接对象
		CoreBaseDao coreBaseDao = (CoreBaseDao) SpringContextUtil.getBean("coreBaseDao");
		SqlSession sqlSession = coreBaseDao.getSqlSession();
		// 设置上下文数据源
		DbContextHolder.setDbType(FrameConfigKey.DEFAULT_DATASOURCE);
		try {
			sqlSession.delete("serviceType.deleteBusCluster",list);
			log.debug("删除业务主集群子集群信息成功...");
			
			sqlSession.delete("busRelationClusterList.delBusRelationClusterList",list);
			log.debug("删除业务主集群关联集群成功...");
			
			sqlSession.delete("busMainCluster.delBusMainCluster",list);
			log.debug("删除业务集群信息成功...");
			
			if (list != null && list.size() > 0) {
				String busClusterId = list.get(0).get("BUS_CLUSTER_ID");
				Map<String, Object> delParams = new HashMap<String, Object>();
				delParams.put("BUS_CLUSTER_ID", busClusterId);
				sqlSession.delete("environments.delEnvByBusClusterId", delParams);
				log.debug("删除业务集群关联环境配置信息成功...");
			}
			transactionMgr.commit(status);
		} catch (Exception e) {
			transactionMgr.rollback(status);
			log.error("删除业务主集群信息失败， 失败信息: ", e);
			throw new RuntimeException("删除业务主集群信息失败！");
		}
		rstMap.put("RST_CODE", BusinessConstant.PARAMS_DO_RST_SUCCESS);
		return rstMap;
	}

	/**
     * 增加集群信息
     *
     * @param params 业务参数
     * @param dbKey  数据库Key
     * @return Map
     */
    @Override
    public Map<String, Object> insertBusMainCluster(Map<String, Object> params, String dbKey) throws Exception {
    	log.debug("新增集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String,Object> resultMap = new HashMap<String,Object>();

		try {
			//保存主业务进群信息
			String BUS_CLUSTER_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
			Map<String,String> addParam = new HashMap<String,String>();
			addParam.put("BUS_CLUSTER_ID", BUS_CLUSTER_ID);
			addParam.put("BUS_CLUSTER_NAME", ObjectUtils.toString(params.get("BUS_CLUSTER_NAME")));
			addParam.put("BUS_CLUSTER_CODE", ObjectUtils.toString(params.get("BUS_CLUSTER_CODE")));
			addParam.put("BUS_CLUSTER_TYPE", ObjectUtils.toString(params.get("BUS_CLUSTER_TYPE")));
			addParam.put("BUS_CLUSTER_SEQ", ObjectUtils.toString(params.get("BUS_CLUSTER_SEQ")));
			coreService.insertObject("busMainCluster.addBusMainCluster", addParam, dbKey);
			
			List<Map<String,Object>> businessList = (List<Map<String, Object>>) params.get("BUSINESS_LIST");
			List<Map<String,String>> addList = new ArrayList<Map<String,String>>();
			if(CollectionUtils.isNotEmpty(businessList)){
				log.info("需添加的业务集群:" + ObjectUtils.toString(businessList));
				for(int i=0;i<businessList.size();++i){
					Map<String,Object> param = businessList.get(i);
					String CLUSTER_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
					param.put("BUS_CLUSTER_ID", BUS_CLUSTER_ID);
					param.put("CLUSTER_ID", CLUSTER_ID);
					
					Map<String,String> tempMap = MapConverter.convert(param);
					addList.add(tempMap);
				}
				coreService.insertBatchObject("busRelationClusterList.batchInsert", addList,dbKey);
				coreService.insertBatchObject("serviceType.batchInsertDcfServiceType", addList,dbKey);
			} else {
				log.info("需要添加的业务集群为空");
			}
			
			List<Map<String,Object>> componentList = (List<Map<String, Object>>) params.get("COMPONENT_LIST");
			if(CollectionUtils.isNotEmpty(componentList)){
				addList.clear();
				log.debug("需要添加的组件集群:" + ObjectUtils.toString(componentList));
				for(int i=0;i<componentList.size();++i){
					Map<String,Object> param = componentList.get(i);
					param.put("BUS_CLUSTER_ID", BUS_CLUSTER_ID);
					Map<String,String> tempMap = MapConverter.convert(param);
					addList.add(tempMap);
				}
				coreService.insertBatchObject("busRelationClusterList.batchInsert", addList,dbKey);
			}
			
		} catch (Exception e) {
			log.error("新增业务主集群信息失败， 失败信息: ", e);
			throw new RuntimeException("新增业务主集群信息失败！");
		}

		resultMap.put("RST_CODE", BusinessConstant.PARAMS_DO_RST_SUCCESS);
		return resultMap;
    }

	/**
	 * 修改集群信息
	 *
	 * @param params
	 *            业务参数
	 * @param dbKey
	 *            数据库Key
	 * @return Map
	 */
	@Override
	public Map<String, Object> updateBusMainCluster(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("修改集群信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		Map<String,Object> resultMap = new HashMap<String,Object>();
		try {
			Map<String,String> tempMap = new HashMap<String,String>();
			tempMap.put("BUS_CLUSTER_ID", ObjectUtils.toString(params.get("BUS_CLUSTER_ID")));
			tempMap.put("BUS_CLUSTER_NAME", ObjectUtils.toString(params.get("BUS_CLUSTER_NAME")));
			tempMap.put("BUS_CLUSTER_CODE", ObjectUtils.toString(params.get("BUS_CLUSTER_CODE")));
			tempMap.put("BUS_CLUSTER_TYPE", ObjectUtils.toString(params.get("BUS_CLUSTER_TYPE")));
			tempMap.put("BUS_CLUSTER_SEQ", ObjectUtils.toString(params.get("BUS_CLUSTER_SEQ")));
			
			int updCnt = coreService.updateObject("busMainCluster.updateBusMainCluster", tempMap,dbKey);
			log.debug("修改业务主机群信息，修改记录数: " + updCnt);

			saveBusClusterInfo(params,dbKey);
			saveComponentClusterInfo(params,dbKey);
		} catch (Exception e) {
			log.error("修改业务主集群信息失败， 失败信息: ", e);
			throw new RuntimeException("修改业务主集群信息失败！");
		}

		resultMap.put("RST_CODE", BusinessConstant.PARAMS_DO_RST_SUCCESS);
		return resultMap;
	}
	
	/**
	 * 批量保存业务集群信息
	 * @Title: saveBusClusterInfo
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: void
	 * @author: tianjc
	 * @date: 2017年6月19日 上午10:23:58
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	private void saveBusClusterInfo(Map<String, Object> params,String dbKey){
		params.put("CLUSTER_PARENT_TYPE", 3);
		List<HashMap<String,Object>> list = coreService.queryForList2New("busMainCluster.queryBusClusterInfo", params,dbKey);
		List<Map<String,Object>> businessList = new ArrayList<Map<String,Object>>();
		JSONArray tempBusinessList = (JSONArray) params.get("BUSINESS_LIST");
		if(tempBusinessList != null && tempBusinessList.size() != 0){
			for(int i=0;i<tempBusinessList.size();++i){
				JSONObject obj = tempBusinessList.getJSONObject(i);
				businessList.add(obj);
			}
		}
		
		//已经部署的不能进行修改和删除
		List<HashMap<String, Object>> CLUSTER_IDs = new ArrayList<HashMap<String,Object>>();
		if(CollectionUtils.isNotEmpty(list)){
			for(int i=0;i<list.size();++i){
				HashMap<String,Object> param = new HashMap<String,Object>();
				
				if(StringUtils.isNotBlank(ObjectUtils.toString(list.get(i).get("CLUSTER_ID")))){
					param.put("CLUSTER_ID", list.get(i).get("CLUSTER_ID"));
					CLUSTER_IDs.add(param);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(businessList)){
			for(int i=0;i<businessList.size();++i){
				HashMap<String,Object> param = new HashMap<String,Object>();
				
				if(StringUtils.isNotBlank(ObjectUtils.toString(businessList.get(i).get("CLUSTER_ID")))){
					param.put("CLUSTER_ID", businessList.get(i).get("CLUSTER_ID"));
					CLUSTER_IDs.add(param);
				}
			}
		}
		Set<Object> CLUSTER_IDSet = new HashSet<Object>();
		if(CollectionUtils.isNotEmpty(CLUSTER_IDs)){
			Map<String,Object> param = new HashMap<String,Object>();
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<CLUSTER_IDs.size();++i){
				HashMap<String, Object> clusterIDMap = CLUSTER_IDs.get(i);
				String clusterID = ObjectUtils.toString(clusterIDMap.get("CLUSTER_ID"));
				
				if(StringUtils.isEmpty(clusterID)){
					continue;
				}
				
				sb.append("'");
				sb.append(clusterID);
				sb.append("',");
				
			}
			param.put("CLUSTER_IDs", StringUtils.removeEnd(sb.toString(), ","));
			
			CLUSTER_IDs = coreService.queryForList2New("businessDeployList.queryDeployClusterIDs",param,dbKey);
			for(int i=0;i<CLUSTER_IDs.size();++i){
				CLUSTER_IDSet.add(CLUSTER_IDs.get(i).get("CLUSTER_ID"));
			}
		}
		
		log.debug("已经存储的业务集群:" + ObjectUtils.toString(list));
		log.debug("准备存储的业务集群:" + ObjectUtils.toString(businessList));
		
		//保存业务集群信息
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> updateList = new ArrayList<Map<String,Object>>();
		List<HashMap<String,Object>> deleteList = new ArrayList<HashMap<String,Object>>();
		if(CollectionUtils.isEmpty(list)){
			insertList = businessList;
		} else if(CollectionUtils.isEmpty(businessList)) {
			deleteList = list;
		} else {
			Iterator<HashMap<String,Object>> iterator= list.iterator();
			while(iterator.hasNext()){
				Map<String,Object> storeMap = iterator.next();
				
				Iterator<Map<String,Object>> busIterator= businessList.iterator();
				while(busIterator.hasNext()){
					Map<String,Object> busMap = busIterator.next();
					//已经部署的不让删除修改
					if(CLUSTER_IDSet.contains(busMap.get("CLUSTER_ID"))){
						busIterator.remove();
						continue;
					}
					
					if (StringUtils.equals(
							ObjectUtils.toString(storeMap.get("CLUSTER_TYPE")),
							ObjectUtils.toString(busMap.get("CLUSTER_TYPE")))) {
						
						busIterator.remove();
						iterator.remove();
						
						updateList.add(busMap);
						break;
					}
				}
				
				//已经部署的不让删除修改
				if(CLUSTER_IDSet.contains(storeMap.get("CLUSTER_ID"))){
					iterator.remove();
				}
			}
			
			deleteList = list;
			insertList = businessList;
		}
		
		List<Map<String,String>> tempList = new ArrayList<Map<String,String>>();
		if(CollectionUtils.isNotEmpty(deleteList)){
			log.debug("需要删除的业务集群:" + ObjectUtils.toString(deleteList));
			for(int i=0;i<deleteList.size();++i){
				Map<String,Object> param = new HashMap<String,Object>();
				param.put("CLUSTER_ID", ObjectUtils.toString(deleteList.get(i).get("CLUSTER_ID")));
				param.put("BUS_CLUSTER_ID", ObjectUtils.toString(deleteList.get(i).get("BUS_CLUSTER_ID")));
				
				Map<String,String> tempMap = MapConverter.convert(param);
				tempList.add(tempMap);
			}
			coreService.deleteBatchObject("busRelationClusterList.batchDelete", tempList,dbKey);
			coreService.deleteBatchObject("serviceType.batchDeleteServiceType", tempList,dbKey);
		}
		if(CollectionUtils.isNotEmpty(updateList)){
			log.debug("需更新的业务集群:" + ObjectUtils.toString(updateList));
			tempList.clear();
			for(int i=0;i<updateList.size();++i){
				Map<String,String> tempMap = MapConverter.convert(updateList.get(i));
				coreService.updateObject("serviceType.updateDcfServiceType", tempMap,dbKey);
			}
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			log.debug("需添加的业务集群:" + ObjectUtils.toString(insertList));
			tempList.clear();
			for(int i=0;i<insertList.size();++i){
				Map<String,Object> param = insertList.get(i);
				String CLUSTER_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
				param.put("CLUSTER_ID", CLUSTER_ID);
				param.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));
				
				Map<String,String> tempMap = MapConverter.convert(insertList.get(i));
				tempList.add(tempMap);
			}
			coreService.insertBatchObject("busRelationClusterList.batchInsert", tempList,dbKey);
			coreService.insertBatchObject("serviceType.batchInsertDcfServiceType", tempList,dbKey);
		}
	}
	
	/**
	 * 批量保存业务集群信息
	 * @Title: saveComponentClusterInfo
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: void
	 * @author: tianjc
	 * @param dbKey 
	 * @date: 2017年6月19日 上午10:23:58
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	private void saveComponentClusterInfo(Map<String, Object> params, String dbKey){
		params.put("CLUSTER_PARENT_TYPE", 1);
		List<HashMap<String,Object>> list = coreService.queryForList2New("busRelationClusterList.queryComponentClusterByBusClusterId", params,dbKey);
		List<Map<String,Object>> componentList = new ArrayList<Map<String,Object>>();
		JSONArray tempComponentList = (JSONArray) params.get("COMPONENT_LIST");
		if(tempComponentList != null && tempComponentList.size() != 0){
			for(int i=0;i<tempComponentList.size();++i){
				JSONObject obj = tempComponentList.getJSONObject(i);
				componentList.add(obj);
			}
		}
		
		//已经部署的不能进行修改和删除
		List<HashMap<String, Object>> CLUSTER_IDs = new ArrayList<HashMap<String,Object>>();
		if(CollectionUtils.isNotEmpty(list)){
			for(int i=0;i<list.size();++i){
				HashMap<String,Object> param = new HashMap<String,Object>();
				
				if(StringUtils.isNotBlank(ObjectUtils.toString(list.get(i).get("CLUSTER_ID")))){
					param.put("CLUSTER_ID", list.get(i).get("CLUSTER_ID"));
					CLUSTER_IDs.add(param);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(componentList)){
			for(int i=0;i<componentList.size();++i){
				HashMap<String,Object> param = new HashMap<String,Object>();
				
				if(StringUtils.isNotBlank(ObjectUtils.toString(componentList.get(i).get("CLUSTER_ID")))){
					param.put("CLUSTER_ID", componentList.get(i).get("CLUSTER_ID"));
					CLUSTER_IDs.add(param);
				}
			}
		}
		Set<Object> CLUSTER_IDSet = new HashSet<Object>();
		if(CollectionUtils.isNotEmpty(CLUSTER_IDs)){
			Map<String,Object> param = new HashMap<String,Object>();
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<CLUSTER_IDs.size();++i){
				HashMap<String, Object> clusterIDMap = CLUSTER_IDs.get(i);
				String clusterID = ObjectUtils.toString(clusterIDMap.get("CLUSTER_ID"));
				
				sb.append("'");
				sb.append(clusterID);
				sb.append("',");
				
			}
			param.put("CLUSTER_IDs", StringUtils.removeEnd(sb.toString(), ","));
			
			CLUSTER_IDs = coreService.queryForList2New("businessDeployList.queryDeployClusterIDs",param,dbKey);
			for(int i=0;i<CLUSTER_IDs.size();++i){
				CLUSTER_IDSet.add(CLUSTER_IDs.get(i).get("CLUSTER_ID"));
			}
		}
		
		
		log.debug("已经存储的组件集群:" + ObjectUtils.toString(list));
		log.debug("准备存储的组件集群:" + ObjectUtils.toString(componentList));
		
		//保存业务集群信息
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		List<HashMap<String,Object>> deleteList = new ArrayList<HashMap<String,Object>>();
		if(CollectionUtils.isEmpty(list)){
			insertList = componentList;
		} else if(CollectionUtils.isEmpty(componentList)) {
			deleteList = list;
		} else {
			Iterator<HashMap<String,Object>> iterator= list.iterator();
			while(iterator.hasNext()){
				Map<String,Object> storeMap = iterator.next();
				
				Iterator<Map<String,Object>> componentIterator= componentList.iterator();
				while(componentIterator.hasNext()){
					Map<String,Object> map = componentIterator.next();
					//已经部署的不让删除修改
					if(CLUSTER_IDSet.contains(map.get("CLUSTER_ID"))){
						componentIterator.remove();
						continue;
					}
					
					boolean CLUSTER_ID_BOOL = ObjectUtils.equals(storeMap.get("CLUSTER_ID"),map.get("CLUSTER_ID"));
					boolean CLUSTER_TYPE_BOOL = ObjectUtils.equals(storeMap.get("CLUSTER_TYPE"),map.get("CLUSTER_TYPE"));
					
					//是否已经在数据库保存
					if (CLUSTER_TYPE_BOOL && CLUSTER_ID_BOOL) {
						iterator.remove();
						componentIterator.remove();
						
						break;
					}
				}
				
				//已经部署的不让删除修改
				if(CLUSTER_IDSet.contains(storeMap.get("CLUSTER_ID"))){
					iterator.remove();
				}
			}
			
			deleteList = list;
			insertList = componentList;
		}
		
		if(CollectionUtils.isNotEmpty(deleteList)){
			log.debug("需要删除的组件集群:" + ObjectUtils.toString(deleteList));
			List<Map<String,String>> tempList = new ArrayList<Map<String,String>>();
			for(int i=0;i<deleteList.size();++i){
				Map<String,Object> param = new HashMap<String,Object>();
				param.put("BUS_CLUSTER_ID", ObjectUtils.toString(deleteList.get(i).get("BUS_CLUSTER_ID")));
				param.put("CLUSTER_ID", ObjectUtils.toString(deleteList.get(i).get("CLUSTER_ID")));
				
				Map<String,String> tempMap = MapConverter.convert(param);
				tempList.add(tempMap);
			}
			coreService.deleteBatchObject("busRelationClusterList.batchDelete", tempList,dbKey);
		}
		if(CollectionUtils.isNotEmpty(insertList)){
			log.debug("需要添加的组件集群:" + ObjectUtils.toString(insertList));
			List<Map<String,String>> tempList = new ArrayList<Map<String,String>>();
			for(int i=0;i<insertList.size();++i){
				Map<String,Object> param = insertList.get(i);
				param.put("BUS_CLUSTER_ID", params.get("BUS_CLUSTER_ID"));
				
				Map<String,String> tempMap = MapConverter.convert(param);
				tempList.add(tempMap);
			}
			coreService.insertBatchObject("busRelationClusterList.batchInsert", tempList,dbKey);
		}
	}

	/**
	 * 获取用户集群配置信息
	 * @Title: getUserBusMainCluster
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return: void
	 * @author: wanw
	 * @param dbKey
	 * @date: 2018年10月24日 上午9:23:58
	 * @editAuthor:
	 * @editDate:
	 * @editReason:
	 */
	public List<HashMap<String, Object>>getUserBusMainCluster(Map<String, Object> params, String dbKey)throws Exception{
		log.debug("获取用户集群配置信息， 业务参数: " + params.toString() + ", dbKey: " + dbKey);
		//超级管理员展示所有集信息
		if(BusinessConstant.PARAMS_BUS_1.equals(StringTool.object2String(params.get("EMPEE_ID")))){
			params.put("PERMISSION_SWITCH", "no");
		}else{
			params.put("PERMISSION_SWITCH", SystemProperty.getContextProperty("cluster.permission.switch"));
		}
		return coreService.queryForList2New("busMainCluster.getUserBusMainClusterInfo", params,dbKey);
	}
}
