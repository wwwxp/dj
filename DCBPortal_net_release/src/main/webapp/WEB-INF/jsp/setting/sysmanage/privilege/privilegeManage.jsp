<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp" %>
    <script src="${ctx}/js/setting/sysmanage/privilege/privilegeManage.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>权限管理</title>
</head>
<body>
<div class="mini-fit p5">
    <div class="mini-splitter" style="width: 100%; height: 100%;" borderStyle="border:0;">

        <div size="300" showCollapseButton="true" style="border:1px;" minSize="300px">
            <div class="mini-toolbar" style="padding: 2px; border-right: 0;height: 30px;">
                <table style="width: 100%;height: 100%;">
                    <tr>
                        <td>
                            <input id="PRIVILEGE_NAME" style="width: 120px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
                            <a class="mini-button" plain="false" onclick="search()">查找</a>
                            <a class="mini-button" plain="false" onclick="refreshPrivilegeTree();">刷新</a>
                        </td>
                    </tr>
                </table>

            </div>
            <div class="mini-fit">
                <ul id="privilege_tree" class="mini-tree"
                    style="width: 100%; height: 99%" showTreeIcon="true" textField="PRIVILEGE_NAME" contextMenu="#tree_right_menu"
                    showTreeIcon="true" idField="PRIVILEGE_ID" parentField="PARENT_PRIVILEGE_ID" resultAsTree="false"
                    expandOnLoad="0"
                    onNodeclick="onClickTreeNode()">
                </ul>
                <ul id="tree_right_menu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">
                    <li name="add" iconCls="icon-add" onclick="onAddNode">新增权限</li>
                    <li name="remove" iconCls="icon-remove" onclick="onRemoveNode">删除权限</li>
                </ul>
            </div>
        </div>
        <div>
            <table id="privilege_form" width="100%" class="formTable6" style="table-layout: fixed;">
                <colgroup>
                    <col width="100px" />
                    <col />
                    <col width="100px" />
                    <col />
                </colgroup>
                <tr>
                    <th><span class="fred">*</span>父权限名称：</th>
                    <td><input id="parentNodeSelect" class="mini-treeselect" style="width: 180px;" popupWidth="100%"
                               popupHeight="340" name="PARENT_PRIVILEGE_ID"
                               valueField="PRIVILEGE_ID" parentField="PARENT_PRIVILEGE_ID" textField="PRIVILEGE_NAME"
                               expandOnLoad="0" onbeforenodeselect="beforenodeselect"
                               resultAsTree="false" allowInput="false" checkRecursive="false"/></td>
                    <th><span class="fred">*</span>父权限编码：</th>
                    <td><input style="width:180px;" class="mini-textbox"  name="PARENT_PRIVILEGE_CODE" id="PARENT_PRIVILEGE_CODE"
                               allowInput="false" enabled="false" /></td>
                </tr>
                <tr>
                    <th><span class="fred">*</span>权限名称：</th>
                    <td><input class="mini-textbox" style="width:180px;" name="PRIVILEGE_NAME"
                               required="true"  vtype="rangeChar:1,50" /></td>
                    <th><span class="fred">*</span>权限编码：</th>
                    <td>
                        <input class="mini-textbox" id="PRIVILEGE_CODE" style="width:180px;" name="PRIVILEGE_CODE"
                               vtype="rangeChar:1,30" required="true" /></td>
                </tr>
                <tr>
                    <th>位置：</th>
                    <td><input  name="POSITION" class="mini-spinner" minValue="0" style="width:180px;" /></td>
                    <th><span class="fred">*</span>权限类型：</th>
                    <td><input class="mini-combobox" id="PRIVILEGE_TYPE" style="width:180px;"
                               data="getSysDictData('privilege_type')" value="3" valueField="code"
                               name="PRIVILEGE_TYPE" enabled="false"/></td>
                </tr>
                <tr>
                    <th>操作路径：</th>
                    <td colspan="3"><input class="mini-textbox" vtype="maxLength:190"
                                           style="width: 100%;"   name="URL" /></td>
                </tr>
                <tr>
                    <th>权限描述：</th>
                    <td colspan="3"><input class="mini-textarea" vtype="maxLength:512"
                                           style="width: 100%; height: 120px;"  name="DESCRIPTION" /></td>
                </tr>
                <tr align="center">
                    <td colspan="4" style="text-align: center;"><a class="mini-button" id="saveBtn" enabled="false"
                                                                   onclick="onSubmit()">保存</a> <a class="mini-button"
                                                                                                  onclick="reset()">重置</a>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>
</body>
</html>
