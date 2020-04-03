<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/runsameip/addEditProgram.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="programForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/> 
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>程序名称：
            	</th>
				<td >
					<input id="PROGRAM_NAME" name="PROGRAM_NAME" 
						valueField="PROGRAM_CODE" textField="PROGRAM_NAME" onvaluechanged="changeProgramType"
						required="true" class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		程序别名：
            	</th>
				<td>
					<input id="PROGRAM_ALIAS" name="PROGRAM_ALIAS" class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
			<tr>
				<th>
            		主机列表：
            	</th>
				<td>
					<input id="HOST_ID" name="HOST_ID" valueField="HOST_ID" textField="HOST_TEXT" multiSelect="true"
						class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		本地网：
            	</th>
				<td>
					<!--onvaluechanged="changeLatnId"-->
					<input id="LATN_ID" name="LATN_ID" valueField="CONFIG_VALUE" textField="CONFIG_TEXT" multiSelect="false"
						class="mini-combobox" showNullItem="true" allowInput="false" style="width:98%;"/>
				</td>
			</tr>
			<tr>
				
            	<th>
					配置文件：
            	</th>
				<td colspan="3">
					<input id="CONFIG_FILE" name="CONFIG_FILE" valueField="targetPath" textField="fileNameExt" 
					     multiSelect="true"  popupHeight="220px" popupWidth="100%"  onbeforenodeselect="beforenodeselect"
						class="mini-treeselect" parentField="parentId" showFolderCheckBox="false" onvaluechanged="changeConfigFile" style="width:99%;"/>
				</td>
				 
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>脚本：
            	</th>
				<td  colspan="3">
					<input id="SCRIPT_SH_NAME" name="SCRIPT_SH_NAME" required="true" class="mini-textbox" style="width:99%;" />
				</td>
            	 
			</tr>
			<!-- <tr>
				 
            	<th>
            		脚本参考示例：
            	</th>
				<td colspan="3">
					<span class="fred" id="exampleSh"></span>
				</td>
			</tr> -->
			<tr>
				<th>
            		程序描述：
            	</th>
				<td colspan="3">
					<input id="PROGRAM_DESC" name="PROGRAM_DESC" vtype="rangeChar:0,220" class="mini-textarea mini-textArea" style="width:98%;" />
				</td>
            	 
			</tr>
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="addProgram" style="width:60px;margin-right:20px;">提交</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
