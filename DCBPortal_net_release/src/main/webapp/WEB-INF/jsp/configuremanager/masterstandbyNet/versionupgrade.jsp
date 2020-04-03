<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台-版本切换</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript"
		src="${ctx}/js/configuremanager/masterstandbyNet/versionupgrade.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<div class="mini-fit">
			<div style="width: 48%;height: 100%;float: left;">
				<div id="currForm" class="search">
					<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
						<colgroup>
							<col width="120px"/>
							<col />
						</colgroup>
						<tr>
			            	<th>
			            		<span>当前运行版本：</span>
			            	</th>
							<td>
								<input id="TOPOLOGY_LIST" name="TOPOLOGY_LIST" class="mini-combobox" style="width: 100%;" 
									onvaluechanged="changeTopology();"
									textField="PROGRAM_TEXT" valueField="ID" showNullItem="false" allowInput="false"/>
							</td>
						</tr>
					</table>
				</div>
				<div class="mini-fit" style="margin-top: 5px;">
					<div class="mini-panel" title="当前运行程序节点运行状态" style="width: 100%;height: 100%;">
						<div id="runningGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				             idField="HOST_IP" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
							<div property="columns">
								<div type="checkcolumn" width="20" headerAlign="center" align="center"></div>
								<div field="HOST_IP" width="120" headerAlign="center" align="center">主机IP</div>
								<div field="STATUS" width="80" headerAlign="center" align="center" renderer="statusRenderer">状态</div>							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div style="width:4%;height: 100%;float: left;overflow: hidden;text-align: center;">
				<div class="mini-fit">
					<div id="arrowDiv" style="height:100%;width:100%;">
						<%-- <div style="height: 30%;width: 100%;">
							<img src="${ctx}/images/arrow.png" style="width:32px;height:32px;margin-top:25%;">
						</div> --%>
						<div style="height: 100%;width: 100%;">
							<img src="${ctx}/images/arrow_01.png" style="width:32px;height:32px;position:relative;top:50%;transform:translateY(-50%);">
						</div>
						<%-- <div style="height: 40%;width: 100%;">
							<img src="${ctx}/images/arrow.png" style="width:32px;height:32px;margin-top:25%;">
						</div> --%>
						<!-- <div style="clear: both;"></div> -->
					</div>
				</div>
			</div>
			
			<div style="width: 48%;height: 100%;float: right;">
				<div id="upgradForm" class="search">
					<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
						<colgroup>
							<col width="120px"/>
							<col />
						</colgroup>
						<tr>
			            	<th>
			            		<span>待升级版本：</span>
			            	</th>
							<td>
								<input id="UPGRAD_LIST" name="UPGRAD_LIST" class="mini-combobox" style="width: 100%;"
									onvaluechanged="changeUpgradTopology();"
									textField="PROGRAM_TEXT" valueField="ID" showNullItem="false" allowInput="false"/>
							</td>
						</tr>
					</table>
				</div>
				<div class="mini-fit" style="margin-top: 5px;">
					<div class="mini-panel" title="待升级程序节点运行状态" style="width: 100%;height: 100%;">
						<div id="upgradGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				             idField="HOST_IP" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
							<div property="columns">
								<!-- <div type="checkcolumn" width="20" headerAlign="center" align="center"></div> -->
								<div field="HOST_IP" width="120" headerAlign="center" align="center">主机IP</div>
								<div field="STATUS" width="80" headerAlign="center" align="center" renderer="statusRenderer">状态</div>							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="mini-toolbar" style="height:45px;text-align: center;margin-top:5px;">
   			<a class="mini-button mini-button-green" onclick="upgradeVersion()" id="sumbitButton" style="margin-top:10px;">灰度升级</a> 
   			<span style="display: inline-block; width: 25px;margin-top:10px;"></span> 
   			<a class="mini-button mini-button-green" onclick="upgradeAllVersion()" id="sumbitButton" style="margin-top:10px;">正式发布</a> 
		</div>
	</div>
</body>
</html>