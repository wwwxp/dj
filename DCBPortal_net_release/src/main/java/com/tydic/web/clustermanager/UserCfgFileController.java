package com.tydic.web.clustermanager;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.tydic.util.StringTool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.clustermanager.UserBusProgramService;
import com.tydic.util.ftp.FileRecord;

import PluSoft.Utils.JSON;

/**
 * Auther: zhuwei
 * Date: 2018-07-09 15:55
 * Description:
 */
@Controller
@RequestMapping(value = "/userBusCfgFile")
public class UserCfgFileController extends BaseController {

    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(UserCfgFileController.class);

    /**
     * 配置文件权限分配Service
     */
    @Autowired
    private UserBusProgramService userBusProgramService;
    @Autowired
    CoreService coreService;
 
    /**
     * 查询配置文件列表
     * @param request
     * @return
     */
    @RequestMapping(value="/queryCfgFileList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String queryCfgFileList(HttpServletRequest request) {
        log.debug("查询配置文件开始...");
/*        List<List<FileRecord>> programList = new ArrayList<List<FileRecord>>();
        try {
        	Map<String,Object> params = this.getParamsMapByObject(request);
        	List<HashMap<String,Object>> packageTypeList = coreService.queryForList2New("config.queryConfigList", this.getParamsMapByObject(request),  getDbKey(request));
            if(packageTypeList !=null && packageTypeList.size() >0){
            	for(int i = 0 ; i < packageTypeList.size() ;i++){
            		params.put("packageType", packageTypeList.get(i).get("CONFIG_VALUE"));
            		params.put("packageTypeName", packageTypeList.get(i).get("CONFIG_NAME"));
            		List<FileRecord> cfgList = userBusProgramService.queryCfgFileList(params, getDbKey(request));
            		programList.add(cfgList);
            	}
            }
        } catch (Exception e) {
            log.error("查询配置文件异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("查询配置文件结束...");
        return JSON.Encode(programList);*/
        List<FileRecord> programList = new ArrayList<FileRecord>();
        try {
        	Map<String,Object> params = this.getParamsMapByObject(request);

        	String packageTypeName = URLDecoder.decode(request.getParameter("packageTypeName"), "UTF-8");
        	params.put("packageTypeName", packageTypeName);
        	programList = userBusProgramService.queryCfgFileList(params, getDbKey(request));
        } catch (Exception e) {
            log.error("查询配置文件异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("查询配置文件结束...");
        return JSON.Encode(programList);
        
    }
    
    /**
     * 添加用户配置文件权限
     * @param request
     * @return
     */
    @RequestMapping(value = "/addCfgFileList", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String addCfgFileList(HttpServletRequest request) {
        log.debug("添加配置文件列表开始...");
        try {
            userBusProgramService.addCfgFileList(this.getParamsMapByObject(request), getDbKey(request));
        } catch (Exception e) {
            log.error("添加配置文件列表异常， 异常信息:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        log.debug("添加配置文件列表结束...");
        return null;
    }
    
}
