
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();


/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
 	//表格获取
    JsVar["datagrid"] = mini.get("datagrid");


});

function query(){
    loaddatagrid( JsVar["data"])
}
function onLoadComplete(data) {
	  JsVar["data"] = data;
	$('#job_id_label').html( JsVar["data"]["JOB_ID"]);
    $('#job_name_label').html( JsVar["data"]["TASK_NAME"]);
    loaddatagrid( JsVar["data"])
}

function onStatusRenderer(e) {
    var EXEC_RESULT_STATUS = e.record.EXEC_RESULT_STATUS;
    if (EXEC_RESULT_STATUS == '0') {
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;失败&nbsp;</span>";
    } else if(EXEC_RESULT_STATUS == '3'){
        return "<span class='label label-info' style='letter-spacing:0.2em;'>&nbsp;运行中&nbsp;</span>";
    } else {
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;成功&nbsp;</span>";
    }
}
 
/**
 * 加载定时任务表格
 */
function loaddatagrid(paramsObj){
	//加载表格信息
	datagridLoadPage(JsVar["datagrid"],paramsObj,"joblist.queryJobLogList");
}


