<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>业务集群管理</title>
	<%@ include file="/public/common/common.jsp"%>
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/businesscluster/clusterBusManage.js"></script>
</head>
<body style="overflow: hidden;">
	<div id="clusterProDiv" class="m10" style="margin: 0px 5px 0px 5px;overflow: hidden;">
		<!--目录导航 begin-->
		<table style="width: 100%;" border="0" cellspacing="0" cellpadding="0" class="ml_menu">
		  <tr>
<%-- 		    <td class="hover" style="width: 25%;"><a href="${ctx}/jsp/clustermanager/businesscluster/clusterBusPartition" target="elePages">1、主机划分</a></td>
		    <td style="width: 25%;"><a href="${ctx}/jsp/clustermanager/businesscluster/clusterBusDeploy" target="elePages">2、业务部署 </a></td>
		    <td style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('3')" target="filesPages">3、配置文件修改</a></td>
		    <td class="nobg" style="width: 25%;"><a href="${ctx}/jsp/clustermanager/businesscluster/clusterBusStartAndStop" target="elePages">4、启动/停止</a></td> --%>
		  	
		  	<td class="hover" style="width: 25%;" title="双击刷新数据"><a href="javascript:void(0)" onclick="changeOperator('0', '${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=0')" target="partitionPages">1、主机划分</a></td>
		    <td style="width: 25%;" title="双击刷新数据"><a href="javascript:void(0)" onclick="changeOperator('1', '${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=1')" target="deployPages">2、业务部署 </a></td>
<%-- 		    <td style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('3', '${ctx}/jsp/configuremanager/editconfig/businessconfigure')" target="filesPages">3、配置文件修改</a></td> --%>
		   	<td style="width: 25%;" title="双击刷新数据"><a href="javascript:void(0)" onclick="changeOperator('2', '${ctx}/jsp/clustermanager/businesscluster/clusterBusConfig?index=2')" target="filesPages">3、配置文件修改</a></td>
		    <td class="nobg" style="width: 25%;" title="双击刷新数据"><a href="javascript:void(0)" onclick="changeOperator('3', '${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=3')" target="startPages">4、启动/停止</a></td>
		  </tr>
		</table>
	</div>
	<div id="frameDiv" style="margin: 0px 5px 0px 5px;" class="mini-fit">
<%-- 		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=0" id="partitionPages" name="partitionPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=1" id="deployPages" name="deployPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusConfig?index=2" id="filesPages" name="filesPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=3" id="startPages" name="startPages"></iframe>
 --%>	
 		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusTabs?index=0" id="partitionPages" name="partitionPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="about:blank;" id="deployPages" name="deployPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/clustermanager/businesscluster/clusterBusConfig?index=2" id="filesPages" name="filesPages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="about:blank;" id="startPages" name="startPages"></iframe>
	</div>
</body>
</html>