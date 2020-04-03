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
	var params = {
		GROUP_CODE:"WEB_BUS_PACKAGE_TYPE"
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务配置文件-查询业务配置文件划分",
        function(result){
		if(result != null && result.length>0){
			var tabs=mini.get("#deploy_tabs");
			$.each(result, function (i, item) {
				if (item.CONFIG_VALUE != null && item.CONFIG_VALUE != '') {
					var tab = {
						title:item.CONFIG_NAME,
						id:item.CONFIG_VALUE,
						dataField:item.CONFIG_VALUE, 
						showCloseButton: false,
						url:Globals.ctx + "/jsp/configuremanager/editconfig/businessconfigure"
					};
					tabs.addTab(tab);
				}
            });
			//给Tabs绑定加载事件，用来传递参数
			tabs.on("tabload", function(e) {
				var item = {
					CONFIG_VALUE:e.tab.id,
					CONFIG_NAME:e.tab.title
				};
		        var iframe = tabs.getTabIFrameEl(e.tab);
		        iframe.contentWindow.loadPage(item);
			});
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
			tabs.reloadTab(tabs.getActiveTab());
		}
    },"config.queryConfigList");
}