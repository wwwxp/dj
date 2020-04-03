<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>supervisor监控信息</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/topology/supervisorMonitor.js"></script>
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
	
		<div class="mini-panel" title="supervisor摘要" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div style="margin:5px;">
				<div id="supervisorInfoGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" 
		             showFooter="false" >
					<div property="columns">
						<div field="host" headerAlign="center" align="center"  width="10" renderer="hostIPRenderer">主机</div>
						<div field="uptime"  width="15" headerAlign="center" align="center" >运行时间</div>
						<div field="ip" width="10" headerAlign="center" align="center">端口使用</div>
						<div field="" width="15"headerAlign="center" align="center" renderer="configRnderer">配置</div>
						<div field="" width="15"headerAlign="center" align="center" renderer="supervisorLogRenderer">日志</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="worker使用情况" style="width:100%;padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"  showToolbar="false"
		>
			<div style="margin:5px;">
				<div id="workUsedGrid" class="mini-datagrid"
					style="width: 100%; height: aoto;" idField="id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true" style = "overflow:auto"
					showFooter="false">
					<div property="columns">
						<div field="port" headerAlign="center" align="center" renderer="portRenderer" width="10">端口</div>
						<div field=""          headerAlign="center" align="center" width="10" renderer="nettyRenderer">Netty</div>
						<div field="uptime" headerAlign="center" align="center" width="25">运行时间</div>
						<div field="topology" headerAlign="center" align="center" width="40">拓扑</div>
						<div field="tasks" headerAlign="center" align="center" width="30" renderer="runTaskRenderer">运行任务</div>
						<div field="" headerAlign="center" align="center" width="10" renderer="logRenderer">日志</div>
						<div field="" headerAlign="center" align="center" width="10" renderer="jstackLogRenderer">jstack日志</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="worker统计" style="width:100%;padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true" showToolbar = "false"
		>
			<div style="margin:5px;">
				<div id="workerGrid" class="mini-datagrid"
					style="width: 100%; height: auto;" idField="id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="port" headerAlign="center" align="center" width="5">端口</div>
						<div field="CpuUsedRatio" headerAlign="center" align="center" width="10">CPU使用(%)</div>
						<div field="MemoryUsed" headerAlign="center" align="center" width="10">内存使用</div>
						<div field="NettyCliSendSpeed" headerAlign="center" align="center" width="10">Netty发送(tps)</div>
						<div field="NettySrvRecvSpeed" headerAlign="center" align="center" width="10">Netty接收(tps)</div>
						<div field="MsgDecodeTime" headerAlign="center" align="center" width="10">Decode耗时(us)</div>
						<div field="NettySrvTransmitTime" headerAlign="center"  align="center" width="10">NettySrv处理耗时</div>
						<div field="NettyCliSendBatchSize" headerAlign="center"  align="center" width="10">批量发送</div>
						<div field="RecvCtrlQueue" headerAlign="center" align="center" width="10">Ctrl接收队列(%)</div>
						<div field="SendCtrlQueue" headerAlign="center" align="center" width="10">Ctrl发送队列(%)</div>
						<div field="HeapMemory" headerAlign="center" align="center"  width="10"renderer="formatterHeapMemory">堆内存</div>
					</div>
				</div>
			</div>
		</div>
		
	</div>
</body>
</html>
