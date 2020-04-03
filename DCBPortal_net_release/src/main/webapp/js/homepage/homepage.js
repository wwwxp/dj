//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    
    JsVar["interval"] = 30*1000;
    JsVar["prePaidGrid"] = mini.get("prePaidGrid");//取得任务表格
    JsVar["postPaidGrid"] = mini.get("postPaidGrid");//取得任务表格
   
    clearInterval(JsVar["timeTicket_search"]);
    JsVar["timeTicket_search"] = setInterval(search,JsVar["interval"]);
    search();
    getWarnLineConfigs();
   
    
    
});

/**
 * 在线表格点击事件
 */
function  preGridClick(){
	JsVar["preGrid_selected"]=JsVar["prePaidGrid"].getSelected();
	getPrePaidChartData();
	initPrePaidPieChart();
}

/**
 * 离线表格点击事件
 */
function  postGridClick(){
	JsVar["postGrid_selected"]=JsVar["postPaidGrid"].getSelected();
	getPostPaidChartData();
	initPostPaidPieChart();
}

/**
 * 在线表格加载完成事件
 */
function preGridOnload(e){
    var index =0;
	
	if(JsVar["preGrid_selected"]){
		var data=e.data;
		for(var i=0;i<data.length;i++){
			if(JsVar["preGrid_selected"]["NET_NAME"]==data[i]["NET_NAME"]){
				index=i;
				break;
			}
			
		}
	}
	
	this.select(index);
	JsVar["preGrid_selected"]=JsVar["prePaidGrid"].getSelected();
	getPrePaidChartData();
	initPrePaidPieChart();
}

/**
 * 离线表格加载完成事件
 */
function postGridOnload(e){
    var index =0;
	
	if(JsVar["postGrid_selected"]){
		var data=e.data;
		for(var i=0;i<data.length;i++){
			if(JsVar["postGrid_selected"]["MAPPING_TYPE"]==data[i]["MAPPING_TYPE"]){
				index=i;
				break;
			}
			
		}
	}
	
	this.select(index);
	
	JsVar["postGrid_selected"]=JsVar["postPaidGrid"].getSelected();
	getPostPaidChartData();
	initPostPaidPieChart();
}
/**
 * 查询在线和离线统计表格数据
 */
function search(){
//	 params["INTERVAL"]='10/60*24';
	var postStart = JsVar["postPaidTime"] == undefined ? "":JsVar["postPaidTime"]
	datagridLoadPage(JsVar["postPaidGrid"],{startTime:postStart},"monitorMapper.handlingCapacityGrid","","anotherDataSource");
	var preStart = JsVar["prePaidTime"] == undefined ? "":JsVar["prePaidTime"]
	datagridLoadPage(JsVar["prePaidGrid"],{startTime:preStart},"monitorMapper.queryDcfOmcPerfDataMinitorGrid","","anotherDataSource");
}

/**
 * 格式化显示
 */
function formatCharge(e){
	var charge = e.record["CHARGE"];
	return (Number(charge)/100).toFixed(2);
}
/**
 * 查询告警线值
 */
function getWarnLineConfigs(){
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"首页-在线监控min警告线",function(result){
		JsVar["warn_line"] = result;

		},"config.queryConfigList",null,false);
}

/**
 * 查询在线付费图表统计数据
 */
function getPrePaidChartData(){
if(!JsVar["preGrid_selected"]) return;
 getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"首页-在线监控图表",function(result){
	 var items=[];
		for(var i=0;i<result.length;i++){
			if(JsVar["preGrid_selected"]["NET_NAME"] == result[i]["NET_NAME"]){
				items.push(result[i]);
				
			}
		}
	 initPrePaidChart(items);
	},"monitorMapper.queryDcfOmcPerfDataMinitorCharts","anotherDataSource");
   hideLoadMask();
 
}

/**
 * 在线付费图表初始化
 * @param rows
 */
function initPrePaidChart(rows){
	if(!rows ||  rows.length<1) return ;
	var num=parseInt(rows.length/60);
	var legendData=[];
	var legendLength=0;
	
	
	for(var i=0;i<num;i++){
		var legend = rows[i*60]["NET_NAME"];
		legendLength+=legend.length;
		//长度超过80进行换行
		if(legendLength> 80){
			legendLength=legend.length;
			legendData.push("");
		}
		legendData.push(legend);
	}
	var series = [];
	
	for(var i=0;i<num;i++){
		var serie = new Object();
		serie["name"]=rows[i*60]["NET_NAME"];
		serie["type"]='line';
		
		var ONLINE_MIN_OMC = getMinWarnValue("ONLINE_MIN_OMC");
		if(ONLINE_MIN_OMC){
			serie["markLine"]={};
			serie["markLine"]["data"]=[];
			var minLine=[];
			var minLine_start={name:"最小值告警线",value:ONLINE_MIN_OMC ,xAxis:0,yAxis:ONLINE_MIN_OMC};
			var minLine_end={xAxis:60,yAxis:ONLINE_MIN_OMC};
			minLine.push(minLine_start);
			minLine.push(minLine_end);
			serie["markLine"]["data"].push(minLine);
		}
	
		
		serie["data"]=[];
		for(var j=0;j<60;j++){
			serie["data"].push(rows[60*i+j]["CCA_COUNT"]);
		}
		
		series.push(serie);
	}
	var xAxisData = [];
	
	for(var i=0;i<60;i++){
		xAxisData.push(rows[i]["MI_NONE_DATE"]);
	}
	
	 JsVar["prePaidChart"] = echarts.init(document.getElementById('prePaidChart'));
	   var option = {
		   title : {
		        text: '消息处理速率',
		        subtext: '近一小时【条/秒】',
		        textStyle:{
		        	fontSize: 14,
//			            fontWeight: 'bolder',
		            color: '#333'
		        }
		    },
		    tooltip : {
		        trigger: 'axis'
		    },
		    legend: {
		        data:legendData,
		        padding:10,
		        x:'center'
		    },
		    calculable : false,
		    grid:{
		    	x:50,
		    	y:60,
		    	x2:40,
		    	y2:40
		    },
		    xAxis : [
		        {
		            type : 'category',
		            boundaryGap : true,
		            data : xAxisData
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value'
		            
		        }
		    ],
		    series : series
		};
	 JsVar["prePaidChart"].setOption(option);
}

function initPrePaidPieChart(){
	JsVar["prePaidPieChart"] = echarts.init(document.getElementById('prePaidPieChart'));
	if(JsVar["preGrid_selected"] == undefined) return;
	
	option = {
		    title : {
		        text: '当天消息处理情况',
		        x:'left',
		        textStyle:{
		        	fontSize: 14,
//			            fontWeight: 'bolder',
		            color: '#333'
		        }
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter: "{a} <br/>{b} : {c} ({d}%)"
		    },
		   
		    toolbox: {
		        show : false
		    },
		    calculable : false,
		    legend: {
		        data:["<50","<100","<200","<500","<1000","<5000",">5000"],
		        padding:[30,10,10,10],
		        x:'right'
		    },
		    series : [
		        {
		            name:'当天话单处理情况',
		            type:'pie',
		            radius : '50%',
		            center: ['50%', '70%'],
		            itemStyle: {
		                normal: {
		                    label: {
		                        show: true,
		                        position:'outer',
		                        formatter:"{d}%"
		                    },
		                    labelLine: {
		                        show: true,
		                        length:1
		                    },
		                    color:
		                    	function(params){
		                    	var colors=["#32cd32","#87cefa","#6495ed","#ff69b4","#da70d6","#ffa500","#ff7f50"];
		                        return colors[params.dataIndex];									  
		                     }
		                }
		            },
		            data:[
		                {value:JsVar["preGrid_selected"]["DELAY_50"], name:'<50'},
		                {value:JsVar["preGrid_selected"]["DELAY_100"], name:'<100'},
		                {value:JsVar["preGrid_selected"]["DELAY_200"], name:'<200'},
		                {value:JsVar["preGrid_selected"]["DELAY_500"], name:'<500'},
		                {value:JsVar["preGrid_selected"]["DELAY_1000"], name:'<1000'},
		                {value:JsVar["preGrid_selected"]["DELAY_5000"], name:'<5000'},
		                {value:JsVar["preGrid_selected"]["DELAY_9999"], name:'>5000'}
		            ]
		        }
		    ]
		};
	JsVar["prePaidPieChart"].setOption(option);
}


/**
 * 获取离线付费图表数据
 */
function getPostPaidChartData(){
	if(!JsVar["postGrid_selected"]) return ;
	 getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"首页-在线监控图表",function(result){
		 var items=[];
			for(var i=0;i<result.length;i++){
				if(JsVar["postGrid_selected"]["MAPPING_TYPE"] == result[i]["MAPPING_TYPE"]){
					items.push(result[i]);
					
				}
			}
		 initPostPaidChart(items);
		},"monitorMapper.handlingCapacityCharts","anotherDataSource");
	 hideLoadMask();
}


/**
 * 离线图表初始化
 * @param rows
 */
function initPostPaidChart(rows){
	if(!rows ||  rows.length<1 ) return ;
	var num=parseInt(rows.length/60);
	
	var legendData=[];
	var legendLength=0;
	
	for(var i=0;i<num;i++){
		var legend = rows[i*60]["MAPPING_NAME"];
		//长度超过50进行换行
		legendLength+=legend.length;
		if(legendLength> 50){
			legendLength=legend.length;
			legendData.push("");
		}
		legendData.push(legend);
	}
	
	var series = [];
	
	for(var i=0;i<num;i++){
		var serie = new Object();
		serie["name"]=rows[i*60]["MAPPING_NAME"];
		serie["type"]='line';
		var OFFLINE_MIN_OMC = getMinWarnValue("OFFLINE_MIN_OMC");
		if(OFFLINE_MIN_OMC){
			serie["markLine"]={};
			serie["markLine"]["data"]=[];
			var minLine=[];
			var minLine_start={name:"最小值告警线",value:OFFLINE_MIN_OMC ,xAxis:0,yAxis:OFFLINE_MIN_OMC};
			var minLine_end={xAxis:60,yAxis:OFFLINE_MIN_OMC};
			minLine.push(minLine_start);
			minLine.push(minLine_end);
			serie["markLine"]["data"].push(minLine);
		}
		serie["data"]=[];
		for(var j=0;j<60;j++){
			serie["data"].push(rows[60*i+j]["HANDLE_SPEED"]);
		}
		
		series.push(serie);
	}
	var xAxisData = [];
	
	for(var i=0;i<60;i++){
		xAxisData.push(rows[i]["MI_NONE_DATE"]);
	}
	
	 JsVar["postPaidChart"] = echarts.init(document.getElementById('postPaidChart'));
	var option = {
			title : {
		        text: '话单处理速率',
		        subtext: '近一小时【条/秒】',
		        textStyle:{
		        	fontSize: 14,
//		            fontWeight: 'bolder',
		            color: '#333'
		        }
		    },
		    tooltip : {
		        trigger: 'axis'
//		        formatter: " {b}\n{a} : {c}条/秒 "
		    },
		    legend: {
		        data:legendData,
		        padding:10,
		        x:'center'
		    },
		    grid:{
		    	x:50,
		    	y:60,
		    	x2:40,
		    	y2:40
		    },
		    calculable : false,
		    xAxis : [
		        {
		            type : 'category',
		            boundaryGap : true,
		            data : xAxisData
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value'
		        }
		    ],
		    series : series
		};
	 JsVar["postPaidChart"].setOption(option);
}

function initPostPaidPieChart(){
	if(!JsVar["postGrid_selected"]) return;
	JsVar["postPaidPieChart"] = echarts.init(document.getElementById('postPaidPieChart'));
	
	
	option = {
		    title : {
		        text: '当天话单处理情况',
		        x:'left',
		        textStyle:{
		        	fontSize: 14,
//		            fontWeight: 'bolder',
		            color: '#333'
		        }
		    },
		    tooltip : {
		        trigger: 'item',
		        formatter: "{a} <br/>{b} : {c} ({d}%)"
		    },
		    legend: {
		        data:["正常话单","无效话单","异常话单","无主话单"],
		        padding:[30,10,10,10],
		        x:'right'
		    },
		    toolbox: {
		        show : false
		        
		    },
		    calculable : false,
		    
		    series : [
		        {
		            name:'当天话单处理情况',
		            type:'pie',
		            radius : '50%',
		            center: ['50%', '70%'],
		            itemStyle: {
		                normal: {
		                    label: {
		                        show: true,
		                        position:'outer',
		                        formatter:"{d}%"
		                    },
		                    labelLine: {
		                        show: true,
		                        length:1
		                    },
		                    color:
		                    	function(params){
		                    	var colors=["#32cd32","#ff69b4","#da70d6","#ffa500","#1e90ff"];
		                        return colors[params.dataIndex];									  
		                     }
		                }
		            },
		            data:[
		                {value:JsVar["postGrid_selected"]["NORMAL_RECORDS"], name:'正常话单'},
		                {value:JsVar["postGrid_selected"]["INVALID_RECORDS"], name:'无效话单'},
		                {value:JsVar["postGrid_selected"]["ABNORMAL_RECORDS"], name:'异常话单'},
		                {value:JsVar["postGrid_selected"]["NOUSER_RECORDS"], name:'无主话单'}
		            ]
		        }
		    ]
		};
	JsVar["postPaidPieChart"].setOption(option);
}
/**
 * 获取最小值告警线
 * @param key
 * @returns
 */
function getMinWarnValue(key){
	for(var i=0;i<JsVar["warn_line"].length;i++){
		if(JsVar["warn_line"][i]["CONFIG_NAME"] == key){
			return JsVar["warn_line"][i]["CONFIG_VALUE"];
		}
	}
	return null;
}

/**
 * 重新设置离线统计开始时间（当前系统时间）
 */
function resetPostPaidTime(){
	JsVar["postPaidTime"]= (new Date()).format("yyyyMMddhhmmss");
	var postStart = JsVar["postPaidTime"] == undefined ? "":JsVar["postPaidTime"]
	datagridLoadPage(JsVar["postPaidGrid"],{startTime:postStart},"monitorMapper.handlingCapacityGrid","","anotherDataSource");
	

}

/**
 * 重新设置在线统计开始时间（当前系统时间）
 */
function resetPrePaidTime(){
	JsVar["prePaidTime"]= (new Date()).format("yyyyMMddhhmmss");
	var preStart = JsVar["prePaidTime"] == undefined ? "":JsVar["prePaidTime"]
	datagridLoadPage(JsVar["prePaidGrid"],{startTime:preStart},"monitorMapper.queryDcfOmcPerfDataMinitorGrid","","anotherDataSource");

}