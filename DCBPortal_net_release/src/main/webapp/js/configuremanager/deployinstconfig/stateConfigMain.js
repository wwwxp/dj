/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
var paramData = new Object();

//初始化
$(document).ready(function () {
    load()
});


function load() {
    var url = Globals.ctx +"/jsp/configuremanager/deployinstconfig/busInstConfigMain";
    getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_GET_URL, null, "集群管理-获取集群",
        function(result){
            var tab_str="";
            if(result != null && result.length>0){
                var tabs = mini.get("#state_tabs");
                for (var i=0; i<result.length; i++) {
                    var tab = {
                        title:result[i]["BUS_CLUSTER_NAME"],
                        id:result[i]["BUS_CLUSTER_ID"],
                        code:result[i]["BUS_CLUSTER_CODE"],
                        dataField:result[i]["BUS_CLUSTER_ID"],
                        showCloseButton: false,
                        url : url
                    };
                    tabs.addTab(tab);
                }

                tabs.on("beforeactivechanged", function(e){
                    mini.get("#state_tabs").reloadTab(e.tab);
                });

                //给Tabs绑定加载事件，用来传递参数
                tabs.on("tabload", function(e) {
                    var item = {
                        BUS_CLUSTER_ID:e.tab.id,
                        BUS_CLUSTER_CODE:e.tab.code,
                        BUS_CLUSTER_NAME:e.tab.title
                    };
                    var iframe = tabs.getTabIFrameEl(e.tab);
                    iframe.contentWindow.loadPage(item);
                });
                //给第一个tab加上active动作
                tabs.setActiveIndex(0);
            }
        });
}

