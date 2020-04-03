//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["clusterGrid"] = mini.get("clusterGrid");//取得表格
    JsVar["clusterGrid"].setData(clusters);
});

function formatPort(e){
	return e.record.used_ports+"/"+e.record.total_ports;
	
}

function formatCluster(e){
	return "<a class='Delete_Button' href='"+Globals.baseJspUrl.MONITOR_ACTION_MAIN_HOST_MONITOR_JSP+"?clusterName="+e.record.name+"'>"+e.record.name+"</a>";
	
}