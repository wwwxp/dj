package com.tydic.web.system;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.system.PrivilegeService;

/**
 * 配置管理-权限管理控制器
 *@author : 朱伟
 */
@Controller
@RequestMapping("/privilege")
public class PrivilegeController extends BaseController {

    @Autowired
    private PrivilegeService privilegeService;
    private static Logger log = Logger.getLogger(PrivilegeController.class);

    /**
     * 增加权限
     *@author : 朱伟
     */
    @RequestMapping(value="/addPrivilege",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addPrivilege(HttpServletRequest request) {
        try {
            //获取参数
            String paramsStr = request.getParameter(FrameParamsDefKey.PARAMS);
            log.debug("参数为 ---> " + paramsStr);
            if ((paramsStr != null) && (!paramsStr.equals(""))) {
                privilegeService.insertPrivilege(this.getParamsList(request),FrameConfigKey.DEFAULT_DATASOURCE);
            }else{
                return  JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "参数为空！"));
            }
        } catch (Exception e) {
            log.error("增加权限失败", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }

    /**
     * 修改权限
     *@author : 朱伟
     */
    @RequestMapping(value="/updatePrivilege",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updatePrivilege(HttpServletRequest request){
        try {
            //获取参数
            String paramsStr = request.getParameter(FrameParamsDefKey.PARAMS);
            log.debug("参数为 ---> " + paramsStr);
            if ((paramsStr != null) && (!paramsStr.equals(""))) {

                privilegeService.updatePrivilege(this.getParamsList(request),FrameConfigKey.DEFAULT_DATASOURCE);
            }else{
                return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "参数为空！"));
            }
        } catch (Exception e) {
            log.error("增加权限失败", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }
}
