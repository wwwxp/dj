package com.tydic.web.system;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.service.system.UserService;

/**
 * 配置管理-用户管理控制器
 *@author : 朱伟
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    private static Logger log = Logger.getLogger(UserController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private CoreService coreService;

    /**
     *  新增用户
     *@author : 朱伟
     */
	@RequestMapping(value="/insertEmpee",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String insertEmpee(HttpServletRequest request){
        try {
            //获取参数
            String paramsStr = request.getParameter(FrameParamsDefKey.PARAMS);
            log.debug("UserAction参数为 ---> " + paramsStr);
            if ((paramsStr != null) && (!paramsStr.equals(""))) {
            	Map<String, String> requestMap = this.getParamsMap(request);
            	requestMap.put("EMPEE_PWD",this.decrypt(request, requestMap.get("EMPEE_PWD")));
                userService.insertEmpee(requestMap,FrameConfigKey.DEFAULT_DATASOURCE);
            }else{
                return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "参数为空！"));
            }

        } catch (Exception e) {
            log.error("添加用户失败---->" , e);
           return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }

    /**
     *  修改用户
     *@author : 朱伟
     */
	@RequestMapping(value="/updateEmpee",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String updateEmpee(HttpServletRequest request){
        try {
            Map paramsMap = (Map) JSON.Decode(request.getParameter(FrameParamsDefKey.PARAMS));
            log.debug("参数------>" + paramsMap);

            if(paramsMap != null){
            	paramsMap.put("EMPEE_PWD",this.decrypt(request, (String)paramsMap.get("EMPEE_PWD")));
                userService.updateEmpee(paramsMap,FrameConfigKey.DEFAULT_DATASOURCE);
            }else{
               return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "参数为空！"));
            }

        } catch (Exception e) {
            log.error("更新用户失败---->" ,e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }

    /**
     *  根据ID查询用户
     *@author : 朱伟
     */
	@RequestMapping(value="/queryEmpeeInfoById",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String queryEmpeeInfoById(HttpServletRequest request){
        try {
            HashMap resultData = coreService.queryForObject("userMapper.queryEmpeeById",this.getParamsMap(request),FrameConfigKey.DEFAULT_DATASOURCE);
            resultData.put("EMPEE_PWD",DesTool.dec((String) resultData.get("EMPEE_PWD")));
            return JSON.Encode(resultData);
        } catch (Exception e) {
            log.error("删除用户失败--->" , e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
    }

    /**
     * 修改密码
     *
     */
	@RequestMapping(value="/editPassword",produces = {"application/json;charset=UTF-8"})
	@ResponseBody
    public String editPassword( HttpServletRequest request) {
        try {
            //获取参数
            Map<String,String> paramsMap = this.getParamsMap(request);
            //解密
            paramsMap.put("newPassword", this.decrypt(request,paramsMap.get("newPassword")));
            paramsMap.put("configPassword", this.decrypt(request,paramsMap.get("configPassword")));
            paramsMap.put("oldPassword", this.decrypt(request,paramsMap.get("oldPassword")));

            if (paramsMap != null) {
                String newPassword = paramsMap.get("newPassword");
                String configPassword = paramsMap.get("configPassword");
                if(newPassword != null || newPassword.length() > 5) {
                    if(!newPassword.equals(configPassword)){
                        return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "输入的密码不一致"));
                    }
                    Map<String, Object> userMap = (Map<String, Object>)request.getSession().getAttribute("userMap");
                    paramsMap.put("userName",(String)userMap.get("EMPEE_ACCT"));
                    userService.editPassword(paramsMap,FrameConfigKey.DEFAULT_DATASOURCE);
                    log.debug("修改成功！");
                }else{
                   return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "新密码输入有误！"));
                }
            }else{
                return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, "参数为空！"));
            }
        } catch (Exception e) {
            log.error("修改失败" ,e);
            return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR, e.getMessage()));
        }
        return null;
    }

}
