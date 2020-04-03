/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //任务积压Grid对象
    JsVar["overstockGrid"] = mini.get("overstockGrid");
    //取得查询表单
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");
    //过滤Form对象
    JsVar["updateForm"] = new mini.Form("#updateForm");
    //加载ZK集群
    loadZookeeperList();
});

/**
 * 加载ZK集群
 */
function loadZookeeperList() {
	var params = {};
    getJsonDataByPost(Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_ZK_QUERY_URL, params, "任务积压查询-查询zookeeper列表",
	function(result){
		if (result != null && result.length > 0) {
            mini.get("CLUSTER_ID").setData(result);
            mini.get("CLUSTER_CHARTS_ID").setData(result);
		}
	});

	// var params = {
	// 	CLUSTER_TYPE:busVar.ZOOKEEPER
	// };
	// comboxLoad(mini.get("CLUSTER_ID"), params, "serviceType.queryServiceTypeList",null, null, false);
	// comboxLoad(mini.get("CLUSTER_CHARTS_ID"), params, "serviceType.queryServiceTypeList",null, null, false);

	//mini.get("CLUSTER_ID").setData([{"CLUSTER_NAME":"zk_cluster1", "CLUSTER_ID":"0001"},{"CLUSTER_NAME":"zk_cluster2", "CLUSTER_ID":"0002"}]);
	//mini.get("CLUSTER_CHARTS_ID").setData([{"CLUSTER_NAME":"zk_cluster1", "CLUSTER_ID":"0001"},{"CLUSTER_NAME":"zk_cluster2", "CLUSTER_ID":"0002"}]);
	
	//默认选中第一个集群
	//var chartsClusterData = mini.get("CLUSTER_CHARTS_ID").getData();
	//if (chartsClusterData != null && chartsClusterData.length > 0) {
	//	mini.get("CLUSTER_CHARTS_ID").select(0);
	//	changeChartsClusterList();
	//}
}

/**
 * 大数字格式化
 * @param num
 * @returns {String}
 */
function toThousands(num) {
    var num = (num || 0).toString(), result = '';
    while (num.length > 3) {
        result = ',' + num.slice(-3) + result;
        num = num.slice(0, num.length - 3);
    }
    if (num) { result = num + result; }
    return result;
}

/**
 * 图表Charts切换
 * @param e
 */
function changeChartsClusterList(e) {
	var params = {
		CLUSTER_ID:mini.get("CLUSTER_CHARTS_ID").getSelected()["CLUSTER_ID"],                       //zookeeper集群ID
		CLUSTER_CODE:mini.get("CLUSTER_CHARTS_ID").getSelected()["CLUSTER_CODE"]   //jstorm集群编码
	};
	 getJsonDataByPost(Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_CHARTS_QUERY_URL, params, "任务积压查询-查询服务积压情况信息",
        function(result){
 			if (result != null && result.length > 0) {
 				var serviceCharts = echarts.init($("#serviceCharts")[0]);
 				initServiceBar(result, serviceCharts);
 			}
        });
}

/**
 * 服务列表视图（以服务维度展示数据）
 * @param data
 */
function initServiceBar(chartsData, chartsObj) {
	var xAxis = [];
    var series= [];
    var hostSeries = [];
    for(var i = 0 ; i < chartsData.length;i++){
    	xAxis.push(chartsData[i]["SERVICE_NAME"]);
    	series.push(chartsData[i]["TOTAL_EXEC_QUENE_SIZE"]);
    	hostSeries.push(chartsData[i]["HOST_SIZE"]);
    }
    var option = {
        tooltip: {
            show: true,
            trigger: "axis",
            formatter: function(params, ticket, callback) {
            	//X轴名称
            	var name = params[0]["name"];
            	//Y轴对应的值
            	var value = params[0]["value"];
            	//Y轴主机对应数字
            	var hostLen = params[1]["value"];
            	if (hostLen == 0) {
            		return "服务名称: " + name + "<br/> 队列大小: " + toThousands(value);
            	}
            	return "服务名称: " + name + "<br/> 队列大小: " + toThousands(value) + "<br/> 端口个数: " + hostLen;
            }
        },
        legend: {
            data:['执行队列大小', '端口个数']
        },
        grid : {
        	y:50,
        	x:80,
        	y2:80,
        },
        xAxis : [
            {
            	show : true,
                type : 'category',
                data : xAxis,
                axisLabel:{
                	rotate:30,
                	interval:0,
                    clickable:true
                }
            }
        ],
        yAxis : [
            {
            	name : "执行队列大小",
                type : 'value',
                position: 'left'
            },
            {
            	name : "端口个数",
                type : 'value',
                position: 'right'
                //min: 0,
                //max: 500,
            }
        ],
        series : [
            {
                name:"执行队列大小",
                type:"bar",
                barMaxWidth:60,
                itemStyle: {
	                normal: {
	                    label: {
	                        show: true,
	                        position:'top'
	                    },
	                    labelLine: {
	                        show: true,
	                        length:1
	                    },
	                    color:
	                    	function(params){
	                    	var colors=["#ff7f50","#32cd32","#87cefa","#6495ed","#ff69b4","#da70d6","#ffa500"];
	                        return colors[0];									  
	                     }
	                }
	            },
                label: {
                    normal: {
                        show: true,
                        position: 'inside',
                        textStyle:{
                        	color:'top'
                        }
                    }
                },
                data:series
            },
            {
                name:"端口个数",
                type:"bar",
                barMaxWidth:60,
                yAxisIndex:1,
                itemStyle: {
	                normal: {
	                    label: {
	                        show: true,
	                        position:'top'
	                    },
	                    labelLine: {
	                        show: true,
	                        length:1
	                    },
	                    color:
	                    	function(params){
	                    	var colors=["#32cd32","#ff7f50","#87cefa","#6495ed","#ff69b4","#da70d6","#ffa500"];
	                        return colors[0];									  
	                     }
	                }
	            },
                label: {
                    normal: {
                        show: true,
                        position: 'inside',
                        textStyle:{
                        	color:'top'
                        }
                    }
                },
                data:hostSeries
            }
        ]
    };
    // 为echarts对象加载数据 
    chartsObj.setOption(option); 
    chartsObj.on('click', function(e) {
    	var boltCharts = echarts.init($("#boltCharts")[0]);
    	var serviceName = e.name;
    	for (var i=0; i<chartsData.length; i++) {
    		if (serviceName == chartsData[i]["SERVICE_NAME"]) {
    			initBoltCharts(chartsData[i], boltCharts);
    			break;
    		}
    	}
    });
}

/**
 * 根据Bolt维度查询展示数据
 */
function initBoltCharts(boltChartsData, chartsObj) {
	var xAxis = [];
    var series= [];
    
    //任务列表
    var taskData = boltChartsData["TASK_DATA"];
    for(var i = 0 ; i < taskData.length;i++){
    	series.push(taskData[i]["TASK_EXEC_QUENE_SIZE"]);
    	xAxis.push(taskData[i]["TASK_NAME"]);
    }
    var option = {
        tooltip: {
            show: true,
            trigger: "axis",
            formatter: function(params, ticket, callback) {
            	//X轴名称
            	var name = params[0]["name"];
            	//X轴对应的值
            	var value = params[0]["value"];
            	return "任务名称: " + name + "<br/> 队列大小: " + toThousands(value);
            }
        },
        legend: {
            data:['执行队列大小']
        },
        grid : {
        	y:50,
        	x:80,
        	y2:80,
        },
        xAxis : [
            {
                type : 'category',
                data : xAxis,
                axisLabel:{
                	rotate:60,
                	interval:0,
                    clickable:true
                }
            }
        ],
        yAxis : [
            {
            	name : '执行队列大小',
                type : 'value',
                position: 'left'
            }
        ],
        series : [
            {
                name:"执行队列大小",
                type:"bar",
                barMaxWidth:60,
                itemStyle: {
	                normal: {
	                    label: {
	                        show: true,
	                        position:'top'
	                    },
	                    labelLine: {
	                        show: true,
	                        length:1
	                    },
	                    color:
	                    	function(params){
	                    	var colors=["#ff7f50","#87cefa","#32cd32","#6495ed","#ff69b4","#da70d6","#ffa500"];
	                        return colors[0];									  
	                     }
	                }
	            },
                label: {
                    normal: {
                        show: true,
                        position: 'inside',
                        textStyle:{
                        	color:'top'
                        }
                    }
                },
                "data":series
            }
        ]
    };
    chartsObj.setOption(option); 
    chartsObj.on('click', function(e) {
    	var taskName = e.name;
    	for (var i=0; i<taskData.length; i++) {
    		if (taskName == taskData[i]["TASK_NAME"]) {
    			initGridData(boltChartsData, taskData[i]);
    			break;
    		}
    	}
    	
    	
    });
}

/**
 * 点击Bolt图表修改Grid数据
 */
function initGridData(boltChartsData, taskData) {
	//设置图标数据
	var chartsClusterId = mini.get("CLUSTER_CHARTS_ID").getValue();
	mini.get("CLUSTER_ID").setValue(chartsClusterId);
	
	//查询所有的服务
	var params = {
		//CLUSTER_ID:mini.get("CLUSTER_ID").getValue()
        CLUSTER_ID:mini.get("CLUSTER_ID").getSelected()["CLUSTER_ID"],      //zookeeper集群ID
        CLUSTER_CODE:mini.get("CLUSTER_ID").getSelected()["CLUSTER_CODE"]   //jstorm集群编码
	};
	comboxLoad(mini.get("SERVICE_NAME"), params, null, Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_LIST_QUERY_URL, null, false);
		
	//服务名称
	var servieName = boltChartsData["SERVICE_NAME"];
	mini.get("SERVICE_NAME").setValue(servieName);
	//执行查询操作
	search();
	//执行过滤操作
	var taskName = taskData["TASK_NAME"];
	mini.get("TASK_NAME").setValue(taskName);
	changeHostList(null);
}

/**
 * 查询ZK集群服务列表
 * @param e
 */
function changeClusterList(e) {
	mini.get("SERVICE_NAME").setValue("");
	var params = {
        CLUSTER_ID:mini.get("CLUSTER_ID").getSelected()["CLUSTER_ID"],      //zookeeper集群ID
        CLUSTER_CODE:mini.get("CLUSTER_ID").getSelected()["CLUSTER_CODE"]   //jstorm集群编码
	};
	comboxLoad(mini.get("SERVICE_NAME"), params, null, Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_LIST_QUERY_URL, null, false);
}

/**
 * 查询
 */
function search() {
	JsVar["queryFrom"].validate();
    if (JsVar["queryFrom"].isValid() == false){
        return;
    }
    mini.get("SORT_RULE").setValue("TASK_ID_ASC");
    var paramsObj = JsVar["queryFrom"].getData();
    paramsObj["CLUSTER_ID"] = mini.get("CLUSTER_ID").getSelected()["CLUSTER_ID"];      //zookeeper集群ID
    paramsObj["CLUSTER_CODE"] = mini.get("CLUSTER_ID").getSelected()["CLUSTER_CODE"];  //jstorm集群编码
    load(paramsObj);
}

/**
 * 加载表格
 * @param param
 */
function load(param){
    //datagridLoad(JsVar["overstockGrid"], param, null, Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_DATA_QUERY_URL);
	JsVar["updateForm"].reset();
	JsVar["overstockGrid"].setData([]);
	JsVar["overstockGrid"].setTotalCount(0);
	getJsonDataByPost(Globals.baseActionUrl.TASK_OVERSTOCK_SERVICE_DATA_QUERY_URL, param, "任务运行积压查询-查询服务积压数据",
			function(result){
			if (result != null && result.length > 0) {
				JsVar["overstockGrid"].setData(result);
				JsVar["overstockGrid"].setTotalCount(result.length);
			}
	}, null, null, false);
	
	//服务对应原始数据
    JsVar["STOCK_DATA"] = JsVar["overstockGrid"].getData();
    
    //初始化查询条件
    initHostData();
    initTaskData();
    initExecData();
}

/**
 * 初始化过滤规则条件
 */
function initHostData() {
	var hostList = [];
	if (JsVar["STOCK_DATA"] != null && JsVar["STOCK_DATA"].length > 0) {
		for (var i=0; i<JsVar["STOCK_DATA"].length; i++) {
			var hostIp = JsVar["STOCK_DATA"][i]["HOST_IP"].split(":")[0];
			hostList.push(hostIp);
		}
	}
	var uniqueHostList = hostList.unique();
	var finalHostList = [];
	for (var i=0; i<uniqueHostList.length; i++) {
		finalHostList.push({HOST_IP:uniqueHostList[i]});
	}
	mini.get("HOST_LIST").setData(finalHostList);
}

/**
 * 任务数据
 */
function initTaskData() {
	var taskNameList = [];
	if (JsVar["STOCK_DATA"] != null && JsVar["STOCK_DATA"].length > 0) {
		for (var i=0; i<JsVar["STOCK_DATA"].length; i++) {
			var taskName = JsVar["STOCK_DATA"][i]["TASK_NAME"];
			taskNameList.push(taskName);
		}
	}
	var uniqueTaskList = taskNameList.unique();
	var finalTaskList = [];
	for (var i=0; i<uniqueTaskList.length; i++) {
		finalTaskList.push({TASK_NAME:uniqueTaskList[i]});
	}
	mini.get("TASK_NAME").setData(finalTaskList);
}

/**
 * 执行队列大小
 */
function initExecData() {
	var execList = [];
	if (JsVar["STOCK_DATA"] != null && JsVar["STOCK_DATA"].length > 0) {
		for (var i=0; i<JsVar["STOCK_DATA"].length; i++) {
			var execSize = JsVar["STOCK_DATA"][i]["EXEC_QUENE_SIZE"];
			execList.push(execSize);
		}
	}

	//剔重并且再次按照数字大小排序
	var uniqueExecList = execList.unique();
	uniqueExecList.sort(function(num1, num2) {
		return parseInt(num1) - parseInt(num2);
	});
	var finalExecList = [];
	for (var i=0; i<uniqueExecList.length; i++) {
		finalExecList.push({EXEC_QUENE_SIZE:">=" + uniqueExecList[i]});
	}
	mini.get("EXEC_QUENE_SIZE").setData(finalExecList);
}


/**
 * 主机列表change事件
 * @param e
 */
function changeHostList(e) {
	//选中主机列表
	var hostIp = mini.get("HOST_LIST").getValue();
	//任务名称
	var taskName = mini.get("TASK_NAME").getValue();
	//执行队列大小
	var execSize = mini.get("EXEC_QUENE_SIZE").getValue();
	
	var tempData = [];
	for (var i=0; i<JsVar["STOCK_DATA"].length; i++) {
		if ((isNotEmptyStr(hostIp) && JsVar["STOCK_DATA"][i]["HOST_IP"].indexOf(hostIp) == -1)) {
			continue;
		}
		if ((isNotEmptyStr(taskName) && JsVar["STOCK_DATA"][i]["TASK_NAME"] != taskName)) {
			continue;
		}
		if (isNotEmptyStr(execSize)) {
			var execNum = execSize.substring(2, execSize.length);
			if (parseInt(JsVar["STOCK_DATA"][i]["EXEC_QUENE_SIZE"]) < parseInt(execNum)) {
				continue;
			}
		}
		tempData.push(JsVar["STOCK_DATA"][i]);
	}
	JsVar["overstockGrid"].clearRows();
	JsVar["overstockGrid"].setData(tempData);
	JsVar["overstockGrid"].setTotalCount(tempData.length);
	
	//调用排序规则
	changeSortRule();
}

/**
 * 排序规则修改
 * @param e
 */
function changeSortRule() {
	var sortRule = mini.get("SORT_RULE").getValue();
	switch(sortRule) {
		case "TASK_ID_ASC":
			sortData("TASK_ID", "ASC");
			break;
		case "TASK_ID_DESC":
			sortData("TASK_ID", "DESC");
			break;
		case "EXEC_QUENE_SIZE_ASC":
			sortData("EXEC_QUENE_SIZE", "ASC");
			break;
		case "EXEC_QUENE_SIZE_DESC":
			sortData("EXEC_QUENE_SIZE", "DESC");
			break;
		case "FILE_QUEUE_SIZE_ASC":
			sortData("FILE_QUEUE_SIZE", "ASC");
			break;
		case "FILE_QUEUE_SIZE_DESC":
			sortData("FILE_QUEUE_SIZE", "DESC");
			break;
        case "MSG_COUNT_ASC":
            sortData("MSG_COUNT", "ASC");
            break;
        case "MSG_COUNT_DESC":
            sortData("MSG_COUNT", "DESC");
            break;
		default:
			break;
	}
}

/**
 * 前台对象排序
 * @param field
 * @param sort
 */
function sortData(field, sort) {
	var stockData = JsVar["overstockGrid"].getData();
	if (stockData != null) {
		if (sort == "ASC") {
			stockData.sort(function(obj1, obj2) {
				return obj1[field] - obj2[field];
			});
		} else {
			stockData.sort(function(obj1, obj2) {
				return obj2[field] - obj1[field];
			});
		}
	}
	JsVar["overstockGrid"].clearRows();
	JsVar["overstockGrid"].setData(stockData);
	JsVar["overstockGrid"].setTotalCount(stockData.length);
}

/**
 * 终端操作
 * @param e
 */
function onRenderHostTermial(e) {
	var hostIp = e.record.HOST_IP;
    return '<a class="Delete_Button" href="javascript:goHostTermial(\'' + hostIp.split(":")[0] + '\')">' + hostIp + '</a>';
}
/**
 * 单个终端显示
 * @param hostId
 */
function goHostTermial(hostIp) {
	//根据主机IP查找主机ID
	var params = {
		HOST_IP:hostIp
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "终端操作-根据主机IP查找主机ID",
        function(result){
 			if (result != null && result.length > 0) {
 				var hostID = result[0]["HOST_ID"];
 				var hostStr = "'" + hostID + "'";
 			    $("#termialHost").val(hostStr);
 			    $("#termialForm").attr("action", Globals.baseActionUrl.HOST_ACTION_TERMINAL_URL);
 			    $("#termialForm").submit();
 			}
     }, "host.queryHostList", null, false);
		 
    
}

