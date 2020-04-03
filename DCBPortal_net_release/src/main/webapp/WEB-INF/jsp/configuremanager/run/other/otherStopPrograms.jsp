<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>停止程序</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
<script type="text/javascript" src="${ctx}/js/configuremanager/run/other/otherStopPrograms.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: auto;">
	<div style="border: 1px solid #b1c3e0">
        <div style="border-bottom: 1px dashed #b1c3e0;height: 30px;padding-top: 5px;">
			<a class="Delete_Button" href="javascript:selectAll();" style="margin-left:14px;">选择未运行</a>
			<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			<span class="fred">请选择要停止的程序：</span>
			<input class="mini-combobox" id="programCb" name="programCb" valueField="PROGRAM_CODE" textField="PROGRAM_NAME" style="width:200px;" onvaluechanged="reload"/>
		</div>
		<div id="hostFitDiv" style="height: auto;min-height: 90px;overflow: hidden;">
		
		</div>
	</div>
	<div style="height:30px;line-height:30px;">
		<label>停止进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
   <a id="sumbitButton" class="mini-button"onclick="stopPrograms()">停止</a> 
   <span style="display: inline-block; width: 25px;"></span> 
   <a class="mini-button" onclick="closeWindow()">关闭</a>
</div>
</body>
</html>