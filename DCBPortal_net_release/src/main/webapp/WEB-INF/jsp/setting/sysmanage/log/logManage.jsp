<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/setting/sysmanage/log/logManage.js" type="text/javascript"></script>
	<title>系统日志管理</title>
</head>
<body>
    <div class="mini-fit" style="padding: 2px;">
	<div class="search" id="queryFrom" style=" padding: 0px;margin-bottom: 5px;height: 50px;">
		<table style="width: 100%;height: 100%;">
			<tr>
                 <td width="50px;"><!--<a class="mini-button" onclick="refresh()" plain="false">刷新</a>--></td>
				<td><span style="margin-left: 5px;">操作用户：</span>
                    <input id="LOGIN_USER" name="LOGIN_USER" class="mini-textbox" style="width:180px;margin-left: 5px;" />
                    <a class="mini-button" onclick="search()" style="margin-left: 5px;">查询</a></td>
			</tr>
		</table>
	</div>
	<div class="mini-fit">
		<div id="log_datagrid" class="mini-datagrid"  style="width: 100%; height: 100%;"
             url="${ctx}/coreAction.do?method=queryForList&execKey=core.querySysLog"
             idField="EMPEE_ID" allowResize="false"  multiSelect="true">
			<div property="columns">
				<div field="LOG_NAME" headerAlign="center" align="center" width="150">日志名称</div>
				<div field="LOGIN_USER" headerAlign="center" align="center" width="150">操作用户</div>
				<div field="EXEC_TYPE" headerAlign="center" align="center" width="150">执行类型</div>
				<div field="METHOD" headerAlign="center" align="center" width="150">方法</div>
				<div field="IP" headerAlign="center" align="center" width="150">IP地址</div>
				<div field="STATE_DATE" headerAlign="center"  align="center" width="180" dateFormat="yyyy-MM-dd HH:mm:ss">操作时间</div>
                <div field="PARAMS" headerAlign="center" align="left" width="100%">参数</div>
                <div field="action" headerAlign="center" align="center" width="100" renderer="actionRenderer">操作</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
