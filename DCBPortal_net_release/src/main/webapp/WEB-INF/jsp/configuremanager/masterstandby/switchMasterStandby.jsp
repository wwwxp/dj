<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>主备切换</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/masterstandby/switchMasterStandby.js"></script>
</head>
<body>
	<!-- <div class="mini-fit p5"> -->
		<!-- TAB页 -->
		<!-- <div id="switchTabs" class="mini-tabs" activeIndex="0"
			style="width: 100%; height: 40px; padding: 0px;" plain="false"
			tabAlign="left" tabPosition="top" onactivechanged="loadPage"></div> -->
		
		<div id="queryForm" class="search">
			<table class="formTable8" style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
				cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100" />
					<col width="200" />
					<col />
				</colgroup>
				<tr>
					<th><span>TOP名称：</span></th>
					<td><input id="searchValue" name="searchValue"
						class="mini-textbox" style="width: 150px;" /></td>
					<td><a class="mini-button" onclick="likeSearch()"
						style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
			<div id="runningProgramGrid" class="mini-datagrid"
				style="width: 100%; height: 100%" idField="ID" allowResize="false"
				allowCellselect="false" showFooter="true">
				<div property="columns">
					<div type="indexcolumn" width="5" headerAlign="center" align="center">序号</div>
					<div field="TASK_CODE" width="20" headerAlign="center" align="center">版本号</div>
					<div field="PROGRAM_CODE" width="20" headerAlign="center" align="center">TOP名称</div>
					<div field="PROGRAM_GROUP" width="20" headerAlign="center" align="center">TOP组</div>
					<div field="RUN_STATE" width="20" headerAlign="center" align="center" renderer="stateRenderer">运行状态</div>
					<div field="RUN_STATE" width="20" headerAlign="center" align="center" renderer="messageRenderer">业务状态</div>
					<div field="opt" width="20" headerAlign="center" align="center" renderer="optionRenderer">操作</div>
				</div>
			</div>
		</div>
<!-- 	</div> -->
</body>
</html>