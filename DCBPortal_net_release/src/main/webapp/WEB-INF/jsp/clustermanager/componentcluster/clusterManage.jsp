<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>组件集群</title>
	<%@ include file="/public/common/common.jsp"%>
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/componentcluster/clusterManage.js"></script>
</head>
<body style="overflow: hidden;">
	<div id="clusterProDiv" class="m10" style="margin: 0px 5px 0px 5px;overflow: hidden;">
		<!--目录导航 begin-->
		<table style="width: 100%;" border="0" cellspacing="0" cellpadding="0" class="ml_menu">
		  <tr>
		    <%-- <td class="hover" style="width: 25%;"><a href="${ctx}/jsp/clustermanager/componentcluster/clusterPartition" target="elePages">1、主机划分</a></td>
		    <td style="width: 25%;"><a href="${ctx}/jsp/clustermanager/componentcluster/clusterDeploy" target="elePages">2、组件部署</a></td>
		    <td style="width: 25%;"><a href="${ctx}/jsp/configuremanager/editconfig/platformconfigure" target="elePages">3、配置文件修改</a></td>
		    <td class="nobg" style="width: 25%;"><a href="${ctx}/jsp/clustermanager/componentcluster/clusterStartAndStop" target="elePages">4、启动/停止</a></td> --%>
		  	<td class="hover" style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('1', '${ctx}/jsp/clustermanager/componentcluster/clusterPartition')" target="elePages">1、主机划分</a></td>
		    <td style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('2', '${ctx}/jsp/clustermanager/componentcluster/clusterDeploy')" target="elePages">2、组件部署</a></td>
		    <td style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('3', '${ctx}/jsp/configuremanager/editconfig/platformconfigure')" target="filesPages">3、配置文件修改</a></td>
		    <td class="nobg" style="width: 25%;"><a href="javascript:void(0)" onclick="changeOperator('4', '${ctx}/jsp/clustermanager/componentcluster/clusterStartAndStop')" target="elePages">4、启动/停止</a></td>
		  </tr>
		</table>
	</div>
	<div style="border: #b1c3e0 0px solid;margin: 0px 5px 0px 5px;" class="mini-fit">
		<iframe frameborder="0" style="width: 100%;height: 100%;display: block;" src="${ctx}/jsp/clustermanager/componentcluster/clusterPartition" id="elePages" name="elePages"></iframe>
		<iframe frameborder="0" style="width: 100%;height: 100%;display: none;" src="${ctx}/jsp/configuremanager/editconfig/businessconfigure" id="filesPages" name="filesPages"></iframe>
	</div>
</body>
</html>