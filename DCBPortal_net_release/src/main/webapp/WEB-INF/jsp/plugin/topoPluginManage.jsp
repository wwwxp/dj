<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp" %>
    <script src="${ctx}/js/plugin/topoPluginManage.js" type="text/javascript"></script>
    <link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css"/>
</head>
<body>

<div class="mini-fit p5">
		<!-- TAB页 -->
		<div id="switchTabs" class="mini-tabs" activeIndex="0"
			style="width: 100%; height: 40px; padding: 0px;" plain="false"
			tabAlign="left" tabPosition="top" onactivechanged="loadPage"></div>
			
		<div id="queryForm" class="search">
			<table class="formTable8" style="width:100%;height:36px;table-layout: fixed; padding: 0" cellpadding="0" cellspacing="0">
				<colgroup>
					<col width="100"/>
					<col width="200"/>
					<col width="50"/>
					<col width="200"/>
					<col/>
				</colgroup>
				<tr>
	            	<th>
	            		<span>topology类型：</span>
	            	</th>
					<td>
						<input class="mini-combobox" id="topologyType" name="topologyType" style="width:100%;"
                          	valueField="CLUSTER_TYPE" textField="CLUSTER_TYPE" />
					</td>
	            	<th>
	            		<span>版本：</span>
	            	</th>
					<td>
						<input class="mini-combobox" id="topologyVersion" name="topologyVersion" 
							valueField="NAME" textField="VERSION" style="width:100%;"/>
					</td>
	                <td><a class="mini-button" onclick="search()" style="margin-left: 20px;">查询</a></td>
				</tr>
			</table>
		</div>
		
		<div class="mini-fit" style="margin-top:5px;">
	    	<div class="mini-splitter" style="width: 100%; height: 100%;" borderStyle="border:0;">
		        <div size="300" showCollapseButton="true" style="border:#b1c3e0 1px solid;border-top: none;" minSize="300">
		            <div class="mini-toolbar" style="padding: 2px; border-right: 0px;border-left:0px; height: 30px;">
		                <table style="width: 100%;height: 100%;">
		                    <tr>
		                        <td>
		                            <input id="node_name" name="node_name" style="width: 180px;" class="mini-textbox" onenter="search" emptyText="输入名称搜索"/>
		                            <a class="mini-button" style="width:50px;" plain="false" onclick="searchTree()">查找</a>
		                            <a class="mini-button" style="width:50px;" plain="false" onclick="refreshTree()">刷新</a>
		                        </td>
		                    </tr>
		                </table>
		            </div>
		            <div class="mini-fit">
		                <ul id="data_tree" class="mini-tree" imgField="img"
		                    style="width: 100%; height: 99%" showTreeIcon="true" textField="desc"  
		                    showTreeIcon="true" idField="id" parentField="parentId" resultAsTree="false"
		                    expandOnLoad="0" imgPath="images/" 
		                    onNodeclick="onClickTreeNode()">
		                </ul>
		            </div>
		        </div>
		        
		        <div style="border:#b1c3e0 1px solid;">
		            <table id="data_form" width="100%" class="formTable6" style="table-layout: fixed;border: none;">
		                <colgroup>
		                    <col width="100px" />
		                    <col />
		                </colgroup>
		                <tr>
		                    <th style="border-left: none;border-top: none;">key名称：</th>
		                    <td style="border-right: none;"><div id="name" /></td>
		                </tr>
		                <tr>
		                    <th style="border-left: none;"><span class="fred">*</span>版本号：</th>
		                    <td style="border-right: none;">  <div id="version"   /></td>
		                </tr>
		                <tr>
		                    <th style="border-left: none;">类型：</th>
		                    <td style="border-right: none;"><div id="category"/></td>
		                </tr>
		                <tr>
		                    <th style="border-left: none;">组件描述：</th>
		                    <td style="border-right: none;"><div id="desc"/></td>
		                </tr>	
		                <tr>
		                    <th style="border-left: none;">库文件：</th>
		                    <td style="border-right: none;"><div id="so"   /></td>
		                </tr>
		                 <tr>
		                    <th style="border-left: none;">版本描述：</th>
		                    <td style="border-right: none;">
		                    	<input class="mini-textarea" id="soDesc" style="width: 100%;min-height: 180px;"/>
		                    </td>
		                </tr>
		            </table>
		        </div>
		    </div>
	    </div>
	</div>
</body>
</html>
