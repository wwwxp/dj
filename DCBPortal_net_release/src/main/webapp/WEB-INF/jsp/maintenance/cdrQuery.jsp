<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>话单文件查询</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/assets/css/bootstrap.min.css"/>
	<script language="javascript" type="text/javascript" src="${ctx}/js/maintenance/cdrQuery.js"></script>
</head>
<body>
	<div class="mini-fit" style="padding: 5px;">
		<div id="queryForm" class="search">
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
	            		<span>业务类型：</span>
	            	</th>
					<td>
						<input class="mini-combobox" id="cdr_type"  name="cdr_type" style="width:80%;"
                           data="getSysDictData('cdr_type')"  valueField="code" value="1" onvaluechanged="loadGrid" />
					</td>
	            	
	                <td><a class="mini-button" onclick="" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		
	<div class="mini-fit" style="margin-top: 0px;">
		<div id="fileGrid" class="mini-datagrid" style="width: 100%; height: 100%"
             allowResize="false" allowCellselect="false" multiSelect="false" showFooter="false" >
			<div property="columns">
				<div type="indexcolumn" width="10" >序号</div>
                <div field="CATALOG"  headerAlign="center"  align=left>目录</div> 
                <div field="DATE"  width="50" headerAlign="center" align="center">时间</div>
				<div field="FILES_NUM"  width="50" headerAlign="center" align="center">文件数</div>
				<div field="NUMBERS"  width="50" headerAlign="center" align="center" renderer="format">记录数</div>
				
			</div>
		</div>
	</div>
    </div>
</body>
</html>
