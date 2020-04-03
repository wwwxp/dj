/**
 * 新增修改集群弹框
 */

//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var fileupload;     //文件上传对象\
var file;//选择的文件对象，主要给change对象使用，
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["uploadForm"] = new mini.Form("#uploadForm");
    JsVar["FILE_NAME"] = mini.get("FILE_NAME");
    JsVar["VERSION"] = mini.get("VERSION");
    JsVar["FILE_PATH"] = mini.get("FILE_PATH");
    JsVar["CRT_DATE"] = mini.get("CRT_DATE");
    JsVar["DESCRIPTION"] = mini.get("DESCRIPTION");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	JsVar["FILE_NAME"].setValue(data.FILE_NAME);
    JsVar["VERSION"].setValue(data.VERSION);
    JsVar["FILE_PATH"].setValue(data.FILE_PATH);
    JsVar["CRT_DATE"].setValue(data.CRT_DATE,'yyyy-MM-dd HH:mm:ss');
    JsVar["DESCRIPTION"].setValue(data.DESCRIPTION);
}

