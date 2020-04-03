<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机压力监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/workerMetrics.js"></script>


</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	<div style="width: 100%;">
        <div class="search" id="queryFrom" style="padding: 0px;height: 50px;">
            <table style="width: 100%;height: 100%;">
                <tr>
                    <td width="300">
	                    <a class="mini-button" onclick="switchComponent('60')" plain="false">1 min</a>
	                    <a class="mini-button" onclick="switchComponent('600')" plain="false">10 min</a>
                    	<a class="mini-button" onclick="switchComponent('7200')" plain="false">2 hours</a> 
                        <a class="mini-button" onclick="switchComponent('86400')" plain="false">1 day</a> 
                    </td>
                    
                </tr>
            </table>
        </div>
    </div>
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="workerGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="5" >序号</div>
				<div field="host"  width="20"headerAlign="center" align="center" >Host</div>
				<div field="port" width="10"headerAlign="center" align="center">Port</div>
				<div field=""  width="10"headerAlign="center" align="center" renderer="formatNetty">Netty</div>
				<div field="CpuUsedRatio" width="15"headerAlign="center" align="right">CPU Used(%)</div>
				<div field="DiskUsage" width="15"headerAlign="center" align="right">Disk Used(%)</div>
                <div field="MemoryUsed" width="15"headerAlign="center" align="right">Mem Used</div>
                <div field="NettyClientSendSpeed" width="20"headerAlign="center" align="right">NettyClientSendSpeed</div>
                <div field="NettyServerRecvSpeed"   width="20"headerAlign="center" align="right">NettyServerRecvSpeed</div>
                <div field="NetworkMsgDecodeTime"  width="25"headerAlign="center" align="right">Network Msg Decode Time(us)</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
