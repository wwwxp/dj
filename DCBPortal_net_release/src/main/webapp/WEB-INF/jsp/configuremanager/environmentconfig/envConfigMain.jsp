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
	src="${ctx}/js/configuremanager/environmentconfig/envConfigMain.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<!-- <div id="cluster_tabs" class="mini-tabs" activeIndex="0" plain="false"
			tabAlign="left" tabPosition="top" onactivechanged="loadPage"
			style="height: 40px; width: 100%;"></div> -->
		<div id="queryForm" class="search">
			<table class="formTable8"
				style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
				cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100" />
					<col width="200" />
					<col width="100" />
					<col width="200" />
					<col width="130" />
					<col width="100" />
					<col />
				</colgroup>
				<tr>
					<th><span>所属集群：</span></th>
					<td><input id="BUS_CLUSTER_ID" name="BUS_CLUSTER_ID"
						class="mini-combobox" allowInput="false" showNullItem="false"
						style="width: 100%;" valueField="BUS_CLUSTER_ID"
						textField="BUS_CLUSTER_NAME" multiSelect="false" /></td>
					<th><span>环境变量名称：</span></th>
					<td><input id="ENV_NAME" name="ENV_NAME" class="mini-textbox"
						style="width: 80%;" /></td>
					<td><span><input id="IS_ALL" name="IS_ALL" type="checkbox" checked="checked"
						style="vertical-align: -2px;" /><label for="IS_ALL"
						style="margin-left: 5px;cursor: pointer;">公用，集群所有</label></span></td>
					<td><a class="mini-button" onclick="search()"
						style="margin-left: 20px;">查询</a></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" cellpadding="0"
				cellspacing="0"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;">
				<tr>
					<td style="text-align: left; width: 80%;">
							<!-- <span style="color: red; font-weight: bold;"> -->
							<span style="color: red; font-weight: bold;">温馨提示：本页面的环境变量只包含启topology时用到，与周边程序无关。</span><br/>
							<span style="color: red; font-weight: bold;">“$P” 代表业务集群根目录，“$V” 业务集群版本号，“$BV” 代表JSTORM版本号， “$CP” 代表JSTORM根目录，“$MV” 代表M2DB版本号， “$MP” 代表M2DB根目录，“$DV” 代表DMDB版本号， “$DP” 代表DMDB根目录 。</span></td>
					<td style="text-align: right;"><a
						class="mini-button mini-button-green" onclick="add()"
						plain="false">新增</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
			<div id="envGrid" class="mini-datagrid"
				style="width: 100%; height: 100%" idField="ID" allowResize="false"
				allowCellselect="false" showFooter="true">
				<div property="columns">
					<div type="indexcolumn" width="20" headerAlign="center">选择</div>
					<div field="ENV_NAME" width="120" headerAlign="center"
						align="center">变量名称</div>
					<div field="ENV_VALUE" width="180" headerAlign="center"
						align="center">变量值</div>
					<div field="BUS_CLUSTER_NAME" width="80" headerAlign="center"
						align="center">所属集群</div>
					<div field="STATE" width="60" headerAlign="center" align="center"
						renderer="stateRenderer">状态</div>
					<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="80" headerAlign="center" align="center">创建时间</div>
					<div field="opt" width="80" headerAlign="center" align="center"
						renderer="optionRenderer">操作</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>