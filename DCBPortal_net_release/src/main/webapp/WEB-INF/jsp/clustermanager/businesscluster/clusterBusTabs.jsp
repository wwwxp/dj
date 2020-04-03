<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>部署图</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/context.standalone.css" />
<!-- 给右键菜单使用 -->
<script type="text/javascript" src="${ctx}/js/common/jquery.min.js"></script>
<!-- 新使用的jquery菜单时，使用j$开头-->
<script type="text/javascript">
    var j$ = jQuery.noConflict(true);
</script>
<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
<script type="text/javascript" src="${ctx}/js/clustermanager/businesscluster/clusterBusTabs.js"></script>

</head>
<body>
<div class="mini-fit" style="overflow:hidden;" id="fitDiv">
	<div id="deploy_tabs" class="mini-tabs" activeIndex="0" style="width: 100%;height: 100%;"
		plain="false" tabAlign="left" tabPosition="top" contextMenu="#tabsMenu">
	</div>
	
	<ul id="tabsMenu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">        
        <li onclick="refreshTab">刷新数据</li>                
    </ul>
</div>
</body>
</html>