<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>编辑数据源</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="/public/common/common.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
<script language="javascript" type="text/javascript"
	src="${ctx}/js/configuremanager/configdfine/addEditDataSource.js"></script>
</head>
<body>
	<div class="mini-fit p5">
        <table id="form" class="formTable6" style="table-layout: fixed;">
            <input name="DATASOURCE_ID" class="mini-hidden" type="hidden"/>
            <colgroup>
                <col width="95px" />
                <col />
                <col width="95px" />
                <col />
            </colgroup>
            	
            <tr>
                <th><span class="fred">*</span>数据源名称：</th>
                <td>
                <input width="100%" name="DATASOURCE_NAME" class="mini-textbox" required="true" /></td>
                <th><span class="fred">*</span>数据源状态：</th>
                <td><input width="100%" id="DATASOURCE_STATE" name="DATASOURCE_STATE" class="mini-combobox"
                    	required="true" textField="text" valueField="code"  data="getSysDictData('valid_flag')" />
                </td>
            </tr>
            <tr>
                <th><span class="fred">*</span>数据源类型：</th>
                <td><input width="100%" id="DATASOURCE_TYPE" name="DATASOURCE_TYPE" class="mini-combobox"
                    	required="true" textField="text" valueField="code" data="getSysDictData('datasource_type')" onvaluechanged="onDataSourceTypeChange"/>
                </td>
                <th><span class="fred">*</span>数据源驱动类：</th>
                <td><input width="100%" id="DATASOURCE_DRIVER" name="DATASOURCE_DRIVER" class="mini-textbox" required="true" allowInput="false"/></td>
            </tr>
            <tr>
                <th><span class="fred">*</span>数据源URL：</th>
                <td colspan="3">
                	<input width="100%" name="DATASOURCE_URL" class="mini-textbox" required="true"/>
                </td>
            </tr>
            <tr>
                <th><span class="fred">*</span>用户名：</th>
                <td><input width="100%" name="DATASOURCE_USER" class="mini-textbox" required="true"/>
                <th><span class="fred">*</span>密码：</th>
                <td><input width="100%" name="DATASOURCE_PWD" class="mini-password" required="true"/>
                </td>						
            </tr>
        </table>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onTest" style="width:60px;margin-right:20px;">测试</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="onCancel" style="width:60px;">取消</a>
    </div>
</body>
</html>
