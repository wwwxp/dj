<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>集群权限配置</title>
    <%@ include file="/public/common/common.jsp"%>
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businessassigned/businessColonyConfig.js"></script>
</head>
<body>
<div class="mini-fit">
    <div class="mini-fit">
        <div class="search2" style="border: 0px;padding-right: 5px;">
            <table id="updateForm" class="formTable9"
                   style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0"
                   cellspacing="0">
                <tr>
                    <td style="text-align: right;">
                        <a class="mini-button mini-button-green" onclick="addUserColony()" 　width="100"
                           plain="false">集群指派</a>
                    </td>
                </tr>
            </table>
        </div>
        <div class="mini-fit">
            <div class="mini-panel" title="集群信息列表" style="width: 100%;height: 100%;">
                <div id="colonyGrid" class="mini-datagrid" style="width: 100%; height: 100%"
                     idField="colonyGrid" allowResize="false" multiSelect="true" showFooter="false"
                     editNextOnEnterKey="true" editNextRowCell="true">
                    <div property="columns">
                        <div field="BUS_CLUSTER_NAME" width="70" headerAlign="center" align="center">业务主集群名称</div>
                        <div field="BUS_CLUSTER_CODE" width="70" headerAlign="center" align="center">业务主集群编码</div>
                        <div field="BUS_CLUSTER_LIST" width="160" headerAlign="center" align="center" renderer="onRenderBusCluster">集群成员</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</body>
</html>