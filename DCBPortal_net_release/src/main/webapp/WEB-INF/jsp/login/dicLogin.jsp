<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>BP_MINIUI前台管理_登录</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <!--公共的css与js -->
    <%@ include file="/public/common/common.jsp" %>
    <!-- VK Style -->
    <link rel="stylesheet" type="text/css" href="${ctx}/css/tydic_style/base.css"/>
    <script type="text/javascript" src="${ctx}/js/login/login.js"></script>
</head>
<body>
<div class="bg">
    <div style="background:url(${ctx}/css/tydic_style/images/login-top.png) no-repeat 0 0; height:165px;"></div>
    <div style="background:#f2faff url(${ctx}/css/tydic_style/images/login-mid.png) no-repeat center center; width:800px; height:300px; ">
        <div style="padding-left:320px;">
            <form id="loginForm" action="${ctx}/loginAction.do?method=login" method="post">
                <table width="280" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td>&nbsp;</td>
                        <td height="95">&nbsp;</td>
                    </tr>
                    <tr>
                        <td height="28">用&nbsp;户&nbsp;名：</td>
                        <td><input id="userName" name="userName" type="text" style="width: 140px;border:solid 1px #b5b8c8;"/></td>
                    </tr>
                    <tr>
                        <td height="28">密&nbsp;&nbsp;&nbsp;&nbsp;码：</td>
                        <td><input id="passWord" name="passWord" type="password" style="width: 140px;border:solid 1px #b5b8c8;"/></td>
                    </tr>
                    <tr>
                        <td height="32">&nbsp;</td>
                        <td align="left"><a onclick="onLoginClick" class="mini-button" style="width: 60px;">登录</a>
                            <a onclick="resetLoginForm" class="mini-button" style="width: 60px;">重置</a>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
    <div style="background: #66b4f8 url(${ctx}/css/tydic_style/images/login-bottom.png) no-repeat right bottom; line-height:31px; height:31px; margin-top:4px; color:#FFF">
        &nbsp;支持IE8以上版本浏览器或者谷歌浏览器，以获得最佳浏览体验
    </div>
</div>
<!-- 错误异常提示 -->
<input type="hidden" id="logErrorTip" value="${param.loginErrorMsg}"/>
</body>
</html>