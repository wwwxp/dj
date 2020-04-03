<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>日志查看</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/logdetails/logDetails.js"></script>
</head>
<body>
	<div style="padding:10px;" class="monitor">
		<div class="headDiv">
		    <div style="height:28px;line-height:28px;font-size:14px;font-weight:bold;padding-bottom:5px;">
		    	日志查看：<span class="LogFile">${dir}/${logName}</span>
		    	<span style="margin-left:15px;">[${host}]</span> 
		    	
		    </div>
	
		    <form id="queryForm" class="search" action="${ctx}/monitorManager/log/searchLogInfo" method="get">
				<a class="mini-hidden" name="clusterName" value="${clusterName}" ></a>
		        <a class="mini-hidden" name="host" value="${host}" ></a>
		        <a class="mini-hidden" name="workerPort" value="${workerPort}" ></a>
		        <a class="mini-hidden" name="port" value="${logServerPort}" ></a>
		        <a class="mini-hidden" name="dir" value="${dir}" ></a>
		        <a class="mini-hidden" name="file" value="${logName}" ></a>
		        <a class="mini-hidden" name="tid" value="${topologyId}" ></a>
				<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
					<colgroup>
						<col width="100"/>
						<col width="200"/>
						<col width="100"/>
						<col/>
					</colgroup>
					<tr>
		            	<th>
		            		<span>关键字搜索：</span>
		            	</th>
						<td>
							<input id="key" name="key" class="mini-textbox" required="true" style="width: 150px;"/>
						</td>
						<td>
							<input id="caseIgnore" name="caseIgnore" class="mini-checkbox" text="忽略大小写"/>
						</td>
		                <td><a class="mini-button" style="margin-left: 20px;" onClick="search">查询</a>
		                	<a class="mini-button mini-button-green"  style="margin-left: 20px;" onclick="back()" plain="false">返回</a>
		                </td>
					</tr>
				</table>
			</div>
	    </form>
    	<hr class="logHr"/>

	    <div id="html-data">
	        <c:choose>
	            <c:when test="${summary!=null}">
	                <div class="col-md-8 col-md-offset-2 alert alert-warning" role="alert">
	                    <strong>日志查询错误：</strong> ${summary}
	                </div>
	            </c:when>
	            <c:otherwise>
	            <pre class="view-plain" style="padding:5px; white-space:pre-wrap;">${log}</pre>
	            </c:otherwise>
	        </c:choose>
	    </div>
	    <hr class="logHr"/>

	    <ul class="pagination">
	        <c:forEach var="page" items="${pages}">
	            <li class="${page.status}">
	                <a href="${page.url}">
	                    <span>${page.text}</span>
	                </a>
	            </li>
	        </c:forEach>
	    </ul>
	</div>
</body>
</html>
