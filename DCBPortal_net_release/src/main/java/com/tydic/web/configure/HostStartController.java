package com.tydic.web.configure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.tools.Base64Util;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.configure.HostStartService;
import com.tydic.util.Constant;
import com.tydic.util.SessionUtil;


/**
 * Created with IntelliJ IDEA.
 * User: zhongsixue
 * Date: 16-7-25
 * Time: 上午11:01
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/hostStart")
public class HostStartController extends BaseController{
	@Autowired
	private HostStartService hostStartService;

	private static Logger log = Logger.getLogger(HostStartController.class);
    
    /**
     * M2DB刷数据
     * @param request
     * @return
     */
    @RequestMapping(value="/m2dbRefresh",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateM2dbRefresh(HttpServletRequest request) {
        log.debug("M2DB刷数据开始...");
        List<String> resultList=null;
        try {
        	// 获取远程主机根目录
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("autoFile", Constant.M2DB_AUTH_FILE_COMMON);
            resultList = hostStartService.updateM2dbRefreshTables(param, getDbKey(request));
        	String resultStr = "";
        	for(int i = 0 ; i < resultList.size();i++){
        		resultStr = resultStr+resultList.get(i)+"\n";
        	}
        	return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, resultStr));
        } catch (Exception e) {
            log.error("M2DB刷数据结束...",e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    /**
     * 刷数据到内存
     * @param request
     * @return
     */
    @RequestMapping(value="/m2dbRefreshMem",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateM2dbRefreshMem(HttpServletRequest request) {
        log.debug("M2DB刷数据开始....");
        List<String> resultList=null;
        try {
        	// 获取远程主机根目录
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	param.put("autoFile", Constant.M2DB_AUTH_FILE_COMMON);
            resultList = hostStartService.updateM2dbRefreshMem(param, getDbKey(request));
        	String resultStr = "";
        	for(int i = 0 ; i < resultList.size();i++){
        		resultStr = resultStr+resultList.get(i)+"\n";
        	}
        	log.debug("M2DB刷数据结束...");
        	return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, resultStr));
        } catch (Exception e) {
            log.error("M2DB刷数据失败， 失败信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }
    
    /**
     * 刷数据到内存
     * @param request
     * @return
     */
    @RequestMapping(value="/m2dbInputTable",produces = {"text/html;charset=UTF-8"})
    @ResponseBody
    public String updateM2dbInputTable(@RequestParam MultipartFile uFile, HttpServletRequest request) {
        log.debug("M2DB导入数据开始...");
        List<String> resultList=null;
        try {
        	// 获取远程主机根目录
        	Map<String,Object> param = new HashMap<String,Object>();
        	String hosts = Base64Util.base64Decode(request.getParameter("hostList"));
        	List hostList = (List) JSON.Decode(hosts);
        	if(BlankUtil.isBlank(hostList)){
        		throw new RuntimeException("选择主机列表为空， 请检查！");
        	}
        	log.debug("M2DB导入数据， 主机列表: " + hosts.toString());
        	
        	param.put("hostList", hostList);
        	param.put("fileName", uFile.getOriginalFilename());
        	param.put("INSTANCE_NAME", request.getParameter("INSTANCE_NAME"));
        	param.put("CLUSTER_ID", request.getParameter("CLUSTER_ID"));
        	param.put("CLUSTER_TYPE", request.getParameter("CLUSTER_TYPE"));
        	param.put("autoFile", Constant.M2DB_AUTH_FILE_COMMON);
        	String filePath = SessionUtil.getWebRootPath(request) + Constant.TMP + uFile.getOriginalFilename();
        	uFile.transferTo(new File(filePath));
        	param.put("webRootPathFile", filePath);
            resultList = hostStartService.updateM2dbInputTable(param, getDbKey(request));
        	String resultStr = "";
        	for(int i = 0 ; i < resultList.size();i++){
        		resultStr = resultStr+resultList.get(i)+"\n";
        	}
        	log.debug("M2DB导入数据结束...");
        	return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, resultStr));
        } catch (Exception e) {
            log.error("M2DB导入数据失败， 异常原因:", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.SUCCESS, e.getMessage()));
        }
    }
    
    /**
     * 检查程序运行状态
     * @param request
     * @return
     */
    @RequestMapping(value="/checkProcessState",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String checkProcessState(HttpServletRequest request) {
        log.debug("检查组件进程状态开始...");
        try {
        	Map<String,Object> param = this.getParamsMapByObject(request);
        	Map<String,Object> resultMap = hostStartService.checkProcessState(param);
        	log.debug("检查组件进程状态结束...");
         	return JSON.Encode(resultMap);
        } catch (Exception e) {
            log.error("检查组件进程状态异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 组件批量状态检查
     * @param request
     * @return
     */
    @RequestMapping(value="/batchCheckStatus",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String batchCheckStatus(HttpServletRequest request) {
        log.debug("组件批量状态检查开始...");
        try {
            List<Map<String,Object>> paramsList = this.getParamsObjList(request);
            Map<String,Object> resultMap = hostStartService.batchCheckStatus(paramsList, this.getDbKey(request));
            log.debug("组件批量状态检查结束...");
            return JSON.Encode(resultMap);
        } catch (Exception e) {
            log.error("组件批量状态检查异常异常， 异常信息: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    
    /**
     * 更新程序/主机状态
     * @param request
     * @return
     */
    @RequestMapping(value="/updateProcessState",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public String updateProcessState(HttpServletRequest request) {
        log.debug("更新组件进程状态开始...");
        try {
        	Map<String, Object> params = this.getParamsMapByObject(request);
        	Map<String,Object> resultMap = hostStartService.updateProcessState(params, this.getDbKey(request));
        	log.debug("更新组件进程状态结束");
         	return JSON.Encode(resultMap);
        } catch (Exception e) {
            log.error("更新组件进程状态异常， 异常原因: ", e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 获取参数Object列表对象
     * @param request
     * @return
     */
    public List<Map<String, Object>> getParamsObjList(HttpServletRequest request) {
        List<Map<String, Object>> paramsList = new ArrayList<Map<String, Object>>();
        String paramsStr = request.getParameter(FrameParamsDefKey.PARAMS);
        log.debug("参数，转换前数据 ---> " + paramsStr);
        if(paramsStr != null && !paramsStr.equals("")) {
            if(paramsStr.startsWith("[") && paramsStr.endsWith("]")) {
                paramsList = (List) com.alibaba.fastjson.JSON.parse(paramsStr);
                log.debug("参数，参数为 ---> " + paramsList);
                return paramsList;
            } else {
                log.debug("参数：参数不是有效的Json格式，不转换");
                return paramsList;
            }
        } else {
            log.debug("参数，参数为空，不转换");
            return paramsList;
        }
    }
}
