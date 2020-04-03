<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript"
		src="${ctx}/js/configuremanager/masterstandby/rollbackOpt.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<div class="mini-fit" >
			<div id="runningProgramGrid" class="mini-datagrid" style="width: 100%;height:100%;overflow:auto;"
	             idField="ID" allowResize="false" allowCellselect="false" showFooter="false" >
				<div property="columns">
					<!-- <div type="checkcolumn" width="5" >选择</div> -->
					<div type="indexcolumn" width="5" headerAlign="center" align="center">序号</div>
					<div field="TASK_CODE" width="20" headerAlign="center" align="center">版本号</div>
	                <div field="PROGRAM_CODE" width="20" headerAlign="center" align="center">TOP名称</div>
	                <div field="RUN_STATE" width="20" headerAlign="center" align="center" renderer="stateRenderer">状态</div>
				</div>
			</div>
		</div>
		<div class="mini-toolbar" style="height:45px;text-align: center;margin-top:5px;">
   			<a class="mini-button"onclick="rollback()" id="sumbitButton" style="margin-top:10px;">回退</a> 
   			<span style="display: inline-block; width: 25px;margin-top:10px;"></span> 
   			<a class="mini-button" onclick="close()" style="margin-top:10px;">取消</a>
		</div>
    </div>
</body>
</html>