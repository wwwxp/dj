<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>zookeeper管理</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/zTreeStyle/zTreeStyle.css"/>
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/zkmanager/zkManagerMain.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/zkmanager/zkTree.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/assets/js/jquery.ztree.all-3.5.min.js"></script>
	
</head>
<body>
	<div class="mini-fit p5">
		<div id="zkHeadDiv" style="height:28px;">
			<span style="float:left;font-size:14px;line-height:28px;font-weight:bold;margin-left:5px;">ZOOKEEPER集群 </span>
			<span style="float:right;margin-right:5px;"><a class="mini-button mini-button-green" onclick="back()" plain="false">返回</a> </span>
			<span style="clear: both;"></span>
		</div>
		<div class="mini-panel" title='<span class="clusterName">zookeeper</span>集群' style="padding:5px;width:100%" bodyStyle="padding:5px;" showCollapseButton="true" >
			<div id="zkInfoGrid" class="mini-datagrid" onselectionchanged="onSelectionChanged" selectOnLoad="true"
	             idField="id" allowResize="false" allowCellselect="false" showFooter="false" style="width: 100%;" >
				<div property="columns">
					<div field="name" width="15" headerAlign="center" align="center">集群名称</div>
					<div field="zkRoot" width="15" headerAlign="center" align="center">根目录</div>
					<div field="hostList" width="60" headerAlign="center" align="center" renderer="zkHostRenderer">主机</div>
					<div field="port" width="15" headerAlign="center" align="center">端口</div>
				</div>
			</div>
		</div>
		<div class="mini-fit" style="overflow:auto;margin:0px 5px 0px 5px;margin-bottom:0px;">
			<div class="mini-panel" title='zoookeeper树' style="width:100%;height:100%;" bodyStyle="padding:0px;" showCollapseButton="true" >
				<div class="mini-splitter"  style="height: 100%;width:100%;" borderStyle="border:0;">
				    <div size="300" showCollapseButton="true" style="border: 1;" minSize="300px">
				        <div class="mini-fit">
				            <ul id="zkTree" class="ztree" style="overflow:auto;height:100%;width:100%;"></ul>
				        </div>
				    </div>
					<div>
					    <div class="mini-fit">
					    	<textarea id="content" disabled="disabled" style="overflow:auto;border:none;height:100%;width:100%;resize:none;"></textarea>
					   	</div>
				   </div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
