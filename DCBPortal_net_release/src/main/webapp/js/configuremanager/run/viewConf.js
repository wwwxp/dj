//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["configGrid"] = mini.get("configGrid");//取得任务表格
});


function onLoadComplete(data){
	datagridLoad(JsVar["configGrid"],data,"",Globals.baseActionUrl.HOSTSTART_ACTION_VIEW_CONF_URL);
}

function formateValue(e){
	return JSON.stringify(e.record.value);
}