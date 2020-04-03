package com.tydic.service.system;

import java.util.List;
import java.util.Map;

/**
 * 业务角色配置管理业务层接口
 *@author : 田玉姣
 */
public interface RoleConfigService {
    /**
     * 删除角色
     *@author : 田玉姣
     */
    public void deleteRole(List<Map<String, String>> params, String dbKey) throws Exception;
    
    /**
     * 根据用户id查询角色
     *@author  
     */
    public Map<String, Object> queryRole(Map<String, Object> params,int pageSize,int pageIndex, String dbKey) throws Exception;

}
