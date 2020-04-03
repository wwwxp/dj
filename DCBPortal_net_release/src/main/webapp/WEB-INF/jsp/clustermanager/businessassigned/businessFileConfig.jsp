<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>业务程序配置</title>
    <%@ include file="/public/common/common.jsp"%>
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businessassigned/businessFileConfig.js"></script>
</head>
<body>
<div class="mini-fit">
        <div class="mini-fit">
            <div class="search">
                <table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
                    <colgroup>
                        <col width="60"/>
                        <col />
                        <col width="70"/>
                        <col />
                        <col width="70"/>
                        <col width="100"/>
                        <col width="70"/>
                        <col />
                        <col width="100"/>
                    </colgroup>
                    <tr>
                        <th>
                            <span>包类型：</span>
                        </th>
                        <td>
                            <input id="packageType" name="packageType" class="mini-combobox" style="width: 100%;"
                                   textField="CONFIG_NAME" valueField="CONFIG_VALUE" showNullItem="true" allowInput="false"/>
                        </td>
                        <th>
                            <span>程序版本：</span>
                        </th>
                        <td>
                            <input id="version" name="version" class="mini-combobox" style="width: 100%;"
                                   textField="NAME" valueField="NAME" showNullItem="true" allowInput="false"/>
                        </td>
                        <th>
                            <span>程序类型：</span>
                        </th>
                        <td>
                            <input id="clusterType" name="clusterType" class="mini-combobox" style="width: 100%;"
                                   textField="CLUSTER_TYPE" valueField="CLUSTER_TYPE" showNullItem="true" allowInput="false"/>
                        </td>
                        <th>
                            <span>文件名称：</span>
                        </th>
                        <td>
                            <input id="filePath" name="filePath" class="mini-textbox" style="width: 100%;"/>
                        </td>
                        <td><a class="mini-button" onclick="queryUserConfig()" style="margin-left: 5px;">查询</a></td>
                    </tr>
                </table>
            </div>
            <div class="search2" style="border: 0px;padding-right: 5px;">
                <table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align: right;">
                            <a class="mini-button mini-button-green" onclick="addUserBusConfig()"　width="100" plain="false">文件指派</a>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="mini-fit">
                <div class="mini-panel" title="角色配置文件列表" style="width: 100%;height: 100%;">
                    <div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%"
                         idField="ID" allowResize="false" multiSelect="true" showFooter="false"
                         editNextOnEnterKey="true"  editNextRowCell="true" onload="loadUserData">
                        <div property="columns">
                            <div field="PACKAGE_TYPE" width="30" headerAlign="center" align="center">包类型</div>
                            <div field="VERSION" width="40" headerAlign="center" align="center">版本</div>
                            <div field="CLUSTER_TYPE" width="20" headerAlign="center" align="center">程序类型</div>
                            <div field="FILE_NAME" width="50" headerAlign="center" align="center">文件名</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>