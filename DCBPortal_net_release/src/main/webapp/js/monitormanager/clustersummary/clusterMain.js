/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["cluster_tabs"] = mini.get("cluster_tabs");
});

var currentTab = null;

function onBeforeOpen(e) {
    currentTab = JsVar["cluster_tabs"].getTabByEvent(e.htmlEvent);
    if (!currentTab) {
        e.cancel = true;
    }
}

function closeTab() {
    JsVar["cluster_tabs"].removeTab(currentTab);
}

function refReshPage() {
    mini.get("#cluster_tabs").activeTab(currentTab);
    mini.get("#cluster_tabs").reloadTab(currentTab);
}
