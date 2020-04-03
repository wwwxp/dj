
//全局对象
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //运行Topology节点Grid
    JsVar["runningGrid"] = mini.get("runningGrid");
    //待升级Topology节点Grid
    JsVar["upgradGrid"] = mini.get("upgradGrid");
    //加载当前运行版本Topology
    initRunTopology();

});

/**
 * 初始化获取当前运行版本Topology
 */
function initRunTopology() {
	var params = {
		BUS_CLUSTER_ID:parent.window.JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:parent.window.JsVar["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME:parent.window.JsVar["BUS_CLUSTER_NAME"]
	};
	comboxLoad(mini.get("TOPOLOGY_LIST"), params, null, Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_URL, null, false);
}

/**
 * Topology切换， 获取节点信息
 */
function changeTopology() {
	var taskProgramId = mini.get("TOPOLOGY_LIST").getValue();
	var params = {
		TASK_PROGRAM_ID:taskProgramId
	};
	JsVar["runningGrid"].setData([]);
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_NODE_LIST_URL, params, "版本切换-查询Topology主机列表", 
			function(result) {
	            if (result != null && result.length > 0) {
	            	JsVar["runningGrid"].setData(result);
	            }
	});
	
	//加载关联的Topology
	loadRelaTopologyList();
}

/**
 * 获取待升级的Topology节点信息
 */
function changeUpgradTopology() {
	var taskProgramId = mini.get("UPGRAD_LIST").getValue();
	var params = {
		TASK_PROGRAM_ID:taskProgramId
	};
	JsVar["upgradGrid"].setData([]);
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_NODE_LIST_URL, params, "版本切换-查询Topology主机列表", 
			function(result) {
	            if (result != null && result.length > 0) {
	            	JsVar["upgradGrid"].setData(result);
	            }
	});
}

/**
 * 加载关联的Topology
 */
function loadRelaTopologyList() {
	var params = {
		BUS_CLUSTER_ID:parent.window.JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:parent.window.JsVar["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME:parent.window.JsVar["BUS_CLUSTER_NAME"],
		PROGRAM_GROUP:mini.get("TOPOLOGY_LIST").getSelected()["PROGRAM_GROUP"],
		TASK_PROGRAM_ID:mini.get("TOPOLOGY_LIST").getSelected()["ID"]
	};
	comboxLoad(mini.get("UPGRAD_LIST"), params, null, Globals.baseActionUrl.SWITCH_LOAD_UPGRADE_TOLOLOGY_URL);
}

/**
 * 灰度发布
 */
function upgradeVersion() {
	var hostList = JsVar["runningGrid"].getSelecteds();
	if (hostList == null || hostList.length == 0) {
        showWarnMessageTips("请选择需要灰度升级的主机！");
		return;
	}
	var finalHostList = [];
	var tipsHostList = [];
	for (var i=0; i<hostList.length; i++) {
		if (hostList[i]["STATUS"] == "1") {
			finalHostList.push({
				"HOST_IP":hostList[i]["HOST_IP"],
				"STATUS":hostList[i]["STATUS"]
			});
			tipsHostList.push(hostList[i]["HOST_IP"]);
		}
	}
	
	if (finalHostList == null || finalHostList.length == 0) {
        showWarnMessageTips("当前运行状态节点进行灰度升级！");
		return;
	}
	
	var upgrad = mini.get("UPGRAD_LIST").getValue();
	if (!upgrad) {
        showWarnMessageTips("请选择待升级版本！");
		return;
	}
	
	var params = {
		OPERATOR_TYPE:1,
		RUNNING_TASK_PROGRAM_ID:mini.get("TOPOLOGY_LIST").getSelected()["ID"],
		UPGREAD_TASK_PROGRAM_ID:mini.get("UPGRAD_LIST").getSelected()["ID"],
		HOST_LIST:finalHostList
	};
	
	var tips = "是否确认进行选中节点版本切换， 当前选中的节点有: " + tipsHostList.join(",") + ", 升级版本为: " + mini.get("UPGRAD_LIST").getSelected()["PROGRAM_TEXT"];
	showConfirmMessageAlter(tips, function ok(){
		getJsonDataByPost(Globals.baseActionUrl.SWITCH_START_NODE_UPGRADE_TOLOLOGY_LIST_URL, params, "版本切换-灰度发布", 
				function(result) {
		            if (result != null && result["RET_CODE"]) {
		            	var retMsg = result["RET_MSG"];
		            	var paramsHtml="<div style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+retMsg+"</div>";
		            	var options={
	            		    title: "日志详情",    
	            		    width:"800",
	            		    height:"600",
	            		    buttons: ["ok"],    
	            		    iconCls: "",
	            		    html: paramsHtml,   
	            		    callback: function(action){
	            		    	
	            		    }
	            		};
		            	
		            	var tipsBox = mini.showMessageBox({
		                    title: "处理详情",
		                    iconCls: "mini-messagebox-success",
		                    buttons: ["yes", "no"],
		                    message: "灰度升级成功， 是否查看日志详情?",
		                    callback: function (action) {
		                    	if (action == "yes") {
		                    		mini.showMessageBox(options);
		                    	} else {
		                    		mini.hideMessageBox(tipsBox);
		                    	}
		                    }
		                });
		            	//重新加载当前运行版本
		            	changeTopology();
		            	//重新加载待升级版本
		            	changeUpgradTopology();
		            }
		});
	});
}

/**
 * 正式发布
 */
function upgradeAllVersion() {
	var hostList = JsVar["runningGrid"].getData();
	var finalHostList = [];
	var tipsHostList = [];
	for (var i=0; i<hostList.length; i++) {
		if (hostList[i]["STATUS"] == "1") {
			finalHostList.push({
				"HOST_IP":hostList[i]["HOST_IP"],
				"STATUS":hostList[i]["STATUS"],
			});
			tipsHostList.push(hostList[i]["HOST_IP"]);
		}
	}
	if (finalHostList == null || finalHostList.length == 0) {
        showWarnMessageTips("当前Topology无正在运行主机，无需进行正式发布！");
		return;
	}
	var upgrad = mini.get("UPGRAD_LIST").getValue();
	if (!upgrad) {
        showWarnMessageTips("请选择待升级版本！");
		return;
	}
	var params = {
		OPERATOR_TYPE:1,
		RUNNING_TASK_PROGRAM_ID:mini.get("TOPOLOGY_LIST").getSelected()["ID"],
		UPGREAD_TASK_PROGRAM_ID:mini.get("UPGRAD_LIST").getSelected()["ID"],
		HOST_LIST:finalHostList
	};
	
	var tips = "是否确认进行正式发布，正式发布将所有节点升级， 当前未升级的节点有: " + tipsHostList.join(",") + ", 升级版本为: " + mini.get("UPGRAD_LIST").getSelected()["PROGRAM_TEXT"];
	showConfirmMessageAlter(tips, function ok(){
		getJsonDataByPost(Globals.baseActionUrl.SWITCH_START_NODE_UPGRADE_TOLOLOGY_LIST_URL, params, "版本切换-正式发布", 
				function(result) {
		            if (result != null && result["RET_CODE"]) {
		            	var retMsg = result["RET_MSG"];
		            	var paramsHtml="<div style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+retMsg+"</div>";
		            	var options={
	            		    title: "日志详情",    
	            		    width:"800",
	            		    height:"600",
	            		    buttons: ["ok"],    
	            		    iconCls: "",
	            		    html: paramsHtml,   
	            		    callback: function(action){
	            		    	
	            		    }
	            		};
		            	
		            	var tipsBox = mini.showMessageBox({
		                    title: "处理详情",
		                    iconCls: "mini-messagebox-success",
		                    buttons: ["yes", "no"],
		                    message: "正式发布成功， 是否查看日志详情?",
		                    callback: function (action) {
		                    	if (action == "yes") {
		                    		mini.showMessageBox(options);
		                    	} else {
		                    		mini.hideMessageBox(tipsBox);
		                    	}
		                    }
		                });
		            	//重新加载当前运行版本
		            	changeTopology();
		            	//重新加载待升级版本
		            	changeUpgradTopology();
		            }
		});
	});
}

/**
 * 节点状态渲染
 * @param e
 */
function statusRenderer(e) {
    var RUN_STATE = e.record.STATUS;
    var html = "";
    if (RUN_STATE == 0 || RUN_STATE == null) {//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>";
    } else if (RUN_STATE == 1) {//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行&nbsp;</span>";
    }
}