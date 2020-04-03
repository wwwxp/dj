//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["topologyGrid"] = mini.get("topologyGrid");//取得任务表格
});



/**
 * 父窗口弹出调用方法
 * @param params
 */
function onLoadComplete(params){
	datagridLoadPage(JsVar["topologyGrid"],params,"",Globals.baseActionUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_STATE_URL);
	$("#topo").attr("src",Globals.ctx+"/jsp/minitor/pressure/topologyDisplay?id="+params.topologyId+"&topologyName="+params.topologyName+"&clusterName="+params.clusterName);
}


