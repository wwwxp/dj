/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var MyChart=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["ClusterRelationGrid"] = mini.get("ClusterRelationGrid");
    JsVar["clusterGrid"] = mini.get("clusterGrid");
    JsVar["topologyGrid"] = mini.get("topologyGrid");
    JsVar["nimbusGrid"] = mini.get("nimbusGrid");
    JsVar["supervisorGrid"] = mini.get("supervisorGrid");
    JsVar["zookeeperGrid"] = mini.get("zookeeperGrid");

    loadingClusterTab();
});


/**
 * 加载业务主集群,只显示具有Jstorm集群的业务主集群
 */
function loadingClusterTab() {
    var params = {
        CLUSTER_TYPE:busVar.JSTORM
    };
    //datagridLoad(JsVar["ClusterRelationGrid"],params,"busMainCluster.queryBusMainClusterRelationJstormList");
    datagridLoadPage(JsVar["ClusterRelationGrid"],params, null, Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_BUS_CLUSTER_LIST_URL);
}


//渲染主ZK集群名称单元格成超链接
function onRenderTopName(e) {
    var index = e.rowIndex;
    return '<a class="Delete_Button" href="javascript:hostDetail(' + index + ')">' + e.record.BUS_CLUSTER_NAME + '</a>';
}

/**
 * 点击集信息添加TAB页
 */
function hostDetail(index) {
    var row = JsVar["ClusterRelationGrid"].getRow(index);

    var tabs = window.parent.mini.get('cluster_tabs');
    var tabData = tabs.getTabs();

    //业务主集群
    JsVar["BUS_CLUSTER_ID"] = row.BUS_CLUSTER_ID;
    JsVar["BUS_CLUSTER_NAME"] = row.BUS_CLUSTER_NAME;
    //当前Jstorm集群编码
    JsVar["CLUSTER_CODE"] = row.CLUSTER_CODE;

    // 重复tab页return
    for (var i = 0;i<tabData.length;i++){
        var data = tabData[i];
        if(data["id"] == row.BUS_CLUSTER_ID){
            tabs.activeTab(data);
            return;
        }
    }

    var tab = {
        title : row.BUS_CLUSTER_NAME,
        id : row.BUS_CLUSTER_ID,
        code:row.BUS_CLUSTER_CODE,
        clusterCode:row.CLUSTER_CODE,
        dataField:row.BUS_CLUSTER_ID,
        showCloseButton: true,
        url: Globals.baseJspUrl.BUS_JSP_DISPATCH_CLUSTER_URL+'?CLUSTER_CODE='+row.CLUSTER_CODE
    }
    tab = tabs.addTab(tab);


    // 选中tab添加的页面
    tabs.activeTab(tab);
}
