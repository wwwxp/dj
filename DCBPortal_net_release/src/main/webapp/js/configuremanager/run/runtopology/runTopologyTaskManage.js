//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
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
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "运行Topology管理-获取主集群信息",
		function success(result){
			if (result != null) {
				$("#clusterNameSpan").html("（当前业务集群：" + result[0]["BUS_CLUSTER_NAME"] + "，请确认已启动nimbus和supervisor集群）");
			}
       	}, "busMainCluster.queryBusMainClusterList");
}

//查询
function search() {
    var paramsObj = {};
    //集群ID
    paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    //集群类型
    paramsObj["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    //业务集群id
    paramsObj["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    //加载程序
    loadTask(paramsObj);
}

//查询出版本号列表 
function loadTask(param){
	datagridLoadPage(JsVar["taskGrid"], param, "deployTask.queryBusTaskList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var RUN_STATE = e.record.RUN_STATE;
	var index = e.rowIndex;
	var html= '';
	html += '<a class="Delete_Button" href="javascript:submit(' + index + ')">操作</a>';
	return html;
}

/**
 * 运行状态高亮显示 
 * @param e
 * @returns
 */
function runStateRenderer(e){
	var run_pro=e.record.PROGRAM_NAME;
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
		return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+html+"&nbsp;</span>";
	}
}

/**
 * 获取请求参数
 */
function getForwardParams(){
    // var forwardParamString = window.location.search;
    // var queryArray = forwardParamString.split("=");
    JsVar["CLUSTER_ID"] = getQueryString('CLUSTER_ID');
    JsVar["CLUSTER_TYPE"] = getQueryString('CLUSTER_TYPE');
    //业务集群ID
    JsVar["BUS_CLUSTER_ID"] = getQueryString('BUS_CLUSTER_ID');
}

/**
 * 运行
 * @param index
 */
function submit(index) {
    var params = {};
    var rowInfo = JsVar["taskGrid"].getRow(index);
    params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    params["TASK_ID"] = rowInfo["TASK_ID"];
    params["NAME"] = rowInfo["NAME"];
    params["TASK_CODE"] = rowInfo["TASK_CODE"];
    params["FILE_NAME"] = rowInfo["FILE_NAME"];
    params["VERSION"] = rowInfo["VERSION"];
    params["PACKAGE_TYPE"] = rowInfo["PACKAGE_TYPE"];
    params["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    var url = Globals.baseJspUrl.RUN_TOPOLOGY_PROGRAM_JSP_MANAGE_URL+"?data="+encodeURIComponent(JSON.stringify(params));

	var rowInfo = JsVar["taskGrid"].getRow(index);
    var deTabs = window.parent.mini.get("deploy_tabs");
    var tabs = window.parent.mini.get("deploy_tabs").tabs;
    var tabname = window.parent.mini.get("deploy_tabs").getActiveTab().title + "-" + rowInfo.TASK_CODE;
    for(var i = 0;i< tabs.length;i++){
        if(tabname === tabs[i].title){
            deTabs.activeTab(tabs[i]);
            return false;
        }
    }

    var tab = {
        headerStyle:"background:#B5C2B6;",
    	title: tabname,
		url: url,
		showCloseButton: true ,
		titleField: window.parent.mini.get("deploy_tabs").getActiveTab()._id
    };
    tab.ondestroy = function (e) {
        var tabs = e.sender;
        var iframe = tabs.getTabIFrameEl(e.tab);
        var index = tabs.hoverTab.titleField;
        deTabs.activeTab(tabs.tabs[index-1]);
        JsVar["taskGrid"].reload();
    }
    deTabs.addTab(tab);
    deTabs.activeTab(tab);
}

/**
 * 返回
 */
function goBack() {
	window.location.href = Globals.ctx+"/jsp/clustermanager/businesscluster/clusterBusStartAndStop?busClusterId=" + JsVar["BUS_CLUSTER_ID"] + "&CLUSTER_ID=" +JsVar["CLUSTER_ID"];
}
