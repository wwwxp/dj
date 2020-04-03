<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/ajaxfileupload.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/addftp.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="uploadForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>选择文件：
            	</th>
				<td >
					<div>
						<input  type="radio" id="uploadType" name="uploadType" value="0" checked="checked" style="margin-right:10px;">本地文件</input>
						<input id="uFile" name="uFile" required="true" type="file" style="width:70%;" />
					</div>
					 <div>
						<input  type="radio" id="uploadType" name="uploadType" value="1" style="margin-right:10px;">远程文件</input> 
						<input id="remoteFile" name="remoteFile" enabled="false" class="mini-textbox" style="width:300px;" />
						<a class="mini-button" onclick="selectFile" style="width:80px;margin-right:10px;">选择文件</a> 
					</div>
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>文件名：
            	</th>
				<td >
					<input id="FILE_NAME" name="FILE_NAME" required="true" class="mini-textbox" style="width:205px;" />
                    <!-- <span class="fred" style="margin-left: 20px;">请上传zip包</span> -->
                    <font color="red">(提示:文件类型必须为zip文件)</font>
                </td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>版本号：
            	</th>
				<td >
					<input id="VERSION" name="VERSION" required="true" class="mini-textbox" style="width:205px;" enabled="false"/>
				</td>
			</tr>
			<tr>
				<th>
            		描述：
            	</th>
				<td >
					<input id="DESCRIPTION" name="DESCRIPTION" required="false" class="mini-textarea" style="width:614px; height:150px" enabled="true"/>
				</td>
			</tr>
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="fileUpload" style="width:60px;margin-right:20px;">上传</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
