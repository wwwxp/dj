//定义变量， 通常是页面控件和参数
var JsVar =new Object();

//Jstorm集群名称
var clusterName = "";
//显示节点名称
var filterList = [{nodeName:"localservice"}];
//初使化
$(document).ready(function () {
    mini.parse();
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	JsVar["DATA"] = data;
    var params = {
    	CLUSTER_TYPE:busVar.JSTORM,
    	BUS_CLUSTER_ID:data["BUS_CLUSTER_ID"]
    };
	initZkConfig(params);
	
	//展开所有节点
	setTimeout(function() {
		expandAll();
	}, 1000);
	
	updateNodesCount();
}

/**
 * 手动刷新数据
 */
function refresh() {
	var params = {
    	CLUSTER_TYPE:busVar.JSTORM,
    	BUS_CLUSTER_ID:JsVar["DATA"]["BUS_CLUSTER_ID"]
    };
	
	$("#btnRefresh").hide();
	
	initZkConfig(params);
	
	//展开所有节点
	setTimeout(function() {
		expandAll();
	}, 1000);
	
	updateNodesCount();
}

/**
 * 修改节点信息
 */
function updateNodesCount() {
	//展开所有节点
	setTimeout(function() {
		if (zTree != null) {
			//根目录localservice
			var rootNodes = zTree.getNodes();
			for (var i=0; i<rootNodes.length; i++) {
				//注册目录 regist
				var childrenNodes = rootNodes[i]["children"];				
				for (var j=0; j<childrenNodes.length; j++) {
					//服务目录 
					var serviceNodes = childrenNodes[j]["children"];
					for (var k=0; k<serviceNodes.length; k++) {
						var serviceChilrens = serviceNodes[k]["children"];
						if (serviceChilrens != null && serviceChilrens.length > 0) {
							serviceNodes[k]["name"] = serviceNodes[k]["name"] + " (" + serviceChilrens.length + "个服务)";
						} else {
							serviceNodes[k]["name"] = serviceNodes[k]["name"] + " (0个服务)";
						}
						zTree.updateNode(serviceNodes[k]);
					}
				}
			}
		}
		$("#btnRefresh").show();
	}, 3000);
}

/**
 * 获取业务集群关联的Jstorm集群
 * @param params
 */
function initZkConfig(params) {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "运行Topology-获取启动Topology信息",
		function success(result){
			if (result != null && result.length >0) {
				clusterName = result[0]["CLUSTER_CODE"];
				
				showZKRoot(clusterName, filterList);
			}
       	}, "busRelationClusterList.queryJstormByBusClusterId");
}