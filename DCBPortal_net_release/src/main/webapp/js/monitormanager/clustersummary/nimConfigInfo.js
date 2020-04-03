/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-23
 * Time: 上午10:40
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var DataObj=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //获取表格
    JsVar["configGrid"] = mini.get("configGrid");
    //获取前台参数
    getForwardParams();
    //加载表格信息
    loadDataGrid();
});

/**
 * 获取跳转页面get请求参数,并保存到JsVar中 
 */
function getForwardParams(){

    // var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	
	DataObj["clusterName"] = getQueryString('clusterName');
}


/**
 *	加载表格信息
 * @param params
 */
function loadDataGrid(){
	datagridLoad(JsVar["configGrid"],DataObj,"",Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_CONFIGURATION_URL);
}

/**
 * 返回
 */
function back(){
	history.back();
}

