package com.tydic.web.system;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.system.RoleService;

/**
 * 配置管理-角色管理控制器
 *@author : 朱伟
 */
@Controller
@RequestMapping("/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;
    private static Logger log = Logger.getLogger(RoleController.class);

    /**
     * 删除角色
     *@author : 朱伟
     */
    @RequestMapping(value="/delete",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delRoleRecord(HttpServletRequest request)
            throws Exception {
        try {
            roleService.deleteRole(this.getParamsList(request), FrameConfigKey.DEFAULT_DATASOURCE);
        } catch (Exception e) {
            log.error("删除角色失败---->", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }

        return null;
    }
}
