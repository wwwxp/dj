<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>DCCP云计费平台</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript"
	src="${ctx}/js/configuremanager/logLevelCfg/confirmDetail.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		 <div id="datagrid" class="mini-datagrid" idField="PRO_ID"   
			       allowCellSelect="true"  
				style="width: 100%; height: 100%" allowResize="false"  
				showEmptyText="false" showFooter="false">
				<div property="columns">
					<div type="indexcolumn" width="5" ></div>
					<div field="PRO_KEY" width="25" headerAlign="center"
						align="center">
						参数名 <input property="editor" id="PRO_KEY" name="PRO_KEY"
							class="mini-textbox" style="height: 100%; width: 100%;" />
					</div>
					<div field="PRO_VALUE" width="25" headerAlign="center"
						align="center">
						参数值 <input property="editor" id="PRO_VALUE" name="PRO_VALUE"
							class="mini-textbox" style="height: 100%; width: 100%;" />
					</div>
				</div>
			</div>
	</div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="sendMsg" style="width:60px;margin-right:20px;">提交</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
