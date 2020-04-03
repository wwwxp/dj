<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
    <script
            src="${ctx}/js/clustermanager/businessassigned/businessDispatchCluster.js"
            type="text/javascript"></script>
    <title>用户集群指派</title>
</head>
<body>

<div class="mini-fit">
    <div class="mini-panel" title="集群列表" style="width: 100%;height: 100%;">
        <div id="BUS_CLUSTER_ID" class="mini-checkboxlist" repeatItems="2" repeatLayout="table"
             textField="BUS_CLUSTER_NAME" valueField="BUS_CLUSTER_ID" value="" onvaluechanged="checkboxChanged"
             url="${ctx}/core/queryForList?execKey=userColonyMapper.queryColonyConfigList" >
        </div>
    </div>
</div>
<div class="mini-toolbar"
     style="height: 28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border-left:0;border-bottom:0;border-right:0;">
    <a class="mini-button" style="width: 60px;" onclick="onSubmit()">确定</a>
    <span style="display: inline-block; width: 25px;"></span> <a
        class="mini-button" style="width: 60px;" onclick="closeWindow()">取消</a>
</div>
</body>
</html>