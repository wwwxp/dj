<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp"%>
	<script src="${ctx}/js/dynamicNodeexpend/thresholdConfig.js" type="text/javascript"></script>
	<link href="${ctx}/css/form/form.css" rel="stylesheet" type="text/css" />
	<title>阀值配置</title>
</head>
<body>
    <div class="mini-fit p5">
        <table id="thresholdForm" class="formTable6" style="table-layout: fixed;">
            <colgroup>
                <col width="120px" />
                <col />  
            </colgroup>
            <tr>
                <th><span class="fred">*</span>指标类型：</th>
                <td>
                	<input width="100%" id="QUOTA_TYPE" name="QUOTA_TYPE" class="mini-combobox" textField="text" valueFiled="code"
                    	data="getSysDictData('quota_dy_type')" value="1" required="true" onvaluechanged="onqtaTypeChange"/>
                </td>
            </tr>
            <tr>
                <th><span class="fred">*</span>条件类型：</th>
                <td>
                	<input width="60%" id="CONDITION_PARAM" name="CONDITION_PARAM" class="mini-combobox" 
                		textField="text" valueFiled="code" value="1" required="true"/>
                    <input width="34%" id="CONDITION_VALUE" name="CONDITION_VALUE" class="mini-textbox" required="true" vtype="float"/><span id="SPAN_TIP">%</span>
					</td>
                </td>
            </tr>
           <!--  <tr>
                <th><span class="fred">*</span>连续次数：</th>
                <td>
                	<input id="CONDITION_COUNT" name="CONDITION_COUNT" class="mini-spinner"  
                		minValue="1" maxValue="100" value="5" style="width:100%;"/>
                </td>
            </tr> -->
           <!--  <tr>
                <th><span class="fred">*</span>一次性扩展节点数：</th>
                <td>
                	<input id="HOST_COUNT" name="HOST_COUNT" class="mini-spinner"  
                		minValue="1" maxValue="100" value="1" style="width:100%;" />
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