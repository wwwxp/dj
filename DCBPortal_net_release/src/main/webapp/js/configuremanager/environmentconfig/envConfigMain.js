/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-18
 * Time: 下午17：06
 * To change this template use File | Settings | File Templates.
 */
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
 	//表格获取
    JsVar["envGrid"] = mini.get("envGrid");
    JsVar["queryForm"] =  new mini.Form("#queryForm");//取得查询表单
    //集群下拉控件
    JsVar["BUS_CLUSTER_ID"] = mini.get("BUS_CLUSTER_ID");
    //初使化下拉数据
    initCombox();
    search();
    //loadingClusterTab();
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
					dataField:item.BUS_CLUSTER_ID, 
					showCloseButton: false
				};
				tabs.addTab(tab);
            });
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterListByState");
}

/**
 * 查询集群列表数据
 */
function initCombox(){
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"",
			function(result){
				if(result.length>0){
					result.unshift({"BUS_CLUSTER_NAME":"全部","BUS_CLUSTER_ID":""});
					JsVar["BUS_CLUSTER_ID"].setData(result);
					JsVar["BUS_CLUSTER_ID"].select(0);
				}
		},"busMainCluster.queryBusMainClusterListByState");
}

/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(e){
	//业务主集群
	var busClusterId = e.tab.id;
	JsVar["CLUSTER_ID"] = busClusterId;
	JsVar["BUS_CLUSTER_NAME"] = e.tab.title;
	JsVar["BUS_CLUSTER_CODE"] = e.tab.code;
	
	search();
}

/**
 * 查询
 */
function search() {
	var paramsObj = JsVar["queryForm"].getData();
	//paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
 
	if($('#IS_ALL').is(':checked')) {
		paramsObj["IS_ALL"] = "ALL";
	}
	loadEnvGrid(paramsObj);
}

/**
 * 加载环境变量表格
 */
function loadEnvGrid(paramsObj){
	//加载表格信息
	datagridLoadPage(JsVar["envGrid"],paramsObj,"environments.queryEnvList");
}

/**
 * 状态渲染
 */
function stateRenderer(e){
	var STATE = e.record.STATE;
	if(STATE==1){
		return "有效";
	}else{
		return "无效";
	}
}

/**
 * 操作渲染
 */
function optionRenderer(e){
	var index = e.rowIndex;
	var html="";
    html += '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
    html += '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';
    return html;
}

/**
 * 新增环境变量
 */
function add() {
    showAddDialog("环境变量配置--新增环境变量",600,250,Globals.baseJspUrl.ENVIRONMENT_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["envGrid"].reload();
                showMessageTips("新增环境变量成功");
            }
    });
}

/**
 * 修改环境变量
 * @param index
 */
function edit(index) {
	var row = JsVar["envGrid"].getRow(index);
	showEditDialog("环境变量配置--修改环境变量",600,250,Globals.baseJspUrl.ENVIRONMENT_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	                JsVar["envGrid"].reload();
	                showMessageTips("修改环境变量成功!");
	            }
	    },row);
}

/**
 * 删除环境变量
 * @param index
 */
function del(index) {
	var row = JsVar["envGrid"].getRow(index);
	var  ids = new Array();
	ids.push({ID:row["ID"]});
	showConfirmMessageAlter("确定删除记录?",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,ids,"环境变量配置--删除环境变量",
            function(result){
				JsVar["envGrid"].reload();
				showMessageTips("删除环境变量成功!");
        },"environments.delEnv");
	});
}