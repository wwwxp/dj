<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp" %>
    <script src="${ctx}/js/setting/sysmanage/role/roleManage.js" type="text/javascript"></script>
    <title>角色管理</title>
</head>
<body>
<div class="mini-fit p5">
    <div style="width: 100%;">
        <div class="search" id="queryFrom" style="padding: 0px;height: 50px;">
            <table style="width: 100%;height: 100%;">
                <tr>
                    <td width="300px;"><a class="mini-button" onclick="delRole()" plain="false">删除</a> <a
                            class="mini-button" onclick="addRole()" plain="false">新增</a>
                        <!--<a class="mini-button" onclick="refresh()" plain="false">刷新</a>--></td>
                    <td><span style="margin-left: 5px;">角色名称：</span> <input id="ROLE_NAME" name="ROLE_NAME"
                                                                            class="mini-textbox"
                                                                            style="width:180px;margin-left: 5px;"/><a
                            class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a></td>
                </tr>
            </table>
        </div>
    </div>
    <div class="mini-fit" style="margin-top: 5px;">
        <div id="role_datagrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="ROLE_ID" allowResize="false" allowCellselect="false" multiSelect="true"
             showFooter="true" sortMode="client">
            <div property="columns">
                <div type="indexcolumn"></div>
                <div type="checkcolumn"></div>
                <div field="ROLE_ID" width="250" headerAlign="center" align="center">角色标识(ID)</div>
                <div field="ROLE_NAME" width="150" headerAlign="center" align="center">角色名称</div>
                <div field="TYPE_NAME" width="150" headerAlign="center" align="center">角色类型</div>
                <div field="DESCRIPTION" width="100%" headerAlign="center">角色描述</div>
                <div field="STATE_TIP" width="80" autoShowPopup="true" headerAlign="center" align="center">状态</div>
                <div name="action" width="160" headerAlign="center" align="center" renderer="onActionRenderer"
                     cellStyle="padding:0;">操作
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
