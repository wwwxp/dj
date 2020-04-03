<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/jobtaskcfg/convention/jobTaskManage.js" type="text/javascript"></script>
	<title>常规任务配置</title>
</head>
<body>
    <div class="mini-fit p5">
    <div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" cellpadding="0"
				cellspacing="0"
				style="width: 100%; padding: 0px; height: 35px; table-layout: fixed; border: 0px;">
				<tr>
					<td style="text-align: left; width: 80%;">
							</td>
					<td style="text-align: right;"><a
						class="mini-button mini-button-green" onclick="add()"
						plain="false">新增</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
		<div id="datagrid" class="mini-datagrid"  style="width: 100%; height: 100%;"
             idField="ID" allowResize="false"  multiSelect="false">
			<div property="columns"> 
				<div type="indexcolumn"></div>
				<div field="TASK_NAME" headerAlign="center" align="center" width="150">任务名称</div>
				<div field="EXEC_TYPE"  headerAlign="center" align="center" width="100" renderer="execTypeRenderer">任务分类</div>
				<div field="TASK_JOB_PARAMS" headerAlign="center" align="center" width="150" renderer="paramsRenderer">任务内容</div>
				<div field="TASK_DES" headerAlign="center" align="center" >任务描述</div>
				<!-- 
			    <div field="STATUS" headerAlign="center" align="center" width="60" renderer="isalarmRenderer">是否告警</div>
                 -->
                <div field="action" headerAlign="center" align="center" width="100" renderer="optionRenderer">操作</div>
			</div>
		</div>
    </div>
    </div>
</body>
</html>
