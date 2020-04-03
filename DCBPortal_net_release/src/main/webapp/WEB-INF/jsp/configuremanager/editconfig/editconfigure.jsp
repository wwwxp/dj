<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>配置修改</title>
<%@ include file="/public/common/common.jsp"%>
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/editconfig/editconfigure.js"></script>
</head>
<body>
<div class="mini-fit" id="fitDiv">
	<div id="deploy_tabs" class="mini-tabs" style="margin-top:5px;height:100%;"
		plain="false" tabAlign="left" tabPosition="top">
		<%-- <div title="业务配置" id="businessconfigure"  url="${ctx}/jsp/configuremanager/editconfig/businessconfigure"></div> --%>
		<div title="组件配置" id="platformconfigure" url="${ctx}/jsp/configuremanager/editconfig/platformconfigure"></div>
		<div title="脚本配置" id="scriptconfigure" url="${ctx}/jsp/configuremanager/editconfig/scriptconfigure"></div>
	</div>
</div>
</body>
</html>