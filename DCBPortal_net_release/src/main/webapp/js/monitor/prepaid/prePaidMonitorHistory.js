//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");
    JsVar["historyGrid"] = mini.get("historyGrid");//取得任务表格
    JsVar["resultCodeGrid"] = mini.get("resultCodeGrid");
});

/**
 * 父窗口弹出调用方法
 * @param params
 */
function onLoadComplete(params){
	search();
}

function search(){
	var params=JsVar["queryFrom"].getData();
	 params = mini.clone(params);
	 params["BEGIN_TIME"]=mini.formatDate(params["BEGIN_TIME"],'yyyy-MM-dd HH:mm:ss');
	 params["END_TIME"]=mini.formatDate(params["END_TIME"],'yyyy-MM-dd HH:mm:ss');
	 
	datagridLoadPage(JsVar["historyGrid"],params,"monitorMapper.queryDcfOmcPerfDataHistory","","anotherDataSource");
	JsVar["resultCodeGrid"].clearRows();
}

/**
 * 点击行事件
 */
function onclickHistoryGrid(){
	var row = JsVar["historyGrid"].getSelected();
	datagridLoadPage(JsVar["resultCodeGrid"],{BATCH_ID:row["BATCH_ID"]},"monitorMapper.queryHistoryResultCode","","anotherDataSource");
}