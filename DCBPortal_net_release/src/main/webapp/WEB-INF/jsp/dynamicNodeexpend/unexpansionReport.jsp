<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/dynamicNodeexpend/unexpansionReport.js"
		type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>收缩报告</title>
</head>
<body>
<div class="mini-fit p5">
		 <p>&nbsp;&nbsp;&nbsp;&nbsp;根据集群中的主机资源监控近一个月的历史数据分析出，已触发预测收缩阀值并超过空闲值，实行收缩。</p>
		 <table class="formTable6" style="table-layout: fixed;">
		  <colgroup>
                <col width="90px" />
                <col />
                <col width="80px" />
                <col />
                 <col width="50px"/>
            </colgroup>
		 	<tr>
		 	 	<th>CPU当前值：</th>
		 	 	<td>&nbsp;&nbsp;<label id="CPU"></label></td>
		 	 	<th>&nbsp;&nbsp;空闲阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_CPU"></label>&nbsp;&nbsp;</td>
		 	 	<td>&nbsp;&nbsp;<label id="A_CPU_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>内存当前值：</th><td>&nbsp;&nbsp;<label id="MEM"></label></td>
		 	 	 	<th>&nbsp;&nbsp;空闲阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_MEM"></label>&nbsp;&nbsp;</td>
		 	 	<td>&nbsp;&nbsp;<label id="A_MEM_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>硬盘当前值：</th><td>&nbsp;&nbsp;<label id="DISK"></label></td>
		 	 	 	<th>&nbsp;&nbsp;空闲阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_DISK"></label>&nbsp;&nbsp;</td>
		 	 	<td>&nbsp;&nbsp;<label id="A_DISK_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<th>业务量当前值：</th><td>&nbsp;&nbsp;<label id="BUSS_VOLUME"></label></td>
		 	 	 	<th>&nbsp;&nbsp;空闲阀值：&nbsp;&nbsp;</th>
		 	 	<td>&nbsp;&nbsp;<label id="A_BUSS_VOLUME"></label>&nbsp;&nbsp;</td>
		 	 	<td>&nbsp;&nbsp;<label id="A_BUSS_VOLUME_RESULT"></label></td>
		 	</tr>
		 	<tr>
		 	 	<td colspan="5">
		 	 	根据以上指标统计建议收缩【
		 	 	<label id="ADVISE_NODE_COUNT"></label>&nbsp;】台
		 	 	</td> 
		 	</tr>
		  
		 </table>
		 <p align="right" style="height: 30px;padding-top: 50px;color: #CFCFCF"> 报告生成时间：<label id="CRT_DATE"></label></p>
	</div>
	<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
    <a class="mini-button" onclick="execjob()" style="margin-right: 20px;" id="btnA">立即执行</a> <span
        style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="timerExecjob" id="btnB">定时执行</a>
</div>
</body>
</html>
