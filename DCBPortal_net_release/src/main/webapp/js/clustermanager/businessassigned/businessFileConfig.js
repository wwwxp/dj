//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //程序展示Grid对象
    JsVar["configGrid"] = mini.get("configGrid");
    //业务程序包类型
    JsVar["packageType"] = mini.get("packageType");
    //业务程序版本
    JsVar["version"] = mini.get("version");
    //业务主集群
    JsVar["clusterType"] = mini.get("clusterType");

    //加载包类型
    loadPackageTypeList();

    //加载业务程序版本
    loadBusVersion();

    //加载业务主集群
    loadClusterType();
});

/**
 * TAB参数
 * @param data
 */
function loadPage(data) {
    JsVar["DATA"] = data;

    //加载角色配置
    queryUserConfig();
}

function loadClusterType() {
    var params = {
        TYPE:"3"
    };
    comboxLoad(JsVar["clusterType"], params, "clusterEleDefine.queryClusterEleList");
}

/**
 * 加载业务程序版本
 */
function loadBusVersion() {
    var params = {
        FILE_TYPE:"2"
    };
    comboxLoad(JsVar["version"], params, "userProgramListMapper.queryBusVersion");
}

/**
 * 加载包类型
 */
function loadPackageTypeList() {
    var params = {
        GROUP_CODE:"WEB_BUS_PACKAGE_TYPE"
    };
    comboxLoad(JsVar["packageType"], params, "config.queryConfigList");
}

/**
 * 加载程序列表信息
 */
function loadUserProgramList(params) {
    datagridLoad(JsVar["configGrid"], params, "userProgramListMapper.queryRoleConfigList");
}

/**
 * 查询用户程序
 */
function queryUserConfig() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showMessageTips("请选择角色！");
        return;
    }
    var packageType = JsVar["packageType"].getValue();
    var version = JsVar["version"].getValue();
    var clusterType = mini.get("clusterType").getValue();
    var filePath = mini.get("filePath").getValue();
    var params = {
        PACKAGE_TYPE:packageType,
        VERSION:version,
        CLUSTER_TYPE:clusterType,
        FILE_PATH:filePath,
        ROLE_ID:JsVar["DATA"]["ROLE_ID"]
    }
    loadUserProgramList(params);
}

/**
 * 配置文件指派
 */
function addUserBusConfig() {
    if (JsVar["DATA"] == null || JsVar["DATA"] == undefined) {
        showWarnMessageTips("请选择需要分配权限的角色!");
        return;
    }

    //业务参数
    var params = {
        ROLE_ID:JsVar["DATA"]["ROLE_ID"],
        ROLE_NAME:JsVar["DATA"]["ROLE_NAME"],
    };
    showDialog("用户配置文件权限指派", 720, 620, Globals.baseJspUrl.BUS_CFGFILE_JSP_DISPATCH_PRIVILEGE_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["configGrid"].reload();
                showMessageTips("配置文件权限指派成功!");
            }
        }, params);
}

/**
 * 合并数据
 * @param e
 */
function loadUserData(e) {
    var gridData = JsVar["configGrid"].getData();
    var mergeCells2="PACKAGE_TYPE,VERSION";
    var mergeCellColumnIndex2="0,1";
    var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
    JsVar["configGrid"].mergeCells(mergeData);
}