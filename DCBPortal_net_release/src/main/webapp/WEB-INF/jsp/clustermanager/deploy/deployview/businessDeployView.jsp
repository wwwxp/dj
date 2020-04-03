<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>业务部署图</title>
	<%@ include file="/public/common/common.jsp" %>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/graph/common.css" />
	<script type="text/javascript" src="${ctx}/js/clustermanager/deploy/businessDeployView.js"></script>
</head>
<body>
<canvas style="border: 0px solid blue;margin: 0px;background: transparent" id="deployCanvas">当前浏览器不支持Canvas！</canvas>

<div style="padding: 5px;position: absolute;top: 0px;left: 0px;height: 45px;right: 0px;">
	<table style="width: 100%;height: 100%;">
		<colgroup>
			<col>
			<col width="80">
			<col width="60">
		</colgroup>
		<tr>
			<td>
				<ul class="tabNav">

				</ul>
			</td>
			<td>
				<input id="autoCom" name="autoCom" class="mini-combobox" onvaluechanged="autoRefresh()" textField="text" valueField="code"
					   showNullItem="false" allowInput="false" value="0"
					   data="getSysDictData('REFRESH_CONFIG')">
			</td>
			<td>
				<img src="${ctx}/images/deployGraph/refresh.png" onclick="handleRefresh()" title="手动刷新" style="margin-left: 10px;"/>
			</td>
		</tr>
	</table>
</div>

<div id="deployView" style="padding: 5px;position: absolute;top: 35px;left: 0px;">
	<div class="tabCnt">
		<div class="tabPane hover">
			<table id="busTab" class="outer-table">
				<tr>
					<th>业务层</th>
					<td id="busPrevTD" style="width: 50%;"></td>
					<td>
						<ul id="busView" class="outer-ul" ulFlag='line'>

						</ul>
					</td>
					<td id="busNextTD" style="width: 50%;"></td>
				</tr>
			</table>
			<table id="compTab" class="outer-table">
				<tr>
					<th>基础组件</th>
					<td id="comPrevTD" style="width: 50%;"></td>
					<td>
						<ul id="compView" class="outer-ul" ulFlag='line'>

						</ul>
					</td>
					<td id="comNextTD" style="width: 50%;"></td>
				</tr>
			</table>
		</div>
	</div>
</div>
</body>
</html>