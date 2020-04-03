//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //集群展示表格
    JsVar["serviceTypeGrid"] = mini.get("serviceTypeGrid");
    //取得查询表单
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    //获取配置文件中的参数
    getPropKey();
    //加载组件集群类型
    loadClusterType();
    //加载主机表格信息
    search();
});

/**
 * 获取配置文件参数
 */
function getPropKey() {
	//获取配置参数
    var propParams = getPropListByKey(cfgVar.tools_dir + "," + cfgVar.env_dir);
	//获取真实目录信息
	var toolsDir = propParams[cfgVar.tools_dir];
	if (!propParams[cfgVar.tools_dir].endWith("/")) {
		toolsDir = propParams[cfgVar.tools_dir] + "/";
	}
	var envDir = propParams[cfgVar.env_dir];
	if (!propParams[cfgVar.env_dir].endWith("/")) {
		envDir = propParams[cfgVar.env_dir] + "/";
	}
	JsVar["TOOLS_DIR"] = toolsDir;
	JsVar["ENV_DIR"] = envDir;
}

/**
 * 加载集群类型
 */
function loadClusterType() {
	var params = {
		TYPE: busVar.COMPONENT_TYPE
	};
	comboxLoad(mini.get("CLUSTER_TYPE"), params, "clusterEleDefine.queryClusterEleList");
}

//查询
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    load(paramsObj);
}

//重新加载表格
function refresh() {
    JsVar["queryForm"].reset();
    load(null);
}

//加载表格
function load(param){
	param["TYPE"] = busVar.COMPONENT_TYPE;
    datagridLoadPage(JsVar["serviceTypeGrid"], param, "serviceType.queryComponentClusterList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
     	html += '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';
    return html;
}

//渲染主机名称单元格成超链接
function onRenderClusterName(e){
	var index=e.rowIndex; 
	return '<a class="Delete_Button" href="javascript:clusterDetail('+index+')">'+e.record.CLUSTER_NAME+'</a>';
}

/**
 * 重新渲染部署真实路径
 * @param e
 * @returns
 */
function rendDeployPath(e) {
	var deployPath = e.record.CLUSTER_DEPLOY_PATH;
	var clusterType = e.record.CLUSTER_TYPE;
	if (!deployPath.endWith("/")) {
		deployPath += "/" + JsVar["TOOLS_DIR"] +  JsVar["ENV_DIR"] + "版本号/" + clusterType;
	}else{
		deployPath += JsVar["TOOLS_DIR"] + JsVar["ENV_DIR"] + "版本号/" + clusterType;
	}
	return deployPath;
}

/**
 * 运行状态高亮显示 
 * @param e
 * @returns
 */
function onRenderBusCluster(e){
	var busCluster = e.record.BUS_CLUSTER_LIST;
	if(busCluster==null || busCluster<1){
		return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
	}else{
		var busClusterList = busCluster.split(",");
		var html="";
		for(var i=0;i<busClusterList.length;i++){
			html+="<span class='label label-success' >"+busClusterList[i]+"</span>  ";
		}
		return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+html+"&nbsp;</span>";
	}
}

//详细信息
function clusterDetail(index){
	var row = JsVar["serviceTypeGrid"].getRow(index);
	showDialog("详细信息",600,400,Globals.baseJspUrl.COMPONENT_CLUSTER_CFG_JSP_DETAIL_URL,
	        function destroy(data){
	            
	    },row);
}
	
//新增集群小类
function add() {
    showAddDialog("集群管理-新增集群",780, 560,Globals.baseJspUrl.COMPONENT_CLUSTER_CFG_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["serviceTypeGrid"].reload();
                showMessageTips("新增集群成功");
            }
    });
}

/**
 * 修改集群
 * @param index
 */
function edit(index) {
	var row;
    //单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
    if(index!=undefined){
        //单个操作
		row = JsVar["serviceTypeGrid"].getRow(index);
	}else{
        //批量操作
		var rows = JsVar["serviceTypeGrid"].getSelecteds();
	    if (rows.length == 1) {
		    row = rows[0];
	    } else {
            showWarnMessageTips("请选中一条记录!") ;
	        return;
	    }
	}
	showEditDialog("修改集群信息",780, 560,Globals.baseJspUrl.COMPONENT_CLUSTER_CFG_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	                JsVar["serviceTypeGrid"].reload();
	                showMessageTips("修改集群成功!");
	            }
	    },row);
}

//删除主机 
function del(index) {
    var row = JsVar["serviceTypeGrid"].getRow(index);
    if (!row) {
    	row = JsVar["serviceTypeGrid"].getSelected();
    }
    
    if (!row) {
        showWarnMessageTips("请选中一条记录!") ;
    }
    
    showConfirmMessageAlter("确定删除记录？",function ok(){
    	getJsonDataByPost(Globals.baseActionUrl.SERVICE_TYPE_ACTION_DELETE_URL, row, "集群管理-删除集群",
            function(result){
                JsVar["serviceTypeGrid"].reload();
                showMessageTips("删除集群成功!");
            });
    });
}
