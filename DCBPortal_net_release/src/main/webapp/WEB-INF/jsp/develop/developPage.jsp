<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.tydic.util.SessionUtil" %>
<%@ page import="java.io.File" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>开发页面</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
     <!--   codemirror -->
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
    <script language="javascript" type="text/javascript" src="${ctx}/js/common/scripts/codemirror/addon/dialog/dialog.js"></script>
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
    
    <script language="javascript" type="text/javascript" src="${ctx}/js/develop/developPage.js"></script>
    <script language="javascript" type="text/javascript" >
    var path="${requestScope.path}";
    <% 
    String templatePath =  SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"develop"+File.separator+"template";
    if(File.separator.equals("\\")){
    	templatePath=templatePath.replaceAll("/+", "\\\\");
    }else if(File.separator.equals("/")){
    	templatePath=templatePath.replaceAll("\\\\+", "/");
    }
    
    %>
    var templatePath = '<%=java.net.URLEncoder.encode(templatePath)%>';
    
    
    </script>
    
    
</head>
<body>
<!-- <span style="color:red;">hello world</span> -->
	<div class="" >
		<form>
			<textarea id="code" name="code" >${requestScope.content}</textarea>
		</form>
	
	<div  id="saveFile" style="text-align: center;margin-top:5px;display:none">
	 <a class="mini-button mini-button-green" onclick="saveFilePage" style="width:150px;text-algin:center;">保存</a>
	</div>
	</div>
	
	
</body>
</html>
