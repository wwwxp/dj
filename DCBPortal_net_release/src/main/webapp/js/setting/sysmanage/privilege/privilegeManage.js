//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    //页面form表单
    JsVar["privilageForm"] = new mini.Form("#privilege_form");
    //左边菜单权限树
    JsVar["privilegeTree"] = mini.get("privilege_tree");
    JsVar["PARENT_PRIVILEGE_CODE"] = mini.get("PARENT_PRIVILEGE_CODE");
    //权限树
    JsVar["parentNodeSelect"] = mini.get("parentNodeSelect");

    loadTree();//加载树
    loadParentNodeSelectTree();
});
//左边树加载前
function onBeforeOpen(e) {
    var node = JsVar["privilegeTree"].getSelectedNode();
    if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
        e.htmlEvent.preventDefault();
        e.cancel = true;
        return;
    }
}
//增加树节点
function onAddNode(e) {
    saveNode();
}
//删除树节点
function onRemoveNode(e) {
    var node = JsVar["privilegeTree"].getSelectedNode();
    var data = new Object();
    data['PRIVILEGE_ID'] = node.PRIVILEGE_ID;
    if (node) {
        showConfirmMessageAlter("确定删除记录？",function ok(){
            getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,[data],"权限管理-删除权限",
                function(result){
                    //JsVar["privilegeTree"].removeNode(node);
                    refreshPrivilegeTree();

                },"privilegeMapper.delPrivilege");
        });
    }
}
//新增树节点
function saveNode() {
    var node = JsVar["privilegeTree"].getSelectedNode();
     var dataObj =  new Object();
    dataObj["PARENT_PRIVILEGE_NAME"] = node["PRIVILEGE_NAME"];
    dataObj["PARENT_PRIVILEGE_ID"] = node["PRIVILEGE_ID"];
    showAddDialog("新增权限",650,400,Globals.baseJspUrl.PRIVILEGE_JSP_ADD_URL,
        function success(action){
            if (action == systemVar.SUCCESS) {
                refreshPrivilegeTree();
                showMessageTips("新增权限成功!");
            }
    },dataObj);
}
//点击树节点时，显示右边详细信息
function onClickTreeNode() {


    var selectedNode = JsVar["privilegeTree"].getSelectedNode();
    if(selectedNode["PARENT_PRIVILEGE_ID"]== "-1"){
        mini.get("saveBtn").setEnabled(false);
       return;
    }
    mini.get("saveBtn").setEnabled(true);
    var PARENT_PRIVILEGE_CODE = JsVar["privilegeTree"].getParentNode(selectedNode).PRIVILEGE_CODE;
    selectedNode["PARENT_PRIVILEGE_CODE"] = PARENT_PRIVILEGE_CODE;
    JsVar["CODE_VALUE"] = PARENT_PRIVILEGE_CODE;
    var obj=new Object();
    JsVar["privilageForm"].setData(selectedNode);
}
//选择时发生
function beforenodeselect(e){
    var selectNode = e.node;
    JsVar["PARENT_PRIVILEGE_CODE"].setValue(selectNode["PRIVILEGE_CODE"]);
   return true;

}
//保存权限
function onSubmit() {
    JsVar["privilageForm"].validate();
    if (JsVar["privilageForm"].isValid() == false){
        return;
    }
    var privilegeInfo = JsVar["privilageForm"].getData();
    privilegeInfo["PRIVILEGE_ID"] = JsVar["privilegeTree"].getSelectedNode()["PRIVILEGE_ID"];
    privilegeInfo["SAVE_FLAG"] = "UPDATE";
    getJsonDataByPost(Globals.baseActionUrl.PRIVILEGE_ACTION_UPDATE_URL,[privilegeInfo],"权限管理-修改权限",
        function(result){
            refreshPrivilegeTree();
            showMessageTips("修改权限成功!");
        });
}

//重置表单
function onCancel() {
    closeWindow();
}
//查询权限
function search() {
    var PRIVILEGE_NAME = mini.get("PRIVILEGE_NAME").getValue();
    if (PRIVILEGE_NAME == "") {
        JsVar["privilegeTree"].clearFilter();
    }
    else {
        PRIVILEGE_NAME = PRIVILEGE_NAME.toLowerCase();
        JsVar["privilegeTree"].filter(function (node) {
            var text = node.PRIVILEGE_NAME ? node.PRIVILEGE_NAME.toLowerCase() : "";
            if (text.indexOf(PRIVILEGE_NAME) != -1) {
                return true;
            }
        });
    }
}

//刷新权限树
function refreshPrivilegeTree() {
    loadTree();
    setEnableToEditPage();
}
function loadTree(){
    treeLoad(JsVar["privilegeTree"],"privilegeMapper.queryPrivilege");
    JsVar["privilegeTree"].expandAll();
}
function loadParentNodeSelectTree(){
    treeLoad(JsVar["parentNodeSelect"],"privilegeMapper.queryPrivilege");
}
//把编辑页面置为不可用状态
function setEnableToEditPage() {
    JsVar["privilageForm"].reset();
    mini.get("saveBtn").setEnabled(false);
}

