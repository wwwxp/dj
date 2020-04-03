<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/jobtaskcfg/convention/addEditJobTask.js"></script>
</head>
<body>
	<div class="mini-fit p5">
        <table id="addEditForm" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="90" />
				<col />
				<col width="90" />
				<col />
				 
			</colgroup>
			<tr>
				<th><span class="fred">*</span>任务名称：</th>
				<td  colspan="3">
					<input id="TASK_NAME" name="TASK_NAME" required="true"
						class="mini-textbox" style="width: 100%;" />
				</td>
				 <!-- <th><span class="fred">*</span>是否告警：</th>
				 <td><input width="100%" name="IS_ALARM" id="IS_ALARM" class="mini-combobox"
							textField="text" valueField="code" value="no"
							required="true" data="getSysDictData('job_is_alarm')"/></td> -->
			</tr>
			 <tr>
				<th><span class="fred">*</span>任务类型：</th>
				<td colspan="3">
					<input width="100%" name="EXEC_TYPE" id="EXEC_TYPE" class="mini-combobox"
							textField="text" valueField="code" value="cmd"
							required="true" data="getSysDictData('job_type_cfg')"/>
				</td>
			</tr>
			<tr id="cmd_flag">
				<th>
            		<span class="fred">*</span>主机：
            	</th>
				<td>  <input id="HOST_ID" name="HOST_ID"  
					class="mini-combobox" textField="HOST_TEXT" valueField="HOST_ID" 
							 style="width: 100%;" /></td>
				<th>
            		<span class="fred">*</span>命令行：
            	</th>
				<td>  <input id="CMD_DESC" name="CMD_DESC" required="true"
					class="mini-combobox" textField="CMD_NAME" valueField="ID" 
							 style="width: 100%;" /></td>
				 
			</tr>
			<tr id="datasource_flag">
				<th><span class="fred">*</span>数据源：</th>
				<td colspan="3"><input id="DATASOURCE_DESC" name="DATASOURCE_DESC" required="true"
					class="mini-combobox" textField="DATASOURCE_NAME" valueField="DATASOURCE_ID" 
							 style="width: 100%;" /></td>
			</tr>
			<tr id="datasource_sql_flag">
				<th><span class="fred">*</span>SQL：</th>
				<td colspan="3"><input id="TASK_CONTENT" name="TASK_CONTENT"  class="mini-textarea" style="width: 100%;height:60px;" /></td>
			</tr>
			<!-- <tr>
				<th>
            		<span class="fred">*</span>任务执行子类：
            	</th>
				<td><input id="TASK_JOB_CLASS" name="TASK_JOB_CLASS" required="true"
					class="mini-textbox" style="width: 100%;" /></td>
				 
			</tr>
			<tr>
				<th><span class="fred">*</span>参数：</th>
				<td><input id="TASK_JOB_PARAMS" name="TASK_JOB_PARAMS" required="true"
					class="mini-textbox" style="width: 100%;" /></td>
			</tr> -->
			<tr>
				<th><span class="fred">*</span>描述：</th>
				<td colspan="3">
				
				<input id="TASK_DES" name="TASK_DES"  class="mini-textarea" style="width: 100%;height:100px;" />
				</td>
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