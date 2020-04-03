/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 集群名称
 */
var clusterName="";
var path="";
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //获取表格
    JsVar["zkInfoGrid"] = mini.get("zkInfoGrid");
    JsVar["fileTree"]=$("#zkTree");
    //获取前台传过来的参数
    getForwardParams();
    
    //加载表格
    loadGridInfo();
});


/**
 * 获取跳转页面get请求参数,并保存到JsVar中 
 */
function getForwardParams(){
    clusterName = getQueryString('clusterName');
}

/**
 * 加载表格
 */
function loadGridInfo(){
	var params = {};
	if (clusterName != null && clusterName != '') {
		params["clusterName"] = clusterName;
	} else {
		//将返回按钮屏蔽
		$("#zkHeadDiv").hide();
		mini.parse();
	}
	datagridLoad(JsVar["zkInfoGrid"], params, "集群摘要-查询ZK集群信息", Globals.baseActionUrl.MONITOR_ACTION_ZOOKEEPER_DATAGRID_URL);
}

/**
 * 主机列渲染:运行状态高亮
 * @param e
 */
function zkHostRenderer(e){
	var html="";
	$.each(e.record.hostList,function(i,item){
		if(item.RUN_STATE==1){
			html+="<span class='label label-success' style='margin-left:10px;'>"+item.HOST_IP+"</span>";
		}else{
			html+="<span class='label label-danger' style='margin-left:10px;'>"+item.HOST_IP+"</span>";
		}
	});
	return html;
}

/**
 * 表格与数据树联动
 */
function onSelectionChanged(e){
	clusterName=e.selected.clusterName;
	showZKRoot(clusterName);
}

/**
 * 树刷新
 */
function refresh(){
	showZKRoot(clusterName);
}

/**
 * 返回
 */
function back(){
	history.back();
}
