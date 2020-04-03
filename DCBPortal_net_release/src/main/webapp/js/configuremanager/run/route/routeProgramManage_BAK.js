//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["programGrid"] = mini.get("programGrid");//取得任务表格 
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	JsVar["data"] = data;
	JsVar["version"] = data["VERSION"];
	//JsVar["version"] = data["TASK_CODE"].toUpperCase().substring(data["TASK_CODE"].toUpperCase().indexOf("_V") + 1);
	  //加载任务表格信息
    search();
}

//查询
function search() {
	var params = {};
	params["TASK_ID"] = JsVar["data"]["TASK_ID"];
	params["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	params["HOST_ID"] = JsVar["data"]["HOST_ID"];
    load(params);
}

//重新加载表格
function refresh() {
    load(null);
}

//加载表格
function load(param){
	datagridLoadPage(JsVar["programGrid"], param, "taskProgram.queryBusProgramList");
}

//渲染操作按钮
function onActionRenderer(e) {
    var RUN_STATE = e.record.RUN_STATE;
    var index = e.rowIndex;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
    	html += '<a class="Delete_Button" href="javascript:submit(' + index + ')">运行</a>';
    }else if(RUN_STATE == 1){//已启用， 运行中
    	html+= '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
    }
    html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
    return html;
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
	 }else if(run_state == 0){
		 return "<span class='label label-danger'>未运行</span>";
	 }else{
		 return "<span class='label label-danger'>未运行</span>";
	 }
}

/**
 * 主机IP渲染
 * @param e
 * @returns {String}
 */
function runHostIpRenderer(e){
	var ip=e.record.HOST_IP;
	var html="";
	html=ip==null?JsVar["data"]["HOST_IP"]:ip;
	return html;
}

/**
 * 检查主机运行状态
 * @param index
 */
function checkHostState(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	var obj = {
		TASK_ID:JsVar["data"]["TASK_ID"],
		HOST_ID:JsVar["data"]["HOST_ID"],
		PROGRAM_ID:rowInfo["PROGRAM_ID"],
		PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
		PID:rowInfo["PID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
		CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
		VERSION:JsVar["version"],
		RUN_STATE:rowInfo["RUN_STATE"]
	};
	rowInfo["versionDir"] = JsVar["version"];	
	getJsonDataByPost(Globals.baseActionUrl.ROUTE_CHECK_ACTION_MANAGE_URL, obj, "启停管理-检查route程序状态",
	        function(result){
		if(result["state"] == "1"){
			if(result["info"] != null){
                showMessageTips(result["info"]);
			}else{
                showMessageAlter("当前程序正在运行，进程号为【 " + result["process"] + "】!");
			}
		}else if(result["state"] == "0"){
			if(result["info"] != null){
                showMessageTips(result["info"]);
			}else{
                showMessageTips("当前程序未运行!");
			}
		}else if(result["state"] == "3"){
            showWarnMessageTips("该主机不存在该程序");
		}
		JsVar["programGrid"].reload();
    });
}

//运行
function submit(index){
	var hostArray = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 0 || rows[i]["RUN_STATE"] == null){
            	hostArray.push({
            		TASK_ID:JsVar["data"]["TASK_ID"],
            		HOST_ID:JsVar["data"]["HOST_ID"],
            		PROGRAM_ID:rows[i]["PROGRAM_ID"],
            		PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
            		PID:rows[i]["PID"],
            		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
            		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		VERSION:JsVar["version"]
            	});
            }else{
                showWarnMessageTips("选中的程序存在运行中状态的列表，请检查!");
            	return;
            }
        }
    }else {
        showWarnMessageTips("请选中一条记录!") ;
        return;
    }
    
	showConfirmMessageAlter("确定启动运行该程序吗？",function ok(){
		var params = {
			CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			HOST_LIST:hostArray
		};
		getJsonDataByPost(Globals.baseActionUrl.ROUTE_RUN_ACTION_MANAGE_URL, params, "启停管理-运行route程序",
	        function(result){
				var textValue=result.info + "<br/>返回结果:"+result.reason+"<br/>";
				textValue=textValue.replaceAll("\n","<br/>");
				$("#deployTextarea").html(textValue);
				JsVar["programGrid"].reload();
//					if(result["flag"]=='error'){
//						  //添加可拖动大小
//					    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
//						showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
//					        function destroy(data){
//				    			JsVar["programGrid"].reload();
//					    },result,opt);
//					}else{
//						JsVar["programGrid"].reload();
//						showMessageAlter("运行成功!");
//					}
	        });
	});
}

//停止
function stop(index){
	var hostArray = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 1){
            	hostArray.push({
            		TASK_ID:JsVar["data"]["TASK_ID"],
            		HOST_ID:JsVar["data"]["HOST_ID"],
            		PROGRAM_ID:rows[i]["PROGRAM_ID"],
            		PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
            		PID:rows[i]["PID"],
            		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
            		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		versionDir:JsVar["version"]
            	});
            }else{
                showWarnMessageTips("选中的程序存在停止状态的列表，请检查!");
            	return;
            }
        }
    }
    else {
        showWarnMessageTips("请选中一条记录!") ;
        return;
    }
	
	showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
		var params = {
			CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			HOST_LIST:hostArray
		};
		getJsonDataByPost(Globals.baseActionUrl.ROUTE_STOP_ACTION_MANAGE_URL, params, "启停管理-停止route程序",
	        function(result){
				var textValue=result.info + "<br/>返回结果:"+result.reason+"<br/>";
				textValue=textValue.replaceAll("\n","<br/>");
				$("#deployTextarea").html(textValue);
				JsVar["programGrid"].reload();
//				if(result["flag"]=='error'){
//					  //添加可拖动大小
//				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
//					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
//				        function destroy(data){
//			    			JsVar["programGrid"].reload();
//				    },result,opt);
//				}else{
//					JsVar["programGrid"].reload();
//					showMessageAlter("停止成功!");
//				}
		    });
	});
}




