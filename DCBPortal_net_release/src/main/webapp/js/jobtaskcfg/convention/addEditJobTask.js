
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
    
	 //任务类型Change事件
	    mini.get("EXEC_TYPE").on("valuechanged", function(e) {
	    	onExecTypeChanage(e);
	    });
	    
	    //加载下拉框
	    loadCombo();
});

function loadCombo(){
	comboxLoad(mini.get('#CMD_DESC'), {}, "jobtaskcfg.queryCmdList");
	comboxLoad(mini.get('#DATASOURCE_DESC'), {}, "jobtaskcfg.queryDatasourceList");
	comboxLoad(mini.get('#HOST_ID'), {}, "host.queryHostList");
}


function onLoadComplete(action,data) {
	
	 
	//设置参数
	JsVar[systemVar.ACTION] = action;
    JsVar["info"] = data;
    if(JsVar[systemVar.ACTION] == "edit"){
	    JsVar["addEditForm"].setData(JsVar["info"]);
	    onExecTypeChanage({value:JsVar["info"]["EXEC_TYPE"]});
	    mini.get('#CMD_DESC').setValue(JsVar["info"]["TASK_JOB_PARAMS"]);
	     mini.get('#DATASOURCE_DESC').setValue(JsVar["info"]["TASK_JOB_PARAMS"]) ;
    }else{
    	onExecTypeChanage({value:'cmd'});
    }  
}
function onSubmit(){
	 if(JsVar[systemVar.ACTION] == systemVar.EDIT){
		 edit();
	        return;
	    }
	    add();
}

//选择任务类型
function onExecTypeChanage(e){
	var triggerType = e.value;
	if(triggerType == 'cmd'){
		$("#cmd_flag").show();
		$("#datasource_flag").hide();
		$('#datasource_sql_flag').hide();
		mini.get("DATASOURCE_DESC").setRequired(false);
	}else{
		$("#datasource_flag").show();
		$('#datasource_sql_flag').show();
		$("#cmd_flag").hide();
		mini.get("CMD_DESC").setRequired(false);
		mini.get("HOST_ID").setRequired(false);
	}
	mini.parse();
}
//新增
function add()
{
    var info = JsVar["addEditForm"].getData();
    info["TASK_TYPE"]= "1";
    var EXEC_TYPE = mini.get("EXEC_TYPE").getValue();
	if (EXEC_TYPE == 'cmd') {
		mini.get("DATASOURCE_DESC").setRequired(false); 
		 
	} else {
		mini.get("CMD_DESC").setRequired(false); 
		mini.get("HOST_ID").setRequired(false);
	}
	
    JsVar["addEditForm"].validate();
    if (JsVar["addEditForm"].isValid() == false){
        return;
    }
    
    info["EXEC_TYPE"] = mini.get("EXEC_TYPE").getValue();
	if ( info["EXEC_TYPE"] == 'cmd') {
		info["TASK_JOB_PARAMS"] = mini.get("CMD_DESC").getValue();
		info["TASK_CONTENT"] = "";
		info["TASK_JOB_CLASS"]='com.tydic.quartz.CommandQuartz';
	} else {
		info["HOST_ID"] = "";
		info["TASK_JOB_CLASS"]='com.tydic.quartz.DataSourceQuartz';
		info["TASK_JOB_PARAMS"] = mini.get("DATASOURCE_DESC").getValue();
	}

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,info,"常规任务管理-新增",
        function success(result){
            if(result["COUNT"]>0){
            	showErrorMessageAlter("常规任务与定时任务不能存在重复任务名称");
			}else{
                getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL,[info],"常规任务管理-新增",
                    function success(result){
                        closeWindow(systemVar.SUCCESS);
                    },"jobtaskcfg.insertTask");
			}
        },"jobtaskcfg.queryTaskByTaskNameAndId");


}

//修改 
function edit()
{
	var EXEC_TYPE = mini.get("EXEC_TYPE").getValue();
	if (EXEC_TYPE == 'cmd') {
		mini.get("DATASOURCE_DESC").setRequired(false); 
		 
	} else {
		mini.get("CMD_DESC").setRequired(false); 
		mini.get("HOST_ID").setRequired(false);
	}
	
    var info = JsVar["addEditForm"].getData();
    JsVar["addEditForm"].validate();
    info["ID"] = JsVar["info"]["ID"];
    if (JsVar["addEditForm"].isValid() == false) {
        return;
    }
    info["EXEC_TYPE"] = mini.get("EXEC_TYPE").getValue();
	if ( info["EXEC_TYPE"] == 'cmd') {
		info["TASK_JOB_PARAMS"] = mini.get("CMD_DESC").getValue();
		info["TASK_CONTENT"] = "";
		info["TASK_JOB_CLASS"]='com.tydic.quartz.CommandQuartz';
	} else {
		info["HOST_ID"] = "";
		info["TASK_JOB_CLASS"]='com.tydic.quartz.DataSourceQuartz';
		info["TASK_JOB_PARAMS"] = mini.get("DATASOURCE_DESC").getValue();
	}
	info["TASK_TYPE"] = '1';
	
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,info,"常规任务管理-新增",
        function success(result){
            if(result["COUNT"]>0){
                showErrorMessageAlter("常规任务与定时任务不能存在重复任务名称");
            }else{
                getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[info],"常规任务管理-修改",
                    function(result){
                        closeWindow(systemVar.SUCCESS);
                    },"jobtaskcfg.updateTask");
            }
        },"jobtaskcfg.queryTaskByTaskNameAndId");


} 
