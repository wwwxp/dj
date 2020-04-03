<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>配置修改</title>
    <%@ include file="/public/common/common.jsp" %>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css"/>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/nodemanager/addEditCluster.css"/>
    <script language="javascript" type="text/javascript"
            src="${ctx}/js/nodemanager/clustermanager/jquery.nicescroll.js"></script>
    <script language="javascript" type="text/javascript"
            src="${ctx}/js/nodemanager/clustermanager/addEditCluster.js"></script>

</head>
<body>

<div class="mini-fit" style="width:100%;height:95%;">
    <table class="formTable8" cellpadding="0" cellspacing="0">
        <colgroup>
            <col width="80"/>
            <col />
            <col width="80"/>
            <col />
            <col width="80"/>
            <col />
        </colgroup>
        <tr>
            <td><span class="fred">*</span>名称：</td>
            <td>
                <input id="clusterId" class="mini-hidden"/>
                <input width="100%" id="clusterName" class="mini-textbox"
                       required="true" emptyText="集群名称"/>
            </td>
            <td><span class="fred">*</span>编码：</td>
            <td>
                <input width="100%" id="clusterCode" class="mini-textbox"
                       required="true" emptyText="集群编码"/>
            </td>
            <td>描述：</td>
            <td>
                <input width="100%" id="clusterDesc" class="mini-textbox"
                        emptyText="集群描述"/>
            </td>
        </tr>
        <%--<tr>--%>

            <%--<td id="addTypeCell" colspan="2" style="text-align: left;padding-left:80px;">--%>
                <%--<span  class='label label-success'  onclick="addNodeType(this)"></span>--%>
            <%--</td>--%>
        <%--</tr>--%>

        <tr id="nodeTypeModule" style="display: none;">
            <td>节点类型：</td>
            <td id="nodeTypeTd">

            </td>

            <td>别名: </td>
            <td id="aliasTd">

            </td>

            <td colspan="2" style="text-align: left;padding-left:80px;">
                <span class='label label-danger' id="tagIndexSpan" xstyle='letter-spacing:0.2em;' onclick="delNodeType(this)"></span>
            </td>
        </tr>


    </table>
</div>
<div class="mini-toolbar" style="height:5%;text-align: center; padding-top: 8px; padding-bottom: 8px;"
     borderStyle="border:0;border-top:solid 1px #b1c3e0;">
    <a class="mini-button" onclick="onSubmit" style="width:80px;margin-right:20px;">提交</a>
    <a class="mini-button" onclick="onCancel" style="width:80px;margin-right:20px;">取消</a>
</div>
</body>
</html>