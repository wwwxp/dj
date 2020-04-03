<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>配置用户命令权限</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript"
	src="${ctx}/js/configuremanager/configdfine/setCmdEmpeeRelation.js"></script>
</head>
<body>
	<div class="mini-fit p5">
        <div class="search" id="queryFrom" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
		<input id="CMD_ID" name="CMD_ID" class="mini-hidden"  /> 
		<table style="width: 100%;height: 100%;">
			<tr>
				<td><span style="margin-left: 5px;">用户名/工号：</span> <input id="EMPEE_NAME" name="EMPEE_NAME" class="mini-textbox" style="width:180px;margin-left: 5px;" /> 
				<a class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a>
				 <span class="fred">*多选按ctrl或点击方框</span></td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div id="user_datagrid" class="mini-datagrid"  onload="onUserLoad" style="width: 100%; height: 100%;"
		 multiSelect="true"  idField="EMPEE_ID"  showFooter="false"   onlyCheckSelection="true"
		 >
			<div property="columns">
				<div type="checkcolumn" width="20" name="checkname"></div>
				<div field="EMPEE_NAME" headerAlign="center" align="center" width="100">用户名</div>
				<div field="ROLE_NAME" headerAlign="center" width="150">用户角色</div>
				<div field="EMPEE_ACCT" headerAlign="center" align="center" width="100">登陆账号</div>
				<div field="EMPEE_ADDR_DESC" headerAlign="center" width="100%">用户描述</div>
			</div>
		</div>
	</div>
    </div>
    
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="onCancel" style="width:60px;">取消</a>
    </div>
    
</body>
</html>
