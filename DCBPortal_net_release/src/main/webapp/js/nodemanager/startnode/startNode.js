//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    //启停程序表格
    JsVar["deployNodeGrid"]=mini.get("deployNodeGrid");

    //程序类型下拉框
    JsVar["NODE_TYPE"]=mini.get("NODE_TYPE");
    //主机IP下拉框
    JsVar["HOST_IP"]=mini.get("HOST_IP");
    //版本下拉框
    JsVar["VERSION"]=mini.get("VERSION");
    JsVar["STATE"]=mini.get("STATE");
    //加载表格
    loadGridTable({});

    //加载下拉框
    refreshCombobox();

});

//页面加载、删除，相关联的下拉框需要更新
function refreshCombobox() {
    JsVar["NODE_TYPE"].setValue("");
    JsVar["HOST_IP"].setValue("");
    JsVar["VERSION"].setValue("");
    loadNodeTypeCombobox();
    loadHostIpCombobox();
    loadVersionCombobox();
}

/**
 * 点击下拉框时，联动进行展示
 */
function onValueChanged() {
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    var nodeHostId = JsVar["HOST_IP"].getValue();
    var params ={
        NODE_TYPE_ID:nodeTypeId==undefined?'':nodeTypeId,
        NODE_HOST_ID:nodeHostId==undefined?'':nodeHostId
    }
    loadHostIpCombobox(params);
    loadVersionCombobox(params);
}

function loadGridTable(param){

    datagridLoadPage(JsVar["deployNodeGrid"],param,"startNodeMapper.queryDeployedNodes");

}

function loadNodeTypeCombobox(){

    comboxLoad( JsVar["NODE_TYPE"],null,"startNodeMapper.queryNodeTypeOnDeploy","","",false);
}

function loadHostIpCombobox(param){

    comboxLoad( JsVar["HOST_IP"],param,"startNodeMapper.queryHostIpOnDeploy","","",false);
}

function loadVersionCombobox(param){

    comboxLoad( JsVar["VERSION"],param,"startNodeMapper.queryVersionOnDeploy","","",false);
}

/**
 * 表格的查找
 */
function search() {

    var param={
        NODE_TYPE_ID:JsVar["NODE_TYPE"].getText()==''?null:JsVar["NODE_TYPE"].getValue(),
        NODE_HOST_ID:JsVar["HOST_IP"].getText()==''?null:JsVar["HOST_IP"].getValue(),
        VERSION:JsVar["VERSION"].getText()==''?null:JsVar["VERSION"].getValue(),
        STATE:JsVar["STATE"].getText()==''?null:JsVar["STATE"].getValue()
    };

    loadGridTable(param);
}

function onRenderer(e) {
    var deployId=e.record.DEPLOY_ID;
    var startId=e.record.START_ID;
    var nodeId=e.record.NODE_ID;
    var version=e.record.VERSION;
    return '<a class="Delete_Button" href="javascript:startNode(\'' + deployId + '\')">启动</a><a class="Delete_Button"  href="javascript:stopNode(\'' + deployId + '\')">停止</a><a class="Delete_Button" href="javascript:checkNode(\'' + deployId + '\',\''+nodeId+'\')">检查</a><a class="Delete_Button" href="javascript:showConfigFile(\'' + nodeId + '\',\''+version+'\')">配置</a><a class="Delete_Button" href="javascript:delNodeVersion(\'' + nodeId + '\',\''+version+'\',\''+deployId+'\')">删除</a>';

}

function onStateRenderer(e) {
    var state=e.record.STATE_NAME

    if (state == "停止") {
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>"
    } else if (state == "运行中") {
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行中&nbsp;</span>"
    }
}

/**
 * 启动程序
 * @param deployId
 */
function startNode(deployId) {

    var deployNodeInfo=new Array();

    //先获得要操作的那几行数据
    if(!deployId){
        var deployNodeInfo=JsVar["deployNodeGrid"].getSelecteds();

        if(deployNodeInfo.length<=0){
            showWarnMessageAlter("请选中一条记录!");
            return;
        }

    }else{
        var allRows=JsVar["deployNodeGrid"].getData();

        for(var i = 0; i < allRows.length; ++i){
            if (allRows[i]["DEPLOY_ID"] == deployId) {
                deployNodeInfo.push(allRows[i]);
                break;
            }
        }
    }

    getJsonDataByPost(Globals.ctx + "/startNode/startNode",deployNodeInfo,"启停程序-启动程序",
        function success(result) {
            if(!result["errorMsg"]){

                var success="";
                var fail="";

                var successNode=result["successNode"];
                var failNode=result["failNode"];

                for (var i = 0; i < successNode.length; ++i) {
                    success += successNode[i] + "<br/><br/>";
                }

                for (var i = 0; i < failNode.length; ++i) {
                    fail += failNode[i] + "<br/><br/>";
                }

                showTip(success+fail+"<br/><div style='font-weight: bold;'>程序启动成功的个数："+successNode.length+"，启动失败的个数："+failNode.length+"</div>");
                JsVar["deployNodeGrid"].reload();
            }else{
                showWarnMessageTips(result["errorMsg"]);
            }
        });
}

/**
 * 停止程序
 * @param startId
 */
function stopNode(deployId) {
    var ids=new Array();

    //先获得要操作的那几行数据
    if (!deployId) {
        var deployNodeInfo = JsVar["deployNodeGrid"].getSelecteds();

        if (deployNodeInfo.length <= 0) {
            showWarnMessageAlter("请选中一条记录!");
            return;
        } else {
            for (var index=0;index<deployNodeInfo.length;++index) {

                //start_id为空，一定为停止状态
                if (deployNodeInfo[index]["DEPLOY_ID"]) {
                    ids.push({DEPLOY_ID: deployNodeInfo[index]["DEPLOY_ID"]});
                }

            }

            if(ids.length==0){
                showWarnMessageTips("选中的程序中，没有正在运行的程序！");
                return;
            }
        }

    } else {

        ids.push({DEPLOY_ID: deployId});
    }

    getJsonDataByPost(Globals.ctx + "/startNode/stopNode",ids,"启停程序-停止程序",
        function success(result) {
            if(!result["errorMsg"]){
                //showMessageTips("成功停止"+result["stopCount"]+"个程序");
                var success="";
                var fail="";

                var successNode=result["successNode"];
                var failNode=result["failNode"];

                for (var i = 0; i < successNode.length; ++i) {

                    success += successNode[i] + "<br/><br/>";
                }

                for (var i = 0; i < failNode.length; ++i) {


                    fail += failNode[i] + "<br/><br/>";
                }

                showTip(success+fail+"<br/><div style='font-weight: bold;'>程序停止成功的个数："+successNode.length+"，停止失败的个数："+failNode.length+"</div>");
                JsVar["deployNodeGrid"].reload();
            }else{
                showWarnMessageTips(result["errorMsg"]);
            }
        });
}

/**
 * 检查程序真正的状态
 * @param deployId
 * @param nodeId
 */

function checkNode(deployId,nodeId) {
     var ids=new Array();
    //先获得要操作的那几行数据
    if (!deployId) {
        var deployNodeInfo = JsVar["deployNodeGrid"].getSelecteds();

        if (deployNodeInfo.length <= 0) {
            showWarnMessageAlter("请选中一条记录!");
            return;
        } else {

            for (var index = 0; index < deployNodeInfo.length; ++index) {

                ids.push(
                    {"DEPLOY_ID": deployNodeInfo[index]["DEPLOY_ID"],
                        "NODE_ID": deployNodeInfo[index]["NODE_ID"]
                    }
                );


            }
        }

    } else {

        ids.push(
            {   "DEPLOY_ID":deployId,
                "NODE_ID":nodeId}
        );

    }

    getJsonDataByPost(Globals.ctx + "/startNode/checkNode",ids,"启停程序-检查程序",
        function success(result) {      //[{"msg":"执行过程","result":"执行结果"}]
            var res="";                 //"&nbsp;&nbsp;&nbsp;&nbsp;"
            for(var i=0;i<result.length;++i){
                res+=result[i]["msg"];

                if(result[i]["updateState"]){
                    JsVar["deployNodeGrid"].reload();
                }

                res+="<br/><br/>";
            }

            showTip(res);

        });
}

/**
 * 打开查看程序的配置文件的页面
 * @param nodeId
 * @param version
 */
function showConfigFile(nodeId,version) {

    showDialog("查看配置文件",1000,"80%", Globals.ctx + "/jsp/nodemanager/startnode/configFile",
        function destroy(data){
        }, {NODE_ID:nodeId,VERSION:version});
    
}

function delNodeVersion(nodeId,version,deployId) {
    var param=new Array();
    var deployIds=new Array();
    if (!nodeId) {
        var rows=JsVar["deployNodeGrid"].getSelecteds();

        if(rows.length<=0){
            showWarnMessageAlter("请选中一条记录!");
            return;
        }

        for(var index=0;index<rows.length;index++){
            param.push({
                NODE_ID: rows[index]["NODE_ID"],
                VERSION: rows[index]["VERSION"]
            });

            deployIds.push(rows[index]["DEPLOY_ID"]);
        }

        deployIds=deployIds.toString();
        param.push({
            DEPLOY_IDS:deployIds
        });

    } else {
        param.push({
            NODE_ID: nodeId,
            VERSION: version
        });

        param.push({
            DEPLOY_IDS:deployId
        });
    }
    showConfirmMessageAlter("删除时会将节点版本目录程序一并删除，是否确认？",function ok(){

        getJsonDataByPost(Globals.ctx + "/startNode/delNodeVersion",param,"程序启停-节点删除",
            function success(result){
                    if(result["effectRow"]!=-1){
                        JsVar["deployNodeGrid"].reload();
                        refreshCombobox();
                        showMessageTips("删除成功！");
                    }else{
                        showWarnMessageTips(result["errorMsg"]);
                    }
            });
    });
}

/**
 *
 * @param index
 */
function showTip(params){
    var paramsHtml="<div id='tipWindow' style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+params+"</div>";
    var options={
        title: "运行结果",
        width:800,
        height:700,
        buttons: ["ok"],
        iconCls: "",
        html: paramsHtml,
        callback: function(action){

        }
    }

    mini.showMessageBox(options);

    var window=document.getElementById("tipWindow");
    //滚动条位置设置
    window.scrollTop = Math.max(0, window.scrollHeight - window.offsetHeight);
}
