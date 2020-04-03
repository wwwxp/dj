<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/user/userManage.js" type="text/javascript"></script>
	<title>用户管理</title>
</head>
<body>
<% Map userMap = (Map)request.getSession().getAttribute("userMap"); %>
<div class="mini-fit" style="padding: 2px;">
	<div class="search" id="queryFrom" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
	<input id="EMPEE_ID" name="EMPEE_ID" class="mini-hidden" value="<%=userMap.get("EMPEE_ID") %>" /> 
		<table style="width: 100%;height: 100%;">
			<tr>
				<td style="width: 300px;"><a class="mini-button" onclick="delUser()" plain="false">删除</a> <a class="mini-button" onclick="addUser()" plain="false">新增</a>
                    <!--<a class="mini-button" onclick="refresh()" plain="false">刷新</a>--></td>
				<td><span style="margin-left: 5px;">用户名/工号：</span> <input id="EMPEE_NAME" name="EMPEE_NAME" class="mini-textbox" style="width:180px;margin-left: 5px;" /> <a class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a></td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div id="user_datagrid" class="mini-datagrid" allowCellEdit="true"  style="width: 100%; height: 100%;" multiSelect="true" idField="EMPEE_ID" showFooter="true"  allowtResize="false">
			<div property="columns">
				<div type="checkcolumn" width="20" name="checkname"></div>
				<div field="EMPEE_NAME" headerAlign="center" align="center" width="100">用户名</div>
				<div field="ROLE_NAME" headerAlign="center" width="150">用户角色</div>
				<div field="EMPEE_ACCT" headerAlign="center" align="center" width="100">登陆账号</div>
				<div field="EMPEE_EMAIL_ADDR" headerAlign="center" width="180">邮箱</div>
				<div field="QQ" headerAlign="center" align="center" width="110">QQ</div>
				<div field="EMPEE_TEL_NO" headerAlign="center" align="center" width="110">电话</div>
				<div field="EMPEE_MOB_NO" headerAlign="center" align="center" width="110">手机</div>
				<div field="EMPEE_ADDR_DESC" headerAlign="center" width="100%">用户描述</div>
				<div name="operation" visible="true" field="" headerAlign="center" align="center" renderer="onRenderer" width="80">操作</div>
			</div>
		</div>
	</div>
</div>
</body>
</html>
