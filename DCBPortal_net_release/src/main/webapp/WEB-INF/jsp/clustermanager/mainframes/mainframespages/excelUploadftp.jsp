<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DCCP云计费平台</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <%@ include file="/public/common/common.jsp" %>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css"/>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/ajaxfileupload.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/excelUploadftp.js"></script>
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
            <td>
                <input id="uFile" name="uFile" required="true" type="file" style="width:70%;" accept=".xls,.xlsx"/>
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
