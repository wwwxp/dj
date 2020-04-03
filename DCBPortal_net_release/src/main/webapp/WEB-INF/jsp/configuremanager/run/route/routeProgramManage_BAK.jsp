<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>运行弹出框 </title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/route/routeProgramManage.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow-x:hidden;overflow-y: auto;">
	<div style="height:360px;">
        <div id="programGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="10"
             idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true">
			<div property="columns">
				<div type="indexcolumn" width="2" ></div>
				<div field="PROGRAM_NAME"  width="25" headerAlign="center" align="center">程序名称</div>
				<div field="HOST_IP" width="25" headerAlign="center" align="center" renderer="runHostIpRenderer">运行主机</div>
                <div field="RUN_STATE" width="17" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
				<div name="action" width="17" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
			</div>
		</div>
	</div>
	<div style="height:30px;line-height:30px;">
		<label>启停进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>

<!--     <div class="mini-fit" style="padding: 5px;">
		<div id="programGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="2" ></div>
				<div field="PROGRAM_NAME"  width="25" headerAlign="center" align="center">程序名称</div>
				<div field="HOST_IP" width="25" headerAlign="center" align="center" renderer="runHostIpRenderer">运行主机</div>
                <div field="RUN_STATE" width="17" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
				<div name="action" width="17" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
			</div>
		</div>
    </div> -->
</body>
</html>
