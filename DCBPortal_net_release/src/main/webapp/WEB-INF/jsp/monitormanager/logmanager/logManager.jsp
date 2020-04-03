<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ct" uri="http://jstorm.alibaba.com/jsp/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>日志管理</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/logmanager/logManager.js"></script>
</head>
<body>
	<div style="padding:10px;" class="monitor" >
		<div style="height:28px;line-height:28px;font-size:14px;font-weight:bold;padding-bottom:5px;margin-bottom:0px;">当前位置：
			<span id="logDir">dir</span>
			<span id="logHost" style="margin-left:15px;">[host]</span>
			<span><a class="mini-button mini-button-green"  style="float:right;" onclick="back()" plain="false">返回</a></span>
		</div>
		<div class="mini-panel" title="目录信息列表" style="width:100%;" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="dirsGrid" class="mini-datagrid" style="width:100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="fileName" width="30" headerAlign="center" align="center" renderer="dirsRenderer">目录名称</div>
						<div field="modifyTime" width="30" headerAlign="center" align="center" renderer="timeRederer">修改时间</div>
						<div field="size" width="30" headerAlign="center" align="center" renderer="fileSizeRenderer">文件大小</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="文件信息列表" style="width:100%;" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="fileGrid" class="mini-datagrid" style="width:100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="fileName" width="30" headerAlign="center" align="center" renderer="filesRenderer">文件名称</div>
						<div field="modifyTime" width="20" headerAlign="center" align="center" renderer="timeRederer" ">修改时间</div>
						<div field="size" width="20" headerAlign="center" align="center" renderer="fileSizeRenderer">文件大小</div>
						<div field="oprate" width="20" headerAlign="center" align="center" renderer="oprateRenderer">操作</div>
					</div>
				</div>
			</div>
		</div>
	
	</div>
</body>
</html>
