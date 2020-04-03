<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>业务程序管理 </title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/program/programManage.js"></script>
</head>
<body>
    <div class="mini-fit" style="padding: 5px;">

		<div id="queryFrom" class="search">
			<table class="formTable8" style="width:100%;height:50px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="80px"/>
					<col />
					<col width="160px"/>
				</colgroup>
				<tr>
					<th>
						<span>程序类型：</span>
					</th>
					<td>
						<input id="PROGRAM_TYPE" name="PROGRAM_TYPE" class="mini-combobox" style="width: 90%;"
							   textField="PROGRAM_TYPE" valueField="PROGRAM_TYPE" showNullItem="true" allowInput="false"/>
					</td>
					<th>
						<span>程序名称：</span>
					</th>
					<td>
						<input id="PROGRAM_NAME" name="PROGRAM_NAME" class="mini-textbox" style="width: 90%;"/>
					</td>
					<th>
						<span>程序编码：</span>
					</th>
					<td>
						<input id="PROGRAM_CODE" name="PROGRAM_CODE" class="mini-textbox" style="width: 90%;"/>
					</td>
					<th>
						<a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a>
					</th>
					<td></td>
					<td></td>
				</tr>
			</table>
		</div>

		<div class="search2" style="border: 0px;">
			<table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
				<tr>
	               <td style="text-align: right;">
	               	<a class="mini-button mini-button-green" onclick="add()" plain="false">新增</a>
                   	<a class="mini-button mini-button-green" onclick="del()" plain="false">删除</a> 
	               </td>
				</tr>
			</table>
		</div>
		<div class="mini-fit" style="margin-top: 0px;">
			<div id="programGrid" class="mini-datagrid" style="width: 100%; height: 100%"
	             idField="PROGRAM_CODE" allowResize="false" allowCellselect="false" multiSelect="true" showFooter="true" >
				<div property="columns">
					<div type="checkcolumn" width="25" ></div>
					<div field="PROGRAM_TYPE"  width="80" headerAlign="center" align="center">程序类型</div>
					<div field="PROGRAM_NAME"  width="100" headerAlign="center" align="center">程序名称</div>
					<div field="PROGRAM_CODE"  width="100" headerAlign="center" align="center">程序编码</div>
					<div field="PROGRAM_GROUP"  width="100" headerAlign="center" align="center">所属组</div>
					<div field="MULTI_PROCESS_DESC"  width="90" headerAlign="center" align="center">是否多实例</div>
					<div field="SCRIPT_SH_NAME"  width="160" headerAlign="center" align="center">脚本名</div>
					<div field="SCRIPT_SH_EXAMPLE"  width="180" headerAlign="center" align="left">脚本说明</div>
					<div field="CRT_DATE" dateFormat="yyyy-MM-dd HH:mm:ss"  width="100" headerAlign="center" align="center">创建时间</div>
					<div name="action" width="80" headerAlign="center" align="center" renderer="onActionRenderer" cellStyle="padding:0;">操作</div>
				</div>
			</div>
		</div>
    </div>
</body>
</html>
