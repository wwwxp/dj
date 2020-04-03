<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/public/common/common.jsp"%>
    <script src="${ctx}/js/configuremanager/run/programView.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>配置文件</title>
</head>

<body>
<div class="mini-fit" style="padding: 5px;">
	<input id="CONTENT" name="CONTENT" required="false" class="mini-textarea" style="width:99.99%;height:99.99%;" enabled="true"/>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">确定</a> 
    </div>
</body>
</html>
