//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    JsVar["operatorLogGrid"]=mini.get("operatorLogGrid");
    JsVar["operatorModule"]=mini.get("operatorModule");

    JsVar["LOG_CONTENT"]=mini.get("LOG_CONTENT");
    JsVar["START_TIME"]=mini.get("START_TIME");
    JsVar["END_TIME"]=mini.get("END_TIME");

    loadLogGrid({});
    loadOperaModuleCombobox();

});

//加载操作模块的下拉框
function loadOperaModuleCombobox(){

    comboxLoad( JsVar["operatorModule"],null,"operatorLogMapper.queryOperatorModule");
}

function loadLogGrid(param) {

    datagridLoadPage(JsVar["operatorLogGrid"],param,"operatorLogMapper.queryOperatorLogInfo");

}

function onRenderer(e){
    var id=e.record.ID;
    return '<a class="Update_Button" href="javascript:getLogContent(\'' + id + '\')">日志内容详情</a>';

}

function getLogContent(logId) {

    var currRow=JsVar["operatorLogGrid"].findRow(function (row) {
        if(row["ID"]==logId){
            return true;
        }
    });

    var paramsHtml = "<div style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:200px;width:99%;overflow:auto;'>" + currRow["LOG_CONTENT"] + "</div>";

    var options = {
        title: "日志内容详情",
        width: 500,
        height: 300,
        buttons: ["ok"],
        iconCls: "",
        html: paramsHtml
    }

    mini.showMessageBox(options);


}

function search() {

    var params = {
        OPERATOR_MODULE: JsVar["operatorModule"].getValue(),
        LOG_CONTENT: JsVar["LOG_CONTENT"].getValue(),
        START_TIME: JsVar["START_TIME"].getValue(),
        END_TIME: JsVar["END_TIME"].getValue()
    }
    loadLogGrid(params);
}