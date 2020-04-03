/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-17
 * Time: 上午11:14
 * To change this template use File | Settings | File Templates.
 */
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
   
    //加载tab页
    loadingClusterTab();
});

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		if(result != null && result.length>0){
			var tabs=mini.get("#switchTabs");
			$.each(result, function (i, item) {
				if (item.BUS_SWITCH_PAGE_URL != null && item.BUS_SWITCH_PAGE_URL != '') {
					var tab = {
						title:item.BUS_CLUSTER_NAME,
						id:item.BUS_CLUSTER_ID,
						code:item.BUS_CLUSTER_CODE,
						config_file:item.SWITCH_CONFIG_FILE,
						cluster_type:item.SWITCH_CLUSTER_TYPE,
						dataField:item.ID, 
						showCloseButton: false,
						url:Globals.ctx + item.BUS_SWITCH_PAGE_URL
					};
					tabs.addTab(tab);
				}
            });
			//给Tabs绑定加载事件，用来传递参数
			tabs.on("tabload", function(e) {
				var item = {
					BUS_CLUSTER_ID:e.tab.id,
					BUS_CLUSTER_CODE:e.tab.code,
					BUS_CLUSTER_NAME:e.tab.title,
					SWITCH_CLUSTER_TYPE:e.tab.cluster_type,
					SWITCH_CONFIG_FILE:e.tab.config_file
				};
		        var iframe = tabs.getTabIFrameEl(e.tab);
		        iframe.contentWindow.loadPage(item);
			});
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterList");
}