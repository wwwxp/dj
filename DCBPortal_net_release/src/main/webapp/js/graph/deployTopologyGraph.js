//部署Topology
$(document).ready(function(){
	mini.parse();
	
	initHost();
	
	//创建应用服务器集群
	initApplicationServer();
	
	//创建组件集群服务器
	initComponentsServer();
});

/**
 * 初始化统计主机数量
 */
function initHost() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "部署图-查询主机部署使用情况",
		function(result){
			if (result != null && result.length > 0) {
				var hostCount = result[0]["TOTAL_HOST"];
				var busCount = result[0]["BUS_HOST_COUNT"];
				var comCount = result[0]["COM_HOST_COUNT"];
				//已经使用的主机数
				var usedCount = result[0]["USED_COUNT"];
				var surplusCount = hostCount - usedCount;
				if (surplusCount < 0) {
					surplusCount = 0;
				}
				$("#totalHost").html(hostCount);
				$("#appHost").html(busCount);
				$("#comHost").html(comCount);
				$("#surplusHost").html(surplusCount);
			} 
	}, "deployHome.queryStatiscHostList");
}

/**
 * 初始化获取应用数据
 */
function initApplicationData() {
	var params = {
		TYPE:busVar.BUSINESS_TYPE
	};
	var bussinessList = [];
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询业务部署情况",
		function(result){
			if (result != null && result.length > 0) {
				bussinessList = result;
			}
	}, "deployHome.queryAppDeployList", null, false);
	return bussinessList;
}

/**
 * 应用服务器集群
 */
function initApplicationServer() {
	var appData = initApplicationData();
	var templateData = $("#appTamplate");
	
	var appCount = appData.length;
	for (var i=0; i<appCount; i++) {
		var appName = appData[i]["APP_NAME"];
		var hostCount = appData[i]["COUNT"];
		var instCount = appData[i]["INST_COUNT"];
		//templateData.find("tr>td:eq(1)").html(appName + "<br/>(" + hostCount + "台主机，实例"+instCount+"个)");
		templateData.find("tr>td:eq(1)").html(appName + "<br/>（" + hostCount + "台）");
		$("#appServer").append(templateData.html());
	}
	
	//动态设置DIV大小
	var divWidth = $("#appServer").find("div:eq(0)").width();
	var marginRightWidth = $("#appServer").find("div:eq(0)").css("margin-right");
	var marginLeftWidth = $("#appServer").find("div:eq(0)").css("margin-left");
	var oneWidth = divWidth + parseInt(marginRightWidth.substr(0, marginRightWidth.length -2)) + parseInt(marginLeftWidth.substr(0, marginLeftWidth.length - 2));
	var totalWidth = (appCount * oneWidth) + parseInt(60);
	var docWidth = $(document.body).width();
	if (totalWidth > docWidth) {
		$(document.body).css("min-width", totalWidth);
	}
}

/**
 * 初始化获取组件数据
 */
function initComponentData(){
	var params = {
		TYPE:busVar.COMPONENT_TYPE
	};
	var componentList = [];
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询组件部署情况",
		function(result){
			if (result != null && result.length > 0) {
				componentList = result;
			}
	}, "deployHome.queryComponentDeployList", null, false);
	
	return componentList;
}

/**
 * 组件服务器集群
 */
function initComponentsServer() {
	var appData = initComponentData();
	
	var templateData = $("#componentTemplate");
	var componentCount = appData.length;
	for (var i=0; i<componentCount; i++) {
		var appName = appData[i]["CLUSTER_ELE_NAME"];
		var hostCount = appData[i]["COUNT"];
		var instCount = appData[i]["INST_COUNT"];
		//templateData.find("tr>td:eq(1)").html(appName + "服务器组<br/>(" + hostCount + "台主机，实例"+instCount+"个)");
		templateData.find("tr>td:eq(1)").html(appName + "服务器组<br/>（" + hostCount + "台）");
		$("#componentServer").append(templateData.html());
	}
	
	//动态设置DIV大小
	var divWidth = $("#componentServer").find("div:eq(0)").width();
	var marginRightWidth = $("#componentServer").find("div:eq(0)").css("margin-right");
	var marginLeftWidth = $("#componentServer").find("div:eq(0)").css("margin-left");
	var oneWidth = divWidth + parseInt(marginRightWidth.substr(0, marginRightWidth.length -2)) + parseInt(marginLeftWidth.substr(0, marginLeftWidth.length - 2));
	var totalWidth = (componentCount * oneWidth) + parseInt(60);
	var docWidth = $(document.body).width();
	if (totalWidth > docWidth) {
		$(document.body).css("min-width", totalWidth);
	}
}