<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>主机资源监控</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/resource/hostResourceMonitor.js"></script>
	<script language="javascript" type="text/javascript" >
	var clusterName='${param.clusterName}';
	</script>
</head>
<body>
<div class="mini-fit">
	<div class="mini-fit" style="margin-top: 5px; ">
		<div id="hostGrid" class="mini-datagrid" style="width: 30%; height: 100%;float:left;"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false"  showFooter="false" 
              onrowclick="onSelectHost" onload="gridOnload">
			<div property="columns">
				<div type="checkcolumn" headerAlign="center" width="30" > </div>
				<div field="host" width="100" headerAlign="center"  align="left">IP地址</div>
				<div field="uptimeSecs" width="90" headerAlign="center" align="center">Uptime</div>
                <div field="" width="60" headerAlign="center" renderer="formatPort" align="center">端口使用</div>
                <div field="" width="40" headerAlign="center" renderer="getConfig" align="center">配置</div>

			</div>
		</div>
		
		<div style="width: 69%;height: 100%;float:left;margin-left:10px;">
			<div class="mini-fit">
				<div id="workerGrid" class="mini-datagrid" style="width: 100%;height: 100%;"
		             idField="host" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  showFooter="false" 
		             onrowclick="repaintCharts">
					
				</div>
			</div>
			<div id="mainChart" style="width: 100%;height:250px;margin-top:5px;"></div>
			</div>
		</div>
  	</div>
</div>
</body>
</html>
