<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript"
		src="${ctx}/js/configuremanager/editconfig/addFile.js"></script>
	<title>新建文件</title>
</head>
<body>
	<div class="mini-fit p5">
		<table id="fileForm" class="formTable6" style="table-layout: fixed;">
			<tr>
				<th id="file" style="width:120px;">
					<span class="fred">*</span>文件名：(带后缀)
				</th>
				<td>
					<input width="100%" id="fileName" name="fileName" class="mini-textbox" required="true" />
				</td>
			</tr>
			<tr id="copyTr">
				<th>可复制文件</th>
				<td>
					<input width="100%" id="copyFilesNames" name="copyFilesNames" popupHeight="120px"
						class="mini-combobox" required="false" multiSelect="false"
						textField="fileName" valueField="fileName"/>
				</td>
			</tr>
		</table>
	</div>
	<div class="mini-toolbar"
		style="height: 28px; text-align: center;"
		borderStyle="border:0;border-top:solid 1px #b1c3e0;">
		<a class="mini-button" onclick="addFile()"
			style="width: 60px; margin-right: 20px;">确定</a>
		<span style="display: inline-block; width: 25px;"></span>
		<a class="mini-button" onclick="closeWindow(systemVar.FAIL)" style="width: 60px;">取消</a>
	</div>
</body>
</html>