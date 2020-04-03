<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>业务功能分配</title>
    <%@ include file="/public/common/common.jsp"%>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businessassigned/businessAssignedManager.js"></script>
</head>
<body>
<div class="mini-fit p5">
    <div id="queryForm" class="search" style="margin-top: 5px;">
        <table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="95"/>
                <col />
                <col width="60"/>
                <col />
                <col width="80"/>
                <col />
                <col width="60"/>
                <col width="220"/>
                <col width="60"/>
                <col width="100"/>
                <col width="320"/>
            </colgroup>
            <tr>
                <th>
                    <span>程序名称/别名：</span>
                </th>
                <td>
                    <input id="QUERY_PROGRAM_NAME" name="QUERY_PROGRAM_NAME"
                           class="mini-textbox" style="width:100%;"/>
                </td>
                <th>
                    <span>本地网：</span>
                </th>
                <td>
                    <input id="QUERY_LATN_ID" name="QUERY_LATN_ID"  multiSelect="true"
                           showNullItem="true" allowInput="false"
                           valueField="CONFIG_VALUE" textField="CONFIG_TEXT"
                           class="mini-combobox" style="width:100%;"/>
                </td>

                <th>
                    <span>程序状态：</span>
                </th>
                <td>
                    <input id="QUERY_PROGRAM_STATE" name="QUERY_PROGRAM_STATE"
                           showNullItem="true" allowInput="false" data="getSysDictData('PROGRAM_STATE_LIST')"
                           valueField="code" textField="text"
                           class="mini-combobox" style="width:100%;"/>
                </td>
                <th>
                    <span>主机IP：</span>
                </th>
                <td>
                    <input id="QUERY_HOST_ID" name="QUERY_HOST_ID"  multiSelect="true"
                           showNullItem="true" allowInput="false"
                           valueField="HOST_ID" textField="HOST_TEXT"
                           class="mini-combobox" style="width:100%;"/>
                </td>
                <th style="text-align: center;">
                    <input style="vertical-align:text-bottom; margin-bottom:1px; margin-bottom:-2px\9;"
                           class="mini-checkbox" name="isCheckR" id="isCheckR" title="查询结果后，立即检查进程的状态">检查</input>
                </th>
                <th style="text-align: center;">
                    <a class="mini-button" onclick="search()">查询</a>
                </th>
                <td style="text-align: right;margin-right: 10px;">
                    <a class="mini-button mini-button-green" style="width:50px" onclick="addBatchRun()" plain="false">运行</a>
                    <a class="mini-button mini-button-green" style="width:50px" onclick="addBatchDtop()" plain="false">停止</a>
                    <a class="mini-button mini-button-green" style="width:50px" onclick="checkHostState()" plain="false">检查</a>
                    <%--<a class="mini-button mini-button-green" style="width:50px" onclick="batchTermal()" plain="false">终端</a>--%>
                </td>
            </tr>
        </table>
    </div>
    <table style="width: 100%">
        <tr>
            <td width="70%">

            </td>
            <td>
                <p style="margin: 3px 2px 0px 2px ;text-align: right;"><span style="font-weight: bold;color: black;">总计</span>：【&nbsp;<span id="countRow" style="color:BLACK;font-weight:bold;font-size: 15px"></span>&nbsp;】个程序，
                    正在运行【&nbsp;<span id="runStatus" style="color:#59bd5d;font-weight:bold;font-size: 15px"></span>&nbsp;】，
                    未运行【&nbsp;<span id="stopStatus" style="color: red;font-weight:bold;font-size: 15px"></span>&nbsp;】</p>

            </td>
        </tr>

    </table>
    <div class="mini-fit">
        <div id="programGrid" class="mini-datagrid" style="width: 100%;height:98%; margin-top: 5px;margin-bottom:5px; overflow-y: hidden;overflow-x: hidden;" pageSize="10"
             idField="ID" allowResize="false" showModified="false" allowCellEdit="true"
             allowCellSelect="true" cellEditAction="celldblclick"  multiSelect="true" showFooter="false" sortMode="client">
            <div property="columns">
                <div type="checkcolumn" width="15"></div>
                <div field="BUS_CLUSTER_NAME" width="80" headerAlign="center" align="center">业务主集群</div>
                <div field="CLUSTER_TYPE" width="50" headerAlign="center" align="center">业务类型</div>
                <div field="VERSION" width="60" headerAlign="center" align="center">业务版本</div>
                <div field="PROGRAM_NAME"  width="100" headerAlign="center" align="center">程序名称</div>
                <div field="PROGRAM_ALIAS" headerAlign="center" allowSort="true" width="60" >程序别名</div>
                <div field="LATN_NAME"  width="55" headerAlign="center" allowSort="true" align="center">本地网</div>
                <div field="HOST_IP" width="70" headerAlign="center" allowSort="true" align="center">运行主机</div>
                <div field="SCRIPT_SH_NAME" width="150" headerAlign="center" allowSort="true" align="left">脚本</div>
                <div field="PROGRAM_DESC" headerAlign="center" allowSort="true" width="70" >程序描述</div>
                <div field="RUN_STATE" allowSort="true" width="40" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
                <div name="action" width="120" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
            </div>
        </div>
    </div>
    </div>
</div>

<!-- 终端操作隐藏表单 -->
<div style="display: none;">
    <form id="termialForm" name="termialForm" method="post" target="_blank">
        <input type="hidden" id="termialHost" name="termialHost"/>
        <input type="hidden" id="logName" name="logName" value="终端操作"/>
    </form>
</div>
</body>
</html>