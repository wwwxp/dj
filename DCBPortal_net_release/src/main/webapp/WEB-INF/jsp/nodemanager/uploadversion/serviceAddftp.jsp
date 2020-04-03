<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/ajaxfileupload.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/nodemanager/uploadversion/serviceAddftp.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="uploadForm" class="formTable6" style="table-layout: fixed; height: 50%">
			 <colgroup>
				 <col width="130"/>
				 <col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>选择文件：
            	</th>
				<td >
					<div>
						<input  type="radio" id="uploadType" name="uploadType" value="0" checked="checked" style="margin-right:10px;">本地文件</input>
						<input id="uFile" name="uFile" required="true" type="file" style="width:70%;" onclick="selectLocalFile()"/>
					</div>
                    <div>
                        <input  type="radio" id="uploadType2" name="uploadType" value="1" style="margin-right:10px;">远程文件</input>
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
					<input id="FILE_NAME" name="FILE_NAME" required="true" class="mini-textbox" style="width:350px;" />
                    <span class="fred">(提示:文件类型必须为tar.gz、zip、war文件)</span>
                </td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>业务包类型：
            	</th>
				<td >
				    <input id="PACKAGE_TYPE" name="PACKAGE_TYPE" class="mini-combobox"   required="true"
								allowInput="false" showNullItem="false" style="width:350px;"
								valueField="ID" textField="VERSION_INFO" />

				</td>
			</tr>

            <tr>
                <th>
                    <span class="fred">*</span>包类型(增量/全量)：
                </th>
                <td >
                    <input id="FULL_TYPE" name="FULL_TYPE" class="mini-combobox"   required="true"
                           allowInput="false" showNullItem="false" style="width:350px;" data="getSysDictData('node_version_pkg_full')"
                           valueField="code" textField="text" value="1" />
                    <span class="fred">(提示:目前增量只支持web程序)</span>
                </td>
            </tr>

			<tr>
				<th>
            		<span class="fred">*</span>本次升级的内容：
            	</th>
				<td >
					<input id="DESC" name="DESC" required="true" class="mini-textarea" style="width:99.99%;height:100px;" enabled="true"/>
				</td>
			</tr>
		</table>
        <span class="fred">(提示:web包名前缀需保持一致,如app.war包,则增量包必须名字为 app.zip或者app.tar.gz 且增量包里面必须根目录为/app)</span>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="fileUpload" style="width:60px;margin-right:20px;">上传</a>
        <span style="display: inline-block; width: 25px;"></span>
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
