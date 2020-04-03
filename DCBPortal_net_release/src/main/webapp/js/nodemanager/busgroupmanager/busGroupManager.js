//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    JsVar["busGroupGrid"]=mini.get("busGroupGrid");
    JsVar["groupName"]=mini.get("GROUP_NAME");
    JsVar["groupCode"]=mini.get("GROUP_CODE");
    loadGrid({})
})

//表格加载
function loadGrid(param){
    datagridLoadPage(JsVar["busGroupGrid"],param,"busGroupManagerMapper.queryBusGroupInfo");
}

//查找
function search() {
    var params={
        GROUP_NAME: JsVar["groupName"].getValue(),
        GROUP_CODE: JsVar["groupCode"].getValue(),
    }

    loadGrid(params);
}

/**
 * 单元格数据渲染
 * */
function onRenderer(e){
    var id=e.record.ID;
    return '<a class="Delete_Button" href="javascript:updateBusGroup(\'' + id + '\')">修改</a><a class="Delete_Button"  href="javascript:delBusGroup(\'' + id + '\')">删除</a>';
}


function addBusGroup() {
    showAddDialog("新增业务组",600,300,Globals.ctx + "/jsp/nodemanager/busgroupmanager/addEditBusGroup",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                JsVar["nodeTypeGrid"].reload();
                showMessageAlter("新增节点成功");
            }
        });
}