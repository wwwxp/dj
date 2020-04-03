package com.tydic.service.system;

import java.util.Map;
/**
 * 用户管理业务层接口
 *@author : 朱伟
 */
public interface UserService {

    /**
     * 增加用户
     * @author : 朱伟
     */
    public void insertEmpee(Map<String, String> param, String dbKey) throws Exception;

    /**
     * 修改用户
     * @author : 朱伟
     */
    public void updateEmpee(Map<String, String> param, String dbKey) throws Exception;


    /**
     * 修改密码
     *@author : 朱伟
     */
    public void editPassword(Map<String, String> params, String dbKey) throws Exception;


}
