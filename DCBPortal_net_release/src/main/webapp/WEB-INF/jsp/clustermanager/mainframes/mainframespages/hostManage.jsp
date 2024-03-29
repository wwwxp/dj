<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/hostManage.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:80%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col />
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="200"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>主机IP：</span>
	            	</th>
					<td>
						<input id="searchHostIP" name="searchHostIP" class="mini-textbox" style="width: 95%;"/>
					</td>
					<th>
						<span>主机名称：</span>
					</th>
					<td>
						<input id="searchHostName" name="searchHostName" class="mini-textbox" style="width: 95%;"/>
					</td>
					<th>
						<span>主机用户：</span>
					</th>
					<td>
						<input id="searchUser" name="searchUser" class="mini-textbox" style="width: 95%;"/>
					</td>
	                <td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>

		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
		        	<td style="text-align: right;">
		               	<a class="mini-button mini-button-green" onclick="add()"　width="100" plain="false">新增</a>
                    	<a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a> 
                    	<a class="mini-button mini-button-green" onclick="terminal()" plain="false">终端操作</a>
						<a class="mini-button mini-button-green" onclick="inputExcelTemplate()" plain="false">批量导入</a>
						<a class="mini-button mini-button-green" onclick="cmdBatchExec()" style="width:100px" plain="false">命令批处理</a>

						<a class="mini-button mini-button-green" onclick="downLoadInfoFromExcel()" plain="false">模板下载</a>
						<a class="mini-button mini-button-green" onclick="batchUpdatePasswd()" plain="false">修改密码</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="hostGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="40"
	             idField="HOST_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="20" ></div>
	                <div field="HOST_NAME" width="120" headerAlign="center" align="center">主机名</div>
					<div field="HOST_IP" width="160" headerAlign="center" align="center" renderer="onRenderTopName">主机IP地址</div>
					<%--<div field="HOST_NET_CARD" width="160" headerAlign="center" align="center">IPV6网卡</div>--%>
					<div field="SSH_USER" width="60" headerAlign="center" align="center">用户</div>
	                <div field="SSH_PORT" width="60" headerAlign="center" align="center">端口</div>
	                <div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="100" headerAlign="center" align="center">创建时间</div>
					<div name="action" width="140" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>

	<div style="display: none;">
		<form id="downloadForm" action="${ctx}/host/downloadExcel" target="_blank" method="post">
		</form>
	</div>
    <div style="display: none;">
    	<form id="termialForm" name="termialForm" method="post" target="_blank">
    		<input type="hidden" id="termialHost" name="termialHost"/>
			<input type="hidden" id="termialCmd" name="termialCmd"/>
    		<input type="hidden" id="logName" name="logName" value="终端操作"/>
    	</form>
    </div>
</body>
</html>