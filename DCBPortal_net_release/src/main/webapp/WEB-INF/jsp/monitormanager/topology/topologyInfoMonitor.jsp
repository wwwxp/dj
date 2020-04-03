<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>拓补监控信息</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<%@ include file="/public/common/common.jsp"%>
	<%-- <jsp:include page="../../jstorm/layout/_head.jsp"/> --%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
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
	
		<div class="mini-panel" title="任务信息" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div style="margin:5px;">
				<div id="topologyGrid" class="mini-datagrid" style="width: 100%; height: auto;"
		             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" 
		             showFooter="false"  selectOnLoad = "true">
					<div property="columns">
						<!-- <div field="name" headerAlign="center" align="center"  width="20" renderer="">拓扑</div> -->
						<div field="id"  width="23" headerAlign="center" align="center" >应用名称</div>
						<div field="id"  width="10" headerAlign="center" align="center"  renderer="versionTRenderer">版本号</div>
						<div field="status" width="10" headerAlign="center" align="center" renderer="stateHighlightRenderer">状态</div>
						<div field="uptime"  width="15" headerAlign="center" align="center" >运行时间</div>
						<div field="numWorkers" width="10" headerAlign="center" align="center">实例总数</div>
						<div field="errorWorkers" width="10" headerAlign="center" align="center" renderer="errorWorkersRenderer">异常实例数</div>
						<div field="hostWorkers" width="10" headerAlign="center" align="center"  renderer="hostWorkers">分布节点数</div>
						<div field="numTasks" width="10" headerAlign="center" align="center">任务数</div>
						<div field="" width="15" headerAlign="center" align="center" renderer="configRenderer">配置</div>
						<div field="errorInfo" width="5" headerAlign="center" align="center" renderer="errorHighlightRenderer">错误</div>
					</div>
				</div>
			</div>
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
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="componentName" width="40" headerAlign="center" align="center" renderer="componentNameRenderer">名称</div>
						<div field="parallel" width="20" headerAlign="center" align="center">任务数</div>
						<div field="Emitted" width="20" headerAlign="center" align="center">消息流转</div>
						<div field="Acked" width="20" headerAlign="center" align="center">成功处理</div>
						<div field="SendTps" width="20" headerAlign="center" align="center">发送(tps)</div>
						<div field="RecvTps" width="20" headerAlign="center" align="center">接收(tps)</div>
						<div field="ProcessLatency" width="20" headerAlign="center" align="center" renderer="formatTime">处理耗时</div>
						<div field="DeserializeTime" width="20" headerAlign="center" align="center">反序列化耗时</div>
						<div field="SerializeTime" width="20" headerAlign="center" align="center">序列化耗时</div>
						<div field="ExecutorTime" width="20" headerAlign="center" align="center" renderer="formatTime">执行耗时</div>
						<div field="TupleLifeCycle" width="20" headerAlign="center" align="center" renderer="formatTime">流转耗时</div>
						<!-- <div field="AckerTime" width="20px" headerAlign="center" align="center">消息生命周期</div> -->
						<div field="errors" width="20px" headerAlign="center" align="center" renderer="errorArrayRenderer">错误</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="work统计" style="width:100%;padding-top:5px; overflow:auto;" bodyStyle="padding:0px;"
			showCollapseButton="true" showToolbar = "false"
		>
			<div class="search2" style="border: 0px;margin-right: 2.5px;">
				<table class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
					<tr>
			               <td style="text-align: right;">
				               	<a class="mini-button mini-button-green" style="width:100px;" onclick="workerDataEvent(60)" plain="false">最近1分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="workerDataEvent(600)" plain="false">最近10分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="workerDataEvent(7200)" plain="false">最近2小时</a>
								<a class="mini-button mini-button-green" style="width:100px;" onclick="workerDataEvent(86400)" plain="false">最近24小时</a> 
			               </td>
					</tr>
				</table>
			</div>
			
			<div style="margin:5px; margin-top : auto;">
				<div id="workerGrid" class="mini-datagrid"
					style="width: 100%; height: auto;" idField="id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="host" headerAlign="center" align="center" width="150" renderer="supervisorMonitorRenderer">主机</div>
						<div field="port" headerAlign="center" align="center" width="60">端口</div>
						<div field="" headerAlign="center" align="center" width="60" renderer="nettyMonitorRenderer">Netty</div>
						<div field="CpuUsedRatio" headerAlign="center" align="center" width="50">CPU(%)</div>
						<div field="MemoryUsed" headerAlign="center" align="center" width="60">内存</div>
						<div field="NettyCliSendSpeed" headerAlign="center" align="center" width="90">Netty发送(tps)</div>
						<div field="NettySrvRecvSpeed" headerAlign="center" align="center" width="90">Netty接收(tps)</div>
						<div field="MsgDecodeTime" headerAlign="center" width="100" align="center" renderer="formatTime">Decode耗时(us)</div>
						<div field="NettySrvTransmitTime" headerAlign="center" width="120" align="center" renderer="formatTime">NettySrv处理耗时(us)</div>
						<div field="NettyCliSendBatchSize" headerAlign="center" width="80" align="center">批量发送</div>
						<div field="RecvCtrlQueue" headerAlign="center" align="center" width="90" >Ctrl接收队列(%)</div>
						<div field="SendCtrlQueue" headerAlign="center" align="center" width="90" >Ctrl发送队列(%)</div>
						<div field="HeapMemory" headerAlign="center" align="center" width="80"  renderer="formatterHeapMemory">堆内存</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="task统计" style="width:100%;padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
		>
			<div style="margin:5px;">
				<div id="taskGrid" class="mini-datagrid"
					style="width: 100%; height: auto" idField="task_id"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="id" headerAlign="center" align="center">任务Id</div>
						<div field="component" headerAlign="center" align="center">名称</div>
						<div field="type" headerAlign="center" align="center">类型</div>
						<div field="host" headerAlign="center" align="center">主机</div>
						<div field="port" headerAlign="center" align="center">端口</div>
						<div field="uptime" headerAlign="center" align="center">运行时间</div>
						<div field="status" headerAlign="center" align="center" renderer="stateHighlightRenderer">状态</div>
						<div field="errors" headerAlign="center" align="center" renderer="errorArrayRenderer">错误</div>
					</div>
				</div>
			</div>
		</div>
		<div class="mini-panel" title="Topology Graph" style="width:100%;padding-top:5px;" bodyStyle="padding:0px;" 
			showCollapseButton="true" id ="topologygraph" expanded="true">
	           <div id="topology-graph"></div>
	           <div id="graph-event">
	               <div v-show="valid" style="display: none;width:95%;">
	                   <h4 style="margin-top: 0;">{{title}}</h4>
	                   <table class="table table-bordered table-hover table-striped" style="table-layout: fixed;">
	                     <thead>
	                       <tr>
	                           <th>Window</th>
	                           <th v-repeat="head" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" title="{{$value}}">
	                               {{$value}}
	                           </th>
	                       </tr>
	                     </thead>
	                     <tbody>
	                       <tr>
	                           <td>1 min</td>
	                           <td v-repeat="head" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" title="{{mapValue[$value][60]}}">
	                               {{mapValue[$value][60]}}
	                           </td>
	                       </tr>
	                       <tr>
	                           <td>10 min</td>
	                           <td v-repeat="head" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" title=" {{mapValue[$value][600]}}">
	                               {{mapValue[$value][600]}}
	                           </td>
	                       </tr>
	                       <tr>
	                           <td>2 hour</td>
	                           <td v-repeat="head" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" title="{{mapValue[$value][7200]}}">
	                               {{mapValue[$value][7200]}}
	                           </td>
	                       </tr>
	                       <tr>
	                           <td>1 day</td>
	                           <td v-repeat="head" style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis;" title="{{mapValue[$value][86400]}}">
	                               {{mapValue[$value][86400]}}
	                           </td>
	                       </tr>
	                     </tbody>
	                   </table>
	               </div>
	            </div>
        </div>
        
		<div class="mini-panel" id="chartPanel" title="运行趋势" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true" expanded="true">	
			<div class="runTrent">
				<div id="filedChart" class="chartList"></div>
				<div id="emittedChart" class="chartList"></div>
				<div id="ackedChart" class="chartList"></div>
			</div>
			<div class="runTrent">
				<div id="sendTpsChart" class="chartList"></div>
				<div id="recTpsChart" class="chartList"></div>
				<div id="processChart" class="chartList"></div>
			</div>
				<div class="runTrent">
				<div id="cpuChart" class="chartList"></div>
				<div id="memUsedChart" class="chartList"></div>
				<div id="heapMemoryChart" class="chartList"></div>
			</div>
		</div>
		
		
		
	</div>
	
<script src="${ctx}/assets/js/vis.min.js"></script>
<script src="${ctx}/assets/js/vue.min.js"></script>
<script src="${ctx}/assets/js/storm.js"></script>
<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/topology/topologyInfoMonitor.js"></script>
<script>
   $(function () {
       
   });
</script>
</body>
</html>
