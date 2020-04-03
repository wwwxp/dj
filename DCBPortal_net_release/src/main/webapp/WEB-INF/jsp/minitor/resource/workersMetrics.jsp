<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Nimbus Conf</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/resource/workersMetrics.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="workerGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="host" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  showFooter="false" 
             onrowclick="repaintCharts">
			
		</div>
	</div>
	<br/>
	<div id="mainChart" style="height:300px"></div>
	
    </div>
</body>
</html>
