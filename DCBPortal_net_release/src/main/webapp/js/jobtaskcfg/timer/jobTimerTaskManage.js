/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
	mini.parse();
	// 表格获取
	JsVar["datagrid"] = mini.get("datagrid");
	search();

});

function onLoadComplete(data) {
	var row = JsVar["datagrid"].findRow(function(row) {
		if (row.ID == data)
			return true;
	});
	JsVar["datagrid"].select(row);
}

/**
 * 查询
 */
function search() {
	var param = mini.get("SEARCH_PARAM").getValue();
	loaddatagrid({
		TASK_NAME : param
	});
}

/**
 * 加载定时任务表格
 */
function loaddatagrid(paramsObj) {
	// 加载表格信息
	// datagridLoad(JsVar["datagrid"],paramsObj,"jobtaskcfg.queryTaskList");
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,
			paramsObj, "定时任务管理-查询定时任务配置", function success(result) {
				JsVar["datagrid"].setData(result);
			}, "jobtaskcfg.queryTaskList", null, false);
}

function isalarmRenderer(e) {
	var IS_ALARM = e.record.IS_ALARM;
	if (IS_ALARM == 'yes') {
		return "是";
	} else {
		return "否";
	}
}

/**
 * 状态渲染
 */
function statusRenderer(e) {
	var STATE = e.record.STATUS;
	if (STATE == 1) {
		return "执行中";
	} else {
		return "未执行";
	}
}

/**
 * 类型渲染
 */
function taskTypeRenderer(e) {
	return render(e, "job_task_type");
}

/**
 * 类型渲染
 */
function statusTypeRenderer(e) {
	return render(e, "job_task_cron_status");
}

/**
 * 操作渲染
 */
function optionRenderer(e) {
	var index = e.rowIndex;
	var html = "";

	// html += '<a class="Delete_Button" href="javascript:immediately(' + index
	// + ')">执行</a>';
	html += '<a class="Delete_Button" href="javascript:edit(' + index
			+ ')">修改</a>';
	html += '<a class="Delete_Button" href="javascript:del(' + index
			+ ')">删除</a>';
	return html;
}
/**
 * 立刻执行
 * 
 * @param index
 */
function immediately(index) {
	var row = JsVar["datagrid"].getRow(index);
	getJsonDataByPost(Globals.baseActionUrl.JOBTASKCFG_ACTION_TIMER_URL, [ {
		ID : row["ID"]
	} ], "定时任务管理-执行" + row["TASK_NAME"], function success(result) {
		search();
        showMessageTips("任务将在规定时间内执行");
	});
}
/**
 * 停止
 * 
 * @param index
 */
function stop(index) {
	var row = JsVar["datagrid"].getRow(index);
	getJsonDataByPost(Globals.baseActionUrl.JOBTASKCFG_ACTION_STOP_URL, [ {
		ID : row["ID"]
	} ], "定时任务管理-执行" + row["TASK_NAME"], function success(result) {
		search();
        showMessageTips("任务已执行");
	});
}
/**
 * 新增定时任务
 */
function add() {
	showAddDialog("定时任务配置--新增", 580, 450,
			Globals.baseJspUrl.JOBTIMERTASKCFG_JSP_ADD_EDIT_URL,
			function destroy(data) {
				if (data == systemVar.SUCCESS) {
					search();
					//showMessageAlter("新增成功");
					showMessageTips("新增成功");
				}
			});
}

/**
 * 修改定时任务
 * 
 * @param index
 */
function edit(index) {
	var row = JsVar["datagrid"].getRow(index);
	showEditDialog("定时任务配置--修改", 580, 450,
			Globals.baseJspUrl.JOBTIMERTASKCFG_JSP_ADD_EDIT_URL,
			function destroy(data) {
				if (data == systemVar.SUCCESS) {
					search();
					//showMessageAlter("修改成功!");
					showMessageTips("修改成功!");
				}
			}, row);
}

/**
 * 删除定时任务
 * 
 * @param index
 */
function del(index) {
	var row = JsVar["datagrid"].getRow(index);
	var ids = new Array();
	ids.push({
		ID : row["ID"]
	});
	var param =
	showConfirmMessageAlter("确定删除记录?", function ok() {
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, {ID:row["ID"]},
				"定时任务配置--删除", function(result) {
					// 查询是否有关联记录，如果有不允许删除
					if (!isNull(result)) {

                        showWarnMessageTips("有关联定时任务不允许删除!");
						
					}else{
						getJsonDataByPost(
								Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,
								ids, "定时任务配置--删除", function(result) {
									search();
									//showMessageAlter("删除定时任务成功!");
									showMessageTips("删除定时任务成功!");
								}, "jobtaskcfg.delTask");
					}

				}, "jobtaskcfg.queryJobTaskBusRelation");
	});
}

function onSubmit() {
	var rows = JsVar["datagrid"].getSelecteds();
	if (rows.length < 1) {
        showWarnMessageTips("请选择至少一条任务");
		return;
	}
	var obj = {
		"text" : "",
		"value" : ""
	};
	for (var i = 0; i < rows.length; i++) {
		if (i == rows.length - 1) {
			obj["text"] += rows[i]["TASK_NAME"];
			obj["value"] += rows[i]["ID"];
		} else {
			obj["text"] += rows[i]["TASK_NAME"] + ",";
			obj["value"] += rows[i]["ID"] + ",";
		}
	}
	closeWindow(obj);
}