<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/jobtaskcfg/jobTaskManage.js" type="text/javascript"></script>
	<script src="${ctx}/js/common/jquery.tmpl.js" type="text/javascript"></script>
    <script id="formTemplate" type="text/x-jquery-tmpl">
        &lt;table class="detailForm" style="width:100%;">
            &lt;tr>
                &lt;td style="width:80px;">任务名称：&lt;/td>
                &lt;td style="width:150px;">${TASK_NAME}&lt;/td>
                &lt;td style="width:80px;">任务执行类：&lt;/td>
                &lt;td style="width:150px;">${TASK_JOB_CLASS}&lt;/td>
                &lt;td style="width:80px;">执行参数：&lt;/td>
                &lt;td style="width:150px;">${TASK_JOB_PARAMS}&lt;/td>
                &lt;td style="width:80px;">任务类型：&lt;/td>
                &lt;td style="width:150px;">${TASK_TYPE_DESC}&lt;/td>
                &lt;td style="width:80px;">最后一次执行时间：&lt;/td>
                &lt;td style="width:150px;">${TASK_EXEC_LAST_TIME}&lt;/td>
                &lt;td style="width:80px;">运行状态：&lt;/td>
                &lt;td style="width:150px;">${EXEC_STATUS_DESC}&lt;/td>
            &lt;/tr>
            &lt;tr>
               &lt;td style="width:80px;">CRON表达式：&lt;/td>
                &lt;td style="width:150px;">${CRON_EXP}&lt;/td>
                &lt;td style="width:80px;">CRON表达式描述：&lt;/td>
                &lt;td style="width:150px;">${CRON_DESC}&lt;/td>
                &lt;td style="width:80px;">开始日期：&lt;/td>
                &lt;td style="width:150px;">${CRON_START_TIME}&lt;/td>
                &lt;td style="width:80px;">结束日期：&lt;/td>
                &lt;td style="width:150px;">${CRON_END_TIME}&lt;/td>
                &lt;td style="width:80px;">任务状态：&lt;/td>
                &lt;td style="width:150px;">${TASK_STATUS_DESC}&lt;/td>
            </tr>
        </table>
    </script>
	<title>任务配置</title>
</head>
<body>
    <div class="mini-fit p5" style="padding: 5px;">
    <div class="search" >
		<table id="queryFrom" class="formTable8" style="width:65%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
			<colgroup>
				<col width="150"/>
				<col  width="100"/>
				<col/>
				<col width="100"/>
				<col/>
				<col width="200"/>
			</colgroup>
			<tr>
				<th>
					<a class="mini-button mini-button-green" onclick="add()" style="margin-right:20px" plain="false">新增</a>
				</th>
				<th>
					<a class="mini-button mini-button-green" onclick="del()" style="margin-right:20px" plain="false">删除</a>
				</th>
				<th>
					<span>任务名称：</span>
				</th>
				<td>
					<input  name="TASK_NAME" class="mini-textbox" style="width: 100%;"/>
				</td>
				<th>
					<span>任务类型：</span>
				</th>
				<td>
					<input  name="TASK_TYPE" class="mini-combobox" style="width: 100%;" textField="text" valueField="code"
							showNullItem="true" allowInput="false" data="getSysDictData('job_type')"/>
				</td>
				<td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
			</tr>
		</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
		<div id="datagrid" class="mini-datagrid"  style="width: 100%; height: 100%;overflow:auto;" onshowrowdetail="onShowRowDetail"
             idField="ID" allowResize="false"  multiSelect="true" showPageInfo="true" showFooter="true">
			<div property="columns"> 
				<div type="checkcolumn" ></div>
				<div type="expandcolumn"></div>
			 	<!--  <div type="indexcolumn"></div>-->
				<div field="TASK_NAME" headerAlign="center" align="center" width="100px">任务名称</div>
				<%--<div field="TASK_JOB_CLASS" headerAlign="center" align="center" width="100">任务执行类</div>--%>
				<div field="TASK_JOB_PARAMS" headerAlign="center" align="left" width="200px">执行参数</div>
				<div field="TASK_TYPE_DESC" headerAlign="center" align="center" width="40px" >任务类型</div>
					<div field="CRON_EXP"  headerAlign="center" align="center" width="70px" >CRON表达式</div>
					<div field="CRON_DESC" headerAlign="center" align="left" width="100px" >CRON表达式描述</div>
					<div field="TASK_EXEC_LAST_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="70px">最后一次执行时间</div>
					<%--<div field="CRON_START_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="90px">开始日期</div>
                    <div field="CRON_END_TIME" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="90px">结束日期</div>--%>
				<div field="EXEC_STATUS_DESC" headerAlign="center" align="center" width="40px">运行状态</div>
				<div field="TASK_STATUS_DESC" headerAlign="center" align="center" width="40px">任务状态</div>
				<!-- <div field="CRT_DATE" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="120px">创建时间</div> -->

				<div field="action" headerAlign="center" align="center" style="min-width: 100px" width="100px" min-width="100px" renderer="onActionRenderer">操作</div>
			</div>
		</div>
    </div>
    </div>
    <div id="detailForm" style="display: none;">
        <table>
            <tr style="height: 20px">
                <td style="width:80px;">任务执行类：</td>
                <td  style="width:280px;"><span id="TASK_JOB_CLASS_DETAIL"></span></td>
                <td style="width:80px;">开始日期：</td>
                <td  style="width:150px;"><span id="CRON_START_TIME_DETAIL"></span></td>
                <td style="width:80px;">结束日期：</td>
                <td  style="width:150px;"><span id="CRON_END_TIME_DETAIL"></span></td>
                <td style="width:80px;">创建时间：</td>
                <td  style="width:150px;"><span id="CRT_DATE_DETAIL"></span></td>
            </tr>
            <tr style="height: 20px">
                <td style="width:80px;">任务描述：</td>
                <td><span id="TASK_DESC_DETAIL"></span></td>
            </tr>
        </table>
    </div>
</body>
</html>
