<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/public/common/common.jsp"%>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <script src="${ctx}/js/configuremanager/busmainclusterconfig/viewBusMainClusterCfg.js" type="text/javascript"></script>
    <title>业务主集群配置信息</title>
</head>

<body>
	<div class="mini-fit p5" id="viewForm">
    	<table class="formTable6" style="table-layout: fixed;">
         <colgroup>
             <col width="100px">
             <col>
             <col width="100px">
             <col>
         </colgroup>
         <tr>
             <th>程序名称：</th>
             <td><div id="HOST_IP"/></td>
             <th>程序编码：</th>
             <td><div id="SSH_PORT"/></td>
         </tr>
     </table>
	</div>
	<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
     	borderStyle="border:0;border-top:solid 1px #b1c3e0;">
     	<span style="display: inline-block; width: 25px;"></span> 
     	<a class="mini-button" onclick="closeWindow()">关闭</a>
	</div>
</body>
</html>
