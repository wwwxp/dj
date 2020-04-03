//定义变量， 通常是页面控件和参数
var JsVar = new Object();


$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["hostForm"] = new mini.Form("#hostForm");
    JsVar["SSH_PASSWD"] = mini.get("SSH_PASSWD");
    JsVar["SSH＿CHECK_PASSWD"] = mini.get("SSH＿CHECK_PASSWD");
});


//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    JsVar["DATA"] = data;
    initTips();
}

/**
 * 提示信息
 */
function initTips() {
    var hostIps = [];
    for (var i=0; i<JsVar["DATA"].length; i++) {
        var hostInfo = JsVar["DATA"][i]["HOST_IP"] + "(" + JsVar["DATA"][i]["SSH_USER"] + ")";
        hostIps.push(hostInfo);
    }
    $("#tips").html(hostIps.join("、 "));
}

//新增和修改点确认按钮保存
function onSubmit() {
    save();
}

//点确认新增主机
function save() {
    if(JsVar["SSH_PASSWD"].getValue() != JsVar["SSH＿CHECK_PASSWD"].getValue()){
        showWarnMessageAlter("输入的密码不一致，请重新输入",function(){
            JsVar["SSH_PASSWD"].setValue('');
            JsVar["SSH＿CHECK_PASSWD"].setValue('');
        });
        return;
    }

    //判断是否有效
    JsVar["hostForm"].validate();
    if (JsVar["hostForm"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据 
    var hostInfo = JsVar["hostForm"].getData();
    hostInfo["DATA"] = JsVar["DATA"];
    showConfirmMessageAlter("确定修改所选主机密码？", function ok() {
        getJsonDataByPost(Globals.baseActionUrl.HOST_ACTION_BATCH_UPDATE_PASSWD_URL, hostInfo, "主机管理-批量修改主机密码",
            function success(result) {
                if (result != null && result["RST_CODE"] == "1") {
                    closeWindow(systemVar.SUCCESS);
                } else {
                    showErrorMessageTips("批量修改主机密码失败，请检查！");
                }
            });
    });
}
