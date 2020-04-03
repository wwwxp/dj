<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>停止主机DMDB</title>
	<%@ include file="/public/common/common.jsp" %>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/context.standalone.css" />
	<!-- 右键使用jq -->
	<script type="text/javascript" src="${ctx}/js/common/jquery.min.js"></script>
	<!-- 新使用的jquery菜单时，使用j$开头-->
	<script type="text/javascript">
        var j$ = jQuery.noConflict(true);
	</script>
	<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
	<script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
	<script type="text/javascript" src="${ctx}/js/configuremanager/addCommon.js"></script>
	<script type="text/javascript" src="${ctx}/js/configuremanager/addDmdbNewForStop.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: auto;">
	<div style="border: 1px solid #b1c3e0">
		<div style="border-bottom: 1px dashed #b1c3e0;height: 22px;">
			<a class="Delete_Button" href="javascript:selectAll();" style="margin-left:14px;">全选</a>
			<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			<a class="Delete_Button" href="javascript:showStatus()">状态查看</a>

			<label class="tips_label_start">：已运行</label>
			<div class="tips_div_start" style="background-color: #5cb85c;" title="当前主机有运行实例" ></div>
			<label class="tips_label_start">：未运行</label>
			<div class="tips_div_start" style="background-color: gray;" title="当前主机无运行实例" ></div>
		</div>
		<div id="hostFitDiv" style="height: auto;min-height: 55px;overflow: hidden;">

		</div>
	</div>

	<div id="queryForm" style="margin-top:5px;" class="search">
		<table class="formTable8" style="width:100%;height:35px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
			<colgroup>
				<col width="80px"/>
				<col width="240px"/>
				<col />
			</colgroup>
			<tr>
				<th>
					<span>部署类型：</span>
				</th>
				<td>
					<input id="DEPLOY_FILE_TYPE" name="DEPLOY_FILE_TYPE" class="mini-combobox" style="width: 90%;" onvaluechanged="search()"
						   textField="text" valueField="code" data="getSysDictData('dmdb_deploy_type')" showNullItem="true" allowInput="false"/>
				</td>
				<td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
			</tr>
		</table>
	</div>

	<div style="padding:5px 0px;overflow-y:auto;overflow-x:hidden;height: auto;">
		<div id="inputForm">
			<div id="deploy_datagrid" class="mini-datagrid" allowCellEdit="true"
				 style="width: 100%;" multiSelect="true" onload="loadStopData"
				 idField="INST_ID" showFooter="false"  allowtResize="true" onselectionchanged="selectChange">
				<div property="columns">
					<div type="checkcolumn" width="20"></div>
					<div field="HOST_IP" headerAlign="center" align="center" width="90">主机</div>
					<div field="DEPLOY_FILE_TYPE" headerAlign="center" align="center" width="80">部署类型</div>
					<div field="INST_NAME" headerAlign="center" align="center" width="90">实例名称</div>
					<div field="INST_PATH" headerAlign="center" align="center" width="140">实例路径</div>
					<div field="VERSION" headerAlign="center" align="center" width="60">版本</div>
					<div field="STATUS" headerAlign="center" align="center" width="80" renderer="onStatusRender">状态</div>
					<div type="field" headerAlign="center" align="center" width="80" renderer="onActionRender">操作</div>
				</div>
			</div>
		</div>
	</div>
	<div style="height:30px;line-height:30px;">
		<label>停止进度说明:</label>
	</div>
	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
	<div style="height: 5px;"></div>
</div>
<div class="mini-toolbar" style="height:42px;text-align: center;line-height:42px;"
	 borderStyle="border:0;border-top:solid 1px #b1c3e0;">
	<a class="mini-button"onclick="onSubmit()" id="sumbitButton">停止</a>
	<span style="display: inline-block; width: 25px;"></span>
	<a class="mini-button" onclick="close()">关闭</a>
</div>

<!-- 终端操作隐藏表单 -->
<div style="display: none;">
	<form id="termialForm" name="termialForm" method="post" target="_blank">
		<input type="hidden" id="termialHost" name="termialHost"/>
		<input type="hidden" id="logName" name="logName" value="终端操作"/>
	</form>
</div>
</body>
</html>