package com.tydic.service.datasourceconfig.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tydic.bp.QuartzManager;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.datasourceconfig.DataHandlingConfigService;
import com.tydic.util.Constant;
import com.tydic.util.StringTool;

@Service
public class DataHandlingConfigServiceImpl implements DataHandlingConfigService {

	@Resource
	private CoreService coreService;
	
	@Autowired
	QuartzManager quartzManager;
	private static Logger log = Logger.getLogger(DataHandlingConfigServiceImpl.class);
	
	/**
	 * 插入表数据处理方法
	 */
	@Override
	public int insertNewHandling(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("DataSourceConfigController,新增表数据处理，开始");
		Map<String, Object> paramMap = new HashMap<String,Object>();
		paramMap.put("TASK_ID", StringTool.object2String(params.get("EXEC_METHOD")));
		
		try{
			coreService.insertObject2New("datasourceArchive.insertDataHandlingConfig", params, dbKey);
			//先查询ARCHIVE_ID
			String archiveId =StringTool.object2String(params.get("ARCHIVE_ID"));
			paramMap.put("BUS_ID", archiveId);
			paramMap.put("BUS_TYPE",Constant.BUS_TYPE_TWO_DB_ARCH); 
			coreService.insertObject2New("jobTaskBus.insert", paramMap, dbKey);
//			if(!"".equals(StringTool.object2String(params.get("EXEC_METHOD")))){
//				//插入关联关系
//				coreService.insertObject2New("jobTaskBus.insert", paramMap, dbKey);
//			}
			
		}catch(Exception e){
			log.error("插入表数据处理，失败：", e);
		}
		log.debug("DataSourceConfigController,新增表数据处理，结束");
		return 0;
	}

	@Override
	public int updateHandling(Map<String, Object> params, String dbKey) throws Exception {
		log.debug("DataSourceConfigController,修改表数据处理，开始");
		Map<String, String> paramMap = new HashMap<String,String>();
		paramMap.put("TASK_ID", StringTool.object2String(params.get("EXEC_METHOD")));
		
		try{
			coreService.updateObject2New("datasourceArchive.updateDataHandlingConfig", params, dbKey);
			//先查询ARCHIVE_ID
			//Map<String,Object> queryMap = coreService.queryForObject2New("datasourceArchive.queryArchiveDataForArchive", params, dbKey);
			paramMap.put("BUS_ID", StringTool.object2String(params.get("ARCHIVE_ID")));
			paramMap.put("BUS_TYPE",Constant.BUS_TYPE_TWO_DB_ARCH); 
			//更新关联关系
			List<HashMap<String, String>> busRel=coreService.queryForList("jobTaskBus.queryDSATaskJobRelation", paramMap, dbKey);
			
			if(BlankUtil.isBlank(busRel)){
				//如果关系记录不存在则添加
				coreService.updateObject("jobTaskBus.insert", paramMap, dbKey);
			}
			coreService.updateObject("jobTaskBus.update", paramMap, dbKey);

	
			
		}catch(Exception e){
			log.error("修改表数据处理，失败：", e);
		}
		log.debug("DataSourceConfigController,修改表数据处理，结束");
		return 0;
	}
	
	@Override
	public int deleteHandling(Map<String, String> params, String dbKey) throws Exception {
		log.debug("DataSourceConfigController,删除表数据处理，开始");
		Map<String, String> paramMap = new HashMap<String,String>();
		paramMap.put("TASK_ID", StringTool.object2String(params.get("EXEC_METHOD")));
		
		try{
			//coreService.deleteObject2New("datasourceArchive.deleteDataHandlingConfig", params, dbKey);
			coreService.deleteObject("datasourceArchive.deleteDataHandlingConfig", params, dbKey);
			//先查询ARCHIVE_ID
			paramMap.put("BUS_ID", StringTool.object2String(params.get("ARCHIVE_ID")));
			paramMap.put("BUS_TYPE", Constant.BUS_TYPE_TWO_DB_ARCH);
			Map<String, String> relationMap = coreService.queryForObject("jobTaskBus.queryDSATaskJobRelation", paramMap, dbKey);
			if(!BlankUtil.isBlank(relationMap)){
				//移除已经存在的任务，jobName 为 关系表 DCF_JOB_TASK_BUS 的 id
				String jobName = StringTool.object2String(relationMap.get("ID"));
				quartzManager.removeJob(jobName, null);
			}
//			quartzManager.removeJob(jobName, jobGroupName);
			//更新关联关系
			coreService.deleteObject("jobTaskBus.delete", paramMap, dbKey);
			
		}catch(Exception e){
			log.error("删除表数据处理，失败：", e);
		}
		log.debug("DataSourceConfigController,删除表数据处理，结束");
		return 0;
	}
	
	
	
}
