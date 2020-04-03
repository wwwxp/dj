<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>新增&修改业务主集群</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/busmainclusterconfig/addEditBusMainClusterCfg.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="busMainClusterForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
				 <col width="100"/>
				 <col/>
				<%--<col width="100"/>
				<col/>--%>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>集群名称：
            	</th>
				<td>
					<input id="BUS_CLUSTER_NAME" name="BUS_CLUSTER_NAME" required="true" maxlength="18" class="mini-textbox" style="width:95%;" />
				</td>
            	<th>
            		<span class="fred">*</span>集群编码：
            	</th>
				<td>
					<input id="BUS_CLUSTER_CODE" name="BUS_CLUSTER_CODE" required="true" maxlength="18" class="mini-textbox" style="width:95%;" />
				</td>
				<th>
					序号：
				</th>
				<td>
					<input id="BUS_CLUSTER_SEQ" name="BUS_CLUSTER_SEQ" changeOnMousewheel="false"
						   class="mini-spinner"  minValue="0" maxValue="1000" value="0" style="width:95%;"/>
				</td>
				<%--<th>
            		<span class="fred">*</span>业务集群类型：
            	</th>
				<td>
					<input class="mini-combobox" id="BUS_CLUSTER_TYPE" style="width:95%;"
                              data="getSysDictData('BUS_CLUSTER_TYPE_LIST')" valueField="code"
                              name="BUS_CLUSTER_TYPE"/>
				</td>--%>
			</tr>
		</table>
		
		<div class="mini-fit" style="padding-top: 5px;">
			<div style="width: 70%;height: 100%;float: left;">
				<div class="mini-fit" style="padding-right: 5px;">
					<div class="mini-panel" title="业务程序" style="width: 100%;height: 100%;">
						<div id="busClusterGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				             idField="CLUSTER_ELE_ID" allowResize="false" multiSelect="true" showFooter="false"
							 allowCellEdit="true" allowCellSelect="true" multiSelect="true"
							 editNextOnEnterKey="true"  editNextRowCell="true">
							<div property="columns">
								<div type="checkcolumn" width="15" ></div>
								<div field="CLUSTER_TYPE" width="50" headerAlign="center" align="center">程序类型
								</div>
				                <div field="CLUSTER_NAME" width="70" headerAlign="center" align="center">程序名称
									<input property="editor" class="mini-textbox" style="width:100%;" minWidth="100" />
								</div>
<!-- 								<div field="CLUSTER_ELE_PERSONAL_CONF" width="40" headerAlign="center" align="center" renderer="renderDcfClusterElePersonalConf">主机IP拆分 -->
<!-- 								</div> -->
								<div field="CLUSTER_ELE_DEFAULT_PATH" width="120" headerAlign="center" align="left" renderer="renderPath">部署根目录
									<input property="editor" class="mini-textbox" style="width:100%;" minWidth="100" />
								</div>
								<div field="CLUSTER_ELE_REAL_PATH" width="120" headerAlign="center" align="left" renderer="renderRealPath">真实部署目录
									<input property="editor" class="mini-textbox" enabled="false" style="width:100%;" minWidth="100" />
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div style="width: 30%;height: 100%;float: left;">
				<div class="mini-fit">
					<div class="mini-panel" title="组件集群" style="width: 100%;height: 100%;">
						<div id="comClusterGrid" class="mini-datagrid" style="width: 100%; height: 100%"
				             idField="CLUSTER_ID" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="false" >
							<div property="columns">
								<div type="checkcolumn" width="20" ></div>
								<div field="CLUSTER_NAME" width="100" headerAlign="center" align="center" renderer="renderClusterName">集群名称（编码）</div>
								<!-- <div field="CLUSTER_CODE" width="60" headerAlign="center" align="center">集群编码</div> -->
								<div field="CLUSTER_TYPE" width="60" headerAlign="center" align="center">集群类型</div>
								<!-- <div field="CLUSTER_DEPLOY_PATH" width="120" headerAlign="center" align="left">集群部署目录</div> -->
							</div>
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
