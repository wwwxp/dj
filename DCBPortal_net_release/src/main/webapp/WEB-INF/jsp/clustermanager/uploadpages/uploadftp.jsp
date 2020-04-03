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
	src="${ctx}/js/clustermanager/uploadftp.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;"
				cellpadding="0" cellspacing="0">
				<tr>
					<td style="text-align: left; color: red; font-weight: bold;"><span>上传主机：<span
							id="ftpHostName"></span>，协议为：<span id="ftpTypeName"></span></span></td>
					<td style="text-align: right;"><a
						class="mini-button mini-button-green" onclick="upload()"
						width="140px" plain="false">上传组件版本包</a> <!--<a class="mini-button mini-button-green" onclick="back()" plain="false">版本回退</a> 
						 <a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a>  -->
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="uploadGrid" class="mini-datagrid"
				style="width: 100%; height: 100%" idField="ID" allowResize="false"
				allowCellselect="false" multiSelect="true" showFooter="true">
				<div property="columns">
					<div type="indexcolumn" width="30" headerAlign="center"
						align="center">序号</div>
					<div field="FILE_NAME" width="160" headerAlign="center"
						align="center" renderer="fileRenderer">文件名称</div>
					<div field="VERSION" width="80" headerAlign="center" align="center">版本号</div>
					<div field="FILE_PATH" width="200" headerAlign="center"
						align="center">文件路径</div>
					<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="100"
						headerAlign="center" align="center">上传时间</div>
					<div field="DESCRIPTION" width="200" headerAlign="center"
						align="center">描述</div>
					<div name="action" width="50" headerAlign="center" align="center"
						renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>