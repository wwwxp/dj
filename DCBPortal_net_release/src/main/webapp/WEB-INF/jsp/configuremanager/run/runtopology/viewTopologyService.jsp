<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/public/common/common.jsp"%>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/zTreeStyle/zTreeStyle.css"/>
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/zkmanager/zkTree.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/assets/js/jquery.ztree.all-3.5.min.js"></script>
    <script src="${ctx}/js/configuremanager/run/runtopology/viewTopologyService.js" type="text/javascript"></script>
    <title>Topology配置文件查看</title>
</head>

<body>
	<div class="mini-toolbar" style="padding: 2px; border-right: 0px;border-left:0px; height: 35px;">
        <table style="width: 100%;text-align: right;margin-right:5px; ">
            <tr>
                <td>
                    <a class="mini-button" id="btnRefresh" style="width:50px;" plain="false" onclick="refresh()">刷新</a>
                </td>
            </tr>
        </table>
    </div>
	<div class="mini-fit">
		<ul id="zkTree" class="ztree" style="overflow:auto;height:100%;width:100%;"></ul>
	</div>
</body>
</html>
