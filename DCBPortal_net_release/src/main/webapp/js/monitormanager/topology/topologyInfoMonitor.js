//定义变量， 通常是页面控件和参数
var JsVar = {};

/**
 * 初始化 
 */
$(document).ready(function () {
    mini.parse();
 
    JsVar["topologyGrid"] = mini.get("topologyGrid");
    JsVar["componentPanel"] = mini.get("componentPanel");
    JsVar["componentGrid"] = mini.get("componentGrid");
    JsVar["workerGrid"] = mini.get("workerGrid");
    JsVar["taskGrid"] = mini.get("taskGrid");
     
    getForwardParams();

    initChart();
    initTopologyGraph();
     
});

//获取跳转页面get请求参数,并保存到JsVar中 
function getForwardParams(){
	// var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	JsVar["clusterName"] = getQueryString('clusterName');
	JsVar["topologyId"] = getQueryString('topologyId');
	JsVar["topologyName"] = getQueryString('topologyName');
    initTopologyGrid();
}

// 初始化所有表格
function initTopologyGrid(){
    var queryParams = {};
    queryParams["clusterName"] = JsVar["clusterName"];
    queryParams["topologyId"] = JsVar["topologyId"];
	
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_SUMMARY_URL,queryParams,"",			
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["workerList"] = result["uIWorkerMetricList"];
			JsVar["taskDataList"] = result["taskEntityList"];
			JsVar["topologyGrid"].setData([result["topologySummaryMap"]]);
			JsVar["componentGrid"].setData(result["uIComponenetMetricList"]);
			JsVar["workerGrid"].setData(result["uIWorkerMetricList"]);
			
			JsVar["taskGrid"].setData(result["taskEntityList"]);
			
		}
	},"","",false);
	
}
function versionTRenderer(e){
	var name =e.record.name;
	return name.substring(name.lastIndexOf('-')+1,name.length);
}
function errorWorkersRenderer(e){
	var errorTask = []; 
	for(var i = 0 ; i < JsVar["taskDataList"].length ;i++){
		var errors = JsVar["taskDataList"][i]["errors"];
		 if(isNotEmptyStr(errors)){
			 var isflag = true;
			 for(var j = 0 ; j < errorTask.length;j++){
				 if(errorTask[j]["host"]== JsVar["taskDataList"][i]["host"]
				 && errorTask[j]["port"]== JsVar["taskDataList"][i]["port"]){
					 isflag= false;
					 break;
				 }
			 }
			 if(isflag){
				 errorTask.push(JsVar["taskDataList"][i]);
			 }
		 }
	}
	return errorTask.length;
	
}

function hostWorkers(e){
	var hosts = []; 
	for(var i = 0 ; i < JsVar["workerList"].length ;i++){
		 var isflag =true;
		 for(var j = 0 ; j < hosts.length;j++){
			 if(hosts[j]["host"]== JsVar["workerList"][i]["host"] ){
				 isflag= false;
				 break;
			 }
		 }
		 if(isflag){
			 hosts.push(JsVar["workerList"][i]);
		 }
			 
		 
	}
	return hosts.length;
	
}

/**
 * component监控渲染超链接
 * @param e 
 * @returns {String}
 */
function componentNameRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:componentMonitorForward('+index+')">'+e.record.componentName+'</a>';
}

/**
 * supervisor监控渲染超链接
 */
function supervisorMonitorRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:supervisorMonitorForward('+index+')">'+e.record.host+'</a>';
}

/**
 * netty监控渲染超链接
 */
function nettyMonitorRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:nettyMonitorForward('+index+')">netty</a>';
}

// component监控跳转
function componentMonitorForward(index){
	var rowInfo = JsVar["componentGrid"].getRow(index);
	
	window.location.href = "componentMonitor?clusterName=" 
									+ JsVar["clusterName"] 
									+ "&topologyId=" 
									+ JsVar["topologyId"] 
									+ "&componentName=" 
									+ rowInfo.componentName
									+ "&topologyName="
									+ JsVar["topologyName"];
}

//supervisor监控跳转 
function supervisorMonitorForward(index){
	var rowInfo = JsVar["workerGrid"].getRow(index);
	window.location.href = "supervisorMonitor?clusterName=" 
								+ JsVar["clusterName"]
								+ "&topologyId=" 
								+ JsVar["topologyId"]
								+ "&host=" 
								+ rowInfo.host;
}

//netty监控跳转
function nettyMonitorForward(index){
	var rowInfo = JsVar["workerGrid"].getRow(index);
	window.location.href = "workerNettyMonitor?clusterName=" 
									+ JsVar["clusterName"] 
									+ "&topologyId=" 
									+ JsVar["topologyId"] 
									+ "&host=" 
									+ rowInfo.host
									+ "&port="
									+ rowInfo.port;
}

/**
 * 拓补状态高亮显示
 */
function stateHighlightRenderer(e){
	if(e.value=="ACTIVE"){
		return "<span class='label label-success'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}else{
		return "<span class='label label-danger'>"+"&nbsp;"+e.value+"&nbsp;&nbsp;</span>";
	}
}

/**
 * 配置渲染超链接
 */
function configRenderer(e){
	var index = e.rowIndex;
	return '<a class="Delete_Button" href="javascript:configLogForward('+index+')">查看</a>';
}

function configLogForward(index){
	var rowInfo = JsVar["topologyGrid"].getRow(index);
	
	window.location.href = Globals.ctx + "/jsp/monitormanager/clustersummary/topConfigInfo?clusterName=" 
		+ JsVar["clusterName"] 
//		+ "&type=topology"
		+ "&topologyId=" 
		+ rowInfo.id;
}

/**
 * 任务状态错误高亮显示
 */
function errorHighlightRenderer(e){
	if(e.value != null && e.value.length > 0){
		return "<span style='color:red;'>" + e.value + "</span>";
	}else{
		return "";
	}
	
}

/**
 * 错误数组渲染器
 * @param e
 */
function errorArrayRenderer(e){
	var errors=e.record.errors;
	if(errors == undefined || errors == null || errors == "" || errors.length<1){
		return "";
	} 
	
	var title="";
	for(var i=0;i<errors.length;i++){
		 var ts = errors[i]["errorTime"]* 1000;
		 if(errors[i]["error"].lastIndexOf() == -1 ){
			 var  idx = errors[i]["error"].indexOf("&#10;");
			 var length = errors[i]["error"].length;
			 if (idx != -1) {
                    var first_line = errors[i]["error"].substring(0, idx)+"&#10;";
                    var rest_lines = errors[i]["error"].substring(idx + 1, length);
                    title+=first_line + "  , at " + mmsecondFormate(ts);
                    title+=rest_lines;
                }else{
                	title+=errors[i]["error"] + "   , at " + mmsecondFormate(ts);
                }
		 }else{
			 title+=errors[i]["error"]+"  , at "+mmsecondFormate(ts);
		 }
		 title+="&#10;";
	 }
	 var isWarningMsg =false;
	 
	 for(var i=0;i<errors.length;i++){
		 if(errors[i]["error"].indexOf("is full")>-1 || errors[i]["error"].indexOf("Backpressure")>-1, errors[i]["error"].indexOf("backpressure")>-1){
			 isWarningMsg = true;
		 }
	 }
	 if(isWarningMsg){
		 return "<span  style='color:red;' title='"+title+"'>W("+errors.length+")"+"</span>";
	 }else{
		 return "<span  style='color:red;' title='"+title+"'>E("+errors.length+")"+"</span>";
	 }
}

function mmsecondFormate(mmsecond){
	 var tf = function(i){return (i < 10 ? '0' : '') + i}; 
	 var date = new Date(mmsecond);
	 return date.getFullYear()+"-"+tf(date.getMonth()+1)+"-"+tf(date.getDate())+" "+tf(date.getHours())+":"+tf(date.getMinutes())+":"+tf(date.getSeconds());
}

// 时间局部刷新按钮 
function componentDataEvent(win){
	var queryParams = {};
    queryParams["clusterName"] = JsVar["clusterName"];
    queryParams["topologyId"] = JsVar["topologyId"];
    queryParams["win"] = win + "";
	
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_COMPONENT_METRICS_LIST_SUMMARY_URL,queryParams,"",
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["componentGrid"].setData(result["uIComponentMetric"]);
		}
	});
}

//时间局部刷新按钮
function workerDataEvent(win){
	var queryParams = {};
    queryParams["clusterName"] = JsVar["clusterName"];
    queryParams["topologyId"] = JsVar["topologyId"];
    queryParams["win"] = win + "";
	
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_WORKER_METRICS_URL,queryParams,"",
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["workerGrid"].setData(result["uIWorkerMetricList"]);
		}
	});
}

/**
* 格式化HeapMemory
* @param e
*/
function formatterHeapMemory(e){
	if(e.value != null){
		return (Number(e.value.replace(/,/g,"")/(1024*1024))).toFixed(2) + " MB";
	}
}

/**
 * 耗时格式化
 * @param e
 */
function formatTime(e){
	if(e.value != null && e.value != "0"){
		return (Number(e.value.replace(/,/g,"")/1000)).toFixed(2) + " ms";
	}
}

function initChart(){
	var lineChartsOptions=new Object();
	var MyChart = {};
	//基于准备好的dom，初始化echarts实例
	MyChart["Failed"] = echarts.init(document.getElementById('filedChart'));
	MyChart["Emitted"] = echarts.init(document.getElementById('emittedChart'));
	MyChart["Acked"] = echarts.init(document.getElementById('ackedChart'));
	MyChart["SendTps"] = echarts.init(document.getElementById('sendTpsChart'));
	MyChart["RecvTps"] = echarts.init(document.getElementById('recTpsChart'));
	MyChart["ProcessLatency"] = echarts.init(document.getElementById('processChart'));
	MyChart["CpuUsedRatio"] = echarts.init(document.getElementById('cpuChart'));
	MyChart["MemoryUsed"] = echarts.init(document.getElementById('memUsedChart'));
	MyChart["HeapMemory"] = echarts.init(document.getElementById('heapMemoryChart'));
	
	 //获取图表数据
    var data=getChartsDataByPost();
    var len = 0;
    var array = ["Failed","Emitted","Acked","SendTps","RecvTps","ProcessLatency","CpuUsedRatio","MemoryUsed","HeapMemory"];
    var transfer = ["失败发送","成功发送","处理反馈","发送速率","接收速率","流转耗时","CPU使用","内存使用","堆内存"];
    for(var j = 0; j < array.length; j ++){
    	if(data[array[j]] != undefined){
    		len = data[array[j]]["series"][0]["data"].length;
    		break;
    	}
    }
    
    for(var i = 0; i < array.length; i ++){
    	if(data[array[i]] != undefined){
    		//提示tooltip
    		if(array[i] == "Failed" ||array[i] == "Emitted" ||array[i] == "Acked"){
    			data[array[i]]["tooltip"]["formatter"] = array[i] + " ：{c}次<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} 次';
    		}
    		if(array[i] == "SendTps" ||array[i] == "RecvTps"){
    			data[array[i]]["tooltip"]["formatter"] = array[i] + " ：{c}tps<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} tps';
    			for(var k = 0; k < len; k ++){
    				data[array[i]]["series"][0]["data"][k] = data[array[i]]["series"][0]["data"][k].toFixed(2);
    			}
    		}
    		if(array[i] == "ProcessLatency"){
    			data[array[i]]["tooltip"]["formatter"] = "Process ：{c}ms<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} ms';
    			data[array[i]]["title"]["text"]='Process';
    			for(var k = 0; k < len; k ++){
    				var tmpData = data[array[i]]["series"][0]["data"][i];
    				data[array[i]]["series"][0]["data"][i] = (tmpData/1000);
    			}
    		}
    		if(array[i] == "CpuUsedRatio"){
    			data[array[i]]["tooltip"]["formatter"] =  array[i] + " ：{c}%<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} %';
    		}
    		if(array[i] == "MemoryUsed" || array[i] == "HeapMemory"){
    			data[array[i]]["tooltip"]["formatter"] =  array[i] + " ：{c}MB<br/>时间：{b}";
    			data[array[i]]["yAxis"]["axisLabel"]["formatter"]='{value} MB';
    			for(var k = 0; k < len; k ++){
    				var tmpData = data[array[i]]["series"][0]["data"][k];
    				data[array[i]]["series"][0]["data"][k]=(tmpData/(1024*1024)).toFixed(2);
    			}
    		}
    		data[array[i]]["title"]["text"]=transfer[i];
    		MyChart[array[i]].setOption(data[array[i]]);
    	}else{
    		MyChart[array[i]].setOption({
				title: {
					text: transfer[i],
					x:'center',
					y:'bottom',
					textStyle:{fontSize:14}
				},
				tooltip: {
					trigger: 'axis',
					axisPointer : {
			            type : 'line',
			            lineStyle : {
			                color: '#48b',
			                width: 2,
			                type: 'solid'
			            }
					}
				},
				legend: {
					data:['data'],
					show:false
				},
				grid:{//表格左上角间距
					y:40,
					x:100
				},
				xAxis: {
					data: [0],
					splitLine:[{
						interval:1
					}]
				},
				yAxis: {
					axisLabel:{
						
					}
				},
				series: [{
					type: 'line',
					data: [0]
				}]
			});
    	}
    }
    
    //将面板折叠
    mini.get("chartPanel").setExpanded(false);
}

function getChartsDataByPost(){
	var lineChartsOptions=new Object();
	getJsonDataByPost(Globals.ctx + "/api/v2/cluster/"+JsVar["clusterName"]+"/topology/"+JsVar["topologyId"]+"/metrics",'',"",
		function(result){
		var time_array=new Array(); 
		var time=result["metrics"][0].category;
		
		for(var i=0;i<time.length;i++){
			var _time=time[i].split(" ")[1];
			time_array.push(_time);
		}
			$.each( result["metrics"] , function( i , item ){
				// 指定图表的配置项和数据
				var option={
					title: {
						text: item.name,
						x:'center',
						y:'bottom',
						textStyle:{fontSize:14}
					},
					tooltip: {
						trigger: 'axis',
						axisPointer : {
				            type : 'line',
				            lineStyle : {
				                color: '#48b',
				                width: 2,
				                type: 'solid'
				            }
						}
					},
					legend: {
						data:['data'],
						show:false
					},
					grid:{//表格左上角间距
						y:40,
						x:100
					},
					xAxis: {
						data: time_array,
						splitLine:[{
							interval:1
						}]
					},
					yAxis: {
						axisLabel:{
							
						}
					},
					series: [{
						type: 'line',
						data: item.data
					}]
				};
				lineChartsOptions[item.name]=option;
			});
		},null,null,false);
	return lineChartsOptions;
}

function initTopologyGraph(){
	//init vue model
    var vm = new Vue({
        el: '#graph-event',
        data: {
            title: "",
            head: [],
            mapValue: [],
            valid: false
        }
    });

    
  //draw vis topology graph
    var url = Globals.ctx + "/api/v2/cluster/"+JsVar["clusterName"]+"/topology/"+JsVar["topologyId"]+"/graph";
    getJsonDataByPost(url,"","", function (data) {
        if (data.error){
            $('#topology-graph').hide();
            $('#topology-graph-tips').html("<p class='text-muted'>" + data.error + "</p>");
            return;
        }else{
            data = data.graph;
        }

        tableData = new VisTable().newData(data);

        var visStyle = new VisNetWork();
        var visData = visStyle.newData(data);
        var options = visStyle.newOptions({depth: data.depth, breadth: data.breadth});

        //reset graph event style
        var event = document.getElementById('graph-event');
        // 初始化表格宽高,固定
        var evenWith = $("#topologygraph").width()- parseInt(options["width"].substring(0,options["width"].length-1))-30;
        event.setAttribute("style", "width:"+evenWith+"px;heigth:auto;margin-top:20px;margin-left:25px;float;right;");

        // 为节点图计算宽度
        var event_width = $("#topologygraph").width() - evenWith - 30;
        // reset topology graph width and height
        var container = document.getElementById('topology-graph');
        // 初始化节点图宽高,不固定
        container.setAttribute("style", "width:"+event_width+"px;height:auto; float:left; border-right:1px dashed #ccc;");



        // initialize your network!
        var network = new vis.Network(container, visData, options);
        network.on("click", function (params) {
            var id = undefined;
            if (params.nodes.length > 0) {
                id = "component-" + params.nodes[0];
            } else if (params.edges.length > 0) {
                id = "stream-" + params.edges[0];
            }

            if (id) {
                vm.$data = tableData[id];
            }
        });

        //do the hash , after draw the graph
        var hash = window.location.hash;
        //$(hash).tab('show');
    },"","",false);
    
   
}

