/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["taskGrid"] = mini.get("taskGrid");//取得任务表格
    // 转发获取参数
	getForwardParams();
	//获取业务主集群信息
	getBusMainCluster();
    //加载任务表格信息 
    search();
});

/**
 * 获取业务主集群信息
 */
function getBusMainCluster() {
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "采集管理-获取主集群信息",
		function success(result){
			if (result != null) {
				$("#clusterNameSpan").html("（当前业务集群：" + result[0]["BUS_CLUSTER_NAME"] + "，请确认已启动nimbus和supervisor集群）");
			}
       	}, "busMainCluster.queryBusMainClusterList");
}

/**
 * 表格查询
 */
function search() {
    var paramsObj = {};
    //集群ID
    paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    //集群类型
    paramsObj["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    //加载程序
    loadTask(paramsObj);
}

/**
 * 查询出版本号列表 
 * @param param
 */
function loadTask(param){
	datagridLoadPage(JsVar["taskGrid"], param, "deployTask.queryBusTaskList");
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
	var index = e.rowIndex;
    var html= '<a class="Delete_Button" href="javascript:runAndStopProgram(' + index + ')">查看</a>';
    return html;
}

/**
 * 获取跳转页面get请求参数,CLUSTER_ID和CLUSTER_TYPE,并保存到JsVar中 
 */
function getForwardParams(){
    // var forwardParamString = window.location.search;
    // var queryArray = forwardParamString.split("=");
    JsVar["CLUSTER_ID"] = getQueryString('CLUSTER_ID');
    JsVar["CLUSTER_TYPE"] = getQueryString('CLUSTER_TYPE');
    //业务集群ID
    JsVar["BUS_CLUSTER_ID"] = getQueryString('TAB_NAME');
}

/**
 * 运行状态高亮显示 
 * @param e
 * @returns
 */
function runStateRenderer(e){
	var run_pro=e.record.RUN_PROGRAM;
	if(run_pro==null || run_pro<1){
		JsVar["run_pro"] = run_pro;
		return "<span class='label label-danger'>&nbsp;未运行topology&nbsp;</span>";
	}else{
		JsVar["run_pro"] = run_pro;
		var array=run_pro.split(",");
		var html="";
		for(var i=0;i<array.length;i++){
			html+="<span class='label label-success' >"+array[i]+"</span>  ";
		}
		return "<span style='word-wrap:break-word;'>"+html+"&nbsp;</span>";
	}
}

/**
 * 行链接：chnegxuguanl
 * @param index
 */
function runAndStopProgram(index){
	var params = {};
	var rowInfo = JsVar["taskGrid"].getRow(index);
	params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
	params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
	params["TASK_ID"] = rowInfo["TASK_ID"];
	params["NAME"] = rowInfo["NAME"];
	params["TASK_CODE"] = rowInfo["TASK_CODE"];
	params["FILE_NAME"] = rowInfo["FILE_NAME"];
	params["VERSION"] = rowInfo["VERSION"];
	params["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	showDialog("程序管理-rent", "98%", "98%", Globals.baseJspUrl.RENT_PROGRAM_JSP_MANAGE_URL,
        function destroy(data){
    		JsVar["taskGrid"].reload();
    },params);
	
//	showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
//		getJsonDataByPost(Globals.baseActionUrl.RENT_RUN_TASK_ACTION_MANAGE_URL,rowInfo,"rent月租启停--运行任务",
//	        function(result){
//				JsVar["taskGrid"].reload();
//				showMessageAlter("停止运行成功!");
//	        });
//	});
	
	
//	showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
//	getJsonDataByPost(Globals.baseActionUrl.RENT_STOP_TASK_ACTION_MANAGE_URL,rowInfo,"rent月租启停--运行任务",
//        function(result){
//			JsVar["taskGrid"].reload();
//			showMessageAlter("停止运行成功!");
//        });
//});
}

/**
 * 返回
 */
function goBack() {
	window.location.href = Globals.ctx+"/jsp/clustermanager/businesscluster/clusterBusStartAndStop?busClusterId=" + JsVar["BUS_CLUSTER_ID"];
}
