<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DCCP云计费平台</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />

    <script language="javascript" type="text/javascript"
            src="${ctx}/js/nodemanager/nodemanager/addEditNode.js"></script>
</head>
<body>
<div class="mini-fit p5">
    <table id="nodeForm" class="formTable6"
           style="table-layout: fixed;">
        <colgroup>
            <col width="95px" />
            <col />
            <col width="95px" />
            <col />
        </colgroup>
        <tr>
            <th><span class="fred">*</span>节点名称：</th>
            <td><input width="100%" name="NODE_NAME" class="mini-textbox"
                       required="true"/></td>
            <th><span class="fred">*</span>节点主机：</th>
            <td><input width="100%" id="HOST_IP" name="HOST_IP" class="mini-combobox"
                       required="true" textField="HOST_IP_USER" valueField="NODE_HOST_ID"/></td>
        </tr>
        <tr>
            <th><span class="fred">*</span>节点类型：</th>
            <td><input width="100%" id="NODE_TYPE" name="NODE_TYPE" class="mini-combobox"
                       required="true" textField="NODE_TYPE" valueField="NODE_TYPE_ID"
                       popupMaxHeight="150" allowInput="true" /></td>
            <th>业务组：</th>
            <td><input width="100%"  id="GROUP_NAME" name="GROUP_NAME"
                       textField="GROUP_NAME" valueField="BUS_GROUP_ID"
                       class="mini-combobox"
                       showNullItem="true" nullItemText="=请选择="
                       allowInput="true" popupMaxHeight="150"/></td>


        </tr>
        <tr>
            <th><span class="fred">*</span>节点路径：</th>
            <td colspan="3"><input width="100%" id="NODE_PATH" name="NODE_PATH" textField="NODE_PATH" valueField="NODE_PATH"
                       class="mini-textbox" required="true" allowInput="true" /></td>

            <%--<td><input width="100%" popupHeight="60px"  name="NODE_STATE" id="NODE_STATE"--%>
                       <%--class="mini-combobox" textField="text" valueFiled="id"--%>
                       <%--value="1"--%>
            <%--/></td>--%>
        </tr>

        <tr>
            <th>节点描述：</th>
            <td colspan="3"><input width="100%"  name="NODE_DESC"
                       class="mini-textarea"/></td>

        </tr>

        <tr style="visibility: hidden;">
            <th></th>
            <td><input name="ID"
                       class="mini-textbox"/></td>

        </tr>


    </table>
</div>
<div class="mini-toolbar"
     style="height: 28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
    <a class="mini-button" onclick="onSubmit"
       style="width: 60px; margin-right: 20px;">确定</a> <span
        style="display: inline-block; width: 25px;"></span> <a
        class="mini-button" onclick="onCancel" style="width: 60px;">取消</a>
</div>
</body>
</html>
