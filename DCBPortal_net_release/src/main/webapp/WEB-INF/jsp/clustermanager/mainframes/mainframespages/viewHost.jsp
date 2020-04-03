<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/public/common/common.jsp"%>
    <script src="${ctx}/js/clustermanager/viewHost.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>详细信息</title>
</head>

<body>
<div class="mini-fit p5" id="viewForm">
    <table  class="formTable6" style="table-layout: fixed;">
            <colgroup>
                <col width="100px">
                <col>
                <col width="100px">
                <col>
            </colgroup>
            <tr>
                <th>主机IP地址：</th>
                <td><div id="HOST_IP"/></td>
                <th>SSH登录端口：</th>
                <td><div id="SSH_PORT"/></td>
            </tr>
            <tr>
                <th>SSH用户：</th>
                <td><div id="SSH_USER"/></td>
                 <th>SSH用户密码：</th>
                <td>******</td>
            </tr>
              <tr>
                <th>CPU核心数(个)：</th>
                <td><div id="CORE_COUNT"/></td>
                 <th>内存大小(G)：</th>
                <td><div id="MEM_SIZE"/></td>
            </tr>
                <tr>
                <th>存储大小(G)：</th>
                <td><div id="STORE_SIZE"/></td>
                 <th>状态：</th>
                <td><div id="HOST_STATE_DESC"/></td>
            </tr>
             </tr>
                <tr>
				<th>创建时间：</th>
                <td><div id="CRT_DATE"/></td>
            </tr>
        </table>
</div>

<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
     <span
        style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="closeWindow()">关闭</a>
</div>
</body>
</html>
