//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["supervisorInfoGrid"] = mini.get("supervisorInfoGrid");
    JsVar["workUsedGrid"] = mini.get("workUsedGrid");
    JsVar["workerGrid"] = mini.get("workerGrid");
    
    getForwardParams();
    initGrid();
});

function initGrid(){
	var queryParams = {};
	queryParams["clusterName"] = JsVar["clusterName"];
	queryParams["host"] = JsVar["host"];
	
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_SUPERVISOR_SUMMARY_URL,queryParams,"",
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["supervisorInfoGrid"].setData([result["supervisorEntity"]]);
			JsVar["workUsedGrid"].setData(result["workerEntityList"]);
			JsVar["workerGrid"].setData(result["uIWorkerMetricList"]);
		}
		
	});
}

//获取跳转页面get请求参数,并保存到JsVar中 
function getForwardParams(){
	// var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	JsVar["clusterName"] = getQueryString('clusterName');
	JsVar["host"] = getQueryString('host');
}

/**
 * supervisor配置渲染超链接
 */
function configRnderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:configForward('+index+')">查看</a>';
}

function configForward(index){
	var rowInfo = JsVar["supervisorInfoGrid"].getRow(index);
	window.location.href = Globals.ctx + "/jsp/monitormanager/clustersummary/supConfigInfo?clusterName=" 
		+ JsVar["clusterName"]
		+ "&host=" 
		+ rowInfo.host;
}

/**
 * supervisor日志渲染超链接
 */
function supervisorLogRenderer(e){
//	var index = e.rowIndex;
//	return '<a class="Delete_Button" href="javascript:supersivorLogForward('+index+')">查看</a>';
	return  '<a class="Delete_Button" href='
	+Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL
	+'?clusterName='
	+JsVar["clusterName"]
	+'&port=8622'
	+'&host='
	+e.record.host
	+'&file=supervisor.log>查看</a>';
}

/**
 * 日志渲染超链接
 */
function logRenderer(e){
	return  '<a class="Delete_Button" href='
		+ Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL
		+ '?clusterName='
		+ JsVar["clusterName"]
		+ '&tid='
		+ e.record.topology
		+ '&wport='
		+ e.record.port
		+ '&port=8622'
		+ '&host='
		+ JsVar["host"]
		+ '>查看</a>';
}

/**
 * jstack日志渲染超链接
 */
function jstackLogRenderer(e){
	return  '<a class="Delete_Button" href='
		+ Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_JSTACK_LOG_URL
		+ '?clusterName='
		+ JsVar["clusterName"]
		+ '&wport='
		+ e.record.port
		+ '&port=8622'
		+ '&host='
		+ JsVar["host"]
		+ '>查看</a>';
}

/**
 * netty渲染超链接
 * @param e
 */
function nettyRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:nettyForward('+index+')">Netty</a>';
}

function nettyForward(index){
	var rowInfo = JsVar["workUsedGrid"].getRow(index);
	window.location.href = "workerNettyMonitor?clusterName=" 
		+ JsVar["clusterName"] 
		+ "&topologyId=" 
		+ rowInfo.topology
		+ "&host=" 
		+ JsVar["host"]
		+ "&port="
		+ rowInfo.port;
}

/**
 * port渲染超链接
 * @param e
 */
function portRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:portForward('+index+')">' + e.record.port + '</a>';
}

function portForward(index){
	var rowInfo = JsVar["workUsedGrid"].getRow(index);
	window.location.href = "workerMonitor?clusterName=" 
		+ JsVar["clusterName"]
		+ "&topologyId=" 
		+ rowInfo.topology
		+ "&host=" 
		+ JsVar["host"]
		+ "&port="
		+ rowInfo.port;
}

// 运行任务渲染
function runTaskRenderer(e){
	var array = e.value;
	var str = "";
	for(var i = 0; i < array.length;i++){
		str +='<div>'
			+ array[i]["component"]
			+ '</div>';
	}
	return str;
}

/**
* 格式化HeapMemory
* @param e
*/
function formatterHeapMemory(e){
	if(e.value != null){
		return (Number(e.value.replace(/,/g,"")/(1024*1024))).toFixed(2) + " MB";
	}
}