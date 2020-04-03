<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务主集群配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/busmainclusterconfig/busMainClusterCfgManage.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="160px"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>群集名称：</span>
	            	</th>
					<td>
						<input id="BUS_CLUSTER_NAME" name="BUS_CLUSTER_NAME" class="mini-textbox" style="width:100%;" />
					</td>
					<th>
	            		<span>集群编码：</span>
	            	</th>
					<td>
						<input id="BUS_CLUSTER_CODE" name="BUS_CLUSTER_CODE" class="mini-textbox" style="width:100%;" />
					</td>
					<th>
	            		<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
	            	</th>
					<td></td>
	                <td></td>
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
			<div id="busClusterGrid" class="mini-datagrid" style="width: 100%; height: 100%"
	             idField="BUS_CLUSTER_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="20" ></div>
	                <div field="BUS_CLUSTER_NAME" width="80" headerAlign="center" align="center">业务主集群名称</div>
					<div field="BUS_CLUSTER_CODE" width="80" headerAlign="center" align="center">业务主集群编码</div>
					<!-- <div field="BUS_CLUSTER_STATE" width="60" headerAlign="center" align="center"
						 valueField="code" renderer="onStatusRender">业务主集群状态</div> -->
					<div field="BUS_CRT_TIME" width="80" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss">创建时间</div>
					<div field="BUS_CLUSTER_LIST" width="160" headerAlign="center" align="center" renderer="onRenderBusCluster">集群成员</div>
					<div name="action" width="60" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>