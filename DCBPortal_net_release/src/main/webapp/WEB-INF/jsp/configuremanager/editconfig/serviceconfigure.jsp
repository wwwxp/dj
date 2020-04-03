<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>配置修改</title>
<%@ include file="/public/common/common.jsp"%>
<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/editconfig/serviceconfigure.js"></script>

     <!--   主题样式 start -->
     <!--默认  codemirror.css-->
    <link rel="stylesheet" type="text/css" href="${ctx}/js/common/scripts/codemirror/lib/codemirror.css" /> 
    <link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/theme/3024-day.css">
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/theme/3024-night.css">
	<!--   主题样式 end -->
	
	 <!-- 文件内容搜索样式 -->
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.css">
	<link rel="stylesheet" href="${ctx}/js/common/scripts/codemirror/addon/search/matchesonscrollbar.css">

    <link rel="stylesheet" type="text/css" href="${ctx}/js/common/scripts/codemirror/addon/hint/show-hint.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/lib/codemirror.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/show-hint.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/javascript-hint.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/javascript/javascript.js"></script>
   <!--     选中行高亮 -->
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/selection/active-line.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/edit/matchbrackets.js"></script>
    
  <!--     文件内容搜索 -->
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/search.js "></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/searchcursor.js"></script>
    <%-- <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.js"></script> --%>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/matchesonscrollbar.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/scroll/annotatescrollbar.js"></script>
    
</head>
<body>
<div class="mini-fit" style="overflow:auto;">
	<div class="mini-splitter"  style="width: 100%; height: 100%;" borderStyle="border:0;">
	    <div size="300" showCollapseButton="true" style="border: 1;" minSize="300">
	        <div class="mini-toolbar" style="padding: 2px; border-right: 0;height: 30px;">
	            <table style="width: 100%;height: 100%;">
	                <tr>
	                    <td>
	                        <input id="node_name" name="node_name" style="width: 120px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
	                        <a class="mini-button" style="width:50px;" plain="false" onclick="searchTree()">查找</a>
	                        <a class="mini-button" style="width:50px;" plain="false" onclick="refresh();">刷新</a>
	                    </td>
	                </tr>
	            </table>
	        </div>
	        <div class="mini-fit">
	            <ul id="fileTree" class="mini-tree"
	                style="width: 100%; height: 99%" textField="fileName"  contextMenu="#tree_left_menu"
	                showTreeIcon="true" idField="currId" parentField="parentId" resultAsTree="false" 
	                expandOnLoad="0" onNodeclick="onClickTreeNode" value="fileName">
	            </ul>
	           <ul id="tree_left_menu" class="mini-contextmenu" onbeforeopen="serviceOnBeforeOpen">
	                <li id="newFolder" iconCls="icon-node" onclick="serviceNewFile('folder')">新建rebalance</li>
	                <li id="newFile" iconCls="icon-node" onclick="serviceNewFile('file')">新建文件</li>
	            </ul>
	        </div>
	    </div>
	    <div>
		    <div class="mini-fit">
		    	<textarea id="content"></textarea>
		        <div  id="saveFile" style="text-align: center;margin-top:5px;display:none">
			      <a class="mini-button mini-button-green" onclick="saveFile" style="width:150px;text-algin:center;">保存</a>
			    </div>
		   </div>
	   </div>
	</div>
</div>
</body>
</html>