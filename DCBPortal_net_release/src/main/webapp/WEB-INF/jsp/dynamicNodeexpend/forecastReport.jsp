<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/dynamicNodeexpend/forecastReport.js"
		type="text/javascript"></script>
		<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>预测扩容报告</title>
</head>
<body>
	<div class="mini-fit p5">
		 <p id="k1" style="display: none;">&nbsp;&nbsp;&nbsp;&nbsp;根据集群中的主机资源监控近一个月的历史数据分析出，将在未来时间：<span style="white-space: pre;margin: 0px;padding: 0px;">【<label id="PREDICTION_TIME"></label>】</span>触发预测阀值并超过警戒线，实行扩容。</p>
		 <p id="k2" style="display: none;">&nbsp;&nbsp;&nbsp;&nbsp;根据集群中的主机资源监控近一个月的历史数据分析出，将在未来时间30天不会触发预测阀值，不用实行扩容。</p>
		 
		<div style="width: 100%;height: 200px;margin: 0" id="chartDiv">
				<div id="aLineChart" style="width:100%;height: 100%"></div>
			</div>
		 <table class="formTable6" style="table-layout: fixed;">
		  <colgroup>
                <col width="80px" />
                <col />
                <col width="80px" />
                <col />
                 <col width="50px"/>
            </colgroup>
		 	<tr>
		 	 	<th>CPU达到：</th>
		 	 	<td>&nbsp;&nbsp;<label id="CPU"></label></td>
		 	 	<th>&nbsp;&nbsp;告警阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_CPU"></label></td>
		 	 	<td>&nbsp;&nbsp;<label  id="A_CPU_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>内存达到：</th><td>&nbsp;&nbsp;<label id="MEM"></label></td>
		 	 	 	<th>&nbsp;&nbsp;告警阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_MEM"></label></td>
		 	 	<td>&nbsp;&nbsp;<label id="A_MEM_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>硬盘达到：</th><td>&nbsp;&nbsp;<label id="DISK"></label></td>
		 	 	 	<th>&nbsp;&nbsp;告警阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_DISK"></label></td>
		 	 	<td>&nbsp;&nbsp;<label id="A_DISK_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>业务量达到：</th><td>&nbsp;&nbsp;<label id="BUSS_VOLUME"></label></td>
		 	 	 	<th>&nbsp;&nbsp;告警阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_BUSS_VOLUME"></label></td>
		 	 	<td>&nbsp;&nbsp;<label id="A_BUSS_VOLUME_RESULT"></label></td>
		 	</tr>
		 </table>
		 <p align="right" style="height: 30px;margin-top: 20px;color: #CFCFCF"> 报告生成时间：<label id="CRT_DATE"></label></p>
	</div>
</body>
</html>
