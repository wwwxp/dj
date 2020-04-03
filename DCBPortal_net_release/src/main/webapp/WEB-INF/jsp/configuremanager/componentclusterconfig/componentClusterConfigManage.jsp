<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>组件集群配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/componentclusterconfig/componentClusterConfigManage.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>集群名称：</span>
	            	</th>
					<td>
						<input id="CLUSTER_NAME" name="CLUSTER_NAME" class="mini-textbox" style="width:80%;" />
					</td>
					<th>
	            		<span>集群编码：</span>
	            	</th>
					<td>
						<input id="CLUSTER_CODE" name="CLUSTER_CODE" class="mini-textbox" style="width:80%;" />
					</td>
					<th>
	            		<span>集群类型：</span>
	            	</th>
					<td>
						<input id="CLUSTER_TYPE" name="CLUSTER_TYPE" valueField="CLUSTER_TYPE"
							showNullItem="true" allowInput="false"
							textField="CLUSTER_TYPE" class="mini-combobox" style="width:80%;" />
					</td>
					<td>
	            		<a class="mini-button" onclick="search()" style="margin-right: 20px;">查询</a>
	            	</td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
		        	<td style="text-align: right;">
		               	<a class="mini-button mini-button-green" onclick="add()"　width="100" plain="false">新增</a>
                    	<a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a> 
		            </td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="serviceTypeGrid" class="mini-datagrid" style="width: 100%; height: 100%"
	             idField="CLUSTER_ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="20" ></div>
	                <div field="CLUSTER_NAME" width="120" headerAlign="center" align="center">集群名称</div>
					<div field="CLUSTER_CODE" width="80" headerAlign="center" align="center" >集群编码</div>
					<div field="CLUSTER_TYPE" width="60" headerAlign="center" align="center">集群类型</div>
	                <div field="CLUSTER_DEPLOY_PATH" width="180" headerAlign="center" align="left" renderer="rendDeployPath">真实部署目录</div>
	                <div field="BUS_CLUSTER_LIST" width="140" headerAlign="center" align="center" renderer="onRenderBusCluster">所属业务集群</div>
	                <div field="CRT_DATE" width="80" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">创建时间</div>
					<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>