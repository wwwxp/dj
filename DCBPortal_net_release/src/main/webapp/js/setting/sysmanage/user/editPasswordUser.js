//定义变量， 通常是页面控件和参数
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["editFrom"] = new mini.Form("#edit_form");

});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {

}

//新增和修改点确认按钮保存
function onSubmit() {
    //判断是否有效
    JsVar["editFrom"].validate();
    if (JsVar["editFrom"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据
    var word = JsVar["editFrom"].getData();
    if(word.newPassword != word.configPassword){
        showErrorMessageAlter("输入的新密码与确认新密码不一致！");
        return;
    }
    word["oldPassword"]= encrypt(word["oldPassword"]);
    word["newPassword"]= encrypt(word["newPassword"]);
    word["configPassword"]= encrypt(word["configPassword"]);
    
    
    JsVar["editFrom"].reset();
    getJsonDataByPost(Globals.baseActionUrl.HOME_ACTION_EDI_PASSWORD_URL,word,"首页-修改密码",
        function success(result){
            showMessageTips("修改成功！");
            closeWindow(systemVar.SUCCESS);
        });
}


