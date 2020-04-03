//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["fileForm"] = new mini.Form("fileForm");
});

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    JsVar["data"]=data;
    loadDefaultFile();
    loadLatnList();
    
    
}
/**
 * 获取本地网
 */
function loadLatnList(){
	comboxLoad(mini.get("LATN_ID"), {GROUP_CODE:'LATN_LIST'}, "config.queryConfigList");
}

/**
 * 获取模板目录latn下的文件列表
 */
function loadDefaultFile(){
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_LOAD_FILE_UNDER_GIVEN_PATH_URL, JsVar["data"],"配置修改-新建实例--获取可选文件",
        function(result){
	    	if(result!=null && result.length>0 ){
	    		var data = [];
	    		for(var i = 0 ; i < result.length ;i++){
	    			if(result[i]["fileType"]!='D'){
	    				data.push(result[i]);
	    			}
	    			
	    		}
	    		//给可选文件下拉框注值
	    		mini.get("copyFilesNames").setData(data);
	    	}
    });
}


/**
 * 新增
 * @param e
 */
function addFile(){
    //是新建文件
    var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    var latnids =  mini.get("LATN_ID").getValue();
    var latnnames =  mini.get("LATN_ID").getText();
    var params = {};
    JsVar["data"]["copyFilesNames"] = mini.get("copyFilesNames").getValue();
    /*for (var i = 0 ; i < latnids.length ;i++){
    	params.push({FILE_PATH:JsVar["data"]["fullPath"],FILE_NAMES:JsVar["data"]["copyFilesNames"]});
    }*/
    params.FILE_PATH = JsVar["data"]["filePath"];
    params.FULL_PATH = JsVar["data"]["targetPath"];
    
    params.FILE_NAMES =  JsVar["data"]["copyFilesNames"];
    params.LATNS = latnids;
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_BATCH_BUSS_FILE_URL, params,"配置修改-新建文件",
        function(result){
	    	
	    	 
	    			closeWindow({flag:systemVar.SUCCESS,LATNS: latnids,FILE_NAMES: params.FILE_NAMES,LATN_NAMES:latnnames});
	    		 
			
        });
}




