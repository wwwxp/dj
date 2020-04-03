<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>入网测试-版本切换回退</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/masterstandbyNet/switchMasterStandby.js"></script>
</head>
<body>
	<!-- TAB页 -->
	<div id="switchTabs" class="mini-tabs" activeIndex="0" bodyStyle="padding:5px 0px 0px 0px;"
		style="width: 100%; height: 100%; " plain="false"
		tabAlign="left" tabPosition="top">
		<div title="版本升级" url="${ctx}/jsp/configuremanager/masterstandbyNet/versionupgrade" ></div>
	    <div title="版本回退" url="${ctx}/jsp/configuremanager/masterstandbyNet/versionrollback" ></div>
	</div>
</body>
</html>