<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>部署主机弹出框</title>
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
<script type="text/javascript" src="${ctx}/js/configuremanager/deploy/deployhostnotin.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: auto;">
	<div style="border: 1px solid #b1c3e0">
        <div style="border-bottom: 1px dashed #b1c3e0;">
			<a class="Delete_Button" href="javascript:selectAll();" style="margin-left:14px;">全选</a>
			<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			
			<label class="tips_label_start">：已部署</label>
			<div class="tips_div_start" style="background-color: #5cb85c;" title="当前主机已部署" ></div>
			<label class="tips_label_start">：未部署</label>
			<div class="tips_div_start" style="background-color: gray;" title="当前主机未部署" ></div>
		</div>
		<div id="hostFitDiv" style="height: auto;min-height: 55px;overflow: hidden;">
		
		</div>
	</div>
	<div style="padding:5px 0px;overflow-y:auto;overflow-x:hidden;height: auto;">
		<div id="inputForm" style="height:100%;">
			<table id="paramsTable" class="formTable6" style="width:100%;min-height:5px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80"/>
					<col />
					<col width="80"/>
					<col />
				</colgroup>
				<tbody id="paramsInfo">
				</tbody>
			</table>
		</div>
	</div>
	<div style="border-top: 1px dashed #C7CBD1;margin-top:5px;height:30px;line-height:30px;">
		<label >部署进度说明:</label>
   	</div>
   	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
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