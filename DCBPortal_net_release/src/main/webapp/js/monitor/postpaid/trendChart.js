//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    Bar();
});

/**
 * 父窗口弹出调用方法
 * @param params
 */
function onLoadComplete(params){
	
}

function Bar(){
	  getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"",function (result){
		  initBarChart(result);
	      },"monitorMapper.queryLogRatingLineDayData","anotherDataSource");
	  
}

function initBarChart(rows){
	if(!rows ||  rows.length<1) return ;
	var legendData=["话单量"];
	var legendLength=0;
	var series = [];
	var serie = new Object();
	serie["name"]="话单量";
	serie["type"]='bar';
	serie["data"]=[];
	for(var i=0;i<rows.length;i++){
		serie["data"].push(rows[i]["TOTAL_RECORDS"]);
	}
	serie["markPoint"]= {  data : [
					            {type : 'max', name: '最大值'},
					            {type : 'min', name: '最小值'}
					        ]
					    };
	serie["markLine"] = { data : [
					            {type : 'average', name: '平均值'}
					        ]
					    };
	series.push(serie);
	var xAxisData = [];
	
	for(var i=0;i<rows.length;i++){
		xAxisData.push(rows[i]["MI_NONE_DATE"]);
	}
	JsVar["trendChart"] = echarts.init(document.getElementById('trendChart'));
	 var option = {
			   title : {
//			        text: '当天话单处理量',
			        textStyle:{
			        	fontSize: 14,
//				            fontWeight: 'bolder',
			            color: '#333'
			        }
			    },
			    tooltip : {
			        trigger: 'axis'
			    },
			    legend: {
			        data:legendData,
			        padding:20,
			        x:'center'
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
	JsVar["trendChart"].setOption(option);
}
