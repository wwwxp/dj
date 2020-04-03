//定义变量， 通常是页面控件和参数
var JsVar = new Object();

//初始化
$(document).ready(function () {
     mini.parse();
    JsVar["BUS_CLUSTER_ID_CHECKBOX"] = mini.get("BUS_CLUSTER_ID");
});

/**
 * 初始化用集群户选中状态
 */
function onLoadCheckCluster() {
    var params = {
        ROLE_ID:JsVar["ROLE_ID"]
    };

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,params,null,
        function success(result){
            var length =  result.length;
            var initID = "";
            for(var i = 0;i<length;i++){
                initID += result[i].BUS_CLUSTER_ID+",";
            }
            mini.get("BUS_CLUSTER_ID").setValue(initID.substring(0,initID.length-1));
        },"userColonyMapper.queryUserColonyConfigList");
}

/**
 * 父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
 * @param action
 * @param data
 */
function onLoadComplete(data) {
    JsVar["ROLE_ID"] = data["ROLE_ID"];
    onLoadCheckCluster();
}

/**
 * 用户集群指派
 */
function onSubmit() {

    var data=[];
    var checkValues =  JsVar["BUS_CLUSTER_ID_CHECKBOX"].getValue().split(",");
    for(var i = 0;i<checkValues.length;i++){
        var obj = {ROLE_ID:JsVar["ROLE_ID"],BUS_CLUSTER_ID:checkValues[i]};
        data.push(obj);
    }

    var paramList = new Array();
    var param = {};
    param["delete|userColonyMapper.delRoleMain"] = [{
        ROLE_ID:JsVar["ROLE_ID"]
    }];
    paramList.push(param);
    param = new Object();
    param["insert|userColonyMapper.addRoleMain"] = data;
    paramList.push(param);
    getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL,paramList,"用户集群指派",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

/**
 * 多选框事件
 */
function checkboxChanged(){
    var clusterId =  this.getValue().split(",");
    JsVar["CLUSTER_ID"] = this.getValue();
    var params = {
        ROLE_ID:JsVar["ROLE_ID"]
    };

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,params,null,
        function success(result){

            var length =  result.length;
            var flag = false;
            var initID="",clusterName="";
            if(length > 0){
                for(var i = 0;i<length;i++){
                    initID += result[i].BUS_CLUSTER_ID+",";
                      var count = 0;
                      var busCLusterID =result[i].BUS_CLUSTER_ID;
                      for (var h =0;h<clusterId.length;h++){
                           if (busCLusterID == clusterId[h]){
                               count++;
                               flag = true;
                               break;
                           }else{
                               clusterName=result[i].BUS_CLUSTER_NAME;
                           }
                       }
                       if(count === 0){
                            flag = false;
                            break;
                       }
                }

                if(!flag){
                    var clusterID = initID.substring(0,initID.length-1)+","+ JsVar["CLUSTER_ID"];
                    clusterID = [clusterID].unique();
                    mini.get("BUS_CLUSTER_ID").setValue(clusterID.toString());
                    showWarnMessageTips(clusterName + "己被程序启停/配置文件指派，请删除后进行勾选操作");
                }
            }
        },"userColonyMapper.queryRoleProgramList");
}

