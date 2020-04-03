//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    JsVar["processForm"] =  new mini.Form("#processForm");
    
    JsVar["processAbstractGrid"] = mini.get("processAbstractGrid");//取得任务表格
    JsVar["processGrid"] = mini.get("processGrid");//取得任务表格
    JsVar["processPanel"] = mini.get("processPanel");//取得任务表格
//    clearInterval(JsVar["timeTicket_processGrid"]);
//    JsVar["timeTicket_processGrid"] = setInterval(search,5000);
    queryProcAbtract();
});
function abstractGridClick(){
	queryProcInfo();
	var row = JsVar["processAbstractGrid"].getSelected();
	JsVar["processAbstractGrid_select"] = row;
	JsVar["processPanel"].setTitle( "主机："+row["HOST"]);
	queryProcAbtract();
}

function abstractGridOnload(e){
	var isSelected = false;
	if(JsVar["processAbstractGrid_select"]){
		for(var i=0;i<e["data"].length;i++){
			if(JsVar["processAbstractGrid_select"]["HOST"] == e["data"][i]["HOST"]){
				this.select(i);
				isSelected = true;
				break;
			}
		}
	}
	
	if(!isSelected){
		this.select(0);
		queryProcInfo();
		var row = JsVar["processAbstractGrid"].getSelected();
		JsVar["processPanel"].setTitle( "主机："+row["HOST"]);
	}
	
	
}

function queryProcInfo(){
	var formData =JsVar["processForm"].getData();
	var params = JsVar["processAbstractGrid"].getSelected();
	 params["PROC_NAME"]=formData["PROC_NAME"];
	 params = mini.clone(params);
	 
	datagridLoadPage(JsVar["processGrid"],params,"monitorMapper.queryDcf_proc_info");
}

function queryProcAbtract(){
	var params =JsVar["queryForm"].getData();
	datagridLoadPage(JsVar["processAbstractGrid"],params,"monitorMapper.queryProcAbstract");
	
}
