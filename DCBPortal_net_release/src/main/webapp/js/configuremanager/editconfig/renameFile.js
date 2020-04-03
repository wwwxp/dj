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
    JsVar["fileForm"].setData(data);
}

/**
 * 重命名
 * @param e
 */
function addFile(){
    var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    JsVar["data"]["newFileName"]=addData["fileName"];
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_RENAME_FILE_URL, JsVar["data"],"配置修改-重命名文件",
        function(result){
    	if(result.successNum!=undefined && result.errorNum!=undefined){
			if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
				showWarnMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:JsVar["data"]["newFileName"]});
	    		});
			}else if(result.successNum>=0 && result.errorNum<=0){//只有有成功  --成功
				showMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:JsVar["data"]["newFileName"]});
	    		});
			}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
				showErrorMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:JsVar["data"]["newFileName"]});
	    		});
			}
		}else{
			showMessageAlter(result.isSuccess,function ok(){
    			closeWindow({flag:systemVar.SUCCESS,fileName:JsVar["data"]["newFileName"]});
    		});
		}
	});
}




