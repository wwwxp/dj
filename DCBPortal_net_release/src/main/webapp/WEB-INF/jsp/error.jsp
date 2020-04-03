<%--
  Created by IntelliJ IDEA.
  User: dong
  Date: 2014/12/25
  Time: 9:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title></title>
</head>
<body>
<% response.getWriter().println("错误消息: " + exception.getMessage());  %>请联系管理员！
</body>
</html>
