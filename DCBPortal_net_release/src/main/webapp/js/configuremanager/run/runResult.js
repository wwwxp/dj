//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
});


function onLoadComplete(data){
	JsVar["RST_STR"] = data["RST_STR"];
	JsVar["RST_FLAG"] = data["RST_FLAG"];
	JsVar["RST_EXEC"] = data["RST_EXEC"];
	initResult();
}

/**
 * 输出启停信息
 */
function initResult(){
	$("#deployTextarea").html(JsVar["RST_STR"]);
	
	if (JsVar["RST_FLAG"] == busVar.ERROR) {
		showErrorMessageAlter("程序实例" + JsVar["RST_EXEC"] + "失败，请检查！");
	} else if (JsVar["RST_FLAG"] == "hidden") {   //隐藏不弹出提示

	} else if (JsVar["RST_FLAG"] == "WARN") {
        showMessageTips("程序实例部分" + JsVar["RST_EXEC"] + "成功!");
	} else {
        showMessageTips("程序实例" + JsVar["RST_EXEC"] + "成功!");
	}
	
}


