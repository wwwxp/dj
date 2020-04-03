<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务部署图实例列表</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/deploy/businessDeployList.js"></script>
</head>
<body>
	<div class="mini-fit p5" style="overflow:auto;">
		<div id="busDiv" class="mini-fit" style="margin-top: 5px;">
			<div id="busInstGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				 idField="ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
				<div property="columns">
					<div field="PROGRAM_NAME" width="100" headerAlign="center" align="center">程序名称</div>
					<div field="PROGRAM_CODE" width="100" headerAlign="center" align="center">程序编码</div>
					<div field="HOST_INFO" width="140" headerAlign="center" align="center">运行主机</div>
					<div field="VERSION" width="60" headerAlign="center" align="center">运行版本</div>
					<div field="CRT_DATE" width="120" headerAlign="center" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">操作时间</div>
				</div>
			</div>
		</div>
		<div id="compDiv" class="mini-fit" style="margin-top: 5px;">
			<div id="compInstGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				 idField="INST_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
				<div property="columns">
					<div field="CLUSTER_TYPE" width="60" headerAlign="center" align="center">组件名称</div>
					<div field="DEPLOY_FILE_TYPE" width="60" headerAlign="center" align="center">部署类型</div>
					<div field="HOST_INFO" width="100" headerAlign="center" align="center">运行主机</div>
					<div field="VERSION" width="60" headerAlign="center" align="center">运行版本</div>
					<div field="MODIFY_TIME" width="100" headerAlign="center" dataType="string" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">操作时间</div>
				</div>
			</div>
		</div>

		<div id="hostDiv" class="mini-fit" style="margin-top: 5px;">
			<div id="hostGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				 idField="HOST_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
				<div property="columns">
					<div field="HOST_NAME" width="80" headerAlign="center" align="center">主机名称</div>
					<div field="HOST_INFO" width="120" headerAlign="center" align="center">主机信息</div>
					<div field="CRT_DATE" width="100" headerAlign="center" dataType="string" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">操作时间</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>