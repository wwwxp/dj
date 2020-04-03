/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
	mini.parse();
	JsVar["aLineChart"] = echarts.init(document.getElementById('aLineChart'));
});
function initChart(data){
	var datalist = data.DATA;
	var xData = [];
	var seriesData = [];
	for(var i = 0 ; i < datalist.length;i++){
		xData.push(datalist[i]["BATCH_NO"]);
		seriesData.push(datalist[i]["PREDICTION_VALUE"]);
	}
	var legendData = [];
	var unit = '%';
	var type = data.QUOTA_TYPE;
	if(type == '1'){
		legendData.push("CPU");
	}else if(type =='2'){
		legendData.push("内存");
	}else if(type =='3'){
		legendData.push("磁盘");
	}else{
		legendData.push("业务量");
		unit = '条';
	}
	var seriesName = legendData[0];
	option = {
			title: {
		        text: '预测收缩数据分析趋势图',
		        textStyle:{
		               fontSize:14
		        }
		    },
		    xAxis: {
		        type: 'category',
		        data: xData
		    },
		    yAxis: {
		        type: 'value',
		        axisLabel : {  
                    formatter: '{value}'+unit  
                },
		    },
		    tooltip: {
		        trigger: 'axis',
		        formatter:seriesName+'：{c}'+unit+'<br/>批次：{b}'
		    },
		    grid:{
                x:50,
                y:35,
                x2:5,
                y2:45,
                borderWidth:1
            },
            legend: {
                data:legendData
            },
		    series: [{
		    	name : seriesName,
		        data: seriesData,
		        type: 'line',
		        markLine : {
	                data : [
	                    {type : 'max', name : '平均值',value:2}
	                ]
	            }
		    }]
		};
	JsVar["aLineChart"].setOption(option,true);
}

function onLoadComplete(data) {
	JsVar["data"] = data;
	 $('#CPU').html(JsVar["data"]["CPU"]+"%");
	 $('#MEM').html(JsVar["data"]["MEM"]+"%");
	 $('#DISK').html(JsVar["data"]["DISK"]+"%");
	 $('#BUSS_VOLUME').html(JsVar["data"]["BUSS_VOLUME"]);
	 $('#PREDICTION_TIME').html(mini.formatDate(JsVar["data"]["PREDICTION_TIME"],"yyyy-MM-dd"));
	 $('#CRT_DATE').html(mini.formatDate(JsVar["data"]["CRT_DATE"],"yyyy-MM-dd HH:mm:ss"));
	 var  PREDICTION_DATA= JsVar["data"]["PREDICTION_DATA"];
	 if(!isNull(PREDICTION_DATA)){
		 var jsonData = JSON.parse(PREDICTION_DATA);
		 if(jsonData.DATA.length <1){
			 $('#chartDiv').height(0);
		 }else{
			 initChart(jsonData);
		 }
		  
	 }else{
		 $('#chartDiv').height(0);
	 }
	 var status = JsVar["data"]["STATUS"];
	 if(status == '0'){
		 $('#k2').show();
	 }else{
		 $('#k1').show();
	 }
	 getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,data, "",
		        function success(result){
					 for(var i = 0 ; i < result.length ;i++){
						 if(result[i]["QUOTA_TYPE"] == '1'){
								 $('#A_CPU').html(result[i]["CONDITION_VALUE"]+"%");
								 JsVar["A_CPU"] = result[i]["CONDITION_VALUE"];
						 }else if(result[i]["QUOTA_TYPE"] == '2'){
								 $('#A_MEM').html(result[i]["CONDITION_VALUE"]+"%");
								 JsVar["#A_MEM"] = result[i]["CONDITION_VALUE"];
						 }
						 else if(result[i]["QUOTA_TYPE"] == '3'){
								 $('#A_DISK').html(result[i]["CONDITION_VALUE"]+"%");
								 JsVar["A_DISK"] = result[i]["CONDITION_VALUE"];
						 }else{
								 $('#A_BUSS_VOLUME').html(result[i]["CONDITION_VALUE"]);
								 JsVar["A_BUSS_VOLUME"] = result[i]["CONDITION_VALUE"];
						 }
					 }
					 if(isNull($('#A_CPU').html())){
						 $('#A_CPU').html("未设置");
					 }else{
						 if(JsVar["data"]["CPU"] -JsVar["A_CPU"]<= 0){
							 $('#A_CPU_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/error.net.png" />');
						 }else{
							 $('#A_CPU_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/success.net.png" />');
						 }
					 }
					 if(isNull($('#A_MEM').html())){
						 
						 $('#A_MEM').html("未设置");
					 }
					 else{
						 if(JsVar["data"]["MEM"] -JsVar["A_MEM"]<= 0){
							 $('#A_MEM_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/error.net.png" />');
						 }else{
							 $('#A_MEM_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/success.net.png" />');
						 }
					 }
					 if(isNull($('#A_DISK').html())){
						 $('#A_DISK').html("未设置");
					 }
					 else{
						 if(JsVar["data"]["DISK"] -JsVar["A_DISK"]<= 0){
							 $('#A_DISK_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/error.net.png" />');
						 }else{
							 $('#A_DISK_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/success.net.png" />');
						 }
					 }
					 if(isNull($('#A_BUSS_VOLUME').html())){
						 $('#A_BUSS_VOLUME').html("未设置");
					 }
					 else{
						 if(JsVar["data"]["BUSS_VOLUME"] -JsVar["A_BUSS_VOLUME"]<= 0){
							 $('#A_BUSS_VOLUME_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/error.net.png" />');
						 }else{
							 $('#A_BUSS_VOLUME_RESULT').html( '<img src="'+Globals.ctx+'/images/instIcon/success.net.png" />');
						 }
					 }
		        },"expendStrategyConfig.queryConfigList");
	 
}

 