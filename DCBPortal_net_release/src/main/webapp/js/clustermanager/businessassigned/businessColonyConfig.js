//定义变量， 通常是页面控件和参数
var JsVar =new Object();

//初始化
$(document).ready(function () {
    mini.parse();
    //展示程序对象
    JsVar["colonyGrid"] = mini.get("colonyGrid");
});

/**
 * TAB参数
 * @param data
 */
function loadPage(data) {
    JsVar["DATA"] = data;
    //查询当前角色业务程序列表
    queryUserProgram();
}


/**
 * 查询用户程序
 */
function queryUserProgram() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showMessageTips("请选择角色！");
        return;
    }

    var params = {
        ROLE_ID:JsVar["DATA"]["ROLE_ID"]
    }
    loadUserProgramList(params);
}

/**
 * 加载程序列表信息
 */
function loadUserProgramList(params) {
    datagridLoad(JsVar["colonyGrid"], params, "userColonyMapper.queryUserColonyConfigList");
}

/**
 * 渲染所属业务集群
 * @param e
 * @returns
 */
function onRenderBusCluster(e) {
    if(e && e.value){
        var busCluster = e.value;
        var busClusterList = busCluster.split(",");
        var elements = new Array();
        for(var i=0;i<busClusterList.length;i++){
            elements.push("<span class='label label-success' >"+busClusterList[i]+"</span>  ");
        }
        if(elements.length != 0){
            return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+elements.join(" ")+"&nbsp;</span>";
        } else {
            return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
        }
    }
    return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
}

/**
 * 新增用户业务权限
 */
function addUserColony() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showWarnMessageTips("请选择需要分配权限的角色!");
        return;
    }

    //业务参数
    var params = {
        ROLE_ID:JsVar["DATA"]["ROLE_ID"]
    };
    showDialog("角色集群指派", 480, 400, Globals.baseJspUrl.BUS_USER_JSP_DISPATCH_CLUSTER_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["colonyGrid"].reload();
                showMessageTips("角色集群指派成功!");
            }
        }, params);
}