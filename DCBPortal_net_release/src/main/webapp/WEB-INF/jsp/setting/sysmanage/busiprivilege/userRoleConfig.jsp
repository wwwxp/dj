<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/busiprivilege/userRoleConfig.js" type="text/javascript"></script>
	<title>用户管理</title>
</head>
<body>

<div class="mini-fit" style="padding: 2px;">
	<div class="search" id="queryFrom" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
 		<table style="width: 100%;height: 100%;">
			<tr>
				<td><span style="margin-left: 5px;">用户名/登录账号：</span> <input id="EMPEE_NAME" name="EMPEE_NAME" class="mini-textbox" style="width:180px;margin-left: 5px;" /> <a class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a></td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div id="user_datagrid" class="mini-datagrid" allowCellEdit="true"  style="width: 100%; height: 100%;" multiSelect="true" idField="EMPEE_ID" showFooter="true"  allowtResize="false">
			<div property="columns">
				<div type="indexcolumn" width="10" name="checkname"></div>
				<div field="EMPEE_NAME" headerAlign="center" align="center" width="80">用户名</div>
				<!-- <div field="ROLE_NAME" headerAlign="center" width="150">用户角色</div> -->
				<div field="EMPEE_ACCT" headerAlign="center" align="center" width="80">登陆账号</div>
				<div field="ROLES" headerAlign="center" align="center" width="100" renderer="onRenderRole">所属角色</div> 
				<div name="operation" visible="true" field="" headerAlign="center" align="center" renderer="onRenderer" width="40">操作</div>
			</div>
		</div>
	</div>
</div>
</body>
</html>
