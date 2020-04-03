<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机压力监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/componentMetrics.js"></script>


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
		<div id="tcomponentGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="8" >序号</div>
				<div field="componentName"  width="20"headerAlign="center" align="center" >Component</div>
				<div field="parallel" width="15"headerAlign="center" align="center">Tasks</div>
				<div field="Emitted"  width="15"headerAlign="center" align="right">Emitted</div>
				<div field="Acked" width="15"headerAlign="center" align="right">Asked</div>
                <div field="SendTps" width="15"headerAlign="center" align="right">SendTps</div>
                <div field="RecvTps" width="15"headerAlign="center" align="right">RecvTps</div>
                <div field="ProcessLatency"   width="15"headerAlign="center" align="right">Process(us)</div>
<!--                 <div field="DeserializeTime"  width="15"headerAlign="center" align="right">Deser(us)</div> -->
<!--                 <div field="SerializeTime"  width="15"headerAlign="center" align="right">Ser(us)</div> -->
<!--                 <div field="ExecutorTime"  width="15"headerAlign="center" align="right">Emit(us)</div> -->
<!--                 <div field="TupleLifeCycle"  width="25"headerAlign="center" align="right">TupleLifeCycle(us)</div> -->
<!--                 <div field="AckerTime"  width="20"headerAlign="center" align="center">Asker(us)</div> -->
                <div field="errors"  width="20"headerAlign="center" align="center" renderer="formatError">Error</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
