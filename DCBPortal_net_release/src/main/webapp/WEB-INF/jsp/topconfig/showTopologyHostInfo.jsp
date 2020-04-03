<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/topconfig/showTopologyHostInfo.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="taskFrom" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		同步主机：
            	</th>
				<td>
					<input id="HOST_IP" name="HOST_IP" class="mini-checkboxlist" repeatItems="3" repeatLayout="flow" value="" 
					    textField="HOST_IP" valueField="HOST_IP"   required="true" 
					    url="${ctx}/core/queryForList?execKey=host.queryComputeHostEffective" >
					</input>
				</td>
            	 
			</tr>
			
		</table>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
