<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@include file="/public/common/common.jsp"%>
    <script src="${ctx}/js/configuremanager/run/runResult.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>业务启停结果输出</title>
</head>

<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: hidden;">
 	<div id="deployTextarea" name="deployTextarea" style="height:100%;border: 1px solid #b1c3e0;overflow:auto;padding: 0px 5px;" ></div>
 	<div style="height: 5px;"></div>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
   <a class="mini-button" onclick="javascript:closeWindow()">关闭</a>
</div>
</body>
</html>
