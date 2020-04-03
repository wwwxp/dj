var nowurl  = document.location.href;
nowurl = nowurl.substring(0,nowurl.indexOf("html"));
var JsVar = new Object();
var type = "";

//初始化
$(document).ready(function() {
    mini.parse();
    JsVar["addForm"] = new mini.Form("#addForm");
    JsVar["HOST_IP"] = mini.get("HOST_IP");
    JsVar["HOST_NAME"] = mini.get("HOST_NAME");
    JsVar["ENV"] = mini.get("ENV");
    JsVar["LATN_ID"] = mini.get("LATN_ID");
    loadLatnCombox();
});

/**
 * 加载本地网数据
 */
function loadLatnCombox() {
    //得到所有的LATN_ID
    var url = nowurl + 'ControlServlet.do?serviceName=HostConfigureServlet&methodName=QueryAllLatns';

    getJsonDataByPost(url,null,"查询Latns",function callback(result) {
        if(result.data){

            JsVar["LATN_ID"].setData(result.data);
            //JsVar["LATN_ID"].select(1);
        }else{
            JsVar["LATN_ID"].setData("");
        }
    });
}

/**
 * 父窗口调用的初始化方法
 * @param action 操作类型
 * @param data   参数
 * 公共方法里有调用js这个方法，让其里的js方法不报错
 */
function onLoadComplete(action,data) {
    if("edit"==action){
        //修改
        type=action;
        JsVar["HOST_ID"] = data.HOST_ID;
        JsVar["HOST_IP"].setValue(data.HOST_IP);
        JsVar["HOST_NAME"].setValue(data.HOST_NAME);
        JsVar["LATN_ID"].setValue(data.LATN_ID);
        JsVar["ENV"].setValue(data.ENV);
        JsVar["HOST_IP"].readOnly=true;
    }else {
        //增加
    }

}


/**
 * 王启帆
 * 验证IP是否已经存在
 */
function onValueChanged(){
    var HOST_IP = JsVar["HOST_IP"].getValue();
    ipCheck(HOST_IP);
}



/**
 * 保存主机信息
 */
function OnAUSubmit(){
    if(null!==type&&""!=type&&"edit"==type){
        /** 修改 */
        //验证表单
        JsVar["addForm"].validate();
        if (JsVar["addForm"].isValid() == false){
            return;
        }
        //获取表单多个控件的数据
        var hostInfo = JsVar["addForm"].getData();

        //检测IP
        var hostIp = hostInfo["HOST_IP"];
        ipCheck(hostIp);
        //修改
        var param = {
            HOST_ID:JsVar["HOST_ID"],
            HOST_NAME:hostInfo["HOST_NAME"],
            ENV:hostInfo["ENV"],
            LATN_ID:hostInfo["LATN_ID"]
        };
        getJsonDataByPost(nowurl + 'ControlServlet.do?serviceName=HostConfigureServlet&methodName=updateHost',param,
            "修改主机配置",
            function(result){
                if("success"==result.success){
                    hostInfo["success"] = result.success;
                    closeWindow(hostInfo);
                }
            });
    }else {
        //增加
        //验证表单
        JsVar["addForm"].validate();
        if (JsVar["addForm"].isValid() == false){
            return;
        }
        //获取表单多个控件的数据
        var hostInfo = JsVar["addForm"].getData();
        //检测IP
        var hostIp = hostInfo["HOST_IP"];
        ipCheck(hostIp);
        //新增
        var param = {
            HOST_IP:hostInfo["HOST_IP"],
            HOST_NAME:hostInfo["HOST_NAME"],
            ENV:hostInfo["ENV"],
            LATN_ID:hostInfo["LATN_ID"]
        };
        getJsonDataByPost(nowurl + 'ControlServlet.do?serviceName=HostConfigureServlet&methodName=addHostH',param,
            "增加主机配置",
            function(result){
                if("success"==result.success){
                    hostInfo["success"] = result.success;
                    closeWindow(hostInfo);
                }else{
                    showErrorMessageTips(result.errormsg);
                }

            });
    }
}

/**
 *  //检测IP
 * @param hostIp
 */
function ipCheck(hostIp) {
    var exp=/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
    if (hostIp.match(exp) == null) {
        showWarnMessageTips("主机IP不合法");
        JsVar["HOST_IP"].setValue("");
        return;
    }
}