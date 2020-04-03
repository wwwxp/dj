<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机压力监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/hostPressureMonitor.js"></script>
	<script language="javascript" type="text/javascript" >
	var clusterName='${param.clusterName}';
	</script>

</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="topologyGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="25" >序号</div>
				<div field="name"  headerAlign="center" align="center" renderer="formatTopologyName">拓扑名称</div>
				<div field="id" headerAlign="center" align="center">拓扑ID</div>
				<div field="status"  headerAlign="center" align="center">状态</div>
				<div field="uptimeSecs" headerAlign="center" align="center">Uptime</div>
                <div field="numWorkers" headerAlign="center" align="center">worker数量</div>
                <div field="numTasks" headerAlign="center" align="center">task数量</div>
                <div field=""   headerAlign="center" renderer="getConfig" align="center">配置</div>
                <div field="errorInfo" headerAlign="center" align="center">是否有错误</div>
                <div field=""  headerAlign="center" align="center" renderer="formatComponentMetrics">Component Metrics</div>
                <div field=""  headerAlign="center" align="center" renderer="formatWorkerMetrics">Worker Metrics</div>
                <div field=""  headerAlign="center" align="center" renderer="formatTaskStats">Task Stats</div>
                
                
                
			</div>
		</div>
	</div>
    </div>
</body>
</html>
