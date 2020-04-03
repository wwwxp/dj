<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-预付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/prepaid/prePaidMonitor.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
		               <td style="text-align: right;">
		               
		               <a class="mini-button mini-button-green" onclick="trendChart()" plain="false">当天趋势</a> 
                    	<a class="mini-button mini-button-green" onclick="openDetail()" plain="false">明细查询</a> 
                    	<a class="mini-button mini-button-green" onclick="openDetailInclude()" plain="false">汇总统计</a> 
		               </td>
				</tr>
			</table>
		</div>
    <div class="mini-fit" style="margin-top: 5px;">
	<div  style="width: 100%; height: 100%">
	   <div style="width: 50%; height: 100%;float:left;">
	    <div class="mini-fit" >
		<div id="prePaidGrid" class="mini-datagrid" style="width: 100%; height: 100%;"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
             showFooter="true" showLoading="false" onrowclick="prePaidGridClick" onload="prePaidGridOnload">
			<div property="columns">
			    <div type="checkcolumn" width="25"></div>
				<div field="NET_NAME" width="150" headerAlign="center" align="center">网元名称</div>
				<div field="END_TIME" width="120" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" >时间</div>
				<div field="CCR_COUNT" width="60" headerAlign="center" align="center">CCR总数</div>
				<div field="CCA_COUNT" width="60" headerAlign="center" align="center">CCA总数</div>
				<div field="DIFF_COUNT" width="60" headerAlign="center" align="center">差异数</div>
<!-- 				<div field="DELAY_50" width="95" headerAlign="center" align="center">&lt50ms内时延数</div> -->
<!-- 				<div field="DELAY_200" width="95" headerAlign="center" align="center"><200ms时延数</div> -->
<!-- 				<div field="DELAY_500" width="95" headerAlign="center" align="center"><500ms时延数</div> -->
<!-- 				<div field="DELAY_1000" width="95" headerAlign="center" align="center"><1000ms时延数</div> -->
<!-- 				<div field="DELAY_5000" width="95" headerAlign="center" align="center"><5000ms时延数</div> -->
<!-- 				<div field="DELAY_9999" width="95" headerAlign="center" align="center">>5000ms时延数</div> -->
<!-- 				<div field="BEGIN_TIME" width="140" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" >开始时间</div> -->
<!-- 				<div field="END_TIME" width="140" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" >结束时间</div> -->
			</div>
		</div>
		</div>
		<div style="width: 100%;height: 200px;"> 
			<div id="prePaidPieChart" style="height:100%;width:100%;"> 112wwss34567</div>
		</div>
		 </div>
		<div style="width: 49.5%; height: 100%;float:left;margin-left:5px;" >
		<div id="resultCodeGrid" class="mini-datagrid" style="width: 100%; height: 100%;float:left;" pageSize="99999"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
             showFooter="false" showLoading="false" onrowclick="resultCodeGridClick" onload="resultCodeGridOnload">
			<div property="columns">
			   <div type="indexcolumn" width="25">序号</div>
				<div field="RESULT_CODE" width="90" headerAlign="center" align="center">结果码</div>
				<div field="RECORDS" width="90" headerAlign="center" align="center"  >记录数</div>
				<div field="REMARKS" width="150" headerAlign="center" align="left">结果码描述</div>
			</div>
		</div>
		
		</div>
	</div>
	<div>
	
	</div>
	</div>
    </div>
</body>
</html>
