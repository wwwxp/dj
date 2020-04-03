<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>top管理</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/clustersummary/clusterMain.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">
        <!-- TAB页 -->
        <div id="cluster_tabs" class="mini-tabs" activeIndex="0" contextMenu="#tabsMenu"
             style="width: 100%; height: 100%; padding: 5px 0px 5px 5px;" plain="false" tabAlign="left" tabPosition="top">
            <div title="业务集群列表" id="1" url="${ctx}/jsp/monitormanager/clustersummary/clusterInfo"></div>
        </div>

    <ul id="tabsMenu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">
        <li onclick="closeTab">关闭当前</li>
        <li onclick="refReshPage">刷新当前</li>
    </ul>
</div>
</body>
</html>
