<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String clusterName=request.getParameter("clusterName");
String topologyid=request.getParameter("topologyid");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE9"/>
<title>拓扑图</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/topology/base.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/topology/button.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/topology/sweetalert.css" />

<script src="${ctx}/js/topology/sweetalert-dev.js"></script>
<script type="text/javascript" src="${ctx}/js/topology/jtopo-min.js"></script>
<script type="text/javascript" src="${ctx}/js/topology/toolbar.js"></script>
<script type="text/javascript" src="${ctx}/js/topology/topologyDisplay.js"></script>

   <script>
    var graph_url=Globals.ctx+"/api/v2/cluster/${param.clusterName}/topology/${param.id}/graph";
    var clusterName='${param.clusterName}';
	var topologyName='${param.topologyName}';
</script>
<body>
	<div style="width: 100%; height: 100%; vertical-align: middle">
	   <div>
	   <input class="button gray" style="width:100px;margin:5px 5px 5px 30px;" type="button" value="返回" onclick="history.back()" />
	   </div>
	   <div>
		<canvas id="canvas"></canvas>
		</div>
		<div style="text-align: center;">
			<input class="button gray"  style="width:200px;" type="button" value="保存当前显示视图" onclick="saveNodeLocation()" />
			<input class="button gray" style="width:200px;"type="button" value="启动数据刷新" onclick="startRefresh()" /> 
			<input class="button gray" style="width:200px;"type="button" value="启动编辑视图" onclick="suspendRefresh()" />
			
		</div>
	</div>


</body>
</html>