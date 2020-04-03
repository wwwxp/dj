<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>运行弹出框 </title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/other/otherProgramManage.js"></script>
</head>
<body>
 <div class="mini-fit" style="padding: 5px;">
	<div id="queryForm" class="search">
			<table class="formTable8"
				style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
				cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="60" />
					<col width="180" />
					<col width="60" />
					<col width="180" />
					<col width="100" />
					<col />
				</colgroup>
				<tr>
					<th><span>程序名：</span></th>
					<td><input id="QUERY_PROGRAM" name="QUERY_PROGRAM" class="mini-textbox" style="width: 100%;" /></td>
					<th><span>主机IP：</span></th>
					<td><input id="QUERY_HOST_IP" name="QUERY_HOST_IP" class="mini-textbox" style="width: 100%;" /></td>
					<td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
					<td style="text-align: right;">
						<!-- <a class="mini-button mini-button-green" onclick="back()" plain="false">运行</a>
						<a class="mini-button mini-button-green" onclick="back()" plain="false">停止</a>
					 --></td>
				</tr>
			</table>
		</div>
    <div class="mini-fit" style="padding-top: 5px;">
		<div id="programGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="5" ></div>
				<div field="PROGRAM_NAME"  width="25" headerAlign="center" align="center">程序名称</div>
				<div field="HOST_IP" width="25" headerAlign="center" align="center" renderer="runHostIpRenderer">运行主机</div>
                <div field="RUN_STATE" width="17" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
				<div name="action" width="17" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
			</div>
		</div>
    </div>
    </div>
</body>
</html>
