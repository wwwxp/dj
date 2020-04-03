<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/role/dispatchPrivilege.js" type="text/javascript"></script>
	<title>权限指派</title>
</head>
<body>
    <div class="search">
        <table style="width: 100%;height: 100%;">
            <tr>
                <td><span style="margin-left: 5px;">权限名称：
                </span> <input id="ROLE_NAME" name="ROLE_NAME" class="mini-textbox"
                               style="width:180px;margin-left: 5px;" onenter="onKeyEnter"/>
                    <a class="mini-button" onclick="search()"  style="margin-left: 5px;">查询</a></td>
            </tr>
        </table>
    </div>

	<div class="mini-fit">
		<ul id="privilege_tree" class="mini-tree" style="width: 100%; height: 100%;"
			showTreeIcon="true" textField="PRIVILEGE_NAME"
			url="${ctx}/core/queryForList?execKey=privilegeMapper.queryPrivilege"
			idField="PRIVILEGE_ID" parentField="PARENT_PRIVILEGE_ID" resultAsTree="false"
			showCheckBox="true" checkRecursive="true" expandOnLoad="1"
			allowSelect="false" enableHotTrack="true" autoCheckParent="true">
		</ul>
	</div>
	<div class="mini-toolbar"
		style="height:28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
		borderStyle="border-left:0;border-bottom:0;border-right:0;">
		<a class="mini-button" style="width: 60px;" onclick="onSubmit()">确定</a> <span
			style="display: inline-block; width: 25px;"></span> <a
			class="mini-button" style="width: 60px;" onclick="closeWindow()">取消</a>
	</div>
</body>
</html>