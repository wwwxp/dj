<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>部署主机弹出框</title>
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
<script type="text/javascript" src="${ctx}/js/configuremanager/deploy/busProgramsdeploy.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: auto;">
	<div style="border: 1px solid #b1c3e0;">
        <div style="border-bottom: 1px dashed #b1c3e0;padding-top: 2px;padding-bottom: 2px;">
			<a class="Delete_Button" href="javascript:selectAllVersionHost();" style="margin-left:14px;">全选</a>
			<a class="Delete_Button" href="javascript:selectAll();">当前版本未部署</a>
			<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			<span class="fred">请选择要部署的版本：</span>
			<input class="mini-combobox" id="versionList" name="versionList" style="width: 260px;"
				valueField="ID" textField="VERSION_TEXT" onvaluechanged="reloadVersionData"/>
			
			<label class="tips_label_start" style="margin-top: 3px;">：已部署</label>
			<div class="tips_div_start" style="background-color: #5cb85c;margin-top: 6px;" title="当前主机已部署该版本程序" ></div>
			<label class="tips_label_start" style="margin-top: 3px;">：未部署</label>
			<div class="tips_div_start" style="background-color: gray;margin-top: 6px;" title="当前主机未部署该版本程序" ></div>
		</div>
		<div id="hostFitDiv" style="height: auto;min-height: 90px;overflow: hidden;">
		
		</div>
	</div>
	<div style="height:30px;line-height:30px;">
		<label>部署进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #C7CBD1;overflow:auto;" ></div>
 	<div style="height: 5px;"></div>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
   <a id="sumbitButton" class="mini-button"onclick="chooseDeploy()">部署</a> 
   <span style="display: inline-block; width: 25px;"></span> 
   <a class="mini-button" onclick="close()">关闭</a>
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