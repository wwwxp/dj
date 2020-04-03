package com.tydic.service.system.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.UserService;

/**
 * 角色管理业务层
 * 朱伟
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(UserServiceImpl.class);
    @Resource
    private CoreService coreService;

    @Override
    public void insertEmpee(Map<String, String> param, String dbKey) throws Exception {
        List empeeRoleRelList = new ArrayList();
        validate(param,dbKey);
            String[] roleIds = ((String)param.get("ROLE_ID")).split(",");
            param.put("EMPEE_PWD",this.encodeDes(param));
            Map sequeceMap = coreService.queryForObject("userMapper.querySequece", null, dbKey);
            String empeeId = String.valueOf(sequeceMap.get("EMPEE_ID"));
            param.put("EMPEE_ID", empeeId);
            for (int i = 0; i < roleIds.length; i++) {
                Map empeeRoleRelMap = new HashMap();
                empeeRoleRelMap.put("EMPEE_ID", empeeId);
                empeeRoleRelMap.put("ROLE_ID", roleIds[i]);
                empeeRoleRelList.add(empeeRoleRelMap);
            }

        coreService.insertObject("userMapper.insertEmpee", param, FrameConfigKey.DEFAULT_DATASOURCE);
        coreService.insertObject("userMapper.insertEmpReleashion", empeeRoleRelList, FrameConfigKey.DEFAULT_DATASOURCE);
        
    }

    @Override
    public void updateEmpee(Map<String, String> param, String dbKey) throws Exception {
        List empeeRoleRelList = new ArrayList();
        param.put("UPDATE_FLAG", "update");
        validate(param,dbKey);
            param.put("EMPEE_PWD",this.encodeDes(param));
            String[] roleIds = ((String) param.get("ROLE_ID")).split(",");
            for (int i = 0; i < roleIds.length; i++) {
                Map empeeRoleRelMap = new HashMap();
                empeeRoleRelMap.put("EMPEE_ID", (String) param.get("EMPEE_ID"));
                empeeRoleRelMap.put("ROLE_ID", roleIds[i]);
                empeeRoleRelList.add(empeeRoleRelMap);
            }

        coreService.deleteObject("userMapper.updateEmpee_delRelation", param, dbKey);
        coreService.updateObject("userMapper.updateEmpee", param, dbKey);
        coreService.insertObject("userMapper.insertEmpReleashion", empeeRoleRelList, dbKey);

    }
    @Override
    public void editPassword(Map<String, String> params, String dbKey) throws Exception {
        params.put("oldPassword", DesTool.enc(params.get("oldPassword")));
        params.put("newPassword", DesTool.enc(params.get("newPassword")));
        Map result = coreService.queryForObject("userMapper.queryPasswordIsSame", params, dbKey);
        Object countObj=result.get("COUNT");
        int count=0;
        //oracle返回BigDecimal，mysql返回Long
        if(countObj instanceof Long){
        	 count =  ((Long)result.get("COUNT")).intValue();
        }else if(countObj instanceof BigDecimal){
        	  count =  ((BigDecimal)result.get("COUNT")).intValue();
        }
       
        if(count < 1){
            throw new Exception("旧密码输入不正确！");
        }
        List<Map<String,String>> paramList = new ArrayList<Map<String, String>>();
        paramList.add(params);
        coreService.updateObject("userMapper.updatePasswordUser", paramList, dbKey);
        log.debug("用户密码修改成功！");
    }
    private void validate(Map<String,String> param, String dbKey) throws Exception{
    	Map rows = coreService.queryForObject("userMapper.queryUserAcctIsRepeat", param, dbKey);
		int sum = ((Long) rows.get("SUM")).intValue();
		if (sum > 0) {
			throw new Exception("重复的登录账号,请重新输入");
		}
    }

    private String encodeDes(Map<String,String> params){
        String empeePwd = (String)params.get("EMPEE_PWD");
        return DesTool.enc(empeePwd);
    }
}