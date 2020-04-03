<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>top管理</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script type="text/javascript" src="${ctx}/js/configuremanager/topic/topicConfig.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="cluster_tabs" class="mini-tabs" activeIndex="0" plain="false"
			tabAlign="left" tabPosition="top" onactivechanged="loadPage" style="height:40px;width:100%;">
		</div>
		<div id="queryForm" class="search">
			<table class="formTable8"
				style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
				cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100" />
					<col width="200" />
					<col width="100" />
					<col />
				</colgroup>
				<tr>
					<th><span>Topology名称：</span></th>
					<td>
						<input class="mini-combobox" id="PROGRAM_CODE" name="PROGRAM_CODE"
							showNullItem="true"  nullItemText=""  emptyText=""
							style="width: 95%;" valueField="PROGRAM_CODE" textField="PROGRAM_NAME" >
					</td>
					<th><span>Topic名称：</span></th>
					<td><input id="topicName" name=topicName class="mini-textbox" style="width: 80%;" /></td>
					<td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;"
				cellpadding="0" cellspacing="0">
				<tr>
					<td style="text-align: right;">
						<a class="mini-button mini-button-green" onclick="addConfig()" plain="false">新增</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit">
			<div id="topicGrid" class="mini-datagrid"
				style="width: 100%; height: 100%;" idField="TOPIC_NAME"
				allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true">
				<div property="columns">
					<div type="checkcolumn" width="30" headerAlign="center" align="center">选择</div>
					<div field="TOPIC_NAME" headerAlign="center" align="center" width="100">Topic名称</div>
					<div field="RQ_CLUSTER_NAME" headerAlign="center" align="center" width="100">RocketMQ 集群</div>
					<div field="RQ_VERSION" headerAlign="center" align="center" width="60">RocketMQ 版本</div>
					<div field="RQ_IP" headerAlign="center" align="center" width="120" renderer="onIpRenderer">RocketMQ IP</div>
					<div field="MQ_R" headerAlign="center" align="center" width="60">可读队列数</div>
					<div field="MQ_W" headerAlign="center" align="center" width="60">可写队列数</div>
					<div field="PROGRAM_NAME" headerAlign="center" align="center" width="60">Topology名称</div>
					<!-- <div field="PROGRAM_ATTR" headerAlign="center" align="center" width="60" renderer="onAttrRender">Topology属性</div> -->
					<div field="TOPIC_DESC" headerAlign="center" align="center" width="140">描述</div>
					<div name="action" width="80" headerAlign="center" align="center"
						renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
