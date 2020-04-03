//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["hostDetailed"] = mini.get("hostDetailed");
});


function loadPage(param,sort) {
    JsVar["hostDetailed"].clearRows();
    JsVar["hostDetailed"].setData(param);
    JsVar["hostDetailed"].setTotalCount(param.length);
    JsVar["STOCK_DATA"] = JsVar["hostDetailed"].getData();


    if (sort != null && sort != ""){
        changeHostList();
    }else{
        window.parent.initParam(JsVar["hostDetailed"].getData());
    }

}


/**
 * 主机列表change事件
 * @param e
 */
function changeHostList() {
    //选中主机列表
    var hostIp = window.parent.mini.get("HOST_LIST").getValue();

    //任务名称
    var taskName = window.parent.mini.get("TASK_NAME").getValue();
    //执行队列大小
    var execSize = window.parent.mini.get("EXEC_QUENE_SIZE").getValue();

    var tempData = [];
    for (var i=0; i<JsVar["STOCK_DATA"].length; i++) {
        if ((isNotEmptyStr(hostIp) && JsVar["STOCK_DATA"][i]["HOST_IP"].indexOf(hostIp) == -1)) {
            continue;
        }
        if ((isNotEmptyStr(taskName) && JsVar["STOCK_DATA"][i]["TASK_NAME"] != taskName)) {
            continue;
        }
        if (isNotEmptyStr(execSize)) {
            var execNum = execSize.substring(2, execSize.length);
            if (parseInt(JsVar["STOCK_DATA"][i]["EXEC_QUENE_SIZE"]) < parseInt(execNum)) {
                continue;
            }
        }
        tempData.push(JsVar["STOCK_DATA"][i]);
    }
    JsVar["hostDetailed"].clearRows();
    JsVar["hostDetailed"].setData(tempData);
    JsVar["hostDetailed"].setTotalCount(tempData.length);

    //调用排序规则
    changeSortRule();
}



/**
 * 排序规则修改
 * @param e
 */
function changeSortRule() {
    var sortRule = window.parent.mini.get("SORT_RULE").getValue();
    switch(sortRule) {
        case "TASK_ID_ASC":
            sortData("TASK_ID", "ASC");
            break;
        case "TASK_ID_DESC":
            sortData("TASK_ID", "DESC");
            break;
        case "EXEC_QUENE_SIZE_ASC":
            sortData("EXEC_QUENE_SIZE", "ASC");
            break;
        case "EXEC_QUENE_SIZE_DESC":
            sortData("EXEC_QUENE_SIZE", "DESC");
            break;
        case "FILE_QUEUE_SIZE_ASC":
            sortData("FILE_QUEUE_SIZE", "ASC");
            break;
        case "FILE_QUEUE_SIZE_DESC":
            sortData("FILE_QUEUE_SIZE", "DESC");
            break;
        case "MSG_COUNT_ASC":
            sortData("MSG_COUNT", "ASC");
            break;
        case "MSG_COUNT_DESC":
            sortData("MSG_COUNT", "DESC");
            break;
        default:
            break;
    }
}

/**
 * 前台对象排序
 * @param field
 * @param sort
 */
function sortData(field, sort) {
    var stockData = JsVar["hostDetailed"].getData();
    if (stockData != null) {
        if (sort == "ASC") {
            stockData.sort(function(obj1, obj2) {
                return obj1[field] - obj2[field];
            });
        } else {
            stockData.sort(function(obj1, obj2) {
                return obj2[field] - obj1[field];
            });
        }
    }
    JsVar["hostDetailed"].clearRows();
    JsVar["hostDetailed"].setData(stockData);
    JsVar["hostDetailed"].setTotalCount(stockData.length);
}

function numberFormat(e) {
    var count = e.value;
    return formatNumber(count);
}