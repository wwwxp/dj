<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>业务功能配置</title>
    <%@ include file="/public/common/common.jsp"%>
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businessassigned/businessAssignedConfig.js"></script>
</head>
<body>
<div class="mini-fit p5" style="padding-top: 0px;">
    <div style="width: 24%;height: 100%;float: left;">
        <div class="mini-fit" style="padding-top: 5px;">
            <div id="queryForm" class="search">
                <table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
                    <colgroup>
                        <col width="60"/>
                        <col />
                        <col width="100"/>
                    </colgroup>
                    <tr>
                        <th>
                            <span>角色名：</span>
                        </th>
                        <td>
                            <input id="roleName" name="roleName" class="mini-textbox" style="width: 100%;"/>
                        </td>
                        <td><a class="mini-button" onclick="queryRoleList()" style="margin-left: 10px;">查询</a></td>
                    </tr>
                </table>
            </div>
            <div class="mini-fit" style="padding-top: 5px;">
                <div class="mini-panel" title="角色列表" style="width: 100%;height: 100%;">
                    <div id="roleGrid" class="mini-datagrid" style="width: 100%; height: 100%"
                         idField="ROLE_ID" allowResize="false" multiSelect="false" showFooter="true"
                         allowRowSelect="true" onrowclick="btnRoleClick" >
                        <div property="columns">
                            <div type="checkcolumn" width="8" ></div>
                            <div field="ROLE_NAME" width="50" headerAlign="center" align="center">角色名称</div>
                            <div field="CRT_DATE" width="50" dateFormat="yyyy-MM-dd HH:mm:ss" headerAlign="center" align="center">创建时间</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div style="width: 76%;height: 100%;float: left;">
        <!-- TAB页 -->
        <div id="busTabs" class="mini-tabs" activeIndex="0"
             style="width: 100%; height: 100%; padding: 5px 0px 5px 5px;" plain="false" ontabload="onTabLoad"
             tabAlign="left" tabPosition="top">
            <div title="集群权限指派" id="busColony" url="${ctx}/jsp/clustermanager/businessassigned/businessColonyConfig"></div>
            <div title="程序启停指派" id="busProgram" url="${ctx}/jsp/clustermanager/businessassigned/businessProgramConfig"></div>
            <div title="配置文件指派" id="busConfig" url="${ctx}/jsp/clustermanager/businessassigned/businessFileConfig"></div>
        </div>
    </div>
</div>
</body>
</html>