<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台--服务组查询任务积压</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<%@ include file="/public/common/common.jsp"%>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/servicequery/taskOverstockAggregateQuery.js"></script>

</head>
<body>
<div class="mini-fit" style="padding: 5px;overflow-y: auto;overflow-x:hidden; height: auto;">
	<div class="search2" style="border: 0px;">
		<div style="width: 35%;padding-left:65%;">
			<table id="chartsForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100px"/>
					<col />
					<col width="120">
				</colgroup>
				<tr>
					<td style="text-align: center;font-weight: bold;">
						<span class="fred">集群名称:</span>
					</td>
					<td style="text-align: right;">
						<input id="CLUSTER_CHARTS_ID" name="CLUSTER_CHARTS_ID" class="mini-combobox" style="width: 100%;" required="true"
							   textField="CLUSTER_INFO" valueField="JS_CLUSTER_ID" showNullItem="false" allowInput="false"
							   onvaluechanged="changeChartsClusterList" />
					</td>
					<td style="text-align: right;">
						<a class="mini-button" onclick="refreshChartData()" style="text-align: right;margin-right: 20px;">手动刷新</a>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div style="height: 300px;">
		<div id="serviceDiv" style="width:49%;height: 100%;border: 1px solid #E6E6E6;float: left;">
			<div id="serviceCharts" style="height:100%;width:100%;" ></div>
		</div>
		<div id="boltDiv" style="width: 49%;height: 100%;border: 1px solid #E6E6E6;float: right;">
			<div id="boltCharts" style="height:100%;width:100%;" ></div>
		</div>
		<div style="clear: both;"></div>
	</div>

	<div style="height: 100%;">
		<div id="queryFrom" class="search" style="margin-top: 5px;">
			<table class="formTable8" style="width:80%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100px"/>
					<col />
					<col width="100px"/>
					<col />
					<col width="160px"/>
				</colgroup>
				<tr>
					<th>
						<span><span class="fred">*</span>集群名称：</span>
					</th>
					<td>
						<input id="CLUSTER_ID" name="CLUSTER_ID" class="mini-combobox" style="width: 90%;" required="true"
							   textField="CLUSTER_INFO" valueField="JS_CLUSTER_ID" showNullItem="false" allowInput="false"
							   onvaluechanged="changeClusterList" />
					</td>
					<th>
						<span><span class="fred">*</span>服务组名称：</span>
					</th>
					<td>
						<input id="SERVICE_NAME" name="SERVICE_NAME" class="mini-combobox" style="width: 90%;" required="true"
							   textField="SERVICE_NAME" valueField="SERVICE_NAME" showNullItem="false"
							   onvaluechanged="changeServiceGroupList" allowInput="false"/>
					</td>
					<td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
				<tr>
					<th>
						<span>服务组包含服务：</span>
					</th>
					<td colspan="4">
						<label id="groupServiceList"></label>
					</td>
				</tr>
			</table>
		</div>


		<div class="mini-fit" style="margin-top: 5px;padding-bottom: 20px;">

			<!--条件过滤-->
			<div class="search2" style="border: 0px;">
				<div style="width: 70%;padding-left:30%;">
					<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
						<colgroup>
							<col width="100px"/>
							<col />
							<col />
							<col />
							<col width="100px"/>
							<col />
						</colgroup>
						<tr>
							<td style="text-align: center;font-weight: bold;">
								<span class="fred">过滤数据:</span>
							</td>
							<td style="text-align: right;">
								<input id="HOST_LIST" name="HOST_LIST" class="mini-combobox" style="width: 100%;"
									   textField="HOST_IP" valueField="HOST_IP" showNullItem="true" nullItemText="请选择过滤主机" tooltip="请选择过滤主机"
									   allowInput="false" onvaluechanged="changeHostList" />
							</td>
							<td style="text-align: right;">
								<input id="TASK_NAME" name="TASK_NAME" class="mini-combobox" style="width: 100%;"
									   textField="TASK_NAME" valueField="TASK_NAME" showNullItem="true" nullItemText="请选择任务名称" tooltip="请选择任务名称"
									   allowInput="false" onvaluechanged="changeHostList" />
							</td>
							<td style="text-align: right;">
								<input id="EXEC_QUENE_SIZE" name="EXEC_QUENE_SIZE" class="mini-combobox" style="width: 100%;"
									   textField="EXEC_QUENE_SIZE" valueField="EXEC_QUENE_SIZE" showNullItem="true" nullItemText="请选择执行队列大小" tooltip="请选择执行队列大小"
									   allowInput="false" onvaluechanged="changeHostList" />
							</td>
							<td style="text-align: center;font-weight: bold;">
								<span class="fred">排序规则:</span>
							</td>
							<td style="text-align: right;">
								<input id="SORT_RULE" name="SORT_RULE" class="mini-combobox" style="width: 100%;" required="true"
									   textField="text" valueField="code" showNullItem="false" value="EXEC_QUENE_SIZE_DESC" data="getSysDictData('TASK_SORT_LIST')"
									   allowInput="false" onvaluechanged="changeHostList"/>
							</td>
						</tr>
					</table>
				</div>
			</div>

			<!--主机明细汇总页面-->
			<div id="deploy_tabs" class="mini-tabs" style="margin-top:-6px;height:100%;"
				 plain="false" tabAlign="left" tabPosition="top">
				<div title="积压明细" id="hostdetailed" url="${ctx}/jsp/monitormanager/servicequery/taskOverstockHostDetailed"></div>
				<div title="积压汇总" id="hostsummary" url="${ctx}/jsp/monitormanager/servicequery/taskOverstockHostSummary"></div>
			</div>

		</div>


	</div>
</div>

<div style="display: none;">
	<form id="termialForm" name="termialForm" method="post" target="_blank">
		<input type="hidden" id="termialHost" name="termialHost"/>
		<input type="hidden" id="logName" name="logName" value="终端操作"/>
	</form>
</div>
</body>
</html>