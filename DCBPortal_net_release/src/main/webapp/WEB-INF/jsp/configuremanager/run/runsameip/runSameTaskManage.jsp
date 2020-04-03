<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>不区分IP运行程序</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript"
	src="${ctx}/js/configuremanager/run/runsameip/runSameTaskManage.js"></script>
</head>
<body>
	<div class="mini-fit p5" >
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9"
				style="width: 100%; padding: 0px; height: 40px; table-layout: fixed; border: 0px;"
				cellpadding="0" cellspacing="0">
				<tr>
					<td style="text-align: left;">
						<span style="line-height: 30px; font-weight: bold; font-family: '微软雅黑'; font-size: 14px; float: left; margin-left: 5px;">选择版本：</span>
						<span style="line-height: 30px;font-family: '微软雅黑'; font-size: 12px; color:red;font-weight:bold;" id="clusterNameSpan"></span>
					</td>
					<td style="text-align: right;">
						<a class="mini-button mini-button-green" onclick="javascript:goBack()" plain="false">返回</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit">
			<div id="taskGrid" class="mini-datagrid"
				style="width: 100%; height: 100%" idField="TASK_ID"
				allowResize="false" allowCellselect="false" multiSelect="true"
				showFooter="true">
				<div property="columns">
					<div type="indexcolumn" width="30"></div>
					<div field="TASK_CODE" headerAlign="center" align="center" width="12%">版本名称</div>
					<div field="VERSION" headerAlign="center" align="center" width="8%">版本</div>
					<div field="TASK_NAME" headerAlign="center" align="center" width="12%">文件名</div>
					<div field="FILE_NAME" headerAlign="center" align="center" width="20%">所属tar包</div>
					<div field="RUN_PROGRAM" width="28%" headerAlign="center"
						align="center" renderer="runStateRenderer">运行程序</div>
					<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="10%"
						headerAlign="center" align="center">创建时间</div>
					<div name="action" width="80" headerAlign="center" align="center"
						renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
