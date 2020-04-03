package com.tydic.service.system.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.RoleService;

/**
 * 角色管理业务层
 *@author : 朱伟
 */
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Resource
    private CoreService coreService;

    public void deleteRole(List<Map<String, String>> params, String dbKey) throws Exception{
        coreService.deleteObject("roleMapper.delEmpeeRole", params, dbKey);
        coreService.deleteObject("roleMapper.delRolePrivilege", params, dbKey);
        coreService.deleteObject("roleMapper.delRole", params, dbKey);
    }
}