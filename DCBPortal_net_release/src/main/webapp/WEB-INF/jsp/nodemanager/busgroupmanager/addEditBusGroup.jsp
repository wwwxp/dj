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
            src="${ctx}/js/nodemanager/nodetypeconfig/addEditNodeType.js"></script>
</head>
<body>
<div class="mini-fit p5">
    <table id="nodeTypeForm" class="formTable6"
           style="table-layout: fixed;">
        <colgroup>
            <col width="95px" />
            <col />
            <col width="95px" />
            <col />
        </colgroup>
        <tr>
            <th><span class="fred">*</span>程序名称：</th>
            <td><input width="100%" name="NAME" class="mini-textbox"
                       required="true" vtype="rangeChar:1,30" maxlength="30"/></td>
            <th><span class="fred">*</span>程序编码：</th>
            <td><input width="100%" id="CODE" name="CODE" class="mini-textbox"
                       required="true" textField="CODE" valueField="CODE"
                       maxlength="15"/></td>
        </tr>
        <tr>
            <th>业务组：</th>
            <td><input width="100%" id="BUS_GROUP" name="BUS_GROUP" class="mini-combobox"
                       textField="GROUP_NAME" valueField="BUS_GROUP_ID"
                       showNullItem="true" nullItemText="=请选择="
                       maxlength="8" /></td>
            <th>开始版本号：</th>
            <td><input width="100%" id="START_VERSION" name="START_VERSION"
                       textField="START_VERSION" valueField="START_VERSION"
                       class="mini-textbox" /></td>

        </tr>
        <tr>

            <th>默认路径：</th>
            <td colspan="3"> <input width="100%" popupHeight="100px"  name="DEFAULT_PATH"
                       class="mini-textbox"/></td>
        </tr>

        <tr>
            <th>是否区分IP：</th>
            <td><input width="100%"  id="DIFF_CFG" name="DIFF_CFG"
                       class="mini-radiobuttonlist" repeatItems="2" repeatLayout="flow" repeatDirection="horizontal"
                       textField="text" valueField="id"/>
            </td>

            <th>是否WEB程序：</th>
            <td><input width="100%"  id="RUN_WEB" name="RUN_WEB"
                       class="mini-radiobuttonlist" repeatItems="2" repeatLayout="flow" repeatDirection="horizontal"
                       textField="text" valueField="id"
            /></td>

        </tr>

        <tr>
            <th>描述：</th>
            <td colspan="3"> <input width="100%" popupHeight="100px"  name="DESC"
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
