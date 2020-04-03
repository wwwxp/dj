<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>周边程序启停管理</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/other/otherProgramManage.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow-x:hidden;overflow-y: hidden;">
	<div class="mini-panel" id="programPanel" style="width:100%;" title="添加程序启动实例" 
    		showCollapseButton="true" expanded="false" collapseOnTitleClick="true">
        <table id="programForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>程序类型：
            	</th>
				<td >
					<input id="PROGRAM_NAME" name="PROGRAM_NAME" 
						valueField="PROGRAM_CODE" textField="PROGRAM_NAME" onvaluechanged="changeProgramType"
						required="true" class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		主机列表：
            	</th>
				<td>
					<input id="HOST_ID" name="HOST_ID" valueField="HOST_ID" textField="HOST_TEXT" multiSelect="true"
						class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
					配置文件：
            	</th>
				<td>
					<input id="CONFIG_FILE" name="CONFIG_FILE" valueField="fileName" textField="fileName" multiSelect="true"
						class="mini-combobox" onvaluechanged="changeConfigFile" style="width:98%;"/>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>脚本：
            	</th>
				<td colspan="3">
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
            	<td style="text-align: center;" colspan="6">
            		<a class="mini-button mini-button-green" onclick="addProgram()" style="width:60px;">提交</a> 
            		<span style="display: inline-block; width: 25px;"></span> 
            		<a class="mini-button mini-button-green" onclick="reset()" style="width:60px;">重置</a> 
            	</td>
			</tr>
		</table>
	</div>
	
	<div id="queryForm" class="search" style="margin-top: 5px;">
		<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
			<colgroup>
				<col width="100"/>
				<col />
				<col width="100"/>
				<col />
				<col width="100"/>
				<col />
				<col width="200"/>
				<col />
			</colgroup>
			<tr>
				<th>
            		<span>程序名称：</span>
            	</th>
				<td>
					<input id="QUERY_PROGRAM_NAME" name="QUERY_PROGRAM_NAME" 
						showNullItem="true" allowInput="false"
						valueField="PROGRAM_CODE" textField="PROGRAM_NAME"
						class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		<span>程序状态:</span>
            	</th>
				<td>
					<input id="QUERY_PROGRAM_STATE" name="QUERY_PROGRAM_STATE" 
						showNullItem="true" allowInput="false" data="getSysDictData('PROGRAM_STATE_LIST')"
						valueField="code" textField="text"
						class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		<span>主机IP:</span>
            	</th>
				<td>
					<input id="QUERY_HOST_ID" name="QUERY_HOST_ID" 
						showNullItem="true" allowInput="false"
						valueField="HOST_ID" textField="HOST_TEXT"
						class="mini-combobox" style="width:98%;"/>
				</td>
				<th style="text-align: center;">
					<a class="mini-button" onclick="search()">查询</a>
				</th>
                <td style="text-align: right;margin-right: 20px;">
                	<a class="mini-button mini-button-green" onclick="addBatchRun()" plain="false">运行</a>
                   	<a class="mini-button mini-button-green" onclick="addBatchDtop()" plain="false">停止</a> 
                </td>
			</tr>
		</table>
	</div>
    <div id="programGrid" class="mini-datagrid" style="width: 100%; height: 280px;margin-top: 5px;overflow-y: auto;overflow-x: hidden;" pageSize="10"
            idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false">
		<div property="columns">
			<div type="checkcolumn" width="20" ></div>
			<div field="PROGRAM_NAME"  width="100" headerAlign="center" align="center">程序名称</div>
			<div field="HOST_IP" width="60" headerAlign="center" align="center">运行主机</div>
			<div field="CONFIG_FILE" width="120" headerAlign="center" align="center">配置文件</div>
			<div field="SCRIPT_SH_NAME" width="160" headerAlign="center" align="center">脚本</div>
            <div field="RUN_STATE" width="40" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
			<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
		</div>
	</div>
	
	<div style="height:30px;line-height:30px;margin-top: 5px;">
		<label>启停进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="height:140px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>
</body>
</html>
