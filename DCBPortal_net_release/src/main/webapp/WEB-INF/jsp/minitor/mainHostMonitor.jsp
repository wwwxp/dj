<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	
</head>
<body>
	<div class="mini-fit p5">
		<div id="tabs1" class="mini-tabs" activeIndex="0"
			style="width: 100%; height: 100%">

			<div title="任务资源监控"
				url="${ctx}/jsp/minitor/resource/hostResourceMonitor?clusterName=${param.clusterName}"></div>
			<div title="任务压力监控"
				url="${ctx}/jsp/minitor/pressure/hostPressureMonitor?clusterName=${param.clusterName}"></div>
			<div title="主机资源监控"
				url="${ctx}/jsp/minitor/host/resourceMonitor"></div>
			<div title="主机进程监控"
				url="${ctx}/jsp/minitor/process/processMonitor"></div>
		    <div title="在线消息监控"
				url="${ctx}/jsp/minitor/prepaid/prePaidMonitor"></div>
			<div title="离线话单监控"
				url="${ctx}/jsp/minitor/postpaid/postPaidMonitor"></div>
			<div title="结果码维护"
			url="${ctx}/jsp/minitor/resultCode/resultCodeManage"></div>
			<div title="流量无缝结转查询"
			url="${ctx}/jsp/minitor/flow/surplusedFlowQuery"></div>
				
				
				
		</div>
	</div>
</body>
</html>
