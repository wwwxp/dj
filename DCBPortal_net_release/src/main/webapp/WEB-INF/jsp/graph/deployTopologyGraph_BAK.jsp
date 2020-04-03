<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
	<title>部署拓扑图</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/jtopo/style.css"/>
    <script type="text/javascript" src="${ctx}/js/common/jtopo/jquery.js"></script>
	<script type="text/javascript" src="${ctx}/js/common/jtopo/jtopo-min.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/graph/deployTopologyGraph.js"></script>
</head>
<body style="overflow-y: hidden;">
	<div id="jtopoDiv" style="border:0px solid red;margin: 5px;">
		<canvas width="1300px" height="497px" id="canvas" ></canvas>				
	</div>
</body>
</html>
