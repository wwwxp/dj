package com.tydic.service.system;

import java.util.List;
import java.util.Map;

/**
 * 权限管理业务层接口
 *@author : 朱伟
 */
public interface PrivilegeService {

    /**
     * 增加权限
     *@author : 朱伟
     */
    public void insertPrivilege(List<Map<String, String>> params, String dbKey) throws Exception;
    /**
     * 修改权限
     *@author : 朱伟
     */
    public void updatePrivilege(List<Map<String, String>> params, String dbKey) throws Exception;
}
