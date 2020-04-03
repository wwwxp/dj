<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>DCCP云计费平台</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <%@ include file="/public/common/common.jsp" %>
    <link rel="stylesheet" type="text/css" href="${ctx}/css/form/form.css"/>
    <script type="text/javascript"
            src="${ctx}/js/common/dynamicMergeCells.js"></script>
</head>
<body>
<div style="padding: 5px;">
    <div id="queryForm" class="search">
        <table class="formTable8"
               style="width: 100%; height: 50px; table-layout: fixed; padding: 0"
               cellpadding="0" cellspacing="0">
            <colgroup>
                <col width="100"/>
                <col/>
                <col width="100"/>
                <col/>
            </colgroup>
            <tr>
                <th><span>程序类型：</span></th>
                <td><input class="mini-combobox" id="CODE" allowInput="true"
                           name="CODE" style="width: 100%;" valueField="ID"
                           textField="NODE_CLUSTER_NAME"></td>

                <td><a class="mini-button" onclick="search()"
                       style="margin-left: 20px;">查询</a></td>
                <td><a class="mini-button" onclick="refreshState()"
                       style="margin-left: 20px;">刷新状态</a></td>
                <td>&nbsp;</td>
            </tr>
        </table>
    </div>
    <div style="width: 100%;position: absolute;top: 60px; bottom: 0px; left: 0px; ">
        <iframe frameborder="0" style="width: 100%;height: 100%;" id="myiframe">

        </iframe>
    </div>
</div>
<script type="application/javascript">
    /**
     * 定义变量， 通常是页面控件和参数
     */
    var JsVar = new Object();


    //初始化
    $(document).ready(function () {
        mini.parse();
        //表格
        JsVar["CODE"] = mini.get("CODE");
        initCombox();
    });

    function initCombox() {
        getJsonDataByPost(Globals.ctx + "/nodeClusterManager/queryNodeClusterConfig", null, "",
            function (result) {
                if (result.length > 0) {
                    result.unshift({"TYPE_INFO": "全部", "CODE": ""});
                    JsVar["CODE"].setData(result);
                }
            });
    }

    function refreshState() {
        var id = JsVar["CODE"].getValue();
        if (id == null || id == undefined || id == '') {
            return;
        }
        getJsonDataByPost(Globals.ctx + "/nodeClusterManager/refreshClusterState", {'CLUSTER_ID': id}, "",
            function (result) {
                //[{"msg":"执行过程","result":"执行结果"}]
                var res = "";                 //"&nbsp;&nbsp;&nbsp;&nbsp;"
                for (var i = 0; i < result.length; ++i) {
                    res += result[i]["msg"];

                    // if (result[i]["updateState"]) {
                    //     JsVar["deployNodeGrid"].reload();
                    // }

                    res += "</br></br>";
                }

                showTip(res);
                search();

            }, null, null);
    }

    function search() {
        var id = JsVar["CODE"].getValue();
        if (id == null || id == undefined || id == '') {
            return;
        }
        $("#myiframe").attr("src", Globals.ctx + "/jsp/nodemanager/clustermanager/cluserTreeMapShowView?id=" + id);
    }



    /**
     *
     * @param index
     */
    function showTip(params) {
        var paramsHtml = "<div id='tipWindow' style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>" + params + "</div>";
        var options = {
            title: "运行结果",
            width: 800,
            height: 700,
            buttons: ["ok"],
            iconCls: "",
            html: paramsHtml,
            callback: function (action) {

            }
        }

        mini.showMessageBox(options);

        var window = document.getElementById("tipWindow");
        //滚动条位置设置
        window.scrollTop = Math.max(0, window.scrollHeight - window.offsetHeight);
    }

    function onLoadComplete(data) {
        if(data!=undefined && data["action"] === "showView"){
            mini.get("CODE").setValue(data["clusterId"]);
            search();
        }
    }

</script>
</body>
</html>