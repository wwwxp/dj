<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>日志级别配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/logLevelCfgNew/logLevelManage.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100px"/>
					<col width="260px"/>
					<col/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>参数名：</span>
	            	</th>
					<td>
						<input id="PRO_KEY" name="PRO_KEY" class="mini-textbox" style="width:95%;" />
					</td>
					<td>
	            		<a class="mini-button" onclick="search()" style="margin-right: 20px;">查询</a>
	            	</td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<colgroup>
					<col/>
					<col width="280px;"/>
				</colgroup>
				<tr>
					<td>
						<span style="color: red; font-weight: bold;">温馨提示：请在DCF_ENVIRONMENTS表中配置OCS_MCAST_CMD_ADDR变量，该变量表示需要发送命令主机信息，ENV_VALUE格式为IP:PORT，例如：192.168.168.12:8009</span>
					</td>
		        	<td style="text-align: right;">
		               	<a class="mini-button mini-button-green" onclick="add()"　width="100" plain="false">新增</a>
                    	<a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a> 
                    	<a class="mini-button mini-button-green" onclick="sendMsg()" plain="false">发送</a> 
		            </td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="logLevelGrid" class="mini-datagrid" style="width: 100%; height: 100%"
	             idField="PRO_ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="20" ></div>
	                <div field="PRO_KEY" width="120" headerAlign="center" align="center">参数名</div>
					<div field="PRO_VALUE" width="100" headerAlign="center" align="center" >参数值</div>
					<div field="PRO_DESC" width="100" headerAlign="center" align="center">备注</div>
					<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>