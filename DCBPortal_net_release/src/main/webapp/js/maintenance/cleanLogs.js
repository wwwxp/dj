//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["hostGrid"] = mini.get("hostGrid");//取得主机表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    //加载主机表格信息
    search();
});

//查询
function search() {
    var paramsObj = JsVar["queryFrom"].getData();
    load(paramsObj);
}
//重新加载表格
function refresh() {
    JsVar["queryFrom"].reset();
    load(null);
}
//加载表格
function load(param){
    datagridLoadPage(JsVar["hostGrid"],param,"host.queryHostList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:clean(' + index + ')">清理日志</a>';
    return html;
}

//详细信息
function clean(index){
	    var ids = new Array();
	    var rows = JsVar["hostGrid"].getSelecteds();
	    if (rows.length > 0) {
	    	for (var i = 0; i < rows.length; i++) {
	            var id = rows[i]["HOST_ID"];
	            ids.push({HOST_ID:id});
	        }
	    }else {
            showWarnMessageTips("请选中一条记录!") ;
	        return;
	    }
	    showConfirmMessageAlter("确定清理日志？",function ok(){
	    			getJsonDataByGet(Globals.baseActionUrl.MAINTENANCE_HOST_CLEAR_LOGS,ids,"清理日志",function (result){
                        showMessageTips("清理日志成功！");
	    	
	    		});
	    });
}
