//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    //程序类型表格
    JsVar["nodeTypeGrid"]=mini.get("nodeTypeGrid");
    //业务组下拉框
    JsVar["BUS_GROUP"]=mini.get("BUS_GROUP");

    loadBusGroupCombobox();
    loadGridTable({});
});

function loadBusGroupCombobox(){

    comboxLoad( JsVar["BUS_GROUP"],null,"nodeTypeManagerMapper.queryBusGroupOnNodeType","","",false);
}

function loadGridTable(param){
    datagridLoadPage(JsVar["nodeTypeGrid"],param,"nodeTypeManagerMapper.queryNodeType");

}

/**
 * 单元格数据渲染
 * */
function onRenderer(e){
    var nodeTypeId=e.record.ID;
    return '<a class="Delete_Button" href="javascript:updateNodeType(\'' + nodeTypeId + '\')">修改</a><a class="Delete_Button"  href="javascript:delNodeType(\'' + nodeTypeId + '\')">删除</a>';
}

function search() {
    var params={
        NAME: mini.get("NAME").getValue(),
        CODE:mini.get("CODE").getValue(),
        BUS_GROUP_ID: JsVar["BUS_GROUP"].getValue()
    }

    datagridLoadPage(JsVar["nodeTypeGrid"],params,"nodeTypeManagerMapper.queryNodeType");
}

function addNodeType() {
    showAddDialog("新增程序类型",600,300,Globals.ctx + "/jsp/nodemanager/nodetypeconfig/addEditNodeType",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                JsVar["nodeTypeGrid"].reload();
                loadBusGroupCombobox();
                showMessageAlter("新增节点成功");
            }
        });
}

/**
 * 修改NodeType
 * */
function updateNodeType(nodeTypeId){


    showEditDialog("修改程序类型",600,300,Globals.ctx + "/jsp/nodemanager/nodetypeconfig/addEditNodeType",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                JsVar["nodeTypeGrid"].reload();
                loadBusGroupCombobox();
                showMessageAlter("修改节点成功");
            }
        }, nodeTypeId);

}

/**
 * 程序类型的删除
 * @param nodeId
 */

function delNodeType(nodeTypeId) {
    var nodeTypeInfo = new Array();

    var canDel = true;

    //获得要删除的多个节点的信息
    if (!nodeTypeId) {
        nodeTypeInfo = JsVar["nodeTypeGrid"].getSelecteds();
        if (nodeTypeInfo.length <= 0) {
            showWarnMessageAlter("请选中一条记录!");
            return;
        }
    } else {
        var allRows = JsVar["nodeTypeGrid"].getData();

        for (var i = 0; i < allRows.length; ++i) {
            if (allRows[i]["ID"] == nodeTypeId) {
                nodeTypeInfo.push(allRows[i]);
                break;
            }
        }
    }

    //判断选中的程序类型中，是否有正在被使用的，有被使用，则无法删除
    getJsonDataByPost(Globals.ctx + "/nodeTypeManager/beingUsed",nodeTypeInfo,"节点配置-节点删除",
        function success(result) {
                if(result["using"]){
                    showWarnMessageAlter("选中的程序类型中，有正在运行的，请先停止程序！");
                }else{
                    showConfirmMessageAlter("程序类型未运行，确认删除？",function ok(){

                        getJsonDataByPost(Globals.ctx + "/nodeTypeManager/delNodeType",nodeTypeInfo,"程序类型配置-程序类型删除",
                            function success(result){
                                if (result["effectRow"] != -1) {
                                    JsVar["nodeTypeGrid"].reload();
                                    loadBusGroupCombobox();
                                    showMessageAlter("删除节点成功");

                                }else{
                                    showWarnMessageAlter(result["msg"])
                                }
                            },"");
                    });
                }
        }
    );

}