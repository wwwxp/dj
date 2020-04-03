<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>拓扑监控信息</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/topology/workerMonitor.js"></script>
    <link href="${ctx}/assets/css/vis.min.css" rel="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
</head>
<body>
	<div class="mini-fit p5" style = "overflow:auto">
		<div style="padding:5px;height:35px;text-align: right">
		    <span style="margin-right: 10px"><a class="mini-button mini-button-green" onclick="javascript:window.location.reload();" plain="false">刷新</a> </span>
			<span style=""><a class="mini-button mini-button-green" onclick="javascript:history.back();" plain="false">返回</a> </span>
		</div>
	
		<div class="mini-panel" title="worker信息" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div style="margin:5px;">
				<div id="workerInfoGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" 
		             showFooter="false" >
					<div property="columns">
						<div field="host" headerAlign="center" align="center" width="30" renderer="hostIPRenderer">主机</div>
						<div field="port" headerAlign="center" align="center" width="20">端口</div>
						<div field="CpuUsedRatio" headerAlign="center" align="center" width="20">CPU(%)</div>
						<div field="MemoryUsed" headerAlign="center" align="center" width="20">内存</div>
						<div field="NettyCliSendSpeed" headerAlign="center" align="center" width="20">netty发送(tps)</div>
						<div field="NettySrvRecvSpeed" headerAlign="center"  align="center" width="20">netty接收(tps)</div>
						<div field="MsgDecodeTime" headerAlign="center" align="center" width="30">Decode耗时(us)</div>
						<div field="NettySrvTransmitTime" headerAlign="center" align="center" width="30">NettySrv处理耗时(us)</div>
						<div field="NettyCliSendBatchSize" headerAlign="center"  align="center" width="20">批量发送</div>
						<div field="RecvCtrlQueue" headerAlign="center" align="center" width="25">Ctrl接收队列(%)</div>
						<div field="SendCtrlQueue" headerAlign="center" align="center" width="25">Ctrl发送队列(%)</div>
						<div field="HeapMemory" headerAlign="center" align="center" width="20" renderer="formatterHeapMemory">堆内存</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="最近一小时" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div class="chartDiv" id="CpuUsedRatio"></div>
			<div class="chartDiv" id="MemoryUsed"></div>
			<div class="chartDiv" id="NettyCliSendSpeed"></div>
			<div class="chartDiv" id="NettySrvRecvSpeed"></div>
			<div class="chartDiv" id="MsgDecodeTime"></div>
			<div class="chartDiv" id="RecvCtrlQueue"></div>
			<div class="chartDiv" id="SendCtrlQueue"></div>
			<!-- <div class="chartDiv" id="NettySrvTransmitTime"></div>
			<div class="chartDiv" id="NettyCliSendBatchSize"></div> -->
			<div class="chartDiv" id="HeapMemory"></div>
		</div>
	</div>
</body>
</html>
