<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>扩容配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/expendCfg/expendCfg.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:60%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col />
					<col width="100"/>
					<col />
					<col width="100"/>
				</colgroup>
				<tr>
					<th>
						<span>集群名称：</span>
					</th>
					<td>
						<input id="QRY_CLUSTER_NAME" name="QRY_CLUSTER_NAME" class="mini-combobox" style="width: 100%;" showNullItem="true"
							   textField="CLUSTER_NAME" valueField="CLUSTER_ID" allowInput="false"/>
					</td>
					<th>
						<span>扩缩方式：</span>
					</th>
					<td>
						<input id="QRY_EXPEND_TYPE" name="QRY_EXPEND_TYPE" class="mini-combobox" style="width: 100%;"
							   data="[{'TEXT':'立即扩容','ID':'1'},{'TEXT':'定时扩容','ID':'2'},]"
							   showNullItem="true" textField="TEXT" valueField="ID" allowInput="false" onvaluechanged="changeType()"/>
					</td>
					<td><a class="mini-button" onclick="query()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>

		<div class="search2" style="border: 0px;margin-top: 5px;">
			<div id="expendForm" class="search">
				<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
					<colgroup>
						<col width="100"/>
						<col />
						<col width="100"/>
						<col />
						<col width="100"/>
						<col/>
						<col width="100"/>
						<col/>
						<col width="100"/>
						<col/>
					</colgroup>
					<tr>
						<th>
							<span>扩充触发源：</span>
						</th>
						<td>
							<input id="EXPEND_SOURCE" name="EXPEND_SOURCE" class="mini-textbox" maxlength="64" style="width: 100%;"/>
						</td>
						<th>
							<span>集群名称：</span>
						</th>
						<td>
							<input id="CLUSTER_NAME" name="CLUSTER_NAME" class="mini-combobox" style="width: 100%;" showNullItem="true"
								   textField="CLUSTER_NAME" valueField="CLUSTER_ID" allowInput="false" required="true"/>
						</td>
						<th>
							<span>节点数：</span>
						</th>
						<td>
							<input id="EXPEND_NUM" name="EXPEND_NUM" class="mini-textbox" vtype="int" required="true" style="width: 100%;"/>
						</td>
						<th>
							<span>扩缩方式：</span>
						</th>
						<td>
							<input id="EXPEND_TYPE" name="EXPEND_TYPE" class="mini-combobox" style="width: 100%;"
								   data="[{'TEXT':'立即扩容','ID':'1'},{'TEXT':'定时扩容','ID':'2'},]" value="1" required="true"
								   showNullItem="true" textField="TEXT" valueField="ID" allowInput="false" onvaluechanged="changeType()"/>
						</td>
						<th id="titleTr" style="display: none">
							<span>扩缩时间：</span>
						</th>
						<td id="titleRet" style="display: none;">
							<input id="EXPEND_TIME" name="EXPEND_TIME" class="mini-datepicker" style="width: 100%;" showTime="true"
								   allowInput="false" format="yyyy-MM-dd HH:mm:ss"/></td>
						</td>
					</tr>
					<tr>
						<td colspan="10" style="text-align: right;margin-right: 20px;">
							<a class="mini-button mini-button-green" onclick="addExpend()" style="margin-left: 20px;">新增</a>
							<a class="mini-button mini-button-green" onclick="delExpend()" style="margin-left: 20px;">删除</a>
						</td>
					</tr>
				</table>
			</div>
		</div>

		<div class="mini-fit" style="margin-top: 0px;margin-top: 5px;">
			<div id="expendGrid" class="mini-datagrid" style="width: 100%; height: 100%" pageSize="10"
				 idField="ID" allowResize="false" allowCellselect="false" multiSelect="false" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="20" ></div>
					<div field="EXPEND_SOURCE" width="120" headerAlign="center" align="center">扩充触发源</div>
					<div field="CLUSTER_NAME" width="100" headerAlign="center" align="center" renderer="onRenderTopName">集群名称</div>
					<div field="EXPEND_NUM" width="60" headerAlign="center" align="center">节点数</div>
					<div field="EXPEND_TYPE" width="60" headerAlign="center" renderer="onTypeRenderer" align="center">扩缩方式</div>
					<div field="EXPEND_TIME" dateFormat="yyyy-MM-dd HH:mm:ss" width="80" headerAlign="center" align="center">扩缩时间</div>
					<div field="EXPEND_STATE" width="40" headerAlign="center" renderer="onStateRenderer" align="center">状态</div>
					<div field="EXPEND_FINAL_TIME" dateFormat="yyyy-MM-dd HH:mm:ss" width="80" headerAlign="center" align="center">完成时间</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>