<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>新增部署主机弹出框</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/context.standalone.css" />
<!-- 右键使用jq -->
<script type="text/javascript" src="${ctx}/js/common/jquery.min.js"></script>
<!-- 新使用的jquery菜单时，使用j$开头-->
<script type="text/javascript">
    var j$ = jQuery.noConflict(true);
</script>
<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
<script type="text/javascript" src="${ctx}/js/clustermanager/delHostPartition.js"></script>
</head>
<body>
	<div id="FitDiv" class="mini-fit p5" style="overflow: auto;">
		<div style="height:99%;overflow: auto;border: 1px solid #b1c3e0;">
			<div style="background: #EAF1F7; height:28px;margin-top: 2px;">
				<a class="Delete_Button" href="javascript:selectAll()" style="margin-left:14px;">全选</a>
				<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			</div>
			<div id="hostFitDiv" >
				
			</div>
		</div>
	</div>
	
	<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom:8px;"
	    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
		   <a class="mini-button"onclick="chooseDeploy()">删除</a>
		   <span style="display: inline-block; width: 25px;"></span> 
		   <a class="mini-button" onclick="closeWindow()">取消</a>
	</div>
	
	<!-- 终端操作隐藏表单 -->
	<div style="display: none;">
	   	<form id="termialForm" name="termialForm" method="post" target="_blank">
	   		<input type="hidden" id="termialHost" name="termialHost"/>
	   		<input type="hidden" id="logName" name="logName" value="终端操作"/>
	   	</form>
	</div>
</body>
</html>