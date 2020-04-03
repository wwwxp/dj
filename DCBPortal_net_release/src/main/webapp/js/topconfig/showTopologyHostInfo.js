/**
 * 定义变量， 通常是页面控件和参数
 * */
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["taskFrom"] = new mini.Form("#taskFrom");
    
    var length =  mini.get("HOST_IP").data.length;
    var initHost = "";
    for(var i=0;i<length;i++){
    	initHost = initHost + mini.get("HOST_IP").data[i].HOST_IP+",";
    }
    mini.get("HOST_IP").setValue(initHost.substring(0,initHost.length-1));
    //mini.get("HOST_IP").getValue();
});

/**
 * 父页面调用，新增和修改时会初使化一些参数
 */
function onLoadComplete(data) {
	JsVar["data"] = data;
}

/**
 * 确认
 */
function onSubmit() {
    var param=JsVar["data"];
    
    param.HOST_IPS = mini.get("HOST_IP").getValue();
    
    getJsonDataByPost(Globals.baseActionUrl.TOPCONFIG_ACTION_SAVE_FILE_URL,param,"配置管理-保存配置",function (){
		closeWindow(systemVar.SUCCESS);
        showMessageTips("保存成功！");
		 
	}) 

}
