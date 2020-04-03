<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<link rel="stylesheet" type="text/css" href="${ctx}/css/nodemanager/chooseRemoteFile.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/nodemanager/uploadversion/chooseRemoteFile.js"></script>

</head>
<body>
	<div class="mini-fit p5" style="overflow: auto;">
		<div class="mini-splitter" handlerSize="4" style="width: 100%; height: 100%;">
			<div showCollapseButton="true" size="180" minWidth="80" minSize="180">
				<div class="search2" style="border: 0px;">
					<table id="updateForm" class="formTable9"
						   style="width:100%;padding:0px;table-layout: fixed;border: 0px;"
						   cellpadding="0" cellspacing="0">
						<tr>
							<td style="text-align: left; padding: 2px 2px 2px 2px">
								<input id="DIR_NAME" style="width: 65%;" class="mini-textbox" onenter="search"
									   emptyText="输入目录名搜索"/>
								<a class="mini-button" onclick="searchDirOnTree()" style="width:30%;" plain="false">查找</a>
							</td>
						</tr>
					</table>
				</div>
				<div class="mini-fit">
					<ul id="dir_tree" class="mini-tree"
						style="width: 100%; height: 99%" showTreeIcon="true" textField="dirName"
						showTreeIcon="true" idField="dirPath" resultAsTree="false"
						expandOnLoad="0"
						onnodeclick="onClickTreeNode">
					</ul>
				</div>
			</div>

			<div class="p5" style="width:100%;height: 100%;text-align: center;">
				<div id="location"><div onclick="dirBack();"></div><div id="uri"></div></div>
				<ul id="fileList">

				</ul>
			</div>
		</div>
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
