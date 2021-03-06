<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>启停管理</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/context.standalone.css" />
<!-- 右键使用jq -->
<script type="text/javascript" src="${ctx}/js/common/jquery.min.js"></script>
<!-- 新使用的jquery菜单时，使用j$开头-->
<script type="text/javascript">
    var j$ = jQuery.noConflict(true);
</script>
<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
<script type="text/javascript" src="${ctx}/js/configuremanager/startManage.js"></script>

</head>
<body>
<div class="mini-fit" style="overflow:auto;" id="fitDiv">
	
	<div id="deploy_tabs" class="mini-tabs" activeIndex="0"
		plain="false" tabAlign="left" tabPosition="top" onactivechanged="loadPage">
		<div title="组件管理" id="componentTab" name="componentTab" showCloseButton="false">
        	
    	</div>
    	<div title="Billing" id="billingTab" name="billingTab" showCloseButton="false">
        	
    	</div>
	</div>
	<div id="fitDiv_cont">
	
	</div>
	
	<div style="display: none;">
    	<form id="termialForm" name="termialForm" method="post" target="_blank">
    		<input type="hidden" id="termialHost" name="termialHost"/>
    		<input type="hidden" id="logName" name="logName" value="终端操作"/>
    	</form>
    </div>
</div>

</body>
</html>