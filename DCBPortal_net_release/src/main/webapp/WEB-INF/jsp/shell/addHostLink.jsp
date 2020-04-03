<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/shell/addHostLink.js"></script>
	<title> </title>
</head>

<body>
    <div class="mini-fit p5">
        <table id="hostForm" class="formTable6" style="table-layout: fixed;">
            
             <tr>
                <th  width="120"><span class="fred">*</span>主机：</th>
                <td>
               <input width="100%" id="host" name="host" value="192.168.161.93" class="mini-textbox" required="true"/>
                </td>
                <th  width="120"><span class="fred">*</span>端口：</th>
                <td>
               <input width="100%" id="port" name="port" value="22" class="mini-textbox" required="true" vtype="integer"/>
                </td>
                </tr>
               </tr>
               <tr>
                <th  width="120"><span class="fred">*</span>用户名：</th>
                <td>
               <input width="100%" id="name" name="name" value="sh_dca" class="mini-textbox" required="true"/>
                </td>
                <th  width="120"><span class="fred">*</span>密码：</th>
                <td>
               <input width="100%" id="password" name="password"  value="sh_dca2015" class="mini-password" required="true"/>
                </td>
                </tr>
               </tr>
            
            
        </table>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">确定</a> <span
            style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="onCancel" style="width:60px;">取消</a>
    </div>
</body>
</html>