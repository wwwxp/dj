<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-后付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/host/resourceMonitor.js"></script>
	
</head>
<body>
<div  style="width:100%;height:100%">
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="100"/>
					<col />
					<col width="100"/>
					<col />
					<col />
					<col />
				</colgroup>
				<tr>
				<th>
	            <span>主机：</span>
	           	</th>
				<td >
					<input id="HOST" name="HOST"  class="mini-textbox" style="width:100%;"/>
				</td>
				<td>
	           	<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
	           	</td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="resourceGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" pageSize="99999" allowCellWrap="true"  
             onrowclick="onclickRow" onload="gridOnload" showFooter="false" showLoading="false">
			<div property="columns">
			     <div type="checkcolumn"></div>   
<!-- 				<div field="RESOURCE_ID" width="90" headerAlign="center" align="center">资源ID</div> -->
				<div field="HOST" width="80" headerAlign="center" align="center">主机IP</div>
				<div field="CPU" width="90" headerAlign="center" align="center">CPU使用率(%)</div>
				<div field="MEM_TOTAL" width="80" headerAlign="center" align="center">内存总量(MB)</div>
				<div field="MEM_USE" width="80" headerAlign="center" align="center">内存使用量(MB)</div>
				<div field="DISK_TOTAL" width="80" headerAlign="center" align="center">磁盘总量(GB)</div>
				<div field="DISK_USE" width="80" headerAlign="center" align="center">磁盘使用量(GB)</div>
				<div field="PROC_NUM" width="80" headerAlign="center" align="center">业务进程数</div>
				<div field="REPORT_TIME" width="80" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">上报时间</div>
				
			</div>
		</div>
	</div>
    </div>
    <div style="height:250px;">
	<div id="mainChart" style="height:100%;width:60%;float:left;"></div>
	<div id="gaugeChart" style="height:100%;width:40%;float:left;">
	</div>
	</div>
	</div>
	
	<div id="diskChart" style="height:400px;width:100%;margin-top:20px;"></div>
</body>
</html>
