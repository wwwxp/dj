<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机压力监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
<script type="text/javascript" src="${ctx}/js/common/scripts/jquery-1.6.2.min.js"></script>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/topologySummary.js"></script>

</head>
<body>
	  <div class="mini-fit" style="overflow:auto;padding: 5px;">
		<div id="topologyGrid" class="mini-datagrid" style="width: 100%;"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div field="Emitted"  headerAlign="center" align="center">Emitted</div>
				<div field="Acked" headerAlign="center" align="center">Acked</div>
				<div field="SendTps"  headerAlign="center" align="center">SendTps</div>
				<div field="RecvTps" headerAlign="center" align="center">RecvTps</div>
                <div field="CpuUsedRatio" headerAlign="center" align="center">CPU Used(%)</div>
                <div field="MemoryUsed" headerAlign="center" align="center">Mem Used</div>
			</div>
		</div>
	
	 <iframe id="topo" style="border:0;margin-top:10px;width:100%;height:700px;"></iframe>
	</div>
	
</body>
</html>
