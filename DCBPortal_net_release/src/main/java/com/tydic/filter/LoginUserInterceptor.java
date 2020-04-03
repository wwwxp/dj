package com.tydic.filter;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.config.FrameParamsDefKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.util.Constant;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class LoginUserInterceptor {

	  /**
     * 前置通知，扩展用户参数
     * @param point
     */
    public Object extendParams(ProceedingJoinPoint point) throws Throwable {
    	// 此方法返回的是一个数组，数组中包括request以及ActionCofig等类对象
        Object[] args = point.getArgs();
        HttpServletRequest request = null;
        //获取request对象
        int k = 0;
        for (int i=0; i<args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                request = (HttpServletRequest) args[i];
                k = i;
                break;
            }
        }
        //遍历request对象中的参数
        Map<String, String[]> paramsMap;
        if (request != null) {
            //获取所有request中的参数拼装为字符串
            paramsMap = new HashMap<String, String[]>(request.getParameterMap());
            //动态将当前登录账户信息写入参数
            if (!paramsMap.containsKey(Constant.LOGIN_EMPEE_ACCT) && !paramsMap.containsKey(Constant.LOGIN_EMPEE_ID)) {
            	Map<String, String> userMap = (Map<String, String>) request.getSession().getAttribute("userMap");
            	Map<String, String> cutomerMap = (Map<String, String>) request.getSession().getAttribute("customerMap");
            	Object superAdmin = request.getSession().getAttribute("superAdmin");
            	superAdmin = (superAdmin==null||superAdmin.equals(""))?"0":superAdmin;
            	Object loginCityId = request.getSession().getAttribute("LOGIN_CITY_ID");
				loginCityId = (loginCityId==null||loginCityId.equals(""))?"''":loginCityId;
            	//测试用
				//Object loginCityId = "0769";
            	
            	if (!BlankUtil.isBlank(userMap)) {
                	String[] busParams = paramsMap.get(FrameParamsDefKey.PARAMS);
                	boolean isParams = false;
                	//如果该参数没有，则要构造
                	if (BlankUtil.isBlank(busParams)) {
                		busParams = new String[]{"{}"};
                		isParams = true;
                	} 
                	for (int i=0; i<busParams.length; i++) {
            			if(busParams[i].trim().startsWith("{")) {
            				Map<String, Object> busParamMap = (Map<String,Object>) JSON.parse(busParams[i]);
            				if(!busParamMap.containsKey(Constant.LOGIN_CITY_ID)){
	            				//ID
	            				busParamMap.put(Constant.LOGIN_CITY_ID, loginCityId);
            				}
            				if(!busParamMap.containsKey(Constant.LOGIN_EMPEE_ID)){
	            				//关联用户ID
	            				busParamMap.put(Constant.LOGIN_EMPEE_ID, userMap.get("EMPEE_ID"));
            				}
            				if(!busParamMap.containsKey(Constant.LOGIN_EMPEE_ACCT)){
	            				//关联用户名称
	            				busParamMap.put(Constant.LOGIN_EMPEE_ACCT, userMap.get("EMPEE_ACCT"));
            				}
            				if(!busParamMap.containsKey(Constant.LOGIN_SUPER_ADMIN)){
	            				//关联用户是否为超级管理员
	            				busParamMap.put(Constant.LOGIN_SUPER_ADMIN, superAdmin);
            				}
            				busParams[i] = JSON.toJSONString(busParamMap);
            			}
            		}
                	if(isParams){
                		paramsMap.put(FrameParamsDefKey.PARAMS, busParams);
                	}
                	
                	ParameterRequestWrapper requestWrapper = new ParameterRequestWrapper(request, paramsMap);
                	args[k] = requestWrapper;
            	}
            }
        }
        return point.proceed(args);
    }
}
