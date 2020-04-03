<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<html>
<head>
	<title>jstack日志查看</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/logdetails/jstack.js"></script>
</head>
<body>
	<div style="padding:10px;" class="">
		<div style="padding:5px;position:absolute;right:5px;top:4px;">
			<span style="float:right;"><a class="mini-button mini-button-green" onclick="javascript:history.back();" plain="false">返回</a> </span>
		</div>
	
		<div class="headDiv">
		    <div style="height:28px;line-height:28px;font-size:14px;font-weight:bold;padding-bottom:5px;">
		    	JStack日志查看<span style="margin-left:15px;">[${hostip}]</span> 
		    </div>
	    </div>
    	<hr class="logHr"/>

	    <div class="html-data">
	        <c:choose>
	            <c:when test="${summary!=null}">
	                <div class="col-md-8 col-md-offset-2 alert alert-warning" role="alert">
	                    <strong>Ooops!</strong> ${summary}
	                </div>
	            </c:when>
	            <c:otherwise>
	            <pre class="view-plain">${jstack}</pre>
	            </c:otherwise>
	        </c:choose>
    	</div>
	    <hr class="logHr"/>
	</div>
</body>
</html>