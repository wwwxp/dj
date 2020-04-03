package com.tydic.web.configure;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.CutOfflineService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.configure]    
  * @ClassName:    [CutOfflineController]     
  * @Description:  [切离线]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-7-5 上午10:19:44]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-7-5 上午10:19:44]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping("/cutOffline")
public class CutOfflineController extends BaseController {
	/**
	 * 日志对象
	 */
	private static Logger log = Logger.getLogger(CutOfflineController.class);
	
	/**
	 * 切离线Service对象
	 */
	@Autowired
	private CutOfflineService cutOfflineService;
	
	
	/**
    * 获取sp_switch.xml已有号段信息
    * @param request
    * @return
    */
   @RequestMapping(value="/info/existNumOrNet",produces = {"application/json;charset=UTF-8"})
   @ResponseBody
   public String loadExistNumInfo(HttpServletRequest request) {
   		log.debug("且离线, 获取网元信息开始...");
   		Map<String, Object> netMap = new HashMap<String, Object>();
   		try {
       		netMap = cutOfflineService.getExistNumOrNetInfo(this.getParamsMapByObject(request), getDbKey(request));
   		} catch (Exception e) {
   			log.debug("且离线获取网元信息异常， 异常信息: ", e);
   			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
   		}
   		log.debug("切离线, 获取网元信息结束...");
   		return JSON.Encode(netMap);
   }
	
    /**
     * 操作：切离线/不切离线
     * @param request
     * @return
     */
    @RequestMapping(value="/opt/cutOffline",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateCutOffline(HttpServletRequest request) {
    	log.debug("切离线开始...");
    	Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
        	resultMap=cutOfflineService.updateCutOffline(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
        	log.debug("切离线异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("切离线结束...");
        return JSON.Encode(resultMap);
    }
}
