<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@include file="/public/common/common.jsp"%>
    <script src="${ctx}/js/configuremanager/run/viewConf.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
    <title>配置文件</title>
</head>

<body>
<div class="mini-fit" style="padding: 5px;">
	
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" allowCellWrap="true"  showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" headerAlign="center" align="right" width="20" >序号 </div>
				<div field="key" width="250" headerAlign="center" align="left">配置项</div>
				<div field="value" width="100%" headerAlign="center" align="left">配置值</div>
			</div>
		</div>
	</div>
    </div>
</body>
</html>
