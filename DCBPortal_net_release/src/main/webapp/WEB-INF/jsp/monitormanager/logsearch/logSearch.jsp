<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>日志搜索结果</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/storm.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/css/monitormanager/monitorManager.css" />
    <script language="javascript" type="text/javascript" src="${ctx}/js/monitormanager/logsearch/logSearch.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding:10px;overflow:auto;" id="monitor">
		<div style="height:28px;line-height:28px;font-size:14px;font-weight:bold;padding-bottom:5px;margin-bottom:0px;">
	    	日志搜索：<span class="LogFile">${dir}/${file}</span>
	    	<span style="margin-left:15px;">[${host}]</span> 
		</div>
		<form id="queryForm" class="search" style="height:50px;" action="${ctx}/monitorManager/log/searchLogInfo" method="get">
            <a class="mini-hidden" name="clusterName" value="${clusterName}" ></a>
	        <a class="mini-hidden" name="host" value="${host}" ></a>
	        <a class="mini-hidden" name="workerPort" value="${workerPort}" ></a>
	        <a class="mini-hidden" name="port" value="${logServerPort}" ></a>
	        <a class="mini-hidden" name="dir" value="${dir}" ></a>
	        <a class="mini-hidden" name="file" value="${file}" ></a>
	        <a class="mini-hidden" name="tid" value="${topologyId}" ></a>
            <a class="mini-hidden" name="pos" value="${nextOffset}" ></a>
            <a class="mini-hidden" name="ignore_before" id="ignore_before" value="${ignore_before}" ></a>
            
            
			<table class="formTable8" style="width:100%;height:100%;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
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
						<input id="key" name="key" value="${keyword}" class="mini-textbox" required="true" style="width: 150px;"  value="${keyword}"/>
					</td>
					<td>
						<input id="caseIgnore" name="caseIgnore"  class="mini-checkbox" text="忽略大小写"/>
					</td>
	                <td>
	                	<a class="mini-button" onclick="search()" style="margin-left: 20px;" >继续搜索</a>
	                	<a class="mini-button mini-button-green"  style="margin-left: 20px;" onclick="back()" plain="false">返回</a>
	                </td>
				</tr>
			</table>
		</form>
		<c:choose>
			<c:when test="${tip != null}">
				<div class="col-md-8 col-md-offset-2 alert alert-warning"
					role="alert">
					<strong>日志搜索错误：</strong> ${tip}
				</div>
			</c:when>
			<c:otherwise>
				<table class="table sortable" style="word-break:break-all; word-wrap:break-word;">
					<thead>
						<tr>
							<th style="font-size:14px;">偏移量</th>
							<th style="padding-left:20px;font-size:14px;">匹配值(${numMatch})</th>
						</tr>
					</thead>
					<tbody id="html-data" >
						<c:forEach var="match" items="${matchResults}">
							<tr>
								<td><a class="searchLogMatchKey" href="${ctx}/monitorManager/log/nimLogInfo?clusterName=${clusterName}&host=${host}&port=${logServerPort}&file=${file}&dir=${dir}&pos=${match.key}"
									> ${match.key} </a></td>
								<td><pre>${match.value}</pre></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</c:otherwise>
		</c:choose>
	</div>
</body>	
</html>
