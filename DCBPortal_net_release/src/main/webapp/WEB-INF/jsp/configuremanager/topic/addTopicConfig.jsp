<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>新增Topic配置</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />
	<script language="javascript" type="text/javascript" src="${ctx}/js/configuremanager/topic/addTopicConfig.js"></script>
</head>
<body>
 	<div class="mini-fit p5">
		<table id="topicForm" class="formTable6" style="table-layout: fixed;">
			 <colgroup>
				<col width="100"/>
				<col/>
				<col width="100"/>
				<col/>
			</colgroup>
			<tr>
            	<th>
            		<span class="fred"></span>Topology名称：
            	</th>
				<td>
					<input id="topology_id" name="topology_id"  class="mini-combobox" 
						showNullItem="true"  nullItemText=""  emptyText=""
						textField="PROGRAM_NAME" valueField="PROGRAM_VALUE" style="width:100%;" onvaluechanged="changeTopology()" />
				</td>
				<th>
            		 
            	</th>
				<td>
					 
				</td>
				<!-- <th>
            		<span class="fred"></span>Topology属性：
            	</th>
				<td>
					<input id="topology_attr" name="topology_attr"  class="mini-combobox" 
						showNullItem="true"  nullItemText=""  emptyText=""
						textField="text" valueField="code" style="width:100%;" />
				</td> -->
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>Topic名称：
            	</th>
				<td colspan="3">
					<input id="topicName" name="topicName"  required="true" class="mini-textbox" style="width:100%;" />
				</td>
				<!-- <th>
            		<span class="fred">*</span>RocketMQ IP：
            	</th>
				<td>
					<input id="rq_cluster" name="rq_cluster" required="true" class="mini-combobox" 
						textField="CLUSTER_NAME" valueField="CLUSTER_ID" style="width:40%;" valuechanged="changeCluster" />
					<input id="rq_ip" name="rq_ip" required="true" class="mini-combobox" 
						textField="NAME" valueField="IP" style="width:40%;" />
					&nbsp;&nbsp;PORT：<input id="rq_port" name="rq_port"  required="true" vtype="int" maxlength="5" value="9876" class="mini-textbox" style="width:20%;" />
				</td> -->
			</tr>
			<tr>
				<th>
            		<span class="fred">*</span>RocketMQ：
            	</th>
				<td colspan="3">
					<input id="rq_cluster" name="rq_cluster" required="true" class="mini-combobox" 
						textField="CLUSTER_NAME" valueField="CLUSTER_ID" style="width:30%;" valuechanged="changeCluster" />
					<input id="rq_version" name="rq_version" required="true" class="mini-combobox" 
						textField="VERSION" valueField="VERSION" style="width:20%;" valuechanged="changeVersion" />
					<input id="rq_ip" name="rq_ip" required="true" class="mini-combobox" 
						textField="NAME" valueField="HOST_IP" style="width:30%;" />
					&nbsp;&nbsp;
					PORT：<input id="rq_port" name="rq_port"  required="true" vtype="int" 
						maxlength="5" value="9876" class="mini-textbox" style="width:10%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred">*</span>可读队列数：
            	</th>
				<td>
					<input id="r_num" name="r_num"  class="mini-spinner" minValue="1" maxValue="999" style="width:100%;" />
				</td>
				<th>
            		<span class="fred">*</span>可写队列数：
            	</th>
				<td>
					<input id="w_num" name="w_num"  class="mini-spinner" minValue="1" maxValue="999" style="width:100%;" />
				</td>
			</tr>
			<tr>
            	<th>
            		<span class="fred"></span>备注信息：
            	</th>
				<td colspan="3">
					<input id="topicDesc" name="topicDesc"  class="mini-textarea" style="width:100%;" />
				</td>
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
