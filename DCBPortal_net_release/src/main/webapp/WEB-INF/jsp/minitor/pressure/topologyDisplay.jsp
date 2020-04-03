<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page language="java" import="com.tydic.util.SessionUtil" %>
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
<style type="text/css">
	.contextmenu {
		border: 1px solid #aaa;
		border-bottom: 0;
		background: #eee;
		position: absolute;
		list-style: none;
		margin: 0;
		padding: 0;
		display: none;
	}
																			   
	.contextmenu li a {
		display: block;
		padding: 10px;
		border-bottom: 1px solid #aaa;
		cursor: pointer;
	}
																			   
	.contextmenu li a:hover {
		background: #fff;
	}
</style>
<script src="${ctx}/js/topology/sweetalert-dev.js"></script>
<script type="text/javascript" src="${ctx}/js/topology/jtopo-min.js"></script>
<script type="text/javascript" src="${ctx}/js/topology/toolbar.js"></script>
<script type="text/javascript" src="${ctx}/js/monitor/pressure/topologyDisplay.js"></script>

   <script>
    var graph_url=Globals.ctx+"/api/v2/cluster/${param.clusterName}/topology/${param.id}/graph";
    var clusterName='${param.clusterName}';
	var topologyName='${param.topologyName}';
//	var imageRootPath=re'<%=SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")%>';
</script>
<body>

	<div style="width: 100%; height: 100%; vertical-align: middle">
	<ul id="contextmenu"  class="contextmenu" style="display:none;">	
	<li><a>添加节点</a></li>
  </ul>
  <ul id="nodeContextmenu" class="contextmenu"  style="display:none;">	
	<li><a>删除节点</a></li>
  </ul>
	
		<canvas id="canvas" ></canvas>
		<div style="text-align: center;">
		     <a class="mini-button" width="150px" onclick="saveNodeLocation()" plain="false">保存当前显示视图</a>
		     <a class="mini-button" width="150px" onclick="startRefresh()" plain="false">启动数据刷新</a>
		     <a class="mini-button" width="150px" onclick="suspendRefresh()" plain="false">启动编辑视图</a>
		</div>
	</div>

</body>
</html>