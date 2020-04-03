<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/public/common/common.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"> 
		<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
<%-- 		<script language="javascript" type="text/javascript" src="${ctx}/js/homepage/homepage.js"></script> --%>
	
		
		<title>首页</title>
	</head>

	<body>
		<div class="mini-fit p5" style="overflow: auto;">
			<table style="width: 100%;table-layout:fixed;height:100%;min-width: 1320px;">
				<colgroup>
					<col width="50%" />
					<col width="50%" />
				</colgroup>
				<tr>
					<td valign="top">
						<div class="mini-panel" title="在线OMC"
							style="width: 100%; height: 100%; " bodyStyle="padding:0px;"
							showCloseButton="false" showFooter="false" 
							showCollapseButton="false">
							<div class="mini-fit p5"  >
							 
								<div id="prePaidGrid" class="mini-datagrid" style="width: 100%; height: 100%; " pageSize="99999"
						             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
						             showFooter="false" showLoading="false" onrowclick="preGridClick" onload="preGridOnload">
									<div property="columns">
									<div header='在线OMC当天处理总量<a style="color:white;margin-left:180px;" class="mini-button mini-button-green" onclick="resetPrePaidTime()" plain="false">重新统计</a>' headerAlign="right" >
									<div property="columns">
									    <div type="checkcolumn" width="25"></div>
										<div field="NET_NAME" width="65" headerAlign="center" align="center">网元名称</div>
										<div field="CCR_COUNT" width="60" headerAlign="center" align="center">CCR总数</div>
										<div field="CCA_COUNT" width="60" headerAlign="center" align="center">CCA总数</div>
										<div field="DELAY_50" width="60" headerAlign="center" align="center"><50ms</div>
										<div field="DELAY_100" width="60" headerAlign="center" align="center"><100ms</div>
										
										<div field="DELAY_200" width="60" headerAlign="center" align="center"><200ms</div>
										<div field="DELAY_500" width="60" headerAlign="center" align="center"><500ms</div>
										<div field="DELAY_1000" width="65" headerAlign="center" align="center"><1000ms</div>
										<div field="DELAY_5000" width="65" headerAlign="center" align="center"><5000ms</div>
										<div field="DELAY_9999" width="65" headerAlign="center" align="center">>5000ms</div>
									</div>
									</div>
									</div>
								</div>
						     
	                       		
	                       	</div>
	                       	<div style="width:100%;height:250px;margin-top: 5px;">
	                       	<div id="prePaidChart" style="height:100%;width:60%;float:left;" ></div>
	                       	<div id="prePaidPieChart" style="height:100%;width:40%;float:left;"> </div>
	                       	</div>
						</div>
					</td>
					<td valign="top">
						<div class="mini-panel" title="离线OMC"
							style="width: 100%; height: 100%;"  bodyStyle="padding:0px;"
							showCloseButton="false"   showCollapseButton="false">
						   <div class="mini-fit p5" >
								<div id="postPaidGrid" class="mini-datagrid" style="width: 100%; height: 100%;" pageSize="99999"
						             idField="" allowResize="false" allowCellselect="false" multiSelect="false" allowCellWrap="true"  
						             showFooter="false" showLoading="false" onrowclick="postGridClick" onload="postGridOnload">
									
									<div property="columns">
									<div header='离线OMC当天处理总量<a style="color:white;margin-left:180px;" class="mini-button mini-button-green" onclick="resetPostPaidTime()" plain="false">重新统计</a>' headerAlign="right">
									<div property="columns">
									    <div type="checkcolumn" width="25"></div>
										<div field="MAPPING_NAME" width="70" headerAlign="center" align="center">业务类型</div>
										<div field="SOURCE_ID_COUNT" width="70" headerAlign="center" align="center">文件总数</div>
										<div field="NORMAL_RECORDS" width="70" headerAlign="center" align="center">正常话单数</div>
										<div field="INVALID_RECORDS" width="70" headerAlign="center" align="center">无效话单数</div>
										<div field="ABNORMAL_RECORDS" width="70" headerAlign="center" align="center">异常话单数</div>
										<div field="NOUSER_RECORDS" width="70" headerAlign="center" align="center">无主话单数</div>
										<div field="TOTAL_RECORDS" width="70" headerAlign="center" align="center">总话单数</div>
										<div field="CHARGE" width="80" headerAlign="center" renderer="formatCharge" align="center">费用（元）</div>
									</div>
									</div>
									</div>
								</div>
						    
	                       		
	                       	</div>
	                       	<div style="width:100%;height:250px;margin-top: 5px;">
	                       	<div id="postPaidChart" style="height:100%;width:60%;float:left;" ></div>
	                       	<div id="postPaidPieChart" style="height:100%;width:40%;float:left;"></div>
	                       	</div>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</body>
</html>