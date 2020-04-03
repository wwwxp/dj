
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
	 JsVar["envForm"] = new mini.Form("#envForm");
    mini.parse();
    //集群下拉控件
    JsVar["BUS_CLUSTER_ID"] = mini.get("BUS_CLUSTER_ID");
});

function onLoadComplete(action,data) {
	
	//初使化下拉数据
    initCombox();
	//设置参数
	JsVar[systemVar.ACTION] = action;
    JsVar["info"] = data;
    if(JsVar[systemVar.ACTION] == "edit"){
	    JsVar["envForm"].setData(JsVar["info"]);
	    if(!JsVar["info"]["BUS_CLUSTER_ID"]){
	    	JsVar["BUS_CLUSTER_ID"].select(0);
	    }
    } else {
    	JsVar["BUS_CLUSTER_ID"].select(0);
    }
}
function onSubmit(){
	 if(JsVar[systemVar.ACTION] == systemVar.EDIT){
		 edit();
	        return;
	    }
	    add();
}

/**
 * 查询集群列表数据
 */
function initCombox(){
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"",
			function(result){
				if(result.length>0){
					result.unshift({"BUS_CLUSTER_NAME":"公用，所有集群拥有","BUS_CLUSTER_ID":""});
					JsVar["BUS_CLUSTER_ID"].setData(result);
					//JsVar["BUS_CLUSTER_ID"].select(0);
				}
		},"busMainCluster.queryBusMainClusterListByState", null, false);
}

//新增
function add()
{
    var info = JsVar["envForm"].getData();
    JsVar["envForm"].validate();
    if (JsVar["envForm"].isValid() == false){
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL,[info],"环境变量管理-新增",
        function success(result){
            closeWindow(systemVar.SUCCESS);
       },"environments.insertEnv");
}

//修改 
function edit()
{
    var info = JsVar["envForm"].getData();
    JsVar["envForm"].validate();
    info["ID"] = JsVar["info"]["ID"];
    if (JsVar["envForm"].isValid() == false) {
        return;
    }
    getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[info],"环境变量管理-修改",
        function(result){
            closeWindow(systemVar.SUCCESS);
        },"environments.updateEnv");
} 
