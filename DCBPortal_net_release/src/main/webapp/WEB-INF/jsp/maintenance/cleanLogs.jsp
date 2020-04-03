<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/maintenance/cleanLogs.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>主机代码/名称：</span>
	            	</th>
					<td>
						<input id="searchValue" name="searchValue" class="mini-textbox" style="width: 80%;"/>
					</td>
	            	<th>
	            		<span>主机状态：</span>
	            	</th>
					<td>
						<input class="mini-combobox" id="HOST_STATE" style="width:80%;"
                           data="getSysDictData('host_state')"  valueField="code" 
                           emptyText="=全部="  showNullItem="true" nullItemText="=全部="
                           name="HOST_STATE" />
					</td>
	                <td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
		               <td style="text-align: right;">
		               <a class="mini-button mini-button-green" onclick="clean()" width="100" plain="false">清理日志</a>
		               </td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="margin-top: 0px;">
		<div id="hostGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="HOST_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
			<div property="columns">
				<div type="checkcolumn" width="20" ></div>
				<div field="HOST_CODE"  headerAlign="center" align="center">主机代码</div>
				<div field="HOST_NAME"  headerAlign="center" align="center" renderer="onRenderTopName">主机名称</div>
				<div field="HOST_IP" width="150" headerAlign="center" align="center">主机IP地址</div>
                <div field="HOST_STATE_DESC" width="90" headerAlign="center" align="center">状态</div>
				<div name="action" width="150" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				
			</div>
		</div>
	</div>
    </div>
</body>
</html>