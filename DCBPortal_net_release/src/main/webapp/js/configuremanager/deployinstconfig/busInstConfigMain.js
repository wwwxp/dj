/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得主机表格
    JsVar["configGrid"] = mini.get("configGrid");
    //取得查询表单
    JsVar["queryForm"] = new mini.Form("#queryForm");
    //主机
    JsVar["HOST_ID"] = mini.get("HOST_ID");
    //集群类型
    JsVar["DEPLOY_TYPE"] = mini.get("DEPLOY_TYPE");
    //集群名称
    JsVar["CLUSTER_ID"] = mini.get("CLUSTER_ID");
    //程序类型
    JsVar["PROGRAM_TYPE"] = mini.get("PROGRAM_TYPE");
    //程序名称
    JsVar["PROGRAM_NAME"] = mini.get("PROGRAM_NAME");
});

function loadPage(data) {
    JsVar["data"] = data;
    //加载下拉框
    // loadCombo();
    //加载表格信息
    search();
    //加载树节点数据
    getTreeData(data["BUS_CLUSTER_ID"]);
}


/**
 * 查询
 */
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    paramsObj["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
    paramsObj["PROGRAM_TYPE"] = JsVar["data"]["PROGRAM_TYPE"];
    paramsObj["CLUSTER_ID"] =  JsVar["data"]["CLUSTER_ID"];
    load(paramsObj);
}

/**
 * 加载表格
 * @param param
 */
function load(param) {
    datagridLoadPage(JsVar["configGrid"], param, "taskProgram.queryTaskProgramList");
}

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
    JsVar["data"] = data;
    JsVar["version"] = data["VERSION"];
    //加载树节点数据
    getTreeData(data["BUS_CLUSTER_ID"]);
}

/**
 * 加载实例状态目录
 * @returns
 */
function getTreeData(BUS_CLUSTER_ID) {
	var params = {
		TYPE:busVar.BUSINESS_TYPE,
        BUS_CLUSTER_ID:BUS_CLUSTER_ID
	};
	treeLoad(mini.get("fileTree"), null, params, Globals.baseActionUrl.INST_CONFIG_BUS_TREE_QUERY_URL);
}


/**
 * 节点点击事件
 * @param e
 */
function onClickTreeNode(e) {
	var currNode = e.node;
	var nodeLevel = e.node.NODE_LEVEL;
	var params = {};
	if (nodeLevel == "1") {  //查询集群类型下所有的实例
		params["PROGRAM_TYPE"] = currNode["CLUSTER_TYPE"];
        JsVar["data"]["PROGRAM_TYPE"]= currNode["CLUSTER_TYPE"];
        params["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
	} else if (nodeLevel == "2") {  //查询集群类型下集群
		params["PROGRAM_TYPE"] = currNode["CLUSTER_TYPE"];
		params["CLUSTER_ID"] = currNode["CLUSTER_ID"];
        params["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
        JsVar["data"]["PROGRAM_TYPE"]= currNode["CLUSTER_TYPE"];
        JsVar["data"]["CLUSTER_ID"]= currNode["CLUSTER_ID"];
	}
	mini.get("PROGRAM_NAME").setValue("");
	load(params);
}

/**
 * 图表重新渲染事件
 * @param e
 */
function nodeRender(e) {
	var level = e.node.NODE_LEVEL;
	if (level == "1") {
		e.iconCls = "tree-node-cluster-type";
	} else if (level == "2") {
		e.iconCls = "tree-node-cluster";
	}
}

/**
 * 加载下拉框
 */
// function loadCombo() {
//     var params = {
//         TYPE: busVar.BUSINESS_TYPE
//     };
//     comboxLoad(JsVar["PROGRAM_TYPE"], params, "clusterEleDefine.queryClusterEleList", null, null, false);
//     // comboxLoad(JsVar["PROGRAM_NAME"], params, "programDefine.queryProgramDefineList", null, null, false);
// }

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
    var rowIndex = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:showConfigContent(' + rowIndex + ')">配置文件</a>';
    html += '<a class="Delete_Button" href="javascript:checkHostState(' + rowIndex + ')">状态检查</a>';
    html += '<a class="Delete_Button" href="javascript:deleteInst(' + rowIndex + ')">删除</a>';
    return html;
}

/**
 * 查看业务程序的配置文件
 */
function showConfigContent(index) {
    var rowData = JsVar["configGrid"].getRow(index);
    showDialog("查看配置文件", 780, "80%", Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_BUS_CONFIG_CONTENT_URL,
        function destroy(data) {
        }, rowData);
}

/**
 * 检查主机运行状态
 * @param index
 */
function checkHostState(index){
    var rowInfo = JsVar["configGrid"].getRow(index);
    var obj = {
        ID:rowInfo["ID"],
        TASK_ID:rowInfo["TASK_ID"],
        HOST_ID:rowInfo["HOST_ID"],
        PROGRAM_CODE:rowInfo["PROGRAM_CODE"],
        PROGRAM_NAME:rowInfo["PROGRAM_NAME"],
        SCRIPT_SH_NAME:rowInfo["SCRIPT_SH_NAME"],
        CONFIG_FILE:rowInfo["CONFIG_FILE"],
        CLUSTER_TYPE:rowInfo["PROGRAM_TYPE"],//根据类型来区分是否区分IP
        CLUSTER_ID: rowInfo["CLUSTER_ID"],
        VERSION:rowInfo["VERSION"],
        RUN_STATE:rowInfo["RUN_STATE"],
        versionDir:rowInfo["VERSION"],
        BUS_CLUSTER_ID:rowInfo["BUS_CLUSTER_ID"]
    };
    obj["versionDir"] = rowInfo["VERSION"];
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, obj,
        "业务集群新增-获取业务程序是否根据IP区分", function(result) {
            JsVar["CLUSTER_ELE_PERSONAL_CONF"] = result[0]["CLUSTER_ELE_PERSONAL_CONF"];
            JsVar["RUN_JSTORM"] = result[0]["RUN_JSTORM"];
        }, "clusterEleDefine.queryClusterEleList", "", false);

    if(JsVar["CLUSTER_ELE_PERSONAL_CONF"] == "1"){
        //区分IP
        getJsonDataByPost(Globals.baseActionUrl.COMMON_DIFF_IP_CHECK_ACTION_MANAGE_URL, [obj], "启停管理-区分IP程序状态检查",
            function(result){
                if (result != null && result.length > 0) {
                    result = result[0];
                }
                if(result["state"] == "1"){
                    if(result["info"] != null){
                        showMessageAlter(result["info"]);
                    }else{
                        showMessageAlter("当前程序正在运行，进程号为【 " + result["process"] + "】");
                    }
                } else if(result["state"] == "0"){
                    if(result["info"] != null){
                        showMessageAlter(result["info"]);
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
    }else{
        if(JsVar["RUN_JSTORM"] == "1"){
            getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_CHECK_PROGRAM_ACTION_MANAGE_URL, [obj], "启停管理-运行Topology进程状态检查",
                function(result){
                    if (result != null && result.length > 0) {
                        result = result[0];
                    	if (result["rstCode"] == busVar.ERROR) {
                    		showErrorMessageAlter(result["info"]);
                    	} else {
                            showMessageTips(result["info"]);
                    	}
                    	search();
                    }
                });
        }else{
            //不区分IP
            getJsonDataByPost(Globals.baseActionUrl.COMMON_IP_CHECK_ACTION_MANAGE_URL, [obj], "启停管理-不区分IP进程状态检查",
                function(result){
                    if (result != null && result.length > 0) {
                        result = [0];
                    }
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
                    } else if(result["state"] == "3") {
                        showWarnMessageTips("该主机不存在该程序");
                    } else if (result["state"] == "4") {
                        if(result["info"] != null){
                            showErrorMessageAlter(result["info"]);
                        } else {
                            showErrorMessageAlter("程序状态检查失败！");
                        }
                    }
                    search();
                }
            );
        }
    }
}

/**
 * 业务批量状态检查
 */
function batchCheckStatus() {
    var rows = JsVar["configGrid"].getSelecteds();
    if (rows == null || rows.length == 0) {
        showWarnMessageTips("请至少选择一条记录！");
    }

    //业务参数
    var paramList=[];
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

    getJsonDataByPost(Globals.baseActionUrl.BUS_USER_CHECK_STATUS_PROGRAM_URL, paramList, "业务状态检查--批量业务状态检查",
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
        }
    );
}

/**
 * 检查状态，数据库同步方法
 */
function synchronizeDatabase(params) {
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_UPDATE_PROCESS_STATE_URL, params, "instconfig--状态检查--同步数据库状态",
        function (result) {
            if (result != "error") {
                showMessageTips("同步数据库成功！");
                //刷新
                search();
            } else {
                showErrorMessageAlter("同步数据库失败:" + result);
            }
        });
}

/**
 * 删除已停止实例
 * @param index
 */
function deleteInst(index) {
    var rowData = JsVar["configGrid"].getRow(index);
    if (rowData == null || rowData == undefined) {
        showWarnMessageTips("请选择一条记录");
        return;
    }
    var RUN_STATE = rowData.STATUS;
    if (RUN_STATE == 1) {
        showWarnMessageTips("该程序正在运行中，不可删除！");
        return;
    }

    var msg = "确定删除记录？";
    if (rowData.DEPLOY_TYPE == busVar.DMDB) {
        msg = "确定删除记录及对应远程主机上的目录？";
    }
    showConfirmMessageAlter(msg, function ok() {
        getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_DEL_CURR_VER_ACTION_MANAGE_URL, [rowData], "启停管理-删除业务程序",
            function (result) {
                if (result.Success != undefined) {
                    showMessageTips("删除成功！");
                } else if (result.error != undefined) {
                    showErrorMessageAlter("删除失败！请检查！");
                }
                search();
            }
        );
    });
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
    var RUN_STATE = e.record.RUN_STATE;
    var html = "";
    if (RUN_STATE == 0 || RUN_STATE == null) {//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>";
    } else if (RUN_STATE == 1) {//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行&nbsp;</span>";
    }
}

/**
 * 合并数据
 * @param e
 */
function loadStopData(e) {
    var gridData = JsVar["configGrid"].getData();
    //var mergeCells2 = "PROGRAM_TYPE,PROGRAM_NAME,SCRIPT_SH_NAME,HOST_INFO";
    //var mergeCellColumnIndex2 = "1,2,3,4";

    var mergeCells2 = "PROGRAM_TYPE,PROGRAM_NAME,HOST_INFO";
    var mergeCellColumnIndex2 = "1,2,3";
    var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
    JsVar["configGrid"].mergeCells(mergeData);
}

