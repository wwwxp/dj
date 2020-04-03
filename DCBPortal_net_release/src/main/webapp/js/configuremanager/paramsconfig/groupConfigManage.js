//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //集群展示表格
    JsVar["groupConfigGrid"] = mini.get("groupConfigGrid");
    //取得查询表单
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    //参数所属组信息
	JsVar["GROUP_CODE"] = mini.get("GROUP_CODE");
    //初始化参数组列表
	initGroupCodeList();
    //加载主机表格信息
    search();
});

function initGroupCodeList() {
	comboxLoad(JsVar["GROUP_CODE"], {}, "config.queryGroupCodeList")
}

//查询
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    load(paramsObj);
}

//重新加载表格
function refresh() {
    JsVar["queryForm"].reset();
    load(null);
}

//加载表格
function load(param){
    datagridLoad(JsVar["groupConfigGrid"], param, "config.queryGroupConfigList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var record = e.record;
	var uid = e.record._uid;
    var html = '<a class="Delete_Button" href="javascript:addConfig()">新增</a>';
    	html += '<a class="Delete_Button" href="javascript:updateConfig(' + uid + ')">修改</a>';
     	html += '<a class="Delete_Button" href="javascript:delConfig(' + uid + ')">删除</a>';

	if(JsVar["groupConfigGrid"].isEditingRow(record)) {
		html = '<a class="Delete_Button" href="javascript:doConfigOK(' + uid + ')">确定</a>';
        html+= '<a class="Delete_Button" href="javascript:doConfigCancel(' + uid + ')">取消</a>';
	}
    return html;
}

/**
 * 确定修改或者添加参数配置
 */
function doConfigOK(uid) {
    JsVar["groupConfigGrid"].commitEdit();
    if (!JsVar["groupConfigGrid"].isChanged ()) {
        return;
    }
    var changeRowData = JsVar["groupConfigGrid"].getChanges();
    if (isNull(changeRowData)) {
        showWarnMessageTips("修改&新增配置参数为空，无法操作!");
        updateConfig(uid);
		return;
	}

	if (isNull(changeRowData[0]["CONFIG_NAME"]) || isNull(changeRowData[0]["CONFIG_VALUE"]) || isNull(changeRowData[0]["GROUP_CODE"])) {
        showWarnMessageTips("属性组&参数名称&参数值均不能为空，请确认!");
        updateConfig(uid);
		return;
	}

	if (!isNull(changeRowData[0]["SEQ"]) && !changeRowData[0]["SEQ"].match(/\d/g)) {
        showWarnMessageTips("显示顺序请输入正整数，请确认!");
        updateConfig(uid);
        return;
	}
    getJsonDataByPost(Globals.baseActionUrl.GROUP_CONFIG_ADD_URL, changeRowData[0], "参数管理-新增&修改参数",
        function(result){
    		if (result != null && result["RST_CODE"] == "1") {
                JsVar["groupConfigGrid"].reload();
                showMessageTips("修改&新增配置参数成功!")
			} else {
                showMessageTips("修改&新增配置参数失败，请检查!")
			}
        });
}

/**
 * 取消加载配置
 * @param uid
 */
function doConfigCancel(uid) {
    JsVar["groupConfigGrid"].reload();
}

/**
 * 新增参数配置
 */
function addConfig() {
	var newRow = {};
    JsVar["groupConfigGrid"].addRow(newRow, 0);
    JsVar["groupConfigGrid"].cancelEdit();
    JsVar["groupConfigGrid"].beginEditRow(newRow);
}

/**
 * 修改参数配置
 * @param uid
 */
function updateConfig(uid) {
    var rowConfig = JsVar["groupConfigGrid"].getRowByUID(uid);
    if (rowConfig) {
        JsVar["groupConfigGrid"].cancelEdit();
        JsVar["groupConfigGrid"].beginEditRow(rowConfig);
	}
}

/**
 * 删除行信息
 */
function delConfig(uid) {
    var rowConfig = JsVar["groupConfigGrid"].getRowByUID(uid);
    if (rowConfig) {
        showConfirmMessageAlter("确定删除记录？",function ok(){
            getJsonDataByPost(Globals.baseActionUrl.GROUP_CONFIG_DEL_URL, rowConfig, "参数管理-删除参数信息",
                function(result){
            		if (result != null && result["RST_CODE"] == "1") {
                        JsVar["groupConfigGrid"].reload();
                        showMessageTips("删除配置参数成功!")
					} else {
                        showErrorMessageTips("删除配置参数失败，请检查!")
					}
                });
        });
	}
}