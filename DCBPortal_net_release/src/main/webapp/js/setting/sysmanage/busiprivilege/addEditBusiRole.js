//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["roleForm"] = new mini.Form("roleForm");

});
//新增和修改提交
function onSubmit(e){
    if(JsVar[systemVar.ACTION] == systemVar.EDIT){
       editRole();
        return;
    }
    addRole();
}

//跳转到该页面设值
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    JsVar["ROLE_ID"] = data;
    if (action == systemVar.EDIT){
        mini.get("ROLE_STATE").setEnabled(true);
        findById(data);
    }
}
//根据id查询角色对象
function findById(id){
    var param = new Object();
    param["ROLE_ID"] = id;
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,param,null,
        function(result){
            JsVar["roleForm"].setData(result);
        },"roleConfigMapper.qeuryRole");

}
//新增角色
function addRole()
{
    var roleObj = JsVar["roleForm"].getData();
    JsVar["roleForm"].validate();
    if (JsVar["roleForm"].isValid() == false){
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL,[roleObj],"角色管理-新增角色",
        function success(result){
            closeWindow(systemVar.SUCCESS);
       },"roleConfigMapper.insertRole");
}
//修改角色
function editRole()
{
    var roleObj = JsVar["roleForm"].getData();
    JsVar["roleForm"].validate();
    roleObj["ROLE_ID"] = JsVar["ROLE_ID"];
    if (JsVar["roleForm"].isValid() == false) {
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[roleObj],"角色管理-修改角色",
        function(result){
            closeWindow(systemVar.SUCCESS);
        },"roleConfigMapper.updateRole");
}

