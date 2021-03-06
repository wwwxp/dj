<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>命令配置页面</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript"
	src="${ctx}/js/configuremanager/configdfine/commandConfigManage.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<div class="search" id="form" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
			<table style="width: 100%;height: 100%;">
				<tr>
					<td style="width: 50%;padding-left: 10px;">
	                    <a class="mini-button opBtn" onclick="refresh()" plain="false" tooltip="刷新" >刷新</a>
						<a class="mini-button opBtn" onclick="add()" plain="false" tooltip="增加" >增加</a> 
						<a class="mini-button opBtn" onclick="del()" plain="false" tooltip="删除">删除</a>
	                </td>
					<td align="right" style="padding-right: 10px;">
						<input name="CMD_NAME" class="mini-textbox" emptyText="命令名称" style="width:150px;" onenter="onKeyEnter"/>
	                    <a class="mini-button" onclick="search()">查询</a>
	                </td>
				</tr>
			</table>
		</div>
		
		<div class="mini-fit">
			<!-- start of datagrid -->
			<div id="datagrid" class="mini-datagrid"
				style="width: 100%; height: 100%" idField="PAGE_ID"
				allowResize="false" allowCellselect="false" multiSelect="false"
				showFooter="true"  >
				<div property="columns">
					<div type="checkcolumn" width="20"></div>
					<div field="CMD_NAME" headerAlign="center" align="center" width="60">命令名称</div>
     				<div field="CMD_CONTENT" headerAlign="center" align="center" >命令内容</div>
     				<div field="CRT_DATE" headerAlign="center" align="center" width="30" >创建日期</div>
					
					<div name="operation" visible="true" field="" headerAlign="center" align="center" renderer="onRenderer" width="40">操作</div>
				</div>
			</div>
			<!-- end of datagrid -->
		</div>
	</div>
</body>
</html>
