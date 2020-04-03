//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["prePaidGrid"] = mini.get("prePaidGrid");//取得任务表格
    JsVar["resultCodeGrid"] = mini.get("resultCodeGrid");//取得任务表格
    JsVar["prePaidGridOnloadInit"]=true;
    search();
    setInterval(prePaidGridReload,5000);
});

function search(){
	datagridLoadPage(JsVar["prePaidGrid"],{},"monitorMapper.queryDcfOmcPerfData","","anotherDataSource");
}

function prePaidGridReload(){
	JsVar["prePaidGrid"].reload();
}
function openDetail(){
	 showDialog("明细查询","1200","550",Globals.baseJspUrl.MONITOR_ACTION_PRE_PAID_HISTORY_JSP,function(){
			
		});

}


function openDetailInclude(){
	 showDialog("汇总统计","1200","550",Globals.baseJspUrl.MONITOR_ACTION_PRE_PAID_INCLUDE_JSP,function(){
			
		});

}

function prePaidGridClick(){
	JsVar["prePaidGrid_selected"] = JsVar["prePaidGrid"].getSelected();
	loadResultCodeGrid();
	initPrePaidPieChart();
}


function prePaidGridOnload(e){
	 var index =0;
		
		if(JsVar["prePaidGrid_selected"]){
			var data=e.data;
			for(var i=0;i<data.length;i++){
				if(JsVar["prePaidGrid_selected"]["NET_NAME"]==data[i]["NET_NAME"]){
					index=i;
					break;
				}
				
			}
		}
		
		this.select(index);
		JsVar["prePaidGrid_selected"]=JsVar["prePaidGrid"].getSelected();
		if(JsVar["prePaidGridOnloadInit"]){
			loadResultCodeGrid();
		}else{
			JsVar["resultCodeGrid"].reload();
			JsVar["prePaidGridOnloadInit"]=false;
		}
		initPrePaidPieChart();
		
}

function resultCodeGridClick(){
	JsVar["resultCodeGrid_selected"] = JsVar["resultCodeGrid"].getSelected();
}

function resultCodeGridOnload(e){
	 var index =0;
		
		if(JsVar["resultCodeGrid_selected"]){
			var data=e.data;
			for(var i=0;i<data.length;i++){
				if(JsVar["resultCodeGrid_selected"]["RESULT_CODE"]==data[i]["RESULT_CODE"]){
					index=i;
					break;
				}
				
			}
		}
		
		this.select(index);
		JsVar["resultCodeGrid_selected"]=JsVar["resultCodeGrid"].getSelected();
	
}
function loadResultCodeGrid(){
	datagridLoadPage(JsVar["resultCodeGrid"],JsVar["prePaidGrid_selected"],"monitorMapper.queryCodeRemarks","","anotherDataSource");
}


function initPrePaidPieChart(){
	JsVar["prePaidPieChart"] = echarts.init(document.getElementById('prePaidPieChart'));
	if(JsVar["prePaidGrid_selected"] == undefined) return;
	
	option = {
		    title : {
		        text: '实时话单处理情况',
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
		        data:["<50 ms","<100 ms","<200 ms","<500 ms","<1000 ms","<5000 ms",">5000 ms"],
		        padding:[30,10,10,10],
		        x:'center'
		    },
		    series : [
		        {
		            name:'实时消息处理情况',
		            type:'pie',
		            radius : '50%',
		            center: ['50%', '70%'],
		            itemStyle: {
		                normal: {
		                    label: {
		                        show: true,
		                        position:'outer',
		                        formatter:"{b} 时延 : {c}  ({d}%)"
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
		                {value:JsVar["prePaidGrid_selected"]["DELAY_50"], name:'<50 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_100"], name:'<100 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_200"], name:'<200 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_500"], name:'<500 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_1000"], name:'<1000 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_5000"], name:'<5000 ms'},
		                {value:JsVar["prePaidGrid_selected"]["DELAY_9999"], name:'>5000 ms'}
		            ]
		        }
		    ]
		};
	JsVar["prePaidPieChart"].setOption(option);
}


function trendChart(){
	showDialog("在线话单当天消息处理趋势","1000","500",Globals.baseJspUrl.MONITOR_ACTION_PREPAID_PRETRENDCHART_JSP,function(){
			
		},{});
	}