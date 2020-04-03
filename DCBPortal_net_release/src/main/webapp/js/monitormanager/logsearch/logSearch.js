/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryForm"] = new mini.Form("#queryForm");
    //高亮
    searchKeyHighLight();
});

/**
 * 返回
 */
function back(){
	history.back();
}
/**
 * 搜索关键字高亮
 */
function searchKeyHighLight(){
	var ignore_before=mini.get("#ignore_before").value=="true"?true:false;
	$("#html-data").textSearch(mini.get("key").value,{markCss: "font-weight:bold;background:yellow;",nullReport:false,caseIgnore:ignore_before});
}

/**
 * 日志搜索
 */
function search(){
	JsVar["queryForm"].validate();
	if (JsVar["queryForm"].isValid() == false){
        return;
    }
	$("#queryForm").submit();
}

