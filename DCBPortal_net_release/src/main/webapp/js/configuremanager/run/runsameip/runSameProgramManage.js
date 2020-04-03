//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //取得任务表格 
    JsVar["programGrid"] = mini.get("programGrid");
    //程序管理表单
    JsVar["programForm"] = new mini.Form("programForm");
    /**
     * 编辑表格前发生
     */
    JsVar["programGrid"].on("cellbeginedit", function (e) {
    	JsVar["onBeginValue"] = e.value;
    });
    /**
     * 编辑表格后发生
     */
    JsVar["programGrid"].on("cellendedit", function (e) {
         var field = e.field;
         var value = e.value;
         var row = e.row;
         var param ={};
         if(isEmptyStr(row.ID)){
        	 param = row;
        	 param["flag"] = "add";
        	 param["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
        	 param["PROGRAM_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
        	 param["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
        	 param["TASK_ID"] = JsVar["data"]["TASK_ID"]; 
        	 param["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
        	 param["VERSION"] = JsVar["version"];
         }else{
        	 param["flag"] = "update";
        	 if(field == "PROGRAM_ALIAS"){
        		 param["PROGRAM_ALIAS_CELL"] = "PROGRAM_ALIAS";
        		 param["PROGRAM_ALIAS"] = value;
        	 }
        	 if(field == "PROGRAM_DESC"){
        		 param["PROGRAM_DESC_CELL"] = "PROGRAM_DESC";
        		 param["PROGRAM_DESC"] = value;
        	 }
        	 if (field == "SCRIPT_SH_NAME") {
                 //程序运行中，脚本不能修改，不然出现程序无法停止掉
                 if (row["RUN_STATE"] == "1") {
                     showWarnMessageTips("当前程序正在运行，无法修改!");
                     JsVar["programGrid"].updateRow(row, {"SCRIPT_SH_NAME":JsVar["onBeginValue"]});
                     return;
                 }
        	 	param["SCRIPT_SH_NAME_CELL"] = "SCRIPT_SH_NAME";
        	 	param["SCRIPT_SH_NAME"] = value;
			 }
        	 if(JsVar["onBeginValue"] == value){
        		 return;
        	 }
             param["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
             param["TASK_ID"] = JsVar["data"]["TASK_ID"];
             param["ID"] = row.ID;
         }
         getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_UPDATE_ACTION_CELL_URL, param, "程序启停管理-修改",
        			function success(result){
        	 			//JsVar["programGrid"].setShowModified(false);
        	       	}
        		);
    });

    //获取跳转页面参数数
	   getForwardParams();
    
});

// 获取页面跳转让
function getForwardParams(){
    var data = JSON.parse(getQueryString("data"));
    JsVar["data"] = data;
    JsVar["version"] = JsVar["data"]["VERSION"];
    $("#versionTD").html("V"+JsVar["version"]);


    var tableName = window.parent.mini.get("deploy_tabs").getActiveTab().title;
    var title = "<span style='color: red;'>（"+tableName+"）</span>" + "<a class=\"mini-button mini-button-green\" style='margin-top: -5px;color:white;' onclick=\"javascript:remove()\" plain=\"false\">返回</a>"
    var panelName = mini.get("programPanel").title +title;
    mini.get("programPanel").set({title:panelName});
    //加载任务表格信息
    load({});
    //程序类型
    loadProgramName();
    //加载配置文件
    loadConfigList();
    //记载当前集群主机列表
    loadClusterHostList();
    //本地网
    loadLatnList();
    mini.parse();

}

function remove() {
    var index = window.parent.mini.get("deploy_tabs").getActiveTab().titleField;
    var activeTab = window.parent.mini.get("deploy_tabs").getTab(index-1);
    var removeTab = window.parent.mini.get("deploy_tabs").getActiveTab();
    window.parent.mini.get("deploy_tabs").activeTab(activeTab);
    window.parent.mini.get("deploy_tabs").removeTab(removeTab);
}
function loadPage() {
}

function beforenodeselect(e) {
    //禁止选中父节点
    if (e.isLeaf == false) e.cancel = true;
}

/**
 * Panel面板折叠
 * @param e
 */
function addBtnClick(e) {
	setTimeout(function() {
		mini.parse();
	}, 200);
}


function loadLatnList(){
	comboxLoad(mini.get("LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
	comboxLoad(mini.get("QUERY_LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
}

//查询
function search(isCheckR) {
	var params = {};
	//版本信息
	params["QUERY_PROGRAM_NAME"] = mini.get("QUERY_PROGRAM_NAME").getValue();
	//集群ID
	var hostIds = mini.get("QUERY_HOST_ID").getValue();
	//程序状态
	params["QUERY_PROGRAM_STATE"] = mini.get("QUERY_PROGRAM_STATE").getValue();
	if(isCheckR){
		//
		params["isCheckR"] = false;
	}else{
		params["isCheckR"] = $("#isCheckR").is(':checked');
	}
	//本地网
	var latns = mini.get("QUERY_LATN_ID").getValue();
	
	params["QUERY_LATN_IDS"] = formatIn(latns);
	params["QUERY_HOST_IDS"] = formatIn(hostIds);
 
	//查询数据
    load(params);
}

function formatIn(objStr){
	if(isEmptyStr(objStr)){
		return null;
	}
	var ids = objStr.split(',');
	var inStr='';
	for(var i = 0 ; i < ids.length;i++){
		inStr += '\'' + ids[i] +'\','
	}
	if(inStr.lastIndexOf(',') > 0){
		inStr = inStr.substring(0,inStr.length-1);
	}
	return inStr;
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
	//版本
	params["VERSION"] = JsVar["version"];
	
	//datagridLoad(JsVar["programGrid"],params,null,Globals.baseActionUrl.BUS_PROGRAM_LIST_ACTION_MANAGE_URL);
	
	getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_LIST_ACTION_MANAGE_URL, params, "程序启停管理-查询不分IP启停程序列表",
		function success(result){
			if (result != null) {
				JsVar["programGrid"].setData(result["PROGRAM_LIST"]);
				$('#runStatus').html(result["runStatus"]);
				$('#stopStatus').html(result["stopStatus"]);
				$('#countRow').html(result["countRow"]);
				if(result["cfgFilePath"]){
					$('#cfgPathTD').html(result["cfgFilePath"]);
				}else{
					$('#cfgPathTD').html("未知");
				}
				
				if(params["isCheckR"]){
					querycheckHostState();
				}
				mini.parse();
			}
       	}
	);
}


/**
 * 加载当前集群主机列表
 */
function loadClusterHostList() {
	var params = {
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"]
	};
	comboxLoad(mini.get("HOST_ID"), params, "deployHome.queryDeployHostByCodeAndHost");
	comboxLoad(mini.get("QUERY_HOST_ID"), params, "deployHome.queryDeployHostByCodeAndHost");
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
	//comboxLoad(mini.get("QUERY_PROGRAM_NAME"), params, "programDefine.queryProgramDefineList", "", "", false);
	
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
	JsVar["PROGRAM_NAME"] = e.selected["PROGRAM_NAME"];
	JsVar["PROGRAM_CODE"] = e.selected["PROGRAM_CODE"];
	JsVar["PROGRAM_STATE"] = e.selected["PROGRAM_STATE"];
	mini.get("CONFIG_FILE").setValue("");
	$("#exampleSh").html(e.selected["SCRIPT_SH_EXAMPLE"]);
}

/**
 * 选择配置文件
 */
function changeConfigFile(e) {
	var configList = e.value;
	if(isEmptyStr(configList)){
		mini.get("SCRIPT_SH_NAME").setValue(JsVar["SCRIPT_SH_NAME"]);
		return;
	}
	configList = configList.split(",");
	var fileNames = "";
	for (var i=0; i<configList.length; i++) {
			var fileName =   configList[i].replace(JsVar["configFilePath"],"");
			fileNames += "$P/" + fileName + ",";
	}
	if(fileNames.lastIndexOf(",") > 0){
		fileNames = fileNames.substring(0,fileNames.length-1);
	}
	var scriptShName = mini.get("SCRIPT_SH_NAME").getValue();
	if (scriptShName != null && scriptShName != '') {
		var newScriptShName = scriptShName.substr(0, scriptShName.indexOf("\"") + 1);
		var finalNewName = newScriptShName + fileNames + "\"";
		mini.get("SCRIPT_SH_NAME").setValue(finalNewName);
	} else {
		mini.get("SCRIPT_SH_NAME").setValue(fileNames);
	}
}

/**
 * 选择本地网时发生
 * @param e
 */
function changeLatnId(e){
	JsVar["COMBOX_LATN_ID"] =e.value;
	mini.get("SCRIPT_SH_NAME").setValue(JsVar["SCRIPT_SH_NAME"]);
	mini.get("CONFIG_FILE").setValue("");
	loadConfigList();
}


/**
 * 加载配置文件列表
 */
function loadConfigList() {
	var params = {
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
		VERSION:JsVar["data"]["VERSION"],
		HOST_ID:JsVar["data"]["HOST_ID"],
		HOST_IP:JsVar["data"]["HOST_IP"],
		TASK_ID:JsVar["data"]["TASK_ID"],
		TASK_CODE:JsVar["data"]["TASK_CODE"],
		BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"],
		NAME:JsVar["data"]["NAME"],
		PACKAGE_TYPE:JsVar["data"]["PACKAGE_TYPE"],
		fileFlag:"ALL"
	};
	if(JsVar["COMBOX_LATN_ID"]){
		params["LATN_ID"] = JsVar["COMBOX_LATN_ID"];
	}
	getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_FILES_ACTION_MANAGE_URL, params, "程序启停管理-获取不分IP启停程序配置文件",
    		function success(result){
    			if (result != null) {
    				//mini.get("CONFIG_FILE").setData(result["FILES_LIST"]);
    				mini.get("CONFIG_FILE").tree.loadList(result["FILES_LIST"], "currId", "parentId");
    				
    				JsVar["configFilePath"] = result["configFilePath"];
    			}
           	});
}

//渲染操作按钮
function onActionRenderer(e) {
    var RUN_STATE = e.record.RUN_STATE;
    var HOST_ID = e.record.HOST_ID;
    var index = e.rowIndex;
    var html= '<a class="Delete_Button" href="javascript:copyRow(' + index + ')">复制</a>';
    html += '<a class="Delete_Button" href="javascript:terimal(\'' + HOST_ID + '\')">终端</a>';
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
    	html += '<a class="Delete_Button" href="javascript:submit(' + index + ')">运行</a>';
    	html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
    	html+= '<a class="Delete_Button" href="javascript:delProgramTask(' + index + ')">删除</a>';
    }else if(RUN_STATE == 1){//已启用， 运行中
    	html+= '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
    	html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
    }
    html += '<a class="Delete_Button" href="javascript:logDetail(' + index + ')">日志</a>';
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
 * 终端操作
 * @param hostId
 */
function terimal(hostId) {
    //获取主机ID
    var hostStr = "'" + hostId + "'";
    goTermainal(hostStr);
}

/**
 * 跳转到主机终端
 * @param hostStr
 */
function goTermainal(hostStr) {
    $("#termialHost").val(hostStr);
    $("#termialForm").attr("action", Globals.baseActionUrl.HOST_ACTION_TERMINAL_URL);
    $("#termialForm").submit();
}

/**
 * 批量终端操作
 */
function batchTermal() {
    var selProgramList = JsVar["programGrid"].getSelecteds();
    if (selProgramList == null || selProgramList.length == 0) {
        showWarnMessageTips("请选择至少一个终端主机!");
        return;
	}
	var hostIdStr = "";
	for (var i=0; i<selProgramList.length; i++) {
        hostIdStr += "'" + selProgramList[i]["HOST_ID"] + "',";
	}
    var finalHostIdStr = hostIdStr.substring(0, hostIdStr.length - 1);
	goTermainal(finalHostIdStr);
}

/**
 * 复制一行数据
 */
function copyRow(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	rowInfo["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
	rowInfo["PROGRAM_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
	rowInfo["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"],
	rowInfo["TASK_ID"] = JsVar["data"]["TASK_ID"];
	rowInfo["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
	rowInfo["VERSION"] = JsVar["version"];
	rowInfo["NAME"] = JsVar["data"]["NAME"],  
	rowInfo["PACKAGE_TYPE"] = JsVar["data"]["PACKAGE_TYPE"],
	showAddDialog("业务程序操作",700,450,Globals.baseJspUrl.RUN_SAME_IP_ADD_JSP_MANAGE_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	search(true);
	    			//加载配置文件
	    		    loadConfigList();
	                showMessageTips("新增成功!");
	            }
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
    programForm["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"],
    programForm["TASK_ID"] = JsVar["data"]["TASK_ID"];
    programForm["PROGRAM_NAME"] = JsVar["PROGRAM_NAME"];
    programForm["PROGRAM_CODE"] = JsVar["PROGRAM_CODE"];
    programForm["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
    programForm["VERSION"] = JsVar["version"];
    var safx = "";
    if(programForm["SCRIPT_SH_NAME"].indexOf("$P") > -1){
        safx = "$P";
	}
    programForm["CONFIG_FILE"] = programForm["CONFIG_FILE"].replaceAll(JsVar["configFilePath"],safx);
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL, programForm, "程序启停管理-添加不分IP启停程序实例",
		function success(result){
    		if (result != null && result["RST_CODE"] == busVar.SUCCESS) {
                showMessageAlter(result["RST_STR"]);
    			search(true);
    			//加载配置文件
    		    loadConfigList();
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
		var isdel= false;
		var rows = JsVar["programGrid"].getSelecteds();
	    if (rows.length > 0) {
	    	 for(var i = 0 ; i < rows.length ;i++){
	    		 if(rows[i]["RUN_STATE"] == 1){
                     showMessageTips("当前选中的程序实例中有正在运行， 不能删除！");
	    				isdel = true;
	    				break;
	    			}
	    		 var obj = {
	    					ID:rows[i]["ID"],
	    					SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
	    					TASK_ID:JsVar["data"]["TASK_ID"],
	    					HOST_ID:rows[i]["HOST_ID"],
					 		LATN_ID:rows[i]["LATN_ID"],
                     		PROGRAM_DESC:rows[i]["PROGRAM_DESC"],
	    					PROGRAM_ID:rows[i]["PROGRAM_ID"],
	    					PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
	    					PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
	    					PROGRAM_ALIAS:rows[i]["PROGRAM_ALIAS"],
	    					CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
	    					CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
	    					VERSION:JsVar["version"],
	    					RUN_STATE:rows[i]["RUN_STATE"],
	    					BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
	    				};
	    		 paramList.push(obj);
		    	 }
	    	 if(isdel){
	    		 return;
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
					HOST_ID:rowInfo["HOST_ID"],
					LATN_ID:rowInfo["LATN_ID"],
                	PROGRAM_DESC:rowInfo["PROGRAM_DESC"],
					PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
					PROGRAM_ID:rowInfo["PROGRAM_ID"],
					PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
					PROGRAM_ALIAS:rowInfo["PROGRAM_ALIAS"],
					CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
					CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
					VERSION:JsVar["version"],
					RUN_STATE:rowInfo["RUN_STATE"],
					BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
			};
			
			 paramList.push(params);
	}
	showConfirmMessageAlter("确定删除选中的实例？",function ok(){
		
		getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_DEL_ACTION_MANAGE_URL, paramList, "程序启停管理-删除不分IP启停程序实例",
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
					showMessageTips(message);
					search(true);
					//加载配置文件
				    loadConfigList();
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
	//加载配置文件
    loadConfigList();
}

/**
 * 检查主机运行状态
 * @param index
 */
function checkHostState(index){
	var paramList=[];
	if(index == undefined){
		var rows = JsVar["programGrid"].getSelecteds();
	    if (rows.length > 0) {
	    	 for(var i = 0 ; i < rows.length ;i++){
	    		 var obj = {
	    					ID:rows[i]["ID"],
	    					TASK_ID:JsVar["data"]["TASK_ID"],
	    					HOST_ID:rows[i]["HOST_ID"],
	    					PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
	    					PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
	    					SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
	    					CONFIG_FILE:rows[i]["CONFIG_FILE"],
	    					CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
	    					CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
	    					VERSION:JsVar["version"],
	    					RUN_STATE:rows[i]["RUN_STATE"],
	    					BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
	    				};
	    		 paramList.push(obj);
	    	 }
	    }else{
            showWarnMessageTips("请选中一条！");
	    	return;
	    }
	}else{
		
	
		var rowInfo = JsVar["programGrid"].getRow(index);
		var obj = {
			ID:rowInfo["ID"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			HOST_ID:rowInfo["HOST_ID"],
			PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
			PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
			SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
			CONFIG_FILE:rowInfo["CONFIG_FILE"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
			VERSION:JsVar["version"],
			RUN_STATE:rowInfo["RUN_STATE"],
			BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
		};
		 paramList.push(obj);
	}
	//rowInfo["versionDir"] = JsVar["version"];	
	getJsonDataByPost(Globals.baseActionUrl.COMMON_IP_CHECK_ACTION_MANAGE_URL, paramList, "程序启停管理-检查不分IP启停程序实例状态",
		function(result){
		
		     if(index == undefined){
		    	 search(true);
		    	 showMessageTips("批量检查状态已完成并已同步数据库，请查看！");
		    	 return;
		     }
		    
	    		 if(result["state"] == "1"){
	 				if(result["info"] != null){
                        showMessageAlter(result["info"]);
	 				}else{
	 					showMessageAlter("当前程序正在运行，进程号为【 " + result["process"] + "】!");
	 				}
	 			} else if(result["state"] == "0"){
	 				if(result["info"] != null){
                        showMessageAlter(result["info"]);
	 				}else{
	 					showMessageTips("当前程序未运行!");
	 				}
	 			} else if(result["state"] == "3") {
                     showWarnMessageTips("该主机不存在该程序");
	 			} else if (result["state"] == "4") {
	 				if(result["info"] != null){
	 					showErrorMessageAlter(result["info"]);
	 				} else {
	 					showErrorMessageAlter("程序状态检查失败！");
	 				}
	 			}
	 			search(true);
		     
			
    	}
	);
}

/**
 * 批量运行
 */
function addBatchRun() {
	submit();
}

/**
 * 启动程序
 * @param index
 */
function submit(index){
	var hostArray = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 0 || rows[i]["RUN_STATE"] == null){
            	/*hostArray.push({
            		ID:rows[i]["ID"],
            		TASK_ID:JsVar["data"]["TASK_ID"],
            		HOST_ID:rows[i]["HOST_ID"],
            		PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
            		PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
            		SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
            		CONFIG_FILE:rows[i]["CONFIG_FILE"],
            		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
            		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		PROGRAM_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"],
                    PROGRAM_DESC:rows[i]["PROGRAM_DESC"],
                    LATN_ID:rows[i]["LATN_ID"],
                    PROGRAM_ALIAS:rows[i]["PROGRAM_ALIAS"],
            		VERSION:JsVar["version"]
            	});*/
                rows[i]["TASK_ID"] = JsVar["data"]["TASK_ID"];
                rows[i]["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
                rows[i]["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
                rows[i]["PROGRAM_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
                rows[i]["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
                rows[i]["VERSION"] = JsVar["version"];
                hostArray.push(rows[i]);
            }else{
                showWarnMessageTips("选中的程序存在运行中状态的列表，请检查!");
            	return;
            }
        }
    } else {
        showWarnMessageTips("请选中至少一条记录!") ;
        return;
    }
    
	showConfirmMessageAlter("确定启动选中的程序实例？",function ok(){
		var params = {
			CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"],
			HOST_LIST:hostArray
		};
		getJsonDataByPost(Globals.baseActionUrl.COMMON_IP_RUN_ACTION_MANAGE_URL, params, "程序启停管理-启动不分IP启停程序实例",
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

                    //告警提示
                    if (failCnt > 0 && successCnt >0) {
                        result["flag"] = "WARN";
                    } else if (failCnt > 0 && successCnt == 0) {
                        result["flag"] = "error";
                    }
					var params = {
						RST_STR:textValue,
						RST_EXEC:"启动",
						RST_FLAG:result["flag"]
					};
					showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
				        function destroy(data){
						search(true);
				    }, params, {allowDrag:true});
				} else {
					showErrorMessageAlter("业务程序启动失败，请检查数据!");
				}
	        });
	});
}

/**
 * 批量停止
 */
function addBatchStop() {
	stop();
}

/**
 * 单个停止
 * @param index
 */
function stop(index){
	var hostArray = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 1){
            	hostArray.push({
            		ID:rows[i]["ID"],
            		TASK_ID:JsVar["data"]["TASK_ID"],
            		HOST_ID:rows[i]["HOST_ID"],
            		PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
            		PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
            		SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
            		CONFIG_FILE:rows[i]["CONFIG_FILE"],
            		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
            		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
            		VERSION:JsVar["version"]
            	});
            }else{
                showWarnMessageTips("选中的程序存在停止状态的列表，请检查!");
            	return;
            }
        }
    } else {
        showWarnMessageTips("请选中至少一条记录!") ;
        return;
    }
	
	showConfirmMessageAlter("确定停止选中的程序实例？",function ok(){
		var params = {
			CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
			CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
			TASK_ID:JsVar["data"]["TASK_ID"],
			HOST_LIST:hostArray
		};
		getJsonDataByPost(Globals.baseActionUrl.COMMON_IP_STOP_ACTION_MANAGE_URL, params, "程序启停管理-停止不分IP启停程序实例",
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

                    //告警提示
                    if (failCnt > 0 && successCnt >0) {
                        result["flag"] = "WARN";
                    } else if (failCnt > 0 && successCnt == 0) {
                        result["flag"] = "error";
                    }
                    var params = {
                        RST_STR:textValue,
                        RST_EXEC:"停止",
                        RST_FLAG:result["flag"]
                    };
                    showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                        function destroy(data){
                            search(true);
                        }, params, {allowDrag:true});
                } else {
                    showErrorMessageAlter("业务程序停止失败，请检查数据!");
                }
		    });
	});
}


/**
 * 检查主机运行状态
 * @param index
 */
function querycheckHostState(){
	var paramList=[];
	 
		var rows = JsVar["programGrid"].getData();;
	    if (rows.length > 0) {
	    	 for(var i = 0 ; i < rows.length ;i++){
	    		 var obj = {
	    					ID:rows[i]["ID"],
	    					TASK_ID:JsVar["data"]["TASK_ID"],
	    					HOST_ID:rows[i]["HOST_ID"],
	    					PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
	    					PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
	    					SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
	    					CONFIG_FILE:rows[i]["CONFIG_FILE"],
	    					CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
	    					CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
	    					VERSION:JsVar["version"],
	    					RUN_STATE:rows[i]["RUN_STATE"],
	    					BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"]
	    				};
	    		 paramList.push(obj);
	    	 }
	    } 
	 
	 
	getJsonDataByPost(Globals.baseActionUrl.COMMON_IP_CHECK_ACTION_MANAGE_URL, paramList, "程序启停管理-检查不分IP启停程序实例状态",
		function(result){
			search(true);	
    	}
	);
}


