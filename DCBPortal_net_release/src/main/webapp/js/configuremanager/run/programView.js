//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["CONTENT"] = mini.get("CONTENT");//取得任务表格
});


function onLoadComplete(data){
	
	JsVar["CONTENT"].setValue(data.info+"\n 返回结果："+data.reason);
}

