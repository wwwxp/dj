package com.tydic.web.clustermanager;

import PluSoft.Utils.JSON;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.clustermanager.GroupConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/groupConfig")
public class GroupConfigController extends BaseController {

    private static Logger log = LoggerFactory.getLogger(GroupConfigController.class);

    @Autowired
    private GroupConfigService groupConfigService;

    /**
     * 新增配置参数
     * @param request
     * @return
     */
    @RequestMapping(value="/addGroupConfig",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addGroupConfig(HttpServletRequest request) {
        log.debug("添加&修改配置参数开始...");
        try {
            Map<String, Object> retMap = groupConfigService.addGroupConfig(this.getParamsMapByObject(request), getDbKey(request));
            log.debug("添加&修改配置参数结束...");
            return JSON.Encode(retMap);
        } catch (Exception e) {
            log.error("添加&修改配置参数失败， 失败原因:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 删除配置参数
     * @param request
     * @return
     */
    @RequestMapping(value="/delGroupConfig",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String delGroupConfig(HttpServletRequest request) {
        log.debug("删除配置参数开始...");
        try {
            Map<String, Object> retMap = groupConfigService.delGroupConfig(this.getParamsMapByObject(request), getDbKey(request));
            log.debug("删除配置参数结束...");
            return JSON.Encode(retMap);
        } catch (Exception e) {
            log.error("删除配置参数异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
}
