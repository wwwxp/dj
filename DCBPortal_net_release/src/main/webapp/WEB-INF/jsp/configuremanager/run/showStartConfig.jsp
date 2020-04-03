<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>重载配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/showStartConfig.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0;" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80px"/>
					<col width="260px"/>
					<col/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>批次名称：</span>
	            	</th>
					<td>
						<input id="BATCH_NAME" name="BATCH_NAME" class="mini-textbox" style="width: 100%;"/>
					</td>
	                <td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 5px;">
			<div id="configGrid" class="mini-treegrid" showTreeIcon="true" pageSize="100"
    			 treeColumn="BATCH_NAME" idField="UID" parentField="PARENT_UID" resultAsTree="false"
	             allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true"
    			 style="width: 100%; height: 100%">
				<div property="columns">
					<div name="BATCH_NAME" field="BATCH_NAME" width="90" headerAlign="center" align="center">批次名称</div>
					<div field="HOST_IP" width="100" headerAlign="center" align="center">主机IP</div>
					<div field="DEPLOY_FILE_TYPE" width="50" headerAlign="center" align="center">部署类型</div>
	                <div field="CONFIG_PATH" width="110" headerAlign="center" align="center">实例名</div>
	                <div field="VERSION" width="35" headerAlign="center" align="center">启动版本</div>
					<div field="UPDATE_DATE" width="60" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">创建时间</div>
					<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer">操作</div>
				</div>
			</div>
		</div>
		
    </div>
</body>
</html>