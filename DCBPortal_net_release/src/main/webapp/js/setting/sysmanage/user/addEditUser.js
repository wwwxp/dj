//定义变量， 通常是页面控件和参数
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["userFrom"] = new mini.Form("#userForm");
    //加载所有的用户角色
    loadRole();
});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    if (action == systemVar.EDIT) {
        JsVar["EMPPE_ID"] = data;
        findById(data);
    }
}

//新增和修改点确认按钮保存
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        updateUser();
    } else {
        saveUser();
    }
}

//加载所有的用户角色
function loadRole() {
    comboxLoad(mini.get("ROLE_ID"),null,"userMapper.queryUserRole");
}

//点确认新增用户
function saveUser() {
    //判断是否有效
    JsVar["userFrom"].validate();
    if (JsVar["userFrom"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据
    var userInfo = JsVar["userFrom"].getData();
    userInfo["EMPEE_PWD"] = encrypt(userInfo["EMPEE_PWD"]);
    getJsonDataByPost(Globals.baseActionUrl.USER_ACTION_ADD_URL,userInfo,"用户管理-新增用户",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//修改用户
function updateUser() {
    //判断是否有效
    JsVar["userFrom"].validate();
    if (JsVar["userFrom"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var userInfo = JsVar["userFrom"].getData();
    userInfo["EMPEE_ID"] = JsVar["EMPPE_ID"];
    userInfo["EMPEE_PWD"] = encrypt(userInfo["EMPEE_PWD"]);
    getJsonDataByPost(Globals.baseActionUrl.USER_ACTION_UPDATE_URL,userInfo,"用户管理-修改用户",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}



//编辑初始化数据
function findById(emppe_id) {
    //查询用户信息
    var param = new Object();
    param["EMPPE_ID"] = emppe_id;
    getJsonDataByPost(Globals.baseActionUrl.USER_ACTION_QUERY_ID_URL,param,null,
        function success(result){
            JsVar["userFrom"].setData(result);
        });
}
