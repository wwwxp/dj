<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Topology启停</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/run/runtopology/runTopologyProgramManage.js"></script>
</head>
<body>
<div id="mainDiv" class="mini-fit p5" style="overflow-x:hidden;overflow-y: auto;">
	<div class="mini-panel" id="programPanel" style="width:100%;" title="添加程序启动实例" 
    		showCollapseButton="true" expanded="true" collapseOnTitleClick="true" onbuttonclick="addBtnClick">
        <table id="programForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<!-- <col width="100"/>
				<col/>
				<col width="100"/>
				<col/> -->
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
            		<span class="fred">*</span>脚本：
            	</th>
				<td>
					<input id="SCRIPT_SH_NAME" name="SCRIPT_SH_NAME" required="true" class="mini-textbox" style="width:98%;" />
				</td>
				<th>
					本地网：
				</th>
				<td>
					<input id="LATN_ID" name="LATN_ID" valueField="CONFIG_VALUE" textField="CONFIG_TEXT" multiSelect="false"
						   class="mini-combobox" showNullItem="true" allowInput="false"  style="width:98%;" />
				</td>

				<!-- <th>
            		程序别名：
            	</th>
				<td>
					<input id="PROGRAM_ALIAS" name="PROGRAM_ALIAS"  class="mini-textbox" style="width:98%;" />
				</td> -->
				
				<!-- <th>
            		程序所属组：
            	</th>
				<td >
					<input id="PROGRAM_GROUP" name="PROGRAM_GROUP" class="mini-textbox" style="width:98%;" />
				</td> -->
			</tr>
			<tr>
            	<th>
            		脚本参考示例：
            	</th>
				<td colspan="5">
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
				<col width="80"/>
				<col /> 
				<col width="80"/>
				<col  />
				<col width="80"/>
				<col  />
				<col width="70"/>
				<col width="150"/>
				<col width="320"/>
			</colgroup>
			<tr>
				<th>
            		<span>程序名称：</span>
            	</th>
				<td>
					<input id="QUERY_PROGRAM_NAME" name="QUERY_PROGRAM_NAME" 
						class="mini-textbox" style="width:100%;"/>
				</td>
				<th>
            		<span>本地网:</span>
            	</th>
				<td>
					<input id="QUERY_LATN_ID" name="QUERY_LATN_ID" showNullItem="true" allowInput="false"
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
                   	<a class="mini-button mini-button-green" style="width:50px" onclick="checkState()" plain="false">检查</a>
                   	<a class="mini-button mini-button-green" style="width:50px" onclick="delProgramTask()" plain="false">删除</a>
                </td>
			</tr>
		</table>
	</div>
	
    <div id="programGrid" class="mini-datagrid" style="width: 100%; height: auto;margin-top: 5px;" sortMode="client"
            idField="PROGRAM_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false">
		<div property="columns">
			<div type="checkcolumn" width="20" ></div>
			<div field="PROGRAM_NAME"  allowSort="true" width="120" headerAlign="center" align="center">程序名称</div>
			<!-- <div field="PROGRAM_ALIAS"  width="120" headerAlign="center" align="center">程序别名</div>
			<div field="LATN_NAME"  width="50" headerAlign="center" align="center">本地网</div> -->
			<div field="CONFIG_FILE" allowSort="true" width="110" headerAlign="center" align="center">配置文件</div>
			<%--<div field="PROGRAM_GROUP" allowSort="true" width="50" headerAlign="center" align="center">程序所属组</div>--%>
			<div field="LATN_NAME" allowSort="true" width="45" headerAlign="center" align="center">本地网</div>
			<div field="SCRIPT_SH_NAME" allowSort="true" width="120" headerAlign="center" align="center">脚本</div>
            <div field="RUN_STATE" width="40" allowSort="true" headerAlign="center" align="center" renderer="runStateRenderer">状态</div>
			<div field="CRT_DATE" width="60" dateFormat="yyyy-MM-dd HH:mm:ss" headerAlign="center" align="center">操作时间</div>
			<div name="action" width="140" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
		</div>
	</div>
    
    <!-- 启停程序输出信息 -->
    <div style="height:30px;line-height:30px;">
		<label>启停进度说明:</label>
   	</div>
    <div id="businessTextarea" name="businessTextarea" style="min-height:260px;border: 1px solid #b1c3e0;padding-left: 2px;" ></div>
 	<div style="height: 5px;"></div>
</div>
</body>
</html>
