
//定义变量， 通常是页面控件和参数
var JsVar = new Object();

$(document).ready(function () {
    mini.parse();
    //策略配置Form对象
    JsVar["thresholdForm"] = new mini.Form("thresholdForm");
    
});

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action, data) {
	JsVar[systemVar.ACTION] = action;
	JsVar["DATA"] = data;
	JsVar["THRESHOLD_TYPE"] = data["THRESHOLD_TYPE"];
	if(JsVar["THRESHOLD_TYPE"] == 'unexpend'){
		//$('#"hostCountDesc"').html('一次性收缩节点数：');
		//$('#"backupHostsDesc"').html('收缩节点：');
	}
	if (data["THRESHOLD_TYPE"] == "expend") {
		mini.get("CONDITION_PARAM").setData(getSysDictData("condition_param_expend_dy"));
	} else if (data["THRESHOLD_TYPE"] == "unexpend") {
		mini.get("CONDITION_PARAM").setData(getSysDictData("condition_param_unexpend_dy"));
	}
	
    if (action == systemVar.EDIT) {
    	loadStrategyConfig(data["STRATEGY_ID"]);
    } else if (action == systemVar.ADD) {
    	
    }
}

/**
 * 新增&修改策略配置
 */
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        updateThresholdConfig();
    } else {
    	addThresholdConfig();
    }
}

/**
 * 新增策略配置
 */
function addThresholdConfig() {
    //判断是否有效
    JsVar["thresholdForm"].validate();
    if (JsVar["thresholdForm"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据
    var thresholdForm = JsVar["thresholdForm"].getData();
    thresholdForm["CLUSTER_ID"] = JsVar["DATA"]["CLUSTER_ID"];
    thresholdForm["TASK_PROGRAM_ID"] = JsVar["DATA"]["TASK_PROGRAM_ID"];
    if (JsVar["DATA"]["THRESHOLD_TYPE"] == "expend") {
    	thresholdForm["OPERATOR_TYPE"] = "7";
    } else if (JsVar["DATA"]["THRESHOLD_TYPE"] == "unexpend") {
    	thresholdForm["OPERATOR_TYPE"] = "8";
    }
    thresholdForm["STATE"] = '1';
    getJsonDataByPost(Globals.baseActionUrl.THRESHOLD_CONFIG_ADD_URL, thresholdForm, "节点伸缩策略配置-新增阀值策略配置",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//修改用户
function updateThresholdConfig() {
    //判断是否有效
    JsVar["thresholdForm"].validate();
    if (JsVar["thresholdForm"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var thresholdForm = JsVar["thresholdForm"].getData();
    thresholdForm["CLUSTER_ID"] = JsVar["DATA"]["CLUSTER_ID"];
    thresholdForm["STRATEGY_ID"] = JsVar["DATA"]["STRATEGY_ID"];
    thresholdForm["TASK_PROGRAM_ID"] = JsVar["DATA"]["TASK_PROGRAM_ID"];
    if (JsVar["DATA"]["THRESHOLD_TYPE"] == "expend") {
    	thresholdForm["OPERATOR_TYPE"] = "7";
    } else if (JsVar["DATA"]["THRESHOLD_TYPE"] == "unexpend") {
    	thresholdForm["OPERATOR_TYPE"] = "8";
    }
    getJsonDataByPost(Globals.baseActionUrl.THRESHOLD_CONFIG_UPDATE_URL,thresholdForm,"节点伸缩策略配置-修改阀值策略配置",
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
            JsVar["thresholdForm"].setData(result);
            onqtaTypeChange({value:result["QUOTA_TYPE"]})
        }, "expendStrategyConfig.queryExpendStrategyConfigById");
}


/**
 * 当选择类型时候变化说明
 * @param e
 */
function onqtaTypeChange(e){
	var type = e.value;
	if(type == "4"){
		 $("#SPAN_TIP").text("M");
	}else if(type == "5"){
		 $("#SPAN_TIP").text("条");
	}else{
		$("#SPAN_TIP").text("%");
	}
}