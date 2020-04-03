//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    //页面form表单
    JsVar["queryForm"] = new mini.Form("#queryForm");
    //版本信息
    JsVar["topologyVersion"] = mini.get("topologyVersion");
    //左边菜单权限树
    JsVar["data_tree"] = mini.get("data_tree");
    
    //加载tab页
    loadingClusterTab();
    //获取版本信息
    loadVersionCombobox();
});

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		var tab_str="";
		if(result.length>0){
			var tabs = mini.get("#switchTabs");
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
 * tab页切换函数：根据集群id加载对应表格
 * @param e
 */
function loadPage(e){
	//业务主集群
	var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = busClusterId;
	JsVar["BUS_CLUSTER_CODE"] = e.tab.code;
	JsVar["BUS_CLUSTER_NAME"] = e.tab.title;
	
	//加载集群类型
	loadClusterType();
	
	//加载插件列表
	loadTree();
}

/**
 * 加在版本号下拉框
 * @returns
 */
function loadVersionCombobox(){
	var params = {
		FILE_TYPE:2
	};
	comboxLoad(JsVar["topologyVersion"], params, "ftpFileUpload.queryFileInfo","","",false);
	JsVar["topologyVersion"].select(0);
}

/**
 * 加载当前集群业务类型
 * @returns
 */
function loadClusterType(){
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		CLUSTER_TYPE:"'billing','rent'"
	};
	comboxLoad(mini.get("topologyType"), params, "busMainCluster.queryBusMainClusterRelationChildrenList", "", "", false);
	mini.get("topologyType").select(0);
}

//左边树加载前
function onBeforeOpen(e) {
    var node = JsVar["data_tree"].getSelectedNode();
    //当没有选择节点， 则阻止浏览器的右键菜单
    if (!node) {
        e.htmlEvent.preventDefault();
        e.cancel = true;
        return;
    }
}
 
//点击树节点时，显示右边详细信息
function onClickTreeNode() {
    var selectedNode = JsVar["data_tree"].getSelectedNode();
    var param = {};
    param["topologyType"] = mini.get("topologyType").getValue();
    param["pluginFileName"] = selectedNode.pluginFileName;
    param["name"] = selectedNode.name;
    param["versionName"] = mini.get("topologyVersion").getValue();
    param["version"] = mini.get("topologyVersion").getText();
    param["busClusterCode"] = JsVar["BUS_CLUSTER_CODE"];
    getJsonDataByPost(Globals.baseActionUrl.TOPOLOGY_PLUGIN_ACTION_GET_XMLDESC_URL,param,"",
    		function(result){
    			deserializerJsonViewForm("data_form",result);
    			mini.get("soDesc").setValue(result.soDesc);
		});
}

/**
 * 查询
 */
function search(){
	loadTree();
}

/**
 * 加载插件列表信息
 */
function loadTree(){
	var params = {
		VERSION:JsVar["topologyVersion"].getValue(),
		CLUSTER_TYPE:mini.get("topologyType").getValue(),
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE:JsVar["BUS_CLUSTER_CODE"]
	};
    getJsonDataByPost(Globals.baseActionUrl.TOPOLOGY_PLUGIN_ACTION_QUERY_URL, params, "插件管理-加载插件列表",
		function(result){
			if(result.length>0){
				 JsVar["data_tree"].loadList(result, "id", "parentId");
				 JsVar["data_tree"].expandAll();
			} else {
				JsVar["data_tree"].loadList([]);
				//showWarnMessageAlter("查询无数据");
			}
	});
}

/**
 * 搜索树节点
 */
function searchTree() {
    var node_name = mini.get("node_name").getValue();
    if (node_name == "") {
    	JsVar["data_tree"].clearFilter();
    }else {
    	node_name = node_name.toLowerCase();
        JsVar["data_tree"].filter(function (node) {
            var text = node.desc ? node.desc.toLowerCase() : "";
            if (text.indexOf(node_name) != -1) {
                return true;
            }
        });
    }
}

/**
 * 刷新
 */
function refreshTree(){
	mini.get("node_name").setValue('');
    JsVar["data_tree"].clearFilter();
}
 

