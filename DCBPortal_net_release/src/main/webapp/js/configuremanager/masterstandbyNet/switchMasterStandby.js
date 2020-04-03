/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
    mini.parse();
});

/**
 * tab页切换函数：根据集群id加载对应表格
 * @param item
 */
function loadPage(item){
	JsVar["BUS_CLUSTER_ID"] = item["BUS_CLUSTER_ID"];
	JsVar["BUS_CLUSTER_CODE"] = item["BUS_CLUSTER_CODE"];
	JsVar["BUS_CLUSTER_NAME"] = item["BUS_CLUSTER_NAME"];
	//该版本切换使用的配置文件
	JsVar["SWITCH_CONFIG_FILE"] = item["SWITCH_CONFIG_FILE"];
	//该版本切换对应的
	JsVar["SWITCH_CLUSTER_TYPE"] = item["SWITCH_CLUSTER_TYPE"];
}

