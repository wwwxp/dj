<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/program/addEditProgram.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="programForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="120"/>
				<col/>
				<col width="120"/>
				<col/>
			</colgroup>
			<tr>
				<th>
					<span class="fred">*</span>程序名称：
				</th>
				<td >
					<input id="PROGRAM_NAME" name="PROGRAM_NAME" required="true" class="mini-textbox" style="width:100%;" />
				</td>
            	<th>
            		<span class="fred">*</span>程序编码：
            	</th>
				<td >
					<input id="PROGRAM_CODE" name="PROGRAM_CODE" required="true" class="mini-textbox" style="width:100%;" />
				</td>
			</tr>
			<tr>
				<th>
					<span class="fred">*</span>程序类型：
				</th>
				<td >
					<input id="PROGRAM_TYPE" name="PROGRAM_TYPE" valueField="CODE" textField="CODE" required="true" class="mini-combobox" style="width:100%;"/>
				</td>
            	<th>
            		所属组：
            	</th>
				<td >
					<input id="PROGRAM_GROUP" name="PROGRAM_GROUP" class="mini-textbox" maxlength="36" style="width:100%;" />
				</td>
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>脚本名：
            	</th>
				<td>
					<input id="SCRIPT_SH_NAME" name="SCRIPT_SH_NAME" required="true" class="mini-textbox" maxlength="120" style="width:100%;" />
				</td>
				<th>
					<span class="fred">*</span>多实例配置：
				</th>
				<td >
					<input id="MULTI_PROCESS" name="MULTI_PROCESS" valueField="code" textField="text" value="0" data="getSysDictData('personal_conf_list')" class="mini-combobox" style="width:100%;"/>
				</td>
				
			</tr>
			<tr>
				<th>
					脚本说明：
				</th>
				<td colspan="3">
					<input id="SCRIPT_SH_EXAMPLE" name="SCRIPT_SH_EXAMPLE" vtype="rangeChar:0,512" class="mini-textarea mini-textArea" style="width:100%;height:100px" />
				</td>
			</tr>
		</table>
		 <p style="color: red;">
		 	多实例配置：该字段表示启动进程时，在进程启动命令后面加入随机参数(如：tid-00001)，用来区分不同进程，达到相同的配置文件、配置参数在同一台机器上可以启动多次
		 </p>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="submit" style="width:60px;margin-right:20px;">提交</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
