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
			DATASOURCE_ID:data.DATASOURCE_ID
	};
	
	
	getJsonDataByPost(Globals.baseActionUrl.DATASOURCE_ACTION_QUERY_URL, param,
			null, function success(result) {
				JsVar["form"].setData(result);
			}, "datasourceConfigMapper.queryDSConfig");
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
	
	//密码前台加密
	params.DATASOURCE_PWD = encrypt(params.DATASOURCE_PWD);
	
	// 添加记录
	getJsonDataByPost(Globals.baseActionUrl.DATASOURCE_ACTION_INSERT_URL, params,
			"新增数据源", function(result) {
				if (result.success != null) {
					closeWindow(systemVar.SUCCESS);
				} else {
					closeWindow(systemVar.FAIL);
				}
			},"datasourceConfigMapper.insertDatasourceById");
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

	//密码前台加密
	params.DATASOURCE_PWD = encrypt(params.DATASOURCE_PWD);
	
	getJsonDataByPost(Globals.baseActionUrl.DATASOURCE_ACTION_EDIT_URL, params,
			"修改数据源",  function(result) {
				if (result.success != null) {
					closeWindow(systemVar.SUCCESS);
				} else {
					closeWindow(systemVar.FAIL);
				}
			},"datasourceConfigMapper.updateDatasourceById");
}

/**
 * 取消
 * 
 * @param e
 */
function onCancel(e) {
	closeWindow();
}

/**
 * 数据库类型事件
 * @param e
 * @returns
 */
function onDataSourceTypeChange(e){
	var value = e.value;
	
	var array = getSysDictData("driverClass");
	for(var i=0;i<array.length;++i){
		if(array[i].text == value){
			mini.get("DATASOURCE_DRIVER").setValue(array[i].code);
			return;
		}
	}
}

/**
 * 测试数据库连接是否有效
 * @param e
 * @returns
 */
function onTest(e){
	JsVar["form"].validate();

	if (JsVar["form"].isValid() == false) {
		return;
	}
	var params = JsVar["form"].getData();
	
	getJsonDataByPost(Globals.baseActionUrl.DATASOURCE_ACTION_TEST_URL, params,
			"数据源测试", function(result) {
		    
					showMessageAlter(result.success);
				
			});
}




