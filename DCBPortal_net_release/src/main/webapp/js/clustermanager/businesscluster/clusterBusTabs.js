/**
 * Created with IntelliJ IDEA.
 * Creater: yuanhao
 * Date: 16-7-19
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //加载集群TAB
    loadingClusterTab();
});

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	//获取父iframe传递过来的Tab页签，根据页签index加载不同的Tab数据
	var url = Globals.ctx + "/jsp/clustermanager/businesscluster/clusterBusPartition?times=" + (new Date()).getTime();
	//var indexParams = window.location.search;
    var indexParams = getQueryString("index");
	if (indexParams) {
        var tabIndex =parseInt(indexParams);
        if (tabIndex == 0) {   //集群划分 
        	url = Globals.ctx + "/jsp/clustermanager/businesscluster/clusterBusPartition?times=" + (new Date()).getTime();
        } else if (tabIndex == 1) {   //集群部署
        	url = Globals.ctx + "/jsp/clustermanager/businesscluster/clusterBusDeploy?times=" + (new Date()).getTime();
        } else if (tabIndex == 3) {   //集群启停
        	url = Globals.ctx + "/jsp/clustermanager/businesscluster/clusterBusStartAndStop?times=" + (new Date()).getTime();
        }
	}
	
	getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_GET_URL, null, "集群管理-获取集群",
        function(result){
		var tab_str="";
		if(result != null && result.length>0){
			var tabs = mini.get("#deploy_tabs");
			for (var i=0; i<result.length; i++) {
				var tab = {
					title:result[i]["BUS_CLUSTER_NAME"],
					id:result[i]["BUS_CLUSTER_ID"],
					code:result[i]["BUS_CLUSTER_CODE"],
					dataField:result[i]["BUS_CLUSTER_ID"], 
					showCloseButton: false,
					url: url
				};
				tabs.addTab(tab);
			}

			tabs.on("beforeactivechanged", function(e){
                changeTabColor(result, e);
			});

			//给Tabs绑定加载事件，用来传递参数
			tabs.on("tabload", function(e) {
				var item = {
					BUS_CLUSTER_ID:e.tab.id,
					BUS_CLUSTER_CODE:e.tab.code,
					BUS_CLUSTER_NAME:e.tab.title
				};
                changeTabColor(result, e);
                var iframe = tabs.getTabIFrameEl(e.tab);
		        iframe.contentWindow.loadPage(item);
			});
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
			//tabs.reloadTab(tabs.getActiveTab());
		}
    });
}

/**
 * 修改页签切换颜色
 * @param result
 */
function changeTabColor(result, evt) {
    //获取页签标题的ID
    var activeTab = $($("#deploy_tabs").find("table:eq(1)").find("tbody>tr>td[index]")[0]);
    var activeTabId = activeTab.attr("id");
    var activeTabPrefix = "";
    if (activeTabId != null && activeTabId.indexOf("$") > 0) {
        activeTabPrefix = activeTabId.substring(0, activeTabId.indexOf("$") + 1);
    }
    var lastTabIndx = result.length - 1;
    var lastTabList = $("#deploy_tabs").find("table:eq(1)").find("tbody>tr>td[index]:gt(" + lastTabIndx + ")");
    var currTabId = activeTabPrefix + evt.tab._id;
    var nextTab = $("#deploy_tabs").find("table:eq(1)").find("tbody>tr>td[id='" + currTabId + "']");
    for (var i=0; i<lastTabList.length; i++) {
    	if ($(lastTabList[i]).attr("id") == currTabId) {
            lastTabList.splice(i, 1);
            break;
		}
	}
    lastTabList.css({"background":"#B5C2B6"});
    lastTabList.unbind("mouseover").mouseover(function(evt){
    	var oldBackground = $(this).css("background");
        $(this).css({"background":""});
        $(this).unbind("mouseout").mouseout(function(evt){
            $(this).css({"background": oldBackground});
        });
    });

    //重新设置Tab标题
    // $.each(lastTabList, function(index, item) {
    //     var tabTitle = $(item).find("span:eq(0)").html();
    //     $(item).attr("title", tabTitle);
    //     if (tabTitle != null && tabTitle.length > 6) {
    //         var showTitle = tabTitle.substring(0, 6) + "...";
    //         $(item).find("span:eq(0)").html(showTitle);
    //     }
    // });

    var currIndx = nextTab.attr("index");
    if (result.length < (currIndx + 1)) {
        nextTab.css({"background":""});
        nextTab.unbind("mouseout").mouseout(function(evt) {
            nextTab.css({"background":""});
		});
    }
}

/**
 * Tab页签打开事件
 */
var currentTab;
function onBeforeOpen(e) {
    currentTab = mini.get("#deploy_tabs").getTabByEvent(e.htmlEvent);
    if (!currentTab) {
        e.cancel = true;                
    }
}

/**
 * 刷新Tab页签数据
 */
function refreshTab() {
	mini.get("#deploy_tabs").activeTab(currentTab);
	mini.get("#deploy_tabs").reloadTab(currentTab);
}
