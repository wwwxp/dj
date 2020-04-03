<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/privilege/addPrivilege.js" type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>网元节点属性</title>
</head>
<body>
    <div class="mini-fit p5">
        <table id="privilege_form" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="100px" />
				<col />
				<col width="100px" />
				<col />
			</colgroup>
			<tr>
				<th><span class="fred">*</span>父权限名称：</th>
				<td><input class="mini-textbox"  name="PARENT_PRIVILEGE_NAME" allowInput="false" enabled="false"
					style="width: 180px;" /></td>
                <th><span class="fred">*</span>父权限编码：</th>
				<td><input style="width:180px;" class="mini-textbox"  name="PARENT_PRIVILEGE_ID"
					allowInput="false" enabled="false" /></td>
			</tr>
			<tr>
                <th><span class="fred">*</span>权限编码：</th>
                <td>
                    <input class="mini-textbox"  style="width:180px;"
                            vtype="rangeChar:1,30"
                           name="PRIVILEGE_CODE" required="true" /></td>
				<th><span class="fred">*</span>权限名称：</th>
				<td><input class="mini-textbox" style="width:180px;"  name="PRIVILEGE_NAME"
					required="true"  vtype="rangeChar:1,50" /></td>
			</tr>
            <tr>
                <th>位置：</th>
                <td><input name="POSITION" class="mini-spinner" minValue="0" style="width:180px;" /></td>
                <th><span class="fred">*</span>权限类型：</th>
                <td><input class="mini-combobox"  style="width:180px;" valueField="code"
                               data="getSysDictData('privilege_type')"    value="3"
                                        name="PRIVILEGE_TYPE" enabled="false"/></td>
            </tr>
			<tr>
				<th>操作路径：</th>
				<td colspan="3"><input class="mini-textbox" style="width: 100%;"  name="URL" /></td>
			</tr>
			<tr>
				<th>权限描述：</th>
				<td colspan="3"><input class="mini-textarea" style="width: 100%; height: 120px;"
					name="DESCRIPTION" /></td>
			</tr>
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit()">保存</a> <span style="display: inline-block; width: 25px;"></span> <a
            class="mini-button" onclick="closeWindow()">取消</a>
    </div>
</body>
</html>
