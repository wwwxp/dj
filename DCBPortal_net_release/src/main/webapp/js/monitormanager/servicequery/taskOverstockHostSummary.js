//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["hostSummary"] = mini.get("hostSummary");
});


function loadPage(param) {

    // 主机去重，消息总量相加
    var fileArray =[],parm_ip;
    for (var i = 0; i< param.length; i++){
        parm_ip = param[i].HOST_IP.substr(0,param[i].HOST_IP.indexOf(":"));
        var flag = true;
        if(i == 0){
            fileArray.push({"HOST_IP":parm_ip,"TASK_NAME":param[i].TASK_NAME,"MSG_COUNT": param[i].MSG_COUNT});
            continue;
        }
        for (var j = 0 ;j <fileArray.length; j++){

            if(fileArray[j].HOST_IP == parm_ip){
                var ipResult = fileArray.findIndex(function (value) {
                    return value.HOST_IP === parm_ip;
                });
                fileArray[ipResult].MSG_COUNT = parseInt(fileArray[ipResult].MSG_COUNT) + parseInt(param[i].MSG_COUNT);

                if (fileArray[j].TASK_NAME != param[i].TASK_NAME){
                    fileArray[ipResult].TASK_NAME = fileArray[ipResult].TASK_NAME + "," + param[i].TASK_NAME;
                }
                flag = false;
                continue;
            }

        }
        if (flag){
            fileArray.push({"HOST_IP":parm_ip,"TASK_NAME":param[i].TASK_NAME,"MSG_COUNT": param[i].MSG_COUNT});
        }
    }

    JsVar["hostSummary"].clearRows();
    JsVar["hostSummary"].setData(fileArray);
    JsVar["hostSummary"].setTotalCount(fileArray.length);
}

function numberFormat(e) {
    var count = e.value;
    return formatNumber(count);
}