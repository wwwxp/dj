<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务监控-后付费</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/postpaid/postPaidMonitor.js"></script>
	
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:35px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
				  <col width="130"/>
					<col />
				   <col width="80"/>
					<col />
					<col width="80"/>
					<col />
					<col width="300"/>
				
				
				</colgroup>
				<tr>
				 <th>
	            	 <span>文件序列号：</span>
	            	</th>
					<td>
						<input id="SOURCE_ID" name="SOURCE_ID"  class="mini-textbox" style="width:100%;"/>
					</td>
				   <th>
	            	 <span>业务类型：</span>
	            	</th>
					<td>
						<input id="OPER_TYPE" name="OPER_TYPE"  class="mini-combobox"  textField="MAPPING_NAME" valueField="MAPPING_TYPE" 
						style="width:100%;" emptyText="=全部="  showNullItem="true" nullItemText="=全部=" />
					</td>
					<th>
	            	 <span>处理环节：</span>
	            	</th>
					<td>
						<input id="PROCER_ID" name="PROCER_ID" class="mini-combobox"   style="width:100%;"
                           data="getSysDictData('procer_id')"   valueField="code"
                           emptyText="=全部="  showNullItem="true" nullItemText="=全部=" />
					</td>
					</tr>
	            	<tr>
	            	<th>
					<span>异常话单百分比(%)：</span>
					</th>
					<td>
					<input id="ABNORMAL_PERCENT" name="ABNORMAL_PERCENT"  class="mini-textbox" style="width:100%;"/>
					</td>
	            	<th>
	            		<span>开始时间：</span>
	            	</th>
					<td>
						<input id="BEGIN_TIME" name="BEGIN_TIME" allowInput="false" class="mini-datepicker" 
					format="yyyy-MM-dd H:mm:ss" showTime="true" timeFormat="H:mm:ss" style="width:100%;" />
					</td>
	            	<th>
	            		<span>结束时间：</span>
	            	</th>
	            	<td>
						<input id="END_TIME" name="END_TIME" allowInput="false" class="mini-datepicker" format="yyyy-MM-dd H:mm:ss" 
					timeFormat="H:mm:ss" showTime="true" style="width:100%;" />
					</td>
					
					<td >
	            	<a class="mini-button" onclick="search()" style="margin-left: 10px;">查询</a>
	            	<a class="mini-button" onclick="trendChart()" style="margin-left: 10px;">当天趋势</a>
	            	<a class="mini-button" onclick="queryOverstock()" style="margin-left: 10px;">话单积压</a>
	            	</td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="margin-top: 5px;">
	<div  style="width: 100%; height: 100%">
	<div class="mini-fit" >
	<div id="postPaidGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="" allowResize="false" allowCellselect="false" multiSelect="true" allowCellWrap="true"  showFooter="true" >
			<div property="columns">
				<div field="RATING_LOG_ID" width="90" headerAlign="center" align="center">ID</div>
				<div field="SOURCE_ID" width="80" headerAlign="center" align="center">文件序列号</div>
				<div field="OPER_TYPE_NAME" width="80" headerAlign="center" align="center">业务类型</div>
<!-- 				<div field="LATN_ID" width="90" headerAlign="center" align="center">本地网</div> -->
				<div field="PROCER_ID_NAME" width="80" headerAlign="center" align="center">处理环节</div>
				<div field="NORMAL_RECORDS" width="80" headerAlign="center" align="center">正常话单数</div>
				<div field="INVALID_RECORDS" width="80" headerAlign="center" align="center">无效话单数</div>
				<div field="ABNORMAL_RECORDS" width="80" headerAlign="center" align="center">异常话单数</div>
				<div field="NOUSER_RECORDS" width="80" headerAlign="center" align="center">无主话单数</div>
				<div field="TOTAL_RECORDS" width="80" headerAlign="center" align="center">总话单数</div>
<!-- 				<div field="DUAL_RECORDS" width="80" headerAlign="center" align="center">重复话单数</div> -->
				<div field="ORI_CHARGE" width="80" headerAlign="center" align="center">总标准费用</div>
				<div field="DISCT_CHARGE" width="80" headerAlign="center" align="center">总优惠费用</div>
				<div field="TOTAL_CHARGE" width="80" headerAlign="center" align="center">总费用</div>
				<div field="CHARGE_RECORDS" width="80" headerAlign="center" align="center">总费用记录数</div>
				<div field="BEGIN_TIME" width="140" headerAlign="center" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">文件处理开始时间</div>
				<div field="END_TIME" width="140" headerAlign="center" dateFormat="yyyy-MM-dd HH:mm:ss" align="center">文件处理结束时间</div>
				
				   <div field="TOPIC" width="80" headerAlign="center" align="center">处理主题名</div>
<!-- 				<div field="PARENT_SOURCE_ID" width="80" headerAlign="center" align="center">PARENT_SOURCE_ID</div> -->
<!-- 				<div field="PROCER_ID_TMP" width="80" headerAlign="center" align="center">PROCER_ID_TMP</div> -->
				<div field="CREATE_TIME" width="140" headerAlign="center"  dateFormat="yyyy-MM-dd HH:mm:ss"  align="center">创建时间</div>
				
				<div field="LOG_STATE" width="80" headerAlign="center" align="center">日志状态</div>
<!-- 				<div field="STAFF_ID" width="80" headerAlign="center" align="center">员工ID</div> -->
				
			</div>
		</div>
	</div>
	
	
	<div style="width: 100%; height: 180px;">
	<div id="formatPieChart" style="width: 50%; height: 100%;float:left;">
	
	</div>
	<div id="pricingPieChart" style="width: 50%; height: 100%;float:left;">
	
	</div>
	</div>
	</div>
	</div>
    </div>
</body>
</html>
