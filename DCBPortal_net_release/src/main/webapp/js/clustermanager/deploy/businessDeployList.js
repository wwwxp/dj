//定义变量， 通常是页面控件和参数
var JsVar = new Object();

$(document).ready(function() {
    mini.parse();
});

//父页面调用
function onLoadComplete(data) {
    JsVar["DATA"] = data;

    //查询实例
    init();
}

function init() {
    var params = {
        BUS_CLUSTER_ID : JsVar["DATA"]["BUS_CLUSTER_ID"],
        CLUSTER_ID : JsVar["DATA"]["CLUSTER_ID"],
        CLUSTER_FLAG : JsVar["DATA"]["CLUSTER_FLAG"],
        PROGRAM_CODE : JsVar["DATA"]["SUB_PROGRAM"]
    };
    if (JsVar["DATA"]["SHOW_FLAG"] == "HOST") {   //主机列表
        $("#busDiv").hide();
        $("#compDiv").hide();
        $("#hostDiv").show();
        mini.parse();
        datagridLoad(mini.get("hostGrid"), params, "deployView.queryDeployViewHostInfoList");
    } else {  //实例列表
        initInstList(params);
    }
}

/**
 * 查询实例列表
 */
function initInstList(params) {
    $("#hostDiv").hide();
    //组件实例
    if (JsVar["DATA"]["CLUSTER_FLAG"] == '1') {
        $("#busDiv").hide();
        $("#compDiv").show();
        mini.parse();
        datagridLoad( mini.get("compInstGrid"), params, "deployView.queryComponentDeployViewListByClusterId");
    } else {
        //业务程序实例
        $("#busDiv").show();
        $("#compDiv").hide();
        mini.parse();
        //实例列表
        datagridLoad(mini.get("busInstGrid"), params, "deployView.queryBusDeployViewListByClusterId");
    }
}