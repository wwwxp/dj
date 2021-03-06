<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script type="text/javascript"
	src="${ctx}/js/common/dynamicMergeCells.js"></script>
<script language="javascript" type="text/javascript"
	src="${ctx}/js/nodemanager/uploadversion/serviceUploadftp.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8"
				style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
				cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100" />
					<col width="250" />
					<col />
				</colgroup>
				<tr>
					<th><span>程序类型：</span></th>
					<td><input class="mini-combobox" id="CODE" allowInput="true"
						name="CODE" style="width: 100%;" valueField="CODE"
						textField="TYPE_INFO"></td>

					<td><a class="mini-button" onclick="search()"
						style="margin-left: 20px;">查询</a></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;"
				cellpadding="0" cellspacing="0">
				<tr>
					<td style="text-align: left; color: red; font-weight: bold;"><span>上传主机：<span
							id="ftpHostName"></span>，协议为：<span id="ftpTypeName"></span></span></td>
                    <td style="text-align: right;float: right;margin-left: 10px"><a
                            class="mini-button mini-button-green" onclick="uploadPatch()"
                            width="140px" plain="false">版本补丁上传</a>
                    <td style="text-align: right;float: right"><a
						class="mini-button mini-button-green" onclick="upload()"
						width="140px" plain="false">上传业务版本包</a>
<!--                     	<a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a>  -->
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="uploadGrid" class="mini-datagrid"
				onload="loadUploadGridData" style="width: 100%; height: 100%"
				idField="ID" allowResize="false" allowCellselect="false"
				multiSelect="true" showFooter="true">
				<div property="columns">
					<div type="indexcolumn" width="20"></div>
					<div field=NAME width="60" headerAlign="center"
						align="center">类型名</div>
					<div field=CODE width="60" headerAlign="center"
						align="center">类型编码</div>
					<div field="FILE_NAME" width="80" headerAlign="center"
						align="left" renderer="fileRenderer">文件名称</div>
					<div field="VERSION" width="30" headerAlign="center" align="center">版本号</div>
                    <div field="IS_FULL_VERSION" width="30" renderer="isFullVersionRenderer" headerAlign="center" align="center">增量/全量</div>
					<div field="FILE_PATH" width="100" headerAlign="center"
						align="left">上传路径</div>
					<%--<div field="CFG_PATH" width="50" headerAlign="center"
						 align="left">配置文件路径</div>--%>
					<div field="UPLOAD_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="60"
						 headerAlign="center" align="center">上传时间</div>
					<div field="FILE_DESC" width="80" headerAlign="center"
						align="center" >文件描述</div>
					<div name="action" width="40" headerAlign="center" align="center"
						renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>