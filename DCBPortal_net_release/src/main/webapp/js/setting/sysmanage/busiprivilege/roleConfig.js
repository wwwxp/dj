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
    datagridLoadPage(JsVar["roleGrid"],param,"roleConfigMapper.qeuryRole");
}

//渲染操作行添加修改，删除按钮
function onActionRenderer(e) {
    var ROLE_ID = e.record.ROLE_ID;
    var rowIndex = e.rowIndex;
    var actionButton = ' <a class="Edit_Button" href="javascript:editRole(\'' + ROLE_ID + '\')" >修改</a>' +
        ' <a class="Delete_Button" href="javascript:delRole(\'' +
        ROLE_ID +
        '\')">删除</a>';
    return actionButton;
}
//新增角色
function addRole() {
    showAddDialog("新增角色",600,300,Globals.baseJspUrl.ROLE_CONFIG_JSP_ADD_EDIT_URL,
        function  destroy(data){
        if (data == systemVar.SUCCESS) {
            JsVar["roleGrid"].reload();
            showMessageTips("新增角色成功!");
        }
    });
}

//修改角色
function editRole(role_id) {
    showEditDialog("修改角色",600,300,Globals.baseJspUrl.ROLE_CONFIG_JSP_ADD_EDIT_URL,
        function destroy(data){

            if (data == systemVar.SUCCESS) {

                JsVar["roleGrid"].reload();
                showMessageTips("修改角色成功!");
            }
    },role_id);
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
    
    var tips = "确定删除记录？";
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,{role_ids:role_ids},"角色管理-删除前检查角色绑定的权限",
    		function (result) {
		        if (result["SUM"] > 0) {
		        	tips = "选择的角色中有分配权限，确定删除记录？";
		        }
		    },"roleConfigMapper.queryRoleBindedPrivilege",null, false);
    showConfirmMessageAlter(tips,function ok(){
            // 数据汇总保存
            var delObj = new Object();
            delObj["delete|roleConfigMapper.delEmpeeRole"] = role_ids;
            delObj["delete|roleConfigMapper.delRoleBusConfig"] = role_ids;
            delObj["delete|roleConfigMapper.delRoleBusProgram"] = role_ids;
            delObj["delete|roleConfigMapper.delRole"] = role_ids;
            getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,[delObj],"角色管理-删除角色",
                function(result){
                    JsVar["roleGrid"].reload();
                    showMessageTips("删除角色成功!")
                });
    });

}

