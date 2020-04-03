<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/masterstandby/greyUpgradeConfigOpt.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px 5px 0px 5px;">
		<table id="paramForm" class="formTable3"
			style="width: 100%; height: 430px; margin-bottom: 5px; table-layout: fixed;">
			<colgroup>
				<col width="80" />
				<col />
				<col width="20" />
			</colgroup>
			<tbody id="paramsInfo">
				<tr>
					<th class="th3"><input
						style="float: left; margin: 2px 0px 0px 5px;" type="radio"
						value="ele" name="searchRadio" /><span>按网元：</span></th>
					<td class="td3" colspan="2">网元选择： <input id="net_element"
						name="net_element" class="mini-combobox" allowInput="false"
						showNullItem="false" style="width: 25%;" valueField="code"
						textField="text" multiSelect="true" />
					</td>
				</tr>
				<tr style="height: 417px; width: 100%;">
					<th class="th3"><input
						style="float: left; margin: 2px 0px 0px 5px;" type="radio"
						value="num" name="searchRadio" checked="true" /><span>按号段：</span></th>
					<td class="td3">
						<div class="mini-fit">
							<div id="numDatagrid" class="mini-datagrid"
								style="width: 100%; height: 100%" allowResize="false"
								idField="id" showEmptyText="false" showFooter="false">
								<div property="columns">
									<div name="action" width="9" headerAlign="center"
										align="center" renderer="onActionRenderer">操作</div>
									<div type="comboboxcolumn" field="busType" width="30"
										headerAlign="center" align="center">
										业务类型 <input property="editor" id="busType" name="busType"
											class="mini-combobox" allowInput="false" showNullItem="false"
											style="width: 100%; height: 100%;" data="busTypeData"
											valueField="CONFIG_VALUE" textField="CONFIG_NAME" />
									</div>
									<div field="startNum" width="30" headerAlign="center"
										align="center">
										开始号段 <input property="editor" id="startNum" name="startNum"
											class="mini-textbox" vtype="int;maxLength:11;minLength:11"
											style="height: 100%; width: 100%;" />
									</div>
									<div field="endNum" width="30" headerAlign="center"
										align="center">
										结束号段 <input property="editor" id="endNum" name="endNum"
											class="mini-textbox" vtype="int;maxLength:11;minLength:11"
											style="height: 100%; width: 100%;" />
									</div>
								</div>
							</div>
						</div>
					</td>
					<td class="td3"><div class="icon-add" title="添加行"
							style="cursor:pointer;height: 16px; width: 16px;" onclick="addRow()"></div></td>
				</tr>
			</tbody>
		</table>
		<div class="mini-toolbar"
			style="height: 45px; text-align: center; ">
			<a class="mini-button" onclick="submitUpgrade()" id="sumbitButton" style="margin-top: 10px;">确定</a>
			<span style="display: inline-block; width: 25px;margin-top: 10px;" ></span> <a
				class="mini-button" onclick="close()" style="margin-top: 10px;">取消</a>
		</div>
	</div>
</body>
</html>