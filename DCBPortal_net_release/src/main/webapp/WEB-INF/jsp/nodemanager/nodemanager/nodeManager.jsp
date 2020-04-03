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
            src="${ctx}/js/nodemanager/nodemanager/nodeManager.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">
    <div id="queryForm" class="search">
        <table class="formTable8"
               style="width: 80%; height: 50px; table-layout: fixed; padding: 0"
               cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="80" />
                <col width="200"/>
                <col width="80" />
                <col width="200"/>
                <col width="80"/>
                <col width="200"/>
                <col width="80"/>
                <col width="200"/>
                <col width="200"/>
            </colgroup>
            <tr>
                <th><span>节点类型：</span></th>
                <td><input class="mini-combobox" id="NODE_TYPE"
                           name="NODE_TYPE" style="width: 100%;"
                           allowInput="true"
                           showNullItem="true" nullItemText="=请选择="
                           textField="NODE_TYPE_TEXT" valueField="NODE_TYPE_ID"
                           onValueChanged="onValueChanged"
                ></td>
                <th><span>节点名称：</span></th>
                <td><input class="mini-combobox" id="NODE_NAME"
                           name="NODE_NAME" style="width: 100%;"
                           allowInput="true"
                           showNullItem="true" nullItemText="=请选择="
                           textField="NODE_NAME" valueField="NODE_NAME"
                           onValueChanged="onValueChanged"
                ></td>
                <th><span>主机IP：</span></th>
                <td><input class="mini-combobox" id="HOST_IP" name="HOST_IP"
                           style="width: 100%;" allowInput="true"
                           showNullItem="true" nullItemText="=请选择="
                           textField="HOST_IP_USER" valueField="NODE_HOST_ID"
                ></td>
                <th><span>业务组：</span></th>
                <td><input class="mini-combobox" id="GROUP_NAME" name="GROUP_NAME"
                           showNullItem="true" nullItemText="=请选择="
                           textField="GROUP_NAME" valueField="ID"
                           style="width: 100%;" allowInput="true"
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
                    <a class="mini-button mini-button-green" onclick="addNode()"　width="100" plain="false">新增</a>
                    <a class="mini-button mini-button-green" onclick="batchAddNode()"　width="100" plain="false">批量新增</a>
                    <a class="mini-button mini-button-green" onclick="delNode()" plain="false">删除</a>

                </td>
            </tr>
        </table>
    </div>

    <div class="mini-fit" style="margin-top: 0px;">
        <div id="nodeInfoGrid" class="mini-datagrid"
              style="width: 100%; height: 100%"
             idField="ID" allowResize="false" allowCellselect="false"
             multiSelect="true" showFooter="true">
            <div property="columns">
                <div type="checkcolumn"  width="20"></div>

                <div field="ID" visible="false" ></div>
                <div field="HOST_IP" visible="false" ></div>
                <div field="NODE_TYPE_ID" visible="false" ></div>

                <div field="NODE_NAME" width="110" headerAlign="center"
                     align="center">节点名称</div>
                <div field="HOST_IP_USER" width="80" headerAlign="center"
                     align="left" renderer="">节点主机</div>
                <div field="NODE_TYPE" width="90" headerAlign="center"
                     align="center">节点类型</div>
                <div field="NODE_PATH" width="80" headerAlign="center"
                     align="left">节点路径</div>
                <div field="NODE_STATE_NAME" width="40" headerAlign="center"
                     align="center" renderer="">节点状态</div>
                <div field="GROUP_NAME" width="50" headerAlign="center"
                     align="center" renderer="">业务组</div>
                <div field="NODE_DESC" width="100" headerAlign="center"
                     align="left" renderer="">节点描述</div>
                <div name="operation" visible="true" field="" headerAlign="center"
                     align="center" renderer="onRenderer" width="80">操作</div>
            </div>
        </div>
    </div>
</div>
</body>
</html>