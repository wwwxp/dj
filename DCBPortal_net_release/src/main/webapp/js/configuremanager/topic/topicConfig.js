/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
	mini.parse();
	JsVar["topicGrid"] = mini.get("topicGrid");//取得任务表格
	JsVar["queryForm"] =  new mini.Form("#queryForm");//取得查询表单
	JsVar["PROGRAMBOX"] = mini.get("PROGRAM_CODE");//取得下拉框
	//加载tab页
	loadingClusterTab();
});

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		var tab_str="";
		if(result.length>0){
			var tabs=mini.get("#cluster_tabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.ID, 
					showCloseButton: false
				};
				tabs.addTab(tab);
            });
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterList");
}

/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(e){
	//业务主集群
	var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = busClusterId;
	JsVar["BUS_CLUSTER_NAME"] = e.tab.title;
	loadProgramCombobox();
	search();
}

/**
 * 查询
 */
function search() {
	var paramsObj = JsVar["queryForm"].getData();
	paramsObj["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	load(paramsObj);
}

/**
 * 加载表格 
 * @param param
 */
function load(param){
	datagridLoadPage(JsVar["topicGrid"], param, "topicConfig.queryTopicConfigList");
}

/**
 * 加载下拉框
 * @returns
 */
function loadProgramCombobox(){
	comboxLoad(JsVar["PROGRAMBOX"],null,"topicConfig.queryTopologyNameList");
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:delConfig(' + index + ')">删除</a>';
}

/**
 * Topology属性
 */
function onAttrRender(e) {
	var attr = e.record.PROGRAM_ATTR;
	var attrs = getSysDictData("topology_attr");
	for (var i=0;i<attrs.length; i++) {
		if (attr == attrs[i].code) {
			return attrs[i].text;
		}
	}
	return "";
}

/**
 * 渲染RqIP地址
 * @param e
 */
function onIpRenderer(e) {
	var rqIp = e.record.RQ_IP;
	var rqPort = e.record.RQ_PORT;
	return rqIp + ":" + rqPort;
}

/**
 * 新增Topic配置
 */
function addConfig() {
	var params = {
		BUS_CLUSTER_ID: JsVar["BUS_CLUSTER_ID"]
	};
	showAddDialog("Topic管理-新增Topic配置", 880, 470, Globals.baseJspUrl.TOPIC_CONFIG_JSP_ADD_URL,
      function destroy(data){
          if (data == systemVar.SUCCESS) {
              JsVar["topicGrid"].reload();
              showMessageTips("新增Topic配置成功!");
          }
	}, params);
}

/**
 * 删除Topic配置信息
 */
function delConfig(index) {
	var data = JsVar["topicGrid"].getRow(index);
	 
	//删除Topic配置参数
	var params = {
		topicName:data["TOPIC_NAME"],
		BUS_CLUSTER_ID:data["BUS_CLUSTER_ID"],
		RQ_CLUSTER_ID:data["RQ_CLUSTER_ID"],
		RQ_VERSION:data["RQ_VERSION"],
		rq_ip:data["RQ_IP"],
		rq_port:data["RQ_PORT"],
		topology_id:data["PROGRAM_CODE"],
		topology_attr:data["PROGRAM_ATTR"]
	};
	
	showConfirmMessageAlter("确定删除记录？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.TOPIC_CONFIG_ACTION_DEL_URL, params, "top管理-删除Topic配置信息",
				function(result){
			JsVar["topicGrid"].reload();
                    showMessageTips("删除Topic配置信息成功!");
		});
	});
}
