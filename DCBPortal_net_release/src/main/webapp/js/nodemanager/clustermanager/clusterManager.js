var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    JsVar["CLUSTER_NAME"]=mini.get("CLUSTER_NAME");
    JsVar["CLUSTER_CODE"]=mini.get("CLUSTER_CODE");
    JsVar["clusterGrid"]=mini.get("clusterGrid");
    loadGridData({});
    refreshCombobox();
});

//刷新下拉框数据
function refreshCombobox() {
    JsVar["CLUSTER_NAME"].setValue("");
    JsVar["CLUSTER_CODE"].setValue("");
    loadClusterNameCombobox();
    loadClusterCodeCombobox();
}

/**
 * 单元格数据渲染
 * */
function onRenderer(e){
    var clusterId=e.record.ID;
    return '<a class="Delete_Button" href="javascript:updateCluster(\'' + clusterId + '\')">修改</a><a class="Delete_Button"  href="javascript:delCluster(\'' + clusterId + '\')">删除</a><a class="Delete_Button"  href="javascript:showCluster(\'' + clusterId + '\')">集群视图</a>';
}

function onMemberRenderer(e) {
    var clusterMembers = e.record.CLUSTER_MEMBER;
    var memberArray = clusterMembers.split(",");

    if (memberArray.length < 0) {
            return;
    }

    var result = "";
    for (var i = 0; i < memberArray.length; ++i) {
        result += "<span  class='label label-success'  style='margin:2px;float: left;'>" + memberArray[i] + "</span>"
    }


    return result;
}

/**
 * 加载集群名称下拉框
 * */
function loadClusterNameCombobox(){

    comboxLoad(JsVar["CLUSTER_NAME"],null,"nodeClusterManager.queryClusterName","","",false);
}

/**
 * 加载集群编码下拉框
 * */
function loadClusterCodeCombobox(param){
    comboxLoad(JsVar["CLUSTER_CODE"],param,"nodeClusterManager.queryClusterCode","","",false);
}

/**
 * 点击集群名称下拉框时触发
 */
function onValueChanged() {
    var clusterName = JsVar["CLUSTER_NAME"].getValue();
    loadClusterCodeCombobox({
        "NODE_CLUSTER_NAME":clusterName
    })
}

/**
 * 加载表格
 * */
function loadGridData(param){
    datagridLoadPage(JsVar["clusterGrid"], param, "集群管理-加载表格",Globals.ctx + "/nodeClusterManager/loadClusterInfo");
}

/**
 * 查询
 * */
function search(){
    var clusterName = JsVar["CLUSTER_NAME"].getValue();
    var clusterCode = JsVar["CLUSTER_CODE"].getValue();
    var param = {
        "NODE_CLUSTER_NAME":clusterName,
        "NODE_CLUSTER_CODE":clusterCode
    };
    loadGridData(param);
}

/**
 * 集群新增
 */
function addCluster(){
    showAddDialog("新增集群",1100,600,Globals.ctx + "/jsp/nodemanager/clustermanager/addEditCluster",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                loadGridData({});
                refreshCombobox();
                showMessageAlter("新增集群成功");
            }
        });
}

/**
 * 集群修改
 */
function updateCluster(clusterId){
    showEditDialog("修改集群",1100,600,Globals.ctx + "/jsp/nodemanager/clustermanager/addEditCluster",
        function destroy(data){     //窗口销毁时，执行的操作
            if (data == systemVar.SUCCESS) {

                loadGridData({});
                refreshCombobox();
                showMessageAlter("修改成功");
            }
        },{CLUSTER_ID:clusterId});
}

/**
 * 集群删除
 * @param clusterId
 */
function delCluster(clusterId){
    var clusterIds = new Array();
    if(!clusterId){
        var gridData = JsVar["clusterGrid"].getSelecteds();
        if(!gridData.length){
            showWarnMessageTips("请选择一条记录！");
            return;
        }
        for(var i=0;i<gridData.length;++i){
            clusterIds.push(gridData[i]["ID"]);
        }

    }else{
        clusterIds.push(clusterId);
    }

    showConfirmMessageAlter("确认删除？",function ok(){

        getJsonDataByPost(Globals.ctx + "/nodeClusterManager/deleteCluster",{IDS:clusterIds},"集群管理-集群删除",
            function success(result){
                if (result["effectRows"] != 0) {
                    loadGridData({});
                    refreshCombobox();
                    showMessageTips("删除成功");
                }else{
                    showWarnMessageTips("删除失败")
                }
            },"");
    });
}

/**
 * 
 */
function showCluster(clusterId) {
    var param = {
        action:"showView",
        clusterId:clusterId
    };
    showDialog("集群视图",1200,600,Globals.ctx + "/jsp/nodemanager/clustermanager/cluserTreeMapView",null,param);
}

