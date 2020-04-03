package com.tydic.web.system;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.system.RoleConfigService;

/**
 * 配置管理-业务角色配置管理控制器
 *@author : 田玉姣
 */
@Controller
@RequestMapping("/roleconfig")
public class RoleConfigController extends BaseController {

    @Autowired
    private RoleConfigService roleConfigService;
    private static Logger log = Logger.getLogger(RoleConfigController.class);

    /**
     * 删除角色
     *@author : 田玉姣
     */
    @RequestMapping(value="/delete",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delRoleRecord(HttpServletRequest request)
            throws Exception {
        try {
            roleConfigService.deleteRole(this.getParamsList(request), FrameConfigKey.DEFAULT_DATASOURCE);
        } catch (Exception e) {
            log.error("删除角色失败---->", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

        return null;
    }
    
    /**
     * 查询用户
     *@author :  
     */
    @RequestMapping(value="/queryEmpeeList",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryEmpeeAndRole(HttpServletRequest request)
            throws Exception {
        try {
        	Map<String,Object> params = this.getParamsMapByObject(request); 
        	Map<String, Object> returnMap = roleConfigService.queryRole(params,getPageSize(request),getPageIndex(request), this.getDbKey(request));
        	return JSON.toJSONString(returnMap);
        } catch (Exception e) {
            log.error("查询用户失败---->", e);
            return JSON.toJSONString(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

      
    }
    
}
