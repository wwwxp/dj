//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["CLUSTER_NAME"] = mini.get("CLUSTER_NAME");
    JsVar["QRY_CLUSTER_NAME"] = mini.get("QRY_CLUSTER_NAME");

    JsVar["queryForm"] = new mini.Form("queryForm");
    JsVar["expendForm"] = new mini.Form("expendForm");
    JsVar["expendGrid"] = mini.get("expendGrid");
    initClusterList();
    query();
});

//查询数据
function query() {
    var params = JsVar["queryForm"].getData(true);
    datagridLoadPage(JsVar["expendGrid"], params, "expendCfgMapper.queryExpendCfgList");

}

//删除操作
function delExpend() {
    var delData = JsVar["expendGrid"].getSelected();
    if (delData != null) {
        showConfirmMessageAlter("确定删除记录？", function ok() {
            getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL, [delData], "伸缩扩容-删除数据",
                function (result) {
                    JsVar["expendGrid"].reload();
                    showMessageAlter("数据删除成功")
                }, "expendCfgMapper.delExpendCfg");

        });
    } else {
        showWarnMessageAlter("请选择要删除的数据")
    }
}

/**
 * 查询集群列表数据
 */
function initClusterList() {
    var params = {};
    comboxLoad(JsVar["CLUSTER_NAME"] , params, "serviceType.queryServiceTypeList");
    comboxLoad(JsVar["QRY_CLUSTER_NAME"] , params, "serviceType.queryServiceTypeList");
}

/**
 * 修改类型
 */
function changeType() {
    var expendType = mini.get("EXPEND_TYPE").getValue();
    if (expendType == "2") {
        $("#titleTr").show();
        $("#titleRet").show();
    } else {
        $("#titleTr").hide();
        $("#titleRet").hide();
    }
    mini.parse();
}


function onTypeRenderer(e) {
    var expendType = e.record.EXPEND_TYPE;
    if (expendType == "1") {
        return "立即扩容";
    } else {
        return "定时扩容"
    }
}

function onStateRenderer(e) {
    var expendState = e.record.EXPEND_STATE;
    if (expendState == "1") {
        return "已处理";
    } else {
        return "未处理"
    }
}

/**
 * 确定
 */
function addExpend() {
    //判断是否有效
    JsVar["expendForm"].validate();
    if (JsVar["expendForm"].isValid() == false){
        return;
    }
    var expendType = mini.get("EXPEND_TYPE").getValue();
    if (expendType == "2") {
        var startTime = mini.get("EXPEND_TIME").getValue();
        if (startTime == null || typeof(startTime) == 'undefined') {
            showWarnMessageAlter("请选择扩缩时间!!");
            return;
        }
    }
    var params = JsVar["expendForm"].getData(true);
    if (params["EXPNED_TYPE"] == "1" && (params["EXPEND_TIME"] == null || typeof(params["EXPEND_TIME"]) == "undefined")) {
        params["EXPEND_TIME"] = '';
    }

    getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL, [params], "伸缩扩容-添加数据",
        function (result) {
            JsVar["expendGrid"].reload();
            showMessageAlter("数据添加成功")
        },"expendCfgMapper.addExpendCfg");
}