package com.tydic.service.system;

import org.aspectj.lang.JoinPoint;

/**
 * 日志记录接口，主要用于业务操作的日志操作
 * @author tangdl
 */
public interface ILogService {
    /**
     * 无参的日志方法
     */
    public void log();
    /**
     * 有参的日志方法
     */
    public void logArg(JoinPoint point);
    /**
     * 有参有返回值的方法
     */
    public void logArgAndReturn(JoinPoint point,Object returnObj);
}


