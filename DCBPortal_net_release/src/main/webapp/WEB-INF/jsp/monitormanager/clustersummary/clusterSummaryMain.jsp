<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>集群摘要</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/clustersummary/clusterSummaryMain.js"></script>
</head>
<body>
	<div style="padding:10px;" class="monitor" >
		<%--<div id="cluster_tabs" class="mini-tabs" activeIndex="0" style="margin-bottom:0px;"--%>
			<%--plain="false" tabAlign="left" tabPosition="top" >--%>
			<%----%>
		<%--</div>--%>
		<%--<div style="height:20px;text-align: right">--%>
			<%--<span style="margin-right: 10px"><a class="mini-button mini-button-green" onclick="javascript:window.location.reload();" plain="false">刷新</a> </span>--%>
		<%--</div>--%>
		<!-- <div class="headTitle">集群信息</div> -->
		<div class="mini-panel" id="resultButton" title="集群信息" style="width:100%;" bodyStyle="padding:0px;" showCollapseButton="true">
			<div style="margin:5px;">
				<div id="clusterGrid" class="mini-datagrid" style="width:100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="clusterName" width="20" headerAlign="center" align="center">ZK集群名称</div>
						<div field="supervisors" width="20" headerAlign="center" align="center">Supervisor数量</div> 
						<div field="slotsUsed" width="20" headerAlign="center" align="center" renderer="clusterSlotsScale">端口使用</div>
						<div field="topologies" width="20" headerAlign="center" align="center">应用总数</div>
						<div field="" width="20" headerAlign="center" align="center" renderer="resultButton">操作</div>
					</div>
				</div>
			</div>
		</div>


			<div class="mini-panel" title="应用信息" style="width:100%" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="topologyGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div type="indexcolumn" headerAlign="center" align="center"  width="5" >序号</div>
						<div field="name" width="20" headerAlign="center" align="center" renderer="topNameRenderer">应用名</div>
						<!-- <div field="id" width="19" headerAlign="center" align="center">拓扑ID</div> -->
						<div field="status" width="9" headerAlign="center" align="center" renderer="topStateRenderer">状态</div>
						<div field="uptime" width="9" headerAlign="center" align="center">运行时间</div>
						<div field="workersTotal" width="6" headerAlign="center" align="center">节点数</div>
						<!-- <div field="workersTotal" width="6" headerAlign="center" align="center">机器数</div> -->
						<div field="tasksTotal" width="6" headerAlign="center" align="center">任务数</div> 
						<div field="" width="7" headerAlign="center" align="center" renderer="topConfRenderer">配置</div>
						<div field="errorInfo" width="8" headerAlign="center" align="center">错误</div>
					</div>
				</div>
			</div>
		</div>
		
		
		
		<div class="mini-panel" title="nimbus信息" style="width:100%" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="nimbusGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="status" width="10" headerAlign="center" align="center">角色</div>
						<div field="host" width="10" headerAlign="center" align="center">主机</div>
						<div field="port" width="10" headerAlign="center" align="center">端口</div>
						<div field="nimbusUpTime" width="10" headerAlign="center" align="center">运行时间</div>
						<div field="" width="10" headerAlign="center" align="center" renderer="nimbusConf">配置</div>
						<div field="" width="10" headerAlign="center" align="center" renderer="nimbusLog">日志</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="supervisor信息" style="width:100%" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="supervisorGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="host" width="18" headerAlign="center" align="center" renderer="superHostRenderer">主机</div>
						<div field="uptime" width="18" headerAlign="center" align="center">运行时间</div>
						<div field="slotsUsed" width="18" headerAlign="center" align="center" renderer="superSlotsUsedRenderer">端口使用</div>
						<div field="" width="18" headerAlign="center" align="center" renderer="superConfRenderer">配置</div>
						<div field="" width="18" headerAlign="center" align="center" renderer="superLogRenderer">日志</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="zookeeper信息" style="width:100%" bodyStyle="padding:0px;" showCollapseButton="true" >
			<div style="margin:5px;">
				<div id="zookeeperGrid" class="mini-datagrid" style="width: 100%;"
		             idField="id" allowResize="false" allowCellselect="false" showFooter="false" >
					<div property="columns">
						<div field="host" width="30" headerAlign="center" align="center">主机</div>
						<div field="port" name="port" width="30" headerAlign="center" align="center">端口</div>
						<div field="" name="zkInfo" width="30" headerAlign="center" align="center" renderer="zkInfoRenderer">信息</div>
					</div>
				</div>
			</div>
		</div>
		<div class="mini-panel" title="运行趋势" style="width:100%" bodyStyle="padding:0px;" showCollapseButton="true" expanded="false">
			<div class="runTrent">
				<div class="chartList" id ="Failed">
					
				</div>
				<div class="chartList" id="Emitted">
					
				</div>
				<div class="chartList" id="Acked">
					
				</div>
			</div>
			<div class="runTrent">
				<div class="chartList" id="SendTps">
					
				</div>
				<div class="chartList" id="RecvTps">
					
				</div>
				<div class="chartList" id="Process">
					
				</div>
			</div>
			<div class="runTrent">
				<div class="chartList" id="CPUUsed">
					
				</div>
				<div class="chartList" id="MemUsed">
					
				</div>
				<div class="chartList" id="HeapMemory">
					
				</div>
			</div>
		</div>
	</div>
</body>
</html>
