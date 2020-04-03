<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/monitor/pressure/nodeEdit.js"></script>
	                                                                
	<title>节点编辑 </title>
	<style >
	  .mini-tree-leaf-png{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/png.gif);}
	  .mini-tree-leaf-gif{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/gif.gif);}
	  .mini-tree-leaf-jpg{   background-image:url(${ctx}/css/vk_style/images/dtree/leaf/jpg.gif);}
	</style>
</head>

<body>
    <div class="mini-fit p5">
        <table id="nodeForm" class="formTable6" style="table-layout: fixed;">
            
             <tr>
                <th width="120"><span class="fred">*</span>节点ID：</th>
                <td>
               <input width="100%"  id="id" name="id" class="mini-textbox" allowInput="false" required="true"/>
                </td>
                 <th width="120"><span class="fred">*</span>节点名称：</th>
                 <td>
                <input width="100%" id="node_name" name="node_name" class="mini-textbox" required="true"/>
                </td>
                </tr>
                <tr>
                <th width="120"><span class="fred">*</span>X坐标：</th>
                <td>
                <input width="100%" id="location_x" name="location_x" class="mini-textbox" required="true" vtype="int"/>
                </td>
                <th width="120"><span class="fred">*</span>Y坐标：</th>
                <td>
               <input width="100%" id="location_y" name="location_y" class="mini-textbox" required="true" vtype="int"/>
                </td>
                </tr>
                <tr>
                <th  width="120"><span class="fred">*</span>节点类型：</th>
                <td>
                 <input width="100%" id="topoNodeType" name="topoNodeType" class="mini-combobox" valueField="code" data="getSysDictData('topolopy_node_type')"  required="true"/>
                </td>
                
                <th width="120">图片URL：</th>
                <td>
                <input width="100%"  id="image_url" name="image_url" class="mini-treeselect"  
                    textField="name" valueField="path" parentField="parentPath"  ondrawnode="onDrawNode" 
                       expandOnLoad="true" popupHeight="150"  popupWidth="250" />
<!--                 <input width="100%" id="image_url" name="image_url" class="mini-textbox" /> -->
                </td>
                </tr>
        </table>
    </div>
    <div class="mini-toolbar" style="height:28px;text-align: center; padding-top: 8px; padding-bottom: 8px;"
         borderStyle="border:0;border-top:solid 1px #b1c3e0;">
        <a class="mini-button" onclick="onSubmit" style="width:60px;margin-right:20px;">暂存</a> <span
            style="display: inline-block; width: 25px;"></span> <a class="mini-button" onclick="onCancel" style="width:60px;">取消</a>
    </div>
</body>
</html>