/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
	mini.parse();
	// 表格获取
	JsVar["datagrid"] = mini.get("datagrid");
});

function onLoadComplete(data) {
	JsVar["data"] = data;
	
	initGridData(JsVar["data"]);
}

/**
 * 确定选择数据
 */
function addData() {
	var data = JsVar["datagrid"].getSelected();
	if (!data) {
        showWarnMessageTips("请选择一条记录！");
		return;
	}
	closeWindow(data);
}

/**
 * 加载定时任务表格
 */
function initGridData(paramsObj) {
	// 加载表格信息
	datagridLoadPage(JsVar["datagrid"], paramsObj, "upgradeHistory.queryUpgradHistoryList");
}


function queryHistory(){
	// 加载表格信息
	datagridLoadPage(JsVar["datagrid"], JsVar["data"], "upgradeHistory.queryUpgradHistoryList");
}
