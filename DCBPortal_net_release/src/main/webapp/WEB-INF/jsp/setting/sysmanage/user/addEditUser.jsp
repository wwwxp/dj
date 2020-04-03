<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/public/common/common.jsp"%>
    <script src="${ctx}/js/setting/sysmanage/user/addEditUser.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>新增用户</title>
</head>

<body>
<div class="mini-fit p5">
    <table id="userForm" class="formTable6" style="table-layout: fixed;">
        <colgroup>
            <col width="85px"/>
            <col/>
            <col width="80px"/>
            <col/>
        </colgroup>
        <tr style="height: 30px">
            <th><span class="fred">*</span>用户名：</th>
            <td><input name="EMPEE_NAME" class="mini-textbox" required="true"
                         vtype="rangeChar:1,20" style="width: 180px"/></td>
            <th><span class="fred">*</span>用户角色：</th>
            <td><input id="ROLE_ID" name="ROLE_ID" class="mini-combobox"
                       textField="ROLE_NAME" valueField="ROLE_ID"
                       showNullItem="false"  required="true" allowInput="false" multiSelect="true" style="width: 180px"/>
            </td>
        </tr>
        <tr>
            <th><span class="fred">*</span>登录账号：</th>
            <td><input name="EMPEE_ACCT" class="mini-textbox"
                        vtype="rangeChar:1,20"
                       required="true" style="width: 180px"/></td>
            <th><span class="fred">*</span>登录密码：</th>
            <td><input name="EMPEE_PWD" class="mini-password" required="true"
                        minLengthErrorText="密码不能少于8个字符"  vtype="minLength:8;PWDCHECK"
                       style="width: 180px"/></td>
        </tr>

        <tr>
            <th>电话：</th>
            <td><input name="EMPEE_TEL_NO" class="mini-textbox"
                       vtype="telephone"
                       required="false" style="width: 180px"/></td>

            <th>手机：</th>
            <td><input name="EMPEE_MOB_NO" class="mini-textbox"
                       vtype="mobile"
                       required="false" style="width: 180px"/></td>
        </tr>
        <tr>
            <th>邮箱：</th>
            <td><input name="EMPEE_EMAIL_ADDR" class="mini-textbox"
                       vtype="email"
                       required="false" style="width: 180px"/></td>
            <th>QQ：</th>
            <td><input name="QQ" class="mini-textbox" required="false"
                       vtype="int;rangeLength:5,11"
                       style="width: 180px"/></td>
        </tr>
        <tr>
            <th>用户描述：</th>
            <td colspan="3"><input name="EMPEE_ADDR_DESC" vtype="rangeChar:0,512"
                                   class="mini-textArea" required="false" style="width: 95%"/>
            </td>
        </tr>
    </table>
</div>

<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
    <a class="mini-button" onclick="onSubmit" style="margin-right: 20px;">保存</a> <span
        style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="closeWindow()">取消</a>
</div>
</body>
</html>
