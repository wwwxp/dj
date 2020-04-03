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
    //定时扩展Grid配置
    JsVar["cronConfigGrid"] = new mini.get("cronConfigGrid");
    //定时收缩Grid配置
    JsVar["uncronConfigGrid"] = new mini.get("uncronConfigGrid");
    //手工扩展Grid配置
    JsVar["unManualConfigGrid"] = new mini.get("unManualConfigGrid");
    //手工收缩Grid配置
    JsVar["manualConfigGrid"] = new mini.get("manualConfigGrid");
    loadingClusterTab();
});

function gridClear(){
	 //阀值扩展Grid配置阀
    JsVar["expendConfigGrid"].clearRows();
    //阀值收缩Grid配置
    JsVar["unexpendConfigGrid"].clearRows();
    //定时扩展Grid配置
    JsVar["cronConfigGrid"].clearRows();
    //定时收缩Grid配置
    JsVar["uncronConfigGrid"].clearRows();
    //手工扩展Grid配置
    JsVar["unManualConfigGrid"].clearRows();
    //手工收缩Grid配置
    JsVar["manualConfigGrid"].clearRows();
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
	if(type == "4"){
		 return e.value+"M";
	}else{
		return e.value+"%";
	}
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
		//扩展查询
		loadExpendThresholdConfig(params);
		
		//收缩查询
		loadUnexpendThresholdConfig(params);
		
	    //定时扩展查询
		loadExpendTimingConfig(params);
		//定时收缩查询
		loadUnexpendTimingConfig(params);
		
		//手动扩展查询
		loadExpendManualConfig(params);
		//手动收缩查询
		loadUnexpendManualConfig(params);
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
	params["OPERATOR_TYPE"] = "1";
	datagridLoad(JsVar["expendConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载收缩配置
 * @param params
 */
function loadUnexpendThresholdConfig(params) {
	params["OPERATOR_TYPE"] = "2";
	datagridLoad(JsVar["unexpendConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载扩展配置
 */
function loadExpendTimingConfig(params) {
	params["OPERATOR_TYPE"] = "3";
	datagridLoad(JsVar["cronConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
    var STATE = e.record.STATE;
    var html = "";
    if (STATE == 0 || STATE == null) {//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;无效&nbsp;</span>";
    } else if (STATE == 1) {//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;有效&nbsp;</span>";
    }
}

/**
 * 加载收缩配置
 * @param params
 */
function loadUnexpendTimingConfig(params) {
	params["OPERATOR_TYPE"] = "4";
	datagridLoad(JsVar["uncronConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载手动扩展配置
 */
function loadExpendManualConfig(params) {
	params["OPERATOR_TYPE"] = "5";
	datagridLoad(JsVar["manualConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
}

/**
 * 加载手动收缩配置
 * @param params
 */
function loadUnexpendManualConfig(params) {
	params["OPERATOR_TYPE"] = "6";
	datagridLoad(JsVar["unManualConfigGrid"], params, "expendStrategyConfig.queryTimingConfigList");
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
	var quotaTypeList = getSysDictData("quota_type");
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
	showAddDialog("新增阀值" + typeName + "策略配置", 400, 320, Globals.baseJspUrl.NODE_STRATEGY_THRESHOLD_CONFIG_URL,
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
	showEditDialog("修改阀值" + typeName + "策略配置", 600, 300, Globals.baseJspUrl.NODE_STRATEGY_THRESHOLD_CONFIG_URL,
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
        		showMessageTips("删除阀值"+typeName+"策略成功！");
            });
    });
}

/**
 * 定时扩展渲染
 * @param e
 */
function onCronExpendRenderer(e) {
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateTimingConfig(\''+id+'\', \'expend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delTimingConfig(\'' + id + '\', \'expend\')">删除</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:queryLog(\'' + id + '\', \'expend\')">日志查看</a>';
	return retStr;
}


/**
 * 定时收缩渲染
 * @param e
 */
function onUncronExpendRenderer(e) {
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateTimingConfig(\''+id+'\', \'unexpend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delTimingConfig(\'' + id + '\', \'unexpend\')">删除</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:queryLog(\'' + id + '\', \'expend\')">日志查看</a>';
	return retStr;
}
/**
 * 手动扩展渲染
 * @param e
 */
function onManualExpendRenderer(e){
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateManualConfig(\''+id+'\', \'expend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delManualConfig(\'' + id + '\', \'expend\')">删除</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:execManual(\'' + id + '\', \'expend\')">手动执行</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:queryLog(\'' + id + '\', \'expend\')">日志查看</a>';
	return retStr;
}

/**
 * 手动收缩渲染
 * @param e
 */
function onUnManualExpendRenderer(e){
	var id = e.record.STRATEGY_ID;
	var retStr = '<a class="Delete_Button" href="javascript:updateManualConfig(\''+id+'\', \'unexpend\')">编辑</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:delManualConfig(\'' + id + '\', \'unexpend\')">删除</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:execManual(\'' + id + '\', \'unexpend\')">手动执行</a>';
	   retStr += '<a class="Delete_Button"  href="javascript:queryLog(\'' + id + '\', \'unexpend\')">日志查看</a>';
	return retStr;
}
/**
 * 日志查看
 */
/**
 * 查看日志
 * @param index
 */
function queryLog(id) {
	var selNode = mini.get("strategyTree").getSelectedNode();
	selNode["STRATEGY_ID"] = id;
	showDialog("日志查看",1000,550,Globals.baseJspUrl.NODE_STRATEGY_TIMING_LOG_URL,
	        function destroy(data){
				 
	    },selNode);
}
/**
 * 手工执行
 */
function execManual(id, type){
	 
	var selNode = mini.get("strategyTree").getSelectedNode();
	if (!selNode || selNode["NODE_LEVEL"] != '3') {
        showWarnMessageTips("请选择需要配置策略的业务程序！");
		return;
	}
	var OPERATOR_TYPE = 5;
	var typeName = "扩展";
	if (type == "unexpend") {
		typeName = "收缩";
		OPERATOR_TYPE = 6;
	}
	 var params = {
 			OPERATOR_TYPE:OPERATOR_TYPE,
 			STRATEGY_ID:id,
 			BUS_ID:id,
 			CLUSTER_ID:selNode["CLUSTER_ID"],
 			TASK_PROGRAM_ID:selNode["TASK_PROGRAM_ID"],
 			THRESHOLD_TYPE:type 
 		};
	
	 getJsonDataByPost(Globals.baseActionUrl.MANUAL_CONFIG_QUERY_RULE_URL, params, "节点伸缩策略配置-查询规则信息",
	            function(result){
		 			var message= "";
		 			if(OPERATOR_TYPE == '5'){
		 				  var isFlag = result["isTrigger"];
			                if(!isFlag){
			                	 message = "主机资源比较空闲，无需扩展，请问是否确认要扩展？"
			                }else{
			                	 message = result["msg"] +"主机资源已超过阀值，请问是否确认要扩展？"
			                }
			                if(result["msg"] == 'error'){
			                	message="主机资源信息获取失败，请问是否确认要扩展？"
			                }
		 			}else{
		 				 var isFlag = result["isTrigger"];
			                if(isFlag){
			                	 message = result["msg"] +"主机资源空闲，请问是否确认要收缩节点？"
			                }else{
			                	
			                	 message = "系统资源不足，不适宜做收缩，是否确认继续收缩节点？"
			                }
			                if(result["msg"] == 'error'){
			                	message="主机资源信息获取失败，请问是否确认要扩展？"
			                }
		 			}
		 			
	              
	            	
	            	showConfirmMessageAlter(message,function ok(){
	                    getJsonDataByPost(Globals.baseActionUrl.MANUAL_CONFIG_EXEC_URL, params, "节点伸缩策略配置-删除定时策略配置",
	                        function(result){
	            	        	if (type == "expend") {
	            	    			JsVar["cronConfigGrid"].reload();
	            	    		} else if (type == "unexpend") {
	            	    			JsVar["uncronConfigGrid"].reload();
	            	    		}
                                showMessageTips("手动执行成功！");
	                        });
	                });
	            	
	            });
	  
	
	 
	
}
/**
 * 新增定时伸缩配置
 * @param type
 */
function addTimingConfig(type) {
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
	showAddDialog("新增定时" + typeName + "策略配置", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_TIMING_CONFIG_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	if (type == "expend") {
	        			JsVar["cronConfigGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["uncronConfigGrid"].reload();
	        		}
	                //showMessageAlter("新增定时" + typeName + "策略成功！");
	            	showMessageTips("新增定时" + typeName + "策略成功！");
	            }
	    }, params);
}

/**
 * 新增手工伸缩配置
 * @param type
 */
function addManualConfig(type) {
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
	showAddDialog("新增定时" + typeName + "策略配置", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_MANUAL_CONFIG_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	if (type == "expend") {
	        			JsVar["manualConfigGrid"].reload();
	        		} else if (type == "unexpend") {
	        			JsVar["unManualConfigGrid"].reload();
	        		}
	                //showMessageAlter("新增手动" + typeName + "策略成功！");
	            	showMessageTips("新增手动" + typeName + "策略成功！");
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
