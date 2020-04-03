/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
	mini.parse();
	 
});
function checkBtnTrue(){
	 
	 if(JsVar["data"]["EXEC_STATUS"]==0 || JsVar["data"]["EXEC_STATUS"]==2){
		 mini.get("btnA").setEnabled(true);
		 mini.get("btnB").setEnabled(true);
	 }
}
function checkBtnFalse(){
	 if(JsVar["data"]["EXEC_STATUS"]== 2){
		 //mini.get("btnA").setEnabled(false);
		 mini.get("btnB").setEnabled(false);
	 }
	 if(JsVar["data"]["EXEC_STATUS"]==1){
		 mini.get("btnA").setEnabled(false);
		 mini.get("btnB").setEnabled(false);
	 }
}

function execjob(){
	 mini.get("btnA").setEnabled(false);
	 mini.get("btnB").setEnabled(false);
	 execManualJobKS();
}
function timerExecjob(){
	 mini.get("btnA").setEnabled(false);
	 mini.get("btnB").setEnabled(false);
	 execJobKS();
}
function onLoadComplete(data) {
	JsVar["data"] = data;
	 $('#CPU').html(JsVar["data"]["CPU"]+"%");
	 $('#MEM').html(JsVar["data"]["MEM"]+"%");
	 $('#DISK').html(JsVar["data"]["DISK"]+"%");
	 $('#BUSS_VOLUME').html(JsVar["data"]["BUSS_VOLUME"]);
	 $('#PREDICTION_TIME').html(mini.formatDate(JsVar["data"]["PREDICTION_TIME"],"yyyy-MM-dd"));
	 $('#CRT_DATE').html(mini.formatDate(JsVar["data"]["CRT_DATE"],"yyyy-MM-dd HH:mm:ss"));
	 $('#ADVISE_NODE_COUNT').html(JsVar["data"]["ADVISE_NODE_COUNT"]);
	 checkBtnFalse();
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

/**
 * 新增定时伸缩配置
 * @param type
 */
function execManualJobKS() {
	 
	var params = {
		OPERTOR_TYPE:systemVar.ADD,
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		TASK_PROGRAM_ID:JsVar["data"]["TASK_PROGRAM_ID"],
		STRATEGY_ID:JsVar["data"]["STRATEGY_ID"],
		THRESHOLD_TYPE:'unexpend',
		ID:JsVar["data"]["ID"],
		FORECAST_REPORT_ID:JsVar["data"]["FORECAST_REPORT_ID"]
	};
	showAddDialog("立刻收缩吗", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_MANUAL_DYNAMIC_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	 
	                //showMessageAlter("新增定时" + typeName + "策略成功！");
	            	showMessageTips("立即执行任务成功，任务正在执行中！");
	            }else{
	            	checkBtnTrue();
	            }
	    }, params);
}
function execJobKS(){
	var params = {
		OPERTOR_TYPE:systemVar.ADD,
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		TASK_PROGRAM_ID:JsVar["data"]["TASK_PROGRAM_ID"],
		STRATEGY_ID:JsVar["data"]["STRATEGY_ID"],
		THRESHOLD_TYPE:'unexpend',
		ID:JsVar["data"]["ID"],
		FORECAST_REPORT_ID:JsVar["data"]["FORECAST_REPORT_ID"]
	};
	showAddDialog("定时收缩吗", 600, 420, Globals.baseJspUrl.NODE_STRATEGY_TIMING_DYNAMIC_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	 
	                //showMessageAlter("新增定时" + typeName + "策略成功！");
	            	showMessageTips("新增定时收缩任务成功！");
	            }else{
	            	checkBtnTrue();
	            }
	    }, params);
}

 