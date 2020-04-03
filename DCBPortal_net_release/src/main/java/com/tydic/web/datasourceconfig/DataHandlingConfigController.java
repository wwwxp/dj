package com.tydic.web.datasourceconfig;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.datasourceconfig.DataHandlingConfigService;

import PluSoft.Utils.JSON;

@Controller
@RequestMapping("/dataHandlingConfig")
public class DataHandlingConfigController extends BaseController {

	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(DataHandlingConfigController.class);
	
	/**
	 * core Service对象
	 */
	@Autowired
	private CoreService coreService;
	@Autowired
	private DataHandlingConfigService dataHandlingConfigService;
	
	/**
	 * 新增数据处理
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/insert", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String insertDataHandlingConfig(HttpServletRequest request) {
		log.debug("DataSourceConfigController,新增表数据处理，开始");
		Map<String, Object> paramMap = this.getParamsMapByObject(request);
		
		try{
			//coreService.insertObject2New("datasourceArchive.insertDataHandlingConfigRelationship", paramMap, getDbKey(request));
			dataHandlingConfigService.insertNewHandling(paramMap, getDbKey(request));
			
		}catch(Exception e){
			log.error("DataSourceConfigController，新增表数据处理，失败  ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		
		log.debug("DataSourceConfigController,新增表数据处理，结束");
		return "";
	}
	
	@RequestMapping(value = "/update", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String updateDataHandlingConfig(HttpServletRequest request) {
		log.debug("DataSourceConfigController,修改表数据处理，开始");
		Map<String, Object> paramMap = this.getParamsMapByObject(request);
		
		try{
			dataHandlingConfigService.updateHandling(paramMap, getDbKey(request));
			
		}catch(Exception e){
			log.error("DataSourceConfigController，修改表数据处理，失败  ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		
		log.debug("DataSourceConfigController,修改表数据处理，结束");
		return "";
	}	
	
	@RequestMapping(value = "/delete", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteDataHandlingConfig(HttpServletRequest request) {
		log.debug("DataSourceConfigController,删除表数据处理，开始");
		//Map<String, Object> paramMap = this.getParamsMapByObject(request);
		List<Map<String,String>> paramMapList = this.getParamsList(request);
		try{
			
			for(int i=0;i<paramMapList.size();i++){
				dataHandlingConfigService.deleteHandling(paramMapList.get(i), getDbKey(request));
			}
			
		}catch(Exception e){
			log.error("DataSourceConfigController，删除表数据处理，失败  ---> ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		
		log.debug("DataSourceConfigController,删除表数据处理，结束");
		return "";
	}	
	
	
	
}
