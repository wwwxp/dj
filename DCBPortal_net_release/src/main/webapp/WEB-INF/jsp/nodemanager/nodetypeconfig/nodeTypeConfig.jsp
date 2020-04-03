<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DCCP云计费平台</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <%@ include file="/public/common/common.jsp"%>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css" />

    <script language="javascript" type="text/javascript"
            src="${ctx}/js/nodemanager/nodetypeconfig/nodeTypeConfig.js"></script>
</head>
<body>
<div class="mini-fit" style="padding: 5px;">
    <div id="queryForm" class="search">
        <table class="formTable8"
               style="width: 80%; height: 50px; table-layout: fixed; padding: 0"
               cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="100" />
                <col />
                <col width="100"/>
                <col />
                <col width="100"/>
                <col />
                <col width="200">
            </colgroup>
            <tr>
                <th><span>名称：</span></th>
                <td><input class="mini-Textbox" id="NAME"
                           name="NAME" style="width: 100%;"></td>

                <th><span>编码：</span></th>
                <td><input class="mini-Textbox" id="CODE" name="CODE"
                           style="width: 100%;"></td>

                <th><span>业务组：</span></th>
                <td><input class="mini-combobox" id="BUS_GROUP" name="BUS_GROUP"
                           textField="BUS_GROUP" valueField="BUS_GROUP_ID"
                           showNullItem="true" nullItemText="=请选择="
                           style="width: 100%;" allowInput="true"></td>

                <td><a class="mini-button" onclick="search()"
                       style="margin-left: 20px;">查询</a></td>

            </tr>
        </table>
    </div>

    <div class="search2" style="border: 0px;">
        <table id="updateForm" class="formTable9" style="width:100%;padding:0px;height:35px;table-layout: fixed;border: 0px;" cellpadding="0" cellspacing="0">
            <tr>
                <td style="text-align: right;">
                    <a class="mini-button mini-button-green" onclick="addNodeType()"　width="100" plain="false">新增</a>
                    <a class="mini-button mini-button-green" onclick="delNodeType()" plain="false">删除</a>

                </td>
            </tr>
        </table>
    </div>

    <div class="mini-fit" style="margin-top: 0px;">
        <div id="nodeTypeGrid" class="mini-datagrid"
              style="width: 100%; height: 100%"
             idField="ID" allowResize="false" allowCellselect="false"
             multiSelect="true" showFooter="true">
            <div property="columns">
                <div type="checkcolumn"  width="20"></div>
                <div field="ID" visible="false" ></div>
                <div field="NAME" width="60" headerAlign="center"
                     align="center">类型名称</div>
                <div field="CODE" width="60" headerAlign="center"
                     align="center" >类型编码</div>
                <div field="DIFF_CFG_VALUE" width="30" headerAlign="center"
                     align="center">是否区分IP</div>
                <div field="RUN_WEB_VALUE" width="40" headerAlign="center"
                     align="center">是否WEB程序</div>
                <div field="START_VERSION" width="50" headerAlign="center"
                     align="center">开始版本号</div>
                <div field="CURR_VERSION" width="50" headerAlign="center"
                     align="center">当前版本号</div>
                <div field="GROUP_NAME" width="30" headerAlign="center"
                     align="center">业务组</div>
                <div field="DESC" width="100" headerAlign="center"
                     align="left">描述</div>
                <div name="operation" visible="true" field="" headerAlign="center"
                     align="center" renderer="onRenderer" width="80">操作</div>
            </div>
        </div>
    </div>
</div>
</body>
</html>