<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript"
		src="${ctx}/js/configuremanager/editconfig/addAndCopyFolder.js"></script>
	<title>新建实例</title>
</head>
<body>
	<div class="mini-fit p5">
		<span class="fred">1、选择IP地址，新建实例分发到所选主机<br/>2、IP地址为空则分发到此集群部署该组件的所有集群<br/></span>
		<span class="fred">3、实例支持批量创建，通过逗号区分，如：端口1,端口2</span>
		<table id="fileForm" class="formTable6" style="table-layout: fixed;margin-top: 3px;">
			<tr>
				<th id="file" style="width:80px;">
					IP：
				</th>
				<td style="width:160px;">
					<input width="100%" id="file_host_ip" name="file_host_ip" allowInput="false"
						class="mini-combobox" required="false" multiSelect="false" showNullItem="true"
						textField="HOST_IP" valueField="HOST_IP" onvaluechanged="changeDefualtFileName"/>
				</td>
				<th style="width: 60px;">
					<span class="fred">*</span>端口：
				</th>
				<td>
					<input width="100%" id="fileName" name="fileName"
						class="mini-textbox" required="true" />
					<input width="100%" id="redisFileName" name="redisFileName" class="mini-combobox" allowInput="false"
						multiSelect="true" showNullItem="false" textField="PORT" valueField="PORT" popupHeight="140px"
						required="true" style="display: none;" />
				</td>
			</tr>
			<tr>
				<th id="copyFile" style="width:80px;">
					<span class="fred">*</span>复制文件：
				</th>
				<td colspan="3">
					<input width="100%" id="copyFilesNames" name="copyFilesNames"
						class="mini-combobox" required="true" multiSelect="true" popupHeight="140px"
						textField="fileName" valueField="fileName"/>
				</td>
			</tr>
		</table>
	</div>
	<div class="mini-toolbar"
		style="height: 28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
		borderStyle="border:0;border-top:solid 1px #b1c3e0;">
		<a class="mini-button" onclick="addBatchFile()"
			style="width: 60px; margin-right: 20px;">确定</a>
		<span style="display: inline-block; width: 25px;"></span>
		<a class="mini-button" onclick="closeWindow(systemVar.FAIL)" style="width: 60px;">取消</a>
	</div>
</body>
</html>