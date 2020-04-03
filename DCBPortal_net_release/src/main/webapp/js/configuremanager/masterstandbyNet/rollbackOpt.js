
//全局对象
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //待回退的版本Grid
    JsVar["lastversionGrid"] = mini.get("lastversionGrid");
    //已升级版本Grid对象
    JsVar["rollbackGrid"] = mini.get("rollbackGrid");
    //加载当前运行版本Topology
    initRollbackTopology();

});

/**
 * 初始化获取当前运行版本Topology
 */
function initRollbackTopology() {
	var params = {
		BUS_CLUSTER_ID:parent.window.JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:parent.window.JsVar["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME:parent.window.JsVar["BUS_CLUSTER_NAME"]
	};
	comboxLoad(mini.get("ROLLBACK_LIST"), params, null, Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_URL, null, false);
}

/**
 * Topology切换， 获取节点信息
 */
function changeTopology() {
	var taskProgramId = mini.get("TOPOLOGY_LIST").getValue();
	var params = {
		TASK_PROGRAM_ID:taskProgramId
	};
	JsVar["lastversionGrid"].setData([]);
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_NODE_LIST_URL, params, "版本回退-查询待回退的版本", 
			function(result) {
	            if (result != null && result.length > 0) {
	            	JsVar["lastversionGrid"].setData(result);
	            }
	});
	
	
}

/**
 * 获取待升级的Topology节点信息
 */
function changeRollbackTopology() {
	var taskProgramId = mini.get("ROLLBACK_LIST").getValue();
	var params = {
		TASK_PROGRAM_ID:taskProgramId
	};
	JsVar["rollbackGrid"].setData([]);
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_LOAD_RUNNING_TOLOLOGY_NODE_LIST_URL, params, "版本回退-查询已升级版本", 
			function(result) {
	            if (result != null && result.length > 0) {
	            	JsVar["rollbackGrid"].setData(result);
	            }
	});
	//加载关联的Topology
	loadRelaTopologyList();
}

/**
 * 加载关联的Topology
 */
function loadRelaTopologyList() {
	var selNode = mini.get("ROLLBACK_LIST").getSelected();
	if (!selNode) {
		return;
	}
	var params = {
		BUS_CLUSTER_ID:parent.window.JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:parent.window.JsVar["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME:parent.window.JsVar["BUS_CLUSTER_NAME"],
		PROGRAM_GROUP:selNode["PROGRAM_GROUP"],
		TASK_PROGRAM_ID:selNode["ID"]
	};
	comboxLoad(mini.get("TOPOLOGY_LIST"), params, null, Globals.baseActionUrl.SWITCH_LOAD_UPGRADE_TOLOLOGY_URL);
}



/**
 * 灰度发布
 */
function rollbackVersion() {
	var hostList = JsVar["rollbackGrid"].getData();
	var finalHostList = [];
	for (var i=0; i<hostList.length; i++) {
		if (hostList[i]["STATUS"] == "1") {
			finalHostList.push({
				"HOST_IP":hostList[i]["HOST_IP"],
				"STATUS":hostList[i]["STATUS"]
			});
		}
	}
	if (finalHostList == null || finalHostList.length == 0) {
        showWarnMessageTips("已升级版本无正在运行的节点，无法进行版本回退！");
		return;
	}
	
	var upgrad = mini.get("TOPOLOGY_LIST").getValue();
	if (!upgrad) {
        showWarnMessageTips("请选择待回退版本！");
		return;
	}
	
	var params = {
		OPERATOR_TYPE:2,
		RUNNING_TASK_PROGRAM_ID:mini.get("ROLLBACK_LIST").getSelected()["ID"],
		UPGREAD_TASK_PROGRAM_ID:mini.get("TOPOLOGY_LIST").getSelected()["ID"],
		HOST_LIST:finalHostList
	};
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_START_NODE_UPGRADE_TOLOLOGY_LIST_URL, params, "版本切换-版本回退", 
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
                    message: "版本回退成功， 是否查看日志详情?",
                    callback: function (action) {
                    	if (action == "yes") {
                    		mini.showMessageBox(options);
                    	} else {
                    		mini.hideMessageBox(tipsBox);
                    	}
                    }
                });
            	//重新加载待回退版本节点数据状态
            	changeTopology();
            	//重新加载已升级版本节点数据状态
            	changeRollbackTopology();
            }
		}
	);
}

/**
 * 历史版本回退
 */
function upgradHistory() {
	var params = {
		BUS_CLUSTER_ID:parent.window.JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:parent.window.JsVar["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME:parent.window.JsVar["BUS_CLUSTER_NAME"]
	};
	showDialog("查看升级历史记录", "80%", "80%", Globals.baseJspUrl.SWITCH_NET_UPGRAD_HISTORY_QUERY_JSP_MANAGE_URL,
	        function destroy(data){
				if (data &&  data != "close") {
					//加载回退版本记录
					mini.get("ROLLBACK_LIST").setValue(data["DEST_TASK_PROGRAM_ID"]);
					changeRollbackTopology();
					
					mini.get("TOPOLOGY_LIST").setValue(data["SOURCE_TASK_PROGRAM_ID"]);
					changeTopology();
				}
	    }, params);
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