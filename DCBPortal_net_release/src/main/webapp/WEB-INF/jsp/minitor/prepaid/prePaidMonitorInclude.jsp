<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-预付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/prepaid/prePaidMonitorInclude.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="100"/>
					<col />
					<col width="100"/>
					<col />
					<col width="100"/>
					<col />
					<col width="100"/>
				</colgroup>
				<tr>
				<th>
	            		<span>网元名称：</span>
	            	</th>
					<td>
						<input id="NET_NAME" name="NET_NAME"  class="mini-textbox" style="width:100%;"/>
					</td>
	            	<th>
	            		<span>开始时间：</span>
	            	</th>
					<td>
						<input id="BEGIN_TIME" name="BEGIN_TIME" allowInput="false" class="mini-datepicker" 
					format="yyyy-MM-dd"  style="width:95%;" />
					</td>
	            	<th>
	            		<span>结束时间：</span>
	            	</th>
	            	<td>
						<input id="END_TIME" name="END_TIME" allowInput="false" class="mini-datepicker" 
						format="yyyy-MM-dd"  style="width:95%;" />
					</td>
					<td>
	            	<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
	            	</td>
				</tr>
			</table>
		</div>

	<div class="mini-fit" style="margin-top: 5px;">
		<div id="includeGrid" class="mini-datagrid" style="width: 100%; height: 50%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
             showFooter="true"  onrowclick="onclickHistoryGrid">
			<div property="columns">
			   <div type="checkcolumn" width="15"></div>
				<div field="NET_NAME" width="90" headerAlign="center" align="center">网元名称</div>
				<div field="CCR_COUNT" width="80" headerAlign="center" align="center">CCR总数</div>
				<div field="CCA_COUNT" width="80" headerAlign="center" align="center">CCA总数</div>
				<div field="DELAY_50" width="95" headerAlign="center" align="center">&lt50ms内时延数</div>
				<div field="DELAY_200" width="95" headerAlign="center" align="center"><200ms时延数</div>
				<div field="DELAY_500" width="95" headerAlign="center" align="center"><500ms时延数</div>
				<div field="DELAY_1000" width="95" headerAlign="center" align="center"><1000ms时延数</div>
				<div field="DELAY_5000" width="95" headerAlign="center" align="center"><5000ms时延数</div>
				<div field="DELAY_9999" width="95" headerAlign="center" align="center">>5000ms时延数</div>
				<div field="BEGIN_TIME" width="140" headerAlign="center" align="center" dateFormat="yyyy-MM-dd" >汇总日期</div>
			</div>
		</div>
		<div style="width: 100%; height:49%;margin-top:10px;">
	   
		
		<div id="resultCodeGrid" class="mini-datagrid" style="width: 100%; height: 100%;"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
               showFooter="true" >
			<div property="columns">
				  <div type="indexcolumn" width="15">序号</div>
				<div field="RESULT_CODE" width="80" headerAlign="center" align="center">结果码</div>
				<div field="RECORDS" width="80" headerAlign="center" align="center"  >记录数</div>
				<div field="REMARKS" width="200" headerAlign="center" align="left">结果码描述</div>
			</div>
		</div>
	</div>
	</div>
    </div>
</body>
</html>
