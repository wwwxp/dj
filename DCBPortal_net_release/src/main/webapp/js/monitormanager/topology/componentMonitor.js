//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["componentGrid"] = mini.get("componentGrid");
    JsVar["taskStateGrid"] = mini.get("taskStateGrid");
    JsVar["taskInfoGrid"] = mini.get("taskInfoGrid");
    
    getForwardParams();
    // 初始化表格
    componentDataEvent(60);
    
});

//获取跳转页面get请求参数,并保存到JsVar中  
function getForwardParams(){
	// var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	JsVar["clusterName"] = getQueryString('clusterName');
	JsVar["topologyId"] = getQueryString('topologyId');
	JsVar["componentName"] = getQueryString('componentName');
	JsVar["topologyName"] = getQueryString('topologyName');
}

// 拓补名称初始化
function topologyNameRenderer(e){
	return JsVar["topologyName"];
}

/**
 * 任务状态高亮显示
 */
function stateHighlightRenderer(e){
	if(e.value=="ACTIVE"){
		return "<span class='label label-success'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}else{
		return "<span class='label label-danger'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}
}

/**
 * 任务状态错误高亮显示
 */
function errorHighlightRenderer(e){
	var errors=e.record.errors;
	if(errors == undefined || errors == null || errors == "" || errors.length<1){
		return "";
	} 
	
	var title="";
	for(var i=0;i<errors.length;i++){
		 var ts = errors[i]["errorTime"]* 1000;
		 if(errors[i]["error"].lastIndexOf() == -1 ){
			 var  idx = errors[i]["error"].indexOf("&#10;");
			 var length = errors[i]["error"].length;
			 if (idx != -1) {
                    var first_line = errors[i]["error"].substring(0, idx)+"&#10;";
                    var rest_lines = errors[i]["error"].substring(idx + 1, length);
                    title+=first_line + "  , at " + mmsecondFormate(ts);
                    title+=rest_lines;
                }else{
                	title+=errors[i]["error"] + "   , at " + mmsecondFormate(ts);
                }
		 }else{
			 title+=errors[i]["error"]+"  , at "+mmsecondFormate(ts);
		 }
		 title+="&#10;";
	 }
	 var isWarningMsg =false;
	 
	 for(var i=0;i<errors.length;i++){
		 if(errors[i]["error"].indexOf("is full")>-1 || errors[i]["error"].indexOf("Backpressure")>-1, errors[i]["error"].indexOf("backpressure")>-1){
			 isWarningMsg = true;
		 }
	 }
	 if(isWarningMsg){
		 return "<span  style='color:red;' title='"+title+"'>W("+errors.length+")"+"</span>";
	 }else{
		 return "<span  style='color:red;' title='"+title+"'>E("+errors.length+")"+"</span>";
	 }
}

function mmsecondFormate(mmsecond){
	 var tf = function(i){return (i < 10 ? '0' : '') + i}; 
	 var date = new Date(mmsecond);
	 return date.getFullYear()+"-"+tf(date.getMonth()+1)+"-"+tf(date.getDate())+" "+tf(date.getHours())+":"+tf(date.getMinutes())+":"+tf(date.getSeconds());
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
		+ JsVar["topologyId"]
		+ '&wport='
		+ e.record.port
		+ '&port=8622'
		+ '&host='
		+ e.record.host
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
		+ e.record.host
		+ '>查看</a>';
}

//时间局部刷新按钮
function componentDataEvent(win){
	var queryParams = {};
    queryParams["clusterName"] = JsVar["clusterName"];
    queryParams["topologyId"] = JsVar["topologyId"];
    queryParams["componentName"] = JsVar["componentName"];
    queryParams["win"] = win + "";
	
    getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_COMPONENT_METRICS_URL,queryParams,"",
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["componentGrid"].setData(result["uIComponentMetric"]);
			JsVar["taskStateGrid"].setData(result["taskEntityList"]);
			JsVar["taskInfoGrid"].setData(result["taskMetricList"]);
		}
		
	});
}
