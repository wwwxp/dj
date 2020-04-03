//定义变量， 通常是页面控件和参数
var JsVar = new Object();
// 初始化
$(document).ready(function() {
	mini.parse();

	JsVar["datagrid"] = mini.get("datagrid");
	JsVar["form"] = new mini.Form("#form");

	loadDataGrid();
});

/**
 * 查询
 * @returns
 */
function search(){
	var params = JsVar["form"].getData();
	loadDataGrid(params);
}

/**
 * 查询报表数据源列表
 * 
 * @param params
 * @returns
 */
function loadDataGrid(params) {
	// 默认{}
	params = params || {};
	datagridLoadPage(JsVar["datagrid"], params, "commandConfigMapper.queryCMDConfig",
			Globals.baseActionUrl.FRAME_QUERY_PAGE_LIST, null);
}

/**
 * 刷新表格
 * 
 * @returns
 */
function refresh() {
	loadDataGrid();
}

/**
 * 新增
 * 
 * @returns
 */
function add() {
	showAddDialog("新增", 500,300, Globals.baseJspUrl.COMMAND_JSP_ADD_EDIT,
			function destroy(data) {
				if (data == systemVar.SUCCESS) {
					showMessageAlter("新增成功!");
					loadDataGrid();
				}
			});
}

/**
 * 修改集群
 * 
 * @param index
 */
function edit(index) {
	var row;
	// 单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
	if (index != undefined) {
		// 单个操作
		row = JsVar["datagrid"].getRow(index);
	} else {
		// 批量操作
		var rows = JsVar["datagrid"].getSelecteds();
		if (rows.length == 1) {
			row = rows[0];
		} else {
			showWarnMessageAlter("请选中一条记录!");
			return;
		}
	}
	
	showEditDialog("修改", 500,300, Globals.baseJspUrl.COMMAND_JSP_ADD_EDIT,
			function destroy(data) {
				if (data == systemVar.SUCCESS) {
					showMessageAlter("修改成功!");
					loadDataGrid();
				}
			}, row);
}

//删除主机
function del(index) {
	// 创建一个集合,用于存放被勾选的ID
	var ids = new Array();
	// 单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
	if (index != undefined) {
		// 单个操作
		var row = JsVar["datagrid"].getRow(index);
		ids.push({
			ID : row["ID"]
		});
	} else {
		// 得到被勾选的行对象
		var rows = JsVar["datagrid"].getSelecteds();
		if (rows.length > 0) {
			for (var i = 0; i < rows.length; i++) {
				// 此处缺少一个判断,集群是否能够删除
				ids.push({
					ID : rows[i]["ID"]
				});
			}
		} else {
			showWarnMessageAlter("请选中一条记录!");
			return;
		}
	}

	showConfirmMessageAlter("确定删除记录？", function ok() {
		getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,
				ids, "报表数据源管理-报表数据源删除", function(result) {
					showMessageAlter("删除成功!");
					loadDataGrid();
				}, "commandConfigMapper.deleteCMDById");
	});
}
/**
 * 将命令配置给 用户
 * @param index
 */
function set(index){
	var row;
	// 单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
	if (index != undefined) {
		// 单个操作
		row = JsVar["datagrid"].getRow(index);
	} else {
		// 批量操作
		var rows = JsVar["datagrid"].getSelecteds();
		if (rows.length == 1) {
			row = rows[0];
		} else {
			showWarnMessageAlter("请选中一条记录!");
			return;
		}
	}
	showEditDialog("配置:["+row.CMD_NAME+"]", 600,500, Globals.baseJspUrl.COMMAND_EMPEE_RELATION_JSP,
			function destroy(data) {
						if (data == systemVar.SUCCESS) {
							showMessageAlter("配置成功!");
							loadDataGrid();
						}
			}, row);
}


/**
 * 渲染操作按钮
 * 
 * @param e
 * @returns {String}
 */
function onRenderer(e) {
	var index = e.rowIndex;
	var rst = '<a class="Delete_Button" data-options=\'{"pId":"edit-report"}\' href="javascript:edit('
			+ index + ')">修改</a>';
	rst += '<a class="Delete_Button" data-options=\'{"pId":"del-report"}\'  href="javascript:del('
			+ index + ')">删除</a>';
	/*rst += '<a class="Delete_Button" data-options=\'{"pId":"set"}\'  href="javascript:set('
		+ index + ')">权限配置</a>';*/
	return rst;
}

/**
 * 渲染报表状态
 * @param e
 * @returns
 */
function renderDatasourceState(e) {
	return render(e,"valid_flag");
}
