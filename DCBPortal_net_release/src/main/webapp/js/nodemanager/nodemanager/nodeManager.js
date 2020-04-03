//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();


    //表格
    JsVar["nodeInfoGrid"]=mini.get("nodeInfoGrid");

    JsVar["NODE_TYPE"]=mini.get("NODE_TYPE");
    //节点名称控件
    JsVar["NODE_NAME"]=mini.get("NODE_NAME");
    //主机IP下拉框控件
    JsVar["HOST_IP"]=mini.get("HOST_IP");
    //业务组下拉框
    JsVar["GROUP_NAME"]=mini.get("GROUP_NAME");

    refreshCombobox();
    loadGridData({});
});

//页面加载、节点插入、修改、删除，相关联的下拉框需要更新
function refreshCombobox() {
    JsVar["NODE_TYPE"].setValue("");
    JsVar["NODE_NAME"].setValue("");
    JsVar["HOST_IP"].setValue("");
    JsVar["GROUP_NAME"].setValue("");
    loadNodeTypeCombobox();
    loadBusGroupCombobox();
    loadHostIpCombobox();
    loadNodeNameCombobox();
}

/**
 * 单元格数据渲染
 * */
function onRenderer(e){
    var nodeId=e.record.ID;
    return '<a class="Delete_Button" href="javascript:updateNode(\'' + nodeId + '\')">修改</a><a class="Delete_Button"  href="javascript:delNode(\'' + nodeId + '\')">删除</a>';
}

/**
 * 加载节点类型下拉框
 * */
function loadNodeTypeCombobox(){

    comboxLoad( JsVar["NODE_TYPE"],null,"nodeManagerMapper.queryNodeTypeOnNode","","",false);
}

/**
 * 加载节点名称下拉框
 * */
function loadNodeNameCombobox(param){

    comboxLoad( JsVar["NODE_NAME"],param,"nodeManagerMapper.queryNodeNameOnNode","","",false);
}

/**
 * 加载主机下拉框
 * */
function loadHostIpCombobox(param){

    comboxLoad( JsVar["HOST_IP"],param,"nodeManagerMapper.queryHostIpOnNode","","",false);
}

/**
 * 加载业务组下拉框
 * */
function loadBusGroupCombobox(param) {
    comboxLoad( JsVar["GROUP_NAME"],param,"nodeManagerMapper.queryBusGroupOnNode","","",false);
}

/**
 * 加载表格
 * */
function loadGridData(param){
    datagridLoadPage(JsVar["nodeInfoGrid"],param,"nodeManagerMapper.queryNodeInfo");
}

function onValueChanged(){
    var params={
        NODE_NAME: JsVar["NODE_NAME"].getValue(),
        NODE_TYPE_ID: JsVar["NODE_TYPE"].getValue()
    }
    loadNodeNameCombobox(params);
    loadHostIpCombobox(params);
    loadBusGroupCombobox(params);
}

/**
 * 新增Node
 * */
function addNode(){

    showAddDialog("新增节点",800,300,Globals.ctx + "/jsp/nodemanager/nodemanager/addEditNode",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                JsVar["nodeInfoGrid"].reload();
                refreshCombobox();
                showMessageAlter("新增节点成功");
            }
        });
}

/**
 * 批量新增Node
 * */
function batchAddNode(){
    showDialog("批量新增",800,300,Globals.ctx + "/jsp/nodemanager/nodemanager/batchAddNode",
        function destroy(result){     //窗口销毁时，执行的操作
            if (result!=null && result["result"] == systemVar.SUCCESS) {

                JsVar["nodeInfoGrid"].reload();
                refreshCombobox();
                showMessageAlter("新增节点成功个数："+result["effectRow"]+"，新增失败的个数为："+result["failCount"]);
            }
        },"batchAdd");
}

/**
 * 修改Node
 * */
function updateNode(nodeId){


    showEditDialog("修改节点",800,300,Globals.ctx + "/jsp/nodemanager/nodemanager/addEditNode",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                JsVar["nodeInfoGrid"].reload();
                refreshCombobox();
                showMessageAlter("修改节点成功");
            }
        }, nodeId);

}

/**
 * 根据节点名称、主机IP、业务组查询节点信息
 * */
function search(){
    var params={
        NODE_NAME: JsVar["NODE_NAME"].getText()==""?null:JsVar["NODE_NAME"].getValue(),
        NODE_HOST_ID:JsVar["HOST_IP"].getText()==""?null:JsVar["HOST_IP"].getValue(),
        BUS_GROUP_ID: JsVar["GROUP_NAME"].getText()==""?null:JsVar["GROUP_NAME"].getValue(),
        NODE_TYPE_ID: JsVar["NODE_TYPE"].getText()==""?null:JsVar["NODE_TYPE"].getValue()
    }

    datagridLoadPage(JsVar["nodeInfoGrid"],params,"nodeManagerMapper.queryNodeInfo");
}

/**
 * 节点的删除
 * */
function delNode(nodeId){
    var nodeInfo=new Array();

    var canDel=true;

    //获得要删除的多个节点的信息
    if(!nodeId){
        nodeInfo=JsVar["nodeInfoGrid"].getSelecteds();
        if(nodeInfo.length<=0){
            showWarnMessageAlter("请选中一条记录!");
            return;
        }
    }else{
        var allRows=JsVar["nodeInfoGrid"].getData();

        for(var i=0;i<allRows.length;++i){
            if(allRows[i]["ID"]==nodeId){
                nodeInfo.push(allRows[i]);
                break;
            }
        }
    }

    //判断节点的部署删除状态

    getJsonDataByPost(Globals.ctx + "/nodeManager/deployRunState",nodeInfo,"节点配置-节点删除",
        function success(result){
            if(result["deploy"] && result["running"]){

                showWarnMessageAlter(result["msg"]);
                canDel=false;

            }else if(result["deploy"] && !result["running"]){

                canDel=false;

                showConfirmMessageAlter(result["msg"],
                    function ok() {
                        getJsonDataByPost(Globals.ctx + "/nodeManager/delNode", nodeInfo, "节点配置-节点删除",
                            function success(result) {

                                if (result["effectRow"] != -1) {
                                    JsVar["nodeInfoGrid"].reload();
                                    refreshCombobox();
                                    showMessageAlter("删除节点成功");
                                }else{
                                    showWarnMessageAlter(result["msg"])
                                }
                            });
                    });
            }

        },null,null,false);

    if(canDel){
        realBatchDelNode(nodeInfo);
    }


}

function realBatchDelNode(nodeInfo) {

    showConfirmMessageAlter("确定删除记录？",function ok(){

        getJsonDataByPost(Globals.ctx + "/nodeManager/delNode",nodeInfo,"节点配置-节点删除",
            function success(result){
                if (result["effectRow"] != -1) {
                    JsVar["nodeInfoGrid"].reload();
                    refreshCombobox();
                    showMessageAlter("删除节点成功");
                }else{
                    showWarnMessageAlter(result["msg"])
                }
            },"");
    });
}