<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>配置信息</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/clustersummary/topConfigInfo.js"></script>
</head>
<body>
	<div style="padding:5px;height:28px;">
		<span style="float:left;font-size:14px;line-height:28px;font-weight:bold;">Topology配置 </span>
		<span style="float:right;"><a class="mini-button mini-button-green" onclick="back()" plain="false">返回</a> </span>
	</div>
	<div class="mini-fit p5">
		<div id="configGrid" class="mini-datagrid" style="width: 100%;height:100%;word-wrap:break-word;word-break:break-all;" 
		 idField="id" allowResize="false" allowCellselect="false"  allowCellWrap="true" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="5" headerAlign="center" align="center">序号</div>
				<div field="key" width="45" headerAlign="center" align="center">属性名</div>
				<div field="value" width="45" headerAlign="center" align="center">属性值</div>
			</div>
		</div>
	</div>
</body>
</html>
