//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //取得任务表格
    JsVar["programGrid"] = mini.get("programGrid");
    //加载本地网
    loadLatnList();
    //加载主机列表
    loadHostList();
    //加载数据
    search(false)
});

/**
 * 加载本地网
 */
function loadLatnList(){
    comboxLoad(mini.get("QUERY_LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
}

/**
 * 加载当前集群主机列表
 */
function loadHostList() {
    comboxLoad(mini.get("QUERY_HOST_ID"), null, "host.queryHostList");
}

/**
 * 查询业务程序
 */
function search(isCheck) {
    var params = {
        QUERY_PROGRAM_NAME:mini.get("QUERY_PROGRAM_NAME").getValue(),
        QUERY_LATN_ID:mini.get("QUERY_LATN_ID").getValue(),
        QUERY_PROGRAM_STATE:mini.get("QUERY_PROGRAM_STATE").getValue(),
        QUERY_HOST_ID:mini.get("QUERY_HOST_ID").getValue(),
    };
    if (!isCheck) {
        params["isCheckR"] = mini.get("isCheckR").getValue();
    } else {
        params["isCheckR"] = false;
    }
    load(params);
}

//加载表格
function load(params){
    getJsonDataByPost(Globals.baseActionUrl.BUS_USER_START_STOP_LIST_PROGRAM_URL, params, "业务程序启停管理-加载用户权限",
        function success(result){
            if (result != null) {
                JsVar["programGrid"].setData(result["PROGRAM_LIST"]);
                $('#runStatus').html(result["runStatus"]);
                $('#stopStatus').html(result["stopStatus"]);
                $('#countRow').html(result["countRow"]);
                if(params["isCheckR"]){
                    querycheckHostState();
                }
            }
        }
    );
}

//渲染操作按钮
function onActionRenderer(e) {
    var RUN_STATE = e.record.RUN_STATE;
    //是否运行Jstorm
    var runJstorm = e.record.RUN_JSTORM;

    var index = e.rowIndex;
    var html = "";
    //html += '<a class="Delete_Button" href="javascript:terimal(\'' + HOST_ID + '\')">终端</a>';
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
        html += '<a class="Delete_Button" href="javascript:submit(' + index + ')">运行</a>';
        html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
        if (runJstorm == "1") {
            html+= '<a class="Delete_Button" href="javascript:viewConf(' + index + ')">查看定义</a>';
            html+= '<a class="Delete_Button" href="javascript:viewService(' + index + ')">服务查看</a>';
        }
    }else if(RUN_STATE == 1){//已启用， 运行中
        html+= '<a class="Delete_Button" href="javascript:stop(' + index + ')">停止</a>';
        html+= '<a class="Delete_Button" href="javascript:checkHostState(' + index + ')">检查</a>';
        if (runJstorm == "1") {
            html+= '<a class="Delete_Button" href="javascript:viewConf(' + index + ')">查看定义</a>';
            html+= '<a class="Delete_Button" href="javascript:viewService(' + index + ')">服务查看</a>';
        }
    }
    return html;
}

/**
 * 查看定义
 * @param index
 */
function viewConf(index){
    var rowInfo = JsVar["programGrid"].getRow(index);
    rowInfo["CLUSTER_TYPE"] = rowInfo["CLUSTER_TYPE"];
    rowInfo["CLUSTER_ID"] = rowInfo["CLUSTER_ID"];
    rowInfo["NAME"] = rowInfo["NAME"];
    rowInfo["VERSION"] = rowInfo["VERSION"];
    rowInfo["CONFIG_FILE"] = rowInfo["PROGRAM_CODE"]+".conf";
    rowInfo["BUS_CLUSTER_ID"] = rowInfo["BUS_CLUSTER_ID"];
    showDialog("启停管理-查看billing定义",900,550,Globals.baseJspUrl.COMMON_RUN_TOPOLOGY_VIEW_JSP_MANAGE_URL,
        function destroy(data){
        },rowInfo);
}

/**
 * 查看Topology服务启动worker信息
 */
function viewService(index) {
    var rowInfo = JsVar["programGrid"].getRow(index);
    rowInfo["CLUSTER_TYPE"] = rowInfo["CLUSTER_TYPE"];
    rowInfo["CLUSTER_ID"] = rowInfo["CLUSTER_ID"];
    rowInfo["BUS_CLUSTER_ID"] = rowInfo["BUS_CLUSTER_ID"];
    showDialog("启停管理-查看服务信息", 480, 600, Globals.baseJspUrl.COMMON_RUN_TOPOLOGY_VIEW_SERVICE_JSP_MANAGE_URL,
        function destroy(data){

        },rowInfo);
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
                    TASK_ID:rows[i]["TASK_ID"],
                    HOST_ID:rows[i]["HOST_ID"],
                    PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                    PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                    SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                    CONFIG_FILE:rows[i]["CONFIG_FILE"],
                    CLUSTER_TYPE:rows[i]["CLUSTER_TYPE"],
                    CLUSTER_ID: rows[i]["CLUSTER_ID"],
                    VERSION:rows[i]["VERSION"],
                    versionDir:rows[i]["VERSION"],
                    RUN_STATE:rows[i]["RUN_STATE"],
                    BUS_CLUSTER_ID:rows[i]["BUS_CLUSTER_ID"],
                    RUN_JSTORM:rows[i]["RUN_JSTORM"],
                    DIFF_IP:rows[i]["DIFF_IP"]
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
            TASK_ID:rowInfo["TASK_ID"],
            HOST_ID:rowInfo["HOST_ID"],
            PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
            PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
            SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
            CONFIG_FILE:rowInfo["CONFIG_FILE"],
            CLUSTER_TYPE:rowInfo["CLUSTER_TYPE"],
            CLUSTER_ID: rowInfo["CLUSTER_ID"],
            VERSION:rowInfo["VERSION"],
            versionDir:rowInfo["VERSION"],
            RUN_STATE:rowInfo["RUN_STATE"],
            BUS_CLUSTER_ID:rowInfo["BUS_CLUSTER_ID"],
            RUN_JSTORM:rowInfo["RUN_JSTORM"],
            DIFF_IP:rowInfo["DIFF_IP"]
        };
        paramList.push(obj);
    }
    getJsonDataByPost(Globals.baseActionUrl.BUS_USER_CHECK_STATUS_PROGRAM_URL, paramList, "程序启停管理-检查不分IP启停程序实例状态",
        function(result){
            var textValue = result.TOTAL_DESC + "<br/>"+result.TOTAL_MSG+"<br/>";
            textValue=textValue.replaceAll("\n","<br/>");
            var params = {
                RST_STR:textValue,
                RST_EXEC:"启动",
                RST_FLAG:"hidden"
            };
            showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                function destroy(data){
                    search(true);
                }, params, {allowDrag:true});



            // if(index == undefined){
            //     search(true);
            //     showMessageTips("批量检查状态已完成并已同步数据库，请查看！");
            //     return;
            // }
            //
            // if(result["state"] == "1"){
            //     if(result["info"] != null){
            //         showMessageTips(result["info"]);
            //     }else{
            //         showMessageAlter("当前程序正在运行，进程号为【 " + result["process"] + "】!");
            //     }
            // } else if(result["state"] == "0"){
            //     if(result["info"] != null){
            //         showMessageTips(result["info"]);
            //     }else{
            //         showMessageTips("当前程序未运行!");
            //     }
            // } else if(result["state"] == "3") {
            //     showWarnMessageAlter("该主机不存在该程序");
            // } else if (result["state"] == "4") {
            //     if(result["info"] != null){
            //         showErrorMessageAlter(result["info"]);
            //     } else {
            //         showErrorMessageAlter("程序状态检查失败！");
            //     }
            // }
            // search(true);
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
                hostArray.push({
                    ID:rows[i]["ID"],
                    TASK_ID:rows[i]["TASK_ID"],
                    HOST_ID:rows[i]["HOST_ID"],
                    HOST_IP:rows[i]["HOST_IP"],
                    PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                    PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                    SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                    CONFIG_FILE:rows[i]["CONFIG_FILE"],
                    CLUSTER_ID:rows[i]["CLUSTER_ID"],
                    CLUSTER_TYPE:rows[i]["CLUSTER_TYPE"],
                    PROGRAM_TYPE:rows[i]["CLUSTER_TYPE"],
                    BUS_CLUSTER_ID:rows[i]["BUS_CLUSTER_ID"],
                    VERSION:rows[i]["VERSION"],
                    RUN_JSTORM:rows[i]["RUN_JSTORM"],
                    DIFF_IP:rows[i]["DIFF_IP"],
                    versionDir:rows[i]["VERSION"]
                });
            }else{
                showWarnMessageTips("选中的程序存在运行中状态的列表，请检查!");
                return;
            }
        }
    } else {
        showWarnMessageTips("请选中至少一条记录!") ;
        return;
    }

    showConfirmMessageAlter("确定启动运行该程序实例吗？",function ok(){
        var params = {
            HOST_LIST:hostArray
        };
        getJsonDataByPost(Globals.baseActionUrl.BUS_USER_START_PROGRAM_URL, params, "程序启停管理-启动业务进程",
            function(result){
                if (result != null) {
                    var textValue = result.TOTAL_DESC + "<br/>"+result.TOTAL_MSG+"<br/>";
                    textValue=textValue.replaceAll("\n","<br/>");
                    var params = {
                        RST_STR:textValue,
                        RST_EXEC:"启动",
                        RST_FLAG:"hidden"
                    };
                    showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                        function destroy(data){
                            search(true);
                        }, params, {allowDrag:true});
                }
            });
    });
}

/**
 * 批量停止
 */
function addBatchDtop() {
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
                    TASK_ID:rows[i]["TASK_ID"],
                    HOST_ID:rows[i]["HOST_ID"],
                    HOST_IP:rows[i]["HOST_IP"],
                    PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                    PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                    SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                    CONFIG_FILE:rows[i]["CONFIG_FILE"],
                    CLUSTER_ID:rows[i]["CLUSTER_ID"],
                    CLUSTER_TYPE:rows[i]["CLUSTER_TYPE"],
                    VERSION:rows[i]["VERSION"],
                    RUN_JSTORM:rows[i]["RUN_JSTORM"],
                    DIFF_IP:rows[i]["DIFF_IP"],
                    versionDir:rows[i]["VERSION"],
                    BUS_CLUSTER_ID:rows[i]["BUS_CLUSTER_ID"]
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

    showConfirmMessageAlter("确定停止运行该程序实例吗？",function ok(){
        var params = {
            HOST_LIST:hostArray
        };
        getJsonDataByPost(Globals.baseActionUrl.BUS_USER_STOP_PROGRAM_URL, params, "程序启停管理-停止业务进程",
            function(result){
                if (result != null) {
                    var textValue = result.TOTAL_DESC + "<br/>"+result.TOTAL_MSG+"<br/>";
                    textValue=textValue.replaceAll("\n","<br/>");

                    var params = {
                        RST_STR:textValue,
                        RST_EXEC:"停止",
                        RST_FLAG:"hidden"
                    };
                    showDialog("结果","80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                        function destroy(data){
                            search(true);
                        }, params, {allowDrag:true});
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
                TASK_ID:rows[i]["TASK_ID"],
                HOST_ID:rows[i]["HOST_ID"],
                PROGRAM_CODE:rows[i]["PROGRAM_CODE"],
                PROGRAM_NAME:rows[i]["PROGRAM_NAME"],
                SCRIPT_SH_NAME:rows[i]["SCRIPT_SH_NAME"],
                CONFIG_FILE:rows[i]["CONFIG_FILE"],
                CLUSTER_TYPE:rows[i]["CLUSTER_TYPE"],
                CLUSTER_ID: rows[i]["CLUSTER_ID"],
                VERSION:rows[i]["VERSION"],
                RUN_STATE:rows[i]["RUN_STATE"],
                BUS_CLUSTER_ID:rows[i]["BUS_CLUSTER_ID"],
                RUN_JSTORM:rows[i]["RUN_JSTORM"],
                DIFF_IP:rows[i]["DIFF_IP"]
            };
            paramList.push(obj);
        }
    }
    getJsonDataByPost(Globals.baseActionUrl.BUS_USER_CHECK_STATUS_PROGRAM_URL, paramList, "程序启停管理-检查不分IP启停程序实例状态",
        function(result){
            search(true);
        }
    );
}