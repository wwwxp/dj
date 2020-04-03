//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");
    JsVar["includeGrid"] = mini.get("includeGrid");//取得任务表格
    JsVar["resultCodeGrid"]= mini.get("resultCodeGrid");
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
	 params["BEGIN_TIME"]=mini.formatDate(params["BEGIN_TIME"],'yyyy-MM-dd');
	 params["END_TIME"]=mini.formatDate(params["END_TIME"],'yyyy-MM-dd');
	 
	datagridLoadPage(JsVar["includeGrid"],params,"monitorMapper.queryDcfOmcPerfDataInclude","","anotherDataSource");
	JsVar["resultCodeGrid"].clearRows();
}

/**
 * 点击行事件
 */
function onclickHistoryGrid(){
	var row = JsVar["includeGrid"].getSelected();
	datagridLoadPage(JsVar["resultCodeGrid"],{NET_NAME:row["NET_NAME"],BEGIN_TIME:row["BEGIN_TIME"]},"monitorMapper.queryHistoryIncludeResultCode","","anotherDataSource");
}
