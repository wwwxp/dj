//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["configGrid"] = mini.get("configGrid");//取得任务表格
});

/**
 * 父窗口弹出调用方法
 * @param params
 */
function onLoadComplete(params){
	datagridLoadPage(JsVar["configGrid"],params,"",Globals.baseActionUrl.MONITOR_ACTION_RESOURCE_CONFIGURATION_URL);
}

