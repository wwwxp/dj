//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    
    //程序管理表单
    JsVar["programForm"] = new mini.Form("programForm");
   
});

 
 

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
	JsVar["data"] = data;
	JsVar["version"] = data["VERSION"]; 
	 
	//程序类型
	loadProgramName();
	//加载配置文件
    loadConfigList(); 
    //记载当前集群主机列表
    loadClusterHostList();
    //本地网
    loadLatnList();
    
    mini.parse();
}

function initData(data){
	mini.get("PROGRAM_NAME").setValue(data["PROGRAM_CODE"]);
	mini.get("PROGRAM_ALIAS").setValue(data["PROGRAM_ALIAS"]);
	mini.get("HOST_ID").setValue(data["HOST_ID"]);
	mini.get("LATN_ID").setValue(data["LATN_ID"]);
	if(data["CONFIG_FILE"]){
		var values = "";
		var files = data["CONFIG_FILE"].split(",");
		for(var i = 0 ; i < files.length ;i++){
			for(var j = 0 ;j < JsVar["CONFIG_FILE_LIST"].length;j++){
				if(files[i] == JsVar["CONFIG_FILE_LIST"][j]["fileName"]){
					values += JsVar["CONFIG_FILE_LIST"][j]["targetPath"] +",";
					break;
				}
			}
			
		}
		if(values.lastIndexOf(',')> 0){
			values = values.substring(0,values.length-1);
		}
		mini.get("CONFIG_FILE").setValue(values);
	}
	
	mini.get("SCRIPT_SH_NAME").setValue(data["SCRIPT_SH_NAME"]);
	mini.get("PROGRAM_DESC").setValue(data["PROGRAM_DESC"]);
	
}

function loadLatnList(){
	comboxLoad(mini.get("LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
}

 
  

/**
 * 加载当前集群主机列表
 */
function loadClusterHostList() {
	var params = {
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"]
	};
	comboxLoad(mini.get("HOST_ID"), params, "deployHome.queryDeployHostByCodeAndHost");
}

/**
 * 加载程序类型
 */
function loadProgramName() {
	var params = {
		PROGRAM_TYPE:JsVar["data"]["CLUSTER_TYPE"]
	};
	var programObj = mini.get("PROGRAM_NAME");
	comboxLoad(programObj, params, "programDefine.queryProgramDefineList", "", "", false);
	//comboxLoad(mini.get("QUERY_PROGRAM_NAME"), params, "programDefine.queryProgramDefineList", "", "", false);
	
	var list = programObj.getData();
	if (list != null && list.length > 0) {
		mini.get("PROGRAM_NAME").select(0);
		mini.get("SCRIPT_SH_NAME").setValue(list[0]["SCRIPT_SH_NAME"]);
		JsVar["SCRIPT_SH_NAME"] = list[0]["SCRIPT_SH_NAME"];
		JsVar["PROGRAM_NAME"] = list[0]["PROGRAM_NAME"];
		JsVar["PROGRAM_CODE"] = list[0]["PROGRAM_CODE"];
		JsVar["PROGRAM_STATE"] = list[0]["PROGRAM_STATE"];
		//$("#exampleSh").html(list[0]["SCRIPT_SH_EXAMPLE"]);
	}
}

/**
 * 脚本
 * @param e
 */
function changeProgramType(e) {
	var scriptShName = e.selected.SCRIPT_SH_NAME;
	mini.get("SCRIPT_SH_NAME").setValue(scriptShName);
	JsVar["PROGRAM_NAME"] = e.selected["PROGRAM_NAME"];
	JsVar["PROGRAM_CODE"] = e.selected["PROGRAM_CODE"];
	JsVar["PROGRAM_STATE"] = e.selected["PROGRAM_STATE"];
	mini.get("CONFIG_FILE").setValue("");
	//$("#exampleSh").html(e.selected["SCRIPT_SH_EXAMPLE"]);
	initData(JsVar["data"]);
}

/**
 * 选择配置文件
 */
function changeConfigFile(e) {
	var configList = e.value;
	if(isEmptyStr(configList)){
		mini.get("SCRIPT_SH_NAME").setValue(JsVar["SCRIPT_SH_NAME"]);
		return;
	}
	configList = configList.split(",");
	var fileNames = "";
	for (var i=0; i<configList.length; i++) {
			var fileName =   configList[i].replace(JsVar["configFilePath"],"");
			fileNames += "$P/" + fileName + ",";
	}
	if(fileNames.lastIndexOf(",") > 0){
		fileNames = fileNames.substring(0,fileNames.length-1);
	}
	var scriptShName = mini.get("SCRIPT_SH_NAME").getValue();
	if (scriptShName != null && scriptShName != '') {
		var newScriptShName = scriptShName.substr(0, scriptShName.indexOf("\"") + 1);
		var finalNewName = newScriptShName + fileNames + "\"";
		mini.get("SCRIPT_SH_NAME").setValue(finalNewName);
	} else {
		mini.get("SCRIPT_SH_NAME").setValue(fileNames);
	}
}

/**
 * 选择本地网时发生
 * @param e
 */
function changeLatnId(e){
	JsVar["COMBOX_LATN_ID"] =e.value;
	mini.get("SCRIPT_SH_NAME").setValue(JsVar["SCRIPT_SH_NAME"]);
	mini.get("CONFIG_FILE").setValue("");
	loadConfigList();
}


/**
 * 加载配置文件列表
 */
function loadConfigList() {
	var params = {
		CLUSTER_ID:JsVar["data"]["CLUSTER_ID"],
		CLUSTER_TYPE:JsVar["data"]["CLUSTER_TYPE"],
		VERSION:JsVar["data"]["VERSION"],
		HOST_ID:JsVar["data"]["HOST_ID"],
		HOST_IP:JsVar["data"]["HOST_IP"],
		TASK_ID:JsVar["data"]["TASK_ID"],
		TASK_CODE:JsVar["data"]["TASK_CODE"],
		BUS_CLUSTER_ID:JsVar["data"]["BUS_CLUSTER_ID"],
		NAME:JsVar["data"]["NAME"],
		PACKAGE_TYPE:JsVar["data"]["PACKAGE_TYPE"],
		fileFlag:"ALL"
	};
	if(JsVar["COMBOX_LATN_ID"]){
		params["LATN_ID"] = JsVar["COMBOX_LATN_ID"];
	}
	getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_FILES_ACTION_MANAGE_URL, params, "程序启停管理-获取不分IP启停程序配置文件",
    		function success(result){
    			if (result != null) {
    				//mini.get("CONFIG_FILE").setData(result["FILES_LIST"]);
    				mini.get("CONFIG_FILE").tree.loadList(result["FILES_LIST"], "currId", "parentId");
    				JsVar["CONFIG_FILE_LIST"] = result["FILES_LIST"];
    				JsVar["configFilePath"] = result["configFilePath"];
    			}
           	},null,null,false);
}

   

/**
 * 添加运行程序
 */
function addProgram() {
	var programForm = JsVar["programForm"].getData();
    JsVar["programForm"].validate();
    if (JsVar["programForm"].isValid() == false){
        return;
    }
    programForm["CLUSTER_ID"] = JsVar["data"]["CLUSTER_ID"];
    programForm["PROGRAM_TYPE"] = JsVar["data"]["CLUSTER_TYPE"];
    programForm["CLUSTER_TYPE"] = JsVar["data"]["CLUSTER_TYPE"],
    programForm["TASK_ID"] = JsVar["data"]["TASK_ID"];
    programForm["PROGRAM_NAME"] = JsVar["data"]["PROGRAM_NAME"];
    programForm["PROGRAM_CODE"] = JsVar["data"]["PROGRAM_CODE"];
    programForm["BUS_CLUSTER_ID"] = JsVar["data"]["BUS_CLUSTER_ID"];
    programForm["VERSION"] = JsVar["version"];
    programForm["actionFlag"] = "copy";
    programForm["CONFIG_FILE"] = programForm["CONFIG_FILE"].replace(JsVar["configFilePath"],"");
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_TASK_ADD_ACTION_MANAGE_URL, programForm, "程序启停管理-添加不分IP启停程序实例",
		function success(result){
    		if (result != null && result["RST_CODE"] == busVar.SUCCESS) {
    			 closeWindow(systemVar.SUCCESS);
    		}
       	}
    );
}
 

/**
 * 新增启停程序表单重置
 */
function reset() {
	JsVar["programForm"].reset();
	//程序类型
	loadProgramName();
	//加载配置文件
    loadConfigList();
}

   


