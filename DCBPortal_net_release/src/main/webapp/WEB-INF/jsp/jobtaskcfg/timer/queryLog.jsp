<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/jobtaskcfg/timer/queryLog.js"
		type="text/javascript"></script>
	<title>任务日志查看</title>
</head>
<body>
	<div class="mini-fit p5">

		<div id="datagrid" class="mini-datagrid"
			style="width: 100%; height: 100%;" idField="ID" allowResize="false"
			multiSelect="false">
			<div property="columns">
				<div type="indexcolumn"></div>
				<div field="TASK_NAME" headerAlign="center" align="center"
					width="100">任务名称</div>

				<div field="TASK_TYPE" headerAlign="center" align="center"
					width="80" renderer="taskTypeRenderer">任务调度类型</div>
				<div field="EXEC_TYPE" headerAlign="center" align="center"
					width="100" renderer="execTypeRenderer">任务分类</div>
				<div field="TASK_JOB_PARAMS" headerAlign="center" align="center"
					width="150" renderer="paramsRenderer">任务内容</div>
				<div field="RUN_TIME" headerAlign="center" align="center"
					dateFormat="yyyy-MM-dd HH:mm:ss" width="130">执行时间</div>
				 
				<div field="RUN_DESC" headerAlign="center" align="center"
					width="100">执行状态</div>
				<div field="RUN_RESULT" headerAlign="center" align="center"
					 width="100">执行结果</div>
			</div>
		</div>
	</div>
</body>
</html>
