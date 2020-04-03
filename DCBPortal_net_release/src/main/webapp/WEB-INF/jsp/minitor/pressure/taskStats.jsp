<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机压力监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/taskStats.js"></script>


</head>
<body>
	<div class="mini-fit" style="padding: 5px;">

	<div class="mini-fit" style="margin-top: 5px;">
		<div id="taskGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
<!-- 				<div type="indexcolumn" headerAlign="center" align="center"  width="5" >序号</div> -->
				<div field="task_id"  width="10" headerAlign="center" align="center" >Task Id</div>
				<div field="component" width="20"headerAlign="center" align="center">Component</div>
				<div field="host"  width="15"headerAlign="center" align="center" >Host</div>
				<div field="port" width="10"headerAlign="center" align="center">Port</div>
				<div field="uptime" width="15"headerAlign="center" align="center">Uptime</div>
                <div field="errors" width="10"headerAlign="center" align="center" renderer="formatError" >Error</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
