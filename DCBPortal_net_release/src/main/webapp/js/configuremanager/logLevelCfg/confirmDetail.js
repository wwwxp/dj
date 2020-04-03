 

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
 
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["datagrid"]=mini.get("datagrid");
   
});

//加载表格 
function load(){
	JsVar["datagrid"].setData(JsVar["rows"]);
}

function onLoadComplete(data) {
    JsVar["rows"] = data;
    load();
    
}
 
/**
 * 保存发送
 */
function sendMsg(){
		var obj ={};
		obj["checkData"] = JsVar["rows"];
		//提交到后台处理
		getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_SENDMSG_URL,obj,"日志级别配置-操作",
			function(result){
			    closeWindow(systemVar.SUCCESS);
                showMessageTips("发送成功！");
			}
		);
 
}
 


 