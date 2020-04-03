<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/configuremanager/masterstandbyNet/upgreadHistory.js" type="text/javascript"></script>
	<title>历史升级记录</title>
</head>
<body>
	<div class="mini-fit p5">
		<div class="search2" style="border: 0px;padding:0px;text-align: right;">
           	<a class="mini-button mini-button-green" onclick="queryHistory()" plain="false">刷新</a>
       	</div>
       	<div class="mini-fit" style="margin-top: 2px;">
			<div id="datagrid" class="mini-datagrid" showFooter="true" 
				style="width: 100%; height: 100%;" idField="ID" allowResize="false" multiSelect="false">
				<div property="columns">
					<div type="checkcolumn" width="20">序号</div>
					<div field="DEST_PROGRAM_NAME" headerAlign="center" align="center" width="80">当前版本</div>
					<div field="SOURCE_PROGRAM_NAME" headerAlign="center" align="left" width="80" >回退版本</div>
					<div field="HOST_LIST" headerAlign="center" align="left" width="120">升级节点</div>
					<div field="CRT_DATE" headerAlign="center" align="center" dateFormat="yyyy-MM-dd HH:mm:ss" width="100">升级时间</div>
				</div>
			</div>
		</div>
	</div>
	<div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="addData()" style="width:60px;margin-right:20px;">确定</a> 
        <span style="display: inline-block; width: 25px;"></span> 
        <a class="mini-button" onclick="closeWindow()" style="width:60px;">取消</a>
    </div>
</body>
</html>
