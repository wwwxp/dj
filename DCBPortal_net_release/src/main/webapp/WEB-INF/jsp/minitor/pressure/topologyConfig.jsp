<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Nimbus Conf</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/topologyConfig.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="true" allowCellWrap="true"  showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" headerAlign="center" align="right" width="10" >序号 </div>
				<div field="key" width="90" headerAlign="center" align="left">Key</div>
				<div field="value" width="140" headerAlign="center" align="left" renderer="formateValue">Value</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
