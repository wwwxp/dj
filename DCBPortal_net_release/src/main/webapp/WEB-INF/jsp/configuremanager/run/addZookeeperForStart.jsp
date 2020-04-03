<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>启动主机ZOOKEEPER</title>
<%@ include file="/public/common/common.jsp" %>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/context.standalone.css" />
<!-- 右键使用jq -->
<script type="text/javascript" src="${ctx}/js/common/jquery.min.js"></script>
<!-- 新使用的jquery菜单时，使用j$开头-->
<script type="text/javascript">
    var j$ = jQuery.noConflict(true);
</script>
<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
<script type="text/javascript" src="${ctx}/js/configuremanager/addCommon.js"></script>
<script type="text/javascript" src="${ctx}/js/configuremanager/addZookeeperForStart.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow: auto;">
	<div style="border: 1px solid #b1c3e0">
        <div style="border-bottom: 1px dashed #b1c3e0;">
			<a class="Delete_Button" href="javascript:selectAll();" style="margin-left:14px;">全选</a>
			<a class="Delete_Button" href="javascript:selectNone()">取消选择</a>
			<!-- <a class="Delete_Button" href="javascript:reloadConfig()">重载配置</a> -->
			<a class="Delete_Button" href="javascript:showStatus()">状态查看</a>

			默认启动版本：<input id="defaultDeployVersion" name="defaultDeployVersion" CLUSTER_TYPE='dca' class="mini-combobox"
						  onvaluechanged="changeDefaultDeployVersion('dca')"
						  style="width: 160px;margin-top:2px;margin-bottom: 2px;"
						  textField="VERSION" valueField="VERSION" showNullItem="true" allowInput="false"/>

			<label class="tips_label_start">：已运行</label>
			<div class="tips_div_start" style="background-color: #5cb85c;" title="当前主机有运行实例" ></div>
			<label class="tips_label_start">：未运行</label>
			<div class="tips_div_start" style="background-color: gray;" title="当前主机无运行实例" ></div>
		</div>
		<div id="hostFitDiv" style="height: auto;min-height: 55px;overflow: hidden;">
		
		</div>
	</div>
	<div class="mini-panel" id="instPanel" style="width:100%;margin-top: 5px;height: auto;" title="已启动组件实例" 
    		showCollapseButton="true" expanded="false" collapseOnTitleClick="true" onbuttonclick="clickPanelBtn">
		<div class="mini-fit">
			<div id="configGrid" class="mini-datagrid" style="width: 100%;height:auto;" pageSize="100" 
				 onselectionchanged="selectGridRow()" onheadercellclick="selectGridRow()" allowRowSelect="true" allowUnselect="true"
	             idField="INST_ID" allowResize="false" allowCellSelect="false" multiSelect="true" showFooter="false" >
				<div property="columns">
					<div type="checkcolumn" width="20" headerAlign="center" align="center"></div>
					<div field="HOST_IP" width="80" headerAlign="center" align="center">主机IP</div>
					<div field="DEPLOY_FILE_TYPE" width="80" headerAlign="center" align="center">启动模式</div>
	                <!-- <div field="INST_NAME" width="110" headerAlign="center" align="center">实例名</div> -->
	                <div field="FILE_NAME" width="110" headerAlign="center" align="center">配置文件</div>
					<div field="VERSION" width="60" headerAlign="center" align="center">启动版本</div>
					<div field="STATUS" width="60" headerAlign="center" align="center" renderer="statusRenderer">实例状态</div>
					<div field="MODIFY_TIME" width="100" headerAlign="center" dataType="string" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">操作时间</div>
					<div name="action" width="120" headerAlign="center" align="center" renderer="onActionRenderer">操作</div>
				</div>
			</div>
		</div>
	</div>
	<div style="padding:5px 0px;overflow-y:auto;overflow-x:hidden;height: auto;">
		<div id="inputForm">
			<table id="paramsTable" class="formTable6" style="width:100%;min-height:5px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80"/>
					<col width="50%"/>
					<col width="80"/>
					<col width="50%"/>
					<col width="60"/>
					<col width="0"/>
				</colgroup>
				<tbody id="paramsInfo">
				</tbody>
			</table>
		</div>
	</div>
	<div style="height:30px;line-height:30px;">
		<label>启动进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
   <!-- <a class="mini-button" onclick="addOperator()">保存配置</a>
   <span style="display: inline-block; width: 25px;"></span>  -->
   <a class="mini-button"onclick="onSubmit()" id="sumbitButton">启动</a> 
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