//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["workerInfoGrid"] = mini.get("workerInfoGrid");
    
    getForwardParams();
    initWorker();
//    initChart();
});

function initWorker(){
	var queryParams = {};
	queryParams["clusterName"] = JsVar["clusterName"];
	queryParams["topologyId"] = JsVar["topologyId"];
	queryParams["host"] = JsVar["host"];
	queryParams["port"] = JsVar["port"];
//	queryParams["win"] = "600";
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_SUPERVISOR_WORKER_METRIC_URL,queryParams,"",
			function (result){
				if(!$.isEmptyObject(result)){
					JsVar["workerInfoGrid"].setData([result["workerMetric"]]);
					initChart(result["workerMetric"]);
				}
	});
}

//获取跳转页面get请求参数,并保存到JsVar中  
function getForwardParams(){
	// var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	JsVar["clusterName"] = getQueryString('clusterName');
	JsVar["topologyId"] = getQueryString('topologyId');
	JsVar["host"] = getQueryString('host');
	JsVar["port"] = getQueryString('port');
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

function initChart(dataArray){
	var lineChartsOptions=new Object();
	//基于准备好的dom，初始化echarts实例
	var NettyCliSendSpeed = echarts.init(document.getElementById('NettyCliSendSpeed'));
	var NettySrvRecvSpeed = echarts.init(document.getElementById('NettySrvRecvSpeed'));
	var MsgDecodeTime = echarts.init(document.getElementById('MsgDecodeTime'));
	var CpuUsedRatio = echarts.init(document.getElementById('CpuUsedRatio'));
	var MemoryUsed = echarts.init(document.getElementById('MemoryUsed'));
	var HeapMemory = echarts.init(document.getElementById('HeapMemory'));
	var SendCtrlQueue = echarts.init(document.getElementById('SendCtrlQueue'));
	var RecvCtrlQueue = echarts.init(document.getElementById('RecvCtrlQueue'));
	
	 //获取图表数据
    var data=getChartsDataByPost(dataArray);
    
    // 处理数据
    for(var i = 0;i < data["NettyCliSendSpeed"]["series"][0]["data"].length;i++){
    	var tmpData = data["HeapMemory"]["series"][0]["data"][i];
   	 	data["HeapMemory"]["series"][0]["data"][i]=(Number(tmpData/(1024*1024))).toFixed(2);
   	 	 
   	 	tmpData = data["MsgDecodeTime"]["series"][0]["data"][i];
   	 	data["MsgDecodeTime"]["series"][0]["data"][i] = (tmpData/1000);
   	 	
	   	data["SendCtrlQueue"]["series"][0]["data"][i] = Number(data["SendCtrlQueue"]["series"][0]["data"][i]).toFixed(2);
		data["RecvCtrlQueue"]["series"][0]["data"][i] = Number(data["RecvCtrlQueue"]["series"][0]["data"][i]).toFixed(2);
    }
    
    // 悬浮框格式化
    data["NettyCliSendSpeed"]["tooltip"]["formatter"] = "NettyCliSendSpeed:{c}tps<br/>时间:{b}";
    data["NettySrvRecvSpeed"]["tooltip"]["formatter"] = "NettySrvRecvSpeed:{c}tps<br/>时间:{b}";
    data["MsgDecodeTime"]["tooltip"]["formatter"] = "MsgDecodeTime:{c}ms<br/>时间:{b}";
    data["MemoryUsed"]["tooltip"]["formatter"] = "MemoryUsed:{c}MB<br/>时间:{b}";
    data["CpuUsedRatio"]["tooltip"]["formatter"] = "CpuUsedRatio:{c}%<br/>时间:{b}";
    data["HeapMemory"]["tooltip"]["formatter"]="HeapMemory ：{c}MB<br/>时间：{b}";
    data["SendCtrlQueue"]["tooltip"]["formatter"]="SendCtrlQueue ：{c}%<br/>时间：{b}";
    data["RecvCtrlQueue"]["tooltip"]["formatter"]="RecvCtrlQueue ：{c}%<br/>时间：{b}";
    
    //y轴加单位
    data["SendCtrlQueue"]["yAxis"]["axisLabel"]["formatter"]='{value} %';
    data["RecvCtrlQueue"]["yAxis"]["axisLabel"]["formatter"]='{value} %';
    data["MsgDecodeTime"]["yAxis"]["axisLabel"]["formatter"]='{value} ms';
    data["CpuUsedRatio"]["yAxis"]["axisLabel"]["formatter"]='{value} %';
    data["MemoryUsed"]["yAxis"]["axisLabel"]["formatter"]='{value} MB';
    data["HeapMemory"]["yAxis"]["axisLabel"]["formatter"]='{value} MB';
    data["NettyCliSendSpeed"]["yAxis"]["axisLabel"]["formatter"]='{value} tps';
    data["NettySrvRecvSpeed"]["yAxis"]["axisLabel"]["formatter"]='{value} tps';
    
    NettyCliSendSpeed.setOption(data["NettyCliSendSpeed"]);
    NettySrvRecvSpeed.setOption(data["NettySrvRecvSpeed"]);
    MsgDecodeTime.setOption(data["MsgDecodeTime"]);
    CpuUsedRatio.setOption(data["CpuUsedRatio"]);
    MemoryUsed.setOption(data["MemoryUsed"]);
    HeapMemory.setOption(data["HeapMemory"]);
    SendCtrlQueue.setOption(data["SendCtrlQueue"]);
    RecvCtrlQueue.setOption(data["RecvCtrlQueue"]);
}

function getChartsDataByPost(dataArray){
	var lineChartsOptions=new Object();
	
	// 各参数数组准备
	var dataTimeArray = [];
	var CpuUsedRatioArray = [];
	var HeapMemoryArray = [];
	var MemoryUsedArray = [];
	var MsgDecodeTimeArray = [];
	var NettyCliSendSpeedArray = [];
	var NettySrvRecvSpeedArray = [];
	var RecvCtrlQueueArray = [];
	var SendCtrlQueueArray = [];
	var nameArray = ["CpuUsedRatio","HeapMemory","MemoryUsed","MsgDecodeTime","RecvCtrlQueue",
	                 "NettyCliSendSpeed","NettySrvRecvSpeed","SendCtrlQueue"];
	var dataObj = {};
	
	for(var i = 0; i < dataArray.length; i++){
		dataTimeArray[i] = dataArray[i]["dataTime"];
		CpuUsedRatioArray[i] = dataArray[i]["CpuUsedRatio"];
		HeapMemoryArray[i] = dataArray[i]["HeapMemory"].replace(/,/g,"");
		MemoryUsedArray[i] = dataArray[i]["MemoryUsed"].split(" ")[0];
		MsgDecodeTimeArray[i] = dataArray[i]["MsgDecodeTime"];
		NettyCliSendSpeedArray[i] = dataArray[i]["NettyCliSendSpeed"];
		NettySrvRecvSpeedArray[i] = dataArray[i]["NettySrvRecvSpeed"].replace(/,/g,"");
		RecvCtrlQueueArray[i] = dataArray[i]["RecvCtrlQueue"];
		SendCtrlQueueArray[i] = dataArray[i]["SendCtrlQueue"];
		
	}
	
	// 根据名称塞进map中
	dataObj["CpuUsedRatio"] = CpuUsedRatioArray;
	dataObj["HeapMemory"] = HeapMemoryArray;
	dataObj["MemoryUsed"] = MemoryUsedArray;
	dataObj["MsgDecodeTime"] = MsgDecodeTimeArray;
	dataObj["NettyCliSendSpeed"] = NettyCliSendSpeedArray;
	dataObj["NettySrvRecvSpeed"] = NettySrvRecvSpeedArray;
	dataObj["RecvCtrlQueue"] = RecvCtrlQueueArray;
	dataObj["SendCtrlQueue"] = SendCtrlQueueArray;
	
	for(var j = 0; j < nameArray.length; j++){
		var dataName = nameArray[j] + "Array";
		// 指定图表的配置项和数据
		var option={
			title: {
				text: nameArray[j],
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
				data: dataTimeArray,
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
				data: dataObj[nameArray[j]]
			}]
		};
		lineChartsOptions[nameArray[j]]=option;
	}
	
	return lineChartsOptions;
}