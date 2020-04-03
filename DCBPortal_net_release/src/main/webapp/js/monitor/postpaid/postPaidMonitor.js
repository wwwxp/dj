//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");
    JsVar["postPaidGrid"] = mini.get("postPaidGrid");//取得任务表格
    JsVar["oper_type"]= mini.get("OPER_TYPE");
    queryOperType();
    search();
});

function search(){
	var params=JsVar["queryFrom"].getData();
	 params = mini.clone(params);
	 params["BEGIN_TIME"]=mini.formatDate(params["BEGIN_TIME"],'yyyy-MM-dd HH:mm:ss');
	 params["END_TIME"]=mini.formatDate(params["END_TIME"],'yyyy-MM-dd HH:mm:ss');
	 
	datagridLoadPage(JsVar["postPaidGrid"],params,"monitorMapper.queryLogRating","","anotherDataSource");
	ininitPieChar();
}

function queryOperType(){
	comboxLoad(JsVar["oper_type"],{},"monitorMapper.queryOperType",null,"anotherDataSource");
}



function ininitPieChar(){
	  getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{PROCER_ID:'A'},"",function (result){
		initFormatPieChart(result.length == undefined ?{}:result[0]);
	      },"monitorMapper.queryLogRatingLineHourData","anotherDataSource");
	  getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{PROCER_ID:'C'},"",function (result){
		initPricingPieChart(result.length == undefined ?{}:result[0]);
		  },"monitorMapper.queryLogRatingLineHourData","anotherDataSource");
}

function trendChart(){
showDialog("离线话单当天处理趋势","1000","400",Globals.baseJspUrl.MONITOR_ACTION_POSTPAID_TREND_JSP,function(){
		
	},{});
}

function initFormatPieChart(inputData){
	if(!inputData) return;
	JsVar["formatPieChart"] = echarts.init(document.getElementById('formatPieChart'));
	
	option = {
		    title : {
		        text: '格式化环节话单处理量',
		        subtext: '近一小时',
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
		        show : false,
		    },
		    calculable : false,
		    legend: {
		        data:["正常话单","无效话单","异常话单","无主话单"],
		        padding:[30,10,10,10],
		        x:'center'
		    },
		    series : [
		        {
		            name:'话单处理量',
		            type:'pie',
		            radius : '50%',
		            center: ['50%', '70%'],
		            itemStyle: {
		                normal: {
		                    label: {
		                        show: true,
		                        position:'outer',
		                        formatter:"{b} 数量 : {c}  ({d}%)"
		                    },
		                    labelLine: {
		                        show: true,
		                        length:1
		                    },
		                    color:
		                    	function(params){
		                    	var colors=["#32cd32","#87cefa","#6495ed","#ff69b4","#da70d6","#ba55d3","#ff7f50"];
		                        return colors[params.dataIndex];									  
		                     }

		                }
		            },
		            data:[
		                {value:inputData["NORMAL_RECORDS"], name:'正常话单'},
		                {value:inputData["INVALID_RECORDS"], name:'无效话单'},
		                {value:inputData["ABNORMAL_RECORDS"], name:'异常话单'},
		                {value:inputData["NOUSER_RECORDS"], name:'无主话单'}
		            ]
		        }
		    ]
		};
	JsVar["formatPieChart"].setOption(option);
}


function initPricingPieChart(inputData){
	JsVar["pricingPieChart"] = echarts.init(document.getElementById('pricingPieChart'));
	option = {
		    title : {
		        text: '批价环节话单处理量',
		        subtext: '近一小时',
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
		        show : false,
		    },
		    calculable : false,
		    legend: {
		        data:["正常话单","无效话单","异常话单","无主话单"],
		        padding:[30,10,10,10],
		        x:'center'
		    },
		    series : [
		        {
		            name:'话单处理量',
		            type:'pie',
		            radius : '50%',
		            center: ['50%', '70%'],
		            itemStyle: {
		                normal: {
		                    label: {
		                        show: true,
		                        position:'outer',
		                        formatter:"{b} 数量 : {c}  ({d}%)"
		                    },
		                    labelLine: {
		                        show: true,
		                        length:1
		                    },
		                    color:
		                    	function(params){
		                    	var colors=["#32cd32","#87cefa","#6495ed","#ff69b4","#da70d6","#ba55d3","#ff7f50"];
		                        return colors[params.dataIndex];									  
		                     }

		                }
		            },
		            data:[
		                {value:inputData["NORMAL_RECORDS"], name:'正常话单'},
		                {value:inputData["INVALID_RECORDS"], name:'无效话单'},
		                {value:inputData["ABNORMAL_RECORDS"], name:'异常话单'},
		                {value:inputData["NOUSER_RECORDS"], name:'无主话单'}
		            ]
		        }
		    ]
		};
	
	JsVar["pricingPieChart"].setOption(option);
}

/**
 * 当前话单积压量查询
 */
function queryOverstock(){
	  getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,{},"",function (result){
          showMessageAlter("当前话单【"+result["NOW_TIME"]+"】积压量为：<span class='fred'>"+result["NUM"]+"</span>");
		      },"monitorMapper.queryOverstock","anotherDataSource");
	
}
