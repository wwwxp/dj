<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/clustermanager/addEditHost.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="hostFrom" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="120"/>
				<col/>
				<col width="120"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred">*</span>主机名：
            	</th>
				<td colspan="3">
					<input id="HOST_NAME" name="HOST_NAME" required="true"  
						class="mini-textbox" style="width:99%;" vtype="maxLength:64" maxlength="64"/>
				</td>
			</tr>
			<tr>
				<th>
					<span class="fred">*</span>主机IP：
				</th>
				<td>
					<input id="HOST_IP" name="HOST_IP" required="true" maxlength="64"
						   class="mini-textbox" style="width:98%;" vtype="maxLength:64"/>
				</td>
				<th>
					IPV6网卡：
				</th>
				<td>
					<input id="HOST_NET_CARD" name="HOST_NET_CARD" maxlength="64"
						   class="mini-textbox" style="width:98%;" vtype="maxLength:64"/>
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>SSH登录端口：
            	</th>
				<td>
					<input id="SSH_PORT" name="SSH_PORT" required="true" value="22" vtype="int;maxLength:6"  maxlength="6"
						class="mini-textbox" style="width:98%;" />
				</td>
            	<th>
            		<span class="fred">*</span>SSH用户：
            	</th>
				<td>
					<input id="SSH_USER" name="SSH_USER" required="true" maxlength="20"
						vtype="maxLength:20" class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>SSH用户密码：
            	</th>
				<td>
					<input id="SSH_PASSWD" name="SSH_PASSWD" required="true" maxlength="20"
						vtype="maxLength:20"  class="mini-password" style="width:98%;" />
				</td>
            	<th>
            		<span class="fred">*</span>CPU核心数(个)：
            	</th>
				<td>
					<input id="CORE_COUNT" name="CORE_COUNT" required="true" maxlength="6"
						vtype="int;maxLength:6"  class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>SSH密码确认：
            	</th>
				<td>
					<input id="SSH＿CHECK_PASSWD" name="SSH＿CHECK_PASSWD" required="true" maxlength="20"
						vtype="maxLength:20" requiredErrorText="密码不一致"  class="mini-password" style="width:98%;" />
				</td>
				<th>
            	<span class="fred">*</span>内存大小(G)：
            	</th>
				<td>
					<input id="MEM_SIZE" name="MEM_SIZE" required="true" vtype="float;maxLength:6" maxlength="6"
						class="mini-textbox" style="width:98%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>存储大小(G)：
            	</th>
				<td>
					<input id="STORE_SIZE" name="STORE_SIZE" required="true" vtype="float;maxLength:6" maxlength="6"
						class="mini-textbox" style="width:98%;" />
				</td>
				<th></th>
				<td></td>
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
