//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["programGrid"] = mini.get("programGrid");//取得任务表格 
    
    //程序管理表单
    JsVar["programForm"] = new mini.Form("programForm");
    getForwardParams();
});

// 获取页面跳转让
function getForwardParams(){
    var data = JSON.parse(getQueryString("data"));
    JsVar["data"] = data;

	//集群类型
	JsVar["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	//集群ID
	JsVar["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	//任务ID
	JsVar["TASK_ID"] = JsVar["data"]["TASK_ID"];
	//任务编码
	JsVar["TASK_CODE"] = JsVar["data"]["TASK_CODE"];
	//启停版本
	JsVar["version"] = JsVar["data"]["VERSION"];
	//业务包类型
	JsVar["PACKAGE_TYPE"] = JsVar["data"]["PACKAGE_TYPE"];
	//业务主机群ID
	JsVar["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];

    var tableName = window.parent.mini.get("deploy_tabs").getActiveTab().title;
    var title = "<span style='color: red;'>（"+tableName+"）</span>" + "<a class=\"mini-button mini-button-green\" style='margin-top: -5px;color:white;' onclick=\"javascript:remove()\" plain=\"false\">返回</a>"
    var panelName = mini.get("programPanel").title +title;
    mini.get("programPanel").set({title:panelName});
	  //本地网
    loadLatnList();

	//加载任务表格信息
    search();
    //程序类型
	loadProgramName();
}

function remove() {
    var index = window.parent.mini.get("deploy_tabs").getActiveTab().titleField;
    var activeTab = window.parent.mini.get("deploy_tabs").getTab(index-1);
    var removeTab = window.parent.mini.get("deploy_tabs").getActiveTab();
    window.parent.mini.get("deploy_tabs").activeTab(activeTab);
    window.parent.mini.get("deploy_tabs").removeTab(removeTab);
}
function loadLatnList(){
	comboxLoad(mini.get("LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
	comboxLoad(mini.get("QUERY_LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
}

function loadPage() {
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
		PROGRAM_TYPE:JsVar["data"]["CLUSTER_TYPE"],
		TASK_ID:JsVar["TASK_ID"],
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	var programObj = mini.get("PROGRAM_NAME");
	comboxLoad(programObj, params, "programDefine.queryTopologyProgramDefineList", "", "", false);
	var list = programObj.getData();
	if (list != null && list.length > 0) {
		mini.get("PROGRAM_NAME").select(0);
		mini.get("SCRIPT_SH_NAME").setValue(list[0]["SCRIPT_SH_NAME"]);
		
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
	
	JsVar["PROGRAM_NAME"] = e.selected["PROGRAM_NAME"];
	JsVar["PROGRAM_CODE"] = e.selected["PROGRAM_CODE"];
	JsVar["PROGRAM_STATE"] = e.selected["PROGRAM_STATE"];
	$("#exampleSh").html(e.selected["SCRIPT_SH_EXAMPLE"]);
}

//查询
function search(isCheckR) {
	var params = {};
	//版本信息
	params["QUERY_PROGRAM_NAME"] = mini.get("QUERY_PROGRAM_NAME").getValue();
	 
	//程序状态
	params["QUERY_PROGRAM_STATE"] = mini.get("QUERY_PROGRAM_STATE").getValue();
	if(isCheckR){
		//
		params["isCheckR"] = false;
	}else{
		params["isCheckR"] = $("#isCheckR").is(':checked');
	}
	//本地网
	params["QUERY_LATN_ID"] = mini.get("QUERY_LATN_ID").getValue();
	//加载Topology
    load(params);
}

//加载表格
function load(params){
	//版本信息
	params["TASK_ID"] = JsVar["data"]["TASK_ID"];
	//集群ID
	params["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	//集群类型
	params["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	//主机ID
	params["HOST_ID"] = JsVar["data"]["HOST_ID"];
	
	//datagridLoad(JsVar["programGrid"], params, "taskProgram.queryTopologyTaskList");
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "",
		function success(result){
			if (result != null) {
				JsVar["programGrid"].setData(result);
				if(params["isCheckR"]){
					querycheckHostState();
				}
				
			}
       	},"taskProgram.queryTopologyTaskList"
	);
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
    html+= '<a class="Delete_Button" href="javascript:viewService(' + index + ')">服务查看</a>';
    html+= '<a class="Delete_Button" href="javascript:logDetail(' + index + ')">日志</a>';
    return html;
}

/**
 * 查看程序启停日志信息
 */
function logDetail(index) {
    var rows = JsVar["programGrid"].getSelected();
    if (!rows) {
        showWarnMessageTips("请选择一条记录进行日志查看!");
        return;
    }
    var params = rows;
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_LOG_DETAIL_ACTION_URL, params, "程序启停管理-获取程序启停日志文件信息",
        function success(result){
            if (result != null && result["retCode"] == "1") {
                var textValue = result["retMsg"];
                if (textValue != null) {
                    textValue = textValue.replaceAll("\n","<br/>");
                }
                var params = {
                    RST_STR:textValue,
                    RST_EXEC:"查看日志",
                    RST_FLAG:result["flag"]
                };
                showDialog("日志详情","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                    function destroy(data){

                    }, params, {allowDrag:true});
            } else if (result != null && result["retCode"] == "2") {
                showWarnMessageTips(result["retMsg"]);
            } else {
                showErrorMessageTips(result["retMsg"]);
            }
        });
}

/**
 * 查看Topology服务启动worker信息
 */
function viewService(index) {
	var rowInfo = JsVar["programGrid"].getRow(index);
	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	showDialog("启停管理-查看服务信息", 480, 600, Globals.baseJspUrl.COMMON_RUN_TOPOLOGY_VIEW_SERVICE_JSP_MANAGE_URL,
	        function destroy(data){
	    },rowInfo);
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
    	SCRIPT_SH_NAME:row["SCRIPT_SH_NAME"],
    	PROGRAM_CODE:row["PROGRAM_CODE"],
    	VERSION:JsVar["version"],
    	CONFIG_FILE: JsVar["data"]["CONFIG_FILE"],
    	BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
    };
    showConfirmMessageAlter("确定对该版本进行重新负载？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.TOP_REBALANCE_CONFIG_RELOAD_URL,params,"启停管理-Rebalance重新负载",
            function(result){
                if (result != null) {
                    var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
                    $("#businessTextarea").html(rstInfo);
                }
                mini.parse();
                search(true);
            });
    });

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
	rowInfo["VERSION"] = JsVar["data"]["VERSION"];
	rowInfo["CONFIG_FILE"] = rowInfo["PROGRAM_CODE"]+".conf";
	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	showDialog("启停管理-查看billing定义",900,550,Globals.baseJspUrl.COMMON_RUN_TOPOLOGY_VIEW_JSP_MANAGE_URL,
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
	 }else if(run_state == 0){
		 return "<span class='label label-danger'>未运行</span>";
	 }else{
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
    programForm["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    programForm["HOST_ID"] = JsVar["data"]["HOST_ID"];
    programForm["TASK_ID"] = JsVar["data"]["TASK_ID"];
    programForm["PROGRAM_NAME"] = JsVar["PROGRAM_NAME"];
    programForm["PROGRAM_CODE"] = JsVar["PROGRAM_CODE"];
    programForm["CONFIG_FILE"] = JsVar["PROGRAM_CODE"]+".conf";
    programForm["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
    programForm["VERSION"] = JsVar["data"]["VERSION"];
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL, programForm, "启停管理-添加Topology运行实例",
		function success(result){
			if (result != null && result["RST_CODE"] == "1") {
                showMessageAlter(result["RST_STR"]);
				//重新加载程序列表
				loadProgramName();
				//加载数据
				search(true);
			} else {
				showErrorMessageAlter(result["RST_STR"]);
			}
       	}
    );
}

/**
 * 删除程序管理
 */
function delProgramTask(index) {
	var paramList=[];
	if(index == undefined){
		var rows = JsVar["programGrid"].getSelecteds();
	    if (rows.length > 0) {
	    	var isdelF = false;
	    	 for(var i = 0 ; i < rows.length ;i++){
	    		 if(rows[i]["RUN_STATE"] == 1){
	    				isdelF = true;
	    				break;
	    			}
	    		 
	    		 var obj = {
	    				ID:rows[i]["ID"],
    					SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
    					TASK_ID:JsVar["data"]["TASK_ID"],
    					HOST_ID:JsVar["data"]["HOST_ID"],
    					PROGRAM_ID:rows[i]["PROGRAM_ID"],
    					PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
    					PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
    					CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
    					CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
    					VERSION:JsVar["version"],
    					RUN_STATE:rows[i]["RUN_STATE"],
    					BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
	    				};
	    		 paramList.push(obj);
	    	 }
	    	 if(isdelF){
                 showMessageTips("当前选 中的版本程序实例中存在正在运行， 不能删除，请检查！");
	    		 return ;
	    	 }
	    }else{
            showWarnMessageTips("请选中一条！");
	    	return;
	    }
	}else{
		var rowInfo = JsVar["programGrid"].getRow(index);
		if(rowInfo["RUN_STATE"] == 1){
            showMessageTips("当前版本程序实例正在运行， 不能删除！");
			return;
		}
		var params = {
				ID:rowInfo["ID"],
				SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
				TASK_ID:JsVar["data"]["TASK_ID"],
				HOST_ID:JsVar["data"]["HOST_ID"],
				PROGRAM_ID:rowInfo["PROGRAM_ID"],
				PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
				PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
				CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
				CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
				VERSION:JsVar["version"],
				RUN_STATE:rowInfo["RUN_STATE"],
				BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
			};
		paramList.push(params);
	}
	
	showConfirmMessageAlter("确定删除所有版本该实例？",function ok(){
		
		getJsonDataByPost(Globals.baseActionUrl.BUS_TOPO_PROGRAM_TASK_DEL_ACTION_MANAGE_URL, paramList, "启停管理-删除Topology实例",
			function(result){
				 
				if(result){
					var message='';
					if(result.message_success){
						message +="程序实例删除成功【"+result.message_success+"】";
					}
					if(result.message_fail){
						if(result.message_success){
							message +="，程序实例删除失败【"+result.message_fail+"】";
						}else{
							message +="程序实例删除失败【"+result.message_fail+"】";
						}
						
					}
					
				}
				if (result.message_success) {
					//重新加载程序列表
					loadProgramName();
					//刷新数据
					search(true);
					showMessageTips(message);
				}else{
					showErrorMessageAlter(message);
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
	var paramList=[];
	if(index == undefined){
		var rows = JsVar["programGrid"].getSelecteds();
	    if (rows.length > 0) {
	    	 for(var i = 0 ; i < rows.length ;i++){
	    		 var rowInfo = rows[i];
	    		    rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	    			rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	    			rowInfo["versionDir"] = JsVar["version"];
	    			rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
	    		 paramList.push(rowInfo);
	    	 }
	    }else{
            showWarnMessageTips("请选中一条！");
	    	return;
	    }
	}else{
		var rowInfo = JsVar["programGrid"].getRow(index);
		rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
		rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
		rowInfo["versionDir"] = JsVar["version"];
		rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
		paramList.push(rowInfo);
	}
		
	getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_CHECK_PROGRAM_ACTION_MANAGE_URL,paramList,"启停管理-Topology运行状态检查",
	        function(result){
				if (result != null) {
					var message = '';
					for(var i = 0 ; i < result.length ;i++){
						message += result[i].info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result[i].reason.replaceAll("\n", "<br/>")+"<br/>" ;
					}
					
					//var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
					$("#businessTextarea").html(message);
					mini.parse();
					search(true);
				}
				
		});
}

function querycheckHostState(){
	var paramList=[];
	var rows = JsVar["programGrid"].getData();
    if (rows.length > 0) {
    	 for(var i = 0 ; i < rows.length ;i++){
    	 	var rowInfo = rows[i];
			rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
			rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
			rowInfo["versionDir"] = JsVar["version"];
			rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
			paramList.push(rowInfo);
    	 }
    	 getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_CHECK_PROGRAM_ACTION_MANAGE_URL,paramList,"启停管理-Topology运行状态检查",
			function(result){
    	 		search(true);
    	 });
    } 
}

/**
 * 批量运行
 */
function addBatchRun() {
    submit();
}

/**
 * 运行
 * @param index
 */
function submit(index){
    // var rowInfo = JsVar["programGrid"].getRow(index);
    // var pro_code=rowInfo.PROGRAM_CODE;
    // if(rowInfo["RUN_STATE"] == 0 || rowInfo["RUN_STATE"] == null){
    // 	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    // 	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    // 	rowInfo["versionDir"] = JsVar["version"];
    // 	rowInfo["RUN_STATE"] = 1;
    // 	rowInfo["NAME"] = JsVar["data"]["NAME"];
    // 	rowInfo["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
    // 	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    // }else{
    //     showWarnMessageTips("选中的程序存在运行中状态的列表，请检查!");
    // 	return;
    // }

    var rows = JsVar["programGrid"].getSelecteds();
    if (rows != null && rows.length > 0) {
        for (var i=0; i<rows.length; i++) {
            if (rows[i]["RUN_STATE"] == 1) {
                showWarnMessageTips("选中的程序存在运行状态的列表，请检查!");
                return;
            }
            rows[i]["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
            rows[i]["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
            rows[i]["versionDir"] = JsVar["version"];
            rows[i]["RUN_STATE"] = 1;
            rows[i]["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
            rows[i]["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
        }
    } else {
        showWarnMessageTips("请选中至少一条记录!");
        return;
    }
	showConfirmMessageAlter("确定启动运行该程序吗？",function ok(){
        var params = {
            CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
            CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            versionDir:JsVar["version"],
            RUN_STATE:1,
            BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
            TOPOLOGY_LIST:rows
        };
		getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_RUN_TASK_ACTION_MANAGE_URL, params, "启停管理-运行Topology",
	        function(result){
                if (result != null && result["RET_CODE"] == busVar.SUCCESS) {
                    //启动程序实例个数
                    var totalCnt = result["TOTAL_CNT"];
                    //启动成功程序实例个数
                    var successCnt = result["SUCCESS_CNT"];
                    //启动失败程序实例个数
                    var failCnt = result["FAIL_CNT"];
                    //启动总耗时
                    var totalTimes = result["TOTAL_TIMES"];
                    //启动程序详细信息
                    var retData = result["RET_DATA"];

                    var textValue = "";
                    textValue += "本次启动程序实例: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + totalCnt + "</font> ]个,"
                        + "成功: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + successCnt + "</font> ]个,"
                        + "失败: [ <font color='red' style='font-weight:bold;font-size: 14px;'>" + failCnt + "</font> ]个,"
                        + "耗时: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + totalTimes + "</font> ]秒";
                    textValue += "<br/><font color='red' style='font-weight:bold;font-size: 14px;'>**************下面是启动实例详细信息************</font>";
                    for (var i=0; i<retData.length; i++) {
                        textValue += "<br/><font color='green' style='font-weight:bold;font-size: 16px;'>" + (i+1) + "、</font>启动结论:" + retData[i]["info"];
                        textValue += "<br/>执行命令: " + (typeof(retData[i]["execCmd"]) == 'undefined' ? "" : retData[i]["execCmd"]);
                        textValue += "<br/>命令结果:" + (typeof(retData[i]["reason"]) == 'undefined' ? "" : retData[i]["reason"]);
                        textValue += "<br/>";
                    }
                    textValue = textValue.replaceAll("\n","<br/>");
                    $("#businessTextarea").html(textValue);
                    mini.parse();
                    search(true);

                    //告警提示
                    // if (failCnt > 0 && successCnt >0) {
                    //     result["flag"] = "WARN";
                    // } else if (failCnt > 0 && successCnt == 0) {
                    //     result["flag"] = "error";
                    // }
                    // var params = {
                    //     RST_STR:textValue,
                    //     RST_EXEC:"启动",
                    //     RST_FLAG:result["flag"]
                    // };
                    // showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                    //     function destroy(data){
                    //         search(true);
                    //     }, params, {allowDrag:true});
                } else {
                    showErrorMessageAlter("业务程序启动失败，请检查数据!");
                }
				// if (result != null) {
				// 	var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
				// 	$("#businessTextarea").html(rstInfo);
				// }
				// mini.parse();
				// search(true);
	    },null,null,true,"后台启动Topology需要一小会，请等待...");
	});
}

function addBatchStop() {
	stop();
}

/**
 * 停止
 * @param index
 */
function stop(index){
    var rows = JsVar["programGrid"].getSelecteds();
    if (rows != null && rows.length > 0) {
    	for (var i=0; i<rows.length; i++) {
    		if (rows[i]["RUN_STATE"] == 0 || rows[i]["RUN_STATE"] == null) {
                showWarnMessageTips("选中的程序存在停止状态的列表，请检查!");
                return;
			}
            rows[i]["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
            rows[i]["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
            rows[i]["versionDir"] = JsVar["version"];
            rows[i]["RUN_STATE"] = 0;
            rows[i]["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
            rows[i]["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
		}
	} else {
        showWarnMessageTips("请选中至少一条记录!");
        return;
	}
    showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
    	var params = {
    		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			versionDir:JsVar["version"],
            RUN_STATE:0,
            BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
			TOPOLOGY_LIST:rows
		};
        getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_STOP_TASK_ACTION_MANAGE_URL, params, "启停管理-停止Topology",
            function(result){
                if (result != null && result["RET_CODE"] == busVar.SUCCESS) {
                    //停止程序实例个数
                    var totalCnt = result["TOTAL_CNT"];
                    //停止成功程序实例个数
                    var successCnt = result["SUCCESS_CNT"];
                    //停止失败程序实例个数
                    var failCnt = result["FAIL_CNT"];
                    //停止总耗时
                    var totalTimes = result["TOTAL_TIMES"];
                    //停止程序详细信息
                    var retData = result["RET_DATA"];
                    var textValue = "";
                    textValue += "本次停止程序实例: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + totalCnt + "</font> ]个,"
                        + "成功: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + successCnt + "</font> ]个,"
                        + "失败: [ <font color='red' style='font-weight:bold;font-size: 14px;'>" + failCnt + "</font> ]个,"
                        + "耗时: [ <font color='green' style='font-weight:bold;font-size: 14px;'>" + totalTimes + "</font> ]秒";
                    textValue += "<br/><font color='red' style='font-weight:bold;font-size: 14px;'>**************下面是停止实例详细信息************</font>";
                    for (var i=0; i<retData.length; i++) {
                        textValue += "<br/><font color='green' style='font-weight:bold;font-size: 14px;'>" + (i+1) + "、</font>停止结论:" + retData[i]["info"];
                        textValue += "<br/>执行命令: " + (typeof(retData[i]["execCmd"]) == 'undefined' ? "" : retData[i]["execCmd"]);
                        textValue += "<br/>命令结果:" + (typeof(retData[i]["reason"]) == 'undefined' ? "" : retData[i]["reason"]);
                        textValue += "<br/>";
                    }
                    textValue = textValue.replaceAll("\n","<br/>");

                    $("#businessTextarea").html(textValue);
                    mini.parse();
                    search(true);

                    //告警提示
                    // if (failCnt > 0 && successCnt >0) {
                    //     result["flag"] = "WARN";
                    // } else if (failCnt > 0 && successCnt == 0) {
                    //     result["flag"] = "error";
                    // }
                    // var params = {
                    //     RST_STR:textValue,
                    //     RST_EXEC:"停止",
                    //     RST_FLAG:result["flag"]
                    // };
                    // showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                    //     function destroy(data){
                    //         search(true);
                    //     }, params, {allowDrag:true});
                } else {
                    showErrorMessageAlter("业务程序停止失败，请检查数据!");
                }

                // if (result != null) {
                //     var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
                //     $("#businessTextarea").html(rstInfo);
                // }
                // mini.parse();
                // search(true);
            },null,null,true,"后台停止Topology需要一小会，请等待...");
    });


	/*var rowInfo = JsVar["programGrid"].getRow(index);
	var pro_code=rowInfo.PROGRAM_CODE;
    if(rowInfo["RUN_STATE"] == 1){
    	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    	rowInfo["versionDir"] = JsVar["version"];
    	rowInfo["RUN_STATE"] = 0;
    	rowInfo["CONFIG_FILE"] = JsVar["data"]["CONFIG_FILE"];
    	rowInfo["BUS_CLUSTER_ID"] = JsVar["BUS_CLUSTER_ID"];
    }else{
        showWarnMessageTips("选中的程序存在停止状态的列表，请检查!");
    	return;
    }
    
	showConfirmMessageAlter("确定停止运行该程序吗？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_STOP_TASK_ACTION_MANAGE_URL,rowInfo,"启停管理-停止Topology",
	        function(result){
				if (result != null) {
					var rstInfo = result.info.replaceAll("\n", "<br/>")+"<br/>返回信息："+result.reason.replaceAll("\n", "<br/>");
					$("#businessTextarea").html(rstInfo);
				}
				mini.parse();
				search(true);
	    },null,null,true,"后台停止该【"+pro_code+"】需要一小会，请等待...");
	});*/
}