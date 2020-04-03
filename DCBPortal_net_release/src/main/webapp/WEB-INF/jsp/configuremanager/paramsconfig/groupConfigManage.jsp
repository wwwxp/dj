<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>云管理平台参数管理</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/paramsconfig/groupConfigManage.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="600px"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>参数名称：</span>
	            	</th>
					<td>
						<input id="CONFIG_NAME" name="CONFIG_NAME" class="mini-textbox" style="width:100%;" />
					</td>
					<th>
	            		<span>归属组：</span>
	            	</th>
					<td>
						<input id="GROUP_CODE" name="GROUP_CODE" class="mini-combobox" textField="GROUP_CODE" valueField="GROUP_CODE"
							   showNullItem="true" nullItemText="=请选择=" allowInput="true" style="width:100%;" />
					</td>
					<td>
	            		<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
	            	</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
			<div id="groupConfigGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				 emptyText="<font style='font-size:16px;'>数据为空，点击<a style='color:red;' href='javascript:addConfig()'>新增</a><font>" showEmptyText="true"
	             idField="CONFIG_CODE" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
				<div property="columns">
					<div type="indexcolumn" width="20" headerAlign="center" align="center">序号</div>
					<div field="GROUP_CODE" width="100" headerAlign="center" align="center" >属性组
						<input property="editor" required="true" maxlength="36" class="mini-textbox" style="width:100%;"/>
					</div>
	                <div field="CONFIG_NAME" width="120" headerAlign="center" align="center">参数名称
						<input property="editor" required="true" maxlength="100" class="mini-textbox" style="width:100%;"/>
					</div>
					<div field="CONFIG_VALUE" width="100" headerAlign="center" align="center">参数值
						<input property="editor" required="true" maxlength="100" class="mini-textbox" style="width:100%;"/>
					</div>
					<div field="CONFIG_DESC" width="120" headerAlign="center" align="center">参数描述
						<input property="editor" class="mini-textbox" maxlength="255" style="width:100%;"/>
					</div>
					<div field="SEQ" width="40" headerAlign="center" align="center" >显示顺序
						<input property="editor" class="mini-textbox" maxlength="6" style="width:100%;"/>
					</div>
					<div field="EXTENDS_FIELD" width="60" headerAlign="center" align="center" >扩展属性
						<input property="editor" class="mini-textbox" maxlength="50" style="width:100%;"/>
					</div>
					<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>