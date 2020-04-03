<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
	<title>部署拓扑图</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/graph/style.css"/>
	<script language="javascript" type="text/javascript" src="${ctx}/js/graph/deployTopologyGraph.js"></script>
</head>
<body style="overflow-y: auto;overflow-x:auto;min-width:1200px;">
	<div style="width:160px;height: 100%;position: absolute;">
		<div id="staticsServer" style="height: 467px; background-color: #FDFDFD; border: 1px solid #e3e3e3; margin: 5px 5px 0px 5px; border-radius: 5px; ">
			<p style="font: 14px Consolas;text-align: center;background: #e3e3e3;font-family: 微软雅黑;height: 25px;padding-top: 5px;">集群部署汇总信息</p>
	  		<div style="padding-left:5px;padding-right:5px;padding-top: 8px;font-size: 14px;">
	  			总主机数：<font id="totalHost" style="font-weight: bold;"></font>台  <br/> <br/>    
	  			应用服务器集群部署占用主机数：<font id="appHost" style="font-weight: bold;"></font>台<br/><br/>    
	  			组件服务器群部署占用主机数：<font id="comHost" style="font-weight: bold;"></font>台 <br/> <br/>     
	  			空闲主机数：<font id="surplusHost" style="font-weight: bold;"></font>台
	  		</div>
		</div>
	</div>
	<div style="margin-left:160px;float: left;width:100%;">
		<div id="appServer" style="height:200px;background-color: #FDFDFD;border: 1px solid #e3e3e3;margin: 5px 5px 0px 5px;border-radius: 5px;">
			<p style="font: 14px Consolas;padding-left: 5px;text-align: center;background: #e3e3e3;font-family: 微软雅黑;height: 25px;padding-top: 5px;">应用服务器集群</p>
		
		</div>
		<div id="arrowDiv" style="height:60px;">
			<div style="height: 100%;float: left;width: 40%;">
				<img src="${ctx}/images/deployGraph/arrow.png" style="margin-left: 80%;width:50px;margin-top:5px;">
			</div>
			<div style="height: 100%;float: left;width: 40%;">
				<img src="${ctx}/images/deployGraph/arrow.png" style="margin-left: 80%;width: 50px;margin-top: 5px;">
			</div>
			<div style="clear: both;"></div>
		</div>
		
		<div id="componentServer" style="height: 200px; background-color: #FDFDFD; border: 1px solid #e3e3e3; margin: 5px 5px 0px 5px; border-radius: 5px;">
			<p style="font: 14px Consolas;padding-left: 5px;text-align: center;background: #e3e3e3;font-family: 微软雅黑;height: 25px;padding-top: 5px;">组件服务器集群</p>
		</div>
	</div>
	
	<div id="template" style="display: none;">
		<div id="appTamplate">
			<div style="min-width:140px;height: 70%;text-align: center;margin: 10px;/* background-color: #4EB3E1; */border: 2px dashed #4EB3E1;float: left;/* border-bottom: 4px solid #E3E3E3; *//* border-radius: 5px; */border-radius: 5px;">
				<table style="height: 100%;table-layout: fixed;padding: 0;text-align: center;width: 140px;">
					<tbody>
					<tr>
						<td>
							<img src="${ctx}/images/deployGraph/deploy_host2.png" style="width: 52px;height: 52px;padding-left: 3px;padding-right: 3px;">
						</td>
					</tr>
					<tr>
						<td>采集服务器组<br>
							(2台, JAVA/C++)
						</td>
					</tr>
				</tbody></table>
			</div>
		</div>
		<div id="componentTemplate">
			<div style="min-width:140px;height: 70%;text-align: center;margin: 10px;border: 2px dashed #FFC156;float: left;border-radius: 5px;">
				<table style="height: 100%;table-layout: fixed;padding: 0;text-align: center;width: 140px;">
					<tbody>
						<tr>
							<td>
								<img src="${ctx}/images/deployGraph/deploy_host.png" style="width: 52px;height:52px;">
							</td>
						</tr>
						<tr>
							<td>dca服务器组<br>(99台)</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>
