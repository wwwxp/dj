<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/masterstandbyABM/greyUpgradeConfigOpt.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px 5px 0px 5px;">
		<table id="paramForm" class="formTable3" 
			style="width: 100%; line-height:30px; margin-bottom: 10px; table-layout: fixed;">
			<colgroup>
				<col width="80" />
				<col />
				<col width="20" />
			</colgroup>
			<tbody id="paramsInfo">
				<tr>
					<th class="th3">
						<span class="fred">*</span>本地网选择
					</th>
					<td class="td3" colspan="2">
						<input id="latn_element" name="latn_element" class="mini-combobox" allowInput="false"
							showNullItem="false" style="width: 50%;" required="true" popupHeight="120"
							textField="CONFIG_NAME" valueField="CONFIG_VALUE" multiSelect="true" />
					</td>
				</tr>
			</tbody>
		</table>
		<div class="mini-toolbar" style="height: 45px; text-align: center; margin-top: 126px;">
			<a class="mini-button" onclick="submitUpgrade()" id="sumbitButton" style="margin-top: 10px;">确定</a>
			<span style="display: inline-block; width: 25px;"></span> 
			<a class="mini-button" onclick="close()" style="margin-top: 10px;">取消</a>
		</div>
	</div>
</body>
</html>