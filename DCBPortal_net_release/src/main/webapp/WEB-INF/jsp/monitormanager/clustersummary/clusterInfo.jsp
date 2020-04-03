<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>top管理</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/clustersummary/clusterInfo.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">

            <div id="ClusterRelationGrid" class="mini-datagrid"
                 style="width: 100%; height: 100%;" idField="BUS_CLUSTER_ID"
                 allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true">
                <div property="columns">
                    <div field="BUS_CLUSTER_ID" width="20" headerAlign="center" align="center" visible="false"></div>
                    <div field="CLUSTER_CODE" width="20" headerAlign="center" align="center" visible="false"></div>
                    <div field="BUS_CLUSTER_NAME" width="20" headerAlign="center" align="center" renderer="onRenderTopName">
                        业务集群名称
                    </div>
                    <div field="BUS_CLUSTER_CODE" width="20" headerAlign="center" align="center">业务集群编码</div>
                    <div field="BUS_CRT_TIME" width="20" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">创建时间</div>
                </div>
            </div>





</div>
</body>
</html>
