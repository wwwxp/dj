//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["hostForm"] = new mini.Form("hostForm");
});



/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
    
    addHost();
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    
}

/**
 * 新增文件或目录
 */
function addHost(){
    var hostForm = JsVar["hostForm"].getData();
    JsVar["hostForm"].validate();
    if (JsVar["hostForm"].isValid() == false){
        return;
    }
    closeWindow(hostForm);
}


/**
 * 取消
 * @param e
 */
function onCancel(e){
    closeWindow();
}


