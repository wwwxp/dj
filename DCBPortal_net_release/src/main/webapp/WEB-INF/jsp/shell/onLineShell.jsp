<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>在线shell</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/shell/onLineShell.js"></script>
	
	
</head>
<body>
<div class="mini-fit p5">
	<div class="mini-splitter"  style="width: 100%; height: 100%;" borderStyle="border:0;">

    <div size="300" showCollapseButton="true" style="border: 1;" minSize="300">
        <div class="mini-toolbar" style="padding: 2px; border-right: 0;height: 30px;">
            <table style="width: 100%;height: 100%;">
                <tr>
                    <td>
                        <input id="node_name" name="node_name" style="width: 120px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
                        <a class="mini-button"  plain="false" onclick="searchTree()">查找</a>
                        <a class="mini-button" plain="false" onclick="refresh();">刷新</a>
                    </td>
                </tr>
            </table>
        </div>
     
        <div class="mini-fit">
            <ul id="shellTree" class="mini-tree"
                style="width: 100%; height: 99%" showTreeIcon="true" textField="text" contextMenu="#tree_left_menu"
                showTreeIcon="true" idField="id" parentField="parentId" resultAsTree="false" ondrawnode="onDrawNode"
                expandOnLoad="0"
                onNodeclick="onClickTreeNode()">
            </ul>
            <ul id="tree_left_menu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">
                <li id="newFile" iconCls="icon-node" onclick="newHostLink">新建连接</li>
                <li id="delete" iconCls="icon-remove" onclick="deleteFile">删除</li>
            </ul>
        </div>
    </div>
    <div>
         <div id="mainShellPage" class="mini-tabs" activeIndex="0" onbeforecloseclick="beforecloseTab" onactivechanged="activechangedTab" 
			style="width: 100%; height: 100%">
		</div>
   </div>
   
</div>

</div>

</body>
</html>
