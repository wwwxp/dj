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
            src="${ctx}/js/nodemanager/clustermanager/clusterManager.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">
    <div id="queryForm" class="search">
        <table class="formTable8"
               style="width: 60%; height: 50px; table-layout: fixed; padding: 0"
               cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="80" />
                <col />
                <col width="80" />
                <col />
            </colgroup>
            <tr>
                <th><span>集群名称：</span></th>
                <td><input class="mini-combobox" id="CLUSTER_NAME"
                           name="CLUSTER_NAME" style="width: 100%;"
                           allowInput="true"
                           showNullItem="true" nullItemText="=请选择="
                           textField="NODE_CLUSTER_NAME" valueField="NODE_CLUSTER_NAME"
                           onvaluechanged="onValueChanged"
                ></td>
                <th><span>集群编码：</span></th>
                <td><input class="mini-combobox" id="CLUSTER_CODE"
                           name="CLUSTER_CODE" style="width: 100%;"
                           allowInput="true"
                           showNullItem="true" nullItemText="=请选择="
                           textField="NODE_CLUSTER_CODE" valueField="NODE_CLUSTER_CODE"
                ></td>
                <td><a class="mini-button" onclick="search()"
                       style="margin-left: 63px;">查询</a></td>

            </tr>
        </table>
    </div>

    <div class="search2" style="border: 0px;">
        <table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
            <tr>
                <td style="text-align: right;">
                    <a class="mini-button mini-button-green" onclick="addCluster()"　width="100" plain="false">新增</a>
                    <a class="mini-button mini-button-green" onclick="delCluster()" plain="false">删除</a>
                    <a class="mini-button mini-button-green" onclick="showCluster()" plain="false">集群视图</a>
                </td>
            </tr>
        </table>
    </div>

    <div class="mini-fit" style="margin-top: 0px;">
        <div id="clusterGrid" class="mini-datagrid"
             style="width: 100%; height: 100%"
             idField="ID" allowResize="true" allowCellselect="false"
             multiSelect="true" showFooter="true">
            <div property="columns">
                <div type="checkcolumn"  width="20"></div>

                <div field="ID" visible="false" ></div>

                <div field="NODE_CLUSTER_NAME" width="100" headerAlign="center"
                     align="center">集群名称</div>
                <div field="NODE_CLUSTER_CODE" width="70" headerAlign="center"
                     align="center">编码</div>
                <div field="NODE_CLUSTER_DESC" width="160" headerAlign="center"
                     align="left" renderer="">描述信息</div>
                <div field="CREATED_DATE" width="80" headerAlign="center"  dateFormat="yyyy-MM-dd HH:mm:ss"
                     align="center">创建时间</div>
                <div field="CLUSTER_MEMBER" width="190" headerAlign="center"
                     align="center" renderer="onMemberRenderer">集群成员</div>
                <div name="operation" visible="true" field="" headerAlign="center"
                     align="center" renderer="onRenderer" width="80">操作</div>
            </div>
        </div>
    </div>
</div>
</body>
</html>