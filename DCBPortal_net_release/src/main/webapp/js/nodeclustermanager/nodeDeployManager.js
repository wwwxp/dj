
//定义变量， 通常是页面控件和参数
var JsVar = new Object();

$(document).ready(function () {
    mini.parse();
    //程序类型
    JsVar["NODE_TYPE"] = mini.get("NODE_TYPE");
    JsVar["NODE_VERSION"] = mini.get("NODE_VERSION");
    JsVar["nodeGrid"] = mini.get("nodeGrid");
    //初始化获取程序
    initNodeType();
});

/**
 * 初始化获取程序列表
 */
function initNodeType() {
    var params = {};
    getJsonDataByPost(Globals.baseActionUrl.NODE_CLUSTER_DEPLOY_PROGRAM_LIST, params, "节点集群部署-获取程序列表",
        function success(result){
            if (result != null && result["retCode"] == busVar.SUCCESS) {
                JsVar["NODE_TYPE"].setData(result["DATA"]);
            } else {
                showErrorMessageTips("获取程序列表失败，请检查!");
            }
        });
}

/**
 * 查询程序版本上传信息
 */
function changeNodeType() {

    $("#deployResult").html("等待部署...");
    document.getElementById("selectedNum").innerText = "";
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    var params = {
        NODE_TYPE_ID:nodeTypeId
    };

    getJsonDataByPost(Globals.baseActionUrl.NODE_CLUSTER_DEPLOY_VERSION_HOST_LIST, params, "节点集群部署-获取程序版本列表&节点列表",
        function success(result){
            if (result != null && result["retCode"] == busVar.SUCCESS) {

                var versionList = result["VERSION_LIST"];
                mini.get("NODE_VERSION").setData(versionList);
                if (versionList != null && versionList != undefined && versionList.length > 0) {
                    mini.get("NODE_VERSION").setValue(versionList[0]["VERSION_ID"]);
                }

                //获得当前程序类型的已部署的“程序、版本”
                getJsonDataByPost(Globals.ctx + "/nodeClusterDeploy/queryDeployedNodeByNodeType",params,"节点集群部署-查询程序类型对应的已部署的版本",function success(result) {
                    if(result!= null && result!=undefined){
                        JsVar["nodeTypeDeployed"] = result;
                    }

                },null,null,false);

                JsVar["nodeGrid"].setData(result["NODE_LIST"]);

                //如果为WEB程序
                if(result["isWeb"]){
                    $(".webTemplatesRow").css("display","table-cell");
                    mini.get("WEB_TEMPLATES").setData(result["webContainers"]);
                    mini.get("WEB_TEMPLATES").setValue(result["webContainers"][0]["WEB_TEMPLATES"]);
                }else{
                    $(".webTemplatesRow").css("display","none");
                    mini.get("WEB_TEMPLATES").setData("");
                }
            } else {
                showErrorMessageTips("获取程序版本列表&节点失败，请检查!");
            }
        });


}

/**
 * 清空部署结果
 */
function changeRadio(e){
    var nodeGrid=JsVar["nodeGrid"];

    //还原是否采用历史配置
    mini.get("USE_HISTORY_CFG").setValue("true");

    //清空部署结果
    nodeGrid.findRows(function (row) {
                                                            //改变值，从而触发onReferer函数
        nodeGrid.updateRow(row, {"DEPLOY_RESULT": "__"});
        return true;
    });

    //刷新部署情况
    refreshDeployed();

    //清空部署结果
    $("#deployResult").html("等待部署...");

    //清空数量
    document.getElementById("selectedNum").innerText = "";
    //取消勾选中的行
    nodeGrid.clearSelect();
}

/**
 * 刷新部署情况的值
 */
function refreshDeployed() {
    var nodeGrid=JsVar["nodeGrid"];

    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    var params = {
        NODE_TYPE_ID:nodeTypeId
    };
    getJsonDataByPost(Globals.ctx + "/nodeClusterDeploy/queryDeployedNodeByNodeType",params,"节点集群部署-查询程序类型对应的已部署的版本",function success(result) {
        if(result!= null && result!=undefined){
            JsVar["nodeTypeDeployed"] = result;
        }
    });

    nodeGrid.findRows(function (row) {
        //改变值，从而触发onReferer函数
        nodeGrid.updateRow(row, {"DEPLOYED":Math.random()});
        return true;
    });
}


/**
 * 部署操作
 * @param e
 */
var deplyInfoPosition =  new Array();
function onSubmit(e) {
    var versionId = mini.get("NODE_VERSION").getValue();
    if (versionId == null || versionId == undefined || versionId == "") {
        showWarnMessageTips("请选择要部署的版本!");
        return;
    }

    var fileName = null;
    var nodeTypeId = null;
    var versionName = null;
    var nodeTypeCode = null;
    var webTemplates =  mini.get("WEB_TEMPLATES").getValue();
    var versionList = mini.get("NODE_VERSION").getData();
    for (var i=0; i<versionList.length; i++) {
        if (versionList[i]["VERSION_ID"] == versionId) {
            fileName = versionList[i]["FILE_NAME"];
            nodeTypeId = versionList[i]["NODE_TYPE_ID"];
            versionName = versionList[i]["VERSION"];
            nodeTypeCode = versionList[i]["NODE_TYPE_CODE"];
            break;
        }
    }

    var nodeGrid=JsVar["nodeGrid"];
    var nodeList = JsVar["nodeGrid"].getSelecteds();
    if (nodeList != null && nodeList.length > 0) {
        var deployResult=document.getElementById("deployResult");

        //清空部署结果输出
        $("#deployResult").html("");

        //清空部署结果
        nodeGrid.findRows(function (row) {
            nodeGrid.updateRow(row, {"DEPLOY_RESULT": "__"});
            return true;
        });

        deplyInfoPosition = new Array();
        var useHistoryCfg = mini.get("USE_HISTORY_CFG").getValue();
        showLoadMask();
        for (var i=0; i<nodeList.length; i++) {
            var params = {
                NODE_LIST: [nodeList[i]],
                NODE_TYPE_ID:nodeTypeId,
                FILE_NAME:fileName,
                VERSION_NAME:versionName,
                VERSION_ID:versionId,
                NODE_TYPE_CODE: nodeTypeCode,
                WEB_TEMPLATES:webTemplates,
                USE_HISTORY_CFG:useHistoryCfg
            };
            getJsonDataByPost(Globals.baseActionUrl.NODE_CLUSTER_DEPLOY_START_LIST, params, "节点集群部署-节点部署",
                function success(result) {
                    var divDom = document.createElement("div");
                    if (result != null && result["retCode"] == busVar.SUCCESS) {

                       // $("#deployResult").html($("#deployResult").html() + result["retMsg"] + "<br/>");
                        divDom.innerHTML = result["retMsg"];
                        $("#deployResult").append(divDom);
                        $("#deployResult").append("<br/>");
                        nodeGrid.findRows(function (row) {
                            if (row["NODE_ID"] == result["nodeId"]) {
                                nodeGrid.updateRow(row, {"DEPLOY_RESULT": 1,"DEPLOYED":1});
                                return true;
                            }
                        });

                    } else {
                        //$("#deployResult").html($("#deployResult").html() + result["retMsg"] + "<br/>");
                        divDom.innerHTML = result["retMsg"];
                        $("#deployResult").append(divDom);
                        $("#deployResult").append("<br/>");
                        nodeGrid.findRows(function (row) {
                            if (row["NODE_ID"] == result["nodeId"]) {
                                nodeGrid.updateRow(row, {"DEPLOY_RESULT": 0});
                                return true;
                            }
                        });
                    }

                    deplyInfoPosition.push({
                        hostText:result["hostText"],
                        nodePath:result["nodePath"],
                        position:Math.max(0,divDom.offsetTop-parseInt($("#deployResult").css("padding-top")))
                    })
                    //滚动条位置设置
                    deployResult.scrollTop = Math.max(0, deployResult.scrollHeight - deployResult.offsetHeight);
                });
        }
    } else {
        showWarnMessageTips("请选择要部署的节点!");
    }

}

/**
 * 树表点击选中的行时，输出结果定位到”该节点部署信息位置“
 * @param e
 */
function toPosition(e) {
    var hostText=e.record.HOST_TEXT;
    var nodePath=e.record.NODE_PATH;
    var deployResult=document.getElementById("deployResult");
    if (deplyInfoPosition.length > 0) {
        for (var i = 0; i < deplyInfoPosition.length; ++i) {

            if (deplyInfoPosition[i]["hostText"] == hostText && deplyInfoPosition[i]["nodePath"] == nodePath) {
                deployResult.scrollTop = deplyInfoPosition[i]["position"];
            }
        }
    }
    document.getElementById("selectedNum").innerText = JsVar["nodeGrid"].getSelecteds().length+"/"+JsVar["nodeGrid"].getData().length;
}

/**
 * 渲染操作按钮
 *
 * @param e
 * @returns {String}
 */
function onRenderer(e) {
    var flag = e.record.DEPLOY_RESULT;

    if (flag == 1) {
        return "<span class='label label-success' style='letter-spacing:0.2em;'>成功</span>";
    } else if (flag == 0){
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>失败</span>";
    } else {
        return "";
    }
}

/**
 * 判断该节点的该版本是否已经部署
 */
function onDeployedRenderer(e) {

        var versionId = JsVar["NODE_VERSION"].getValue();
        var versionRadioData = JsVar["NODE_VERSION"].getData();
        var version;
        for(var index=0;index<versionRadioData.length;++index){
            if(versionId == versionRadioData[index]["VERSION_ID"]){
                version = versionRadioData[index]["VERSION"];
            }
        }

        var nodeId =  e.record.NODE_ID;
        var deployedValue =  e.record.DEPLOYED;
        var deployed = false;
        for(var i=0;i<JsVar["nodeTypeDeployed"].length;++i){
                if(version == JsVar["nodeTypeDeployed"][i]["VERSION"] && nodeId == JsVar["nodeTypeDeployed"][i]["NODE_ID"]){
                    deployed = true;
                    break;
                }
        }

        if(deployed || deployedValue == 1){
            return "<span class='label label-success' style='letter-spacing:0.2em;'>已部署</span>";
        }else{
            return "<span class='label label-danger' style='letter-spacing:0.2em;'>未部署</span>";
        }
}