package com.tydic.service.system.impl;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.RoleConfigService;
import com.tydic.util.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务角色配置管理业务层
 *@author : 田玉姣
 */
@Service("roleConfigService")
public class RoleConfigServiceImpl implements RoleConfigService {

    @Resource
    private CoreService coreService;

    public void deleteRole(List<Map<String, String>> params, String dbKey) throws Exception{
        coreService.deleteObject("roleMapper.delEmpeeRole", params, dbKey);
        coreService.deleteObject("roleMapper.delRoleBusConfig", params, dbKey);
        coreService.deleteObject("roleMapper.delRoleBusProgram", params, dbKey);
        coreService.deleteObject("roleMapper.delRole", params, dbKey);
    }

	@Override
	public Map<String, Object> queryRole(Map<String, Object> params,int pageSize,int pageIndex, String dbKey) throws Exception {

		Map<String, Object> dataMap = coreService.queryPageList2New("userRoleConfigMapper.queryEmpee", pageSize, pageIndex, params, FrameConfigKey.SSO_DATASOURCE);
        
	    List<Map<String,Object>> empeeList = (List<Map<String,Object>>)dataMap.get(FrameParamsDefKey.DATA);
	  
	    if(empeeList !=null && !empeeList.isEmpty()){
		  
		  Map<String,Object> queryRoleMap = new HashMap<String,Object>();
		  queryRoleMap.put("list", empeeList);
		  List<HashMap<String,Object>> roleList = coreService.queryForList2New("userRoleConfigMapper.queryRoleList", queryRoleMap, dbKey);
		  if(roleList !=null && !roleList.isEmpty()){
			  for(int i= 0 ; i < empeeList.size() ;i++){
				  Map<String,Object> empeeMap = empeeList.get(i);
				  for(int j = 0 ;j < roleList.size() ;j++){
					  Map<String,Object> roleMap = roleList.get(j);
					  if(StringUtils.equals(StringTool.object2String(empeeMap.get("EMPEE_ID")),
							  StringTool.object2String(roleMap.get("EMPEE_ID")))){
						  if(empeeMap.containsKey("ROLES")){
							  empeeMap.put("ROLES", empeeMap.get("ROLES") +"," + roleMap.get("ROLE_NAME"));
						  }else{
							  empeeMap.put("ROLES",  roleMap.get("ROLE_NAME"));

						  }
					  }
				  }
			  }
		  }
		  
	  }
	    return dataMap;
		
	}
}