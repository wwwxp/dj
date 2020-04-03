
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
	mini.parse();
	 JsVar["addEditForm"] = new mini.Form("#addEditForm");
	 JsVar["taskForm"] = new mini.Form("#taskForm");
	 JsVar["TASK_STATUS"] = mini.get("TASK_STATUS");

	 //任务调度类型Change事件
    mini.get("TRIGGER_TYPE").on("valuechanged", function(e) {
    	onChangeValue(e);
    });

});

function onLoadComplete(action,data) {
	//设置参数
	JsVar[systemVar.ACTION] = action;
    JsVar["info"] = data;
    if(JsVar[systemVar.ACTION] == "edit"){
    	 
	    JsVar["addEditForm"].setData(JsVar["info"]);
	    setTaskForm(JsVar["info"]);
    }else {
    	JsVar["TASK_STATUS"].select(0);
    	$("#intervalExp").show();
		$("#offsetTip").hide();
		$("#offsetExp").hide();
        //新增则使用参数模板
        getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, null, null, function (result){
            JsVar["addEditForm"].setData(result);
        }, "joblist.selectLastRecord");
    }
}
//给时间设值
function setTaskForm(result){
	 var taskType = result.TASK_TYPE;
     var cron_exp = result.CRON_EXP;
     var tips = result.CRON_DESC;
     mini.get("TRIGGER_TYPE").setValue(taskType);
     JsVar["TASK_STATUS"].setValue(result.TASK_STATUS);
     if (taskType == '2') {//cron
    	 $("#intervalExp").show();
 		$("#offsetTip").hide();
 		$("#offsetExp").hide();
 		
 		mini.get("CRON_START_TIME").setRequired(true);
 		mini.get("CRON_END_TIME").setRequired(true);
 		$("#timeExp").show();
 		
     	mini.get("intervalSecond").setValue(cron_exp);
     	mini.get("CRON_START_TIME").setValue(result.CRON_START_TIME);
     	mini.get("CRON_END_TIME").setValue(result.CRON_END_TIME);

     	
     } else if (taskType == '1') {//simple
    	 $("#offsetTip").show();
 		$("#offsetExp").show();
 		$("#intervalExp").hide();
 		
 		mini.get("CRON_START_TIME").setRequired(false);
 		mini.get("CRON_END_TIME").setRequired(false);

 		$("#timeExp").hide();

 		
		mini.get("offsetTip").setValue(tips);
		mini.get("offsetTip").setText(tips);
 		mini.get("offset").setValue(cron_exp);
     	//mini.get("CRON_START_TIME").setValue(result.CRON_START_TIME);
     	//mini.get("CRON_END_TIME").setValue(result.CRON_END_TIME);
     }else{
         $("#offsetTip").hide();
         $("#timeExp").hide();
         $("#offsetExp").hide();
         $("#intervalExp").hide();
         mini.get("offsetTip").setRequired(false);
         mini.get("CRON_START_TIME").setRequired(false);
         mini.get("CRON_END_TIME").setRequired(false);
         mini.get("intervalSecond").setRequired(false);
	 }
     mini.get("TASK_JOB_CLASS").setEnabled(false);
     mini.parse();
}


/**
 * 下拉选项Change事件
 * @param e
 */
function onChangeValue(e) {
	var triggerType = e.value;
	if (triggerType == "2") {//循环任务
		$("#intervalExp").show();
		$("#offsetTip").hide();
		$("#offsetExp").hide();
		
		mini.get("CRON_START_TIME").setRequired(true);
		mini.get("CRON_END_TIME").setRequired(true);
        $("#timeExp").show();
	} else if(triggerType == "1"){//自定义任务
		$("#offsetTip").show();
		$("#offsetExp").show();
		$("#intervalExp").hide();
		
		mini.get("CRON_START_TIME").setRequired(false);
		mini.get("CRON_END_TIME").setRequired(false);

        $("#timeExp").hide();
	}else{
        $("#offsetTip").hide();
        $("#timeExp").hide();
        $("#offsetExp").hide();
        $("#intervalExp").hide();
        mini.get("offsetTip").setRequired(false);
        mini.get("CRON_START_TIME").setRequired(false);
        mini.get("CRON_END_TIME").setRequired(false);
        mini.get("intervalSecond").setRequired(false);
	}
	mini.parse();
}

/**
 *弹出周期选择框
 */
function showCycleWindow(){
	var param={};
	param.cron=mini.get("offset").getValue();
	param.cron_text = mini.get("offsetTip").getValue();
	showDialog("偏移量设置", 850, 600,Globals.baseJspUrl.JOBTIMERTASKCFG_JSP_ADD_CRON_URL,function destroy(data){
		if(data != undefined && data.action == systemVar.SUCCESS){
			mini.get("offset").setValue(data.cron);
			mini.get("offsetTip").setValue(data.cron_text);
			mini.get("offsetTip").setText(data.cron_text);
		}
	}, param);
}


function onSubmit(){
	 if(JsVar[systemVar.ACTION] == systemVar.EDIT){
		 edit();
	        return;
	    }
	    add();
}


//新增
function add()
{
	//自定义调度模式需要设置开始时间、结束时间为空，循环调度模式设置Cron表达式为空
	var triggerType = mini.get("TRIGGER_TYPE").getValue();

    //任务调度校验
    JsVar["taskForm"].validate();
    if (JsVar["taskForm"].isValid() == false){
        return;
    }
    
    JsVar["addEditForm"].validate();
    if (JsVar["addEditForm"].isValid() == false){
        return;
    }
    var info = JsVar["addEditForm"].getData();
    var taskParams = JsVar["taskForm"].getData(true);
    info["TASK_TYPE"] = mini.get("TRIGGER_TYPE").getValue();
    info["TASK_STATUS"]=mini.get("TASK_STATUS").getValue();
    //循环任务
    if (info["TASK_TYPE"] == '2') {
    	info["CRON_EXP"] = mini.get("intervalSecond").getValue();
    	info["CRON_DESC"] = "每隔: " + info["CRON_EXP"] + "秒执行一次";
    	info["CRON_START_TIME"] = taskParams["CRON_START_TIME"];
    	info["CRON_END_TIME"] = taskParams["CRON_END_TIME"];
    } 
    //自定义任务
    else if (info["TASK_TYPE"] == '1') {
    	info["CRON_EXP"] = mini.get("offset").getValue();
    	info["CRON_DESC"] = mini.get("offsetTip").getValue();
    	
    }
    info["TASK_DESC"] = mini.get("TASK_DESC").getValue();

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,info,"任务管理-新增",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        },"joblist.insertJob");
	
}

//修改 
function edit()
{
	//自定义调度模式需要设置开始时间、结束时间为空，循环调度模式设置Cron表达式为空
	var triggerType = mini.get("TRIGGER_TYPE").getValue();

	//任务调度校验
    JsVar["taskForm"].validate();
    if (JsVar["taskForm"].isValid() == false){
        return;
    }
    
    JsVar["addEditForm"].validate();
    if (JsVar["addEditForm"].isValid() == false){
        return;
    }
    
    var info = JsVar["addEditForm"].getData();
    var taskParams = JsVar["taskForm"].getData(true);
    
    info["ID"] = JsVar["info"]["ID"];
    
    info["TASK_TYPE"] = mini.get("TRIGGER_TYPE").getValue();
    info["TASK_STATUS"]=mini.get("TASK_STATUS").getValue();
    //循环任务
    if (info["TASK_TYPE"] == '2') {
    	info["CRON_EXP"] = mini.get("intervalSecond").getValue();
    	info["CRON_DESC"] = "每隔: " + info["CRON_EXP"] + "秒执行一次";
    	info["CRON_START_TIME"] = taskParams["CRON_START_TIME"];
    	info["CRON_END_TIME"] = taskParams["CRON_END_TIME"];
    } 
    //自定义任务
    else if (info["TASK_TYPE"] == '1') {
    	info["CRON_EXP"] = mini.get("offset").getValue();
    	info["CRON_DESC"] = mini.get("offsetTip").getValue();

    }
    info["TASK_DESC"] = mini.get("TASK_DESC").getValue();
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,info,"常规任务管理-新增",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        },"joblist.updateTask");

} 
