<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-后付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/flow/surplusedFlowQuery.js"></script>
	
</head>
<body>
<div  style="width:100%;height:100%">
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:30px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				   <col width="100"/>
					<col />
					<col width="100"/>
					<col />
					<col />
					<col />
				</colgroup>
				<tr>
				<th>
	            <span>设备号：</span>
	           	</th>
				<td >
					<input id="SERVNO" name="SERVNO"  class="mini-textbox" style="width:100%;"  required="true" vtype="int"/>
				</td>
				<td>
	           	<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
	           	</td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="margin-top: 5px;">
		<div id="flowGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="false" pageSize="99999" allowCellWrap="true"  
             showFooter="false" showLoading="false"  onload="onload">
			<div property="columns">
			     <div type="indexcolumn">序号</div>   
				<div field="GROUPID_NAME" width="150" headerAlign="center" align="center">套餐</div>
				<div field="BASERESOURCEID_NAME" width="250" headerAlign="center" align="center" >累积量</div>
				<div field="TRANSFERVALUE" width="80" headerAlign="center" align="center" renderer="numberFormat">结转剩余量</div>
				<div field="TRANSFERVALUETOTAL" width="80" headerAlign="center" align="center" renderer="numberFormat">结转总量</div>
				<div field="VALUE" width="80" headerAlign="center" align="center" renderer="numberFormat">上月使用量</div>
				<div field="VALUETOTAL" width="80" headerAlign="center" align="center" renderer="numberFormat">上月总量</div>
				
			</div>
		</div>
	</div>
   </div>
</body>
</html>
