//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["programGrid"] = mini.get("programGrid");//取得任务表格 
    JsVar["queryForm"] = new mini.Form("#queryForm");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	JsVar["data"] = data;
	//JsVar["version"] = data["TASK_CODE"].toUpperCase().substring(data["TASK_CODE"].toUpperCase().indexOf("_V") + 1);
	  //加载任务表格信息
    search();
}

//查询
function search() {
	var params = JsVar["queryForm"].getData();
	params["TASK_ID"] = JsVar["data"]["TASK_ID"];
	params["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	
    load(params);
}
//加载表格
function load(param){
    datagridLoad(JsVar["programGrid"],param,"taskProgram.queryProgramListByTaskID");
}

/**
 * 操作按钮渲染
 * @param e
 * @returns {String}
 */
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
		HOST_ID:rowInfo["HOST_ID"],
		PROGRAM_ID:rowInfo["PROGRAM_ID"],
		PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
		PID:rowInfo["PID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
		versionDir:JsVar["version"],
		RUN_STATE:rowInfo["RUN_STATE"]
	};
	
	getJsonDataByPost(Globals.baseActionUrl.OTHER_CHECK_ACTION_MANAGE_URL,obj,"启停管理-检查other周边程序",
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

/**
 * 运行
 * @param index
 */
function submit(index){ 
	var obj = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 0 || rows[i]["RUN_STATE"]==null){
            	obj.push({
            		TASK_ID:JsVar["data"]["TASK_ID"],
            		HOST_ID:rows[i]["HOST_ID"],
            		PROGRAM_ID:rows[i]["PROGRAM_ID"],
            		PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
            		PID:rows[i]["PID"],
            		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		versionDir:JsVar["version"]
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
    var is_param = rows[0]["IS_PARAM"];
    var info = {};
    if(is_param == 1){
         info["IS_PARAM"] = is_param;
         info["PARAM_DESC"] = rows[0]["PARAM_DESC"];
         info["obj"] = obj;
    	showDialog("运行",450,200,Globals.baseJspUrl.OTHER_PROGRAM_JSP_INPUT_PARAM_URL,
    	        function destroy(data){
    				if(data ==systemVar.SUCCESS){
    					JsVar["programGrid"].reload();
                        showMessageTips("运行成功!");
    				}
		    		
    	    },info);
    }else{
       info["list"] = obj;
    	postAjaxParam(info);
    }
}
/**
 * 提交
 */
function postAjaxParam(obj){
	showConfirmMessageAlter("确定启动运行该程序吗？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.OTHER_RUN_ACTION_MANAGE_URL,obj,"启停管理-运行other周边程序",
	        function(result){
				if(result["flag"]=='error'){
					  //添加可拖动大小
				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
				        function destroy(data){
			    			JsVar["programGrid"].reload();
				    },result,opt);
				}else{
					if(obj["IS_PARAM"] == 1){
						closeWindow(systemVar.SUCCESS);
					}
					  //添加可拖动大小
				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
				    result["info"] ="运行成功";
					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
				        function destroy(data){
			    			JsVar["programGrid"].reload();
				    },result,opt);
				}
	        });
	});
}

/**
 * 停止
 * @param index
 */
function stop(index){
	var obj = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 1){
            	obj.push({
	            	TASK_ID:JsVar["data"]["TASK_ID"],
	            	HOST_ID:rows[i]["HOST_ID"],
	            	PROGRAM_ID:rows[i]["PROGRAM_ID"],
	            	PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
	            	PID:rows[i]["PID"],
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
		getJsonDataByPost(Globals.baseActionUrl.OTHER_STOP_ACTION_MANAGE_URL,obj,"启停管理-停止运行other周边程序",
	        function(result){
				if(result["flag"]=='error'){
					  //添加可拖动大小
				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
				        function destroy(data){
			    			JsVar["programGrid"].reload();
				    },result,opt);
				}else{
					JsVar["programGrid"].reload();
					  //添加可拖动大小
				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
				    result["info"] ="运行成功";
					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
				        function destroy(data){
			    			JsVar["programGrid"].reload();
				    },result,opt);
				}
	        });
	});
}