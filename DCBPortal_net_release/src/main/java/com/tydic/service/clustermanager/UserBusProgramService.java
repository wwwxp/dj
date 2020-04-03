package com.tydic.service.clustermanager;

import com.tydic.bean.UserPrivilegeNode;
import com.tydic.common.BusException;
import com.tydic.util.ftp.FileRecord;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Auther: Yuanh
 * Date: 2018-07-09 15:57
 * Description:
 */
public interface UserBusProgramService {

    /**
     * 查询业务程序列表
     * @return
     */
    public List<UserPrivilegeNode> queryUserProgramPrivilegeList(Map<String, Object> params, String dbKey);

    /**
     * 添加用户权限
     * @param params
     * @param dbKey
     */
    public void addUserBusProgramList(Map<String, Object> params, String dbKey);


    /**
     * 查询业务程序启停列表
     * @return
     */
    public Map<String, Object> queryUserProgramStartStopList(Map<String, Object> params, String dbKey, HttpServletRequest request);

    /**
     * 添加业务程序启停功能
     * @return
     */
    public Map<String, Object> addRunStopProgram(Map<String, Object> params, String dbKey) throws BusException;


    /**
     * 查询业务程序启停列表
     * @return
     */
    public Map<String, Object> checkRunStopProgram(List<Map<String, Object>> params, String dbKey) throws BusException;
    
    /**
     * 查询包括组件和业务的配置文件列表
     * @return
     */
    public List<FileRecord> queryCfgFileList(Map<String, Object> params, String dbKey) throws Exception;
    
    /**
     * 添加业务的配置文件列表
     * @param params
     * @param dbKey
     */
    public void addCfgFileList(Map<String, Object> params, String dbKey);

    
}
