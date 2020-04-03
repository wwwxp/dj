//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["workerGrid"] = mini.get("workerGrid");//取得任务表格
});



/**
 * 父窗口弹出调用方法
 * @param params
 */
function onLoadComplete(params){
	JsVar["params"]=params;
	query();
}

/**
 * 切换win参数进行查询
 * @param type
 */
function switchComponent(type){
	JsVar["params"].win=type;
	query();
}

/**
 * 查询
 */
function query(){
	datagridLoadPage(JsVar["workerGrid"],JsVar["params"],"",Globals.baseActionUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_WORKER_METRICS_URL);
}

/**
 * 格式化netty显示
 * @param e
 * @returns {String}
 */
function formatNetty(e){
	 return "Netty";
}