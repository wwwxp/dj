<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/user/editPasswordUser.js" type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>修改密码</title>
</head>
<body>
    <div class="mini-fit p5">
        <table id="edit_form" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="100px" />
				<col />
			</colgroup>
			<tr>
				<th><span class="fred">*</span>原密码：</th>
				<td><input class="mini-password" required="true"
                           minLengthErrorText="密码不能少于8个字符"
                           vtype="minLength:6" name="oldPassword" style="width: 150px"/></td>
			</tr>
			<tr>
                <th><span class="fred">*</span>新密码：</th>
                <td>
                    <input class="mini-password" required="true"
                           minLengthErrorText="密码不能少于8个字符"
                           vtype="minLength:8;PWDCHECK" name="newPassword" style="width: 150px"/></td>
			</tr>
            <tr>
                <th><span class="fred">*</span>确认新密码：</th>
                <td><input class="mini-password" required="true"
                           minLengthErrorText="密码不能少于8个字符"
                           vtype="minLength:8;PWDCHECK" name="configPassword" style="width: 150px"/></td>
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
