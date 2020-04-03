package com.tydic.service.system.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameLogDefKey;
import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.ILogService;

/**
 * Spring AOP 日志拦截实现类
 * 由于拦截service层可能会导致记录多次日志,此处拦截Action层
 * 从request中获取参数进行过滤,并保存入库
 *
 * @author tangdl
 */
public class LogServiceImpl implements ILogService {
    /**
     * log4j日志对象
     */
    private static Logger log = Logger.getLogger(LogServiceImpl.class);
    /**
     * 注入coreService
     */
    @Resource
    private CoreService coreService;


    public void log() {
        log.debug("日志");
    }

    // 有参无返回值的方法
    public void logArg(JoinPoint point) {
        try {
            // 此方法返回的是一个数组，数组中包括request以及ActionCofig等类对象
            Object[] args = point.getArgs();
            HttpServletRequest request = null;
            //获取request对象
            for (Object obj : args) {
                if (obj instanceof HttpServletRequest) {
                    request = (HttpServletRequest) obj;
                    break;
                }
            }
            //遍历request对象中的参数
            Map<String, String[]> paramsMap;
            if (request != null) {
                StringBuffer allParamsBuffer = new StringBuffer("");
                //获取所有request中的参数拼装为字符串
                paramsMap = request.getParameterMap();
                //获取执行方法名称
                String lowerCaseMethod = StringUtils.trimToEmpty(point.getSignature().getName());
                //如果是execute方法，则再判断下参数中是什么方法
                if(lowerCaseMethod.equals(FrameLogDefKey.EXECUTE)){
                    lowerCaseMethod = StringUtils.trimToEmpty(paramsMap.get(FrameLogDefKey.METHOD)[0]);
                }
                if(lowerCaseMethod.length()<1){
                    return;
                }
                //获取执行操作类型
                String execMethodType = null;
                if (lowerCaseMethod.contains(FrameLogDefKey.INSERT) || lowerCaseMethod.contains(FrameLogDefKey.ADD)) {
                    execMethodType = FrameLogDefKey.INSERT;//新增
                } else if (lowerCaseMethod.contains(FrameLogDefKey.UPDATE) || lowerCaseMethod.contains(FrameLogDefKey.EDIT)) {
                    execMethodType = FrameLogDefKey.UPDATE;//更新
                } else if (lowerCaseMethod.contains(FrameLogDefKey.DEL)) {
                    execMethodType = FrameLogDefKey.DELETE;//删除
                } else if (lowerCaseMethod.contains(FrameLogDefKey.MULTI)) {
                    execMethodType = FrameLogDefKey.MULTI;//综合操作
                }
                if (execMethodType != null) {
                    Set<String> ks = paramsMap.keySet();
                    for (Iterator<String> it = ks.iterator(); it.hasNext(); ) {
                        String key = (String) it.next();
                        if (!isNoRecordParams(key)) {
                            String value = paramsMap.get(key)[0];
                            allParamsBuffer.append(key + ":" + value + ",");
                        }
                    }
                    
                    String allParams = CommonTool.splitLastSymbol(allParamsBuffer.toString(), ",");
                    
                    //参数长度过长时，截取部分参数，否则插入数据库日志时报错。
                    if(allParams!=null && allParams.getBytes().length>4000){
                    	allParams=allParams.substring(0, 2000)+"...";
                    }
                    //获取模块名称
                    String logName = paramsMap.get(FrameLogDefKey.LOGNAME) == null ? lowerCaseMethod : paramsMap.get(FrameLogDefKey.LOGNAME)[0];
                    //ip地址
                    String ip = CommonTool.getRemortIP(request);
                    //用户
                    Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
                    String loginUser = userMap == null ? "" : userMap.get("EMPEE_NAME");

                    log.debug("日志记录开始");
                    log.debug("日志记录 --- 执行类型  ---> " + execMethodType);
                    log.debug("日志记录 --- 执行方法  ---> " + lowerCaseMethod);
                    log.debug("日志记录 --- 执行方参数 ---> " + allParams);
                    //保存日志到数据库
                    Map<String, String> sysLogMap = new HashMap<String, String>();
                    sysLogMap.put(FrameLogDefKey.LOGNAME, logName);
                    sysLogMap.put(FrameLogDefKey.IP, ip);
                    sysLogMap.put(FrameLogDefKey.LOGINUSER, loginUser);
                    sysLogMap.put(FrameLogDefKey.PARAMS, allParams);
                    sysLogMap.put(FrameLogDefKey.EXECTYPE, execMethodType);
                    sysLogMap.put(FrameLogDefKey.METHOD, lowerCaseMethod);
                    List<Map<String, String>> sysLogList = new ArrayList<Map<String, String>>();
                    sysLogList.add(sysLogMap);
                    //设置为默认数据源
                    coreService.insertObject(FrameConfigKey.INSERT_SYS_LOG, sysLogList, FrameConfigKey.DEFAULT_DATASOURCE);
                    log.debug("日志记录结束");
                }
            }
        } catch (Exception e) {
            log.warn("拦截日志出错[忽略错误] ---> ", e);
        }
    }

    // 有参并有返回值的方法
    public void logArgAndReturn(JoinPoint point, Object returnObj) {
        try {
            // 此方法返回的是一个数组，数组中包括request以及ActionCofig等类对象
            Object[] args = point.getArgs();
            HttpServletRequest request = null;
            //获取request对象
            for (Object obj : args) {
                if (obj instanceof HttpServletRequest) {
                    request = (HttpServletRequest) obj;
                    break;
                }
            }
            //遍历request对象中的参数
            Map<String, String[]> paramsMap;
            if (request != null) {
                StringBuffer allParamsBuffer = new StringBuffer("");
                //获取所有request中的参数拼装为字符串
                paramsMap = request.getParameterMap();
                //获取执行方法名称
                String lowerCaseMethod = paramsMap.get(FrameLogDefKey.METHOD)[0];
                //获取执行操作类型
                String execMethodType = null;
                if (lowerCaseMethod.contains(FrameLogDefKey.INSERT) || lowerCaseMethod.contains(FrameLogDefKey.ADD)) {
                    execMethodType = FrameLogDefKey.INSERT;//新增
                } else if (lowerCaseMethod.contains(FrameLogDefKey.UPDATE) || lowerCaseMethod.contains(FrameLogDefKey.EDIT)) {
                    execMethodType = FrameLogDefKey.UPDATE;//更新
                } else if (lowerCaseMethod.contains(FrameLogDefKey.DEL)) {
                    execMethodType = FrameLogDefKey.DELETE;//删除
                } else if (lowerCaseMethod.contains(FrameLogDefKey.MULTI)) {
                    execMethodType = FrameLogDefKey.MULTI;//综合操作
                }
                if (execMethodType != null) {
                    Set<String> ks = paramsMap.keySet();
                    for (Iterator<String> it = ks.iterator(); it.hasNext(); ) {
                        String key = it.next();
                        if (!isNoRecordParams(key)) {
                            String value = paramsMap.get(key)[0];
                            allParamsBuffer.append(key + ":" + value + ",");
                        }
                    }
                    String allParams = CommonTool.splitLastSymbol(allParamsBuffer.toString(), ",");
                    
                    //参数长度过长时，截取部分参数，否则插入数据库日志时报错。
                    if(allParams!=null && allParams.getBytes().length>4000){
                    	allParams=allParams.substring(0, 2000)+"...";
                    }
                    
                    //获取模块名称
                    String logName = paramsMap.get(FrameLogDefKey.LOGNAME) == null ? lowerCaseMethod : paramsMap.get(FrameLogDefKey.LOGNAME)[0];
                    //ip地址
                    String ip = CommonTool.getRemortIP(request);
                    //用户
                    Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
                    String loginUser = userMap == null ? "" : userMap.get("EMPEE_NAME");

                    log.debug("日志记录开始");
                    log.debug("日志记录 --- 执行类型  ---> " + execMethodType);
                    log.debug("日志记录 --- 执行方法  ---> " + lowerCaseMethod);
                    log.debug("日志记录 --- 执行方参数 ---> " + allParams);
                    log.debug("日志记录 --- 返回结果 ---> " + returnObj);
                    //保存日志到数据库
                    Map<String, String> sysLogMap = new HashMap<String, String>();
                    sysLogMap.put(FrameLogDefKey.LOGNAME, logName);
                    sysLogMap.put(FrameLogDefKey.IP, ip);
                    sysLogMap.put(FrameLogDefKey.LOGINUSER, loginUser);
                    sysLogMap.put(FrameLogDefKey.PARAMS, allParams);
                    sysLogMap.put(FrameLogDefKey.EXECTYPE, execMethodType);
                    sysLogMap.put(FrameLogDefKey.METHOD, lowerCaseMethod);
                    List<Map<String, String>> sysLogList = new ArrayList<Map<String, String>>();
                    sysLogList.add(sysLogMap);
                    //设置为默认数据源
                    coreService.insertObject(FrameConfigKey.INSERT_SYS_LOG, sysLogList, FrameConfigKey.DEFAULT_DATASOURCE);
                    log.debug("日志记录结束");
                }
            }
        } catch (Exception e) {
            log.warn("拦截日志出错[忽略错误] ---> ", e);
        }
    }

    /**
     * 判断不记录日志的参数Key值
     *
     * @param paramKey
     * @return
     */
    private boolean isNoRecordParams(String paramKey) {
        for (int i = 0; i < FrameLogDefKey.NO_RECORD_LOG_PARAM_KEY_LIST.size(); i++) {
            if (FrameLogDefKey.NO_RECORD_LOG_PARAM_KEY_LIST.get(i).equals(paramKey)) {
                return true;
            }
        }
        return false;
    }
}
