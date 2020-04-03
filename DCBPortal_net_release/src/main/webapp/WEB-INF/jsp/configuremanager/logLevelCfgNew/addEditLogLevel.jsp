<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>新增 &修改日志级别</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/logLevelCfgNew/addEditLogLevel.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
 		<div style="width: 100%;height: auto;">
			<table id="logLevelForm" class="formTable6" style="table-layout: fixed;">
				 <colgroup>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
				</colgroup>
				<tr>
	            	<th>
	            		<span class="fred">*</span>参数名：
	            	</th>
					<td>
						<input id="PRO_KEY" name="PRO_KEY" required="true" class="mini-textbox" maxlength="50" style="width:100%;" />
					</td>
	            	<th>
	            		<span class="fred">*</span>参数值：
	            	</th>
					<td>
						<input id="PRO_VALUE" name="PRO_VALUE" required="true" class="mini-textbox" maxlength="50" style="width:100%;" />
					</td>
				</tr>
				<tr>
	            	<th>
	            		备注：
	            	</th>
					<td colspan="3">
						<input id="PRO_DESC" name="PRO_DESC" class="mini-textarea" style="width:100%;" />
					</td>
				</tr>
			</table>
		</div>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
