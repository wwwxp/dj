//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["hostGrid"] = mini.get("hostGrid");//取得任务表格
    JsVar["workerGrid"] = mini.get("workerGrid");//取得任务表格
    search();
});


function onSelectHost(){
	var row=JsVar["hostGrid"].getSelected();
	initWorkersMetrics({clusterName:clusterName,host:row["host"]});
}

function gridOnload(e){
	this.select(0);
	var row=JsVar["hostGrid"].getSelected();
	initWorkersMetrics({clusterName:clusterName,host:row["host"]});
}
/**
 * 查询
 */
function search() {
	datagridLoadPage(JsVar["hostGrid"],{clusterName:clusterName},"",Globals.baseActionUrl.MONITOR_ACTION_RESOURCE_SUMMARY_URL);
}

/**
 * 格式化端口显示
 * @param e
 * @returns {String}
 */
function formatPort(e){
	return e.record.numUsedWorkers+"/"+e.record.numWorkers;
}
/**
 * 格式化配置字段显示
 * @param e
 * @returns {String}
 */
function getConfig(e){
	return  '<a class="Delete_Button" href="javascript:showConfig()">查看</a>';
}

/**
 * 格式化workers字体显示
 * @param e
 * @returns {String}
 */
function workers(e){
	return  '<a class="Delete_Button" href="javascript:showWorkers(\''+e.record.host+'\')">'+e.record.host+'</a>';
}

/**
 * 显示nimbus配置
 */
function showConfig(){
	showDialog("Nimbus Conf","800","550",Globals.baseJspUrl.MONITOR_ACTION_RESOURCE_CONFIGURATION_JSP,function(){
		
	},{clusterName:clusterName});
	 
	
}

/**
 * 弹窗显示Workers信息
 * @param host
 */
function showWorkers(host){
showDialog("Worker Metrics","1000","550",Globals.baseJspUrl.MONITOR_ACTION_RESOURCE_WORKER_METRICE_JSP,function(){
		
	},{clusterName:clusterName,host:host});


}


function initWorkersMetrics(params){
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_RESOURCE_WORKER_METRICS_URL,params,"测试",function(result){
		setColunms(result["workerHead"],result["workerHeadWrap"]);
		var workerMetrics=result["workerMetrics"];
	    var list=[];
	    for(var i=0;i<workerMetrics.length;i++){
	    	var row=workerMetrics[i]["metrics"];
	    	row["Port"]=workerMetrics[i]["port"];
	    	list.push(workerMetrics[i]["metrics"]);
	    }
	    JsVar["workerGrid"].setData(list);
	    //默认选中第一行
	    JsVar["workerGrid"].setSelected(list[0]);
	},null,null,false);
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_RESOURCE_WORKER_CHARTS_URL,params,"测试",function(result){
		if(result  == undefined){
			return;
		}
		JsVar["workerMetrics"]=result;
		JsVar["dateTimes"]=[] ;
		for(var i=0;i<result.length;i++){
			JsVar["dateTimes"].push(result[i]["dataTime"]);
			var workerMetrics=result[i]["workerMetrics"];
			for(var j=0;j<workerMetrics.length;j++){
				var port=workerMetrics[j].port;
				if(JsVar[port] == undefined ||JsVar[port] == null){
					JsVar[port]=[];
					JsVar[port].push(workerMetrics[j].metrics);
				}else{
					JsVar[port].push(workerMetrics[j].metrics);
				}
			}
		}
		var row=JsVar["workerGrid"].getSelected();
		paintCharts(row["Port"]);
//		$("#test").html(JSON.stringify(result));
		
	},null,null,false);
	
}


/**
 * 设置表格列
 * @param columns
 * @param columnsWrap
 */
function setColunms(columns,columnsWrap){
	col_arr=[];
	var type= new Object();
	type["type"]="checkcolumn";
	type["header"]="";
	type["width"]=4;
	var port= new Object();
	port["header"]="Port";
	port["headerAlign"]="center";
	port["align"]="center";
	port["width"]=10;
	port["field"]="Port"
	col_arr.push(type);
	col_arr.push(port);
	if(columns.length>0){
		for(var i=0;i<columns.length;i++){
			var column=new Object();
			column["field"]=columns[i]
			column["header"]=columnsWrap[i];
			column["headerAlign"]="center";
			column["align"]="right";
			column["width"]=columnsWrap[i].length;
			col_arr.push(column)
		}
	}
	JsVar["workerGrid"].set({ columns: col_arr });
}

/**
 * 画图展示
 * @param port
 */
function paintCharts(port){
	 var legendData= [];
	 var datas=JsVar[port];
	 if(datas==undefined) return;
	 for( var key  in  datas[0] ){
		 legendData.push(key);
		}
	 
	 var series =[];
	 for(var i=0;i<legendData.length;i++){
		 var column=legendData[i];
		 var serie = new Object();
		 serie["name"]=column;
		 serie["type"]="line";
		 serie["stack"]="总量";
		 serie["data"]=[];
		 //达不到数据量的用空字符串填充
		 if(JsVar["dateTimes"].length >datas.length){
			 var lengh =JsVar["dateTimes"].length-datas.length;
			 for(var k=0;k<lengh;k++){
				 serie["data"].push("");
			 }
		 }
		 for(var j=0;j<datas.length;j++){
			 
			 var tmp=datas[j][column];
			 tmp=tmp.replaceAll(",","").replaceAll("MB","");
			 var reg=/GB$/;
			 if(reg.test(tmp)){
				 tmp=tmp.replaceAll(" ","").replaceAll("GB","");
				 tmp=Number(tmp)*1024;
			 }
			 serie["data"].push(tmp);
			
		 }
		 series.push(serie);
	 }
	
	var myChart = echarts.init(document.getElementById('mainChart'));
	var option = {
		    tooltip : {
		        trigger: 'axis'
		    },
		    legend: {
		        data:legendData,
		        selectedMode:"single"
		    },
		    grid:{
		    	x:50,
		    	y:60,
		    	x2:40,
		    	y2:40
		    },
		    calculable : true,
		    xAxis : [
		        {
		            type : 'category',
		            boundaryGap : false,
		            data : JsVar["dateTimes"]
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value'
		        }
		    ],
		    series : series
		};
    myChart.setOption(option);
    var legendInstance = myChart.component.legend;
    legendInstance.setSelected(legendData[0],true);
}

/**
 * 根据选择行重新画图
 */
function repaintCharts(){
	var row=JsVar["workerGrid"].getSelected();
	paintCharts(row["Port"]);
}