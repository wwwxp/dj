<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>业务程序配置</title>
    <%@ include file="/public/common/common.jsp"%>
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businessassigned/businessProgramConfig.js"></script>
</head>
<body>
<div class="mini-fit">
        <div class="mini-fit">
            <div class="search">
                <table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
                    <colgroup>
                        <col width="100"/>
                        <col />
                        <col width="100"/>
                        <col />
                        <col width="100"/>
                        <col />
                        <col width="140"/>
                    </colgroup>
                    <tr>
                        <th>
                            <span>业务主集群：</span>
                        </th>
                        <td>
                            <input id="busCluster" name="busCluster" class="mini-combobox" style="width: 100%;"
                                   textField="BUS_CLUSTER_NAME" valueField="BUS_CLUSTER_ID" showNullItem="true"
                                   allowInput="false"/>
                        </td>
                        <th>
                            <span>业务类型：</span>
                        </th>
                        <td>
                            <input id="clusterType" name="clusterType" class="mini-combobox" style="width: 100%;"
                                   textField="CLUSTER_TYPE" valueField="CLUSTER_TYPE" showNullItem="true"
                                   allowInput="false"/>
                        </td>
                        <th>
                            <span>程序名称/编码：</span>
                        </th>
                        <td>
                            <input id="programName" name="programName" class="mini-textbox" style="width: 100%;"/>
                        </td>
                        <td><a class="mini-button" onclick="queryUserProgram()" style="margin-left: 20px;">查询</a></td>
                    </tr>
                </table>
            </div>
            <div class="search2" style="border: 0px;padding-right: 5px;">
                <table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
                    <tr>
                        <td style="text-align: right;">
                            <a class="mini-button mini-button-green" onclick="addUserBusPrivilege()"　width="100" plain="false">程序指派</a>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="mini-fit">
                <div class="mini-panel" title="角色业务程序列表" style="width: 100%;height: 100%;">
                    <div id="programGrid" class="mini-datagrid" style="width: 100%; height: 100%"
                         idField="programGrid" allowResize="false" multiSelect="true" showFooter="false"
                         editNextOnEnterKey="true"  editNextRowCell="true" onload="loadUserData">
                        <div property="columns">
                            <div field="BUS_CLUSTER_NAME" width="50" headerAlign="center" align="center">集群主集群名称</div>
                            <div field="CLUSTER_NAME" width="70" headerAlign="center" align="center" renderer="renderClusterType">业务集群（集群类型）</div>
                            <div field="VERSION" width="70" headerAlign="center" align="center">版本</div>
                            <div field="PROGRAM_CODE" width="70" headerAlign="center" align="center">程序编码</div>
                            <div field="PROGRAM_NAME" width="70" headerAlign="center" align="center">程序名称</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>