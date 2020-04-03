package com.tydic.service.system;

import java.util.List;
import java.util.Map;

/**
 * 角色管理业务层接口
 *@author : 朱伟
 */
public interface RoleService {
    /**
     * 删除角色
     *@author : 朱伟
     */
    public void deleteRole(List<Map<String, String>> params, String dbKey) throws Exception;

}
