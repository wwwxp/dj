//定义变量， 通常是页面控件和参数
var JsVar = new Object();
// 初使化
$(document).ready(function() {
	mini.parse();
	JsVar["form"] = new mini.Form("form");
	JsVar["showHighAttrBtn"] = mini.get("showHighAttrBtn");
	
	//默认隐藏连接池属性
	$("#config").hide();
	$("#configInput").hide(); 
});

/**
 * 新增和修改提交
 * 
 * @param e
 */
function onSubmit(e) {
	if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
		edit();
		return;
	}
	add();
}

/**
 * 跳转到该页面设值
 * 
 * @param action
 * @param data
 */
function onLoadComplete(action, data) {
	JsVar[systemVar.ACTION] = action;
	if (action == systemVar.EDIT) {
		findById(data);
		return;
	}
}

/**
 * 根据PAGE_ID查找report
 * 
 * @param data
 * @returns
 */
function findById(data) {
	var param = {
			ID:data.ID
	};
	
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param,
			null, function success(result) {
				JsVar["form"].setData(result);
			}, "commandConfigMapper.queryCMDConfig");
}

/**
 * 新增
 * 
 * @returns
 */
function add() {
	JsVar["form"].validate();
	if (JsVar["form"].isValid() == false) {
		return;
	}
	var params = JsVar["form"].getData();
	var PAGE_NAME = params.PAGE_NAME;
	var isHave = false;

	// 判断是否存在
	/*getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, {
		PAGE_NAME : PAGE_NAME
	}, null, function success(result) {
		if (result.PAGE_NAME) {
			showWarnMessageAlter("报表数据源名已存在！");
			isHave = true;
		}
	}, "commandConfigMapper.query", null, false);

	if (isHave) {
		return;
	}*/

	// 添加记录
	getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL, [params],
			"报表数据源管理-报表数据源新增", function(result) {
				if (result == null) {
					closeWindow(systemVar.SUCCESS);
				} else if (result.retCode == '1') {
					showWarnMessageAlter(result.retMsg, function() {
						closeWindow(systemVar.FAIL);
					});
				}
			},"commandConfigMapper.insertCMDById");
}

/**
 * 修改
 * 
 * @returns
 */
function edit() {
	JsVar["form"].validate();

	if (JsVar["form"].isValid() == false) {
		return;
	}
	var params = JsVar["form"].getData();

	getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL, [params],
			"报表数据源管理-报表数据源修改", function(result) {
				if (result == null) {
					closeWindow(systemVar.SUCCESS);
				} else if (result.retCode == '1') {
					showWarnMessageAlter(result.retMsg, function() {
						closeWindow(systemVar.FAIL);
					});
				}
			},"commandConfigMapper.updateCMDById");
}

/**
 * 取消
 * 
 * @param e
 */
function onCancel(e) {
	closeWindow();
}





