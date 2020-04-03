<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>业务集群划分</title>
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
<script type="text/javascript" src="${ctx}/js/clustermanager/businesscluster/clusterBusPartition.js"></script>

</head>
<body>
<div class="mini-fit" style="overflow:auto;height: 100%;width: 100%;" id="fitDiv">
	<!-- <div id="deploy_tabs" class="mini-tabs" activeIndex="0" contextMenu="#tabsMenu"
		plain="false" tabAlign="left" tabPosition="top" onactivechanged="loadPage">
	</div>
	<ul id="tabsMenu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">        
        <li onclick="refreshTab">刷新数据</li>                
    </ul> -->
    
	<div id="fitDiv_cont">
	
	</div>
	
	<!-- 用来显示终端操作 -->
	<div style="display: none;">
    	<form id="termialForm" name="termialForm" method="post" target="_blank">
    		<input type="hidden" id="termialHost" name="termialHost"/>
    		<input type="hidden" id="logName" name="logName" value="终端操作"/>
    	</form>
    </div>
</div>

</body>
</html>