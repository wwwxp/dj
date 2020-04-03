<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/ajaxfileupload.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/serviceFileView.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="uploadForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		文件名：
            	</th>
				<td >
					<input id="FILE_NAME" name="FILE_NAME" required="true" class="mini-textbox" style="width:100%;" enabled="false"/>
                </td>
				<th>
            		版本号：
            	</th>
				<td >
					<input id="VERSION" name="VERSION" required="true" class="mini-textbox" style="width:100%;" enabled="false"/>
				</td>
			</tr>
			<tr>
            	<th>
            		文件路径：
            	</th>
				<td >
					<input id="FILE_PATH" name="FILE_PATH" required="true" class="mini-textbox" style="width:100%;" enabled="false"/>
                </td>
				<th>
            		上传时间：
            	</th>
				<td >
					<input id="CRT_DATE" name="CRT_DATE" required="true" class="mini-datepicker" 
						format="yyyy-MM-dd HH:mm:ss" style="width:100%;" enabled="false"/>
				</td>
			</tr>
			<tr>
			</tr>
			<tr>
				<th>
            		描述：
            	</th>
				<td colspan="3">
					<input id="DESCRIPTION" name="DESCRIPTION" required="false" class="mini-textarea" 
						style="height:150px;width: 100%;" enabled="false"/>
				</td>
			</tr>
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="closeWindow()" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
