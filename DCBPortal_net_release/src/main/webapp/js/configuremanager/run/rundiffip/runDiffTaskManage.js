//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //取得主机表格
    JsVar["hostGrid"] = mini.get("hostGrid");
    //取得任务表格
    JsVar["taskGrid"] = mini.get("taskGrid");
    //获取页面跳转get请求传过来的值,CLUSTER_ID和CLUSTER_TYPE,并保存到JsVar中  
	getForwardParams();
    //获取业务主集群信息
    getBusMainCluster();
    //加载任务表格信息
    search();


});

//查询
function search() {
    var paramsObj = {};
    paramsObj["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
   
    loadHost(paramsObj);
}


//查询出版本号列表 
function loadTask(param){
	 //业务集群id
	param["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	datagridLoadPage(JsVar["taskGrid"],param,"deployTask.queryBusTaskList");
}

//加载主机表格
function loadHost(param){
    datagridLoad(JsVar["hostGrid"], param, "deployTask.queryHostList");
}

//渲染操作按钮
function onActionRenderer(e) {
    var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:run(' + index + ')">操作</a>';
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

//获取跳转页面get请求参数,CLUSTER_ID和CLUSTER_TYPE,并保存到JsVar中 
function getForwardParams(){
	// var forwardParamString = window.location.search;
    // var queryArray = forwardParamString.split("=");
    //集群ID
    JsVar["CLUSTER_ID"] = getQueryString('CLUSTER_ID');
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

function run(index){
	var params = {};
    var rowInfo = JsVar["taskGrid"].getRow(index);
    params["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    params["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    params["HOST_ID"] = JsVar["hostGrid"].getSelected().HOST_ID;
    params["HOST_IP"] = JsVar["hostGrid"].getSelected().HOST_IP;
    params["TASK_ID"] = rowInfo["TASK_ID"];
    params["TASK_CODE"] = rowInfo["TASK_CODE"];
    params["VERSION"] = rowInfo["VERSION"];
    params["NAME"] = rowInfo["NAME"];
    params["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    params["PACKAGE_TYPE"] = rowInfo["PACKAGE_TYPE"];

    var url = Globals.baseJspUrl.RUN_DIFF_IP_PROGRAM_JSP_MANAGE_URL+"?data="+encodeURIComponent(JSON.stringify(params));


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
 * 返回到启停界面
 */
function goBack() {
	window.location.href = Globals.ctx+"/jsp/clustermanager/businesscluster/clusterBusStartAndStop?busClusterId=" + JsVar["BUS_CLUSTER_ID"]+ "&CLUSTER_ID=" +JsVar["CLUSTER_ID"];
}
