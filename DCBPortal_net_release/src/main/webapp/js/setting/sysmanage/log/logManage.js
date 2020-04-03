//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["logGrid"] = mini.get("log_datagrid");//取得页面表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
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
    datagridLoadPage(JsVar["logGrid"],param,"log.querySysLog");
}

/**
 * 操作渲染:查看参数
 * @param e
 */
function actionRenderer(e){
	var index = e.rowIndex;
	var html = '<a class="Delete_Button" href="javascript:scanParams(' + index + ')">参数详情</a>';
	return html;
}

/**
 * 
 * @param index
 */
function scanParams(index){
	var row=JsVar["logGrid"].getRow(index);
	var params=row.PARAMS;
	var paramsHtml="<div style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+params+"</div>";
	var options={
		    title: "参数详情",    
		    width:550,
		    height:400,
		    buttons: ["ok"],    
		    iconCls: "",
		    html: paramsHtml,   
		    callback: function(action){
		    	
		    }
		}
	mini.showMessageBox(options);
}

