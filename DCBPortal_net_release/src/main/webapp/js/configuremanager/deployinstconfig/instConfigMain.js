/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
var paramData = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    var dialogFlag= $("#dialogFlag").val();
    JsVar["configGrid"] = mini.get("configGrid");//取得主机表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    //主机
    JsVar["HOST_ID"] = mini.get("HOST_ID");
    //集群类型
    JsVar["DEPLOY_TYPE"] = mini.get("DEPLOY_TYPE");
    //集群名称
    JsVar["CLUSTER_ID"] = mini.get("CLUSTER_ID");
    //加载下拉框
    loadCombo();

    //加载树节点数据
    getTreeData();
    /**	jsp页面隐藏变量：
     **	用于判断是菜单加载还是弹框加载(弹框加载在config.js中添加参数)
     **	如若不加隐藏变量，弹框加载数据时，将会出现数据混乱
     **/
    if(dialogFlag=="" || dialogFlag!="1"){//弹框
        //加载表格信息
        search();
    }
});

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
    if(data != null){
        paramData = data;
        JsVar["HOST_ID"].setValue(data.HOST_ID);
        //JsVar["HOST_IP"].disable();
        JsVar["DEPLOY_TYPE"].setValue(data.CLUSTER_TYPE);
        //JsVar["DEPLOY_TYPE"].disable();
        changeDeployType();
        //加载表格信息
        search();

        //加载树节点数据
        getTreeData();
    }
}

/**
 * 加载实例状态目录
 * @returns
 */
function getTreeData() {
    var params = {
        TYPE:busVar.COMPONENT_TYPE
    };
    treeLoad(mini.get("fileTree"), null, params, Globals.baseActionUrl.INST_CONFIG_TREE_QUERY_URL);
}

/**
 * 刷新文件目录树
 */
function refresh(){
    mini.get("queryNodeName").setValue("");
    var params = {
        TYPE:busVar.COMPONENT_TYPE
    };
    treeLoad(mini.get("fileTree"), null, params, Globals.baseActionUrl.INST_CONFIG_TREE_QUERY_URL);
}

/**
 * 搜索树节点
 */
function searchTree() {
    var nodeNameStr = mini.get("queryNodeName").getValue();
    if (nodeNameStr == "") {
        mini.get("fileTree").clearFilter();
    }else {
        nodeNameStr = nodeNameStr.toLowerCase();
        mini.get("fileTree").filter(function (node) {
            var text = node["NODE_TEXT"] ? node["NODE_TEXT"].toLowerCase() : "";
            if (text.indexOf(nodeNameStr) != -1) {
                return true;
            }
        });
    }
}

/**
 * 节点点击事件
 * @param e
 */
function onClickTreeNode(e) {
    var currNode = e.node;
    var nodeLevel = e.node.NODE_LEVEL;
    var params = {};
    if (nodeLevel == "1") {  //查询所有集群类型
        params["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
    } else if (nodeLevel == "2") {  //查询所有集群类型下对应集群
        params["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
        params["CLUSTER_ID"] = currNode["CLUSTER_ID"];
    }
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
 * 查询
 */
function search(checkNode) {
    var paramsObj = JsVar["queryFrom"].getData();

    //选中左边节点
    if (checkNode) {
        var currNode = mini.get("fileTree").getSelectedNode();
        if (currNode != null && currNode != undefined) {
            var nodeLevel = currNode["NODE_LEVEL"];
            if (nodeLevel == "1") {  //查询所有集群类型
                paramsObj["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
            } else if (nodeLevel == "2") {  //查询所有集群类型下对应集群
                paramsObj["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
                paramsObj["CLUSTER_ID"] = currNode["CLUSTER_ID"];
            }
        }
    }
    load(paramsObj);
}

/**
 * 加载表格
 * @param param
 */
function load(param){
    datagridLoadPage(JsVar["configGrid"],param,"instConfig.queryInstConfigInfo");
}

/**
 * 加载下拉框
 */
function loadCombo(){
    var params = {
        TYPE: busVar.COMPONENT_TYPE
    };
    comboxLoad(JsVar["CLUSTER_ID"], params, "serviceType.queryAllClusterCode",null, null, false);
    comboxLoad(JsVar["DEPLOY_TYPE"], params, "clusterEleDefine.queryClusterEleList", null, null, false);
    comboxLoad(JsVar["HOST_ID"], {}, "host.queryHostList", null, null, false);
}

/**
 * 部署类型
 */
function changeDeployType() {
    var deployType = mini.get("DEPLOY_TYPE").getValue();
    if (deployType != null && deployType != "") {
        var deployNewType = deployType + "_deploy_type";
        var deployFileTypeData = getSysDictData(deployNewType);
        mini.get("DEPLOY_FILE_TYPE").setData(deployFileTypeData);
    }
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
    var rowIndex=e.rowIndex;
    var html='<a class="Delete_Button" href="javascript:showConfigContent(' + rowIndex +')">配置文件</a>';
    html+='<a class="Delete_Button" href="javascript:checkHost('+rowIndex+')">状态检查</a>';
    html+='<a class="Delete_Button" href="javascript:deleteInst('+rowIndex+')">删除</a>';
    return html;
}

/**
 * 查看配置
 */
function showConfigContent(index) {
    var rowData=JsVar["configGrid"].getRow(index);
    var hostId = rowData.HOST_ID;
    var filePath = rowData.FILE_PATH;
    var instId = rowData.INST_ID;
    var fileName = rowData.FILE_NAME;
    var clusterType = rowData.CLUSTER_TYPE;
    var deployFileType = rowData.DEPLOY_FILE_TYPE;
    if((clusterType==busVar.DCA
        && deployFileType!=busVar.REDIS
        && deployFileType!=busVar.SENTINEL
        && deployFileType != busVar.REDIS_INC_REFRESH
        && deployFileType != busVar.REDIS_REVISE
        && deployFileType != busVar.REDIS_WHOLE_CHECK
        && deployFileType != busVar.REDIS_WHOLE_REFRESH)
        || clusterType==busVar.MONITOR
        || clusterType==busVar.DMDB
        || clusterType==busVar.DCLOG
        || clusterType==busVar.M2DB
        || clusterType==busVar.DSF){//多个配置文件
        var params = {
            INST_ID:instId,
            HOST_ID:hostId,
            filePath:filePath
        };
        showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL,
            function destroy(data){
            }, params);
    }else{
        var params = {
            INST_ID:instId,
            HOST_ID:hostId,
            filePath:filePath,
            fileName:fileName
        };
        showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_CONFIG_CONTENT_URL,
            function destroy(data){
            }, params);
    }
}

/**
 * 检查
 */
function checkHost(index){
    var rowData=JsVar["configGrid"].getRow(index);
    if(rowData==null || rowData==undefined){
        showWarnMessageTips("请选择一条记录");
        return;
    }
    var state=rowData.STATUS;

    var updateParams=new Object();
    updateParams["INST_ID"]=rowData.INST_ID;
    updateParams["HOST_ID"]=rowData.HOST_ID;
    updateParams["DEPLOY_TYPE"]=rowData.DEPLOY_TYPE;
    updateParams["CLUSTER_TYPE"]=rowData.CLUSTER_TYPE;

    updateParams["DEPLOY_FILE_TYPE"] = rowData.DEPLOY_FILE_TYPE;
    //updateParams["CLUSTER_CODE"]=rowData.DEPLOY_TYPE;
    updateParams["SOFT_LINK_PATH"]=rowData.SOFT_LINK_PATH;
    updateParams["VERSION"]=rowData.VERSION;

    //1.远程主机检查,返回真实状态
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_CHECK_PROCESS_STATE_URL,rowData,"instconfig--状态检查",
        function(result){
            var processInfo=(result.processPort==undefined?"":result.processPort);
            //2.真实状态与数据库的做对比
            if(state==1){//数据库:运行
                if(result.processState==1){//远程主机：运行
                    if(rowData.DEPLOY_FILE_TYPE == "m2db"){
                        showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，实例名为【"+processInfo+"】");
                    }else{
                        showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】");
                    }

                }else{//远程主机：未运行
                    //3.状态不正确，提示更新数据库
                    updateParams["STATUS"]='0';
                    showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序未运行，"+processInfo+"是否同步数据库?", function ok(){
                        synchronizeDatabase(updateParams);
                    });
                }
            }else{//数据库:未运行
                if(result.processState==1){//远程主机：运行
                    //3.状态不正确，提示更新数据库
                    updateParams["STATUS"]='1';
                    showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】，是否同步数据库?", function ok(){
                        synchronizeDatabase(updateParams);
                    });
                }else{//远程主机：未运行
                    showWarnMessageTips(rowData.DEPLOY_FILE_TYPE+"程序未运行!"+processInfo+"");
                }
            }
        });
}


/**
 * 批量状态检查
 */
function batchCheckStatus() {
    var instList = JsVar["configGrid"].getSelecteds();
    if (instList == null || instList.length == 0) {
        showWarnMessageTips("请至少选择一条记录！");
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_BATCH_CHECK_STATE_URL, instList, "组件实例-组件批量状态检查",
        function(result) {
            var textValue = result.TOTAL_DESC + "<br/>" + result.TOTAL_MSG + "<br/>";
            textValue = textValue.replaceAll("\n", "<br/>");
            var params = {
                RST_STR: textValue,
                RST_EXEC: "启动",
                RST_FLAG: "hidden"
            };
            showDialog("结果", "80%", "80%", Globals.baseJspUrl.RUN_STOP_RESULT_JSP_URL,
                function destroy(data) {
                    search(true);
                }, params, {allowDrag: true});
        });
}

/**
 * 检查状态，数据库同步方法
 */
function synchronizeDatabase(params){
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_UPDATE_PROCESS_STATE_URL,params,"instconfig--状态检查--同步数据库状态",
        function(result){
            if(result!="error"){
                showMessageTips("同步数据库成功！");
                //刷新
                search(true);
            }else{
                showErrorMessageAlter("同步数据库失败:"+result);
            }
        });
}

/**
 * 删除已停止实例
 * @param index
 */
function deleteInst(index){
    var rowData=JsVar["configGrid"].getRow(index);
    if(rowData==null || rowData==undefined){
        showWarnMessageTips("请选择一条记录");
        return;
    }
    var RUN_STATE=rowData.STATUS;
    if(RUN_STATE == 1){
        showWarnMessageTips("该程序正在运行中，不可删除！");
        return;
    }

    var msg="确定删除记录？";
    if(rowData.DEPLOY_TYPE==busVar.DMDB){
        msg="确定删除记录及对应远程主机上的目录？";
    }
    showConfirmMessageAlter(msg,function ok(){
        getJsonDataByPost(Globals.baseActionUrl.INST_CONFIG_TASK_ACTION_DELETE_INFOMATION_URL,rowData,"instconfig--删除已停止的实例信息",
            function(result){
                if(result.Success!=undefined){
                    showMessageTips("删除成功！");
                }else if(result.error!=undefined){
                    showErrorMessageAlter("删除失败！请检查！");
                }

                search(true);
            });
    });
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
    var RUN_STATE=e.record.STATUS;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>";
    }else if(RUN_STATE == 1){//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行&nbsp;</span>";
    }
}

/**
 * 合并数据
 * @param e
 */
function loadStopData(e) {
    var gridData = JsVar["configGrid"].getData();
    var mergeCells2="DEPLOY_TYPE,HOST_INFO,DEPLOY_FILE_TYPE,CLUSTER_NAME";
    var mergeCellColumnIndex2="1,2,3,4";
    var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
    JsVar["configGrid"].mergeCells(mergeData);
}
