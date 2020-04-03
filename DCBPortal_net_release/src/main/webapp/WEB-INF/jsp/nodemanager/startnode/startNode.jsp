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
            src="${ctx}/js/nodemanager/startnode/startNode.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">
    <div id="queryForm" class="search">
        <table class="formTable8"
               style="width: 80%; height: 50px; table-layout: fixed; padding: 0"
               cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="70" />
                <col  />
                <col width="70"/>
                <col />
                <col width="70"/>
                <col />
                <col width="70"/>
                <col />
                <col width="70"/>
            </colgroup>
            <tr>
                <th><span>程序类型：</span></th>
                <td><input class="mini-combobox" id="NODE_TYPE" name="NODE_TYPE"
                           textField="NODE_TYPE_NAME" valueField="NODE_TYPE_ID"
                           showNullItem="true" nullItemText="=请选择="
                           onvaluechanged="onValueChanged"
                           style="width: 100%;" allowInput="true"></td>
                <th><span>主机IP：</span></th>
                <td><input class="mini-combobox" id="HOST_IP" name="HOST_IP"
                           textField="HOST_IP_USER" valueField="NODE_HOST_ID"
                           showNullItem="true" nullItemText="=请选择="
                           onvaluechanged="onValueChanged"
                           style="width: 100%;" allowInput="true"></td>
                <th><span>版本：</span></th>
                <td><input class="mini-combobox" id="VERSION" name="VERSION"
                           textField="VERSION" valueField="VERSION"
                           showNullItem="true" nullItemText="=请选择="
                           style="width: 100%;" allowInput="true"></td>
                <th><span>状态：</span></th>
                <td><input class="mini-combobox" id="STATE" name="STATE"
                           textField="text" valueField="code"
                           showNullItem="true" nullItemText="=请选择="
                           data="getSysDictData('deployed_node_state')"
                           style="width: 100%;"></td>
                <td><a class="mini-button" onclick="search()"
                       style="margin-left: 20px;">查询</a></td>
            </tr>
        </table>
    </div>

    <div class="search2" style="border: 0px;">
        <table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
            <tr>
                <td style="text-align: right;">
                    <a class="mini-button mini-button-green" onclick="startNode()"　width="100" plain="false">启动</a>
                    <a class="mini-button mini-button-green" onclick="stopNode()" plain="false">停止</a>
                    <a class="mini-button mini-button-green" onclick="checkNode()" plain="false">检查</a>
                    <a class="mini-button mini-button-green" onclick="delNodeVersion()" plain="false">删除</a>
                </td>
            </tr>
        </table>
    </div>

    <div class="mini-fit" style="margin-top: 0px;">
        <div id="deployNodeGrid" class="mini-datagrid"
              style="width: 100%; height: 100%"
             idField="ID" allowResize="false" allowCellselect="false"
             multiSelect="true" showFooter="true">
            <div property="columns">
                <div type="checkcolumn"  width="20"></div>

                <div field="DEPLOY_ID" visible="false" ></div>
                <div field="NODE_ID" visible="false" ></div>
                <div field="START_ID" visible="false" ></div>
                <div field="STATE" visible="false" ></div>
                <div field="NODE_NAME" width="190" headerAlign="center"
                     align="center">节点</div>
                <div field="NODE_TYPE" width="180" headerAlign="center"
                     align="center" >程序类型</div>
                <div field="VERSION" width="45" headerAlign="center"
                     align="center">版本</div>
                <div field="IP_USER" width="140" headerAlign="center"
                     align="center">主机IP</div>
                <div field="NODE_PATH" width="140" headerAlign="center"
                     align="center">路径</div>
                <div field="STATE_NAME" width="110" headerAlign="center"
                     renderer="onStateRenderer"
                     align="center">运行状态</div>
                <div field="START_DATE" width="125" headerAlign="center"
                     dateFormat="yyyy-MM-dd HH:mm:ss"
                     align="center">启动时间</div>
                <div field="STOP_DATE" width="125" headerAlign="center"
                     dateFormat="yyyy-MM-dd HH:mm:ss"
                     align="center">停止时间</div>

                <div name="operation" visible="true" field="" headerAlign="center"
                     align="center" renderer="onRenderer" width="160">操作</div>
            </div>
        </div>
    </div>
</div>
</body>
</html>