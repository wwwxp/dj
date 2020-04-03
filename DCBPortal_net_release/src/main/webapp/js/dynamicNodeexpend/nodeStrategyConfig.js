/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () { 
    mini.parse();
    
    //阀值扩展Grid配置阀
    JsVar["expendConfigGrid"] = new mini.get("expendConfigGrid");
    //阀值收缩Grid配置
    JsVar["unexpendConfigGrid"] = new mini.get("unexpendConfigGrid");
    //预测扩展Grid配置
    JsVar["expandGrid"] = new mini.get("expandGrid");
    //预测收缩Grid配置
    JsVar["unExpandGrid"] = new mini.get("unExpandGrid");
    //手工扩展Grid配置
    JsVar["unReportGrid"] = new mini.get("unReportGrid");
    //手工收缩Grid配置
    JsVar["reportGrid"] = new mini.get("reportGrid");
    loadingClusterTab();
    //定时查询
    timerLoad();
});

function gridClear(){
	 //阀值扩展Grid配置阀
    JsVar["expendConfigGrid"].clearRows();
    JsVar["unexpendConfigGrid"].clearRows();
    //预测扩展Grid配置
    JsVar["expandGrid"].clearRows();
    //预测收缩Grid配置
    JsVar["unExpandGrid"].clearRows();
    //手工扩展Grid配置
    JsVar["unReportGrid"].clearRows();
    //手工收缩Grid配置
    JsVar["reportGrid"].clearRows();
}
/**
 * 获取当前tab页的id
 * @param e
 */
function loadPage(e){
	//业务主集群
	var busClusterId = e.tab.id;
	JsVar["BUS_CLUSTER_ID"] = busClusterId;
	JsVar["BUS_CLUSTER_NAME"] = e.tab.title;
	 
	//加载树节点数据
    getTreeData();
    gridClear();
}

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		var tab_str="";
		if(result.length>0){
			var tabs=mini.get("#cluster_tabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.ID, 
					showCloseButton: false
				};
				tabs.addTab(tab);
            });
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterList");
}





function timerLoad(){
	 
	 window.setInterval(function(){
		 var selNode = mini.get("strategyTree").getSelectedNode();
			if (!selNode || selNode["NODE_LEVEL"] != '3') {
				return;
			}
			
			JsVar["expandGrid"].reload();
			JsVar["unExpandGrid"].reload();
			JsVar["unReportGrid"].reload();
			JsVar["reportGrid"].reload();
	 },8000);
}

/**
 * 加载实例状态目录
 * @returns
 */
function getTreeData() {
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	treeLoad(mini.get("strategyTree"), null, params, Globals.baseActionUrl.NODE_EXPEND_STRATEGY_CONFIG_TREE_QUERY_URL);
}

/**
 * 刷新文件目录树
 */
function refresh(){
	mini.get("queryNodeName").setValue("");
	var params = {
        BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"]
	};
	treeLoad(mini.get("strategyTree"), null, params, Globals.baseActionUrl.NODE_EXPEND_STRATEGY_CONFIG_TREE_QUERY_URL);
}

/**
 * 搜索树节点
 */
function searchTree() {
    var nodeNameStr = mini.get("queryNodeName").getValue();
    if (nodeNameStr == "") {
    	mini.get("strategyTree").clearFilter();
    }else {
    	nodeNameStr = nodeNameStr.toLowerCase();
    	mini.get("strategyTree").filter(function (node) {
            var text = node["NODE_NAME"] ? node["NODE_NAME"].toLowerCase() : "";
            if (text.indexOf(nodeNameStr) != -1) {
                return true;
            }
        });
    }
}

function onUnitRenderer(e){
	 
	var type = e.record.QUOTA_TYPE;
	if(type == "5"){
		 return e.value+"条";
	}else{
		return e.value+"%";
	}
}
function onUnitfieldRenderer(e){

	var field = e.field;
	if(field == "BUSS_VOLUME"){
		 return e.value;
	}else{
		return e.value+"%";
	}
}
function onActionfieldRenderer(e){
	var type = e.value;
	if(type == 1){
		return "手动";
	}else{
		return "定时";
	}
}

/**
 * 终端操作
 * @param e
 */
function onRenderReportDesc(e) {
	var retDesc = e.record.RESULT_DESC;
    return '<a class="Delete_Button" href="javascript:showDesc(\'' + retDesc + '\')">' + retDesc + '</a>';
}

/**
 * 展示信息
 * @param retDesc
 */
function showDesc(retDesc) {
	var paramsHtml="<div style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+retDesc+"</div>";
	var options={
	    title: "报告详情",    
	    width:"800",
	    height:"600",
	    buttons: ["ok"],    
	    iconCls: "",
	    html: paramsHtml,   
	    callback: function(action){
	    	
	    }
	};
	mini.showMessageBox(options);
}

/**
 * 节点点击事件
 * @param e
 */
function onClickTreeNode(e) {
	var currNode = e.node;
	var nodeLevel = e.node.NODE_LEVEL;
	var params = {};
	if (nodeLevel == "1") {  //查询所有集群类型
		//params["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
	} else if (nodeLevel == "2") {  //查询所有集群类型下对应集群
		//params["DEPLOY_TYPE"] = currNode["CLUSTER_TYPE"];
		//params["CLUSTER_ID"] = currNode["CLUSTER_ID"];
	} else if (nodeLevel == "3") {
		var params = {
			CLUSTER_ID:currNode.CLUSTER_ID,
			TASK_PROGRAM_ID:currNode.TASK_PROGRAM_ID,
		};
		//扩展阀值查询
		loadExpendThresholdConfig(params);
		
		//收缩阀值查询
		loadUnexpendThresholdConfig(params);
		
	    //预测扩展查询
		loadExpendGrid(params);
		//预测收缩查询
		loadUnExpendGrid(params);
		
		//手动扩展查询
		loadExpendReportGrid(params);
		//手动收缩查询
		loadUnExpendReportGrid(params);
	}
}

/**
 * 图表重新渲染事件
 * @param e
 */
function nodeRender(e) {
	var level = e.node.NODE_LEVEL;
	if (level == "1") {
		e.iconCls = "tree-node-cluster";
	} else if (level == "2") {
		e.iconCls = "tree-node-version";
	} else if (level == "3") {
		e.iconCls = "tree-node-program";
	}
}



/**
 * 加载扩展配置
 */
function loadExpendThresholdConfig(params) {
	params["OPERATOR_TYPE"] = "7";
	datagridLoad(JsVar["expendConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载收缩配置
 * @param params
 */
function loadUnexpendThresholdConfig(params) {
	params["OPERATOR_TYPE"] = "8";
	datagridLoad(JsVar["unexpendConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载扩展配置
 */
function loadExpendGrid(params) {
	params["OPERATOR_TYPE"] = "7";
	datagridLoad(JsVar["expandGrid"], params, "forecastReport.queryforcastReportList");
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
    var STATE = e.record.STATUS;
    var html = "";
    if (STATE == 0 || STATE == null) {//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;未触发&nbsp;</span>";
    } else if (STATE == 1) {//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;触发中&nbsp;</span>";
    }else{
    	 return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;已触发&nbsp;</span>";
    }
}

function execstatusRenderer(e){
	var EXEC_STATUS = e.record.EXEC_STATUS;
    var html = "";
    if (EXEC_STATUS == 0 || EXEC_STATUS == null) {//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;未执行&nbsp;</span>";
    } else if (EXEC_STATUS == 1) {//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;执行中&nbsp;</span>";
    }else if(EXEC_STATUS == 2){
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;已执行&nbsp;</span>";

    }else{
    	return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;执行失败&nbsp;</span>";
    }
}

/**
 * 加载收缩配置
 * @param params
 */
function loadUnExpendGrid(params) {
	params["OPERATOR_TYPE"] = "8";
	datagridLoad(JsVar["unExpandGrid"], params, "forecastReport.queryforcastReportList");
}

/**
 * 加载手动扩展配置
 */
function loadExpendReportGrid(params) {
	params["OPERATOR_TYPE"] = "7";
	datagridLoad(JsVar["reportGrid"], params, "expansionReport.queryExpansionReportList");
}

/**
 * 加载手动收缩配置
 * @param params
 */
function loadUnExpendReportGrid(params) {
	params["OPERATOR_TYPE"] = "8";
	datagridLoad(JsVar["unReportGrid"], params, "expansionReport.queryExpansionReportList");
}

/**
 * 修改扩展策略
 * @param e
 */
function onExpendActionRenderer(e) {
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateThresholdConfig(\''+id+'\', \'expend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delThresholdConfig(\'' + id + '\', \'expend\')">删除</a>';
	   return retStr;
}

/**
 * 修改扩展策略
 * @param e
 */
function onUnexpendActionRenderer(e) {
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateThresholdConfig(\''+id+'\', \'unexpend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delThresholdConfig(\'' + id + '\', \'unexpend\')">删除</a>';
	   return retStr;
}

/**
 * 指标类型渲染
 * @param e
 */
function onQuotaTypeRenderer(e) {
	var quotaTypeStr = "";
	var quotaType = e.record.QUOTA_TYPE;
	var quotaTypeList = getSysDictData("quota_dy_type");
	for (var i=0; i<quotaTypeList.length; i++) {
		if (quotaTypeList[i]["code"] == quotaType) {
			quotaTypeStr = quotaTypeList[i]["text"];
			break;
		}
	}
	return quotaTypeStr;
}

/**
 * 条件类型渲染
 * @param e
 */
function onConditionParamsRenderer(e) {
	var conditionStr = "";
	var conditionParams = e.record.CONDITION_PARAM;
	var conditionList = getSysDictData("condition_param_expend");
	for (var i=0; i<conditionList.length; i++) {
		if (conditionList[i]["code"] == conditionParams) {
			conditionStr = conditionList[i]["text"];
			break;
		}
	}
	return conditionStr;
}

/**
 * 条件类型渲染
 * @param e
 */
function onUnConditionParamsRenderer(e) {
	var conditionStr = "";
	var conditionParams = e.record.CONDITION_PARAM;
	var conditionList = getSysDictData("condition_param_unexpend");
	for (var i=0; i<conditionList.length; i++) {
		if (conditionList[i]["code"] == conditionParams) {
			conditionStr = conditionList[i]["text"];
			break;
		}
	}
	return conditionStr;
}

/**
 * 新增策略配置
 */
function addThresholdConfig(type) {
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		OPERTOR_TYPE:systemVar.ADD,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		THRESHOLD_TYPE:type
	};
	showAddDialog("新增阀值" + typeName + "策略配置", 500, 320, Globals.baseJspUrl.NODE_STRATEGY_THRESHOLD_DY_CONFIG_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	if (type == "expend") {
	        			JsVar["expendConfigGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["unexpendConfigGrid"].reload();
	        		}
	               // showMessageAlter("新增阀值" + typeName + "策略成功！");
	            	showMessageTips("新增阀值" + typeName + "策略成功！");
	            }
	    }, params);
}

/**
 * 修改策略配置
 */
function updateThresholdConfig(id, type) {
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		OPERTOR_TYPE:systemVar.EDIT,
		STRATEGY_ID:id,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		THRESHOLD_TYPE:type
	};
	showEditDialog("修改阀值" + typeName + "策略配置", 500, 320, Globals.baseJspUrl.NODE_STRATEGY_THRESHOLD_DY_CONFIG_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
            	if (type == "expend") {
        			JsVar["expendConfigGrid"].reload();
        		} else if (type == "unexpend") {
        			JsVar["unexpendConfigGrid"].reload();
        		}
                //showMessageAlter("修改阀值" + typeName + "策略成功！");
            	showMessageTips("修改阀值" + typeName + "策略成功！");
            }
    }, params);
}

/**
 * 删除策略配置
 */
function delThresholdConfig(id, type) {
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		STRATEGY_ID:id
	};
	showConfirmMessageAlter("确定删除记录？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.THRESHOLD_CONFIG_DEL_URL, params, "节点伸缩策略配置-删除阀值策略配置",
            function(result){
        		if (type == "expend") {
        			JsVar["expendConfigGrid"].reload();
        		} else if (type == "unexpend") {
        			JsVar["unexpendConfigGrid"].reload();
        		}
                //showMessageAlter("删除阀值"+typeName+"策略成功！");
        		showMessageTips("删除"+typeName+"策略成功！");
            });
    });
}

/**
 * 定时扩展渲染
 * @param e
 */
function onExpendRenderer(e) {
	var id = e.record.ID;
	var status = e.record.status;
	var index = e.rowIndex;
	var retStr="";
	/*if(status == 1){
		 retStr = '<a class="Delete_Button" href="javascript:updateStatus(\''+id+'\', \'expend\', \'0\')">失效</a>'; 
	}else{
		retStr = '<a class="Delete_Button" href="javascript:updateStatus(\''+id+'\', \'expend\', \'1\')">有效</a>'; 
	}*/
   retStr += '<a class="Delete_Button"  href="javascript:queryReportK(' + index + ', \'expend\')">查看报告</a>';
	return retStr;
}


/**
 * 定时收缩渲染
 * @param e
 */
function onUnExpendRenderer(e) {
	var index = e.rowIndex;
	var id = e.record.ID;
	var status = e.record.status;
	var retStr="";
	/*if(status == 1){
		 retStr = '<a class="Delete_Button" href="javascript:updateStatus(\''+id+'\', \'unexpend\', \'0\')">失效</a>'; 
	}else{
		retStr = '<a class="Delete_Button" href="javascript:updateStatus(\''+id+'\', \'unexpend\', \'1\')">有效</a>'; 
	}*/
   retStr += '<a class="Delete_Button"  href="javascript:queryReportS(' + index + ', \'unexpend\')">查看报告</a>';
	return retStr;
}
function updateStatus(id,type,state){
	var params = {
			ID:id,
			STATUS:state
		};
		showConfirmMessageAlter("确定此操作吗？",function ok(){
	        getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL, params, "动态伸缩策略报告-修改状态",
	            function(result){
	        		if (type == "expend") {
	        			JsVar["expandGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["unExpandGrid"].reload();
	        		}
	                //showMessageAlter("删除阀值"+typeName+"策略成功！");
	        		showMessageTips("修改成功！");
	            },"forecastReport.updateStatusByID");
	    });
}
/**
 * 手动扩展渲染
 * @param e
 */
function onManualExpendRenderer(e){
	var index = e.rowIndex;
	var exec_status = e.record.EXEC_STATUS;
	var retStr = '<a class="Delete_Button" href="javascript:queryExpansioinReportK('+index+', \'expend\')">查看报告</a>';
	if(exec_status == 0){
		retStr += '<a class="Delete_Button"  href="javascript:execManualJobKS(' + index + ', \'expend\')">立即执行</a>';
		   retStr += '<a class="Delete_Button"  href="javascript:execJobKS(' + index + ', \'expend\')">定时执行</a>';
	}else {
		if(exec_status == 3){
			retStr += '<a class="Delete_Button"  href="javascript:execManualJobKS(' + index + ', \'expend\')">手工执行</a>';
		}
		retStr += '<a class="Delete_Button"  href="javascript:queryLog(' + index + ', \'expend\')">日志查看</a>';

	}
	
	   
	return retStr;
}

/**
 * 手动收缩渲染
 * @param e 
 */
function onUnManualExpendRenderer(e){
	var index = e.rowIndex;
	var exec_status = e.record.EXEC_STATUS;
	var retStr = '<a class="Delete_Button" href="javascript:queryExpansioinReportS('+index+', \'unexpend\')">查看报告</a>';
	if(exec_status == 0){  
	      retStr += '<a class="Delete_Button"  href="javascript:execManualJobKS(' + index + ', \'unexpend\')">立即执行</a>';
	      retStr += '<a class="Delete_Button"  href="javascript:execJobKS(' + index + ', \'unexpend\')">定时执行</a>';
	}else{
		if(exec_status == 3){
			retStr += '<a class="Delete_Button"  href="javascript:execManualJobKS(' + index + ', \'unexpend\')">手工执行</a>';
		}
		  retStr += '<a class="Delete_Button"  href="javascript:queryLog(' + index + ', \'unexpend\')">日志查看</a>';

	}
	return retStr;
}

function queryExpansioinReportS(index,type){
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var row = JsVar["unReportGrid"].getRow(index);
	row["CLUSTER_ID"]=selNode["CLUSTER_ID"];
	row["TASK_PROGRAM_ID"]=selNode["TASK_PROGRAM_ID"];
	row["OPERATOR_TYPE"]="8";
	row["index"] = index;
	row["OPERATOR_TYPE_DESC"] = type;
	showDialog("收缩报告",450,500,Globals.baseJspUrl.NODE_STRATEGY_UNEXPANSION_REPORT_URL,
	        function destroy(result){
				 
	    },row);
}

function queryExpansioinReportK(index,type){
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var row = JsVar["reportGrid"].getRow(index);
	row["CLUSTER_ID"]=selNode["CLUSTER_ID"];
	row["TASK_PROGRAM_ID"]=selNode["TASK_PROGRAM_ID"];
	row["OPERATOR_TYPE"]="7";
	row["index"] = index;
	row["OPERATOR_TYPE_DESC"] = type;
	showDialog("扩容报告",450,500,Globals.baseJspUrl.NODE_STRATEGY_EXPANSION_REPORT_URL,
	        function destroy(result){
				 
	    },row);
}

function queryReportK(index,type){
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var row = JsVar["expandGrid"].getRow(index);
	row["CLUSTER_ID"]=selNode["CLUSTER_ID"];
	row["TASK_PROGRAM_ID"]=selNode["TASK_PROGRAM_ID"];
	row["OPERATOR_TYPE"]="7";
	showDialog("预测扩容报告",500,500,Globals.baseJspUrl.NODE_STRATEGY_FORECAST_REPORT_URL,
	        function destroy(result){
				 
	    },row);
}

function queryReportS(index,type){
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var row = JsVar["unExpandGrid"].getRow(index);
	row["CLUSTER_ID"]=selNode["CLUSTER_ID"];
	row["TASK_PROGRAM_ID"]=selNode["TASK_PROGRAM_ID"];
	row["OPERATOR_TYPE"]="8";
	showDialog("预测收缩报告",500,500,Globals.baseJspUrl.NODE_STRATEGY_UNFORECAST_REPORT_URL,
	        function destroy(result){
				 
	    },row);
}
/**
 * 日志查看
 */
/** 
 * 查看日志
 * @param index
 */
function queryLog(index,type) {
	var row =JsVar["reportGrid"].getRow(index);
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
		row =JsVar["unReportGrid"].getRow(index);
	}
	var selNode = mini.get("strategyTree").getSelectedNode();
	selNode["STRATEGY_ID"] = row["STRATEGY_ID"];
	showDialog("日志查看",1000,550,Globals.baseJspUrl.NODE_STRATEGY_DYNMAIC_LOG_URL,
	        function destroy(data){
				 
	    },selNode);
}
 
/**
 * 新增定时伸缩配置
 * @param type
 */
function execManualJobKS(index,type) {
	var row =JsVar["reportGrid"].getRow(index);
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
		row =JsVar["unReportGrid"].getRow(index);
	}
	var params = {
		OPERTOR_TYPE:systemVar.ADD,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		STRATEGY_ID:row["STRATEGY_ID"],
		THRESHOLD_TYPE:type,
		ID:row["ID"],
		FORECAST_REPORT_ID:row["FORECAST_REPORT_ID"]
	};
	showAddDialog("立刻" + typeName + "吗", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_MANUAL_DYNAMIC_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	if (type == "expend") {
	        			JsVar["reportGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["unReportGrid"].reload();
	        		}
	                //showMessageAlter("新增定时" + typeName + "策略成功！");
	            	showMessageTips("新增定时" + typeName + "策略成功！");
	            }
	    }, params);
}
function execJobKS(index,type){
	var row =JsVar["reportGrid"].getRow(index);
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
		row =JsVar["unReportGrid"].getRow(index);
	}
	var params = {
		OPERTOR_TYPE:systemVar.ADD,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		STRATEGY_ID:row["STRATEGY_ID"],
		THRESHOLD_TYPE:type,
		ID:row["ID"],
		FORECAST_REPORT_ID:row["FORECAST_REPORT_ID"]
	};
	showAddDialog("定时" + typeName + "吗", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_TIMING_DYNAMIC_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	if (type == "expend") {
	        			JsVar["reportGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["unReportGrid"].reload();
	        		}
	                //showMessageAlter("新增定时" + typeName + "策略成功！");
	            	showMessageTips("新增定时" + typeName + "策略成功！");
	            }
	    }, params);
}
 

/**
 * 修改定时策略配置
 */
function updateTimingConfig(id, type) {
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		OPERTOR_TYPE:systemVar.EDIT,
		STRATEGY_ID:id,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		THRESHOLD_TYPE:type 
	};
	showEditDialog("修改定时" + typeName + "策略配置", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_TIMING_CONFIG_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
            	if (type == "expend") {
        			JsVar["cronConfigGrid"].reload();
        		} else if (type == "unexpend") {
        			JsVar["uncronConfigGrid"].reload();
        		}
                //showMessageAlter("修改定时" + typeName + "策略成功！");
            	showMessageTips("修改定时" + typeName + "策略成功！");
            }
    }, params);
}

/**
 * 修改手动策略配置
 */
function updateManualConfig(id, type) {
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		OPERTOR_TYPE:systemVar.EDIT,
		STRATEGY_ID:id,
		CLUSTER_ID:selNode["CLUSTER_ID"],
		TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
		THRESHOLD_TYPE:type 
	};
	showEditDialog("修改手动" + typeName + "策略配置", 700, 460, Globals.baseJspUrl.NODE_STRATEGY_MANUAL_CONFIG_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
            	if (type == "expend") {
        			JsVar["manualConfigGrid"].reload();
        		} else if (type == "unexpend") {
        			JsVar["unManualConfigGrid"].reload();
        		}
                //showMessageAlter("修改手动" + typeName + "策略成功！");
            	showMessageTips("修改手动" + typeName + "策略成功！");
            }
    }, params);
}

/**
 * 删除手动策略配置
 */
function delManualConfig(id, type) {
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		STRATEGY_ID:id
	};
	showConfirmMessageAlter("确定删除记录？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.THRESHOLD_CONFIG_DEL_URL, params, "节点伸缩策略配置-删除定时策略配置",
            function(result){
	        	if (type == "expend") {
	    			JsVar["manualConfigGrid"].reload();
	    		} else if (type == "unexpend") {
	    			JsVar["unManualConfigGrid"].reload();
	    		}
                //showMessageAlter("删除手动"+typeName+"策略成功！");
	        	showMessageTips("删除手动"+typeName+"策略成功！");
            });
    });
}


/**
 * 删除定时策略配置
 */
function delTimingConfig(id, type) {
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
	}
	var params = {
		STRATEGY_ID:id
	};
	showConfirmMessageAlter("确定删除记录？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.THRESHOLD_CONFIG_DEL_URL, params, "节点伸缩策略配置-删除定时策略配置",
            function(result){
	        	if (type == "expend") {
	    			JsVar["cronConfigGrid"].reload();
	    		} else if (type == "unexpend") {
	    			JsVar["uncronConfigGrid"].reload();
	    		}
                //showMessageAlter("删除定时"+typeName+"策略成功！");
	        	showMessageTips("删除定时"+typeName+"策略成功！");
            });
    });
}
 
