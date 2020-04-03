<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-后付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/process/processMonitor.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	<div   style="width: 39%; height: 100%;float:left;">
	<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="50"/>
					<col />
					<col width="100"/>
					<col width="150"/>
				</colgroup>
				<tr>
				<th>
	            <span>主机：</span>
	           	</th>
				<td >
					<input id="HOST" name="HOST"  class="mini-textbox" style="width:100%;"/>
				</td>
				<td>
	           	<a class="mini-button" onclick="queryProcAbtract()" style="margin-left: 20px;">查询</a>
	           	</td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="padding-top: 5px;">
	
		<div id="processAbstractGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
              showFooter="true" onrowclick="abstractGridClick" onload="abstractGridOnload" >
			<div property="columns">
			    <div type="checkcolumn" width="20"></div>
				<div field="HOST" width="105" headerAlign="center" align="center">主机IP</div>
				<div field="AVTIVE_STATE" width="70" headerAlign="center" align="center">运行进程</div>
				<div field="STOP_STATE" width="70" headerAlign="center" align="center">停止进程</div>
				<div field="PID_NUM" width="70" headerAlign="center" align="center" >进程总量</div>
<!-- 				<div field="CPU_TOTAL" width="70" headerAlign="center" align="center">CPU(%)</div> -->
<!-- 				<div field="MEM_USE_TOTAL" width="70" headerAlign="center" align="center">内存(MB)</div> -->
				
			</div>
		</div>
	</div>
	</div>
	
	<div style="width: 60.5%; height: 100%;float:left;margin-left:5px;">
	
	<div class="mini-fit" >
	     <div  id="processPanel" name="processPanel" class="mini-panel" title="主机"
			style="width: 100%; height: 100%;"  bodyStyle="padding:0px;"
			showCloseButton="false"   showCollapseButton="false">
			<div class="mini-fit p5" >
			<div id="processForm" class="search">
			<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="100"/>
					<col />
					<col width="100"/>
					<col width="40%"/>
				</colgroup>
				<tr>
				<th>
	            <span>进程名称：</span>
	           	</th>
				<td >
					<input id="PROC_NAME" name="PROC_NAME"  class="mini-textbox" style="width:100%;"/>
				</td>
				<td>
	           	<a class="mini-button" onclick="queryProcInfo()" style="margin-left: 20px;">查询</a>
	           	</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" >
		<div id="processGrid" class="mini-datagrid" style="width: 100%; height: 100%;margin-top:5px;"
             idField="PROC_RCD_ID" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
              showFooter="true" >
			<div property="columns">
				<div field="PROC_NAME" width="60" headerAlign="center" align="center">进程名称</div>
				<div field="PID" width="60" headerAlign="center" align="center">进程PID</div>
				<div field="START_TIME" width="90" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">启动时间</div>
				<div field="STOP_TIME" width="90" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">停止时间</div>
				<div field="CPU" width="73" headerAlign="center" align="center">进程CPU占用率(%)</div>
				<div field="MEM_USE" width="70" headerAlign="center" align="center">进程内存占用(MB)</div>
				<div field="REPORT_TIME" width="90" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">上报时间</div>
				
			</div>
		</div>
		</div>
		</div>
		</div>
	</div>
	
	</div>
		
    </div>

</body>
</html>
