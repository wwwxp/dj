<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/jobtaskcfg/timer/jobTimerTaskManage.js" type="text/javascript"></script>
	<title>定时任务配置</title>
</head>
<body>
    <div class="mini-fit p5" style="padding: 5px;">
    <div class="search2" >
			<table id="updateForm" class="search" cellpadding="0"
				cellspacing="0"
				style="width: 100%; height: 50px; table-layout: fixed; ">
				<tr>
					<td style="text-align: left; width: 80%;">
							<span style="margin-left: 5px;">任务名称：</span> <input id="SEARCH_PARAM" name="SEARCH_PARAM" class="mini-textbox" style="width:180px;margin-left: 5px;" /> <a class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a></td>
					<td style="text-align: right;"><a
						class="mini-button mini-button-green" onclick="add()" style="margin-right:20px"
						plain="false">新增</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
		<div id="datagrid" class="mini-datagrid"  style="width: 100%; height: 100%;"
             idField="ID" allowResize="false"  multiSelect="false" showPageInfo="false" showFooter="flase">
			<div property="columns"> 
				<div type="checkcolumn" ></div> 
			 	<!--  <div type="indexcolumn"></div>-->
				<div field="TASK_NAME" headerAlign="center" align="center" width="100">任务名称</div>
				
				<div field="TASK_TYPE" headerAlign="center" align="center" width="80" renderer="taskTypeRenderer">任务类型</div>
				<div field="CRON_EXP"  headerAlign="center" align="center" width="100" >定时任务</div>
				<div field="CRON_DESC" headerAlign="center" align="center" width="150" >定时任务描述</div>
				<div field="CRON_START_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="130">开始日期</div>
				<div field="CRON_END_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="130">结束日期</div>
				<div field="CRON_STATUS" headerAlign="center" align="center" width="100" renderer="statusTypeRenderer">任务状态</div>
				<!-- <div field="CRT_DATE" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="120">创建时间</div> -->

				<div field="action" headerAlign="center" align="center" width="120" renderer="optionRenderer">操作</div>
			</div>
		</div>
    </div>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">关闭</a>
    </div>
</body>
</html>
