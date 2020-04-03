//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["programGrid"] = mini.get("programGrid");//取得任务表格 
    
  //程序管理表单
    JsVar["programForm"] = new mini.Form("programForm");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	//集群类型
	JsVar["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	//集群ID
	JsVar["CLUSTER_ID"] = data["CLUSTER_ID"];
	//任务ID
	JsVar["TASK_ID"] = data["TASK_ID"];
	//任务编码
	JsVar["TASK_CODE"] = data["TASK_CODE"];
	//启停版本
	JsVar["version"] = data["VERSION"];
	//业务主集群ID
	JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
	JsVar["data"] = data;
	//加载任务表格信息
    search();
    
    //程序类型
	loadProgramName();
}

/**
 * Panel面板折叠
 * @param e
 */
function addBtnClick(e) {
	setInterval(function() {
		mini.parse();
	}, 200);
}

/**
 * 加载程序类型
 */
function loadProgramName() {
	var params = {
		PROGRAM_TYPE:JsVar["data"]["CLUSTER_TYPE"]
	};
	var programObj = mini.get("PROGRAM_NAME");
	comboxLoad(programObj, params, "programDefine.queryProgramDefineList", "", "", false);
	var list = programObj.getData();
	if (list != null && list.length > 0) {
		mini.get("PROGRAM_NAME").select(0);
		mini.get("SCRIPT_SH_NAME").setValue(list[0]["SCRIPT_SH_NAME"]);
		JsVar["SCRIPT_SH_NAME"] = list[0]["SCRIPT_SH_NAME"];
		JsVar["PROGRAM_NAME"] = list[0]["PROGRAM_NAME"];
		JsVar["PROGRAM_CODE"] = list[0]["PROGRAM_CODE"];
		JsVar["PROGRAM_STATE"] = list[0]["PROGRAM_STATE"];
		$("#exampleSh").html(list[0]["SCRIPT_SH_EXAMPLE"]);
	}
}

/**
 * 脚本
 * @param e
 */
function changeProgramType(e) {
	var scriptShName = e.selected.SCRIPT_SH_NAME;
	mini.get("SCRIPT_SH_NAME").setValue(scriptShName);
	JsVar["SCRIPT_SH_NAME"] = e.selected["SCRIPT_SH_NAME"];
	JsVar["PROGRAM_NAME"] = e.selected["PROGRAM_NAME"];
	JsVar["PROGRAM_CODE"] = e.selected["PROGRAM_CODE"];
	JsVar["PROGRAM_STATE"] = e.selected["PROGRAM_STATE"];
	$("#exampleSh").html(e.selected["SCRIPT_SH_EXAMPLE"]);
}

//查询
function search() {
	var params = {};
	//版本信息
	params["TASK_ID"] = JsVar["data"]["TASK_ID"];
	//集群ID
	params["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	//集群类型
	params["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	//主机ID
	params["HOST_ID"] = JsVar["data"]["HOST_ID"];
    load(params);
}

//加载表格
function load(param){
	getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_LIST_WITHOUT_HOST_ACTION_MANAGE_URL, param, "billing管理-获取billing程序列表",
    		function success(result){
    			if (result != null) {
    				JsVar["programGrid"].setData(result["PROGRAM_LIST"]);
    			}
           	});
}

/**
 * 操作按钮渲染
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
	var RUN_STATE=e.record.RUN_STATE;
    var index = e.rowIndex;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
    	html+= '<a class="Delete_Button" href="javascript:submit(' + index + ')">运行</a>';
    	html+= '<a class="Delete_Button" href="javascript:delProgramTask(' + index + ')">删除</a>';
    }else if(RUN_STATE == 1){//已启用， 运行中
    	html+= '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
    	html+= '<a class="Delete_Button" href="javascript:rebalance(' + index + ')">重新负载</a>';
    }
    html+= '<a class="Delete_Button" href="javascript:checkState('+index+')">检查</a>';
    html+= '<a class="Delete_Button" href="javascript:viewConf(' + index + ')">查看定义</a>';
    return html;
}

/**
 * 启用Rebalance ，让节点生效
 */
function rebalance(index){
	var row = JsVar["programGrid"].getRow(index);
	var params = {
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["CLUSTER_TYPE"],
    	CONFIG_FILE:row["CONFIG_FILE"],
    	SCRIPT_SH_NAME:JsVar["SCRIPT_SH_NAME"],
    	PROGRAM_CODE:row["PROGRAM_CODE"],
    	VERSION:JsVar["version"],
    	CONFIG_FILE: JsVar["data"]["CONFIG_FILE"],
    	BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
    };
	getJsonDataByPost(Globals.baseActionUrl.TOP_REBALANCE_CONFIG_RELOAD_URL,params,"启停管理-Rebalance重新负载",
        function(result){
			if (result != null) {
				var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
				$("#businessTextarea").html(rstInfo);
			}
			mini.parse();
			search();
	});
		
//	var row = JsVar["programGrid"].getRow(index);
//	var fullName = JsVar["data"]["FILE_NAME"];
//	param["FILE_NAME_DIV"] = fullName.substring(0,fullName.indexOf(".tar.gz"));
//    param["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
//    param["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
//    param["PROGRAM_CODE"] = row["PROGRAM_CODE"];
//    param["version"] = JsVar["version"];
//	
//    // 重新负载
//    showDialog("TOPO管理-重新负载",600,300,Globals.baseJspUrl.TOP_REBALANCE_RELOAD_JSP_MANAGE_URL,
//        function destroy(data){
//    	JsVar["programGrid"].reload();
//    },param);
}

/**
 * 查看定义
 * @param index
 */
function viewConf(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	rowInfo["NAME"] = JsVar["data"]["NAME"];
	rowInfo["CONFIG_FILE"] = JsVar["PROGRAM_NAME"]+".conf";
	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	showDialog("启停管理-查看billing定义",900,550,Globals.baseJspUrl.BILLING_VIEW_JSP_MANAGE_URL,
	        function destroy(data){
	    },rowInfo);
}

/**
 * 运行状态高亮显示
 * @param e
 * @returns
 */
function runStateRenderer(e){
	 var run_state=e.record.RUN_STATE;
	 if(run_state == 1){
		 return "<span class='label label-success'>运行中</span>";
	 } else {
		 return "<span class='label label-danger'>未运行</span>";
	 }
}

/**
 * 添加运行程序
 */
function addProgram() {
	var programForm = JsVar["programForm"].getData();
    JsVar["programForm"].validate();
    if (JsVar["programForm"].isValid() == false){
        return;
    }
    programForm["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    programForm["PROGRAM_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    programForm["HOST_ID"] = JsVar["data"]["HOST_ID"];
    programForm["TASK_ID"] = JsVar["data"]["TASK_ID"];
    programForm["PROGRAM_NAME"] = JsVar["PROGRAM_NAME"];
    programForm["PROGRAM_CODE"] = JsVar["PROGRAM_CODE"];
    programForm["CONFIG_FILE"] = JsVar["PROGRAM_CODE"]+".conf";
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL, programForm, "rent管理-添加rent程序",
    		function success(result){
    			if (result != null && result["RST_CODE"] == "1") {
                    showMessageTips(result["RST_STR"]);
    				//加载数据
    				search();
    			} else {
    				showErrorMessageAlter(result["RST_STR"]);
    			}
    			
           	});
}

/**
 * 删除程序管理
 */
function delProgramTask(index) {
	var rowInfo = JsVar["programGrid"].getRow(index);
	if(rowInfo["RUN_STATE"] == 1){
        showMessageTips("当前版本程序实例正在运行， 不能删除！");
		return;
	}
	showConfirmMessageAlter("确定删除所有版本该实例？",function ok(){
		var params = {
			ID:rowInfo["ID"],
			SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			HOST_ID:JsVar["data"]["HOST_ID"],
			PROGRAM_ID:rowInfo["PROGRAM_ID"],
			PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
			VERSION:JsVar["version"],
			RUN_STATE:rowInfo["RUN_STATE"]
		};
		getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_DEL_ACTION_MANAGE_URL, params, "启停管理-删除rent程序",
			function(result){
				if (result != null && result["RST_CODE"] == busVar.SUCCESS) {
					showMessageAlter(result["RST_STR"], function() {
						search();
					});
				} else {
					showErrorMessageAlter(result["RST_STR"]);
				}
	    	}
		);
	});
}

/**
 * 新增启停程序表单重置
 */
function reset() {
	JsVar["programForm"].reset();
	//程序类型
	loadProgramName();
}

/**
 * 检查状态
 */
function checkState(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	rowInfo["versionDir"] = JsVar["version"];
	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	getJsonDataByPost(Globals.baseActionUrl.BILLING_CHECK_ACTION_MANAGE_URL,rowInfo,"启停管理-billing程序状态检查",
        function(result){
			if (result != null) {
				var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
				$("#businessTextarea").html(rstInfo);
			}
			mini.parse();
			search();
	});
}

/**
 * 运行
 * @param index
 */
function submit(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	var pro_code=rowInfo.PROGRAM_CODE;
    if(rowInfo["RUN_STATE"] == 0 || rowInfo["RUN_STATE"] == null){
    	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    	rowInfo["versionDir"] = JsVar["version"];
    	rowInfo["RUN_STATE"] = 1;
    	rowInfo["NAME"] = JsVar["data"]["NAME"];
    	rowInfo["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
    	rowInfo["SCRIPT_SH_NAME"] = JsVar["SCRIPT_SH_NAME"];
    	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    }else{
        showWarnMessageTips("选中的程序存在运行中状态的列表，请检查!");
    	return;
    }
	showConfirmMessageAlter("确定启动运行该程序吗？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.BILLING_RUN_ACTION_MANAGE_URL,rowInfo,"启停管理-运行billing程序",
	        function(result){
				if (result != null) {
					var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
					$("#businessTextarea").html(rstInfo);
				}
				mini.parse();
				search();
	    },null,null,true,"后台运行该【"+pro_code+"】需要一小会，请等待...");
	});
}

/**
 * 停止
 * @param index
 */
function stop(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	var pro_code=rowInfo.PROGRAM_CODE;
    if(rowInfo["RUN_STATE"] == 1){
    	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    	rowInfo["versionDir"] = JsVar["version"];
    	rowInfo["RUN_STATE"] = 0;
    	rowInfo["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
    	rowInfo["SCRIPT_SH_NAME"] = JsVar["SCRIPT_SH_NAME"];
    	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    }else{
        showWarnMessageTips("选中的程序存在停止状态的列表，请检查!");
    	return;
    }
	showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.BILLING_STOP_ACTION_MANAGE_URL,rowInfo,"启停管理-停止billing程序",
	        function(result){
				if (result != null) {
					var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
					$("#businessTextarea").html(rstInfo);
				}
				mini.parse();
				search();
	    },null,null,true,"后台停止该【"+pro_code+"】需要一小会，请等待...");
	});
}