
//定义变量， 通常是页面控件和参数
var JsVar = new Object();

 

$(document).ready(function () {
    mini.parse();
    JsVar["datagrid1"] = mini.get("datagrid1");
    JsVar["BACKUP_HOSTS"]=mini.get("BACKUP_HOSTS");
    JsVar["keyText"] = mini.get("keyText");
    //策略配置Form对象
    JsVar["timingForm"] = new mini.Form("timingForm");
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action, data) {
	JsVar[systemVar.ACTION] = action;
	JsVar["THRESHOLD_TYPE"] = data["THRESHOLD_TYPE"];
	if(JsVar["THRESHOLD_TYPE"] == 'unexpend'){
		$('#backupHostsDesc').html('收缩节点：');
	}
	JsVar["DATA"] = data;
	onSearchClick();
     
}

/**
 * 获取Cron表达式定义配置
 */
function onButtonEdit(e) {
	 
	showDialog("选择定时扩展表达式", 1000, 600, Globals.baseJspUrl.NODE_STRATEGY_TIMING_TASK_CONFIG_URL,
	        function destroy(data){
				if(data!= undefined){
		           e.sender.setValue(data.value);
		           e.sender.setText(data.text);
				}
	    });
}

//按照HOST_IP查找
function onSearchClick(e){
	var queryKey = ""
		  if (JsVar["DATA"]["THRESHOLD_TYPE"] == "expend") {
		    	 queryKey = "expendStrategyConfig.queryExpandHostList";
		    } else if (JsVar["DATA"]["THRESHOLD_TYPE"] == "unexpend") {
		    	 queryKey = "expendStrategyConfig.queryDownHostList";
		    }
	
	 datagridLoadPage(JsVar["datagrid1"],{HOST_IP:JsVar["keyText"].value,CLUSTER_ID:JsVar["DATA"]["CLUSTER_ID"]},queryKey);
}
//关闭下拉窗口
function onCloseClick(e) {
    var HOST_ID = mini.get("BACKUP_HOSTS");
    HOST_ID.hidePopup();
}
//清除主机ID数据
function onClearClick(e) {
    var HOST_ID = mini.get("BACKUP_HOSTS");
    HOST_ID.deselectAll();    							
	keyText = mini.get("keyText");
	keyText.setValue("");
}

/**
 * 新增&修改手动配置
 */
function onSubmit() {
	addTimingConfig();
}

/**
 * 新增手动配置
 */
function addTimingConfig() {
    //判断是否有效
    JsVar["timingForm"].validate();
    if (JsVar["timingForm"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据
    var timingForm = JsVar["timingForm"].getData();
    timingForm["CLUSTER_ID"] = JsVar["DATA"]["CLUSTER_ID"];
    timingForm["TASK_PROGRAM_ID"] = JsVar["DATA"]["TASK_PROGRAM_ID"];
    timingForm["STRATEGY_ID"] = JsVar["DATA"]["STRATEGY_ID"];
    timingForm["FORECAST_REPORT_ID"] = JsVar["DATA"]["FORECAST_REPORT_ID"];
    timingForm["HOST_COUNT"] = timingForm["BACKUP_HOSTS"].split(',').length;
    timingForm["ID"] = JsVar["DATA"]["ID"];
    getJsonDataByPost(Globals.baseActionUrl.EXEC_NODE_EXPEND_JOB_URL, timingForm, "节点伸缩策略配置-新增定时策略配置",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

 


/**
 * 根据策略ID查询策略配置信息
 * @param id
 */
function loadStrategyConfig(id) {
	var params = {
		STRATEGY_ID:id
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, params, "节点伸缩策略配置-查询策略配置信息",
        function success(result){
            JsVar["timingForm"].setData(result);
            mini.get("BACKUP_HOSTS").setText(result["BACKUP_HOSTS"]);
        }, "expendStrategyConfig.queryExpendStrategyConfigById");
}