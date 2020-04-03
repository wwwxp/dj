<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>DCCP云计费平台--服务组查询任务积压详细</title>
    <%@ include file="/public/common/common.jsp"%>
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/servicequery/taskOverstockHostDetailed.js"></script>
</head>
<body>
<div class="mini-fit p5" style="padding-top: 0px;">

    <div id="hostDetailed" class="mini-datagrid" style="width: 100%; height: 100%;" pageSize="100000"
         idField="TASK_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true">
        <div property="columns">
            <!-- <div field="TOPOLOGY_NAME" width="100" name="TOPOLOGY_NAME" headerAlign="center" align="center">Topology名称</div> -->
            <div field="HOST_IP" width="100" headerAlign="center" align="center">主机</div>
            <div field="TASK_NAME" width="100" headerAlign="center" align="center">任务名称</div>
            <div field="TASK_ID" width="80" headerAlign="center" align="center">任务ID</div>
            <div field="C_PRO_ID" width="80" headerAlign="center" align="center">C进程号</div>
            <div field="PENDING_SIZE" width="80" headerAlign="center" align="center">pending大小</div>
            <div field="EXEC_QUENE_SIZE" width="100" headerAlign="center" align="center">执行队列大小</div>
            <div field="FILE_QUEUE_SIZE" width="100" headerAlign="center" align="center">fileQueue大小</div>
            <div field="MSG_COUNT" width="100" headerAlign="center" align="center" renderer="numberFormat">发送消息总量(向c进程)</div>
        </div>
    </div>
</div>
</body>
</html>
