<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript"
	src="${ctx}/js/monitor/resultCode/addEditResultCodeManage.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<table id="resultCodeForm" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="100" />
				<col />
				<col width="100" />
				<col />
			</colgroup>
			<tr>
				<th><span class="fred">*</span>OCS错误代码：</th>
				<td><input id="OCS_RESULT_CODE" name="OCS_RESULT_CODE"
					required="true"  class="mini-textbox"
					style="width: 100%;" /></td>
				<th><span class="fred">*</span>OCP错误代码：</th>
				<td><input id="OCP_RESULT_CODE" name="OCP_RESULT_CODE"
					required="true"  class="mini-textbox"
					style="width: 100%;" /></td>
			</tr>
			
			<tr>
				 <th><span class="fred">*</span>生效时间：</th>
				 <td><input id="EFF_DATE" name="EFF_DATE" allowInput="false"
					class="mini-datepicker" format="yyyy-MM-dd H:mm:ss" showTime="true"
					timeFormat="H:mm:ss" style="width: 100%;" required="true" />
				</td> 
				<th><span class="fred">*</span>失效时间：</th>
				<td><input id="EXP_DATE" name="EXP_DATE" allowInput="false"
					class="mini-datepicker" format="yyyy-MM-dd H:mm:ss"
					timeFormat="H:mm:ss" showTime="true" style="width: 100%;" required="true" />
				</td>
			</tr>
			<tr>
				<th>描述：</th>
				<td colspan="3"><input id="REMARKS" name="REMARKS"  class="mini-textarea" style="width: 100%;" />
				</td>
			</tr>
		</table>
	</div>
	<div class="mini-toolbar"
		style="height: 28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
		borderStyle="border:0;border-top:solid 1px #b1c3e0;">
		<a class="mini-button" onclick="onSubmit"
			style="width: 60px; margin-right: 20px;">确定</a> <span
			style="display: inline-block; width: 25px;"></span> <a
			class="mini-button" onclick="closeWindow()" style="width: 60px;">取消</a>
	</div>
</body>
</html>
