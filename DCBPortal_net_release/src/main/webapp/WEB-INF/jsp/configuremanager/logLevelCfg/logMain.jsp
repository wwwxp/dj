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
	src="${ctx}/js/configuremanager/logLevelCfg/logMain.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;"
				cellpadding="0" cellspacing="0">
				<tr>
					<td style="text-align: right;"><a
						class="mini-button mini-button-green" onclick="submit()"
						plain="false">保存</a>  
						<a style="padding-left: 5px"
						class="mini-button mini-button-green" onclick="sendMsg()"
						plain="false">发送</a>  
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit">
			<div id="datagrid" class="mini-datagrid" idField="PRO_ID"  multiSelect="true"
			      allowCellEdit="true"  allowCellSelect="true" onload="treeLoadEvent"
				style="width: 100%; height: 100%" allowResize="false"  
				showEmptyText="false" showFooter="false">
				<div property="columns">
					<div type="checkcolumn" width="5" ></div>
					<div field="PRO_KEY" width="25" headerAlign="center"
						align="center">
						参数名 <input property="editor" id="PRO_KEY" name="PRO_KEY"
							class="mini-textbox" style="height: 100%; width: 100%;" />
					</div>
					<div field="PRO_VALUE" width="25" headerAlign="center"
						align="center">
						参数值 <input property="editor" id="PRO_VALUE" name="PRO_VALUE"
							class="mini-textbox" style="height: 100%; width: 100%;" />
					</div>
					<div field="PRO_DESC" width="35" headerAlign="center" align="center">
						描述 <input property="editor" id="PRO_DESC" name="PRO_DESC"
							class="mini-textbox" style="height: 100%; width: 100%;" />
					</div>
					<div name="action" width="15" headerAlign="center" align="center"
						renderer="onActionRenderer">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>