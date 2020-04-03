<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>新增 &修改组件集群配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/componentclusterconfig/addEditComponentClusterConfig.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
 		<div style="width: 100%;height: auto;">
		<table id="serviceTypeForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>集群编码：
            	</th>
				<td>
					<input id="CLUSTER_CODE" name="CLUSTER_CODE" required="true" onvalidation="onEnglishAndNumberValidation" class="mini-textbox" maxlength="18" style="width:95%;" />
				</td>
            	<th>
            		<span class="fred">*</span>集群名称：
            	</th>
				<td>
					<input id="CLUSTER_NAME" name="CLUSTER_NAME" required="true" class="mini-textbox" maxlength="25" style="width:95%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>集群类型：
            	</th>
				<td>
					<input id="CLUSTER_TYPE" name="CLUSTER_TYPE" required="true" valueField="CLUSTER_TYPE"
						textField="CLUSTER_TYPE" onvaluechanged="changeClusterType" class="mini-combobox" style="width:95%;" />
				</td>
				<th>
            		<span class="fred">*</span>显示序列：
            	</th>
				<td>
					<input id="SEQ" name="SEQ" required="true" changeOnMousewheel="false" class="mini-spinner"  minValue="1" maxValue="99" maxlength="2" style="width:95%;" />
				</td>
			</tr>
			<tr id="m2dbTr" style="display: none;">
				<th>
            		<span class="fred">*</span>M2DB实例：
            	</th>
				<td colspan="3">
					<input id="M2DB_INSTANCE" name="M2DB_INSTANCE" maxlength="18" class="mini-textbox" style="width:55%;" />
					<span class="fred">（提示:可配置多个实例，实例之间以,分割，例如1,2）</span>
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>部署路径：
            	</th>
				<td colspan="3">
					<input id="DEPLOY_PATH" name="DEPLOY_PATH" required="true" onvaluechanged="changeDeployPath" maxlength="100" class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>真实部署路径：
            	</th>
				<td colspan="3">
					<input id="CLUSTER_DEPLOY_PATH" name="CLUSTER_DEPLOY_PATH" required="true" readonly="readonly" maxlength="100" class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
		</table>
		</div>
		
		<div class="mini-fit" style="margin-top: 5px;">
			<div class="mini-panel" style="width: 100%;height:100%;" bodyStyle="padding-top:0px;"
				showCollapseButton="true" expanded="true" collapseOnTitleClick="true" title="组件参数配置">
				<table id="updateForm" class="formTable9" style="width:100%;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
					<tr>
			        	<td style="text-align: right;">
			               	<a class="mini-button mini-button-green" onclick="defaultParmas()"　width="100" plain="false">默认</a>
	                    	<a class="mini-button mini-button-green" onclick="backParams()" plain="false">还原</a> 
			            </td>
					</tr>
				</table>
				<div class="mini-fit">
					<div id="configGrid" class="mini-datagrid" style="width: 100%; height: 100%;" ondrawcell="onRenderParamValue"
				          idField="ID"  showFooter="false" allowCellEdit="false" allowCellSelect="false" multiSelect="false" >
				        <div property="columns">
				            <div field="CFG_TYPE" width="60" headerAlign="center" align="center" allowSort="true">配置项类型</div>
				            <div field="CFG_NAME" width="80" headerAlign="center" align="right" allowSort="true">配置项名称</div>
				            <div field="CFG_VALUE" width="200" headerAlign="center" align="left">配置项值</div>
				        </div>
				   </div>
			   </div>
		   </div>
	   </div>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
