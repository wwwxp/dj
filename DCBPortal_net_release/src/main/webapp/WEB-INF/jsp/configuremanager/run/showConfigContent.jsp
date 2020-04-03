<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>查看配置文件</title>
	<%@ include file="/public/common/common.jsp" %>
	<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<link rel="stylesheet" type="text/css" href="${ctx}/css/clustermanager/deployhome.css" />
	<script type="text/javascript" src="${ctx}/js/configuremanager/showConfigContent.js"></script>

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
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/xml-hint.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/xml/xml.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/hint/css-hint.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/css/css.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/yaml/yaml.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/properties/properties.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/htmlmixed/htmlmixed.js"></script>
   <!--     选中行高亮 -->
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/selection/active-line.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/edit/matchbrackets.js"></script>
    
  <!--     文件内容搜索 -->
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/search.js "></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/searchcursor.js"></script>
    <%-- <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.js"></script> --%>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/search/matchesonscrollbar.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/scroll/annotatescrollbar.js"></script>
    <!-- 折叠 -->
    <link rel="stylesheet" type="text/css" href="${ctx}/js/common/scripts/codemirror/addon/fold/foldgutter.css">
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/foldcode.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/foldgutter.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/brace-fold.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/xml-fold.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/markdown-fold.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/fold/comment-fold.js"></script>
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/mode/markdown/markdown.js"></script>
</head>
<body>
<div class="mini-fit p5">
	<textarea id="content"></textarea>
</div>
<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
    borderStyle="border:0;border-top:solid 1px #b1c3e0;">
   <a class="mini-button" onclick="javascript:closeWindow()">关闭</a>
</div>
</body>
</html>