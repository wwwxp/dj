package com.tydic.web.configure;

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
import com.tydic.service.configure.ProcessStateOfHostService;

/**
 * 
  * Simple to Introduction    
  * @ProjectName:  [DCBPortal_net_release]   
  * @Package:      [com.tydic.web.configure]    
  * @ClassName:    [ProcessStateOfHostController]     
  * @Description:  [检查主机运行状态]     
  * @Author:       [Yuanh]     
  * @CreateDate:   [2017-6-13 下午5:56:04]     
  * @UpdateUser:   [Yuanh]     
  * @UpdateDate:   [2017-6-13 下午5:56:04]     
  * @UpdateRemark: [说明本次修改内容]    
  * @Version:      [v1.0]   
  *
 */
@Controller
@RequestMapping("/hostProcess")
public class ProcessStateOfHostController extends BaseController {
	/**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(ProcessStateOfHostController.class);
	
	@Autowired
	private ProcessStateOfHostService processStateOfHostService;
	
	
    /**
     * 检查运行状态
     * @param request
     * @return
     */
    @RequestMapping(value="/checkHostState",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String multiCheckHostState(HttpServletRequest request) {
        log.debug("HostProcessStateController,检查运行状态");
        try {
        	Map<String, Object> resultMap =processStateOfHostService.checkHostState(this.getParamsMapByObject(request), this.getDbKey(request));
         	return JSON.Encode(resultMap);
        } catch (Exception e) {
            log.error("HostProcessStateController，检查运行状态失败 ---> ",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
 }


