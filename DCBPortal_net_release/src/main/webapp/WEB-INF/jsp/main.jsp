<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!--公共的css与js -->
    <%@ include file="/public/common/common.jsp"%>
    <title>跳转主页</title>
</head>

<body>
<!--中间过度页面，选择主页面 -->
<form method="post" id="mainPageJumpForm"></form>
<!--中间过度页面，跳转主页面 -->
<script type="text/javascript">
    $("#mainPageJumpForm").attr("action",MainPages[TheamFlag]);
    $("#mainPageJumpForm").submit();
</script>
</body>
</html>
