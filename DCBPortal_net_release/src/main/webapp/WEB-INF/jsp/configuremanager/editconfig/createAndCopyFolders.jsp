<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
    <script language="javascript" type="text/javascript"
            src="${ctx}/js/configuremanager/editconfig/createAndCopyFolders.js"></script>
    <title>新建实例</title>
</head>
<body>
<div style="height:390px;display:block;overflow-y:auto;" class="mini-fit p5" id="total">
    <span class="fred">1、勾选IP地址，端口生成对应Redis配置文件实例<br/></span>
    <span class="fred">2、勾选IP地址后，至少需要勾选一个当前IP对应的端口，支持批量快速选择一台主机所有端口</span>
    <table id="fileForm" class="formTable6" style="table-layout: fixed;margin-top:10px;">

    </table>
</div>
<div class="mini-toolbar"
     style="height: 28px; text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
    <a class="mini-button" onclick="addBatchFile()"
       style="width: 60px; margin-right: 20px;">确定</a>
    <span style="display: inline-block; width: 25px;"></span>
    <a class="mini-button" onclick="closeWindow(systemVar.FAIL)" style="width: 60px;">取消</a>
</div>
</body>
</html>