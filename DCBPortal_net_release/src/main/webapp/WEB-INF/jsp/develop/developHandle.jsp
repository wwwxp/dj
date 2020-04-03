<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.tydic.util.SessionUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>二次开发</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/develop/developHandle.js"></script>
	<style >
	  .mini-tree-leaf-css{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/css.gif);}
	  .mini-tree-leaf-html{  background-image:url(${ctx}/css/vk_style/images/dtree/leaf/html.gif);}
	  .mini-tree-leaf-js{    background-image:url(${ctx}/css/vk_style/images/dtree/leaf/js.gif);}
	  .mini-tree-leaf-txt{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/txt.gif);}
	  .mini-tree-leaf-xml{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/xml.gif);}
	  .mini-tree-leaf-zip{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/zip.gif);}
	  .mini-tree-leaf-java{  background-image:url(${ctx}/css/vk_style/images/dtree/leaf/java.gif);}
	  .mini-tree-leaf-sql{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/sql.gif);}
	  .mini-tree-leaf-png{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/png.gif);}
	  .mini-tree-leaf-gif{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/gif.gif);}
	  .mini-tree-leaf-jpg{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/jpg.gif);}
	  .mini-tree-leaf-ico{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/ico.gif);}
	  .mini-tree-leaf-db{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/db.gif);}
	</style>
	<script type="text/javascript">
	 var templatePath='<%=java.net.URLEncoder.encode(SessionUtil.getConfigValue("WEB_HOST_ROOT_DIR")+"template")%>';
	</script>
</head>
<body>
<div class="mini-fit p5">
	<div class="mini-splitter"  style="width: 100%; height: 100%;" borderStyle="border:0;">

    <div size="300" showCollapseButton="true" minSize="300">
        <div class="mini-toolbar" style="padding: 2px;border-right: #b1c3e0 0px solid; height: 30px;">
            <table style="width: 100%;height: 100%;">
                <tr>
                    <td>
                        <input id="node_name" name="node_name" style="width: 120px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
                        <a class="mini-button" style="width:50px;" plain="false" onclick="searchTree()">查找</a>
                        <a class="mini-button" style="width:50px;" plain="false" onclick="refresh();">刷新</a>
                         <a class="mini-button" style="width:50px;" plain="false" onclick="help();">帮助</a>
                    </td>
                </tr>
            </table>
        </div>
     
        <div class="mini-fit" style="border: #b1c3e0 1px solid;border-right: #b1c3e0 0px solid;">
            <ul id="fileTree" class="mini-tree"
                style="width: 100%; height: 99%" showTreeIcon="true" textField="name" contextMenu="#tree_left_menu"
                showTreeIcon="true" idField="id" parentField="parentId" resultAsTree="false" ondrawnode="onDrawNode"
                expandOnLoad="0" onNodeclick="onClickTreeNode()">
            </ul>
            <ul id="tree_left_menu" class="mini-contextmenu" onbeforeopen="onBeforeOpen">
                <li id="newFile" iconCls="icon-node" onclick="newFile('file')">新建文件</li>
                <li id="newTopology" iconCls="icon-node" onclick="newFile('topology')">新建topology</li>
                <li id="newDirectory" iconCls="icon-addfolder" onclick="newFile('directory')">新建目录</li>
                <li id="uploadFile" iconCls="icon-upload" onclick="uploadFile()">上传文件</li>
                <li id="rename" iconCls="icon-reload" onclick="renameFile">重命名</li>
                <li id="delete" iconCls="icon-remove" onclick="deleteFile">删除</li>
                <li id="release" iconCls="icon-db-commit" onclick="releaseFile">发布</li>
            </ul>
        </div>
    </div>
    <div style="border: #b1c3e0 1px solid; ">
         <div id="mainDevelopPage" class="mini-tabs" activeIndex="0" onbeforecloseclick="beforecloseTab" onactivechanged="activechangedTab" 
			style="width: 100%; height: 100%">
		</div>
   </div>
</div>

</div>
</body>
</html>
