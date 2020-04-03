//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["userGrid"] = mini.get("user_datagrid");//取得查询表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    //加载用户表格信息
    search();
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
    datagridLoadPage(JsVar["userGrid"],param,"userMapper.queryEmpee");
}

//渲染操作按钮
function onRenderer(e) {
    var empee_id = e.record.EMPEE_ID;
    return '<a class="Delete_Button" href="javascript:updateUser(\'' + empee_id + '\')">编辑</a><a class="Delete_Button"  href="javascript:delUser(\'' + empee_id + '\')">删除</a>';
}
//新增用户
function addUser() {
    showAddDialog("新增用户",600,300,Globals.baseJspUrl.USER_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["userGrid"].reload();
                showMessageTips("新增用户成功");
            }
    });
}
//修改用户
function updateUser(empee_id) {
    showEditDialog("修改用户",600,300,Globals.baseJspUrl.USER_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["userGrid"].reload();
                //mini.alert("修改用户成功");
                showMessageTips("修改用户成功!");

                //showWarnMessageAlter("修改用户成功");
            }
    },empee_id);
}

//删除用户
function delUser(id) {
    var empee_ids = new Array();
    if (id) {
        empee_ids.push({EMPEE_ID : id});
    } else {
        var rows = JsVar["userGrid"].getSelecteds();
        if (rows.length > 0) {
            for (var i = 0; i < rows.length; i++) {
                var id = rows[i]["EMPEE_ID"];
                empee_ids.push({EMPEE_ID:id});
            }
        }
        else {
            showWarnMessageTips("请选中一条记录!") ;
            return;
        }
    }

    showConfirmMessageAlter("确定删除记录？",function ok(){
        var delObj = new Object();
        delObj["delete|userMapper.deleteEmpeeRelation"] = empee_ids;
        delObj["update|userMapper.updateEmpeeForDelete"] = empee_ids;
        getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,[delObj],"用户管理-删除用户",
            function(result){
                JsVar["userGrid"].reload();
                showMessageTips("删除用户成功!");
            });
    })
}


