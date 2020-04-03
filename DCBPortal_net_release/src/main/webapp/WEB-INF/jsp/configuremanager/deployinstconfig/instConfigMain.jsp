<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script type="text/javascript" src="${ctx}/js/common/dynamicMergeCells.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/deployinstconfig/instConfigMain.js"></script>

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
			<div size="240" showCollapseButton="true" style="border:#b1c3e0 1px solid;border-top: none;" minSize="240">
		        <div class="mini-toolbar" style="padding: 2px; border-right: 0px;border-left:0px; height: 30px;">
		            <table style="width: 100%;height: 100%;">
		                <tr>
		                    <td>
		                        <input id="queryNodeName" name="queryNodeName" style="width: 120px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
		                        <a class="mini-button" style="width:50px;" plain="false" onclick="searchTree()">查找</a>
		                        <a class="mini-button" style="width:50px;" plain="false" onclick="refresh();">刷新</a>
		                    </td>
		                </tr>
		            </table>
		        </div>
		        <div class="mini-fit">
		            <ul id="fileTree" class="mini-tree" onDrawNode="nodeRender"
		                style="width: 100%; height: 99%;" showTreeIcon="true" textField="NODE_TEXT"
		                showTreeIcon="true" idField="NODE_ID" parentField="PARENT_NODE_ID" resultAsTree="false" 
		                expandOnLoad="0" onNodeclick="onClickTreeNode">
		            </ul>
		        </div>
		    </div>
		    
			<div style="border-left: none;">
				<div id="queryFrom" class="search">
					<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
						<colgroup>
							<col width="80px"/>
							<col />
							<col width="80px"/>
							<col />
							<col width="80px"/>
							<col />
							<col width="220px"/>
						</colgroup>
						<tr>
			            	<th>
			            		<span>主机IP：</span>
			            	</th>
							<td>
								<input id="HOST_ID" name="HOST_ID" class="mini-combobox" style="width: 100%;"
									textField="HOST_TEXT" valueField="HOST_ID" showNullItem="true" allowInput="false"/>
							</td>
							<th>
								<span>版本：</span>
							</th>
							<td>
								<input id="VERSION" name="VERSION" class="mini-textbox" style="width: 100%;"/>
							</td>
							<th>
								<span>状态：</span>
							</th>
							<td>
								<input id="STATUS" name="STATUS" class="mini-combobox" style="width: 100%;"
									   data="getSysDictData('INST_STATUS')" showNullItem="true" textField="text" valueField="code" allowInput="false"/>
							</td>
			                <td>
								&nbsp;
							</td>
						</tr>
						<tr>
							<th>
								<span>集群名称：</span>
							</th>
							<td>
								<input id="CLUSTER_ID" name="CLUSTER_ID" class="mini-combobox" style="width: 100%;"
									   textField="CLUSTER_NAME" valueField="CLUSTER_ID" showNullItem="true" allowInput="false"/>
							</td>
							<th>
								<span>集群类型：</span>
							</th>
							<td>
								<input id="DEPLOY_TYPE" name="DEPLOY_TYPE" class="mini-combobox" style="width: 100%;"
									   textField="CLUSTER_TYPE" valueField="CLUSTER_TYPE" showNullItem="true" allowInput="false" onvaluechanged="changeDeployType()"/>
							</td>
							<th>
								<span>部署类型：</span>
							</th>
							<td>
								<input id="DEPLOY_FILE_TYPE" name="DEPLOY_FILE_TYPE" class="mini-combobox" style="width: 100%;"
									   textField="text" valueField="code" showNullItem="true" allowInput="false"/>
							</td>
							<td>
								<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
								<a class="mini-button" onclick="batchCheckStatus()" style="margin-left: 20px;background: #59BD5D;">批量检查</a>
							</td>
						</tr>
					</table>
				</div>
				<div class="mini-fit" style="margin-top: 5px;">
					<div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="100" onload="loadStopData"
			             idField="INST_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
						<div property="columns">
							<div type="checkcolumn" width="20"></div>
							<div field="DEPLOY_TYPE" width="80" headerAlign="center" align="center">组件名称</div>
							<div field="HOST_INFO" width="100" headerAlign="center" align="center">主机</div>
							<div field="DEPLOY_FILE_TYPE" width="60" headerAlign="center" align="center">部署类型</div>
							<div field="CLUSTER_NAME" width="80" headerAlign="center" align="center">集群名称</div>
			                <!-- <div field="INST_NAME" width="110" headerAlign="center" align="center">实例名</div> -->
			                <div field="INST_PATH" width="140" headerAlign="center" align="center">启动文件(实例)</div>
			                <!-- <div field="FILE_NAME" width="110" headerAlign="center" align="center">文件名</div> -->
							<div field="STATUS" width="60" headerAlign="center" align="center" renderer="statusRenderer">状态</div>
							<div field="VERSION" width="60" headerAlign="center" align="center">版本</div>
							<div field="MODIFY_TIME" width="100" headerAlign="center" dataType="string" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">操作时间</div>
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