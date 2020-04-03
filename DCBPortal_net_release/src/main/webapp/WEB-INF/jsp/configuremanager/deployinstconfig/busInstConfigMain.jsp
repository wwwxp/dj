<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务实例状态管理</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script type="text/javascript" src="${ctx}/js/clustermanager/context.js"></script>
	<script type="text/javascript" src="${ctx}/js/configuremanager/addCommon.js"></script>
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/deployinstconfig/busInstConfigMain.js"></script>
	<style type="text/css">
	.tree-node-cluster-type{
		background:url(${ctx}/images/instIcon/clusterType.png) no-repeat;
	}
	.tree-node-cluster{
		background:url(${ctx}/images/instIcon/cluster.png) no-repeat;
	}		
	</style>
</head>
<body>
	<div class="mini-fit p5" style="overflow:auto;">
		<div class="mini-splitter"  style="width: 100%; height: 100%;" borderStyle="border:0px;">
			<div size="240" showCollapseButton="true" style="border:#b1c3e0 1px solid;" minSize="240">
		        <div class="mini-fit">
		            <ul id="fileTree" class="mini-tree" onDrawNode="nodeRender"
		                style="width: 100%; height: 99%;" showTreeIcon="true" textField="NODE_TEXT"
		                showTreeIcon="true" idField="NODE_ID" parentField="PARENT_NODE_ID" resultAsTree="false" 
		                expandOnLoad="0" onNodeclick="onClickTreeNode">
		            </ul>
		        </div>
		    </div>
		    <div style="border-left: none;">
				<div id="queryForm" class="search">
					<table class="formTable8" style="width:60%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
						<colgroup>
							<%--<col width="80px"/>--%>
							<%--<col />--%>
							<col width="80px"/>
							<col width="220px"/>
							<col />
						</colgroup>
						<tr>
			            	<%--<th>--%>
			            		<%--<span>程序类型：</span>--%>
			            	<%--</th>--%>
							<%--<td>--%>
								<%--<input id="PROGRAM_TYPE" name="PROGRAM_TYPE" class="mini-combobox" style="width: 100%;"--%>
									<%--textField="CLUSTER_TYPE" valueField="CLUSTER_TYPE" showNullItem="true" allowInput="false"/>--%>
							<%--</td>--%>
							<th>
			            		<span>程序名称：</span>
			            	</th>
							<td>
								<input id="PROGRAM_NAME" name="PROGRAM_NAME" class="mini-textbox" style="width: 200px;"/>
							</td>
							<td>
			            		<a class="mini-button" onclick="search()" >查询</a>
								<a class="mini-button" onclick="batchCheckStatus()" style="margin-left: 20px;">批量检查</a>
			            	</td>
						</tr>
					</table>
				</div>
				<div class="mini-fit" style="margin-top: 5px;">
					<div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="100" onload="loadStopData"
			             idField="ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
						<div property="columns">
							<div type="checkcolumn" width="20"></div>
							<div field="PROGRAM_TYPE" width="80" headerAlign="center" align="center">程序类型</div>
							<div field="PROGRAM_NAME" width="100" headerAlign="center" align="center">程序名称</div>
							<%--<div field="SCRIPT_SH_NAME" width="120" headerAlign="center" align="center">脚本</div>--%>
			                <div field="HOST_INFO" width="100" headerAlign="center" align="center">主机</div>
							<div field="PROGRAM_ALIAS" width="60" headerAlign="center" align="center">程序别名</div>
							<div field="VERSION" width="60" headerAlign="center" align="center">版本</div>
							<div field="RUN_STATE" width="60" headerAlign="center" align="center" renderer="statusRenderer">运行状态</div>
							<div field="CRT_DATE" width="80" dateFormat="yyyy-MM-dd HH:mm:ss" headerAlign="center" align="center">创建时间</div>
							<div name="action" width="120" headerAlign="center" align="center" renderer="onActionRenderer">操作</div>
						</div>
					</div>
				</div>
			</div>
		</div>
    </div>
    
    <!-- 页面标识：用于判断是菜单还是弹框（弹框值为1）；
    		如若不加此标识，将出现datagrid重复加载且数据混乱的情况； -->
    <input type="hidden" value="${param.dialog}" id="dialogFlag"/>
</body>
</html>