<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>跳转登录</title>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
        <!--公共的css与js -->
        <%@ include file="/public/common/common.jsp"%>
    </head>
    <body>
        <!--中间过度页面，选择登录页面 -->
        <form method="post" id="loginPageJumpForm">
            <input type="hidden" value="${loginErrorMsg}" id="loginErrorMsg" name="loginErrorMsg"/>
        </form>

        <script type="text/javascript">
            $("#loginPageJumpForm").attr("action",LoginPages[TheamFlag]);
            $("#loginPageJumpForm").submit();
        </script>
    </body>
</html>