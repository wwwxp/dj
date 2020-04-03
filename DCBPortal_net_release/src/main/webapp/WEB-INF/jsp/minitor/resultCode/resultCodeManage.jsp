<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/resultCode/resultCodeManage.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
					<col/>
					<col width="100"/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>OCS错误代码:</span>
	            	</th>
					<td>
						<input id="OCS_RESULT_CODE" name="OCS_RESULT_CODE" class="mini-textbox" style="width: 80%;"/>
					</td>
					<th>
	            		<span>OCP错误代码:</span>
	            	</th>
					<td>
						<input id="OCP_RESULT_CODE" name="OCP_RESULT_CODE" class="mini-textbox" style="width: 80%;"/>
					</td>
					<th>
	            		<span>描述:</span>
	            	</th>
					<td>
						<input id="REMARKS" name="REMARKS" class="mini-textbox" style="width: 80%;"/>
					</td>
	            	
	                <td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
		               <td style="text-align: right;">
		               	<a class="mini-button mini-button-green" onclick="addResultCode()" plain="false">新增</a>
                    	<a class="mini-button mini-button-green" onclick="deleteResultCode()" plain="false">删除</a> 
		               </td>
				</tr>
			</table>
		</div>
	<div class="mini-fit" style="margin-top: 0px;">
		<div id="resultCodeGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             idField="OCS_RESULT_CODE" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
			<div property="columns">
				<div type="checkcolumn" width="20" ></div>
				 <div field="OCS_RESULT_CODE" width="60" headerAlign="center" align="center">OCS错误代码</div>
                <div field="OCP_RESULT_CODE"  width="60" headerAlign="center"  align=center>OCP错误代码</div> 
				<div field="REMARKS"  width="180"headerAlign="center" align="left">描述</div>
                <div field="EFF_DATE" dateFormat="yyyy-MM-dd HH:mm:ss"  width="80" headerAlign="center" align="center">生效时间</div>
                <div field="EXP_DATE" dateFormat="yyyy-MM-dd HH:mm:ss"  width="80" headerAlign="center" align="center" >失效时间</div>
                <div field="CREATE_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="80" headerAlign="center" align="center">创建时间</div>
                <div field="MODIFY_DATE" dateFormat="yyyy-MM-dd HH:mm:ss" width="80" headerAlign="center" align="center">修改时间</div>
				<div name="action" width="80"   headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				
			</div>
		</div>
	</div>
    </div>
</body>
</html>
