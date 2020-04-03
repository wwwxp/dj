//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["nettyGrid"] = mini.get("nettyGrid");
    JsVar["chartId"] = mini.get("chartId");
    
    getForwardParams();
    JsVar["chartId"].getHeaderEl().getElementsByClassName("mini-panel-title")[0].innerHTML = "发展速度趋势【" + JsVar['host'] + ":" + JsVar['port'] + "】";
    nettyEvent(60);
});

//获取跳转页面get请求参数,HOST_IP,并保存到JsVar中 
function getForwardParams(){
	// var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	JsVar["clusterName"] = getQueryString('clusterName');
	JsVar["topologyId"] = getQueryString('topologyId');
	JsVar["host"] = getQueryString('host');
	JsVar["port"] = getQueryString('port');
}

/**
 * 按钮点击事件
 * @param win
 */
function nettyEvent(win){
	var queryParams = {};
	queryParams["clusterName"] = JsVar["clusterName"];
    queryParams["topologyId"] = JsVar["topologyId"];
    queryParams["host"] = JsVar["host"];
    queryParams["port"] = JsVar["port"];
    queryParams["win"] = win + "";
	
    getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_TOPOLOGY_NETTY_METRICS_URL,queryParams,"",
			function (result){
		if(!$.isEmptyObject(result)){
			JsVar["nettyGrid"].setData(result["uINettyMetricList"]);
			
			if(result["chartSeriesList"] !=null && result["chartSeriesList"].length > 0){
				// 画图
				initChart(result["chartSeriesList"]);
				document.getElementById('main').style.visibility = "visible";
			}else{// 隐藏图表
				document.getElementById('main').style.visibility = "hidden";
			}
		}
		
	});
}

function initChart(dataArray){
	//基于准备好的dom，初始化echarts实例
		var myChart = echarts.init(document.getElementById('main'));
		var nameArray = [];
		var data = [];
		var timeArray = []; 
		for(var i = 0;i < dataArray.length; i++){
			var name = "->" + dataArray[i]["name"].split("->")[1];
			nameArray.push(name);
			
			var obj = {};
			obj["name"] = name;
			obj["type"] = "line";
			obj["data"] = dataArray[i]["label"];
			data.push(obj);
			
		}
		
		for(var j = 0;j < dataArray[0]["category"].length; j++){
			var time = dataArray[0]["category"][j].split(" ")[1];
			timeArray.push(time);
		}
		
	// 指定图表的配置项和数据
		var option = {
//				title: {
//					show:false,
//					text: '',
//				},
				tooltip: {
					trigger:'axis'
					
				},
				legend: {
					data:nameArray
				},
				xAxis: {
					type:'category',
					data: timeArray
				},
				yAxis: {
					type:'value'
				},
				series: data
		};
		// 使用刚指定的配置项和数据显示图表。
		myChart.setOption(option);
	}