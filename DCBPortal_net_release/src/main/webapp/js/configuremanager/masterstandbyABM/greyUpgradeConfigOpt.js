/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-17
 * Time: 下午15:20
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
    //取得程序表格 
    JsVar["paramForm"]= new mini.Form("#paramForm");
    //本地网对象
    JsVar["latn_element"]= mini.get("latn_element");
    
});
/**
 * 父页面调用
 * @param data 
 */
function onLoadComplete(data) {
	//选择集群
    JsVar["DATA"] = data;
    JsVar["DATA"]["PROGRAM_CODE"] = data["PROGRAM_CODE_1"];
    //获取网元下拉框值
    getLatnElementData();
}

/**
 * 获取网元下拉框值
 */
function getLatnElementData(){
	var params = {
		CLUSTER_ID:JsVar["DATA"]["CLUSTER_ID"],
		CLUSTER_CODE:JsVar["DATA"]["CLUSTER_CODE"],
		//程序所属组 
		PROGRAM_GROUP:JsVar["DATA"]["PROGRAM_GROUP"],
		//业务包类型
		PACKAGE_TYPE:JsVar["DATA"]["PACKAGE_TYPE"],
		//版本切换使用配置文件
		SWITCH_CONFIG_FILE:JsVar["DATA"]["SWITCH_CONFIG_FILE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_ABM_MASTER_STANDBY_UPGRADE_LATN_VALUE_ACTION_MANAGE_URL, params, "灰度升级--查询本地网下拉框值",
		function(result){
			if(result != null && result.length>0){
				JsVar["latn_element"].setData(result);
				
				//初始化选中本地网
				var latnArray = [];
				for (var i=0; i<result.length; i++) {
					if (result[i]["isUsed"] == "1") {
						latnArray.push(result[i]["CONFIG_VALUE"]);
					}
				}
				JsVar["latn_element"].setValue(latnArray.join(","));
			} else {
				JsVar["latn_element"].setData([]);
			}
	});
}

/**
 * 提交到后台
 */
function submitUpgrade(){
	//判断是否有效
    JsVar["paramForm"].validate();
    if (JsVar["paramForm"].isValid() == false){
        return;
    }
    
    var upgradeParams = JsVar["DATA"];
    
    //获取本地网数据
	var formData = JsVar["paramForm"].getData();
	if(formData.latn_element==""){
        showWarnMessageTips("请选择本地网！");
    	return;
	}
	var latnStr = formData.latn_element;
	var latnArray = latnStr.split(",");		
	//弧度升级本地网列表
	upgradeParams["latn_element"] = latnArray;
	
	//灰度升级所属Group
	upgradeParams["PROGRAM_GROUP"] = JsVar["DATA"]["PROGRAM_GROUP"];
	
	//业务主集群ID
	upgradeParams["BUS_CLUSTER_ID"] = JsVar["DATA"]["BUS_CLUSTER_ID"];
	
	//灰度升级配置文件
	upgradeParams["SWITCH_CONFIG_FILE"] = JsVar["DATA"]["SWITCH_CONFIG_FILE"];
	
	//版本切换集群类型
	upgradeParams["SWITCH_CLUSTER_TYPE"] = JsVar["DATA"]["SWITCH_CLUSTER_TYPE"];
	
	//提交到后台处理
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_ABM_MASTER_STANDBY_OPT_UPGRADE_CONFIG_ACTION_MANAGE_URL,upgradeParams,"主备切换-灰度配置文件修改",
		function(result){
			if (result.rstCode == "0") {
                showMessageTips("修改成功！");
				closeWindow();
			}
		}
	);
}

/**
 * 关闭
 */
function close(){
	closeWindow();
}