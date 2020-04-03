//定义变量， 通常是页面控件和参数
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["resultCodeForm"] = new mini.Form("#resultCodeForm");
 
});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    if (action == systemVar.EDIT) {
        findById(data);
        mini.get("OCS_RESULT_CODE").disable();
        mini.get("OCP_RESULT_CODE").disable();
    }
}

//新增和修改点确认按钮保存
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        updateResultCode();
    } else {
        saveResultCode();
    }
}



//点确认新增结果码
function saveResultCode() {
    //判断是否有效
    JsVar["resultCodeForm"].validate();
    if (JsVar["resultCodeForm"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据
    var resultCodeMap = JsVar["resultCodeForm"].getData();
    resultCodeMap = mini.clone(resultCodeMap);
    resultCodeMap["CODE_TYPE"]='1';
    resultCodeMap["EFF_DATE"] = mini.formatDate(resultCodeMap["EFF_DATE"],"yyyyMMddHHmmss");
    resultCodeMap["EXP_DATE"] = mini.formatDate(resultCodeMap["EXP_DATE"],"yyyyMMddHHmmss");
    getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL,[resultCodeMap],"结果码管理-新增结果码",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        },"monitorMapper.saveResultCode","anotherDataSource");
}

//修改结果码
function updateResultCode() {
    //判断是否有效
    JsVar["resultCodeForm"].validate();
    if (JsVar["resultCodeForm"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var resultCodeMap = JsVar["resultCodeForm"].getData();
    resultCodeMap = mini.clone(resultCodeMap);
    resultCodeMap["EFF_DATE"] = mini.formatDate(resultCodeMap["EFF_DATE"],"yyyyMMddHHmmss");
    resultCodeMap["EXP_DATE"] = mini.formatDate(resultCodeMap["EXP_DATE"],"yyyyMMddHHmmss");
    getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[resultCodeMap],"结果码管理-修改结果码",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        },"monitorMapper.updateResultCode","anotherDataSource");
}



//编辑初始化数据
function findById(resultCodeMap) {
    //查询结果码信息
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,resultCodeMap,null,
        function success(result){
            JsVar["resultCodeForm"].setData(result);
        },"monitorMapper.queryResultCodeById","anotherDataSource");
}
