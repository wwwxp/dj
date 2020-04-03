//定义变量， 通常是页面控件和参数
var JsVar = new Object();
// 初使化
$(document).ready(function() {
	mini.parse();
	JsVar["userGrid"] = mini.get("user_datagrid");// 取得查询表格
	JsVar["queryFrom"] = new mini.Form("#queryFrom");// 取得查询表单
	
});

function onUserLoad(e){
	var rows = JsVar["userGrid"].findRows(function(row){
	    if(row.HAS_CMD > 0) return true;
	});
	JsVar["userGrid"].selects(rows);
}

/**
 * 跳转到该页面设值
 * 
 * @param action
 * @param data
 */
function onLoadComplete(action, data) {

	// 命令id
	JsVar["CMD_ID"] = data.ID;
	search();

}
// 查询
function search() {
	if(isNull(JsVar["CMD_ID"])){
		showMessageAlter("请选择命令!");
		closeWindow();
		return;
	}
	JsVar["userGrid"].deselectAll();
	var paramsObj = JsVar["queryFrom"].getData();
	paramsObj["CMD_ID"] = JsVar["CMD_ID"]
	load(paramsObj);
}


// 重新加载表格
function refresh() {
	JsVar["queryFrom"].reset();
	load(null);
}
// 加载表格
function load(param) {
	datagridLoad(JsVar["userGrid"], param, "commandConfigMapper.queryEmpee");
}


/**
 * 取消
 * 
 * @param e
 */
function onCancel(e) {
	closeWindow();
}

// 删除用户
function onSubmit() {
	if(isNull(JsVar["CMD_ID"])){
		showMessageAlter("请选择命令!");
		closeWindow();
		return;
	}
	// 新增参数
	var addParamArray = new Array();

	// 选择的
	var selectRows = JsVar["userGrid"].getSelecteds();
	if (selectRows.length > 0) {
		for (var i = 0; i < selectRows.length; i++) {

			addParamArray.push({
				EMPEE_ID : selectRows[i]["EMPEE_ID"],
				CMD_ID : JsVar["CMD_ID"]
			});
		}
	}


	showConfirmMessageAlter("确定配置命令权限？", function ok() {
		var array = new Array();
		array.push({
			"delete|commandConfigMapper.deleteEmpeeCmdRelation" : [{CMD_ID:JsVar["CMD_ID"]}]
		});
		array.push({
			"insert|commandConfigMapper.insertEmpeeCmdRelation" : addParamArray
		});

		getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,
				array, "配置", function(result) {
					
					closeWindow(systemVar.SUCCESS);
				});
	})
}
