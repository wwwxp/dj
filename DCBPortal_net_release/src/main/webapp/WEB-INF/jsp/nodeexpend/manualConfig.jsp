<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/nodeexpend/manualConfig.js" type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>定时配置</title>
</head>
<body>
    <div class="mini-fit p5">
        <table id="timingForm" class="formTable6" style="table-layout: fixed;">
            <colgroup>
                <col width="120px" />
                <col />
            </colgroup>
           
            <tr>
                <th><span class="fred">*</span><span id="hostCountDesc">一次性扩展节点数：</span></th>
                <td>
                	<input id="HOST_COUNT" name="HOST_COUNT" class="mini-spinner" required="true"  
                		minValue="1" maxValue="100" value="1" style="width:100%;" />
                </td>
            </tr>
             <tr>
                <th><span class="fred">*</span><span id="backupHostsDesc">扩展节点：</span></th>
                <td>
                	<input id="BACKUP_HOSTS" name="BACKUP_HOSTS" class="mini-lookup" style="width:100%;" 
				        textField="HOST_IP" valueField="HOST_IP" popupWidth="auto" allowInput="false"
				        popup="#gridPanel" grid="#datagrid1" multiSelect="true" required="true"/>
				        
				        <div id="gridPanel" class="mini-panel" title="header" iconCls="icon-add" style="width:550px;height:250px;" 
        showToolbar="true" showCloseButton="true" showHeader="false" bodyStyle="padding:0" borderStyle="border:0" 
    >
        <div property="toolbar" style="padding:5px;padding-left:8px;text-align:center;">   
            <div style="float:left;padding-bottom:2px;">
                <span>IP：</span>                
                <input id="keyText" name="keyText" class="mini-textbox" style="width:160px;" onenter="onSearchClick"/>
                <a class="mini-button" onclick="onSearchClick">查询</a>
                <a class="mini-button" onclick="onClearClick">清除</a>
            </div>
            <div style="float:right;padding-bottom:2px;">
                <a class="mini-button" onclick="onCloseClick">关闭</a>
            </div>
            <div style="clear:both;"></div>
        </div>
        <div id="datagrid1" class="mini-datagrid" style="width:100%;height:100%;" 
            borderStyle="border:0" showPageSize="false" showPageIndex="true" multiSelect="true"
        >
            <div property="columns">
                <div type="checkcolumn" width="5"></div>
                <div field="HOST_IP" headerAlign="center" width="20" align="left">IP</div> 
                <div field="SSH_USER" headerAlign="center" width="15" align="left">用户名</div> 
				<div field="CORE_COUNT" headerAlign="center" width="10" align="left">CPU核心数</div>
				<div field="MEM_SIZE" headerAlign="center" width="10" align="left">内存大小</div>
				<div field="STORE_SIZE"  headerAlign="center" width="10" align="center">存储大小</div>
            </div>
        </div>  
    </div>	
    
                </td>
            </tr>
           <!--  <tr>
                <th><span class="fred">*</span>定时执行的任务：</th>
                <td>
                	<input id="CRON_EXP" name="CRON_EXP" required="true" class="mini-buttonedit" onbuttonclick="onButtonEdit" width="100%"/>
                </td>
            </tr> -->
            
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