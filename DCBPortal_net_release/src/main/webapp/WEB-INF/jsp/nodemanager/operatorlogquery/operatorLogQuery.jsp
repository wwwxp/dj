<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/nodemanager/operatorlogquery/operatorLogQuery.js" type="text/javascript"></script>
	<title>系统日志管理</title>
</head>
<body>
    <div class="mini-fit" style="padding: 5px;">
	<div class="search" id="queryFrom" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
		<table style="width: 100%;height: 100%;">
			<colgroup>
				<col width="100" />
				<col width="180" />

				<col width="100"/>
				<col width="180" />

				<col width="100"/>
				<col width="180" />

				<col width="100"/>
				<col width="180" />

			</colgroup>
			<tr>
				<th><span>操作模块：</span></th>
				<td><input class="mini-combobox" id="operatorModule" name="OPERATOR_MODULE"
						   textField="OPERATOR_MODULE" valueField="OPERATOR_MODULE"
						   showNullItem="true" nullItemText="=请选择="
						   style="width: 95%;" allowInput="true"></td>

				<th><span>日志内容：</span></th>
				<td><input class="mini-Textbox" id="LOG_CONTENT"
						   name="LOG_CONTENT" style="width: 95%;"></td>

				<th><span>开始时间：</span></th>
				<td><input class="mini-datepicker" id="START_TIME" name="CODE"
						   format="yyyy-MM-dd HH:mm:ss" timeFormat="HH:mm:ss"
						   showTime="true" showTodayButton="true" showOkButton="true"
						   style="width: 95%;"></td>

				<th><span>结束时间：</span></th>
				<td><input class="mini-datepicker" id="END_TIME" name="CODE"
						   format="yyyy-MM-dd HH:mm:ss" timeFormat="HH:mm:ss"
						   showTime="true" showTodayButton="true" showOkButton="true"
						   style="width: 95%;"></td>

				<td><a class="mini-button" onclick="search()"
					   style="margin-left: 20px;">查询</a></td>

			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div id="operatorLogGrid" class="mini-datagrid"  style="width: 100%; height: 100%;"
             idField="ID" allowResize="false"  multiSelect="true">
			<div property="columns">
				<div field="ID" visible="false"></div>
				<div field="OPERATOR_MODULE" width="50" headerAlign="center"
					 align="center">操作模块</div>
				<div field="OPERATOR_NAME" width="50" headerAlign="center"
					 align="center" >操作方式</div>
				<div field="LOG_CONTENT" width="120" headerAlign="center"
					 align="center">日志内容</div>

				<div field="CREATED_DATE" width="50" headerAlign="center"
					 dateFormat="yyyy-MM-dd HH:mm:ss"
					 align="center">操作时间</div>
				<div field="CREATED_USER" width="50" headerAlign="center"
					 align="center">操作人</div>

				<div name="operation" headerAlign="center"
					 align="center" renderer="onRenderer" width="50">操作</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
