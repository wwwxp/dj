//定义变量， 通常是页面控件和参数
var JsVar =new Object();

//初使化
$(document).ready(function () {
    mini.parse();
    //权限树对象
    JsVar["privilegeTree"] = mini.get("privilege_tree");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
    JsVar["params"] = {};
    JsVar["params"]["ROLE_ID"] = data["ROLE_ID"];
    JsVar["params"]["ROLE_NAME"] = data["ROLE_NAME"]

    //先去查询已有权限
    var params = {
        ROLE_ID:JsVar["params"]["ROLE_ID"]
    };

    //初始化查询所有节点数据
    initTreeData(params);


    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, null,
        function(result){
            var selectedMenuItems = result;
            var allNodes = JsVar["privilegeTree"].getAllChildNodes();
            var node;
            var selectedNode;
            for (var j = 0, jlen = selectedMenuItems.length; j < jlen; j++) {//对已选择过的菜单进行过滤控制
                selectedNode = selectedMenuItems[j];
                for (var i = 0, len = allNodes.length; i < len; i++) {
                    node = allNodes[i];
                    if (selectedNode["ID"] == node["busId"]) {
                        JsVar["privilegeTree"].checkNode(node);
                        break;
                    }
                }
            }
        },"userProgramListMapper.queryRoleProgramList");
}

/**
 * 初始化节点数据
 */
function initTreeData(params) {
    treeLoad(JsVar["privilegeTree"], null, params, Globals.baseActionUrl.BUS_USER_TREE_QUERY_PROGRAM_URL);
}

/**
 * 获取选中节点列表，值获取业务程序列表
 * @returns {Array}
 * @constructor
 */
function GetData() {
    var checkedNodes = JsVar["privilegeTree"].getCheckedNodes();
    var selNodes = [];
    if (checkedNodes != null) {
        for (var i=0; i<checkedNodes.length; i++) {
            var nodeLevel = checkedNodes[i]["nodeLevel"];
            if (nodeLevel == "4") {
                selNodes.push({
                    NODE_TYPE:checkedNodes[i]["nodeType"],
                    TASK_PROGRAM_ID:checkedNodes[i]["busId"],
                    ROLE_ID:JsVar["params"]["ROLE_ID"]
                });
            }
        }
    }
    return selNodes;
}

/**
 * 图表重新渲染事件
 * @param e
 */
function nodeRender(e) {
    var level = e.node.nodeLevel;
    if (level == "1") {   //业务主集群
        e.iconCls = "tree-node-main-cluster";
    } else if (level == "2") {  //业务集群
        e.iconCls = "tree-node-cluster";
    } else if (level == "3") {  //版本
        e.iconCls = "tree-node-version";
    } else if (level == "4") {  //业务程序
        e.iconCls = "tree-node-program";
    }
}

//查询
function search() {
    var PROGRAM_NAME = mini.get("PROGRAM_NAME").getValue();
    if (PROGRAM_NAME == "") {
        JsVar["privilegeTree"].clearFilter();
    } else {
        PROGRAM_NAME = PROGRAM_NAME.toLowerCase();
        JsVar["privilegeTree"].filter(function (node) {
            var text = node.nodeName ? node.nodeName.toLowerCase() : "";
            if (text.indexOf(PROGRAM_NAME) != -1) {
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
    //获取选中用户权限
    var paramsArray = GetData();
    var params = {
        PRIVILEGE_LIST:paramsArray,
        ROLE_ID:JsVar["params"]["ROLE_ID"]
    };

    if (paramsArray == null || paramsArray.length == 0) {
        showConfirmMessageAlter("角色【" + JsVar["params"]["ROLE_NAME"] +"】未指派任何程序，是否确认?", function ok(){
            getJsonDataByPost(Globals.baseActionUrl.BUS_USER_ADD_PROGRAM_URL, params, "业务指派管理-提交角色指派程序列表",
                function success(result){
                    closeWindow(systemVar.SUCCESS);
                });
        });
        return;
    } else {
        getJsonDataByPost(Globals.baseActionUrl.BUS_USER_ADD_PROGRAM_URL, params, "业务指派管理-提交角色指派程序列表",
            function success(result){
                closeWindow(systemVar.SUCCESS);
            });
    }
}

