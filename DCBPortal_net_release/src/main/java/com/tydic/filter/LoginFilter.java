package com.tydic.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ControllerTools;
import com.tydic.bp.common.utils.web.ResponseObj;



/**
 * 用户登录过滤器
 * @author tangdl
 *
 */
public class LoginFilter implements Filter {
    /**
     * log4j对象
     */
    private static Logger log = Logger.getLogger(LoginFilter.class);
    /**
     * 白名单Key值
     */
    private static final String WHITE_LIST = "whiteList";
    /**
     * 白名单
     */
    private List<String> whiteList = new ArrayList<String>();

    /**
     * 系统导航页，登录页面Key值
     */
    private String INDEX_PAGEKEY="indexPage";
    /**
     * 系统导航页，登录页面
     */
    private String indexPage="index.jsp";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("初始化登录过滤器");
        this.indexPage=filterConfig.getInitParameter(INDEX_PAGEKEY);
        log.info("登录过滤器，系统导航页，登录页面为 ---> "+this.indexPage);
        String whiteListString = filterConfig.getInitParameter(WHITE_LIST);
        log.info("登录过滤器，白名单 ---> "+whiteListString.replaceAll("\n","").trim());
        if(StringUtils.isNotBlank(whiteListString)){
            if(StringUtils.indexOf(whiteListString,",")!=-1){
                for(String no : whiteListString.split(",")){
                    this.whiteList.add(StringUtils.trimToEmpty(no));
                }
            }else{
                this.whiteList.add(whiteListString);
            }
        }
        log.info("登录过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestUrl=request.getRequestURI();
        if(checkWhiteList(requestUrl)){
            filterChain.doFilter(servletRequest, servletResponse);
            return ;
        }
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);
        Map<String, Object> rsMap =null;
        
//      //开发用，跳过登录 start
 /*       if(session == null || session.getAttribute("userMap") == null ){
        	Map loginParamsMap = new HashMap();
            loginParamsMap.put("userName", "admin");
            loginParamsMap.put("passWord", "F29365EA241831F7");
            CoreService ls=(CoreService)SpringContextUtil.getBean("coreService");
            Map userMap = ls.queryForObject("login.userLogin", loginParamsMap, FrameConfigKey.DEFAULT_DATASOURCE);
            session=request.getSession();
            
            session.setAttribute("userMap", userMap);
        }*/
//       //开发用，跳过登录 end
        if(session!=null){
            rsMap = (Map<String, Object>) session.getAttribute("userMap");
            if (rsMap == null) {
                log.debug("登录过滤器，用户session失效[用户信息为空]，跳转登录页面");
                sessionLose(request,response);
                return ;
            }
        }else{
            log.debug("登录过滤器，用户session失效，跳转登录页面");
            sessionLose(request,response);
            return ;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 判断是否为白名单中的路径
     * @param path
     * @return
     */
    private boolean checkWhiteList(String path) {
        if (whiteList == null || whiteList.size() <= 0){
            return false;
        }else if(whiteList.equals("*")){
            //*号代表所有的都放行
            return true;
        }
        for (String s : whiteList) {
            if (path.indexOf(s) > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * session失效后的处理
     * @param request
     * @param response
     * @throws IOException
     */
    private void sessionLose(HttpServletRequest request,HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        String goPage =  request.getContextPath()+"/"+this.indexPage;
        if(ControllerTools.isAjax(request)){
            response.getWriter().print(JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR_SESSION_TIMEOUT, goPage)));
        }else{
             // 跳转到登录页
            response.getWriter().print("<script>top.location='" + goPage+ "';</script>");
        }
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
