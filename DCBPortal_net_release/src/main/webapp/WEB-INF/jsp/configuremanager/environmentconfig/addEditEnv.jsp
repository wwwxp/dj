<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/environmentconfig/addEditEnv.js"></script>
</head>
<body>
	<div class="mini-fit p5">
        <table id="envForm" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="100" />
				<col />
				<col width="100" />
				<col />
			</colgroup>
			<tr>
				<th><span class="fred">*</span>变量名称：</th>
				<td>
					<input id="ENV_NAME" name="ENV_NAME" required="true"
						class="mini-textbox" style="width: 100%;" />
				</td>
				<th><span class="fred">*</span>状态：</th>
				<td>
					<input class="mini-combobox" style="width: 100%;" id="STATE" name="STATE"
					valueField="code" textField="text" data="getSysDictData('env_state')" value="1" />
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>所属集群：
            	</th>
				<td >
				    <input id="BUS_CLUSTER_ID" name="BUS_CLUSTER_ID" class="mini-combobox" 
								allowInput="false" showNullItem="false" style="width:100%;"
								valueField="BUS_CLUSTER_ID" textField="BUS_CLUSTER_NAME" 
								multiSelect="false"/>
				    
				</td>
				<th> </th>
				<td > </td>
			</tr>
			<tr>
				<th><span class="fred">*</span>变量值：</th>
				<td colspan="3"><input id="ENV_VALUE" name="ENV_VALUE" required="true"
					class="mini-textbox" style="width: 100%;" /></td>
			</tr>
		</table>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>