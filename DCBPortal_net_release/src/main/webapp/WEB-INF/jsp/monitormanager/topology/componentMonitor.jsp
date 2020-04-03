<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>component监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/topology/componentMonitor.js"></script>
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
	
		<div class="mini-panel" title="Component统计" style="width:100%;padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"  showToolbar="false"
		>
			<div class="search2" style="border: 0px;margin-right: 2.5px;">
				<table class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
					<tr>
			               <td style="text-align: right;">
				               	<a class="mini-button mini-button-green" style="width:100px;" onclick="componentDataEvent(60)" plain="false">最近1分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="componentDataEvent(600)" plain="false">最近10分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="componentDataEvent(7200)" plain="false">最近2小时</a>
								<a class="mini-button mini-button-green" style="width:100px;" onclick="componentDataEvent(86400)" plain="false">最近24小时</a> 
			               </td>
					</tr>
				</table>
			</div>
			
			<div style="margin:5px; margin-top : auto;">
				<div id="componentGrid" class="mini-datagrid"
					style="width: 100%; height: auto;" idField="id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true" style = "overflow:auto"
					showFooter="false">
						<div property="columns">
						<div field="topologyName"  headerAlign="center" align="center" width="50" renderer="topologyNameRenderer">拓扑ID</div>
						<div field="componentName"   headerAlign="center" align="center" width="30">名称</div>
						<div field="parallel"  headerAlign="center" align="center" width="20">任务数</div>
						<div field="Emitted"   headerAlign="center" align="center" width="20">消息流转</div>
						<div field="Acked"  headerAlign="center" align="center" width="20">处理反馈</div>
		                <div field="SendTps"  headerAlign="center" align="center" width="20">发送(tps)</div>
		                <div field="RecvTps"  headerAlign="center" align="center" width="20">接收(tps)</div>
		                <div field="ProcessLatency"    headerAlign="center" align="center" width="30">处理耗时(us)</div>
		                <div field="DeserializeTime"   headerAlign="center" align="center" width="35">反序列化耗时(us)</div>
		                <div field="SerializeTime"   headerAlign="center" align="center" width="35">序列化耗时(us)</div>
		                <div field="ExecutorTime"   headerAlign="center" align="center" width="30">执行耗时(us)</div>
		                <div field="TupleLifeCycle"   headerAlign="center" align="center" width="30">流转耗时(us)</div>
		                <div field="errors"   headerAlign="center" align="center" width="20" renderer="errorHighlightRenderer">错误</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="任务状态" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div style="margin:5px;">
				<div id="taskStateGrid" class="mini-datagrid"
					style="width: 100%; height: auto" idField="id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="id" headerAlign="center" align="center" width="20">任务ID</div>
						<div field="type" headerAlign="center" align="center" width="20">类型</div>
						<div field="host" headerAlign="center" align="center" width="20">主机</div>
						<div field="port" headerAlign="center" align="center" width="20">端口</div>
						<div field="uptime" headerAlign="center" align="center" width="20">运行时间</div>
						<div field="status" headerAlign="center" align="center" width="20" renderer="stateHighlightRenderer">状态</div>
						<div field="errors" headerAlign="center" align="center" width="20" renderer="errorHighlightRenderer">错误</div>
						<div field="" headerAlign="center" align="center" width="20" renderer="logRenderer">日志</div>
						<div field="" headerAlign="center" align="center" width="20" renderer="jstackLogRenderer">jstack日志</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="任务明细" style="width:100%;padding-top:5px; overflow:auto;" bodyStyle="padding:0px;"
			showCollapseButton="true" showToolbar = "false"
		>
			<div style="margin:5px;">
				<div id="taskInfoGrid" class="mini-datagrid"
					style="width: 100%; height: auto;" idField="host"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<!-- <div type="indexcolumn" width="20"></div> -->
						<div field="host" headerAlign="center" align="center" width="20">任务ID</div>
						<div field="Emitted" headerAlign="center" align="center" width="20">消息流转</div>
						<div field="Acked" headerAlign="center" align="center" width="20">处理反馈</div>
						<div field="SendTps" headerAlign="center" align="center" width="20">发送(tps)</div>
						<div field="RecvTps" headerAlign="center" align="center" width="20">接收(tps)</div>
						<div field="ProcessLatency" headerAlign="center" align="center" width="20">处理耗时(us)</div>
						<div field="DeserializeTime" headerAlign="center" align="center" width="30">反序列化耗时(us)</div>
						<div field="SerializeTime" headerAlign="center" align="center" width="30">序列化耗时(us)</div>
						<div field="DeserializeQueue" headerAlign="center" align="center" width="30">反序列化队列(%)</div>
						<div field="SerializeQueue" headerAlign="center" align="center" width="30">序列化队列(%)</div>
						<div field="ExecutorQueue" headerAlign="center" align="center" width="30">执行队列(%)</div>
						<div field="CtrlQueue" headerAlign="center" align="center" width="30">控制队列(%)</div>
						<div field="TupleLifeCycle" headerAlign="center" align="center" width="30">流转耗时(us)</div>
					</div>
				</div>
			</div>
		</div>
		
	</div>
</body>
</html>
