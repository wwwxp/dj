<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>采集运行程序 </title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/dcm/dcmProgramManage.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow-x:hidden;overflow-y: auto;">
    <div class="mini-panel" id="programPanel" style="width:100%;" title="添加程序启动实例" 
    		showCollapseButton="true" expanded="false" collapseOnTitleClick="true" onbuttonclick="addBtnClick">
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
					<input id=PROGRAM_NAME name="PROGRAM_NAME" 
						valueField="PROGRAM_CODE" textField="PROGRAM_NAME" onvaluechanged="changeProgramType"
						required="true" class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
					<span class="fred">*</span>配置文件：
            	</th>
				<td>
					<input id="CONFIG_FILE" name="CONFIG_FILE" valueField="fileName" textField="fileName" multiSelect="true"
						required="true" class="mini-combobox" onvaluechanged="changeConfigFile" style="width:98%;"/>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>脚本：
            	</th>
				<td>
					<input id="SCRIPT_SH_NAME" name="SCRIPT_SH_NAME" required="true" class="mini-textbox" style="width:98%;" />
				</td>
            	<th>
            		脚本参考示例：
            	</th>
				<td>
					<span class="fred" id="exampleSh"></span>
				</td>
			</tr>
			<tr>
            	<td style="text-align: center;" colspan="4">
            		<a class="mini-button mini-button-green" onclick="addProgram()" style="width:60px;">提交</a> 
            		<span style="display: inline-block; width: 25px;"></span> 
            		<a class="mini-button mini-button-green" onclick="reset()" style="width:60px;">重置</a> 
            	</td>
			</tr>
		</table>
	</div>
	
    <div id="programGrid" class="mini-datagrid" style="width: 100%; height: auto;margin-top: 5px;"
            idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false">
		<div property="columns">
			<div type="indexcolumn" width="20" ></div>
			<div field="PROGRAM_NAME"  width="80" headerAlign="center" align="center">程序名称</div>
			<div field="HOST_IP" width="60" headerAlign="center" align="center" renderer="runHostIpRenderer">运行主机</div>
			<div field="CONFIG_FILE" width="120" headerAlign="center" align="center">配置文件</div>
			<div field="SCRIPT_SH_NAME" width="160" headerAlign="center" align="center">脚本</div>
            <div field="RUN_STATE" width="40" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
			<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
		</div>
	</div>
	
	<div style="height:30px;line-height:30px;margin-top: 5px;">
		<label>启停进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>
</body>
</html>
