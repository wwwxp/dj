<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>DCBP云计费平台_登录</title>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
        <!--公共的css与js -->
        <%@ include file="/public/common/common.jsp"%>
        <!-- VK Style -->
        <link rel="stylesheet" type="text/css" href="${ctx}/css/vk_style/css/common.css" />
        <script type="text/javascript" src="${ctx}/js/login/login.js"></script>
    </head>
    <body class="login_bg">
        <!--头部 begin-->
        <div class="login_head">
            <img src="${ctx}/css/vk_style/images/login_logo.gif" />
        </div>
        <!--登录 begin-->
        <div class="login_yhdl">用户登录</div>
        <form id="loginForm" action="${ctx}/login" method="post">
            <div class="loginK">
                <ul class="login">
                <input type="text" name="notautosubmit" style="display:none"/>
                    <li>
                        <span><img src="${ctx}/css/vk_style/images/login_ico1.gif" /></span><input id="userName" name="userName" type="text" class="login_inp" />
                    </li>
                    <li>
                        <span><img src="${ctx}/css/vk_style/images/login_ico2.gif" /></span><input id="replacePassWord" name="replacePassWord" type="text" class="login_inp" /><input id="passWord" name="passWord" type="password" style="display: none;" class="login_inp" />
                        <br />
                    </li>
                    <!-- <li><a href="#">忘记密码？</a><div class="clear"></div></li> -->
                    <li style="text-align:center"><button id="login_btn" class="login_btn" >登 录</button></li>
                </ul>
            </div>
            <input type="hidden" name="theamCfg" value="vk"/>
        </form>
        <!--版权所有 begin-->
        <div class="login_bq">版权所有：深圳天源迪科信息技术股份有限公司，推荐IE8以上或者谷歌浏览器，以获得最佳浏览体验</div>
        <!-- 错误异常提示 -->
        <input type="hidden" id="logErrorTip" value="${param.loginErrorMsg}"/>
    </body>
</html>