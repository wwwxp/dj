//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    //表格
    JsVar["nodeForm"]=new mini.Form("#nodeForm");
    //主机IP下拉框控件
    JsVar["HOST_IP"]=mini.get("HOST_IP");
    //节点类型下拉框
    JsVar["NODE_TYPE"]=mini.get("NODE_TYPE");
    //业务组下拉框
    JsVar["GROUP_NAME"]=mini.get("GROUP_NAME");

});

//加载HOST_IP的下拉框
function loadHostIpCombobox(){

    comboxLoad( JsVar["HOST_IP"],null,"nodeManagerMapper.queryHostIp","","",false);
}

//加载节点类型的下拉框
function loadNodeTypeCombobox(){

    comboxLoad( JsVar["NODE_TYPE"],null,"nodeManagerMapper.queryNodeType","","",false);
}


function loadGroupNameCombobox(){

    comboxLoad( JsVar["GROUP_NAME"],null,"nodeManagerMapper.queryBusGroup","","",false);
}

//调用showAddDialog/showEditDialog时会执行
function onLoadComplete(action,data) {

    JsVar[systemVar.ACTION] = action;

    //mini.get("NODE_STATE").setData([{text:'有效',id:1},{text:'无效',id:0}]);

    loadHostIpCombobox();
    loadNodeTypeCombobox();
    loadGroupNameCombobox();

    if (action == systemVar.EDIT){
        findNodeById(data);
    }
}

function findNodeById(id){
        var param={
            ID:id
        };

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,param,null,
        function success(result) {

                if(result!=null && result!=undefined){
                    JsVar["nodeForm"].setData(result);

                    JsVar["HOST_IP"].setValue(result["NODE_HOST_ID"]);
                    JsVar["NODE_TYPE"].setValue(result["NODE_TYPE_ID"]);
                    JsVar["GROUP_NAME"].setValue(result["BUS_GROUP_ID"]);
                }

        },"nodeManagerMapper.queryNodeInfo");
}

/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
    if(JsVar[systemVar.ACTION] == systemVar.EDIT){
        updateNode();
        return;
    }else if(JsVar[systemVar.ACTION] == "batchAdd"){
        batchAddNode();
        return;
    }
    addNode();
}

/**
 * 判断linux路径是否为正确的二级及二级以上的绝对路径
 */
function checkNodePath(nodePath) {
    var reg = new RegExp("^/[^/]+/[^/]+(/[^/]+)*/?$");

    return reg.test(nodePath);
}

/**
 * 节点信息的新增
 */
function addNode(){
    JsVar["nodeForm"].validate();

    if(JsVar["nodeForm"].isValid()===false){
        return;
    }

    var nodeInfo=JsVar["nodeForm"].getData();

    if (!checkNodePath(nodeInfo["NODE_PATH"])) {
        showWarnMessageTips("路径必须为2级以上的合法的绝对路径！");
        return;

    }

    showLoadMask();

    getJsonDataByPost(Globals.ctx + "/nodeManager/addNode", nodeInfo, "节点管理-新增节点-节点信息插入",
        function success(result) {
            if (result["effectRow"] != -1) {
                closeWindow(systemVar.SUCCESS);
            } else {
                showWarnMessageTips(result["errorMsg"]);
            }
        });

}

function batchAddNode(){
    JsVar["nodeForm"].validate();

    if(JsVar["nodeForm"].isValid()===false){
        return;
    }

    var nodeInfo=JsVar["nodeForm"].getData();

    if (!checkNodePath(nodeInfo["NODE_PATH"])) {
        showWarnMessageTips("路径必须为2级以上的合法的绝对路径！");
        return;

    }

    showLoadMask();
    getJsonDataByPost(Globals.ctx + "/nodeManager/batchAddNode", nodeInfo, "节点管理-批量新增节点-节点信息插入",
        function success(result) {
            if (result["effectRow"] != 0) {
                closeWindow(
                    {
                        "result": systemVar.SUCCESS,
                        "effectRow": result["effectRow"],
                        "failCount": result["failCount"],
                    }
                );
            } else {
                showWarnMessageTips(result["errorMsg"]);
            }
        });

}

function updateNode() {


    JsVar["nodeForm"].validate();

    if(JsVar["nodeForm"].isValid()===false){
        return;
    }

    var nodeInfo=JsVar["nodeForm"].getData();

    if (!checkNodePath(nodeInfo["NODE_PATH"])) {
        showWarnMessageTips("路径必须为2级以上的合法的绝对路径！");
        return;

    }

    showLoadMask();
    getJsonDataByPost(Globals.ctx + "/nodeManager/updateNode", nodeInfo, "节点管理-修改节点",
        function success(result) {
            if (result["effectRow"] != -1) {
                closeWindow(systemVar.SUCCESS);
            } else {
                showWarnMessageTips(result["errorMsg"]);
            }
        });

}

function onCancel(e) {
    closeWindow();
}