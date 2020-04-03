<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>配置修改</title>
<%@ include file="/public/common/common.jsp"%>
<script language="javascript" cfg_type="text/javascript" src="${ctx}/js/nodemanager/startnode/configFile.js"></script>

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

	                    </td>
	                </tr>
	            </table>
	        </div>
	        <div class="mini-fit">
	            <ul id="fileTree" class="mini-tree"
	                style="width: 100%; height: 99%" textField="fileName"
	                showTreeIcon="true" idField="nodeId"
					onnodeclick="getFileContent"
	                expandOnLoad="0">
	            </ul>
	        </div>
	    </div>
	    <div style="border:#b1c3e0 1px solid;">
	    	<div style="height:30px;padding-left:38px; border-bottom:#b1c3e0 1px solid;">
		    	<div id="tips" style="padding-top: 5px;">
		    		
		    	</div>
		    </div>
		    <div class="mini-fit">
		    	<textarea id="fileContent"></textarea>
		   </div>
	   </div>
	</div>
</div>
</body>
</html>