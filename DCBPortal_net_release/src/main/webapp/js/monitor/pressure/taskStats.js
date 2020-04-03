//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["taskGrid"] = mini.get("taskGrid");//取得任务表格
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
 * 查询
 */
function query(){
	datagridLoadPage(JsVar["taskGrid"],JsVar["params"],"",Globals.baseActionUrl.MONITOR_ACTION_PRESSURE_TOPOLOGY_TASK_STATS_URL);
}

/**
 * 格式化错误信息显示
 * @param e
 * @returns {String}
 */
function formatError(e){
	var errors=e.record.errors;
	if(errors == undefined || errors == null || errors == "" || errors.length<1) return "";
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