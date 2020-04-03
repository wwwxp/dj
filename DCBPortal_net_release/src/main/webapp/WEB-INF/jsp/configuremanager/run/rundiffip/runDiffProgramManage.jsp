<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>区分IP运行业务程序 </title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/rundiffip/runDiffProgramManage.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow-x:hidden;overflow-y: auto;">
    <div class="mini-panel" id="programPanel" style="width:100%;" title="添加程序启动实例" 
    		showCollapseButton="true" expanded="true" collapseOnTitleClick="true" onbuttonclick="addBtnClick">
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
					<input id=PROGRAM_NAME name="PROGRAM_NAME" allowInput="true"
						valueField="PROGRAM_CODE" textField="PROGRAM_NAME" onvaluechanged="changeProgramType"
						required="true" class="mini-combobox" style="width:98%;"/>
				</td>
				<th>
            		程序别名：
            	</th>
				<td>
					<input id="PROGRAM_ALIAS" name="PROGRAM_ALIAS"  class="mini-textbox" style="width:98%;" />
				</td>
				
			</tr>
			<tr>
				<th>
            		本地网：
            	</th>
				<td>
					<input id="LATN_ID" name="LATN_ID" valueField="CONFIG_VALUE" textField="CONFIG_TEXT" multiSelect="false"
						class="mini-combobox" showNullItem="true" allowInput="false"  style="width:98%;" onvaluechanged="changeLatnId"/>
				</td>
				<th>
					配置文件：
            	</th>
				<td>
					<input id="CONFIG_FILE" name="CONFIG_FILE" valueField="targetPath" textField="fileNameExt" allowInput="true"
					     multiSelect="true"  popupHeight="220px" popupWidth="100%"  onbeforenodecheck="beforenodeselect"
						class="mini-treeselect" parentField="parentId" showFolderCheckBox="true" onvaluechanged="changeConfigFile" style="width:98%;"/>
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
	<div id="queryForm" class="search" style="margin-top: 5px;">
		<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
			<colgroup>
				<col width="100"/>
				<col />
				<col width="60"/>
				<col />
				<col width="80"/>
				<col  />
				<col width="60"/>
				<col width="100"/>
				<col width="320"/>
			</colgroup>
			<tr>
				<th>
            		<span>程序名称/别名：</span>
            	</th>
				<td>
					<input id="QUERY_PROGRAM_NAME" name="QUERY_PROGRAM_NAME" 
						class="mini-textbox" style="width:100%;"/>
				</td>
				<th>
            		<span>本地网:</span>
            	</th>
				<td>
					<input id="QUERY_LATN_ID" name="QUERY_LATN_ID" 
						  showNullItem="true" allowInput="false"
						valueField="CONFIG_VALUE" textField="CONFIG_TEXT"
						class="mini-combobox" style="width:100%;"/>
				</td>
				
				<th>
            		<span>程序状态:</span>
            	</th>
				<td>
					<input id="QUERY_PROGRAM_STATE" name="QUERY_PROGRAM_STATE" 
						showNullItem="true" allowInput="false" data="getSysDictData('PROGRAM_STATE_LIST')"
						valueField="code" textField="text"
						class="mini-combobox" style="width:100%;"/>
				</td>
				 
				<th style="text-align: center;">
					<input style="vertical-align:text-bottom; margin-bottom:1px; margin-bottom:-2px\9;" type="checkbox" id="isCheckR" title="查询结果后，立即检查进程的状态">检查</input>
				</th>
				<th style="text-align: center;">
					<a class="mini-button" onclick="search()">查询</a>
				</th>
                <td style="text-align: right;margin-right: 10px;">
					<a class="mini-button mini-button-green" style="width:50px" onclick="addBatchRun()" plain="false">运行</a>
					<a class="mini-button mini-button-green" style="width:50px" onclick="addBatchStop()" plain="false">停止</a>
                   	<a class="mini-button mini-button-green" style="width:50px" onclick="checkHostState()" plain="false">检查</a>
                   	<a class="mini-button mini-button-green" style="width:50px" onclick="delProgramTask()" plain="false">删除</a>
					<a class="mini-button mini-button-green" style="width:50px" onclick="batchTermal()" plain="false">终端</a>
				</td>
			</tr>
		</table>
	</div>
	<table style="width: 100%">
		<tr>
			<td width="70%">
				当前版本：<span style="font-weight: bold;color: red;" id="versionTD"></span>，配置文件路径：<span style="font-weight: bold;color: red;" id="cfgPathTD"></span>
			</td>
			<td>
				<p style="margin: 3px 2px 0px 2px ;text-align: right;"><span style="font-weight: bold;color: black;">总计</span>：【&nbsp;<span id="countRow" style="color:#block;font-weight:bold;font-size: 15px"></span>&nbsp;】个程序，
				正在运行【&nbsp;<span id="runStatus" style="color:#59bd5d;font-weight:bold;font-size: 15px"></span>&nbsp;】，
				未运行【&nbsp;<span id="stopStatus" style="color: red;font-weight:bold;font-size: 15px"></span>&nbsp;】</p>
		
			</td>
		</tr>
	
	</table>
    <div id="programGrid" class="mini-datagrid" style="width: 100%; height: auto;margin-top: 5px;"
            idField="PROGRAM_ID" allowResize="false" showModified="false" allowCellEdit="true"
		 	allowCellSelect="true" cellEditAction="celldblclick" multiSelect="true" showFooter="false" sortMode="client">
		<div property="columns">
			<div type="checkcolumn" width="20" ></div>
			<div field="PROGRAM_NAME"  width="60" headerAlign="center" align="center">程序名称</div>
			<div name="PROGRAM_ALIAS"  field="PROGRAM_ALIAS" headerAlign="center" allowSort="true" width="60" >程序别名
                <input property="editor" class="mini-textbox" style="width:100%;" minWidth="30" />
            </div>			
            <div field="LATN_NAME" allowSort="true"  width="60" headerAlign="center" align="center">本地网</div>
			<div field="HOST_IP" allowSort="true" width="60" headerAlign="center" align="center" renderer="runHostIpRenderer">运行主机</div>
<!-- 			<div field="CONFIG_FILE" allowSort="true" width="120" headerAlign="center" align="center">配置文件</div>
 -->			<%--<div field="SCRIPT_SH_NAME"  allowSort="true" width="160" headerAlign="center" align="left">脚本</div>--%>

			<div field="SCRIPT_SH_NAME"  allowSort="true" width="160" headerAlign="center" align="left">脚本
				<input property="editor" class="mini-textbox" style="width:100%;" minWidth="100"/>
			</div>
            <div field="RUN_STATE" allowSort="true" width="40" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
			<div field="CRT_DATE" width="60" dateFormat="yyyy-MM-dd HH:mm:ss" headerAlign="center" align="center">操作时间</div>
			<div name="action" width="80" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
		</div>
	</div>
	<!-- <div style="height:30px;line-height:30px;margin-top: 5px;">
		<label>启停进度说明:</label>
   	</div>
 	<div id="deployTextarea" name="deployTextarea" style="min-height:260px;border: 1px solid #b1c3e0;overflow:auto;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div> -->
</div>

<!-- 终端操作隐藏表单 -->
<div style="display: none;">
	<form id="termialForm" name="termialForm" method="post" target="_blank">
		<input type="hidden" id="termialHost" name="termialHost"/>
		<input type="hidden" id="logName" name="logName" value="终端操作"/>
	</form>
</div>
</body>
</html>
