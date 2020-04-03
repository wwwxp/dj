//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");
    JsVar["resourceGrid"] = mini.get("resourceGrid");//取得任务表格
    JsVar["resource_initPage"] = true;
    clearInterval(JsVar["timeTicket_resourceGrid"]);
    JsVar["timeTicket_resourceGrid"] = setInterval(search,5000);
    search(true);
    
});

function search(){
//	if(isClear){
//		clearInterval(JsVar["timeTicket_resourceGrid"]);
//	}
	var params=JsVar["queryFrom"].getData();
	 params = mini.clone(params);
	 
	datagridLoadPage(JsVar["resourceGrid"],params,"monitorMapper.queryDcf_resource");
}


function getResourceChartsData(resource_id){
	var row=JsVar["resourceGrid"].getSelected();
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_BUSINESS_HOST_RESOURCE_URL,{RESOURCE_ID:resource_id+""},"主机资源监控数据",function(result){
		if(JsVar["resource_initPage"] == true){
			JsVar["resource_initPage"]=false;
			JsVar["paintChartData"]=result;
			paintCharts(result);
			gaugeChart(row);
			getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{HOST:row["HOST"]},"主机磁盘资源监控",function(result){
				initDiskChart(result);
			},"monitorMapper.queryHostDiskInfo");
			
		}else{
			if(!result) return;
			var newRows;
			if(JsVar["paintChartData"].length == result.length){
				var index =0;
				for(var i=0;i<JsVar["paintChartData"].length;i++){
					if(JsVar["paintChartData"][i]["REPORT_TIME"]== result[0]["REPORT_TIME"]){
						index=i;
						break;
					}
				}
				 newRows=result.slice(0);
			     newRows.splice(0,result.length-index);
			}else{
				 newRows=result.slice(0);
				 newRows.splice(0,JsVar["paintChartData"].length);
			}
			
			
			var append=false;
			if(JsVar["paintChartData"].length != result.length){
				append=true;
			}
			for(var i=0;i<newRows.length;i++){
				
				    var addDatas = [];
					addDatas.push([0,newRows[i]["CPU"],false,append,newRows[i]["REPORT_TIME_ONLY"]]);
					addDatas.push([1,newRows[i]["MEM_USE"],false,append]);
					addDatas.push([2,newRows[i]["DISK_USE"],false,append]);
					addDatas.push([3,newRows[i]["PROC_NUM"],false,append]);
					JsVar["resourceChart"].addData(addDatas);
			}
			JsVar["paintChartData"]=result;
			//仪表
			JsVar["gaugeOption"].series[0].data[0].value = Number(row["CPU"]).toFixed(2);
			JsVar["gaugeOption"].series[1].data[0].value =  (Number(row["MEM_USE"])/Number(row["MEM_TOTAL"])*100).toFixed(2);
			JsVar["gaugeOption"].series[2].data[0].value = (Number(row["DISK_USE"])/Number(row["DISK_TOTAL"])*100).toFixed(2);
			JsVar["gaugeChart"].setOption(JsVar["gaugeOption"],true);
			
			getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{HOST:row["HOST"]},"主机磁盘资源监控",function(result){
				var disk_used=[];
				var disk_unused=[];
				var disk_percent=[];
				var disk_total=[];
				for(var i=0;i<result.length;i++){
					disk_used.push(result[i]["DISK_USED"]);
					disk_unused.push(Number(result[i]["DISK_TOTAL"])-Number(result[i]["DISK_USED"]));
					disk_percent.push(result[i]["DISK_PERCENT"]);
					disk_total.push(result[i]["DISK_TOTAL"]);
				}
				JsVar["diskChartOption"].series[0].data=disk_used;
				JsVar["diskChartOption"].series[1].data=disk_unused;
				JsVar["diskChartOption"].series[2].data=disk_percent;
				JsVar["diskChartOption"].series[3].data=disk_total;
				JsVar["diskChart"].setOption(JsVar["diskChartOption"],true);
			},"monitorMapper.queryHostDiskInfo");
			hideLoadMask();
		}
		
	});
	
	hideLoadMask();
	
	
}
/**
 * 表格加载后选中
 * @param e
 */
function gridOnload(e){
	var index =0;
	
	if(JsVar["select_row"]){
		var data=e.data;
		for(var i=0;i<data.length;i++){
			if(JsVar["select_row"]["RESOURCE_ID"]==data[i]["RESOURCE_ID"]){
				index=i;
				break;
			}
			
		}
	}
	
	this.select(index);
	
	var row=JsVar["resourceGrid"].getSelected();
	
	getResourceChartsData(row["RESOURCE_ID"]);
}

/**
 * 画图展示
 * @param port
 */
function paintCharts(datas){
	 var mapping ={};
	     mapping["CPU使用率(%)"] = "CPU";
	     mapping["内存总量(MB)"] = "MEM_TOTAL";
	     mapping["内存使用量(MB)"] = "MEM_USE";
	     mapping["磁盘总量(GB)"] = "DISK_TOTAL";
	     mapping["磁盘使用量(GB)"] = "DISK_USE";
	     mapping["业务进程数"] = "PROC_NUM";
	if(datas==undefined) return;
	 var legendData= ["CPU使用率(%)","内存使用量(MB)","磁盘使用量(GB)","业务进程数"];
	 
	 JsVar["dateTimes"]=[];
	 var series =[];
	 for(var i=0;i<legendData.length;i++){
		 var column=legendData[i];
		 var serie = new Object();
		 serie["name"]=column;
		 serie["type"]="line";
		 serie["stack"]="总量";
		 serie["data"]=[];
		 for(var j=0;j<datas.length;j++){
			 if(i == 0){
				 JsVar["dateTimes"].push(datas[j]["REPORT_TIME_ONLY"]);
			 }
			 var tmp=datas[j][mapping[column]];
			 serie["data"].push(tmp);
		 }
		 series.push(serie);
	 }
	
	var myChart = echarts.init(document.getElementById('mainChart'));
	JsVar["resourceChart"]=myChart;
	myChart.on(echarts.config.EVENT.LEGEND_SELECTED,function (e){
	  JsVar["select_legend_item"]=e["target"];
	});
	var option = {
		    tooltip : {
		        trigger: 'axis'
		    },
		    legend: {
		        data:legendData,
		        selectedMode:"single"
		    },
		    calculable : true,
		    xAxis : [
		        {
		            type : 'category',
		            boundaryGap : false,
		            data : JsVar["dateTimes"]
		        }
		    ],
		    grid:{
		    	x:50,
		    	y:30,
		    	x2:40,
		    	y2:30
		    },
		    yAxis : [
		        {
		            type : 'value'
		        }
		    ],
		    series : series
		};
     myChart.setOption(option);
     var legendInstance = myChart.component.legend;
     if(JsVar["select_legend_item"]){
    	 legendInstance.setSelected(JsVar["select_legend_item"],true);
     }else{
    	 legendInstance.setSelected("CPU使用率(%)",true);
     }
     
//   legendInstance.setSelected("业务进程数",true);
    
}

/**
 * 根据选择行重新画图
 */
function onclickRow(){
	var row=JsVar["resourceGrid"].getSelected();
	JsVar["select_row"]=row;
	JsVar["resource_initPage"] = true;
	getResourceChartsData(row["RESOURCE_ID"]);
}

/**
 * 实时仪表展示（内存使用率，CPU使用率，磁盘使用率）
 */
function gaugeChart(row){
	if(row==undefined) return;
	var gaugeChart = echarts.init(document.getElementById('gaugeChart'));
	JsVar["gaugeChart"]=gaugeChart;
	var option = {
		    tooltip : {
		        formatter: "{a}:<br/> {c}%"
		    },
		    toolbox: {
		        show : true,
		        feature : {
		            mark : {show: false},
		            restore : {show: false},
		            saveAsImage : {show: true}
		        }
		    },
		    series : [
		        {
		            name:'CPU使用率',
		            type:'gauge',
		            z: 3,
		            min:0,
		            max:100,
		            splitNumber:10,
		            axisLine: {            // 坐标轴线
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    width: 10
		                }
		            },
		            axisTick: {            // 坐标轴小标记
		                length :15,        // 属性length控制线长
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    color: 'auto'
		                }
		            },
		            splitLine: {           // 分隔线
		                length :20,         // 属性length控制线长
		                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
		                    color: 'auto'
		                }
		            },
		            title : {
		            	show:true,
		            	offsetCenter: ['0%','100%'],
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    fontWeight: 'bolder',
		                    fontSize: 18
		                }
		            },
		            detail : {
		                show : true,
		                backgroundColor: 'rgba(0,0,0,0)',
		                borderWidth: 0,
		                borderColor: '#ccc',
		                width: 100,
		                height: 40,
		                formatter:'{value}%',
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    color: 'auto',
		                    fontWeight: 'bolder',
		                    fontSize : 25
		                }
		            },
		            data:[{value: Number(row["CPU"]).toFixed(2), name: 'CPU使用率'}]
		        },
		        {
		            name:'内存使用率',
		            type:'gauge',
		            center : ['25%', '55%'],    // 默认全局居中
		            radius : '50%',
		            min:0,
		            max:100,
		            endAngle:45,
		            splitNumber:5,
		            axisLine: {            // 坐标轴线
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    width: 8
		                }
		            },
		            axisTick: {            // 坐标轴小标记
		                length :15,        // 属性length控制线长
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    color: 'auto'
		                }
		            },
		            splitLine: {           // 分隔线
		                length :20,         // 属性length控制线长
		                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
		                    color: 'auto'
		                }
		            },
		            pointer: {
		                width:5
		            },
		            title : {
		            	show:true,
		            	offsetCenter: ['0%','130%'],
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    fontWeight: 'bolder',
		                    fontSize: 18
		                }
		            },
		            detail : {
		                show : true,
		                backgroundColor: 'rgba(0,0,0,0)',
		                borderWidth: 0,
		                borderColor: '#ccc',
		                width: 100,
		                height: 40,
		                formatter:'{value}%',
		                offsetCenter: [10, '40%'],
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    color: 'auto',
		                    fontWeight: 'bolder',
		                    fontSize : 25
		                }
		            },
		            data:[{value: (Number(row["MEM_USE"])/Number(row["MEM_TOTAL"])*100).toFixed(2), name: '内存使用率'}]
		        },
		        {
		            name:'磁盘使用率',
		            type:'gauge',
		            center : ['75%', '55%'],    // 默认全局居中
		            radius : '50%',
		            min:0,
		            max:100,
		            startAngle:135,
		            endAngle:-45,
		            splitNumber:5,
		            axisLine: {            // 坐标轴线
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    width: 8
		                }
		            },
		            axisTick: {            // 坐标轴小标记
		                splitNumber:5,
		                length :15,        // 属性length控制线长
		                lineStyle: {       // 属性lineStyle控制线条样式
		                    color: 'auto'
		                }
		            },
		            splitLine: {           // 分隔线
		                length :20,         // 属性length控制线长
		                lineStyle: {       // 属性lineStyle（详见lineStyle）控制线条样式
		                    color: 'auto'
		                }
		            },
		            pointer: {
		                width:2
		            },
		            title : {
		            	show:true,
		            	offsetCenter: ['0%','130%'],
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    fontWeight: 'bolder',
		                    fontSize: 18
		                }
		            },
		            detail : {
		                show : true,
		                backgroundColor: 'rgba(0,0,0,0)',
		                borderWidth: 0,
		                borderColor: '#ccc',
		                width: 100,
		                height: 40,
		                formatter:'{value}%',
		                offsetCenter: [-10, '40%'],
		                textStyle: {       // 其余属性默认使用全局文本样式，详见TEXTSTYLE
		                    color: 'auto',
		                    fontWeight: 'bolder',
		                    fontSize : 25
		                }
		            },
		            data:[{value: (Number(row["DISK_USE"])/Number(row["DISK_TOTAL"])*100).toFixed(2), name: '磁盘使用率'}]
		        }
		    ]
		};
	   gaugeChart.setOption(option);
	   JsVar["gaugeOption"]=option;
		
		
		
}

function initDiskChart(results){
	var row=JsVar["resourceGrid"].getSelected();
	var diskChart = echarts.init(document.getElementById('diskChart'));
	JsVar["diskChart"]=diskChart;
	var mapping ={};
	var legendData=["未使用量(MB)","已使用量(MB)","使用率(%)","总量(MB)"];
	
	var series = [];
	
	var serie = new Object();
	serie["name"]="已使用量(MB)";
	serie["type"]='bar';
	serie["stack"]='总量(MB)';
	serie["itemStyle"]={normal:{color:"#ff7f50"}},
	serie["data"]=[];
	for(var i=0;i<results.length;i++){
		serie["data"].push(results[i]["DISK_USED"]);
	}
	series.push(serie);
	
	
	
	serie={};
	serie["name"]="未使用量(MB)";
	serie["type"]='bar';
	serie["stack"]='总量(MB)';
	serie["itemStyle"]={normal:{color:"#32cd32"}},
	serie["data"]=[];
	for(var i=0;i<results.length;i++){
		serie["data"].push(Number(results[i]["DISK_TOTAL"])-Number(results[i]["DISK_USED"]));
	}
	series.push(serie);
	
	serie={};
	serie["name"]="使用率(%)";
	serie["type"]='line';
	serie["yAxisIndex"]=1;
	serie["itemStyle"]={normal:{color:"#da70d6"}},
	serie["data"]=[];
	for(var i=0;i<results.length;i++){
		serie["data"].push(results[i]["DISK_PERCENT"]);
	}
	series.push(serie);
	
	serie={};
	serie["name"]="总量(MB)";
	serie["type"]='bar';
	serie["itemStyle"]={normal:{color:"#87cefa"}},
	serie["data"]=[];
	for(var i=0;i<results.length;i++){
		serie["data"].push(results[i]["DISK_TOTAL"]);
	}
	series.push(serie);
	
	var xAxisData = [];
	for(var i=0;i<results.length;i++){
		xAxisData.push(results[i]["DISK_NAME"]);
	}
	
	option = {
			title : {
		        text: '主机【'+row["HOST"]+'】磁盘使用情况',
		        x:'left',
		        textStyle:{
		        	fontSize: 14,
//			            fontWeight: 'bolder',
		            color: '#333'
		        }
		    },
		    tooltip : {
		        trigger: 'axis',
		        axisPointer : {            // 坐标轴指示器，坐标轴触发有效
		            type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
		        }
		    },
//		    tooltip : {
//		        formatter: "{b}<br/>{a}:{c} MB"
//		    },
		    legend: {
		        data:legendData
		    },
		    grid:{
		    	x:80,
		    	y:60,
		    	x2:80,
		    	y2:100
		    },
		    toolbox: {
		        show : false,
		        orient: 'vertical',
		        x: 'right',
		        y: 'center',
		        feature : {
		            dataView : {show: true, readOnly: false},
		            magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
		            restore : {show: true},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : false,
		    xAxis : [
		        {
		            type : 'category',
		            axisLabel:{rotate:-20},
		            data : xAxisData
		        }
		    ],
		    yAxis : [
		             {
		                 type : 'value',
		             },
		             {
		                 type : 'value',
		                 axisLabel : {
		                     formatter: '{value} %'
		                 }
		             }
		         ],
		    series : series
		};
		                    
	JsVar["diskChartOption"]=option;
	diskChart.setOption(option);
	
}