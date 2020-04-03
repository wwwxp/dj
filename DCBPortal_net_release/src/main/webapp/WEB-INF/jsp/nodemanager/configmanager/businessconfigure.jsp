<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>配置修改</title>
<%@ include file="/public/common/common.jsp"%>
<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/nodemanager/configmanager/businessconfigure.js"></script>

     <!--   主题样式 start -->
     <!--默认  codemirror.css-->
    <link rel="stylesheet" cfg_type="text/css" href="${ctx}/js/common/scripts/codemirror/lib/codemirror.css" />
    <link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/theme/3024-day.css">
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/theme/3024-night.css">
	<!--   主题样式 end -->
	
	 <!-- 文件内容搜索样式 -->
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.css">
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/addon/search/matchesonscrollbar.css">

    <link rel="stylesheet" cfg_type="text/css" href="${ctx}/js/common/scripts/codemirror/addon/hint/show-hint.css" />
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/lib/codemirror.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/show-hint.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/javascript-hint.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/javascript/javascript.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/xml-hint.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/xml/xml.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/css-hint.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/css/css.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/yaml/yaml.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/properties/properties.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/htmlmixed/htmlmixed.js"></script>
   <!--     选中行高亮 -->
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/selection/active-line.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/edit/matchbrackets.js"></script>
    
  <!--     文件内容搜索 -->
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/search.js "></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/searchcursor.js"></script>
    <%-- <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.js"></script> --%>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/matchesonscrollbar.js"></script>
    <script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/scroll/annotatescrollbar.js"></script>
	<!-- 折叠 -->
	<link rel="stylesheet" cfg_type="text/css" href="${ctx}/js/common/scripts/codemirror/addon/fold/foldgutter.css">
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/foldcode.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/foldgutter.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/brace-fold.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/xml-fold.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/markdown-fold.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/comment-fold.js"></script>
	<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/markdown/markdown.js"></script>
    
</head>
<body>
<div class="mini-fit" style="overflow:auto;">
	<div class="mini-splitter"  style="width: 100%; height: 100%;" borderStyle="border:0px;">
	    <div size="300" showCollapseButton="true" style="border:#b1c3e0 1px solid;border-top: none;" minSize="300">
	        <div class="mini-toolbar" style="padding: 2px; border-right: 0;border-left:0px;height: 30px;">
	            <table style="width: 100%;height: 100%;">
	                <tr>
	                    <td>
	                        <input id="node_name" name="node_name" style="width: 180px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
	                        <a class="mini-button" style="width:50px;" plain="false" onclick="searchTree()">查找</a>
	                        <a class="mini-button" style="width:50px;" plain="false" onclick="refresh();">刷新</a>
	                    </td>
	                </tr>
	            </table>
	        </div>
	        <div class="mini-fit">
	            <ul id="fileTree" class="mini-tree" ondrawnode="distinIsUsed" showCheckBox="true"
	                style="width: 100%; height: 99%" textField="fileName"  contextMenu="#tree_left_menu"
	                showTreeIcon="true" idField="currId" parentField="parentId" resultAsTree="false"
	                expandOnLoad="0" onNodeclick="onClickTreeNode">
	            </ul>
<%--	           <ul id="tree_left_menu" class="mini-contextmenu" onbeforeopen="serviceOnBeforeOpen">--%>
<%--	                <li id="addFile" iconCls="icon-node" onclick="serviceAddFile('file')">新增文件</li>--%>
<%--	                <li id="addFolder" iconCls="icon-node" onclick="serviceAddFile('folder')">新增文件夹</li>--%>
<%--	                <li id="batchAddFile" iconCls="icon-node" onclick="serviceAddFile('batch')">批量新增</li>--%>
<%--	                <li id="delFile" iconCls="icon-node" onclick="serviceDelFile('file')">删除文件</li>--%>
<%--	                <li id="delFolder" iconCls="icon-node" onclick="serviceDelFile('folder')">删除文件夹</li>--%>
<%--	                <li id="delBatchFolder" iconCls="icon-node" onclick="delBatchFile('batch')">批量删除文件</li>--%>
<%--	            </ul>--%>
	        </div>
	    </div>
	    <div style="border:#b1c3e0 1px solid;">
	    	<div style="height:30px;padding-left:38px; border-bottom:#b1c3e0 1px solid;">
		    	<div id="tips" style="padding-top: 5px;">
		    		
		    	</div>
		    </div>
		    <div class="mini-fit">
		    	<textarea id="content"></textarea>
		        <div id="saveFile" style="text-align: center;margin-top:0px;border-top:#b1c3e0 1px solid;">
			    	<a class="mini-button mini-button-green" onclick="saveFile" style="width:150px;text-algin:center;margin-top: 5px;">保存</a>
			    </div>
		   </div>
	   </div>
	</div>
</div>
</body>
</html>