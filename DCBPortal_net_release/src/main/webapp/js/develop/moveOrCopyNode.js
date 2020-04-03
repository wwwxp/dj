//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["fileForm"] = new mini.Form("fileForm");
});



/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
    if(JsVar[systemVar.ACTION] == systemVar.EDIT){
       editFile();
        return;
    }
    addFile();
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    var obj=new Object();
    obj["directory"]=data["directory"];
    obj["path"]=data["path"];
    obj["parentPath"]=data["parentPath"];
    obj["type"]=data["type"];
    JsVar["fileData"]=obj;
    
    if(data.type == 'directory'){
    	$("#directory").removeAttr("style");
    }else if(data.type == 'file'){
    	$("#file").removeAttr("style");
    }else if(data.type == 'topology'){
    	$("#topology").removeAttr("style");
    }
    if (action == systemVar.EDIT){
    	 obj["fileName"]=data["name"];
    }
    
     JsVar["fileForm"].setData(JsVar["fileData"]);
}

/**
 * 新增文件或目录
 */
function addFile(){
    var addData = JsVar["fileForm"].getData();
   
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    JsVar["fileData"]["fileName"]=addData["fileName"];
    getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_CREATE_DIRECTORY_OR_FILE_URL, JsVar["fileData"],"策略配置管理-新增段落动作",
            function(result){
    	    closeWindow(systemVar.SUCCESS);
            });
    
}
/**
 * 修改文件名或目录名
 */
function editFile()
{
    var editData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false) {
        return;
    }
    JsVar["fileData"]["fileName"]=editData["fileName"];
    getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_RENAME_DIRECTORY_OR_FILE_URL, JsVar["fileData"],"二次开发管理-重命名",
        function(result){
            closeWindow(systemVar.SUCCESS);
        });
}

/**
 * 取消
 * @param e
 */
function onCancel(e){
    closeWindow();
}


