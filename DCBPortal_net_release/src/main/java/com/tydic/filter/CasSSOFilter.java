package com.tydic.filter;

import com.alibaba.fastjson.JSON;
import com.tydic.bp.common.utils.tools.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CAS集成SSO
 */
public class CasSSOFilter implements Filter {
	private static Logger log = Logger.getLogger(CasSSOFilter.class);

	/**
	 * 白名单
	 */
	private List<String> whiteList = new ArrayList<String>();
	
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		log.info("begin Pricing CasSsoFilter ......");
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;

		if(request.getSession().getAttribute("userMap") == null){
			//SSO登陆标志
			request.getSession().setAttribute("ssoFlag", "true");

			//是否已经登录
			AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
			Map<String, Object> userAttribute = principal.getAttributes();
			Object systemUserDetail = userAttribute.get("SystemUserDetail");
			log.info("SSO INFO ---> " + systemUserDetail);
			if (systemUserDetail != null) {
				Map<String, Object> systemUserMap = (Map<String, Object>) JSON.parse(StringTool.object2String(systemUserDetail));
				addUserMap(systemUserMap, request);
			}
		}else{
			log.debug("cass get session  userMap---------->  "+request.getSession().getAttribute("userMap"));
		}
		log.info("end Agent CasSsoFilter ......");
		chain.doFilter(request, response);
	}

	/**
	 * 添加用户信息
	 * SELECT  EMPEE_ID,EMPEE_LEVEL,EMPEE_NAME, EMPEE_CODE,LATN_ID,EMPEE_ACCT, EMPEE_PWD,USER_TYPE FROM tb_bp_sys_empee
	 * @param systemUserMap
	 */
	private void addUserMap(Map<String, Object> systemUserMap, HttpServletRequest request) {
		log.info("CRM SSO, systemUserMap ---> " + systemUserMap);
		Object authSystemUser = systemUserMap.get("authSystemUser");
		if (authSystemUser != null) {
			Map<String, Object> authUserMap = (Map<String, Object>) authSystemUser;
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("EMPEE_ID", authUserMap.get("sysUserId"));
			userMap.put("EMPEE_LEVEL", authUserMap.get("isMainUser"));
			userMap.put("EMPEE_NAME", authUserMap.get("staffName"));
			userMap.put("EMPEE_CODE", authUserMap.get("sysUserCode"));
			userMap.put("LATN_ID", authUserMap.get("latnId"));
			userMap.put("EMPEE_ACCT", authUserMap.get("sysUserCode"));
			userMap.put("EMPEE_PWD", systemUserMap.get("password"));
			userMap.put("USER_TYPE", authUserMap.get("staffType"));

			request.getSession().setAttribute("userMap", userMap);
			log.info("CRM SSO, addUser ---> " + userMap);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String whiteListString =  StringUtils.defaultIfEmpty(config.getInitParameter("exitSuffixs"), "");
		log.info("sso filter white list : " + whiteListString);
		if (StringUtils.isNotBlank(whiteListString)) {
			if (StringUtils.isNotBlank(whiteListString)) {
				if (StringUtils.indexOf(whiteListString, ",") != -1) {
					for (String no : whiteListString.split(",")) {
						this.whiteList.add(StringUtils.trimToEmpty(no));
					}
				} else {
					this.whiteList.add(whiteListString);
				}
			}
		}
	}

}
