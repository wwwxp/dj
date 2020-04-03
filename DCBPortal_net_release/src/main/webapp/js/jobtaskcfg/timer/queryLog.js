
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

function onLoadComplete(data) {
	loaddatagrid(data)
}

 
/**
 * 加载定时任务表格
 */
function loaddatagrid(paramsObj){
	//加载表格信息
	datagridLoadPage(JsVar["datagrid"],{JOB_ID:paramsObj["ID"]},"joblog.queryJobLogList");
}


function execTypeRenderer(e){
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if(EXEC_TYPE == 'cmd'){
		return "命令模式";
	}else{
		return "数据库模式";
	}
}
function paramsRenderer(e){
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if(EXEC_TYPE == 'cmd'){
		return e.record.CMD_CONTENT;
	}else{
		return e.record.TASK_CONTENT;
	} 
}


/**
 * 状态渲染
 */
function taskTypeRenderer(e){
	var TASK_TYPE = e.record.TASK_TYPE;
	if(TASK_TYPE==2){
		return "循环调度";
	}else if(TASK_TYPE==3){
		return "自定义调度";
	}else{
		return "常规任务";
	}
}
 