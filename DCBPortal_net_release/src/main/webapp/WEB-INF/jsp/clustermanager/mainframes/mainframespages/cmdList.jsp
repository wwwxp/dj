<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/cmdList.js"></script>
</head>
<body>
	<div class="mini-fit p5">
		<table id="hostFrom" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>命令列表：
            	</th>
				<td>  <input id="CMD_DESC" name="CMD_DESC" required="true"
					class="mini-combobox" textField="CMD_NAME" valueField="CMD_CONTENT" 
							 style="width: 100%;" /></td>
            	 
			</tr>
			<tr>
            	<th>
            		命令内容：
            	</th>
				<td>
					<textarea class="mini-textarea"  
							id="cmdMsg" style="width: 100%; height: 100px" ></textarea>
				</td>
            	 
			</tr>
			 
		 
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
  
</body>
</html>