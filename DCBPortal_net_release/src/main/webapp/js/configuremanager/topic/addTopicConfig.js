/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["topicForm"] = new mini.Form("#topicForm");
});

/**
 * 获取所有RocketMq集群
 */
function initRocketMqClusterList() {
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		CLUSTER_TYPE: busVar.ROCKETMQ
	};
	comboxLoad(mini.get("rq_cluster"), params, "topicConfig.queryRocketMqClusterList", null, null, false);
	
	var clusterList = mini.get("rq_cluster").getData();
	if (clusterList != null && clusterList.length > 0) {
		mini.get("rq_cluster").select(0);
		
		//查询部署版本
		var verParams = {
			CLUSTER_ID:mini.get("rq_cluster").getValue(),
			CLUSTER_TYPE:busVar.ROCKETMQ
		};
		comboxLoad(mini.get("rq_version"), verParams, "topicConfig.queryRocketMqVersionList", null, null, false);
		var versionList = mini.get("rq_version").getData();
		if (versionList != null && versionList.length > 0) {
			mini.get("rq_version").select(0);
			
			//获取当前RocketMq集群对应主机列表
			var hostParams = {
				CLUSTER_ID:mini.get("rq_cluster").getValue(),
				CLUSTER_TYPE:busVar.ROCKETMQ,
				VERSION:mini.get("rq_version").getValue()
			};
			comboxLoad(mini.get("rq_ip"), hostParams, "topicConfig.queryRocketMqHostList");
		}
	}
}

/**
 * 切换RocketMq集群
 * @param e
 */
function changeVersion(e) {
	var currVersion = e.record.VERSION;
	//获取当前RocketMq集群对应主机列表
	var hostParams = {
		CLUSTER_ID:mini.get("rq_cluster").getValue(),
		CLUSTER_ID:busVar.ROCKETMQ,
		VERSION:currVersion
	};
	comboxLoad(mini.get("rq_ip"), hostParams, "topicConfig.queryRocketMqHostList");
}

/**
 * 切换RocketMq集群
 * @param e
 */
function changeCluster(e) {
	var currClusterId = e.record.CLUSTER_ID;
	//获取当前RocketMq集群对应主机列表
	var hostParams = {
		CLUSTER_ID:currClusterId,
		CLUSTER_ID:busVar.ROCKETMQ
	};
	comboxLoad(mini.get("rq_version"), hostParams, "topicConfig.queryRocketMqVersionList");
}

/**
 * 父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
	//jstorm集群ID
	JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
	//获取RocketMq集群
	initRocketMqClusterList();
    //查询所有的Topology
    initTopology();
}

/**
 * 查询所有的Topology
 */
function initTopology() {
//	var params = {
//		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
//	};
	comboxLoad(mini.get("topology_id"), null, "topicConfig.queryTopologyList");
}

/**
 * 切换topology
 */
function changeTopology() {
	var topologyId = mini.get("topology_id").getValue();
	
	if (topologyId != null && topologyId != '') {
		//判断该Topology是否已经配置主备节点
		var topologyList = [];
		var params = {
			PROGRAM_CODE:topologyId
		};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,params,"Topic管理配置-查询Topology是否配置主备",
	        function success(result){
				topologyList = result;
	    	}, "topicConfig.queryTopologyAttrList", null, false);
		
		var data = getSysDictData("topology_attr");
		if (topologyList != null && topologyList.length > 0) {
			for (var i=0; i<topologyList.length; i++) {
				for (var j=0;j<data.length; j++) {
					if (topologyList[i].PROGRAM_ATTR  == data[j].code) {
						data.remove(data[j]);
					}
				}
			}
		}
		mini.get("topology_attr").setData(data);
	}
}

/**
 * 新增和修改点确认按钮保存
 */
function onSubmit() {
    addConfig();
}

/**
 * 新增Topic配置
 */
function addConfig() {
    //判断是否有效
    JsVar["topicForm"].validate();
    if (JsVar["topicForm"].isValid() == false){
        return;
    }
    
    //新增操作下获取表单的数据
    var topicData = JsVar["topicForm"].getData();
    var topValue = mini.get("topology_id").getValue().split('_');
    topicData["PROGRAM_GROUP"] = topValue[1];
    topicData["topology_id"] = topValue[0];
    topicData["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    getJsonDataByPost(Globals.baseActionUrl.TOPIC_CONFIG_ACTION_ADD_URL,topicData,"Topic管理配置-新增Topic配置",
            function success(result){
    			if (result.rstCode == "0") {
    				closeWindow(systemVar.SUCCESS);
    			}
            });
}
