//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["releaseForm"] = new mini.Form("releaseForm");
});





/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(data) {
	JsVar["node"]=data;
	JsVar["releaseForm"].setData({release:data["name"]});
}

/**
 * 发布
 */
function release(){
    var formData = JsVar["releaseForm"].getData();
    var params=mini.clone(JsVar["node"]);
        formData["path"]=params["path"];
    
    JsVar["releaseForm"].validate();
    if (JsVar["releaseForm"].isValid() == false){
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_RELEASE_URL, formData,"策略配置管理-新增段落动作",
            function(result){
    	    closeWindow(systemVar.SUCCESS);
            });
    
}

/**
 * 取消
 * @param e
 */
function onCancel(e){
    closeWindow();
}


