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
	<div class="mini-fit p5" style="overflow:hidden;">
		<div class="mini-fit" >
			<div class="mini-splitter" style="width: 100%;height: 100%;" borderStyle="border:0px;">
				<div style="width: 40%;float: left;">
			        <div id="currForm" class="search">
						<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
							<colgroup>
								<col width="160px"/>
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
			        <div class="mini-fit" style="margin-top: 5px;border:0px;">
						<div id="runningGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="100"
				             idField="HOST_IP" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
							<div property="columns">
								<div type="checkcolumn" width="20" headerAlign="center" align="center"></div>
								<div field="HOST_IP" width="120" headerAlign="center" align="center">主机IP</div>
								<div field="STATUS" width="80" headerAlign="center" align="center" renderer="statusRenderer">状态</div>
							</div>
						</div>
					</div>
			    </div>
			  <%--   <div id="arrowDiv" style="width:60px;float: left;">
					<div style="height: 40%;width: 100%;">
						<img src="${ctx}/images/arrow.png" style="margin-top:5px;">
					</div>
					<div style="height: 80%;width: 100%;">
						<img src="${ctx}/images/arrow.png" style="margin-top: 50px;">
					</div>
					<div style="clear: both;"></div>
				</div> --%>
			    <div style="float: right;width: 40%;">
			    	<div id="upgradForm" class="search">
						<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
							<colgroup>
								<col width="160px"/>
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
						<div id="upgradGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="100"
				             idField="HOST_IP" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
							<div property="columns">
								<div type="checkcolumn" width="20" headerAlign="center" align="center"></div>
								<div field="HOST_IP" width="120" headerAlign="center" align="center">主机IP</div>
								<div field="STATUS" width="80" headerAlign="center" align="center" renderer="statusRenderer">状态</div>
							</div>
						</div>
					</div>
				</div>
				<div style="clear: both;"></div>
			</div>
		</div>
		<div class="mini-toolbar" style="height:45px;text-align: center;margin-top:5px;">
   			<a class="mini-button"onclick="upgradeVersion()" id="sumbitButton" style="margin-top:10px;">灰度升级</a> 
   			<a class="mini-button"onclick="upgradeAllVersion()" id="sumbitButton" style="margin-top:10px;">正式发布</a> 
		</div>
    </div>
</body>
</html>