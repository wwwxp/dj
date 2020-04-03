//定义变量， 通常是页面控件和参数
var JsVar = new Object();
///初使化
$(document).ready(function () {
    mini.parse();
    //权限表单
    JsVar["privilegeForm"] = new mini.Form("#privilege_form")

});
//父窗口调用的初始化方法
function onLoadComplete(action,data) {
    //新增节点
    var obj = new Object();
    JsVar["privilegeForm"].setData(data,false);
}
//保存提交
function onSubmit() {
    JsVar["privilegeForm"].validate();
    if (JsVar["privilegeForm"].isValid() == false){
        return;
    }
    var privilegeInfo = JsVar["privilegeForm"].getData();      //获取表单多个控件的数据
    getJsonDataByPost(Globals.baseActionUrl.PRIVILEGE_ACTION_ADD_URL,[privilegeInfo],"权限管理-增加权限",
        function(result){
            closeWindow(systemVar.SUCCESS);
        });
}



