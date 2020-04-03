//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();//声明使用miniui
    JsVar["roleGrid"] = mini.get("role_datagrid");//取得表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    search();//读取表格数据
});

//查询
function search() {
    var paramsObj = JsVar["queryFrom"].getData();
    load(paramsObj);
}
//重新加载表格
function refresh() {
    JsVar["queryFrom"].reset();
    load(null);
}

//加载表格
function load(param){
    datagridLoadPage(JsVar["roleGrid"],param,"roleMapper.qeuryRole");
}

//渲染操作行添加修改，删除按钮
function onActionRenderer(e) {
    var ROLE_ID = e.record.ROLE_ID;
    var rowIndex = e.rowIndex;
    var actionButton = ' <a class="Edit_Button" href="javascript:editRole(\'' + ROLE_ID + '\')" >修改</a>' +
        ' <a class="Delete_Button" href="javascript:delRole(\'' +
        ROLE_ID +
        '\')">删除</a>' +
        ' <a class="Edit_Button" href="javascript:privilegeEdit(\'' +
        ROLE_ID +
        '\')">权限指派</a>';
    return actionButton;
}
//新增角色
function addRole() {
    showAddDialog("新增角色",600,300,Globals.baseJspUrl.ROLE_JSP_ADD_EDIT_URL,
        function  destroy(data){
        if (data == systemVar.SUCCESS) {
            JsVar["roleGrid"].reload();
            showMessageTips("新增角色成功!");
        }
    });
}

//修改角色
function editRole(role_id) {
    showEditDialog("修改角色",600,300,Globals.baseJspUrl.ROLE_JSP_ADD_EDIT_URL,
        function destroy(data){

            if (data == systemVar.SUCCESS) {

                JsVar["roleGrid"].reload();
                showMessageTips("修改角色成功!");
            }
    },role_id);
}

//权限指派
function privilegeEdit(id) {
    showDialog("权限指派",480,500,Globals.baseJspUrl.ROLE_JSP_DISPATCH_PRIVILEGE_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["roleGrid"].reload();
                showMessageTips("权限指派成功!");
            }
    },id);
}

//删除角色
function delRole(id) {
    var role_ids = new Array();
    if(id){//删除单个角色
        role_ids.push({ROLE_ID:id});
    }else{//删除多个角色
        var rows = JsVar["roleGrid"].getSelecteds();
        if (rows.length > 0) {
            for (var i = 0; i < rows.length; i++) {
                var id = rows[i]["ROLE_ID"];
                role_ids.push({ROLE_ID:id});
            }
        }
        else {
            showWarnMessageTips("请选中一条记录!") ;
            return ;
        }
    }
    showConfirmMessageAlter("确定删除记录？",function ok(){
            // 数据汇总保存
            var delObj = new Object();
            delObj["delete|roleMapper.delEmpeeRole"] = role_ids;
            delObj["delete|roleMapper.delRolePrivilege"] = role_ids;
            delObj["delete|roleMapper.delRole"] = role_ids;
            getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,[delObj],"角色管理-删除角色",
                function(result){
                    JsVar["roleGrid"].reload();
                    showMessageTips("删除角色成功!")
                });
    });

}

