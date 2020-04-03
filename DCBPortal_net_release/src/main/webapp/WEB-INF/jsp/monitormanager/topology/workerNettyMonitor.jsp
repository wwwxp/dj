<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>拓补监控信息</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
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
	
		<div class="mini-panel" title="netty监控" style="width:100%;padding-top:5px; overflow:auto;" bodyStyle="padding:0px;"
			showCollapseButton="true" showToolbar = "false"
		>
			<div class="search2" style="border: 0px;margin-right: 2.5px;">
				<table class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
					<tr>
			               <td style="text-align: right;">
				               	<a class="mini-button mini-button-green" style="width:100px;" onclick="nettyEvent(60)" plain="false">最近1分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="nettyEvent(600)" plain="false">最近10分钟</a> 
								<a class="mini-button mini-button-green" style="width:100px;" onclick="nettyEvent(7200)" plain="false">最近2小时</a>
								<a class="mini-button mini-button-green" style="width:100px;" onclick="nettyEvent(86400)" plain="false">最近24小时</a> 
			               </td>
					</tr>
				</table>
			</div>
			
			<div style="margin:5px; margin-top : auto;">
				<div id="nettyGrid" class="mini-datagrid"
					style="width: 100%; height: auto;" idField="TASK_ID"
					allowResize="false" allowCellselect="false" multiSelect="true"
					allowResizeColumn = "true"
					showFooter="false">
					<div property="columns">
						<div field="fromWorker" headerAlign="center" align="center" renderer="">发送端</div>
						<div field="toWorker" headerAlign="center" align="center">接收端</div>
						<div field="" headerAlign="center" align="center">NettySrv处理耗时(us)</div>
						<div field="NettyCliSendPending" headerAlign="center" align="center">Netty发送缓存</div>
						<div field="NettyCliSendSpeed" headerAlign="center" align="center">Netty发送(byte/s)</div>
						<div field="NettyCliCacheSize" headerAlign="center" align="center">Netty发送缓冲区</div>
						<div field="NettyCliSendTime" headerAlign="center" align="center">Netty发送耗时</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="mini-panel" title="发展速度趋势" style="width:100%; padding-top:5px;" bodyStyle="padding:0px;"
			showCollapseButton="true"
			id="chartId"
		>
			<div id="main" style="width:auto;height:400px;"></div>
		</div>
		
	</div>
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/topology/workerNettyMonitor.js"></script>
</body>
</html>
