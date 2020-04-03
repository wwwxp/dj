package com.tydic.service.system.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.PrivilegeService;

/**
 * 权限管理业务层实现类
 *@author : 朱伟
 */
@Service("privilegeService")
public class PrivilegeServiceImpl  implements PrivilegeService {

    @Resource
    private CoreService coreService;
    public void insertPrivilege(List<Map<String, String>> params, String dbKey) throws Exception {

        for(int i = 0 ; i< params.size() ;i++){
            Map map = params.get(i);
            validate(map,dbKey);
            int row = coreService.insertObject("privilegeMapper.addPrivilege", params, dbKey);
        }

    }

    public void updatePrivilege(List<Map<String, String>> params, String dbKey) throws Exception {
        for(int i = 0 ; i< params.size() ;i++){
            Map map = params.get(i);
            validate(map,dbKey);
            int row = coreService.updateObject("privilegeMapper.updatePrivilege", params, dbKey);
        }

    }
    private void validate(Map param,String dbKey) throws Exception{
        Map  rows = coreService.queryForObject("privilegeMapper.qeuryPrivilegeCode", param, dbKey);
        int sum = 0;
        Object sumObj=rows.get("SUM");
        
      //oracle返回BigDecimal，mysql返回Long
        if(sumObj instanceof Long){
        	sum =  ((Long)sumObj).intValue();
        }else if(sumObj instanceof BigDecimal){
        	sum =  ((BigDecimal)sumObj).intValue();
        }else{
        	throw new Exception("未知数据类型");
        }
        
        if(sum >0){
            throw new Exception("权限编码已存在");
        }
    }
}