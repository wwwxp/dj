<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/dynamicNodeexpend/nodeStrategyConfig.js"></script>
	<style type="text/css">
	.tree-node-cluster{
		background:url(${ctx}/images/instIcon/clusterType.png) no-repeat;
	}
	.tree-node-version{
		background:url(${ctx}/images/instIcon/cluster.png) no-repeat;
	}		
	.tree-node-program{
		background:url(${ctx}/images/instIcon/program.png) no-repeat;
	}
	</style>
</head>
<body>
	<div class="mini-fit p5" style="overflow:auto;">
	<div id="cluster_tabs" class="mini-tabs" activeIndex="0" plain="false"
			tabAlign="left" tabPosition="top" onactivechanged="loadPage" style="height:40px;width:100%;">
		</div>
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
		            <ul id="strategyTree" class="mini-tree" onDrawNode="nodeRender"
		                style="width: 100%; height: 99%;" showTreeIcon="true" textField="NODE_NAME"
		                showTreeIcon="true" idField="NODE_ID" parentField="PARENT_NODE_ID" resultAsTree="false" 
		                expandOnLoad="1" onNodeclick="onClickTreeNode">
		            </ul>
		        </div>
		    </div>
		    
			<div style="border:#b1c3e0 1px solid;">
				<div id="nodeConfigTab" class="mini-tabs" activeIndex="0" style="width:100%;height:100%;margin-top: 5px;" plain="false">
				    <div title="动态伸缩阀值配置" class="mini-fit">
				    	<div class="mini-fit">
					    	<div style="height: 49%;padding-bottom:5px;">
			                    <div class="mini-panel" title="扩展阀值配置"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                        <div class="search2" style="border: 0px;padding:0px;text-align: right;">
							               	<a class="mini-button mini-button-green" onclick="addThresholdConfig('expend')" plain="false">新增</a>
					                    	<!-- <a class="mini-button mini-button-green" onclick="delThresholdConfig('expend')" plain="false">删除</a> -->
										</div>
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="expendConfigGrid" class="mini-datagrid" style="width: 100%; height: 100%"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="checkcolumn" width="20" ></div>
													<div field="QUOTA_TYPE" width="60" headerAlign="center" renderer="onQuotaTypeRenderer" align="center">指标类型</div> 
									                <div field="CONDITION_PARAM" width="100" headerAlign="center" renderer="onConditionParamsRenderer" align="center">条件类型</div> 
													<div field="CONDITION_VALUE" width="80" headerAlign="center" align="center" renderer="onUnitRenderer">取值</div>
													<!--<div field="CONDITION_COUNT" width="80" headerAlign="center" align="center">连续次数</div>
													 <div field="HOST_COUNT" width="80" headerAlign="center" align="center">一次扩展节点数</div> -->
													<div field="CRT_DATE" width="100" headerAlign="center" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">创建时间</div>
													<div name="action" width="60" headerAlign="center" align="center" renderer="onExpendActionRenderer">操作</div>
												</div>
											</div>
										</div>
			                        </div>
			                    </div>
			                </div>
			                <div style="height: 49%;">
			                    <div class="mini-panel" title="动态收缩阀值配置"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                        <div class="search2" style="border: 0px;padding:0px;text-align: right;">
							               	<a class="mini-button mini-button-green" onclick="addThresholdConfig('unexpend')" plain="false">新增</a>
					                    	<!-- <a class="mini-button mini-button-green" onclick="delThresholdConfig('unexpend')" plain="false">删除</a> -->
										</div>
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="unexpendConfigGrid" class="mini-datagrid" style="width: 100%; height: 100%"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="checkcolumn" width="20" ></div>
													<div field="QUOTA_TYPE" width="60" headerAlign="center" renderer="onQuotaTypeRenderer" align="center">指标类型</div> 
									                <div field="CONDITION_PARAM" width="100" headerAlign="center" renderer="onUnConditionParamsRenderer" align="center">条件类型</div> 
													<div field="CONDITION_VALUE" width="80" headerAlign="center" align="center" renderer="onUnitRenderer">取值</div>
													<!--<div field="CONDITION_COUNT" width="80" headerAlign="center" align="center">连续次数</div>
													<!-- <div field="HOST_COUNT" width="80" headerAlign="center" align="center">一次扩展节点数</div> -->
													<div field="CRT_DATE" width="100" headerAlign="center" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">创建时间</div>
													<div name="action" width="60" headerAlign="center" align="center" renderer="onUnexpendActionRenderer">操作</div>
												</div>
											</div>
										</div>
			                        </div>
			                    </div>
			                </div>
		                </div>
					</div>
				    <div title="伸缩预测报告" >
				       <div class="mini-fit">
					    	<div style="height: 49%;padding-bottom:5px;">
			                    <div class="mini-panel" title="预测扩展报告"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                        <!-- <div class="search2" style="border: 0px;padding:0px;text-align: right;">
							               	<a class="mini-button mini-button-green" onclick="addTimingConfig('expend')" plain="false">新增</a>
										</div> -->
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="expandGrid" class="mini-datagrid" style="width: 100%; height: 100%" showLoading="false"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="indexcolumn" width="10" ></div> 
													<div field="CPU" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">CPU</div>
													<div field="MEM" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">内存</div>
													<div field="DISK" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">磁盘</div>
													<div field="BUSS_VOLUME" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">业务量</div>
													<div field="PREDICTION_TIME" dateFormat="yyyy-MM-dd" width="30" headerAlign="center" align="center">预测时间</div>
													<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="50" headerAlign="center" align="center">生成时间</div>
													<div field="RESULT_DESC" width="100" headerAlign="center" align="left" renderer="onRenderReportDesc">描述</div>
													<div field="STATUS" width="30" headerAlign="center" align="center" renderer="statusRenderer">状态</div>
													<div name="action" width="40" headerAlign="center" align="center" renderer="onExpendRenderer">操作</div>
													
												</div>
											</div> 
										</div>
			                        </div>
			                    </div>
			                </div>
			                <div style="height: 49%;">
			                    <div class="mini-panel" title="预测收缩报告"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                       <!--  <div class="search2" style="border: 0px;padding:0px;text-align: right;">
							               	<a class="mini-button mini-button-green" onclick="addTimingConfig('unexpend')" plain="false">新增</a>
										</div> -->
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="unExpandGrid" class="mini-datagrid" style="width: 100%; height: 100%" showLoading="false"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="indexcolumn" width="12" ></div> 
													<div field="CPU" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">CPU</div>
													<div field="MEM" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">内存</div>
													<div field="DISK" width="25" headerAlign="center" align="center" renderer="onUnitfieldRenderer">磁盘</div>
													<div field="BUSS_VOLUME" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">业务量</div>
													<div field="PREDICTION_TIME" dateFormat="yyyy-MM-dd" width="30" headerAlign="center" align="center">预测时间</div>
													<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="50" headerAlign="center" align="center">生成时间</div>
													<div field="RESULT_DESC" width="100" headerAlign="center" align="left" renderer="onRenderReportDesc" >描述</div>
													<div field="STATUS" width="30" headerAlign="center" align="center" renderer="statusRenderer">状态</div>
													<div name="action" width="40" headerAlign="center" align="center" renderer="onUnExpendRenderer">操作</div>
												</div>
											</div>
										</div>
			                        </div>
			                    </div>
			                </div>
		                </div>
				    </div>
				
				   <div title="伸缩计划报告" >
				       <div class="mini-fit">
					    	<div style="height: 49%;padding-bottom:5px;">
			                    <div class="mini-panel" title="扩展计划报告"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                        
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="reportGrid" class="mini-datagrid" style="width: 100%; height: 100%" showLoading="false"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="indexcolumn" width="12" ></div> 
													<div field="CPU" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">CPU</div>
													<div field="MEM" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">内存</div>
													<div field="DISK" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">磁盘</div>
													<div field="BUSS_VOLUME" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">业务量</div>
													<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="60" headerAlign="center" align="center">生成时间</div>
													<div field="EXEC_STATUS" width="45" headerAlign="center" align="center" renderer="execstatusRenderer">执行状态</div>
													<div field="ACTION_TYPE" width="30" headerAlign="center" align="center" renderer="onActionfieldRenderer">执行类型</div>
													<div field="EXEC_TIME" dateFormat="yyyy-MM-dd HH:mm:ss" width="60" headerAlign="center" align="center">执行时间</div>
													<div name="action" width="80" headerAlign="center" align="center" renderer="onManualExpendRenderer">操作</div>
												</div>
											</div>
										</div>
			                        </div>
			                    </div>
			                </div>
			                <div style="height: 49%;">
			                    <div class="mini-panel" title="收缩计划报告"style="width: 100%;height: 100%;" showToolbar="false" 
			                         showCloseButton="false" showFooter="false" showCollapseButton="false">
			                        <div class="mini-fit">
				                       
										<div class="mini-fit" style="margin-top: 2px;">
											<div id="unReportGrid" class="mini-datagrid" style="width: 100%; height: 100%" showLoading="false"
									             idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
												<div property="columns">
													<div type="indexcolumn" width="12" ></div> 
													<div field="CPU" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">CPU</div>
													<div field="MEM" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">内存</div>
													<div field="DISK" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">磁盘</div>
													<div field="BUSS_VOLUME" width="40" headerAlign="center" align="center" renderer="onUnitfieldRenderer">业务量</div>
													<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="60" headerAlign="center" align="center">生成时间</div>
													<div field="EXEC_STATUS" width="45" headerAlign="center" align="center" renderer="execstatusRenderer">执行状态</div>
													<div field="ACTION_TYPE" width="30" headerAlign="center" align="center" renderer="onActionfieldRenderer">执行类型</div>
													<div field="EXEC_TIME" dateFormat="yyyy-MM-dd HH:mm:ss" width="60" headerAlign="center" align="center">执行时间</div>
													<div name="action" width="80" headerAlign="center" align="center" renderer="onUnManualExpendRenderer">操作</div>
												</div>
											</div>
										</div>
			                        </div>
			                    </div>
			                </div>
		                </div>
				    </div>
				
				
				</div>
			</div>
		</div>
    </div>
</body>
</html>