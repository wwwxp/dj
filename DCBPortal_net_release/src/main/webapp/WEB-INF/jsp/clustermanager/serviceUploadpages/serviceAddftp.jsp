<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/ajaxfileupload.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/serviceAddftp.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="uploadForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				 <col width="110"/>
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
					<input id="FILE_NAME" name="FILE_NAME" required="true" class="mini-textbox" style="width:250px;" />
                    <font color="red">(提示:文件类型必须为tar.gz、zip文件)</font>
                </td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>版本号：
            	</th>
				<td >
					<input id="VERSION" name="VERSION" required="true" class="mini-textbox" style="width:250px;" enabled="false"/>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>业务包类型：
            	</th>
				<td >
				    <input id="PACKAGE_TYPE" name="PACKAGE_TYPE" class="mini-combobox"   required="true"
								allowInput="false" showNullItem="false" style="width:250px;"
								valueField="EXTENDS_FIELD" textField="CONFIG_NAME" />
				    
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>所属集群：
            	</th>
				<td >
				    <input id="BUS_CLUSTER_ID" name="BUS_CLUSTER_ID" class="mini-combobox" 
								allowInput="false" showNullItem="false" style="width:250px;"
								valueField="BUS_CLUSTER_ID" textField="BUS_CLUSTER_NAME" 
								 multiSelect="true"  required="true"/>
				    
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>版本负责人：
            	</th>
				<td >
					<input id="PRINCIPAL" name="PRINCIPAL" required="true" class="mini-textbox" style="width:250px;" enabled="true"/>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>升级相关模块：
            	</th>
				<td >
					<input id="MODEL" name="MODEL" required="true" class="mini-textbox" style="width:99.99%;" enabled="true"/>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>本次升级的内容：
            	</th>
				<td >
					<input id="CONTENT" name="CONTENT" required="true" class="mini-textarea" style="width:99.99%;height:100px;" enabled="true"/>
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
