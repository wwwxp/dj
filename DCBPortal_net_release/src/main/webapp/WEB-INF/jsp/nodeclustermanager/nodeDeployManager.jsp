<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>节点部署管理</title>
	<%@ include file="/public/common/common.jsp" %>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/graph/common.css" />
	<script type="text/javascript" src="${ctx}/js/nodeclustermanager/nodeDeployManager.js"></script>
	<style type="text/css">
		/*table tr td {
			padding-bottom:10px;
		}*/

		.formTable8 th{
			font-weight:bold;

		}

		#deployResult{
			background-color:rgba(200,200,200,0.2);
			box-sizing: border-box;
			padding:5px;
			border:1px #ccc solid;
			outline:none;
			overflow-y: auto;
		}

		/*textBox的框的修饰*/
		.mini-buttonedit *{
			border-radius:5px;
			border-color:#ccc;
		}

		/*单选框按钮的修饰*/
		.mini-radiobuttonlist input{
			margin-right:8px;
		}

		/*单选框文本的修饰*/
		.mini-radiobuttonlist label{
			width: 82px;
			color:green;
			font-weight: bold;
		}

		.webTemplatesRow .mini-radiobuttonlist label{
			width:auto;
		}

		/*表格整体的修饰*/
		.mini-grid *{
			border-color:#ccc;
		}

        /*表格首行的修饰*/
		.mini-grid-headerCell{
			font-weight:bold;
		}

		.mini-radiobuttonlist{
			overflow-x: auto;
		}

		#selectedNum{
			position:absolute;
			width:50px;
			height: 30px;
			line-height:30px;
			top:0px;
			right:0px;
		}
	</style>
</head>
<body>
<div class="mini-fit" style="padding: 5px 20px;height:95%;box-sizing: border-box">
	<!--class="search"-->
	<div id="deployForm">
		<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
			<colgroup>
				<col width="180"/>
				<col />
				<col width="180"/>
				<col />
				<col width="180"/>
				<col />
			</colgroup>
			<tr>
				<th style="padding-top: 10px;"><span class="fred">*</span>请选择程序: </th>
				<td style="padding-top: 10px;" colspan="5">
					<input width="100%" name="NODE_TYPE" id="NODE_TYPE" class="mini-combobox" allowInput="true"
						   valueField="NODE_TYPE_ID" textField="NODE_TYPE_NAME" required="true" onvaluechanged="changeNodeType()"
					/>
				</td>
			</tr>
			<tr	style="height:45px;">
				<th><span class="fred">*</span>请选择版本号: </th>
				<td colspan="5">
					<div id="NODE_VERSION" name="NODE_VERSION" class="mini-radiobuttonlist"
						 repeatLayout="table" repeatDirection="horizontal"
						 style="width: 100%;height:100%;box-sizing:border-box;line-height:100%;padding-top:12px;"
						 onValuechanged="changeRadio"
						 textField="VERSION" valueField="VERSION_ID"/>
				</td>

			</tr>

			<tr style="height:45px;">

				<th><span class="fred">*</span>采用历史配置: </th>
				<td colspan="1">
					<select id="USE_HISTORY_CFG" name="USE_HISTORY_CFG" class="mini-radiobuttonlist" value="true">
						<option value="true">是</option>
						<option value="false">否</option>
					</select>
				</td>

				<th style="display: none;" class="webTemplatesRow"><span class="fred">*</span>WEB容器模板: </th>
				<td  colspan="3" style="display: none;" class="webTemplatesRow">
					<div id="WEB_TEMPLATES" name="WEB_TEMPLATES" class="mini-radiobuttonlist"
						 repeatLayout="table" repeatDirection="horizontal"
						 style="width: 100%;height:100%;box-sizing:border-box;line-height:100%;padding-top:12px;"
						 textField="WEB_TEMPLATES" valueField="WEB_TEMPLATES"
					/>
				</td>
			</tr>

			<tr style="height:180px;line-height: 180px;">
				<th style="position: relative;"><div></div><span class="fred">*</span>请选择节点: <div id="selectedNum"></div></th>
				<td colspan="5">
					<div class="mini-fit">
						<div id="nodeGrid" class="mini-datagrid" style="width: 100%; height: 100%" showFooter="false"
							 idField="NODE_ID" allowResize="false" allowCellselect="false" multiSelect="true"
							 showModified="false"
							 onrowclick="toPosition"
							>
							<div property="columns">
								<div type="checkcolumn" width="20" headerAlign="center"></div>
								<div field="NODE_NAME" width="80" headerAlign="center" align="center">节点名称</div>
								<div field="HOST_TEXT" width="120" headerAlign="center" align="center">节点主机</div>
								<div field="NODE_PATH" width="160" headerAlign="center" align="left">部署目录</div>
								<div field="DEPLOYED" width="100" headerAlign="center" align="center" renderer="onDeployedRenderer">部署情况</div>
								<div field="DEPLOY_RESULT" width="100" headerAlign="center" align="center" renderer="onRenderer">部署结果</div>
							</div>
						</div>
					</div>
				</td>
			</tr>
			<tr>
				<th><span class="fred">*</span>部署详情:</th>
				<td colspan="5">
					<%--<div id="deployResult" style="margin-top:10px;padding:5px;width:100%;height: 80px;border:1px #ccc solid;background-color:rgba(200,200,200,0.5)">--%>
					<%--<span style="color:#ccc;">等待部署...</span>--%>
					<%--</div>--%>

					<div id="deployResult"
							  allowInput="false"
							  style="margin-top:10px;width:100%;height:160px;overflow-y: auto">等待部署...
					</div>
				</td>
			</tr>
		</table>
	</div>
</div>
<div class="mini-toolbar" style="height:5%;text-align: center;padding:0px;margin:0px;"
	 borderStyle="border:0;border-top:solid 1px #b1c3e0;">
	<a class="mini-button" onclick="onSubmit" style="width:80px;margin-right:20px;height:100%">执行部署</a>
</div>
</body>
</html>