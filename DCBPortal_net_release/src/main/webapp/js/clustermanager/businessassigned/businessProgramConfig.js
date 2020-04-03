//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //程序展示Grid对象
    JsVar["programGrid"] = mini.get("programGrid");
    //业务程序类型
    JsVar["clusterType"] = mini.get("clusterType");
    //业务程序版本
    JsVar["busCluster"] = mini.get("busCluster");

    //集群类型
    loadClusterType();

    //加载业务主集群
    loadBusMainCluster();
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
 * 加载业务程序类型
 */
function loadClusterType() {
    var params = {
        TYPE:busVar.BUSINESS_TYPE
    };
    comboxLoad(JsVar["clusterType"], params, "clusterEleDefine.queryClusterEleList");
}

/**
 * 加载业务程序版本
 */
function loadBusMainCluster() {
    var params = {};
    comboxLoad(JsVar["busCluster"], params, "busMainCluster.queryBusMainClusterListByState");
}

/**
 * 加载程序列表信息
 */
function loadUserProgramList(params) {
    datagridLoad(JsVar["programGrid"], params, "userProgramListMapper.queryRoleProgramList");
}

/**
 * 查询用户程序
 */
function queryUserProgram() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showMessageTips("请选择角色！");
        return;
    }
    var clusterType = JsVar["clusterType"].getValue();
    var busMainCluster = JsVar["busCluster"].getValue();
    var programName = mini.get("programName").getValue();
    var params = {
        BUS_CLUSTER_ID:busMainCluster,
        CLUSTER_TYPE:clusterType,
        PROGRAM_NAME:programName,
        ROLE_ID:JsVar["DATA"]["ROLE_ID"]
    }
    loadUserProgramList(params);
}

/**
 * 重新设置集群名称
 * @param e
 */
function renderClusterType(e) {
    var clusterName = e.record.CLUSTER_NAME;
    var clusterType = e.record.CLUSTER_TYPE;
    return clusterName + "（"  + clusterType+ "）";
}

/**
 * 新增用户业务权限
 */
function addUserBusPrivilege() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showWarnMessageTips("请选择需要分配权限的角色!");
        return;
    }

    //业务参数
    var params = {
        ROLE_ID:JsVar["DATA"]["ROLE_ID"],
        ROLE_NAME:JsVar["DATA"]["ROLE_NAME"],
    };
    showDialog("角色业务程序权限指派", 480, 500, Globals.baseJspUrl.BUS_USER_JSP_DISPATCH_PRIVILEGE_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["programGrid"].reload();
                showMessageTips("业务程序权限指派成功!");
            }
        }, params);
}

/**
 * 合并数据
 * @param e
 */
function loadUserData(e) {
    var gridData = JsVar["programGrid"].getData();
    var mergeCells2="BUS_CLUSTER_NAME,CLUSTER_NAME,VERSION";
    var mergeCellColumnIndex2="0,1,2";
    var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
    JsVar["programGrid"].mergeCells(mergeData);
}