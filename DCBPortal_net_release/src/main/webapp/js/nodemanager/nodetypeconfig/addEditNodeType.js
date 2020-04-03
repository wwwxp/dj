//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();

    //程序类型表格
    JsVar["nodeTypeForm"]=new mini.Form("#nodeTypeForm");
    //业务组下拉框
    JsVar["BUS_GROUP"]=mini.get("BUS_GROUP");

    JsVar["DIFF_CFG"]=mini.get("DIFF_CFG");
    JsVar["RUN_WEB"]=mini.get("RUN_WEB");
});

/**
 * 业务组下拉框的加载
 */
function loadBusGroupCombobox(){

    comboxLoad( JsVar["BUS_GROUP"],null,"nodeTypeManagerMapper.queryBusGroupOnBusGroupTable","","",false);
}

//调用showAddDialog/showEditDialog时会执行
function onLoadComplete(action,data) {

    loadBusGroupCombobox();
    JsVar["DIFF_CFG"].setData([{text:'是',id:1},{text:'否',id:0}]);
    JsVar["RUN_WEB"].setData([{text:'是',id:1},{text:'否',id:0}]);

    //设置默认选中项
    JsVar["DIFF_CFG"].setValue(0);
    JsVar["RUN_WEB"].setValue(0);

    JsVar[systemVar.ACTION] = action;

    if (action == systemVar.EDIT){
        findNodeTypeById(data);
    }
}

/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
    if(JsVar[systemVar.ACTION] == systemVar.EDIT){
        updateNodeType();
        return;
    }

    addNodeType();
}

/**
 * 判断linux路径是否为正确的二级及二级以上的绝对路径
 */
function checkDefaultPath(path) {
    var reg = new RegExp("^/[^/]+/[^/]+(/[^/]+)*/?$");

    return reg.test(path);
}

/**
 * 判断编码是否为正确格式
 */
function checkCode(elem) {
    var reg = new RegExp("^[\\w-]+$");

    return reg.test(elem);
}

/**
 * 判断版本是否为正确格式
 */
function checkVersion(elem) {
    var reg = new RegExp("^[1-9][0-9]*\.[0-9]\.[0-9]$");

    return reg.test(elem);
}

/**
 * 程序类型的新增
 */
function addNodeType() {
    JsVar["nodeTypeForm"].validate();

    if(JsVar["nodeTypeForm"].isValid()===false){
        return;
    }


    var nodeTypeInfo=JsVar["nodeTypeForm"].getData();

    var defaultPath=nodeTypeInfo["DEFAULT_PATH"];

    if(defaultPath!='' && !checkDefaultPath(defaultPath)){

        showWarnMessageTips("输入的路径要么为空，要么必须为2级以上的合法的绝对路径！");
        return;
    }

    if(!checkCode(nodeTypeInfo["CODE"])){
        showWarnMessageTips("程序编码只能由字母、数字、-、下划线组成！");
        return;
    }

    // if(!checkVersion(nodeTypeInfo["START_VERSION"])){
    //
    //     showWarnMessageTips("开始版本号的格式不正确，请重新输入！");
    //     return;
    // }

    showLoadMask();
    getJsonDataByPost(Globals.ctx + "/nodeTypeManager/addNodeType",nodeTypeInfo,"程序类型配置-新增程序类型-程序类型信息插入",
        function success(result) {
            if(result["effectRow"]>=0) {
                closeWindow(systemVar.SUCCESS);
            }else{
                showWarnMessageTips(result["errorMsg"]);
            }
        });
}

/**
 * 通过ID获得程序类型信息
 * @param nodeTypeId
 */
function findNodeTypeById(nodeTypeId) {
    var param={
        ID:nodeTypeId
    };

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,param,null,
        function success(result) {

            if(result!=null && result!=undefined){
                JsVar["nodeTypeForm"].setData(result);
                JsVar["BUS_GROUP"].setValue(result["BUS_GROUP_ID"]);
            }

        },"nodeTypeManagerMapper.queryNodeType");
}

/**
 * 更新程序类型信息
 */
function updateNodeType() {
    JsVar["nodeTypeForm"].validate();

    if(JsVar["nodeTypeForm"].isValid()===false){
        return;
    }
    var nodeTypeInfo=JsVar["nodeTypeForm"].getData();

    var defaultPath=nodeTypeInfo["DEFAULT_PATH"];

    if(defaultPath!='' && !checkDefaultPath(defaultPath)){

        showWarnMessageTips("输入的路径要么为空，要么必须为2级以上的合法的绝对路径！");
        return;
    }

    if(!checkCode(nodeTypeInfo["CODE"])){
        showWarnMessageTips("程序编码只能由字母、数字、-、下划线组成！");
        return;
    }

    // if(!checkVersion(nodeTypeInfo["START_VERSION"])){
    //
    //     showWarnMessageTips("开始版本号的格式不正确，请重新输入！");
    //     return;
    // }

    showLoadMask();

    var nodeTypeInfo=JsVar["nodeTypeForm"].getData();

    getJsonDataByPost(Globals.ctx + "/nodeTypeManager/updateNodeType",nodeTypeInfo,"程序类型配置-修改程序类型",
        function success(result) {
            if(result["effectRow"]!=-1) {
                closeWindow(systemVar.SUCCESS);
            }else{
                showWarnMessageTips(result["errorMsg"]);
            }
        });
}

function onCancel(e) {
    closeWindow();
}