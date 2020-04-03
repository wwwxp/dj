package com.tydic.web.clustermanager;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.clustermanager.BusProgramTaskService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;
import com.tydic.util.StringTool;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busProgramTask")
public class BusProgramTaskController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(BusProgramTaskController.class);

	/**
	 * 主机操作Service
	 */
	@Autowired
	private BusProgramTaskService busProgramTaskService;

	@RequestMapping(value="/getBusTargetConfigList",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getBusTargetConfigList(HttpServletRequest request){
		log.debug("业务程序状态管理-获取配置文件信息开始...");
		try {
			Map<String,Object> param = this.getParamsMapByObject(request);
			param.put("webRootPath", SessionUtil.getWebRootPath(request));
			Map<String, Object> rstJson = busProgramTaskService.getBusTargetConfigList(param, getDbKey(request));
			log.debug("业务程序状态管理-获取配置文件信息结束...");
			return JSON.Encode(rstJson);
		} catch (Exception e) {
			log.error("获取业务程序配置文件信息异常， 异常信息:", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 添加业务程序
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/getBusConfigList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getBusConfigList(HttpServletRequest request) {
        log.debug("获取业务程序配置文件信息开始...");
        try {
        	Map<String, Object> rstJson = busProgramTaskService.getBusConfigList(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("获取业务程序配置文件信息结束...");
        	return JSON.Encode(rstJson);
        } catch (Exception e) {
            log.error("获取业务程序配置文件信息异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
	
	/**
	 * 添加业务程序
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/getBusProgramListWithHost",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String getBusProgramListWithHost(HttpServletRequest request) {
        log.debug("获取业务程序配置文件信息开始...");
        try {
        	Map<String, Object> rstJson = busProgramTaskService.getBusProgramListWithHost(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("获取业务程序配置文件信息结束...");
        	return JSON.Encode(rstJson);
        } catch (Exception e) {
            log.error("获取业务程序配置文件信息异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
	/**
	 * 添加业务程序
	 * @param request
	 * @return
	 */
    @RequestMapping(value="/insertBusProgramTask",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String insertBusProgramTask(HttpServletRequest request) {
        log.debug("添加业务程序信息开始...");
        try {
        	Map<String, Object> rstMap = busProgramTaskService.insetBusProgramTask(this.getParamsMapByObject(request), getDbKey(request));
        	log.debug("添加业务程序结束...");
        	return JSON.Encode(rstMap);
        } catch (Exception e) {
            log.error("添加业务程序异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
    /**
     * 修改集群配置信息
     * @param request
     * @return
     */
    @RequestMapping(value="/updateBusProgramTask",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateBusProgramTask(HttpServletRequest request) {
        log.debug("修改业务程序开始...");
        try {
        	busProgramTaskService.updateBusProgramTask(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("修改业务程序异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改业务程序结束...");
        return null;
    }



	/**
	 * 业务程序状态检查页面使用（该页面只删除当前选中的业务程序，不会删除其他版本当前程序类型）
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delCurrVerBusProgramTask",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delCurrVerBusProgramTask(HttpServletRequest request) {
		log.debug("删除业务程序开始...");
		try {
			List list  = this.getParamsList(request);
			Map<String,Object> rstMap =null;
			String success="";
			String fail ="";
			for(int i = 0 ; i < list.size() ;i++){
				Map<String,Object> pMap = (Map<String, Object>)list.get(i);
				rstMap = busProgramTaskService.deleteCurrVersionBusProgramTask(pMap, getDbKey(request));
				String name = StringTool.object2String(pMap.get("PROGRAM_ALIAS"));
				if(StringUtils.isBlank(name)){
					name = StringTool.object2String(pMap.get("PROGRAM_NAME"));
				}
				if(Constant.RST_CODE_SUCCESS.equals(rstMap.get(Constant.RST_CODE))){

					if(StringUtils.isNotBlank(success)){
						success +=","+name;
					}else{
						success = name;
					}
				}else{
					if(StringUtils.isNotBlank(fail)){
						fail +="," +name;
					}else{
						fail = name;
					}
				}
			}
			rstMap.put("message_success", success);
			rstMap.put("message_fail", fail);
			log.debug("删除业务程序结束...");
			return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("删除业务程序异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}

	/**
	 * 删除业务程序
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delBusProgramTask",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delBusProgramTask(HttpServletRequest request) {
		log.debug("删除业务程序开始...");
		try {
			List list  = this.getParamsList(request);
			Map<String,Object> rstMap =null;
			String success="";
			String fail ="";
			for(int i = 0 ; i < list.size() ;i++){
				Map<String,Object> pMap = (Map<String, Object>)list.get(i);
				rstMap = busProgramTaskService.deleteBusProgramTask(pMap, getDbKey(request));
				String name = StringTool.object2String(pMap.get("PROGRAM_ALIAS"));
				if(StringUtils.isBlank(name)){
					name = StringTool.object2String(pMap.get("PROGRAM_NAME"));
				}
				if(Constant.RST_CODE_SUCCESS.equals(rstMap.get(Constant.RST_CODE))){
					
					if(StringUtils.isNotBlank(success)){
						success +=","+name;
					}else{
						success = name;
					}
				}else{
					if(StringUtils.isNotBlank(fail)){
						fail +="," +name;
					}else{
						fail = name;
					}
				}
			}
			rstMap.put("message_success", success);
			rstMap.put("message_fail", fail);
			log.debug("删除业务程序结束...");
			return JSON.Encode(rstMap);
		} catch (Exception e) {
			log.error("删除业务程序异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
	 * 删除Topology业务程序
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delBusTopologyProgramTask",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String delBusTopologyProgramTask(HttpServletRequest request) {
		log.debug("删除Topology业务程序开始...");
		try {
			List list  = this.getParamsList(request);
			Map<String,Object> rstMap =null;
			String success="";
			String fail ="";
			for(int i = 0 ; i < list.size() ;i++){
				Map<String,Object> pMap = (Map<String, Object>)list.get(i);
				rstMap = busProgramTaskService.deleteBusTopologyProgramTask(pMap, getDbKey(request));				
				if(Constant.RST_CODE_SUCCESS.equals(rstMap.get(Constant.RST_CODE))){
					if(StringUtils.isNotBlank(success)){
						success +=","+pMap.get("PROGRAM_NAME");
					}else{
						success = pMap.get("PROGRAM_NAME")+"";
					}
					
				}else{
					if(StringUtils.isNotBlank(fail)){
						fail +="," + pMap.get("PROGRAM_NAME");
					}else{
						fail = pMap.get("PROGRAM_NAME")+"";
					}
				}
			}
			rstMap.put("message_success", success);
			rstMap.put("message_fail", fail);
			log.debug("删除Topology业务程序结束...");
			return JSON.Encode(rstMap);
			 
		} catch (Exception e) {
			log.error("删除Topology业务程序异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
	}
	
	/**
     * 修改集群配置信息
     * @param request
     * @return
     */
    @RequestMapping(value="/updateTaskCell",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateTaskCell(HttpServletRequest request) {
        log.debug("修改业务程序开始...");
        try {
        	busProgramTaskService.updateTaskCell(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("修改业务程序异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("修改业务程序结束...");
        return null;
    }


	/**
	 * 查询程序启停日志文件信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/queryLogDetail",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String queryLogDetail(HttpServletRequest request) {
		log.debug("查询业务程序启停日志信息...");
		Map<String, Object> retMap = null;
		try {
			retMap = busProgramTaskService.queryLogDetail(this.getParamsMapByObject(request), getDbKey(request));
		} catch (Exception e) {
			log.error("查询业务程序启停日志信息异常， 异常信息: ", e);
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
		}
		log.debug("查询业务程序启停日志信息结束...");
		return JSON.Encode(retMap);
	}
}
