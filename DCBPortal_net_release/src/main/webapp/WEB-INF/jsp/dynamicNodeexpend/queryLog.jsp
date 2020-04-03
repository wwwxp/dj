<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/dynamicNodeexpend/queryLog.js"
		type="text/javascript"></script>
	<title>任务日志查看</title> 
</head>
<body>
	<div class="mini-fit p5">
		<div class="search2" style="border: 0px;padding:0px;">
			<span>所属集群：<label id="clustern_name_label"></label>，节点名称：<label id="node_name_label"></label></span> 
           	<a class="mini-button mini-button-green" style="margin-left:470px;"  onclick="queryl()" plain="false">刷新</a>
       </div>
       <div class="mini-fit" style="margin-top: 2px;">
		<div id="datagrid" class="mini-datagrid"
			style="width: 100%; height: 100%;" idField="ID" allowResize="false"
			multiSelect="false">
			<div property="columns">
				<div type="indexcolumn" width="3">序号</div>
				<div field="HOST_IP_LIST" headerAlign="center" align="left"
					width="13" >IP列表</div>
				<!-- <div field="EXEC_RESULT" headerAlign="center" align="center"
					width="80" renderer="onStatusRenderer">执行状态</div> -->
					<div field="TRIGGER_RESULT" headerAlign="center" align="center"
					width="6" renderer="onStatusRenderer">执行结果</div>
					<div field="RULE_MSG" headerAlign="center" align="left"
					width="15" renderer="onRuleMsgRenderer">触发值</div>
				<div field="EXEC_MESSAGE" headerAlign="center" align="left"
					width="25" >任务结果描述</div>
	<!-- 			<div field="EXEC_LOG" headerAlign="center" align="left"
					width="160" >执行日志</div> -->
				<div field="CRT_DATE" headerAlign="center" align="center"
					dateFormat="yyyy-MM-dd HH:mm:ss" width="10">执行时间</div>
				 </div>
			</div>
		</div>
	</div>
</body>
</html>
