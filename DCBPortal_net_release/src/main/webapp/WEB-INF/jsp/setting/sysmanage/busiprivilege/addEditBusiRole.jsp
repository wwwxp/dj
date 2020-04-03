<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/busiprivilege/addEditBusiRole.js" type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>角色</title>
</head>

<body>
    <div class="mini-fit p5">
        <table id="roleForm" class="formTable6" style="table-layout: fixed;">
            <colgroup>
                <col width="80px" />
                <col />
               
            </colgroup>
            <tr>
                <th><span class="fred">*</span>角色名称：</th>
                <td><input width="100%" name="ROLE_NAME" class="mini-textbox" required="true"
                    vtype="rangeChar:1,30" /></td>
                <th>状态：</th>
                  <td ><input width="100%" name="ROLE_STATE" id="ROLE_STATE"  class="mini-combobox" valueFiled="code"
                    data="getSysDictData('role_state')" value="1" required="true" enabled="false"/></td>
            </tr>
            <tr>
                <th>角色描述：</th>
                <td colspan="3"><input width="100%" name="REMARK" class="mini-textarea"
                    vtype="rangeChar:0,512" /></td>
            </tr>
        </table>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> <span
            style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>