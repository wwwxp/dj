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
    //获取页面跳转get请求传过来的值,CLUSTER_ID和CLUSTER_TYPE,并保存到JsVar中  
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
				$("#clusterNameSpan").html("（当前业务集群：" + result[0]["BUS_CLUSTER_NAME"] + "）");
			}
       	}, "busMainCluster.queryBusMainClusterList");
}

/**
 * 表格查询
 */
function search() {
    var paramsObj = {};
    //集群类型
    paramsObj["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    //集群ID
    paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    //业务集群id
    paramsObj["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    //加载任务
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
 * 加载主机表格
 * @param param
 */
function loadHost(param){
    datagridLoad(JsVar["hostGrid"], param, "deployTask.queryHostList");
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
    var index = e.rowIndex;
/*    var html = '<a class="Delete_Button" href="javascript:run(' + index + ')">启动</a>';
    	html += '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
     	html += '<a class="Delete_Button" href="javascript:queryInstatnce(' + index + ')">查看</a>';*/
    var html = '<a class="Delete_Button" href="javascript:addOperator(' + index + ')">操作</a>';
    return html;
}

/**
 * 表格联动
 * @param e
 */
function onSelectionChanged(e){
	var params = {};
	params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
	params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
	params["HOST_ID"] = e.selected.HOST_ID;
	loadTask(params);
}

/**
 * 获取跳转页面get请求参数,CLUSTER_ID和CLUSTER_TYPE,并保存到JsVar中 
 */
function getForwardParams(){
    // var forwardParamString = window.location.search;
    // var queryArray = forwardParamString.split("=");
    //集群ID
    JsVar["CLUSTER_ID"] = getQueryString('CLUSTER_ID');
  //集群code
    JsVar["CLUSTER_CODE"] = getQueryString('CLUSTER_CODE');
    //集群类型
    JsVar["CLUSTER_TYPE"] = getQueryString('CLUSTER_TYPE');
    //业务集群ID
    JsVar["BUS_CLUSTER_ID"] = getQueryString('BUS_CLUSTER_ID');
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
		return "<span class='label label-danger'>&nbsp;无运行程序&nbsp;</span>";
	}else{
		JsVar["run_pro"] = run_pro;
		var array=run_pro.split(",");
		var html="";
		for(var i=0;i<array.length;i++){
			html+="<span class='label label-success'>"+array[i]+"</span>  ";
		}
		return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+html+"&nbsp;</span>";
	}
}

/**
 * 行链接：运行
 * @param index
 */
function run(index){
	var params = {};
	var rowInfo = JsVar["taskGrid"].getRow(index);
	params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
	params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
	params["TASK_ID"] = rowInfo["TASK_ID"];
	params["TASK_CODE"] = rowInfo["TASK_CODE"];
	params["VERSION"] = rowInfo["VERSION"];
	showDialog("程序管理", "98%", "98%", Globals.baseJspUrl.OTHER_PROGRAM_JSP_RUNS_URL,
        function destroy(data){
    		JsVar["taskGrid"].reload();
    },params, {allowDrag:false});
}

/**
 * 周边程序操作
 */
function addOperator(index) {
    var params = {};
    var rowInfo = JsVar["taskGrid"].getRow(index);
    params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    params["CLUSTER_CODE"] = JsVar["CLUSTER_CODE"];
    params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    params["TASK_ID"] = rowInfo["TASK_ID"];
    params["TASK_CODE"] = rowInfo["TASK_CODE"];
    params["VERSION"] = rowInfo["VERSION"];
    params["NAME"] = rowInfo["NAME"];
    params["PACKAGE_TYPE"] = rowInfo["PACKAGE_TYPE"];
    params["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];

    var url = Globals.baseJspUrl.RUN_SAME_IP_PROGRAM_JSP_MANAGE_URL+"?data="+encodeURIComponent(JSON.stringify(params));

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

    // var newTabName = tabname;
    // if (newTabName != null && newTabName.length > 6) {
    //     newTabName = tabname.substring(0, 6) + "...";
    // }

    var tab = {
        headerStyle:"background:#B5C2B6;",
        title: tabname,
        url: url,
        tooltip:tabname,
		name:tabname,
        showCloseButton: true,
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
 * 行链接:停止
 * @param index
 */
function stop(index){
	var params = {};
	var rowInfo = JsVar["taskGrid"].getRow(index);
	var run_pro = rowInfo.RUN_PROGRAM;
	if(run_pro==null || run_pro<1){
        showWarnMessageTips("该版本没有运行任何进程");
		 return;
	}
	params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
	params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
	params["TASK_ID"] = rowInfo["TASK_ID"];
	params["TASK_CODE"] = rowInfo["TASK_CODE"];
	params["VERSION"] = rowInfo["VERSION"];
	
	showDialog("程序管理", "98%", "98%", Globals.baseJspUrl.OTHER_PROGRAM_JSP_STOPS_URL,
        function destroy(data){
    		JsVar["taskGrid"].reload();
    },params, {allowDrag:false});
}

//查看实例，
function queryInstatnce(index){
	var params = {};
	var rowInfo = JsVar["taskGrid"].getRow(index);
	params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
	params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
	params["TASK_ID"] = rowInfo["TASK_ID"];
	params["TASK_CODE"] = rowInfo["TASK_CODE"];
	showDialog("程序管理","98%", "98%", Globals.baseJspUrl.OTHER_PROGRAM_JSP_MANAGE_URL,
        function destroy(data){
    		JsVar["taskGrid"].reload();
    },params, {allowDrag:false});
}

/**
 * 返回到启停界面
 */
function goBack() {
	window.location.href = Globals.ctx+"/jsp/clustermanager/businesscluster/clusterBusStartAndStop?busClusterId=" + JsVar["BUS_CLUSTER_ID"]+ "&CLUSTER_ID=" +JsVar["CLUSTER_ID"];
}
