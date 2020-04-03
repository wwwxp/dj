package com.tydic.web.login;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tydic.bp.common.utils.config.FrameConfigKey;
import com.tydic.bp.common.utils.config.FrameLogDefKey;
import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.CommonTool;
import com.tydic.bp.common.utils.tools.DesTool;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.bp.core.service.CoreService;
import com.tydic.util.SessionUtil;

@Controller
@RequestMapping("/login")
public class LoginController extends BaseController {
    @Autowired
    private CoreService coreService;
    /**
     * 日志对象
     */
    private static Logger log = Logger.getLogger(LoginController.class);
    /**
     * 登录失败
     */
    private String LOGIN_FAIL = "forward:/index.jsp";
    /**
     * 登录成功
     */
    private String LOGIN_SUCCESS="main";

    /**
     * 登录  必须是ＰＯＳＴ请求
     */
    @RequestMapping(method = RequestMethod.POST)
    public String login(Model model, HttpServletRequest request) {
        if(request.getMethod().toLowerCase().equals("get")){
            return LOGIN_FAIL;
        }

        try {
            log.debug("登录controller，登录开始");
            Map<String,String> loginParamsMap;
            try{
                loginParamsMap= getParams("userName,passWord", request);
            }catch (Exception e){
                return LOGIN_FAIL;
            }
            String passWord = (String) loginParamsMap.get("passWord");
            //解密
            passWord=this.decrypt(request, passWord);
            if ((passWord != null) && (!"".equals(passWord))) {
                loginParamsMap.put("passWord", DesTool.enc(passWord));
            }
            log.debug("登录controller，登录参数为 ---> " + loginParamsMap);

            List<HashMap<String, String>> userList = coreService.queryForList("login.queryLoginFail", loginParamsMap, FrameConfigKey.DEFAULT_DATASOURCE);
            if(!BlankUtil.isBlank(userList)){
                HashMap<String, String> user = userList.get(0);
                Map<String,String> loginConfgiMap = SessionUtil.getConfigByGroupCode("LOGIN");
                String MaxFail = loginConfgiMap.get("LOGIN_FAIL_MAX");
                String timeoutFail = loginConfgiMap.get("LOGIN_FAIL_TIMEOUT");
                SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date fail_time =fmt.parse(user.get("FAIL_TIME") == null ? "0000-00-00 00:00:00":user.get("FAIL_TIME"));
                Date now_time =fmt.parse(user.get("NOW_TIME"));
                if(now_time.getTime()-fail_time.getTime() <Integer.parseInt(timeoutFail)*1000*60 ){
                    if(user.get("FAIL_NUM") != null && !((Long)(Object)user.get("FAIL_NUM")).equals("")){
                        if(Integer.parseInt(MaxFail) - Integer.parseInt((Long)(Object)user.get("FAIL_NUM")+"") <= 0){
                            request.setAttribute("loginErrorMsg", "连续登录失败次数超过"+MaxFail+"次，账户已锁定，请在"+timeoutFail+"分钟之后再登录！");
                            return LOGIN_FAIL;

                        }
                    }
                }
            }

            Map userMap = coreService.queryForObject("login.userLogin", loginParamsMap, FrameConfigKey.DEFAULT_DATASOURCE);
            if (userMap == null || userMap.isEmpty()) {
                coreService.updateObject("login.updateLoginFail", loginParamsMap, FrameConfigKey.DEFAULT_DATASOURCE);
                log.debug("登录controller，用户名或者密码错误... ");
                request.setAttribute("loginErrorMsg", "用户名或者密码错误");
                return LOGIN_FAIL;
            }else{
                coreService.updateObject("login.resetLogin", loginParamsMap, FrameConfigKey.DEFAULT_DATASOURCE);

            }
            request.getSession().setAttribute("userMap", userMap);
            String userName = (String) userMap.get("EMPEE_NAME");
            //recordLoginLog(request, userName);
            log.debug("登录controller，登录成功... ");
        } catch (Exception e) {
            log.error("登录controller，登陆异常 ---> ", e);
            request.setAttribute("loginErrorMsg", "系统错误：登陆异常");
            return LOGIN_FAIL;
        }
        log.debug("登录controller，登录结束 ");
        return LOGIN_SUCCESS;
    }

    /**
     * 获取权限
     *
     */
    @RequestMapping(value="/getPrivilege",produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public List getPrivilege(HttpServletRequest request)
            throws Exception {
        try {
            log.debug("登录Action，获取菜单开始");
            List empMenuList = new ArrayList();
            Map empeeMap = null;
            if (request.getSession() != null && request.getSession().getAttribute("userMap") != null) {
                empeeMap = (Map) request.getSession().getAttribute("userMap");
                Map params = new HashMap();
                String empId = String.valueOf(empeeMap.get("EMPEE_ID"));
                params.put("EMPEE_ID", empId);
                if(empId.equals("1")){//如果超级用户登录，给一个超级标识，查询权限用
                    params.put("SUPER", "1");
                }
                log.debug("登录Action，获取菜单参数 ---> " + params);
                //如果权限列表存在session中，则取session
                if( request.getSession().getAttribute("empeePrivilege") != null){
                    empMenuList =  (List)request.getSession().getAttribute("empeePrivilege");
                }else {
                    empMenuList = coreService.queryForList("login.queryEmpPrivilege", params, FrameConfigKey.DEFAULT_DATASOURCE);
                    request.getSession().setAttribute("empeePrivilege", empMenuList);
                }
                log.debug("登录Action，获取权限成功 ---> " + empMenuList);

                return empMenuList;
            } else {
                //登录异常，返回异常信息
                log.error("登录Action，登录用户session为空--->");
                throw new RuntimeException("登录Action，登录用户session为空，无法获取菜单信息");
            }
        } catch (Exception e) {
            log.error("登录Action，菜单加载异常--->" , e);
            throw new RuntimeException("登录Action，菜单加载异常");
        }

    }

    /**
     * 退出，注销
     */
    @RequestMapping(value="/logonOut")
    public String logonOut(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        log.debug("登录Action，注销开始");
        request.getSession().invalidate();
        log.debug("登录Action，注销结束");
        return LOGIN_FAIL;
    }

    private void recordLoginLog(HttpServletRequest request, String loginUser) {
        //保存登录日志到数据库
        try {
            log.debug("记录登录日志");
            Map<String, String> sysLogMap = new HashMap<String, String>();
            sysLogMap.put(FrameLogDefKey.LOGNAME, "登录");
            sysLogMap.put(FrameLogDefKey.IP, CommonTool.getRemortIP(request));
            sysLogMap.put(FrameLogDefKey.LOGINUSER, loginUser);
            sysLogMap.put(FrameLogDefKey.PARAMS, "");
            sysLogMap.put(FrameLogDefKey.EXECTYPE, FrameLogDefKey.LOGIN);
            sysLogMap.put(FrameLogDefKey.METHOD, FrameLogDefKey.LOGIN);
            List<Map<String, String>> sysLogList = new ArrayList<Map<String, String>>();
            sysLogList.add(sysLogMap);
            //设置为默认数据源
            coreService.insertObject(FrameConfigKey.INSERT_SYS_LOG, sysLogList, FrameConfigKey.DEFAULT_DATASOURCE);
        } catch (Exception e) {
            log.warn("登录日志记录失败[忽略此错误] ---> ", e);
        }
    }
}
