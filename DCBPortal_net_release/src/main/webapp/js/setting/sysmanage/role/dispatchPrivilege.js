//定义变量， 通常是页面控件和参数
var JsVar =new Object();
var selectedMenuItems;
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["privilegeTree"] = mini.get("privilege_tree");
    JsVar["params"] = {};
    JsVar["params"]["EMPEE_ID"] = 1
});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
    JsVar["params"]["ROLE_ID"] = data;
    //先去查询已有权限
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,JsVar["params"],null,
        function(result){
            selectedMenuItems = result;
            var allNodes = JsVar["privilegeTree"].getAllChildNodes();
            var node;
            var selectedNode;
            for (var j = 0, jlen = selectedMenuItems.length; j < jlen; j++) {//对已选择过的菜单进行过滤控制
                selectedNode = selectedMenuItems[j];
                for (var i = 0, len = allNodes.length; i < len; i++) {
                    node = allNodes[i];
                    if (selectedNode["PRIVILEGE_ID"] == node["PRIVILEGE_ID"]) {
                        JsVar["privilegeTree"].checkNode(node);
                        break;
                    }
                }
            }
        },"roleMapper.queryReadyPrivilege");
}

function GetData() {
    var priCodeStr = JsVar["privilegeTree"].getValue(true);
    var priCodes = priCodeStr.split(",");
    var selectItems = [];
    var obj = null;
    for (var i = 0; i < priCodes.length; i++) {
        obj = new Object();
        obj["ROLE_ID"] = JsVar["params"]["ROLE_ID"];
        obj.PRIVILEGE_ID = priCodes[i];
        selectItems.push(obj);
    }
    return selectItems;
}
//查询
function search() {
    var ROLE_NAME = mini.get("ROLE_NAME").getValue();
    if (ROLE_NAME == "") {
        JsVar["privilegeTree"].clearFilter();
    } else {
        ROLE_NAME = ROLE_NAME.toLowerCase();
        JsVar["privilegeTree"].filter(function (node) {
            var text = node.PRIVILEGE_NAME ? node.PRIVILEGE_NAME.toLowerCase() : "";
            if (text.indexOf(ROLE_NAME) != -1) {
                return true;
            }
        });
    }
}
//按enter键时发生
function onKeyEnter(e) {
    search();
}

//新增和修改时提交
function onSubmit() {
    //入库
    var paramsArray = GetData();
    // 获取待删除的角色关联的权限列表
    var deleteArray = new Array();
    // 数据汇总保存
    var param = new Object();
    deleteArray.push(paramsArray[0]);
    var paramList = new Array();
    param["delete|roleMapper.delEmpeePrivilege"] = deleteArray;
    paramList.push(param);
    param = new Object();
    param["insert|roleMapper.insertEmpeePrivilege"] = paramsArray;
    paramList.push(param);
    //alert(saveArray);
    getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,paramList,"角色管理-指派权限",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        },"roleMapper.delEmpeePrivilege|roleMapper.insertEmpeePrivilege");
}

