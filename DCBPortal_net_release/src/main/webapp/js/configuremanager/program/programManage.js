//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
	mini.parse();
	JsVar["programGrid"] = mini.get("programGrid");//取得任务表格
    JsVar["queryFrom"] = new mini.Form("#queryFrom");//取得查询表单
    //程序类型
    JsVar["PROGRAM_TYPE"] = mini.get("PROGRAM_TYPE");
    //程序名称
    JsVar["PROGRAM_NAME"] = mini.get("PROGRAM_NAME");

    //加载下拉框
    loadCombo();

	load();

});

//查询
function search() {
    var paramsObj = JsVar["queryFrom"].getData();
    //load(paramsObj);
	loadBySearch(paramsObj);
}

function loadBySearch(params) {
    datagridLoadPage(JsVar["programGrid"],params,"programDefine.queryInfoBySearch");
}

//加载表格 
function load(){
	datagridLoadPage(JsVar["programGrid"],{},"programDefine.queryProgramList");
}

/**
 * 加载下拉框
 */
function loadCombo() {
    var params = {

    };
    comboxLoad(JsVar["PROGRAM_TYPE"], params, "programDefine.queryProgramTypeList", null, null, false);
    comboxLoad(JsVar["PROGRAM_NAME"], params, "programDefine.queryProgramDefineList", null, null, false);
}

//渲染操作按钮
function onActionRenderer(e) {
	var index = e.rowIndex;
	var html= '';

	html+= '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
	html+= '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';

	return html;
}

/**
 * 添加业务程序
 */
function add(){
	showAddDialog("业务程序操作",700,450,Globals.baseJspUrl.PROGRAM_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	JsVar["programGrid"].reload();
	                showMessageTips("新增成功!");
	            }
	    });
}

/**
 * 修改业务程序
 * @param index
 */
function edit(index){
	var rowInfo = JsVar["programGrid"].getRow(index);
	showEditDialog("业务程序操作",700,450,Globals.baseJspUrl.PROGRAM_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	JsVar["programGrid"].reload();
	            	showMessageTips("修改成功!");
	            }
	},rowInfo);
}

/**
 * 删除程序
 */
function del(index) {
	var ids = new Array();
	var rows = JsVar["programGrid"].getSelecteds();
	if (rows.length > 0) {
		for (var i = 0; i < rows.length; i++) {
			ids.push({PROGRAM_CODE:rows[i]["PROGRAM_CODE"]});
		}
	}else {
        showWarnMessageTips("请选中一条记录!") ;
		return;
	}
	
	showConfirmMessageAlter("确定删除记录？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,ids,"业务程序管理-删除程序",
				function(result){
			JsVar["programGrid"].reload();
			showMessageTips("删除成功!");
		},"programDefine.delProgram");
	});
}
