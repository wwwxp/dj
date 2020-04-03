//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //取得任务表格
    JsVar["programGrid"] = mini.get("programGrid");
    //程序管理表单
    JsVar["programForm"] = new mini.Form("programForm");
});


/**
 * Panel面板折叠
 * @param e
 */
function addBtnClick(e) {
    setInterval(function() {
        mini.parse();
    }, 200);
}


//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
    JsVar["data"] = data;
    JsVar["version"] = data["VERSION"];
    //加载任务表格信息
    search();
    //程序类型
    loadProgramName();
    //配置文件
    loadConfigList();

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

//重新加载表格
function refresh() {
    load(null);
}

//加载表格
function load(params){
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_LIST_ACTION_MANAGE_URL, params, "路由管理-获取路由程序列表",
        function success(result){
            if (result != null) {
                JsVar["programGrid"].setData(result["PROGRAM_LIST"]);
            }
        });
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {string}
 */
function onActionRenderer(e) {
    var RUN_STATE = e.record.RUN_STATE;
    var index = e.rowIndex;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
        html += '<a class="Delete_Button" href="javascript:submit(' + index + ')">运行</a>';
        html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
        html+= '<a class="Delete_Button" href="javascript:delProgramTask(' + index + ')">删除</a>';
    }else if(RUN_STATE == 1){//已启用， 运行中
        html+= '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
        html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
    }
    return html;
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
 * 新增启停程序表单重置
 */
function reset() {
    JsVar["programForm"].reset();
    //程序类型
    loadProgramMemberType();
    //加载配置文件
    loadConfigList();
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


/**
 * 选择配置文件
 */
function changeConfigFile(e) {
    var configList = e.selecteds;
    var fileNames = "";
    for (var i=0; i<configList.length; i++) {
        if (i != (configList.length - 1)) {
            fileNames += configList[i]["fileName"] + ",";
        } else {
            fileNames += configList[i]["fileName"];
        }
    }
    var scriptShName = JsVar["SCRIPT_SH_NAME"];
    if (scriptShName != null && scriptShName != '') {
        var newScriptShName = scriptShName.substr(0, scriptShName.lastIndexOf("\""));
        var finalNewName = newScriptShName + fileNames + "\"";
        mini.get("SCRIPT_SH_NAME").setValue(finalNewName);
    } else {
        mini.get("SCRIPT_SH_NAME").setValue(fileNames);
    }
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
        NAME:JsVar["data"]["NAME"]
    };
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_FILES_ACTION_MANAGE_URL, params, "采集管理-获取采集配置文件",
        function success(result){
            if (result != null) {
                mini.get("CONFIG_FILE").setValue("");
                mini.get("CONFIG_FILE").setData(result["FILES_LIST"]);
            }
        });
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
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL, programForm, "采集管理-添加采集程序",
        function success(result){
            if (result != null && result["RST_CODE"] == busVar.SUCCESS) {
                showMessageTips(result["RST_STR"]);
                //加载数据
                search();
                //加载配置文件
                loadConfigList();
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
        getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_DEL_ACTION_MANAGE_URL, params, "启停管理-删除采集程序",
            function(result){
                if (result != null && result["RST_CODE"] == busVar.SUCCESS) {
                    showMessageAlter(result["RST_STR"], function() {
                        search();
                        //加载配置文件
                        loadConfigList();
                    });
                }
            }
        );
    });
}

/**
 * 检查主机运行状态
 * @param index
 */
function checkHostState(index){
    var rowInfo = JsVar["programGrid"].getRow(index);
    var obj = {
        ID:rowInfo["ID"],
        TASK_ID:JsVar["data"]["TASK_ID"],
        HOST_ID:JsVar["data"]["HOST_ID"],
        PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
        PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
        SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
        CONFIG_FILE:rowInfo["CONFIG_FILE"],
        CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
        CLUSTER_ID: JsVar["data"]["CLUSTER_ID"],
        VERSION:JsVar["version"],
        VERSION:JsVar["version"],
        RUN_STATE:rowInfo["RUN_STATE"]
    };
    rowInfo["versionDir"] = JsVar["version"];
    $("#deployTextarea").html("");
    getJsonDataByPost(Globals.baseActionUrl.ROUTE_CHECK_ACTION_MANAGE_URL, obj, "启停管理-检查route程序状态",
        function(result){
            if(result["state"] == "1"){
                if(result["info"] != null){
                    showMessageTips(result["info"]);
                }else{
                    showMessageAlter("当前程序正在运行，进程号为【 " + result["process"] + "】");
                }
            } else if(result["state"] == "0"){
                if(result["info"] != null){
                    showMessageTips(result["info"]);
                }else{
                    showMessageTips("当前程序未运行!");
                }
            } else if(result["state"] == "3"){
                showWarnMessageTips("该主机不存在该程序");
            } else if (result["state"] == "4") {
				if(result["info"] != null){
					showErrorMessageAlter(result["info"]);
				} else {
					showErrorMessageAlter("程序状态检查失败！");
				}
			}
            search();
        });
}

/**
* 运行新增的业务程序
* @param index
*/
function submit(index){
    var hostArray = new Array();
    var rows = JsVar["programGrid"].getSelecteds();
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(rows[i]["RUN_STATE"] == 0 || rows[i]["RUN_STATE"] == null){
                hostArray.push({
                    ID:rows[i]["ID"],
                    TASK_ID:JsVar["data"]["TASK_ID"],
                    HOST_ID:JsVar["data"]["HOST_ID"],
                    PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                    PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                    SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                    CONFIG_FILE:rows[i]["CONFIG_FILE"],
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
        $("#deployTextarea").html("");
        getJsonDataByPost(Globals.baseActionUrl.ROUTE_RUN_ACTION_MANAGE_URL, params, "启停管理-运行route程序",
            function(result){
                if (result != null) {
                    var textValue=result.info + "<br/>返回结果:"+result.reason+"<br/>";
                    textValue=textValue.replaceAll("\n","<br/>");
                    $("#deployTextarea").html(textValue);

                    if (result["flag"] == "error") {
                        showErrorMessageAlter("程序启动失败，请检查！");
                    } else {
                        showMessageTips("程序启动成功！");
                        search();
                    }
                }
            });
    });
}

/**
 * 停止程序
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
                    HOST_ID:JsVar["data"]["HOST_ID"],
                    PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                    PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                    SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                    CONFIG_FILE:rows[i]["CONFIG_FILE"],
                    PID:rows[i]["PID"],
                    CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
                    CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
                    VERSION:JsVar["version"]
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
        $("#deployTextarea").html("");
        getJsonDataByPost(Globals.baseActionUrl.ROUTE_STOP_ACTION_MANAGE_URL, params, "启停管理-停止route程序",
            function(result){
                if (result != null) {
                    var textValue=result.info + "<br/>返回结果:"+result.reason+"<br/>";
                    textValue=textValue.replaceAll("\n","<br/>");
                    $("#deployTextarea").html(textValue);
                    if (result["flag"] == "error") {
                        showErrorMessageAlter("程序停止失败，请检查！");
                    } else {
                        showMessageTips("程序停止成功！");
                        search();
                    }
                }
            });
    });
}


