<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>DCCP云计费平台</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<style type="text/css">
	fieldset {
		border: solid 1px #aaa;
	}
	.hideFieldset {
		border-left: 0;
		border-right: 0;
		border-bottom: 0;
	}
	.hideFieldset .fieldset-body {
		display: none;
	}
	</style>
	
<script language="javascript" type="text/javascript" src="${ctx}/js/jobtaskcfg/timer/addEditJobTimerTask.js"></script>
</head>
<body>
	<div class="mini-fit p5">
        <table id="addEditForm" class="formTable6" style="table-layout: fixed;">
			<colgroup>
				<col width="100" />
				<col />
				<col width="100" />
				<col />
				 
			</colgroup>
			<tr>
				<th><span class="fred">*</span>任务名称：</th>
				<td colspan="3">
					<input id="TASK_NAME" name="TASK_NAME" required="true"
						class="mini-textbox" style="width: 100%;" />
				</td>
				<!-- 
				 <th><span class="fred">*</span>是否告警：</th>
				 <td><input width="100%" name="IS_ALARM" id="IS_ALARM" class="mini-combobox"
							textField="text" valueField="code" value="no"
							required="true" data="getSysDictData('job_is_alarm')"/></td>
						 -->
			</tr>
		</table>
		 <!-- 定时任务调度 -->
        <fieldset id="taskfieldset" >
			<legend>
				<span>执行时间配置</span>
			</legend>
			<div class="fieldset-body" height="100%">
				<table id="taskForm" class="formTable6" style="table-layout: fixed;">
		            <colgroup>
		                <col width="105px" />
		                <col />
		                <col width="105px" />
		                <col />
		            </colgroup>
		            <tr>
						<th><span class="fred">*</span>任务类型：</th>
						<td>
							<input width="100%" name="TRIGGER_TYPE" id="TRIGGER_TYPE" class="mini-combobox"
									textField="text" valueField="code" 
									required="true" data="getSysDictData('job_task_type')"/>
						</td>
						
						<th><span class="fred">*</span>任务状态：</th>
						<td>
							<input width="100%" name="CRON_STATUS" id="CRON_STATUS" class="mini-combobox"
									textField="text" valueField="code"  
									required="true" data="getSysDictData('job_task_cron_status')"/>
						</td>
					</tr>
					
					<!-- 简单表达式 -->
					<tr id="timeExp">
		                <th><span class="fred">*</span>调度起始时间：</th>
		                <td>
		                	<input width="100%" name="CRON_START_TIME" id="CRON_START_TIME"
		                		format="yyyy-MM-dd H:mm:ss" timeFormat="H:mm:ss" showTime="true" showOkButton="true" 
		                		showClearButton = "true" class="mini-datepicker" required="true" />
		                </td>
		                <th class="showOrHide"><span class="fred">*</span>调度结束时间：</th>
		                <td class="showOrHide">
		                	<input width="100%" name="CRON_END_TIME" id="CRON_END_TIME" 
		                		format="yyyy-MM-dd H:mm:ss" timeFormat="H:mm:ss" showTime="true" showOkButton="true" 
		                		showClearButton = "true" class="mini-datepicker" required="true" />
		               	</td>
		            </tr>
					<tr id="intervalExp">
						<th><span class="fred">*</span>间隔时间：</th>
						<td colspan="3">
							  <input id="intervalSecond" name="intervalSecond" class="mini-spinner" required="true"
							  		minValue="1" value="5" allowInput="true" increment="5" maxValue="3600" />
							  		秒执行一次
						</td>
					</tr>
					
					<!-- Cron表达式 -->
					<tr id="offsetTip">
						<th><span class="fred">*</span>偏移量：</th>
						<td colspan="3">
							<input id="offsetTip" width="100%" class="mini-buttonedit"  allowInput="false" required="true"
		                          name="offsetTip" showClose="true" oncloseclick="onCloseClick" onbuttonclick="showCycleWindow"/>
						</td>
					</tr>
					<tr id="offsetExp">
						<th>cron表达式：</th>
						<td colspan="3">
							<input id="offset" name="offset"  enabled="false" class="mini-textbox" width="100%" />
						</td>
					</tr>
				</table>
			</div>
		</fieldset>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>