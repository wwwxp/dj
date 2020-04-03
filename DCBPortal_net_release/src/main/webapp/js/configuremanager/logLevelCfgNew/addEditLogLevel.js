//定义变量， 通常是页面控件和参数
var JsVar = new Object();

$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["logLevelForm"] = new mini.Form("#logLevelForm");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action, data) {
    JsVar[systemVar.ACTION] = action;
    JsVar["DATA"] = data;
    if (action == systemVar.EDIT) {
    	initData(data);
    }
}

//新增和修改点确认按钮保存
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        update();
    } else {
        save();
    }
}

//点确认新增主机
function save() {
    //判断是否有效
    JsVar["logLevelForm"].validate();
    if (JsVar["logLevelForm"].isValid() == false){
        return;
    }
    
    //新增操作下获取表单的数据 
    var logLevelData = JsVar["logLevelForm"].getData();
    
    getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_NEW_ADD_URL, logLevelData, "日志级别管理-新增日志级别",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//修改主机
function update() {
    //判断是否有效
    JsVar["logLevelForm"].validate();
    if (JsVar["logLevelForm"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var logLevelData = JsVar["logLevelForm"].getData();
    logLevelData["PRO_ID"] = JsVar["DATA"]["PRO_ID"];
    
    getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_NEW_UPATE_URL, logLevelData, "日志级别管理-修改日志级别",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//编辑初始化数据
function initData(data) {
	JsVar["logLevelForm"].setData(data);
}

/**
 * 校验编码
 * @param e
 */
function onEnglishAndNumberValidation(e) {
    if (e.isValid) {
        if (isEnglishAndNumber(e.value) == false) {
            e.errorText = "参数名称只能包含数字和字母！";
            e.isValid = false;
        }
    }
}

/**
 * 英文或者数字
 * @param v
 * @returns {Boolean}
 */
function isEnglishAndNumber(v) {
    var re = new RegExp("^[0-9a-zA-Z\_]*$");
    if (re.test(v)) return true;
    return false;
}
