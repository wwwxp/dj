package com.tydic.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.sso.client.config.SSOConfigKey;
import org.apache.log4j.Logger;

import com.tydic.bp.sso.client.filter.SSOAbstractFilter;


/**
 * 用户登录过滤器
 * @author tangdl
 *
 */
public class SSOFilter extends SSOAbstractFilter{
    /**
     * log4j对象
     */
    private static Logger log = Logger.getLogger(SSOFilter.class);

    @Override
    protected void initClient(FilterConfig filterConfig) {

    }

    @Override
    protected void loadAuthInfo(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, Map<String, Object> authInfo) {
        log.info("客户端登录成功 --->");
        //request.getSession().setAttribute("userMap", userMap);
        paramHttpServletRequest.getSession().setAttribute(SSOConfigKey.USER_MAP, authInfo.get(SSOConfigKey.USER_MAP));
        List<JSONObject> userRoleList =  (List<JSONObject>)authInfo.get(SSOConfigKey.USER_ROLE);
        paramHttpServletRequest.getSession().setAttribute(SSOConfigKey.USER_ROLE, userRoleList);
        String empeeId = String.valueOf(((Map)(authInfo.get(SSOConfigKey.USER_MAP))).get("EMPEE_ID"));
        if("1".equals(empeeId)){
            paramHttpServletRequest.getSession().setAttribute("superAdmin", 1);
        }else{
            paramHttpServletRequest.getSession().setAttribute("superAdmin", 0);
            paramHttpServletRequest.getSession().setAttribute("LOGIN_EMPEE_ID", empeeId);
        }
        List<String> cityList = new ArrayList<String>();
        if (!BlankUtil.isBlank(userRoleList)) {
            for (JSONObject roleMap : userRoleList) {
                cityList.add(String.valueOf(roleMap.get("LATN_ID")));
            }
        }
        String cityId = "";
        for(String tmpCity :cityList){
            cityId += "'"+tmpCity+"',";
        }
        if(cityId.lastIndexOf(",")>-1){
            cityId = cityId.substring(0,cityId.length()-1);
        }
        cityId=cityId.equals("")?"''":cityId;
        log.debug("LOGIN_CITY_ID ----------------> "+cityId);
        paramHttpServletRequest.getSession().setAttribute("LOGIN_CITY_ID",cityId );
    }
}
