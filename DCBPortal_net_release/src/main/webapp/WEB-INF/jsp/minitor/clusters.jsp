<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>集群列表</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitor/clusters.js"></script>
	<script>
	var clusters=${clusters};
	</script>
</head>
<body>
	<div class="mini-fit p5">
		<div id="clusterGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="id" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" headerAlign="center" align="center"  width="5" >序号</div>
				<div field="name"  width="10" headerAlign="center" align="center" renderer="formatCluster">集群名称</div>
				<div field="supervisor_num" width="20"headerAlign="center" align="center">supervior数量</div>
				<div field=""  width="15" headerAlign="center" align="center" renderer="formatPort" >端口使用比例</div>
				<div field="topology_num" width="10"headerAlign="center" align="center">topology数量</div>
				<div field="version" width="15"headerAlign="center" align="center">版本</div>
			</div>
		</div>
	</div>
</body>
</html>
