<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>当天走势</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/prepaid/preTrendChart.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
	<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:35px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="100"/>
					<col />
					<col />
				
				
				</colgroup>
				<tr>
				<th>
	            		<span class="fred"> *</span> 网元：
	            	</th>
					<td>
						<input id="NET_NAME" name="NET_NAME" class="mini-combobox" style="width:100%;"
						 textField="NET_NAME" valueField="NET_NAME" required="true" allowInput="false"
						 onvaluechanged="search" />
					</td>
					<td>
<!-- 	            	<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a> -->
	            	</td>
				</tr>
			</table>
		</div>
	
	<div id="trendChart" style="width:100%;height:400px;"></div>
	
    </div>
</body>
</html>
